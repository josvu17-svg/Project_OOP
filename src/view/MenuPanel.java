package view;

import pattern.ScoreManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Main menu screen matching the dark minimal design:
 * Title, Start Game button, Settings button, Ranking table.
 */
public class MenuPanel extends JPanel {

    private static final Color BG      = new Color(18, 18, 24);
    private static final Color ACCENT  = new Color(0, 180, 255);
    private static final Color TEXT    = new Color(200, 210, 220);
    private static final Color MUTED   = new Color(90, 100, 115);
    private static final Color BTN_BG  = new Color(28, 35, 50);
    private static final Color BTN_HOV = new Color(0, 150, 210, 60);

    private final Runnable onStart;
    private JPanel rankingPanel;

    public MenuPanel(Runnable onStart) {
        this.onStart = onStart;
        setBackground(BG);
        setLayout(new GridBagLayout());
        setFocusable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setPreferredSize(new Dimension(280, 500));

        // ── Title ─────────────────────────────────────────────
        col.add(Box.createVerticalStrut(40));
        col.add(centeredLabel("TETRIS", new Font("Arial Black", Font.BOLD, 48), ACCENT));
        col.add(Box.createVerticalStrut(6));
        col.add(centeredLabel("O O P   E D I T I O N", new Font("SansSerif", Font.PLAIN, 11), MUTED));
        col.add(Box.createVerticalStrut(36));

        // ── Start button ──────────────────────────────────────
        JButton startBtn = menuButton("►  START GAME");
        startBtn.addActionListener(e -> onStart.run());
        col.add(startBtn);
        col.add(Box.createVerticalStrut(12));

        // ── Settings button ───────────────────────────────────
        JButton settingsBtn = menuButton("⚙  SETTINGS");
        settingsBtn.addActionListener(e -> openMenuSettings());
        col.add(settingsBtn);
        col.add(Box.createVerticalStrut(32));

        // ── Ranking ───────────────────────────────────────────
        JLabel rankTitle = new JLabel("—  R A N K I N G  —");
        rankTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        rankTitle.setForeground(ACCENT);
        rankTitle.setAlignmentX(CENTER_ALIGNMENT);
        col.add(rankTitle);
        col.add(Box.createVerticalStrut(10));

        rankingPanel = new JPanel();
        rankingPanel.setOpaque(false);
        rankingPanel.setLayout(new BoxLayout(rankingPanel, BoxLayout.Y_AXIS));
        rankingPanel.setAlignmentX(CENTER_ALIGNMENT);
        buildRankingRows();
        col.add(rankingPanel);

        col.add(Box.createVerticalStrut(24));

        // ── Footer ────────────────────────────────────────────
        JLabel hint = new JLabel("PRESS ENTER TO START");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 10));
        hint.setForeground(MUTED);
        hint.setAlignmentX(CENTER_ALIGNMENT);
        col.add(hint);

        add(col);
    }

    private void buildRankingRows() {
        rankingPanel.removeAll();
        List<String> names  = ScoreManager.getNames();
        List<int[]>  scores = ScoreManager.getScores();

        String[] demoNames  = {"ACE","ZEN","NOVA","REX","KAI","MIA"};
        int[]    demoScores = {98400, 76210, 64880, 58100, 48350, 42180};

        int count = names.isEmpty() ? demoNames.length : Math.min(names.size(), 6);

        for (int i = 0; i < count; i++) {
            String name  = names.isEmpty() ? demoNames[i]  : names.get(i);
            int    score = names.isEmpty() ? demoScores[i] : scores.get(i)[0];

            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(260, 28));
            row.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

            // Rank + Name
            JLabel rankLbl = new JLabel(String.valueOf(i + 1));
            rankLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
            rankLbl.setForeground(i == 0 ? new Color(255, 200, 0) :
                                  i == 1 ? new Color(180, 180, 180) :
                                  i == 2 ? new Color(200, 120, 60) : MUTED);
            rankLbl.setPreferredSize(new Dimension(20, 20));

            JLabel nameLbl = new JLabel(name.toUpperCase());
            nameLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            nameLbl.setForeground(i < 3 ? TEXT : MUTED);
            nameLbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

            JPanel left = new JPanel(new BorderLayout());
            left.setOpaque(false);
            left.add(rankLbl, BorderLayout.WEST);
            left.add(nameLbl, BorderLayout.CENTER);

            JLabel scoreLbl = new JLabel(String.format("%,d", score));
            scoreLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            scoreLbl.setForeground(i == 0 ? new Color(255, 200, 0) : MUTED);

            row.add(left,     BorderLayout.WEST);
            row.add(scoreLbl, BorderLayout.EAST);
            rankingPanel.add(row);
        }
        rankingPanel.revalidate();
        rankingPanel.repaint();
    }

    public void refreshScores() {
        buildRankingRows();
    }

    private void openMenuSettings() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        view.SettingsDialog dlg = new view.SettingsDialog(topFrame, null, () -> {});
        dlg.setVisible(true);
    }

    private JButton menuButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? BTN_HOV : BTN_BG);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.setColor(getModel().isRollover() ? ACCENT : new Color(60, 80, 110));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(TEXT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(260, 48));
        btn.setPreferredSize(new Dimension(260, 48));
        return btn;
    }

    private JLabel centeredLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(CENTER_ALIGNMENT);
        l.setMaximumSize(new Dimension(280, 60));
        return l;
    }
}
