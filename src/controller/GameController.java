package controller;

import model.GameModel;
import model.GameState;
import pattern.GameListener;
import pattern.SoundManager;
import view.BoardPanel;

import javax.swing.*;
import java.util.List;

/**
 * Controller in MVC pattern.
 * Keyboard input is now handled via KeyBindings in TetrisGame (WHEN_IN_FOCUSED_WINDOW)
 * so it works regardless of which component has focus.
 */
public class GameController implements GameListener {
    private final GameModel model;
    private final BoardPanel boardPanel;
    private final JPanel infoPanel;
    private Timer gameTimer;

    public GameController(GameModel model, BoardPanel boardPanel, JPanel infoPanel) {
        this.model = model;
        this.boardPanel = boardPanel;
        this.infoPanel = infoPanel;
        model.addListener(this);
        setupTimer();
    }

    private void setupTimer() {
        gameTimer = new Timer(model.getDropInterval(), e -> model.tick());
    }

    // ── Called by KeyBindings in TetrisGame ───────────────────
    public void handlePauseToggle() {
        model.togglePause();
        if (model.getState() == GameState.PAUSED) gameTimer.stop();
        else if (model.getState() == GameState.PLAYING) gameTimer.start();
    }

    public void handleStartGame() {
        model.startGame();
        gameTimer.setDelay(model.getDropInterval());
        gameTimer.start();
    }

    @Override public void onScoreChanged(int score, int lines, int level) { infoPanel.repaint(); }

    @Override
    public void onGameOver(int finalScore) {
        gameTimer.stop();
        SoundManager.stopBgm();
        SoundManager.playGameOver();
        boardPanel.repaint();
        infoPanel.repaint();
    }

    @Override
    public void onPieceChanged() {
        SoundManager.playLock();
        boardPanel.repaint();
        infoPanel.repaint();
    }

    @Override public void onBoardChanged() { boardPanel.repaint(); }

    @Override
    public void onLevelUp(int newLevel) {
        gameTimer.setDelay(model.getDropInterval());
        SoundManager.playLevelUp();
        infoPanel.repaint();
    }

    @Override
    public void onLinesCleared(List<Integer> clearedRows, int linesCount) {
        boardPanel.triggerLineClear(clearedRows, linesCount);
        switch (linesCount) {
            case 1: SoundManager.playSingle(); break;
            case 2: SoundManager.playDouble(); break;
            case 3: SoundManager.playTriple(); break;
            case 4: SoundManager.playTetris(); break;
        }
    }
}
