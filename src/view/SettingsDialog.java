package view;

import model.GameModel;
import pattern.SoundManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

/**
 * Settings dialog with:
 * - Song dropdown + Add Song button
 * - BGM volume slider
 * - SFX volume slider
 * - Restart / Resume buttons
 */
public class SettingsDialog extends JDialog {

    private static final Color BG     = new Color(22, 28, 45);
    private static final Color ACCENT = new Color(0, 190, 255);
    private static final Color TEXT   = new Color(220, 225, 235);
    private static final Color MUTED  = new Color(120, 130, 150);
    private static final Color RED    = new Color(190, 40, 40);
    private static final Color GREEN  = new Color(30, 130, 60);

    private static int selectedSongIndex = 0;
    private JComboBox<String> songBox;

    public SettingsDialog(JFrame parent, GameModel model, Runnable onRestart) {
        super(parent, "Settings", true);
        setUndecorated(true);
        setSize(440, 410);
        setLocationRelativeTo(parent);
        setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(BorderFactory.createLineBorder(ACCENT, 2));

        // ── Title bar ─────────────────────────────────────────
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(15, 20, 35));
        titleBar.setBorder(BorderFactory.createEmptyBorder(12, 16, 10, 12));

        JLabel titleLbl = new JLabel("⚙   Settings");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLbl.setForeground(ACCENT);

        JButton closeBtn = makeIconBtn("X");
        closeBtn.addActionListener(e -> dispose());

        titleBar.add(titleLbl, BorderLayout.WEST);
        titleBar.add(closeBtn, BorderLayout.EAST);

        // ── Content ───────────────────────────────────────────
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG);
        content.setBorder(BorderFactory.createEmptyBorder(12, 18, 14, 18));

        // ── Song selector ─────────────────────────────────────
        content.add(sectionLabel("♪   Music"));
        content.add(Box.createVerticalStrut(8));

        // Dropdown row
        JPanel songRow = new JPanel(new BorderLayout(8, 0));
        songRow.setOpaque(false);
        songRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        songBox = buildSongCombo();
        JButton addBtn = new JButton("+ Add Song");
        addBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        addBtn.setForeground(Color.WHITE);
        addBtn.setBackground(new Color(40, 80, 130));
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.setPreferredSize(new Dimension(90, 28));
        addBtn.addMouseListener(hoverEffect(addBtn, new Color(40,80,130), new Color(60,120,190)));
        addBtn.addActionListener(e -> addSong(parent));

        songRow.add(songBox, BorderLayout.CENTER);
        songRow.add(addBtn, BorderLayout.EAST);
        content.add(songRow);
        content.add(Box.createVerticalStrut(10));

        // BGM Volume
        JLabel bgmPct = pctLabel(SoundManager.getBgmVolume100());
        JSlider bgmSlider = makeSlider(SoundManager.getBgmVolume100());
        bgmSlider.addChangeListener(e -> {
            int v = bgmSlider.getValue();
            bgmPct.setText(v + "%");
            SoundManager.setBgmVolume(v / 100f);
        });
        content.add(sliderRow("🔈", bgmPct, bgmSlider));
        content.add(Box.createVerticalStrut(16));

        // ── SFX Volume ────────────────────────────────────────
        content.add(sectionLabel("🔊   SFX Volume"));
        content.add(Box.createVerticalStrut(8));

        JLabel sfxPct = pctLabel(SoundManager.getSfxVolume100());
        JSlider sfxSlider = makeSlider(SoundManager.getSfxVolume100());
        sfxSlider.addChangeListener(e -> {
            int v = sfxSlider.getValue();
            sfxPct.setText(v + "%");
            SoundManager.setSfxVolume(v / 100f);
        });
        content.add(sliderRow("🔈", sfxPct, sfxSlider));
        content.add(Box.createVerticalStrut(18));

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(50, 60, 90));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        content.add(sep);
        content.add(Box.createVerticalStrut(14));

        // Buttons
        JButton restartBtn = actionBtn("↺   Restart Game", RED, new Color(220, 60, 60));
        restartBtn.addActionListener(e -> { dispose(); onRestart.run(); });
        content.add(restartBtn);
        content.add(Box.createVerticalStrut(8));

        JButton resumeBtn = actionBtn("▶   Resume Game", GREEN, new Color(50, 180, 90));
        resumeBtn.addActionListener(e -> dispose());
        content.add(resumeBtn);

        root.add(titleBar, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Song combo ────────────────────────────────────────────
    private JComboBox<String> buildSongCombo() {
        List<String[]> songs = SoundManager.getSongList();
        String[] names = songs.stream().map(s -> s[0]).toArray(String[]::new);
        JComboBox<String> box = new JComboBox<>(names);
        if (selectedSongIndex < names.length) box.setSelectedIndex(selectedSongIndex);
        box.setFont(new Font("SansSerif", Font.PLAIN, 12));
        box.setForeground(TEXT);
        box.setBackground(new Color(35, 45, 70));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        box.addActionListener(e -> {
            int idx = box.getSelectedIndex();
            if (idx < 0) return;
            selectedSongIndex = idx;
            String path = SoundManager.getSongList().get(idx)[1];
            SoundManager.stopBgm();
            SoundManager.setSongFile(path);
            if (SoundManager.isBgmEnabled()) SoundManager.startBgm();
        });
        return box;
    }

    private void addSong(JFrame parent) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select a WAV or MP3 file");
        fc.setFileFilter(new FileNameExtensionFilter("Audio files (WAV, MP3)", "wav", "mp3"));
        int result = fc.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = fc.getSelectedFile();
        try {
            // Only WAV supported for now — show message if MP3
            if (selectedFile.getName().toLowerCase().endsWith(".mp3")) {
                JOptionPane.showMessageDialog(this,
                    "MP3 không được hỗ trợ trực tiếp.\nHãy convert sang WAV trước (dùng VLC hoặc online converter).",
                    "Format không hỗ trợ", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String displayName = SoundManager.addExternalSong(selectedFile);
            // Refresh dropdown
            List<String[]> songs = SoundManager.getSongList();
            songBox.addItem(displayName);
            int newIdx = songs.size() - 1;
            songBox.setSelectedIndex(newIdx);
            selectedSongIndex = newIdx;
            SoundManager.stopBgm();
            SoundManager.setSongFile(songs.get(newIdx)[1]);
            if (SoundManager.isBgmEnabled()) SoundManager.startBgm();

            JOptionPane.showMessageDialog(this,
                "Đã thêm: " + displayName, "Thêm nhạc thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Lỗi: " + ex.getMessage(), "Không thể thêm nhạc", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── UI helpers ────────────────────────────────────────────
    private JPanel sectionLabel(String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(TEXT);
        p.add(l);
        return p;
    }

    private JLabel pctLabel(int val) {
        JLabel l = new JLabel(val + "%");
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(ACCENT);
        l.setPreferredSize(new Dimension(38, 20));
        return l;
    }

    private JPanel sliderRow(String icon, JLabel pct, JSlider slider) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("SansSerif", Font.PLAIN, 18));
        ico.setPreferredSize(new Dimension(26, 26));
        row.add(ico, BorderLayout.WEST);
        row.add(pct, BorderLayout.CENTER);
        row.add(slider, BorderLayout.EAST);
        return row;
    }

    private JSlider makeSlider(int init) {
        JSlider s = new JSlider(0, 100, init);
        s.setOpaque(false);
        s.setPreferredSize(new Dimension(250, 28));
        s.setMaximumSize(new Dimension(250, 28));
        s.setUI(new BasicSliderUI(s) {
            @Override public void paintTrack(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cy = trackRect.y + trackRect.height / 2;
                g2.setColor(new Color(50, 60, 90));
                g2.fillRoundRect(trackRect.x, cy - 3, trackRect.width, 6, 6, 6);
                int filled = (int)(trackRect.width * s.getValue() / 100.0);
                g2.setColor(ACCENT);
                g2.fillRoundRect(trackRect.x, cy - 3, filled, 6, 6, 6);
            }
            @Override public void paintThumb(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.fillOval(thumbRect.x+2,thumbRect.y+2,thumbRect.width-4,thumbRect.height-4);
                g2.setColor(Color.WHITE);
                g2.fillOval(thumbRect.x+6,thumbRect.y+6,thumbRect.width-12,thumbRect.height-12);
            }
        });
        return s;
    }

    private JButton actionBtn(String label, Color bg, Color hover) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.addMouseListener(hoverEffect(btn, bg, hover));
        return btn;
    }

    private JButton makeIconBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(60, 60, 90));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(32, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(hoverEffect(btn, new Color(60,60,90), new Color(180,40,40)));
        return btn;
    }

    private MouseAdapter hoverEffect(JButton btn, Color normal, Color hover) {
        return new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(normal); }
        };
    }
}
