package pattern;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages all sound effects and background music.
 * - SFX: synthesized tones (no external files needed)
 * - BGM: loaded from bundled WAV resource
 */
public class SoundManager {

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    // Separate toggles for SFX and BGM
    private static boolean sfxEnabled = true;
    private static boolean bgmEnabled = true;

    // Background music state
    private static Thread bgmThread;
    private static volatile boolean bgmRunning = false;
    private static final Object bgmLock = new Object();

    // ── Toggle controls ───────────────────────────────────────
    public static void setSfxEnabled(boolean b) { sfxEnabled = b; }
    public static boolean isSfxEnabled()        { return sfxEnabled; }

    public static void setBgmEnabled(boolean b) {
        bgmEnabled = b;
        if (bgmEnabled) {
            startBgm();
        } else {
            stopBgm();
        }
    }
    public static boolean isBgmEnabled() { return bgmEnabled; }

    // ── BGM control ───────────────────────────────────────────
    public static void startBgm() {
        if (!bgmEnabled) return;
        stopBgm(); // stop if already running
        bgmRunning = true;
        bgmThread = new Thread(() -> {
            while (bgmRunning && bgmEnabled) {
                try {
                    playBgmOnce();
                } catch (Exception e) {
                    break;
                }
            }
        }, "BGM-Thread");
        bgmThread.setDaemon(true);
        bgmThread.start();
    }

    public static void stopBgm() {
        bgmRunning = false;
        if (bgmThread != null) {
            bgmThread.interrupt();
            bgmThread = null;
        }
    }

    private static void playBgmOnce() throws Exception {
        // Load WAV from classpath (bundled in JAR)
        InputStream is = SoundManager.class.getResourceAsStream("/dotoc2.wav");
        if (is == null) return;

        try (BufferedInputStream bis = new BufferedInputStream(is);
             AudioInputStream ais = AudioSystem.getAudioInputStream(bis)) {

            AudioFormat baseFormat = ais.getFormat();
            AudioFormat targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(), 16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(), false
            );

            AudioInputStream converted = AudioSystem.getAudioInputStream(targetFormat, ais);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, targetFormat);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(targetFormat);
            line.start();

            byte[] buf = new byte[4096];
            int bytesRead;
            while (bgmRunning && bgmEnabled &&
                   (bytesRead = converted.read(buf, 0, buf.length)) != -1) {
                line.write(buf, 0, bytesRead);
            }
            line.drain();
            line.close();
            converted.close();
        }
    }

    // ── SFX methods ───────────────────────────────────────────
    public static void playLock() {
        play(() -> tone(180, 60, 0.25f, WaveType.SQUARE, true));
    }
    public static void playSingle() {
        play(() -> { tone(400, 60, 0.35f, WaveType.SINE, false); tone(600, 80, 0.30f, WaveType.SINE, false); });
    }
    public static void playDouble() {
        play(() -> { tone(400,50,0.35f,WaveType.SINE,false); tone(600,50,0.35f,WaveType.SINE,false); tone(800,100,0.40f,WaveType.SINE,false); });
    }
    public static void playTriple() {
        play(() -> { tone(400,40,0.35f,WaveType.SINE,false); tone(600,40,0.35f,WaveType.SINE,false); tone(800,40,0.40f,WaveType.SINE,false); tone(1000,120,0.45f,WaveType.SINE,false); });
    }
    public static void playTetris() {
        play(() -> {
            tone(523,80,0.5f,WaveType.SINE,false);
            tone(659,80,0.5f,WaveType.SINE,false);
            tone(784,80,0.5f,WaveType.SINE,false);
            tone(1047,200,0.6f,WaveType.SINE,false);
            noise(100, 0.2f);
        });
    }
    public static void playLevelUp() {
        play(() -> { tone(523,80,0.4f,WaveType.SINE,false); tone(659,80,0.4f,WaveType.SINE,false); tone(784,80,0.4f,WaveType.SINE,false); tone(1047,150,0.5f,WaveType.SINE,false); });
    }
    public static void playMove()     { play(() -> tone(220, 25, 0.12f, WaveType.SQUARE, true)); }
    public static void playRotate()   { play(() -> tone(300, 30, 0.15f, WaveType.SQUARE, true)); }
    public static void playHardDrop() { play(() -> { sweep(800, 200, 60, 0.4f); tone(150, 80, 0.3f, WaveType.SQUARE, true); }); }
    public static void playGameOver() {
        play(() -> { tone(440,150,0.4f,WaveType.SINE,false); tone(349,150,0.4f,WaveType.SINE,false); tone(294,150,0.4f,WaveType.SINE,false); tone(220,400,0.5f,WaveType.SINE,false); });
    }

    // ── Synthesis helpers ─────────────────────────────────────
    private enum WaveType { SINE, SQUARE, SAW }

    private static void play(Runnable task) {
        if (!sfxEnabled) return;
        executor.submit(() -> { try { task.run(); } catch (Exception ignored) {} });
    }

    private static void tone(int freq, int durationMs, float vol, WaveType type, boolean decay) {
        try {
            int sr = 44100;
            int n = sr * durationMs / 1000;
            byte[] buf = new byte[n * 2];
            for (int i = 0; i < n; i++) {
                double t = (double) i / sr;
                double wave = switch (type) {
                    case SQUARE -> Math.sin(2*Math.PI*freq*t) >= 0 ? 1.0 : -1.0;
                    case SAW    -> 2.0*((freq*t)%1.0)-1.0;
                    default     -> Math.sin(2*Math.PI*freq*t);
                };
                double env = decay ? Math.max(0,1.0-(double)i/n) : 1.0;
                if (i < sr*0.005) env *= (double)i/(sr*0.005);
                short val = (short)(wave*env*vol*Short.MAX_VALUE);
                buf[i*2]   = (byte)(val & 0xFF);
                buf[i*2+1] = (byte)((val>>8) & 0xFF);
            }
            playBuffer(buf, sr);
        } catch (Exception ignored) {}
    }

    private static void sweep(int f0, int f1, int durationMs, float vol) {
        try {
            int sr = 44100;
            int n = sr * durationMs / 1000;
            byte[] buf = new byte[n * 2];
            for (int i = 0; i < n; i++) {
                double t = (double)i/sr, p = (double)i/n;
                double wave = Math.sin(2*Math.PI*(f0+(f1-f0)*p)*t);
                short val = (short)(wave*(1-p)*vol*Short.MAX_VALUE);
                buf[i*2]=(byte)(val&0xFF); buf[i*2+1]=(byte)((val>>8)&0xFF);
            }
            playBuffer(buf, sr);
        } catch (Exception ignored) {}
    }

    private static void noise(int durationMs, float vol) {
        try {
            int sr = 44100, n = sr*durationMs/1000;
            byte[] buf = new byte[n*2];
            java.util.Random rng = new java.util.Random();
            for (int i = 0; i < n; i++) {
                double env = 1.0-(double)i/n;
                short val = (short)((rng.nextDouble()*2-1)*env*vol*Short.MAX_VALUE);
                buf[i*2]=(byte)(val&0xFF); buf[i*2+1]=(byte)((val>>8)&0xFF);
            }
            playBuffer(buf, sr);
        } catch (Exception ignored) {}
    }

    private static void playBuffer(byte[] buf, int sr) throws Exception {
        AudioFormat fmt = new AudioFormat(sr, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(fmt); line.start();
        line.write(buf, 0, buf.length);
        line.drain(); line.close();
    }
}
