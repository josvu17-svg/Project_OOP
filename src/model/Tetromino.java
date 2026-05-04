package model;

import java.awt.Color;

/**
 * Represents a Tetromino piece on the board.
 * Stores its type, position, and current rotation state.
 */
public class Tetromino {
    private TetrominoType type;
    private int x, y;         // position on board (top-left of bounding box)
    private int rotation;     // 0-3

    public Tetromino(TetrominoType type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.rotation = 0;
    }

    /** Get absolute board positions of all 4 blocks */
    public int[][] getAbsoluteBlocks() {
        int[][] relativeBlocks = type.getBlocks(rotation);
        int[][] absolute = new int[4][2];
        for (int i = 0; i < 4; i++) {
            absolute[i][0] = x + relativeBlocks[i][0];
            absolute[i][1] = y + relativeBlocks[i][1];
        }
        return absolute;
    }

    /** Get blocks for a hypothetical rotation (for collision check) */
    public int[][] getBlocksAtRotation(int rot) {
        int[][] relativeBlocks = type.getBlocks(rot);
        int[][] absolute = new int[4][2];
        for (int i = 0; i < 4; i++) {
            absolute[i][0] = x + relativeBlocks[i][0];
            absolute[i][1] = y + relativeBlocks[i][1];
        }
        return absolute;
    }

    /** Get blocks at a hypothetical position */
    public int[][] getBlocksAt(int newX, int newY) {
        int[][] relativeBlocks = type.getBlocks(rotation);
        int[][] absolute = new int[4][2];
        for (int i = 0; i < 4; i++) {
            absolute[i][0] = newX + relativeBlocks[i][0];
            absolute[i][1] = newY + relativeBlocks[i][1];
        }
        return absolute;
    }

    public void moveLeft()  { x--; }
    public void moveRight() { x++; }
    public void moveDown()  { y++; }
    public void moveUp()    { y--; }

    public void rotate() { rotation = (rotation + 1) % 4; }
    public void rotateBack() { rotation = (rotation + 3) % 4; }

    public TetrominoType getType()  { return type; }
    public Color getColor()         { return type.getColor(); }
    public int getX()               { return x; }
    public int getY()               { return y; }
    public int getRotation()        { return rotation; }
    public void setX(int x)         { this.x = x; }
    public void setY(int y)         { this.y = y; }
}
