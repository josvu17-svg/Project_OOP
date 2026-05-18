package pattern;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Manages BGM (via Clip) and SFX (synthesized).
 * BGM files are loaded from:
 *   1. Built-in WAVs bundled in JAR (classpath resources)
 *   2. External WAVs in ./bgm/ folder next to the JAR
 */
public class SoundManager {

    private static final java.util.concurrent.ExecutorService executor =
        java.util.concurrent.Executors.newCachedThreadPool();

    private static boolean sfxEnabled = true;
    private static boolean bgmEnabled = true;
    private static float   sfxVolume  = 0.8f;
    private static float   bgmVolume  = 0.6f;

    private static Clip   bgmClip        = null;
    private static String currentSongFile = "dotoc2.wav"; // built-in default

    // ── Song registry ─────────────────────────────────────────
    // Each entry: [displayName, resourcePath_or_filePath, isExternal]
    private static final List<String[]> songList = new ArrayList<>();

    static {
        // Built-in songs (bundled in JAR)
        songList.add(new String[]{"Độ Tộc 2 - Đô Mixi",        "dotoc2.wav",    "false"});
        songList.add(new String[]{"Tình Cha - Killerqueen",     "tinhcha.wav",   "false"});
        songList.add(new String[]{"Chiều Hôm Ấy - Jaykii",     "chieuhomay.wav","false"});
        // External songs from ./bgm/ folder are loaded at runtime
        loadExternalSongs();
    }

    /** Load any WAV files from ./bgm/ folder next to the JAR */
    public static void loadExternalSongs() {
        File bgmDir = getBgmDir();
        if (bgmDir == null || !bgmDir.exists()) return;
        File[] wavFiles = bgmDir.listFiles(f -> f.getName().toLowerCase().endsWith(".wav"));
        if (wavFiles == null) return;
        for (File f : wavFiles) {
            String name = f.getName().replaceFirst("\\.wav$", "");
            // Check not already registered
            boolean exists = songList.stream().anyMatch(s -> s[1].equals(f.getAbsolutePath()));
            if (!exists) {
                songList.add(new String[]{name, f.getAbsolutePath(), "true"});
            }
        }
    }

    /** Add a new song by copying it to ./bgm/ and registering it */
    public static String addExternalSong(File sourceFile) throws IOException {
        File bgmDir = getBgmDir();
        if (bgmDir == null) throw new IOException("Cannot find bgm directory");
        bgmDir.mkdirs();

        // Convert to WAV if needed (basic copy for now — must be WAV)
        String filename = sourceFile.getName();
        File dest = new File(bgmDir, filename);
        Files.copy(sourceFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

        String displayName = filename.replaceFirst("\\.[^.]+$", "");
        boolean exists = songList.stream().anyMatch(s -> s[1].equals(dest.getAbsolutePath()));
        if (!exists) {
            songList.add(new String[]{displayName, dest.getAbsolutePath(), "true"});
        }
        return displayName;
    }

    public static List<String[]> getSongList() { return Collections.unmodifiableList(songList); }

    /** Get bgm/ directory next to JAR or in working directory */
    private static File getBgmDir() {
        try {
            File jarDir = new File(SoundManager.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getParentFile();
            return new File(jarDir, "bgm");
        } catch (Exception e) {
            return new File("bgm");
        }
    }

    // ── BGM controls ──────────────────────────────────────────
    public static void setSongFile(String pathOrResource) {
        currentSongFile = pathOrResource;
        if (bgmClip != null) { bgmClip.close(); bgmClip = null; }
    }

    private static void ensureBgmLoaded() {
        if (bgmClip != null && bgmClip.isOpen()) return;
        try {
            AudioInputStream ais;
            File external = new File(currentSongFile);
            if (external.exists()) {
                // External file
                ais = AudioSystem.getAudioInputStream(external);
            } else {
                // Built-in resource in JAR
                InputStream is = SoundManager.class.getResourceAsStream("/" + currentSongFile);
                if (is == null) { System.err.println("BGM not found: " + currentSongFile); return; }
                ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is, 65536));
            }
            // Convert to PCM if needed
            AudioFormat base = ais.getFormat();
            if (base.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                AudioFormat target = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    base.getSampleRate(), 16, base.getChannels(),
                    base.getChannels()*2, base.getSampleRate(), false);
                ais = AudioSystem.getAudioInputStream(target, ais);
            }
            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);
            ais.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void applyBgmVolume() {
        if (bgmClip == null) return;
        try {
            FloatControl c = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = bgmVolume <= 0 ? -80f : (float)(20 * Math.log10(bgmVolume));
            c.setValue(Math.max(c.getMinimum(), Math.min(c.getMaximum(), dB)));
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
        if (bgmClip != null && bgmClip.isRunning()) bgmClip.stop();
    }

    public static void setBgmEnabled(boolean b) {
        bgmEnabled = b;
        if (bgmEnabled) startBgm(); else stopBgm();
    }
    public static boolean isBgmEnabled()   { return bgmEnabled; }
    public static void setBgmVolume(float v) {
        bgmVolume = Math.max(0, Math.min(1, v));
        applyBgmVolume();
    }
    public static float getBgmVolume()     { return bgmVolume; }
    public static int   getBgmVolume100()  { return (int)(bgmVolume * 100); }

    // ── SFX controls ──────────────────────────────────────────
    public static void setSfxEnabled(boolean b) { sfxEnabled = b; }
    public static boolean isSfxEnabled()        { return sfxEnabled; }
    public static void setSfxVolume(float v)    { sfxVolume = Math.max(0, Math.min(1, v)); }
    public static float getSfxVolume()          { return sfxVolume; }
    public static int   getSfxVolume100()       { return (int)(sfxVolume * 100); }

    // ── SFX methods ───────────────────────────────────────────
    public static void playLock()     { play(() -> tone(180,60,0.25f,WaveType.SQUARE,true)); }
    public static void playMove()     { play(() -> tone(220,25,0.12f,WaveType.SQUARE,true)); }
    public static void playRotate()   { play(() -> tone(300,30,0.15f,WaveType.SQUARE,true)); }
    public static void playHardDrop() { play(() -> { sweep(800,200,60,0.4f); tone(150,80,0.3f,WaveType.SQUARE,true); }); }
    public static void playSingle()   { play(() -> { tone(400,60,0.35f,WaveType.SINE,false); tone(600,80,0.30f,WaveType.SINE,false); }); }
    public static void playDouble()   { play(() -> { tone(400,50,0.35f,WaveType.SINE,false); tone(600,50,0.35f,WaveType.SINE,false); tone(800,100,0.40f,WaveType.SINE,false); }); }
    public static void playTriple()   { play(() -> { tone(400,40,0.35f,WaveType.SINE,false); tone(600,40,0.35f,WaveType.SINE,false); tone(800,40,0.40f,WaveType.SINE,false); tone(1000,120,0.45f,WaveType.SINE,false); }); }
    public static void playTetris()   { play(() -> { tone(523,80,0.5f,WaveType.SINE,false); tone(659,80,0.5f,WaveType.SINE,false); tone(784,80,0.5f,WaveType.SINE,false); tone(1047,200,0.6f,WaveType.SINE,false); noise(100,0.2f); }); }
    public static void playLevelUp()  { play(() -> { tone(523,80,0.4f,WaveType.SINE,false); tone(659,80,0.4f,WaveType.SINE,false); tone(784,80,0.4f,WaveType.SINE,false); tone(1047,150,0.5f,WaveType.SINE,false); }); }
    public static void playGameOver() { play(() -> { tone(440,150,0.4f,WaveType.SINE,false); tone(349,150,0.4f,WaveType.SINE,false); tone(294,150,0.4f,WaveType.SINE,false); tone(220,400,0.5f,WaveType.SINE,false); }); }

    // ── Synthesis ─────────────────────────────────────────────
    private enum WaveType { SINE, SQUARE, SAW }

    private static void play(Runnable t) {
        if (!sfxEnabled || sfxVolume == 0) return;
        executor.submit(() -> { try { t.run(); } catch (Exception ignored) {} });
    }

    private static void tone(int freq, int ms, float vol, WaveType type, boolean decay) {
        try {
            float v=vol*sfxVolume; int sr=44100,n=sr*ms/1000; byte[] buf=new byte[n*2];
            for (int i=0;i<n;i++) {
                double t=(double)i/sr;
                double wave=switch(type){
                    case SQUARE->Math.sin(2*Math.PI*freq*t)>=0?1.0:-1.0;
                    case SAW->2.0*((freq*t)%1.0)-1.0;
                    default->Math.sin(2*Math.PI*freq*t);
                };
                double env=decay?Math.max(0,1.0-(double)i/n):1.0;
                if(i<sr*0.005)env*=(double)i/(sr*0.005);
                short val=(short)(wave*env*v*Short.MAX_VALUE);
                buf[i*2]=(byte)(val&0xFF);buf[i*2+1]=(byte)((val>>8)&0xFF);
            }
            playBuffer(buf,sr);
        } catch(Exception ignored){}
    }

    private static void sweep(int f0,int f1,int ms,float vol){
        try{
            float v=vol*sfxVolume; int sr=44100,n=sr*ms/1000; byte[] buf=new byte[n*2];
            for(int i=0;i<n;i++){
                double t=(double)i/sr,p=(double)i/n;
                short val=(short)(Math.sin(2*Math.PI*(f0+(f1-f0)*p)*t)*(1-p)*v*Short.MAX_VALUE);
                buf[i*2]=(byte)(val&0xFF);buf[i*2+1]=(byte)((val>>8)&0xFF);
            }
            playBuffer(buf,sr);
        }catch(Exception ignored){}
    }

    private static void noise(int ms,float vol){
        try{
            float v=vol*sfxVolume; int sr=44100,n=sr*ms/1000; byte[] buf=new byte[n*2];
            java.util.Random rng=new java.util.Random();
            for(int i=0;i<n;i++){
                short val=(short)((rng.nextDouble()*2-1)*(1.0-(double)i/n)*v*Short.MAX_VALUE);
                buf[i*2]=(byte)(val&0xFF);buf[i*2+1]=(byte)((val>>8)&0xFF);
            }
            playBuffer(buf,sr);
        }catch(Exception ignored){}
    }

    private static void playBuffer(byte[] buf,int sr) throws Exception {
        AudioFormat fmt=new AudioFormat(sr,16,1,true,false);
        SourceDataLine line=(SourceDataLine)AudioSystem.getLine(new DataLine.Info(SourceDataLine.class,fmt));
        line.open(fmt);line.start();line.write(buf,0,buf.length);line.drain();line.close();
    }
}
