package view;

import model.Board;
import model.GameModel;
import model.Tetromino;
import model.TetrominoType;
import pattern.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Side panel showing game info: next piece, score, level, lines, controls.
 * Also contains SFX and BGM toggle buttons.
 */
public class InfoPanel extends JPanel {
    private static final int PREVIEW_CELL = 22;
    private static final Color BG_COLOR    = new Color(30, 30, 30);
    private static final Color TEXT_COLOR  = new Color(220, 220, 220);
    private static final Color LABEL_COLOR = new Color(150, 150, 150);
    private static final Color ACCENT_COLOR = new Color(0, 200, 255);

    private final GameModel model;

    // Toggle button rects (for click detection)
    private final Rectangle sfxBtnRect = new Rectangle();
    private final Rectangle bgmBtnRect = new Rectangle();

    public InfoPanel(GameModel model) {
        this.model = model;
        setPreferredSize(new Dimension(180, Board.ROWS * BoardPanel.getCellSize()));
        setBackground(BG_COLOR);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (sfxBtnRect.contains(e.getPoint())) {
                    SoundManager.setSfxEnabled(!SoundManager.isSfxEnabled());
                    repaint();
                } else if (bgmBtnRect.contains(e.getPoint())) {
                    SoundManager.setBgmEnabled(!SoundManager.isBgmEnabled());
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int y = 20;

        // ── NEXT PIECE ──
        y = drawLabel(g2d, "NEXT", y);
        y = drawNextPiece(g2d, y + 5);
        y += 20;

        // ── SCORE ──
        y = drawLabel(g2d, "SCORE", y);
        y = drawValue(g2d, String.valueOf(model.getScore()), y);
        y += 15;

        // ── LEVEL ──
        y = drawLabel(g2d, "LEVEL", y);
        y = drawValue(g2d, String.valueOf(model.getLevel()), y);
        y += 15;

        // ── LINES ──
        y = drawLabel(g2d, "LINES", y);
        y = drawValue(g2d, String.valueOf(model.getLinesCleared()), y);
        y += 20;

        // ── SOUND TOGGLES ──
        y = drawLabel(g2d, "SOUND", y);
        y += 8;
        y = drawToggleButton(g2d, "🔊 SFX", SoundManager.isSfxEnabled(), y, sfxBtnRect);
        y += 6;
        y = drawToggleButton(g2d, "🎵 Music", SoundManager.isBgmEnabled(), y, bgmBtnRect);
        y += 20;

        // ── CONTROLS ──
        y = drawLabel(g2d, "CONTROLS", y);
        y += 5;
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2d.setColor(LABEL_COLOR);
        String[] controls = {
            "\u2190 \u2192   Move",
            "\u2191      Rotate",
            "\u2193      Soft Drop",
            "Space  Hard Drop",
            "P      Pause",
            "Enter  Start/Restart"
        };
        for (String ctrl : controls) {
            g2d.drawString(ctrl, 15, y);
            y += 17;
        }
    }

    private int drawToggleButton(Graphics2D g, String label, boolean on, int y, Rectangle rect) {
        int btnX = 12, btnW = 155, btnH = 28;
        rect.setBounds(btnX, y, btnW, btnH);

        // Button background
        Color bg = on ? new Color(0, 160, 80) : new Color(80, 30, 30);
        Color border = on ? new Color(0, 220, 100) : new Color(180, 50, 50);
        g.setColor(bg);
        g.fillRoundRect(btnX, y, btnW, btnH, 8, 8);
        g.setColor(border);
        g.drawRoundRect(btnX, y, btnW, btnH, 8, 8);

        // Label
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(Color.WHITE);
        String status = on ? "ON" : "OFF";
        g.drawString(label, btnX + 10, y + 18);

        // Status badge
        g.setColor(on ? new Color(0, 255, 120) : new Color(255, 80, 80));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(status, btnX + btnW - fm.stringWidth(status) - 10, y + 18);

        return y + btnH;
    }

    private int drawLabel(Graphics2D g, String text, int y) {
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(ACCENT_COLOR);
        g.drawString(text, 15, y);
        return y + 5;
    }

    private int drawValue(Graphics2D g, String text, int y) {
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.setColor(TEXT_COLOR);
        g.drawString(text, 15, y + 25);
        return y + 30;
    }

    private int drawNextPiece(Graphics2D g, int startY) {
        Tetromino next = model.getNextPiece();
        if (next == null) return startY + 4 * PREVIEW_CELL;

        TetrominoType type = next.getType();
        int[][] blocks = type.getBlocks(0);
        Color color = type.getColor();

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (int[] b : blocks) {
            minX = Math.min(minX, b[0]); maxX = Math.max(maxX, b[0]);
            minY = Math.min(minY, b[1]); maxY = Math.max(maxY, b[1]);
        }
        int pieceWidth  = (maxX - minX + 1) * PREVIEW_CELL;
        int pieceHeight = (maxY - minY + 1) * PREVIEW_CELL;
        int offsetX = (getWidth() - pieceWidth) / 2 - minX * PREVIEW_CELL;
        int offsetY = startY;

        g.setColor(new Color(45, 45, 45));
        g.fillRoundRect(10, startY - 5, getWidth() - 20, pieceHeight + 15, 8, 8);

        for (int[] b : blocks) {
            int px = offsetX + b[0] * PREVIEW_CELL;
            int py = offsetY + (b[1] - minY) * PREVIEW_CELL;
            g.setColor(color);
            g.fillRect(px + 1, py + 1, PREVIEW_CELL - 2, PREVIEW_CELL - 2);
            g.setColor(color.brighter());
            g.drawLine(px+1, py+1, px+PREVIEW_CELL-2, py+1);
            g.drawLine(px+1, py+1, px+1, py+PREVIEW_CELL-2);
        }

        return startY + pieceHeight + 15;
    }
}
