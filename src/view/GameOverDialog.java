package view;

import pattern.ScoreManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Custom dark-themed Game Over dialog.
 * Shows final score and asks for player name.
 */
public class GameOverDialog extends JDialog {

    private static final Color BG     = new Color(22, 28, 45);
    private static final Color ACCENT = new Color(0, 190, 255);
    private static final Color TEXT   = new Color(220, 225, 235);
    private static final Color MUTED  = new Color(120, 130, 150);
    private static final Color RED    = new Color(190, 40, 40);
    private static final Color GREEN  = new Color(30, 130, 60);

    private String playerName = "";

    public static void show(JFrame parent, int score, int level, int lines) {
        GameOverDialog dlg = new GameOverDialog(parent, score, level, lines);
        dlg.setVisible(true);
    }

    private GameOverDialog(JFrame parent, int score, int level, int lines) {
        super(parent, "Game Over", true);
        setUndecorated(true);
        setSize(360, 300);
        setLocationRelativeTo(parent);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(BorderFactory.createLineBorder(RED, 2));

        // ── Header ────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(RED);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("GAME OVER");
        title.setFont(new Font("Arial Black", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        // ── Stats ─────────────────────────────────────────────
        JPanel stats = new JPanel(new GridLayout(3, 2, 10, 8));
        stats.setBackground(new Color(30, 38, 58));
        stats.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        stats.add(statLabel("SCORE", false));
        stats.add(statLabel(String.format("%,d", score), true));
        stats.add(statLabel("LEVEL", false));
        stats.add(statLabel(String.valueOf(level), true));
        stats.add(statLabel("LINES", false));
        stats.add(statLabel(String.valueOf(lines), true));

        // ── Name input ────────────────────────────────────────
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BG);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(14, 20, 8, 20));

        JLabel nameLbl = new JLabel("Your name:");
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        nameLbl.setForeground(TEXT);

        JTextField nameField = new JTextField();
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        nameField.setForeground(TEXT);
        nameField.setBackground(new Color(35, 45, 70));
        nameField.setCaretColor(ACCENT);
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50,70,110), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        inputPanel.add(nameLbl,    BorderLayout.WEST);
        inputPanel.add(nameField,  BorderLayout.CENTER);

        // ── Buttons ───────────────────────────────────────────
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setBackground(BG);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(4, 20, 16, 20));

        JButton cancelBtn = new JButton("Skip");
        styleBtn(cancelBtn, new Color(50, 55, 75), new Color(70, 75, 100));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton("Save Score");
        styleBtn(saveBtn, GREEN, new Color(50, 180, 90));
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                ScoreManager.addScore(name.toUpperCase(), score, level, lines);
            }
            dispose();
        });

        // Enter key saves
        nameField.addActionListener(e -> saveBtn.doClick());

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        root.add(header,     BorderLayout.NORTH);
        root.add(stats,      BorderLayout.CENTER);
        root.add(inputPanel, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(BG);
        bottom.add(inputPanel, BorderLayout.NORTH);
        bottom.add(btnPanel,   BorderLayout.SOUTH);

        root.add(header, BorderLayout.NORTH);
        root.add(stats,  BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);

        // Focus name field
        SwingUtilities.invokeLater(nameField::requestFocusInWindow);
    }

    private JLabel statLabel(String text, boolean isValue) {
        JLabel l = new JLabel(text, isValue ? SwingConstants.RIGHT : SwingConstants.LEFT);
        l.setFont(isValue
            ? new Font("Arial Black", Font.BOLD, 18)
            : new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(isValue ? ACCENT : MUTED);
        return l;
    }

    private void styleBtn(JButton btn, Color bg, Color hover) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
    }
}
