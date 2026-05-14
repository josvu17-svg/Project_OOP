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

    private static final int LINES_PER_LEVEL = 10;

    public GameModel() {
        board = new Board();
        factory = new TetrominoFactory();
        scoreStrategy = new ClassicScoreStrategy();
        listeners = new ArrayList<>();
        state = GameState.READY;
    }

    public void addListener(GameListener listener) { listeners.add(listener); }
    public void removeListener(GameListener listener) { listeners.remove(listener); }

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
    private void notifyLinesCleared(List<Integer> rows, int count) {
        for (GameListener l : listeners) l.onLinesCleared(rows, count);
    }

    public void setScoreStrategy(ScoreStrategy strategy) { this.scoreStrategy = strategy; }

    public void startGame() {
        board.clear();
        score = 0; linesCleared = 0; level = 0;
        state = GameState.PLAYING;
        spawnPiece();
        notifyScoreChanged();
        notifyBoardChanged();
    }

    public void togglePause() {
        if (state == GameState.PLAYING) state = GameState.PAUSED;
        else if (state == GameState.PAUSED) state = GameState.PLAYING;
        notifyBoardChanged();
    }

    private void spawnPiece() {
        if (nextPiece == null) {
            currentPiece = factory.createPiece();
            nextPiece = factory.createPiece();
        } else {
            currentPiece = nextPiece;
            nextPiece = factory.createPiece();
        }
        if (!board.isValidPosition(currentPiece.getAbsoluteBlocks())) {
            state = GameState.GAME_OVER;
            notifyGameOver();
            return;
        }
        notifyPieceChanged();
    }

    public void moveLeft() {
        if (state != GameState.PLAYING) return;
        currentPiece.moveLeft();
        if (!board.isValidPosition(currentPiece.getAbsoluteBlocks())) currentPiece.moveRight();
        notifyBoardChanged();
    }

    public void moveRight() {
        if (state != GameState.PLAYING) return;
        currentPiece.moveRight();
        if (!board.isValidPosition(currentPiece.getAbsoluteBlocks())) currentPiece.moveLeft();
        notifyBoardChanged();
    }

    public boolean moveDown() {
        if (state != GameState.PLAYING) return false;
        currentPiece.moveDown();
        if (!board.isValidPosition(currentPiece.getAbsoluteBlocks())) {
            currentPiece.moveUp();
            lockAndAdvance();
            return true;
        }
        notifyBoardChanged();
        return false;
    }

    public void softDrop() {
        if (state != GameState.PLAYING) return;
        if (!moveDown()) {
            score += scoreStrategy.calculateSoftDropScore(1);
            notifyScoreChanged();
        }
    }

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

    public void rotate() {
        if (state != GameState.PLAYING) return;
        currentPiece.rotate();
        if (!board.isValidPosition(currentPiece.getAbsoluteBlocks())) {
            currentPiece.setX(currentPiece.getX() - 1);
            if (!board.isValidPosition(currentPiece.getAbsoluteBlocks())) {
                currentPiece.setX(currentPiece.getX() + 2);
                if (!board.isValidPosition(currentPiece.getAbsoluteBlocks())) {
                    currentPiece.setX(currentPiece.getX() - 1);
                    currentPiece.rotateBack();
                }
            }
        }
        notifyBoardChanged();
    }

    private void lockAndAdvance() {
        board.lockPiece(currentPiece);
        int cleared = board.clearLines();
        if (cleared > 0) {
            // Notify with row info BEFORE updating score/state
            List<Integer> clearedRows = board.getLastClearedRows();
            notifyLinesCleared(clearedRows, cleared);

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

    public int getDropInterval() {
        return Math.max(100, 800 - (level * 60));
    }

    public void tick() {
        if (state == GameState.PLAYING) moveDown();
    }

    public Board getBoard()             { return board; }
    public Tetromino getCurrentPiece()  { return currentPiece; }
    public Tetromino getNextPiece()     { return nextPiece; }
    public GameState getState()         { return state; }
    public int getScore()               { return score; }
    public int getLinesCleared()        { return linesCleared; }
    public int getLevel()               { return level; }
}
