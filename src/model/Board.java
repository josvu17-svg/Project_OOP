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
    public static final int HIDDEN_ROWS = 2;

    private Color[][] grid;

    public Board() {
        grid = new Color[ROWS + HIDDEN_ROWS][COLS];
    }

    public boolean isValidPosition(int[][] blocks) {
        for (int[] block : blocks) {
            int bx = block[0];
            int by = block[1];
            if (bx < 0 || bx >= COLS || by >= ROWS + HIDDEN_ROWS) return false;
            if (by < 0) continue;
            if (grid[by][bx] != null) return false;
        }
        return true;
    }

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

    public Color getColorAt(int row, int col) {
        if (row < 0 || row >= ROWS + HIDDEN_ROWS || col < 0 || col >= COLS) return null;
        return grid[row][col];
    }

    public Color getVisibleColorAt(int visibleRow, int col) {
        return getColorAt(visibleRow + HIDDEN_ROWS, col);
    }

    public void clear() {
        grid = new Color[ROWS + HIDDEN_ROWS][COLS];
    }

    public boolean isOccupied(int visibleRow, int col) {
        return getVisibleColorAt(visibleRow, col) != null;
    }
}
