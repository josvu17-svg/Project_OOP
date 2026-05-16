package pattern;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages all sound effects and background music.
 * BGM uses Clip for smooth gapless looping.
 * SFX uses synthesized tones via SourceDataLine.
 */
public class SoundManager {

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private static boolean sfxEnabled = true;
    private static boolean bgmEnabled = true;
    private static float   sfxVolume  = 0.8f;
    private static float   bgmVolume  = 0.6f;

    // BGM uses Clip — handles looping internally, no thread needed
    private static Clip bgmClip = null;

    // ── BGM ───────────────────────────────────────────────────
    private static void ensureBgmLoaded() {
        if (bgmClip != null && bgmClip.isOpen()) return;
        try {
            InputStream is = SoundManager.class.getResourceAsStream("/dotoc2.wav");
            if (is == null) { System.err.println("BGM not found in JAR"); return; }
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is, 65536));
            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);
            ais.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void applyBgmVolume() {
        if (bgmClip == null) return;
        try {
            FloatControl ctrl = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = bgmVolume <= 0 ? -80f : (float)(20 * Math.log10(bgmVolume));
            ctrl.setValue(Math.max(ctrl.getMinimum(), Math.min(ctrl.getMaximum(), dB)));
        } catch (Exception ignored) {}
    }

    public static void startBgm() {
        if (!bgmEnabled) return;
        try {
            ensureBgmLoaded();
            if (bgmClip == null) return;
            applyBgmVolume();
            bgmClip.setFramePosition(0);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void stopBgm() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
        }
    }

    public static void setBgmEnabled(boolean b) {
        bgmEnabled = b;
        if (bgmEnabled) startBgm(); else stopBgm();
    }
    public static boolean isBgmEnabled() { return bgmEnabled; }

    public static void setBgmVolume(float v) {
        bgmVolume = Math.max(0, Math.min(1, v));
        applyBgmVolume();
    }
    public static float getBgmVolume()   { return bgmVolume; }
    public static int   getBgmVolume100(){ return (int)(bgmVolume * 100); }

    // ── SFX ───────────────────────────────────────────────────
    public static void setSfxEnabled(boolean b) { sfxEnabled = b; }
    public static boolean isSfxEnabled()        { return sfxEnabled; }

    public static void setSfxVolume(float v) { sfxVolume = Math.max(0, Math.min(1, v)); }
    public static float getSfxVolume()        { return sfxVolume; }
    public static int   getSfxVolume100()     { return (int)(sfxVolume * 100); }

    public static void playLock()     { play(() -> tone(180, 60,  0.25f, WaveType.SQUARE, true)); }
    public static void playMove()     { play(() -> tone(220, 25,  0.12f, WaveType.SQUARE, true)); }
    public static void playRotate()   { play(() -> tone(300, 30,  0.15f, WaveType.SQUARE, true)); }
    public static void playHardDrop() { play(() -> { sweep(800,200,60,0.4f); tone(150,80,0.3f,WaveType.SQUARE,true); }); }

    public static void playSingle() {
        play(() -> { tone(400,60,0.35f,WaveType.SINE,false); tone(600,80,0.30f,WaveType.SINE,false); });
    }
    public static void playDouble() {
        play(() -> { tone(400,50,0.35f,WaveType.SINE,false); tone(600,50,0.35f,WaveType.SINE,false); tone(800,100,0.40f,WaveType.SINE,false); });
    }
    public static void playTriple() {
        play(() -> { tone(400,40,0.35f,WaveType.SINE,false); tone(600,40,0.35f,WaveType.SINE,false); tone(800,40,0.40f,WaveType.SINE,false); tone(1000,120,0.45f,WaveType.SINE,false); });
    }
    public static void playTetris() {
        play(() -> { tone(523,80,0.5f,WaveType.SINE,false); tone(659,80,0.5f,WaveType.SINE,false); tone(784,80,0.5f,WaveType.SINE,false); tone(1047,200,0.6f,WaveType.SINE,false); noise(100,0.2f); });
    }
    public static void playLevelUp() {
        play(() -> { tone(523,80,0.4f,WaveType.SINE,false); tone(659,80,0.4f,WaveType.SINE,false); tone(784,80,0.4f,WaveType.SINE,false); tone(1047,150,0.5f,WaveType.SINE,false); });
    }
    public static void playGameOver() {
        play(() -> { tone(440,150,0.4f,WaveType.SINE,false); tone(349,150,0.4f,WaveType.SINE,false); tone(294,150,0.4f,WaveType.SINE,false); tone(220,400,0.5f,WaveType.SINE,false); });
    }

    // ── Synthesis ─────────────────────────────────────────────
    private enum WaveType { SINE, SQUARE, SAW }

    private static void play(Runnable task) {
        if (!sfxEnabled || sfxVolume == 0) return;
        executor.submit(() -> { try { task.run(); } catch (Exception ignored) {} });
    }

    private static void tone(int freq, int ms, float vol, WaveType type, boolean decay) {
        try {
            float v = vol * sfxVolume;
            int sr=44100, n=sr*ms/1000;
            byte[] buf=new byte[n*2];
            for (int i=0;i<n;i++) {
                double t=(double)i/sr;
                double wave = switch(type) {
                    case SQUARE -> Math.sin(2*Math.PI*freq*t)>=0?1.0:-1.0;
                    case SAW    -> 2.0*((freq*t)%1.0)-1.0;
                    default     -> Math.sin(2*Math.PI*freq*t);
                };
                double env=decay?Math.max(0,1.0-(double)i/n):1.0;
                if (i<sr*0.005) env*=(double)i/(sr*0.005);
                short val=(short)(wave*env*v*Short.MAX_VALUE);
                buf[i*2]=(byte)(val&0xFF); buf[i*2+1]=(byte)((val>>8)&0xFF);
            }
            playBuffer(buf,sr);
        } catch (Exception ignored) {}
    }

    private static void sweep(int f0,int f1,int ms,float vol) {
        try {
            float v=vol*sfxVolume;
            int sr=44100,n=sr*ms/1000; byte[] buf=new byte[n*2];
            for (int i=0;i<n;i++) {
                double t=(double)i/sr,p=(double)i/n;
                short val=(short)(Math.sin(2*Math.PI*(f0+(f1-f0)*p)*t)*(1-p)*v*Short.MAX_VALUE);
                buf[i*2]=(byte)(val&0xFF); buf[i*2+1]=(byte)((val>>8)&0xFF);
            }
            playBuffer(buf,sr);
        } catch (Exception ignored) {}
    }

    private static void noise(int ms,float vol) {
        try {
            float v=vol*sfxVolume;
            int sr=44100,n=sr*ms/1000; byte[] buf=new byte[n*2];
            java.util.Random rng=new java.util.Random();
            for (int i=0;i<n;i++) {
                short val=(short)((rng.nextDouble()*2-1)*(1.0-(double)i/n)*v*Short.MAX_VALUE);
                buf[i*2]=(byte)(val&0xFF); buf[i*2+1]=(byte)((val>>8)&0xFF);
            }
            playBuffer(buf,sr);
        } catch (Exception ignored) {}
    }

    private static void playBuffer(byte[] buf,int sr) throws Exception {
        AudioFormat fmt=new AudioFormat(sr,16,1,true,false);
        SourceDataLine line=(SourceDataLine)AudioSystem.getLine(new DataLine.Info(SourceDataLine.class,fmt));
        line.open(fmt); line.start();
        line.write(buf,0,buf.length);
        line.drain(); line.close();
    }
}
