package view;

import model.Board;
import model.GameModel;
import model.GameState;
import model.Tetromino;

import javax.swing.*;
import java.awt.*;

/**
 * JPanel that renders the Tetris game board.
 * Displays: grid, locked pieces, current piece, ghost piece.
 */
public class BoardPanel extends JPanel {
    private static final int CELL_SIZE = 30;
    private static final Color GRID_COLOR = new Color(40, 40, 40);
    private static final Color GRID_LINE_COLOR = new Color(55, 55, 55);
    private static final Color GHOST_ALPHA = new Color(255, 255, 255, 50);
    private static final Color BG_COLOR = new Color(25, 25, 25);

    private final GameModel model;

    public BoardPanel(GameModel model) {
        this.model = model;
        int width = Board.COLS * CELL_SIZE + 1;
        int height = Board.ROWS * CELL_SIZE + 1;
        setPreferredSize(new Dimension(width, height));
        setBackground(BG_COLOR);
    }

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

        if (model.getState() == GameState.PAUSED) {
            drawOverlay(g2d, "PAUSED", "Press P to resume");
        } else if (model.getState() == GameState.GAME_OVER) {
            drawOverlay(g2d, "GAME OVER", "Press Enter to restart");
        } else if (model.getState() == GameState.READY) {
            drawOverlay(g2d, "TETRIS", "Press Enter to start");
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
                if (color != null) {
                    drawBlock(g, col, row, color);
                }
            }
        }
    }

    private void drawCurrentPiece(Graphics2D g) {
        Tetromino piece = model.getCurrentPiece();
        if (piece == null) return;
        int[][] blocks = piece.getAbsoluteBlocks();
        for (int[] block : blocks) {
            int drawRow = block[1] - Board.HIDDEN_ROWS;
            if (drawRow >= 0) {
                drawBlock(g, block[0], drawRow, piece.getColor());
            }
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

        // Main block
        g.setColor(color);
        g.fillRect(x, y, size, size);

        // Highlight (top-left shine)
        g.setColor(color.brighter());
        g.drawLine(x, y, x + size - 1, y);
        g.drawLine(x, y, x, y + size - 1);

        // Shadow (bottom-right)
        g.setColor(color.darker());
        g.drawLine(x + 1, y + size - 1, x + size - 1, y + size - 1);
        g.drawLine(x + size - 1, y + 1, x + size - 1, y + size - 1);
    }

    private void drawOverlay(Graphics2D g, String title, String subtitle) {
        // Semi-transparent overlay
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());

        // Title
        g.setFont(new Font("SansSerif", Font.BOLD, 36));
        FontMetrics fm = g.getFontMetrics();
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        int titleY = getHeight() / 2 - 20;
        g.setColor(Color.WHITE);
        g.drawString(title, titleX, titleY);

        // Subtitle
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        fm = g.getFontMetrics();
        int subX = (getWidth() - fm.stringWidth(subtitle)) / 2;
        g.setColor(new Color(200, 200, 200));
        g.drawString(subtitle, subX, titleY + 35);
    }

    public static int getCellSize() { return CELL_SIZE; }
}
