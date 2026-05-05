package model;

import java.awt.Color;

/**
 * Enum representing the 7 standard Tetromino types.
 * Each type has a color and shape definitions for all 4 rotation states.
 */
public enum TetrominoType {
    I(new Color(0, 240, 240), new int[][][] {
        {{0,0},{1,0},{2,0},{3,0}},
        {{0,0},{0,1},{0,2},{0,3}},
        {{0,0},{1,0},{2,0},{3,0}},
        {{0,0},{0,1},{0,2},{0,3}}
    }),
    O(new Color(240, 240, 0), new int[][][] {
        {{0,0},{1,0},{0,1},{1,1}},
        {{0,0},{1,0},{0,1},{1,1}},
        {{0,0},{1,0},{0,1},{1,1}},
        {{0,0},{1,0},{0,1},{1,1}}
    }),
    T(new Color(160, 0, 240), new int[][][] {
        {{0,0},{1,0},{2,0},{1,1}},
        {{0,0},{0,1},{0,2},{1,1}},
        {{1,0},{0,1},{1,1},{2,1}},
        {{1,0},{1,1},{1,2},{0,1}}
    }),
    S(new Color(0, 240, 0), new int[][][] {
        {{1,0},{2,0},{0,1},{1,1}},
        {{0,0},{0,1},{1,1},{1,2}},
        {{1,0},{2,0},{0,1},{1,1}},
        {{0,0},{0,1},{1,1},{1,2}}
    }),
    Z(new Color(240, 0, 0), new int[][][] {
        {{0,0},{1,0},{1,1},{2,1}},
        {{1,0},{0,1},{1,1},{0,2}},
        {{0,0},{1,0},{1,1},{2,1}},
        {{1,0},{0,1},{1,1},{0,2}}
    }),
    J(new Color(0, 0, 240), new int[][][] {
        {{0,0},{0,1},{1,1},{2,1}},
        {{0,0},{1,0},{0,1},{0,2}},
        {{0,0},{1,0},{2,0},{2,1}},
        {{1,0},{1,1},{0,2},{1,2}}
    }),
    L(new Color(240, 160, 0), new int[][][] {
        {{2,0},{0,1},{1,1},{2,1}},
        {{0,0},{0,1},{0,2},{1,2}},
        {{0,0},{1,0},{2,0},{0,1}},
        {{0,0},{1,0},{1,1},{1,2}}
    });

    private final Color color;
    private final int[][][] rotations; // [rotationState][blockIndex][x,y]

    TetrominoType(Color color, int[][][] rotations) {
        this.color = color;
        this.rotations = rotations;
    }

    public Color getColor() { return color; }

    /**
     * Get block positions for a given rotation state.
     * @param rotation 0-3
     * @return array of [x,y] pairs for each of the 4 blocks
     */
    public int[][] getBlocks(int rotation) {
        return rotations[rotation % 4];
    }
}
