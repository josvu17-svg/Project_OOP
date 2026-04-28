package model;

import pattern.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Core game model containing all Tetris game logic.
 * Notifies registered GameListeners (Observer Pattern) on state changes.
 * Uses ScoreStrategy (Strategy Pattern) for score calculation.
 * Uses TetrominoFactory (Factory Pattern) for piece generation.
 */
public class GameModel {
    private Board board;
    private Tetromino currentPiece;
    private Tetromino nextPiece;
    private GameState state;

    private int score;
    private int linesCleared;
    private int level;

    private final TetrominoFactory factory;
    private ScoreStrategy scoreStrategy;
    private final List<GameListener> listeners;

    // Level up every 10 lines
    private static final int LINES_PER_LEVEL = 10;

    public GameModel() {
        board = new Board();
        factory = new TetrominoFactory();
        scoreStrategy = new ClassicScoreStrategy();
        listeners = new ArrayList<>();
        state = GameState.READY;
    }

    // ── Observer Pattern: register/remove listeners ──────────────
    public void addListener(GameListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GameListener listener) {
        listeners.remove(listener);
    }

    private void notifyScoreChanged() {
        for (GameListener l : listeners) l.onScoreChanged(score, linesCleared, level);
    }

    private void notifyGameOver() {
        for (GameListener l : listeners) l.onGameOver(score);
    }

    private void notifyPieceChanged() {
        for (GameListener l : listeners) l.onPieceChanged();
    }

    private void notifyBoardChanged() {
        for (GameListener l : listeners) l.onBoardChanged();
    }

    private void notifyLevelUp() {
        for (GameListener l : listeners) l.onLevelUp(level);
    }

    // ── Strategy Pattern: swap scoring ───────────────────────────
    public void setScoreStrategy(ScoreStrategy strategy) {
        this.scoreStrategy = strategy;
    }

    // ── Game lifecycle ───────────────────────────────────────────
    public void startGame() {
        board.clear();
        score = 0;
        linesCleared = 0;
        level = 0;
        state = GameState.PLAYING;
        spawnPiece();
        notifyScoreChanged();
        notifyBoardChanged();
    }

    public void togglePause() {
        if (state == GameState.PLAYING) {
            state = GameState.PAUSED;
        } else if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
        }
        notifyBoardChanged();
    }

    // ── Piece spawning ───────────────────────────────────────────
    private void spawnPiece() {
        if (nextPiece == null) {
            currentPiece = factory.createPiece();
            nextPiece = factory.createPiece();
        } else {
            currentPiece = nextPiece;
            nextPiece = factory.createPiece();
        }

        // Check if spawn position is valid — if not, game over
        if (!board.isValidPosition(currentPiece.getAbsoluteBlocks())) {
            state = GameState.GAME_OVER;
            notifyGameOver();
            return;
        }
        notifyPieceChanged();
    }

    // ── Movement ─────────────────────────────────────────────────
    public void moveLeft() {
        if (state != GameState.PLAYING) return;
        currentPiece.moveLeft();
        if (!board.isValidPosition(currentPiece.getAbsoluteBlocks())) {
            currentPiece.moveRight(); // undo
        }
        notifyBoardChanged();
    }

    public void moveRight() {
        if (state != GameState.PLAYING) return;
        currentPiece.moveRight();
        if (!board.isValidPosition(currentPiece.getAbsoluteBlocks())) {
            currentPiece.moveLeft(); // undo
        }
        notifyBoardChanged();
    }

    /** Move piece down by one. Returns true if piece was locked. */
    public boolean moveDown() {
        if (state != GameState.PLAYING) return false;
        currentPiece.moveDown();
        if (!board.isValidPosition(currentPiece.getAbsoluteBlocks())) {
            currentPiece.moveUp(); // undo
            lockAndAdvance();
            return true;
        }
        notifyBoardChanged();
        return false;
    }

    /** Soft drop — move down and add score */
    public void softDrop() {
        if (state != GameState.PLAYING) return;
        if (!moveDown()) {
            score += scoreStrategy.calculateSoftDropScore(1);
            notifyScoreChanged();
        }
    }

    /** Hard drop — instantly drop to bottom */
    public void hardDrop() {
        if (state != GameState.PLAYING) return;
        int cellsDropped = 0;
        while (true) {
            currentPiece.moveDown();
            if (!board.isValidPosition(currentPiece.getAbsoluteBlocks())) {
                currentPiece.moveUp();
                break;
            }
            cellsDropped++;
        }
        score += scoreStrategy.calculateHardDropScore(cellsDropped);
        notifyScoreChanged();
        lockAndAdvance();
    }

    /** Rotate piece clockwise with wall kick */
    public void rotate() {
        if (state != GameState.PLAYING) return;
        currentPiece.rotate();
        if (!board.isValidPosition(currentPiece.getAbsoluteBlocks())) {
            // Wall kick: try shifting left, then right
            currentPiece.setX(currentPiece.getX() - 1);
            if (!board.isValidPosition(currentPiece.getAbsoluteBlocks())) {
                currentPiece.setX(currentPiece.getX() + 2);
                if (!board.isValidPosition(currentPiece.getAbsoluteBlocks())) {
                    currentPiece.setX(currentPiece.getX() - 1); // restore
                    currentPiece.rotateBack(); // undo rotation
                }
            }
        }
        notifyBoardChanged();
    }

    // ── Lock piece and handle line clears ────────────────────────
    private void lockAndAdvance() {
        board.lockPiece(currentPiece);
        int cleared = board.clearLines();
        if (cleared > 0) {
            linesCleared += cleared;
            score += scoreStrategy.calculateScore(cleared, level);
            int newLevel = linesCleared / LINES_PER_LEVEL;
            if (newLevel > level) {
                level = newLevel;
                notifyLevelUp();
            }
            notifyScoreChanged();
        }
        notifyBoardChanged();
        spawnPiece();
    }

    /** Get the ghost piece position (preview of where piece will land) */
    public int getGhostY() {
        if (currentPiece == null) return 0;
        int ghostY = currentPiece.getY();
        while (true) {
            int[][] blocks = currentPiece.getBlocksAt(currentPiece.getX(), ghostY + 1);
            if (!board.isValidPosition(blocks)) break;
            ghostY++;
        }
        return ghostY;
    }

    /** Get drop speed in milliseconds based on level */
    public int getDropInterval() {
        // Speed curve: starts at 800ms, decreases with level
        int interval = Math.max(100, 800 - (level * 60));
        return interval;
    }

    // ── Tick (called by game timer) ──────────────────────────────
    public void tick() {
        if (state == GameState.PLAYING) {
            moveDown();
        }
    }

    // ── Getters ──────────────────────────────────────────────────
    public Board getBoard()             { return board; }
    public Tetromino getCurrentPiece()  { return currentPiece; }
    public Tetromino getNextPiece()     { return nextPiece; }
    public GameState getState()         { return state; }
    public int getScore()               { return score; }
    public int getLinesCleared()        { return linesCleared; }
    public int getLevel()               { return level; }
}
