package view;

import model.GameModel;
import pattern.SoundManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

/**
 * Settings dialog — X button only, Back to Menu button added.
 */
public class SettingsDialog extends JDialog {

    private static final Color BG     = new Color(22, 28, 45);
    private static final Color ACCENT = new Color(0, 190, 255);
    private static final Color TEXT   = new Color(220, 225, 235);
    private static final Color RED    = new Color(190, 40, 40);
    private static final Color NAVY   = new Color(30, 50, 100);

    private static int selectedSongIndex = 0;
    private JComboBox<String> songBox;
    private Runnable onClose;
    private Runnable onBackToMenu;

    public void setOnClose(Runnable r)      { this.onClose = r; }
    public void setOnBackToMenu(Runnable r) { this.onBackToMenu = r; }

    public SettingsDialog(JFrame parent, GameModel model, Runnable onRestart) {
        super(parent, "Settings", true);
        setUndecorated(true);
        setSize(440, model != null ? 400 : 320);
        setLocationRelativeTo(parent);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                if (onClose != null) onClose.run();
            }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(BorderFactory.createLineBorder(ACCENT, 2));

        // ── Title bar — only X button ─────────────────────────
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(15, 20, 35));
        titleBar.setBorder(BorderFactory.createEmptyBorder(12, 16, 10, 12));

        JLabel titleLbl = new JLabel("⚙   Settings");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLbl.setForeground(ACCENT);

        JButton xBtn = new JButton("X");
        xBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        xBtn.setForeground(Color.WHITE);
        xBtn.setBackground(new Color(60, 60, 90));
        xBtn.setBorderPainted(false);
        xBtn.setFocusPainted(false);
        xBtn.setPreferredSize(new Dimension(32, 28));
        xBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        xBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { xBtn.setBackground(RED); }
            public void mouseExited(MouseEvent e)  { xBtn.setBackground(new Color(60,60,90)); }
        });
        xBtn.addActionListener(e -> dispose());

        titleBar.add(titleLbl, BorderLayout.WEST);
        titleBar.add(xBtn,     BorderLayout.EAST);

        // ── Content ───────────────────────────────────────────
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG);
        content.setBorder(BorderFactory.createEmptyBorder(12, 18, 14, 18));

        // Music section
        content.add(sectionLbl("♪   Music"));
        content.add(Box.createVerticalStrut(8));

        JPanel songRow = new JPanel(new BorderLayout(8, 0));
        songRow.setOpaque(false);
        songRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        songBox = buildSongCombo();
        JButton addBtn = new JButton("+ Add Song");
        styleSmallBtn(addBtn, new Color(40,80,130), new Color(60,120,190));
        addBtn.addActionListener(e -> addSong(parent));
        songRow.add(songBox, BorderLayout.CENTER);
        songRow.add(addBtn,  BorderLayout.EAST);
        content.add(songRow);
        content.add(Box.createVerticalStrut(10));

        JLabel bgmPct = pctLbl(SoundManager.getBgmVolume100());
        JSlider bgmSlider = makeSlider(SoundManager.getBgmVolume100());
        bgmSlider.addChangeListener(e -> {
            int v = bgmSlider.getValue();
            bgmPct.setText(v + "%");
            SoundManager.setBgmVolume(v / 100f);
        });
        content.add(sliderRow("🔈", bgmPct, bgmSlider));
        content.add(Box.createVerticalStrut(16));

        // SFX section
        content.add(sectionLbl("🔊   SFX Volume"));
        content.add(Box.createVerticalStrut(8));
        JLabel sfxPct = pctLbl(SoundManager.getSfxVolume100());
        JSlider sfxSlider = makeSlider(SoundManager.getSfxVolume100());
        sfxSlider.addChangeListener(e -> {
            int v = sfxSlider.getValue();
            sfxPct.setText(v + "%");
            SoundManager.setSfxVolume(v / 100f);
        });
        content.add(sliderRow("🔈", sfxPct, sfxSlider));

        // In-game buttons
        if (model != null) {
            content.add(Box.createVerticalStrut(18));
            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(50, 60, 90));
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            content.add(sep);
            content.add(Box.createVerticalStrut(12));

            // Restart button
            JButton restartBtn = actionBtn("↺   Restart Game", RED, new Color(220, 60, 60));
            restartBtn.addActionListener(e -> { dispose(); onRestart.run(); });
            content.add(restartBtn);
            content.add(Box.createVerticalStrut(8));

            // Back to Menu button
            JButton menuBtn = actionBtn("⌂   Back to Menu", NAVY, new Color(50, 80, 150));
            menuBtn.addActionListener(e -> {
                dispose();
                if (onBackToMenu != null) onBackToMenu.run();
            });
            content.add(menuBtn);
        }

        root.add(titleBar, BorderLayout.NORTH);
        root.add(content,  BorderLayout.CENTER);
        setContentPane(root);
    }

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
            SoundManager.stopBgm();
            SoundManager.setSongFile(SoundManager.getSongList().get(idx)[1]);
            if (SoundManager.isBgmEnabled()) SoundManager.startBgm();
        });
        return box;
    }

    private void addSong(JFrame parent) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select WAV file");
        fc.setFileFilter(new FileNameExtensionFilter("WAV files", "wav"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            String displayName = SoundManager.addExternalSong(fc.getSelectedFile());
            List<String[]> songs = SoundManager.getSongList();
            songBox.addItem(displayName);
            int idx = songs.size() - 1;
            songBox.setSelectedIndex(idx);
            selectedSongIndex = idx;
            SoundManager.stopBgm();
            SoundManager.setSongFile(songs.get(idx)[1]);
            if (SoundManager.isBgmEnabled()) SoundManager.startBgm();
            JOptionPane.showMessageDialog(this, "Added: " + displayName, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel sectionLbl(String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(TEXT);
        p.add(l);
        return p;
    }

    private JLabel pctLbl(int v) {
        JLabel l = new JLabel(v + "%");
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
        row.add(ico,    BorderLayout.WEST);
        row.add(pct,    BorderLayout.CENTER);
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
                g2.fillRoundRect(trackRect.x, cy-3, trackRect.width, 6, 6, 6);
                g2.setColor(ACCENT);
                g2.fillRoundRect(trackRect.x, cy-3, (int)(trackRect.width * s.getValue() / 100.0), 6, 6, 6);
            }
            @Override public void paintThumb(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.fillOval(thumbRect.x+2, thumbRect.y+2, thumbRect.width-4, thumbRect.height-4);
                g2.setColor(Color.WHITE);
                g2.fillOval(thumbRect.x+6, thumbRect.y+6, thumbRect.width-12, thumbRect.height-12);
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
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    private void styleSmallBtn(JButton btn, Color bg, Color hover) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(90, 28));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
    }
}
