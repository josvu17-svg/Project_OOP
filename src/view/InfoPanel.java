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
 * Side panel with gear icon at top-right corner for Settings.
 */
public class InfoPanel extends JPanel {
    private static final int PREVIEW_CELL   = 22;
    private static final Color BG_COLOR     = new Color(30, 30, 30);
    private static final Color TEXT_COLOR   = new Color(220, 220, 220);
    private static final Color LABEL_COLOR  = new Color(150, 150, 150);
    private static final Color ACCENT_COLOR = new Color(0, 200, 255);

    private final GameModel model;
    private JFrame parentFrame;
    private Runnable onRestart;

    // Gear button rect (top-right)
    private final Rectangle gearBtnRect = new Rectangle();
    private boolean gearHover = false;

    public InfoPanel(GameModel model) {
        this.model = model;
        setPreferredSize(new Dimension(180, Board.ROWS * BoardPanel.getCellSize()));
        setBackground(BG_COLOR);
        setFocusable(false);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (gearBtnRect.contains(e.getPoint())) openSettings();
            }
            @Override public void mouseExited(MouseEvent e) {
                gearHover = false; repaint();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                boolean was = gearHover;
                gearHover = gearBtnRect.contains(e.getPoint());
                if (was != gearHover) repaint();
            }
        });
    }

    public void setParentFrame(JFrame f) { parentFrame = f; }
    public void setOnRestart(Runnable r)  { onRestart = r; }

    private void openSettings() {
        if (parentFrame == null) return;
        SettingsDialog dlg = new SettingsDialog(parentFrame, model,
            onRestart != null ? onRestart : () -> {});
        dlg.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ── Gear icon top-right ───────────────────────────────
        int gSize = 32, gx = getWidth() - gSize - 6, gy = 6;
        gearBtnRect.setBounds(gx, gy, gSize, gSize);
        g2.setColor(gearHover ? new Color(0, 200, 255, 60) : new Color(255,255,255,10));
        g2.fillRoundRect(gx, gy, gSize, gSize, 8, 8);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 20));
        g2.setColor(gearHover ? ACCENT_COLOR : new Color(160,160,160));
        FontMetrics fm = g2.getFontMetrics();
        String gear = "⚙";
        g2.drawString(gear, gx + (gSize - fm.stringWidth(gear))/2, gy + gSize - (gSize - fm.getAscent())/2 - 2);

        int y = 45;

        // ── NEXT ─────────────────────────────────────────────
        y = drawLabel(g2, "NEXT", y);
        y = drawNextPiece(g2, y + 5);
        y += 20;

        // ── SCORE / LEVEL / LINES ─────────────────────────────
        y = drawLabel(g2, "SCORE", y);
        y = drawValue(g2, String.valueOf(model.getScore()), y);
        y += 15;
        y = drawLabel(g2, "LEVEL", y);
        y = drawValue(g2, String.valueOf(model.getLevel()), y);
        y += 15;
        y = drawLabel(g2, "LINES", y);
        y = drawValue(g2, String.valueOf(model.getLinesCleared()), y);
        y += 25;

        // ── CONTROLS ─────────────────────────────────────────
        y = drawLabel(g2, "CONTROLS", y);
        y += 5;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(LABEL_COLOR);
        String[] controls = {
            "\u2190 \u2192   Move",
            "\u2191      Rotate",
            "\u2193      Soft Drop",
            "Space  Hard Drop",
            "P      Pause",
            "Enter  Start/Restart"
        };
        for (String ctrl : controls) {
            g2.drawString(ctrl, 15, y);
            y += 17;
        }
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
        int minX=Integer.MAX_VALUE,maxX=Integer.MIN_VALUE,minY=Integer.MAX_VALUE,maxY=Integer.MIN_VALUE;
        for (int[] b : blocks) { minX=Math.min(minX,b[0]); maxX=Math.max(maxX,b[0]); minY=Math.min(minY,b[1]); maxY=Math.max(maxY,b[1]); }
        int pw=(maxX-minX+1)*PREVIEW_CELL, ph=(maxY-minY+1)*PREVIEW_CELL;
        int ox=(getWidth()-pw)/2-minX*PREVIEW_CELL;
        g.setColor(new Color(45,45,45));
        g.fillRoundRect(10,startY-5,getWidth()-20,ph+15,8,8);
        for (int[] b : blocks) {
            int px=ox+b[0]*PREVIEW_CELL, py=startY+(b[1]-minY)*PREVIEW_CELL;
            g.setColor(color); g.fillRect(px+1,py+1,PREVIEW_CELL-2,PREVIEW_CELL-2);
            g.setColor(color.brighter()); g.drawLine(px+1,py+1,px+PREVIEW_CELL-2,py+1); g.drawLine(px+1,py+1,px+1,py+PREVIEW_CELL-2);
        }
        return startY+ph+15;
    }
}
