package view;

import model.Board;
import model.GameModel;
import model.GameState;
import model.Tetromino;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * JPanel that renders the Tetris game board.
 * Displays: grid, locked pieces, current piece, ghost piece.
 * Added: Line clear explosion effect (scales with lines cleared).
 */
public class BoardPanel extends JPanel {
    private static final int CELL_SIZE = 30;
    private static final Color GRID_LINE_COLOR = new Color(55, 55, 55);
    private static final Color GHOST_ALPHA = new Color(255, 255, 255, 50);
    private static final Color BG_COLOR = new Color(25, 25, 25);

    private final GameModel model;

    // ── Explosion effect state ────────────────────────────────
    private final List<Particle> particles = new ArrayList<>();
    private final List<FlashRow> flashRows = new ArrayList<>();
    private Timer animTimer;
    private final Random rng = new Random();

    public BoardPanel(GameModel model) {
        this.model = model;
        int width = Board.COLS * CELL_SIZE + 1;
        int height = Board.ROWS * CELL_SIZE + 1;
        setPreferredSize(new Dimension(width, height));
        setBackground(BG_COLOR);

        // Animation timer: 60fps
        animTimer = new Timer(16, e -> {
            tickParticles();
            if (!particles.isEmpty() || !flashRows.isEmpty()) repaint();
        });
        animTimer.start();
    }

    // ── Public API: trigger explosion ─────────────────────────
    /**
     * Call this when lines are cleared.
     * @param clearedRows  visible row indices (0-based) that were cleared
     * @param linesCount   how many lines cleared (1-4)
     */
    public void triggerLineClear(List<Integer> clearedRows, int linesCount) {
        // Colors scale with line count
        Color[] palette = getPalette(linesCount);
        int particlesPerRow = 8 + linesCount * 6;   // 14 / 20 / 26 / 32
        float speedMult = 1.0f + linesCount * 0.5f; // 1.5 / 2.0 / 2.5 / 3.0
        int flashFrames = 6 + linesCount * 3;        // 9 / 12 / 15 / 18

        for (int row : clearedRows) {
            // Flash the row
            flashRows.add(new FlashRow(row, flashFrames, linesCount));

            // Spawn particles along the row
            for (int i = 0; i < particlesPerRow; i++) {
                float px = rng.nextFloat() * Board.COLS * CELL_SIZE;
                float py = row * CELL_SIZE + CELL_SIZE / 2f;
                float angle = (float)(rng.nextFloat() * Math.PI * 2);
                float speed = (1.5f + rng.nextFloat() * 3.5f) * speedMult;
                float vx = (float)Math.cos(angle) * speed;
                float vy = (float)Math.sin(angle) * speed - speedMult; // bias upward
                Color c = palette[rng.nextInt(palette.length)];
                int life = 25 + rng.nextInt(20) + linesCount * 5;
                float size = 3 + rng.nextFloat() * 4 * linesCount;
                particles.add(new Particle(px, py, vx, vy, c, life, size));
            }
        }

        // For Tetris (4 lines): add big burst from center
        if (linesCount == 4) {
            float cx = Board.COLS * CELL_SIZE / 2f;
            float cy = clearedRows.get(clearedRows.size() / 2) * CELL_SIZE + CELL_SIZE / 2f;
            for (int i = 0; i < 40; i++) {
                float angle = (float)(i / 40.0 * Math.PI * 2);
                float speed = 4 + rng.nextFloat() * 6;
                float vx = (float)Math.cos(angle) * speed;
                float vy = (float)Math.sin(angle) * speed;
                Color c = palette[rng.nextInt(palette.length)];
                particles.add(new Particle(cx, cy, vx, vy, c, 45, 5 + rng.nextFloat() * 5));
            }
        }
    }

    private Color[] getPalette(int lines) {
        switch (lines) {
            case 1: return new Color[]{ // White/blue — subtle
                new Color(200, 220, 255), new Color(150, 180, 255),
                new Color(255, 255, 255), new Color(100, 150, 255)
            };
            case 2: return new Color[]{ // Green
                new Color(100, 255, 150), new Color(50, 220, 100),
                new Color(200, 255, 200), new Color(0, 200, 80)
            };
            case 3: return new Color[]{ // Orange/red
                new Color(255, 150, 50), new Color(255, 80, 30),
                new Color(255, 220, 100), new Color(220, 50, 20)
            };
            default: return new Color[]{ // Gold/yellow — Tetris!
                new Color(255, 220, 0), new Color(255, 180, 0),
                new Color(255, 255, 100), new Color(255, 140, 0),
                new Color(255, 255, 255)
            };
        }
    }

    private void tickParticles() {
        particles.removeIf(p -> {
            p.x += p.vx;
            p.y += p.vy;
            p.vy += 0.18f; // gravity
            p.vx *= 0.97f; // friction
            p.life--;
            p.alpha = Math.max(0, (int)(255 * p.life / (float)p.maxLife));
            return p.life <= 0;
        });
        flashRows.removeIf(f -> {
            f.framesLeft--;
            return f.framesLeft <= 0;
        });
    }

    // ── Painting ──────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2d);
        drawLockedBlocks(g2d);

        if (model.getState() == GameState.PLAYING || model.getState() == GameState.PAUSED) {
            drawGhostPiece(g2d);
            drawCurrentPiece(g2d);
        }

        // Draw effects on top
        drawFlashRows(g2d);
        drawParticles(g2d);

        if (model.getState() == GameState.PAUSED) {
            drawOverlay(g2d, "PAUSED", "Press P to resume");
        } else if (model.getState() == GameState.GAME_OVER) {
            drawOverlay(g2d, "GAME OVER", "Press Enter to restart");
        } else if (model.getState() == GameState.READY) {
            drawOverlay(g2d, "TETRIS", "Press Enter to start");
        }
    }

    private void drawFlashRows(Graphics2D g) {
        for (FlashRow f : flashRows) {
            float progress = f.framesLeft / (float)f.totalFrames;
            int alpha = (int)(180 * progress);
            Color flashColor;
            switch (f.lines) {
                case 4:  flashColor = new Color(255, 220, 0,   alpha); break;
                case 3:  flashColor = new Color(255, 100, 0,   alpha); break;
                case 2:  flashColor = new Color(50,  255, 100, alpha); break;
                default: flashColor = new Color(180, 200, 255, alpha); break;
            }
            g.setColor(flashColor);
            g.fillRect(0, f.row * CELL_SIZE, Board.COLS * CELL_SIZE, CELL_SIZE);
        }
    }

    private void drawParticles(Graphics2D g) {
        for (Particle p : particles) {
            Color c = new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), p.alpha);
            g.setColor(c);
            float drawSize = p.size * (p.life / (float)p.maxLife * 0.5f + 0.5f);
            g.fill(new Ellipse2D.Float(p.x - drawSize/2, p.y - drawSize/2, drawSize, drawSize));
        }
    }

    private void drawGrid(Graphics2D g) {
        g.setColor(GRID_LINE_COLOR);
        for (int row = 0; row <= Board.ROWS; row++) {
            g.drawLine(0, row * CELL_SIZE, Board.COLS * CELL_SIZE, row * CELL_SIZE);
        }
        for (int col = 0; col <= Board.COLS; col++) {
            g.drawLine(col * CELL_SIZE, 0, col * CELL_SIZE, Board.ROWS * CELL_SIZE);
        }
    }

    private void drawLockedBlocks(Graphics2D g) {
        Board board = model.getBoard();
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                Color color = board.getVisibleColorAt(row, col);
                if (color != null) drawBlock(g, col, row, color);
            }
        }
    }

    private void drawCurrentPiece(Graphics2D g) {
        Tetromino piece = model.getCurrentPiece();
        if (piece == null) return;
        int[][] blocks = piece.getAbsoluteBlocks();
        for (int[] block : blocks) {
            int drawRow = block[1] - Board.HIDDEN_ROWS;
            if (drawRow >= 0) drawBlock(g, block[0], drawRow, piece.getColor());
        }
    }

    private void drawGhostPiece(Graphics2D g) {
        Tetromino piece = model.getCurrentPiece();
        if (piece == null) return;
        int ghostY = model.getGhostY();
        int[][] relBlocks = piece.getType().getBlocks(piece.getRotation());
        for (int[] block : relBlocks) {
            int drawCol = piece.getX() + block[0];
            int drawRow = ghostY + block[1] - Board.HIDDEN_ROWS;
            if (drawRow >= 0) {
                int x = drawCol * CELL_SIZE + 1;
                int y = drawRow * CELL_SIZE + 1;
                g.setColor(GHOST_ALPHA);
                g.fillRect(x, y, CELL_SIZE - 1, CELL_SIZE - 1);
                g.setColor(new Color(piece.getColor().getRed(), piece.getColor().getGreen(),
                        piece.getColor().getBlue(), 80));
                g.drawRect(x, y, CELL_SIZE - 2, CELL_SIZE - 2);
            }
        }
    }

    private void drawBlock(Graphics2D g, int col, int row, Color color) {
        int x = col * CELL_SIZE + 1;
        int y = row * CELL_SIZE + 1;
        int size = CELL_SIZE - 1;
        g.setColor(color);
        g.fillRect(x, y, size, size);
        g.setColor(color.brighter());
        g.drawLine(x, y, x + size - 1, y);
        g.drawLine(x, y, x, y + size - 1);
        g.setColor(color.darker());
        g.drawLine(x + 1, y + size - 1, x + size - 1, y + size - 1);
        g.drawLine(x + size - 1, y + 1, x + size - 1, y + size - 1);
    }

    private void drawOverlay(Graphics2D g, String title, String subtitle) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setFont(new Font("SansSerif", Font.BOLD, 36));
        FontMetrics fm = g.getFontMetrics();
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        int titleY = getHeight() / 2 - 20;
        g.setColor(Color.WHITE);
        g.drawString(title, titleX, titleY);
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        fm = g.getFontMetrics();
        int subX = (getWidth() - fm.stringWidth(subtitle)) / 2;
        g.setColor(new Color(200, 200, 200));
        g.drawString(subtitle, subX, titleY + 35);
    }

    public static int getCellSize() { return CELL_SIZE; }

    // ── Inner classes ─────────────────────────────────────────
    private static class Particle {
        float x, y, vx, vy, size;
        Color color;
        int life, maxLife, alpha;
        Particle(float x, float y, float vx, float vy, Color c, int life, float size) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.color = c; this.life = this.maxLife = life;
            this.size = size; this.alpha = 255;
        }
    }

    private static class FlashRow {
        int row, framesLeft, totalFrames, lines;
        FlashRow(int row, int frames, int lines) {
            this.row = row; this.framesLeft = this.totalFrames = frames; this.lines = lines;
        }
    }
}
