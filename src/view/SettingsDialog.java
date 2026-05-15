package view;

import model.GameModel;
import model.GameState;
import pattern.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Settings dialog with:
 * - Restart game button
 * - BGM volume slider
 * - SFX volume slider
 */
public class SettingsDialog extends JDialog {

    private static final Color BG       = new Color(25, 25, 35);
    private static final Color PANEL_BG = new Color(35, 35, 50);
    private static final Color ACCENT   = new Color(0, 200, 255);
    private static final Color TEXT     = new Color(220, 220, 220);
    private static final Color BTN_RED  = new Color(180, 40, 40);
    private static final Color BTN_HOV  = new Color(220, 60, 60);

    public SettingsDialog(JFrame parent, GameModel model, Runnable onRestart) {
        super(parent, "Settings", true); // modal
        setUndecorated(true);
        setSize(340, 340);
        setLocationRelativeTo(parent);
        setBackground(BG);

        JPanel root = new JPanel();
        root.setLayout(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(BorderFactory.createLineBorder(ACCENT, 2));

        // ── Title bar ──────────────────────────────────────────
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(15, 15, 25));
        titleBar.setBorder(new EmptyBorder(10, 16, 10, 10));

        JLabel title = new JLabel("⚙  Settings");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(ACCENT);

        JButton closeBtn = makeIconButton("✕", new Color(80, 80, 80), Color.WHITE);
        closeBtn.addActionListener(e -> dispose());

        titleBar.add(title, BorderLayout.WEST);
        titleBar.add(closeBtn, BorderLayout.EAST);

        // ── Content ────────────────────────────────────────────
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(16, 20, 16, 20));

        // BGM Volume
        content.add(makeSliderRow(
            "🎵  Music Volume",
            SoundManager.getBgmVolume100(),
            val -> SoundManager.setBgmVolume(val / 100f)
        ));
        content.add(Box.createVerticalStrut(16));

        // SFX Volume
        content.add(makeSliderRow(
            "🔊  SFX Volume",
            SoundManager.getSfxVolume100(),
            val -> SoundManager.setSfxVolume(val / 100f)
        ));
        content.add(Box.createVerticalStrut(24));

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 60, 80));
        sep.setBackground(new Color(60, 60, 80));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        content.add(sep);
        content.add(Box.createVerticalStrut(20));

        // Restart button
        JButton restartBtn = new JButton("↺   Restart Game");
        restartBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        restartBtn.setForeground(Color.WHITE);
        restartBtn.setBackground(BTN_RED);
        restartBtn.setBorderPainted(false);
        restartBtn.setFocusPainted(false);
        restartBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        restartBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        restartBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        restartBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { restartBtn.setBackground(BTN_HOV); }
            public void mouseExited(java.awt.event.MouseEvent e)  { restartBtn.setBackground(BTN_RED); }
        });
        restartBtn.addActionListener(e -> {
            dispose();
            onRestart.run();
        });

        content.add(restartBtn);
        content.add(Box.createVerticalStrut(10));

        // Resume button
        JButton resumeBtn = new JButton("▶   Resume Game");
        resumeBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        resumeBtn.setForeground(Color.WHITE);
        resumeBtn.setBackground(new Color(0, 120, 60));
        resumeBtn.setBorderPainted(false);
        resumeBtn.setFocusPainted(false);
        resumeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resumeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        resumeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        resumeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { resumeBtn.setBackground(new Color(0, 160, 80)); }
            public void mouseExited(java.awt.event.MouseEvent e)  { resumeBtn.setBackground(new Color(0, 120, 60)); }
        });
        resumeBtn.addActionListener(e -> dispose());
        content.add(resumeBtn);

        root.add(titleBar, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel makeSliderRow(String labelText, int initVal, java.util.function.IntConsumer onChange) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setBackground(BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Label + value
        JPanel labelRow = new JPanel(new BorderLayout());
        labelRow.setBackground(BG);
        labelRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(TEXT);

        JLabel valLbl = new JLabel(initVal + "%");
        valLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        valLbl.setForeground(ACCENT);

        labelRow.add(lbl, BorderLayout.WEST);
        labelRow.add(valLbl, BorderLayout.EAST);
        row.add(labelRow);
        row.add(Box.createVerticalStrut(6));

        // Slider
        JSlider slider = new JSlider(0, 100, initVal);
        slider.setBackground(BG);
        slider.setForeground(ACCENT);
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        slider.setAlignmentX(Component.LEFT_ALIGNMENT);
        slider.setPaintTicks(false);
        slider.setPaintLabels(false);

        // Custom slider UI color
        slider.setUI(new javax.swing.plaf.basic.BasicSliderUI(slider) {
            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Rectangle t = trackRect;
                int cy = t.y + t.height / 2;
                g2.setColor(new Color(60, 60, 80));
                g2.fillRoundRect(t.x, cy - 3, t.width, 6, 6, 6);
                int filled = (int)(t.width * slider.getValue() / 100.0);
                g2.setColor(ACCENT);
                g2.fillRoundRect(t.x, cy - 3, filled, 6, 6, 6);
            }
            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.fillOval(thumbRect.x + 2, thumbRect.y + 2, thumbRect.width - 4, thumbRect.height - 4);
                g2.setColor(Color.WHITE);
                g2.fillOval(thumbRect.x + 6, thumbRect.y + 6, thumbRect.width - 12, thumbRect.height - 12);
            }
        });

        slider.addChangeListener(e -> {
            int v = slider.getValue();
            valLbl.setText(v + "%");
            onChange.accept(v);
        });

        row.add(slider);
        return row;
    }

    private JButton makeIconButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(32, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
