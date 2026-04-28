package model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Tetris board (grid).
 * Handles collision detection, piece placement, and line clearing.
 */
public class Board {
    public static final int COLS = 10;
    public static final int ROWS = 20;
    public static final int HIDDEN_ROWS = 2; // extra rows above visible area

    private Color[][] grid; // null = empty cell

    public Board() {
        grid = new Color[ROWS + HIDDEN_ROWS][COLS];
    }

    /** Check if a position is valid (in bounds and not occupied) */
    public boolean isValidPosition(int[][] blocks) {
        for (int[] block : blocks) {
            int bx = block[0];
            int by = block[1];
            if (bx < 0 || bx >= COLS || by >= ROWS + HIDDEN_ROWS) return false;
            if (by < 0) continue; // allow above top
            if (grid[by][bx] != null) return false;
        }
        return true;
    }

    /** Lock a piece into the grid */
    public void lockPiece(Tetromino piece) {
        int[][] blocks = piece.getAbsoluteBlocks();
        for (int[] block : blocks) {
            int bx = block[0];
            int by = block[1];
            if (by >= 0 && by < ROWS + HIDDEN_ROWS && bx >= 0 && bx < COLS) {
                grid[by][bx] = piece.getColor();
            }
        }
    }

    /**
     * Clear completed lines and return number cleared.
     * FIX: Rebuild grid by collecting non-full rows, avoiding index-shift bug.
     */
    public int clearLines() {
        int totalRows = ROWS + HIDDEN_ROWS;
        List<Color[]> remaining = new ArrayList<>();
        int cleared = 0;

        // Scan from bottom to top, keep non-full rows
        for (int row = totalRows - 1; row >= 0; row--) {
            boolean full = true;
            for (int col = 0; col < COLS; col++) {
                if (grid[row][col] == null) { full = false; break; }
            }
            if (full) {
                cleared++;
            } else {
                remaining.add(grid[row].clone());
            }
        }

        if (cleared == 0) return 0;

        // Rebuild grid: fill from bottom with kept rows, pad top with empty rows
        for (int row = totalRows - 1; row >= 0; row--) {
            int remainIdx = totalRows - 1 - row;
            grid[row] = (remainIdx < remaining.size()) ? remaining.get(remainIdx) : new Color[COLS];
        }

        return cleared;
    }

    /** Check if game is over (blocks in hidden rows locked) */
    public boolean isTopReached() {
        for (int col = 0; col < COLS; col++) {
            if (grid[HIDDEN_ROWS][col] != null) return false;
        }
        for (int row = 0; row < HIDDEN_ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (grid[row][col] != null) return true;
            }
        }
        return false;
    }

    /** Get color at a grid position (for rendering) */
    public Color getColorAt(int row, int col) {
        if (row < 0 || row >= ROWS + HIDDEN_ROWS || col < 0 || col >= COLS) return null;
        return grid[row][col];
    }

    /** Get color at visible position (offset by hidden rows) */
    public Color getVisibleColorAt(int visibleRow, int col) {
        return getColorAt(visibleRow + HIDDEN_ROWS, col);
    }

    /** Reset the board */
    public void clear() {
        grid = new Color[ROWS + HIDDEN_ROWS][COLS];
    }

    /** Check if a visible row/col is occupied */
    public boolean isOccupied(int visibleRow, int col) {
        return getVisibleColorAt(visibleRow, col) != null;
    }
}
