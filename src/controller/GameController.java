package controller;

import model.GameModel;
import model.GameState;
import pattern.GameListener;
import pattern.SoundManager;
import view.BoardPanel;
import view.GameOverDialog;

import javax.swing.*;
import java.util.List;

/**
 * Controller — game events, timer management.
 */
public class GameController implements GameListener {
    private final GameModel  model;
    private final BoardPanel boardPanel;
    private final JPanel     infoPanel;
    private Timer   gameTimer;
    private JFrame  parentFrame;

    public GameController(GameModel model, BoardPanel boardPanel, JPanel infoPanel) {
        this.model      = model;
        this.boardPanel = boardPanel;
        this.infoPanel  = infoPanel;
        model.addListener(this);
        setupTimer();
    }

    public void setParentFrame(JFrame f) { parentFrame = f; }

    private void setupTimer() {
        gameTimer = new Timer(model.getDropInterval(), e -> model.tick());
    }

    // ── Called by TetrisGame KeyBindings ──────────────────────
    public void handlePauseToggle() {
        model.togglePause();
        if (model.getState() == GameState.PAUSED) gameTimer.stop();
        else if (model.getState() == GameState.PLAYING) gameTimer.start();
    }

    public void handleStartGame() {
        gameTimer.setDelay(model.getDropInterval());
        gameTimer.start();
    }

    /** Pause game before opening Settings, resume after */
    public void openSettings(JFrame frame, Runnable onRestart, Runnable onBackToMenu) {
        // Force pause if playing
        if (model.getState() == GameState.PLAYING) {
            model.togglePause();
            gameTimer.stop();
            boardPanel.repaint();
        }

        view.SettingsDialog dlg = new view.SettingsDialog(frame, model, () -> {
            model.startGame();
            handleStartGame();
            SoundManager.startBgm();
            onRestart.run();
        });
        dlg.setOnBackToMenu(() -> {
            SoundManager.stopBgm();
            onBackToMenu.run();
        });
        dlg.setOnClose(() -> {
            // Resume game after settings closed
            if (model.getState() == GameState.PAUSED) {
                model.togglePause();
                gameTimer.start();
                boardPanel.repaint();
            }
            SwingUtilities.invokeLater(frame::requestFocusInWindow);
        });
        dlg.setVisible(true);
    }

    public void stopTimer()  { gameTimer.stop(); }
    public void startTimer() { gameTimer.start(); }
    public boolean isTimerRunning() { return gameTimer.isRunning(); }

    // ── GameListener callbacks ────────────────────────────────
    @Override
    public void onScoreChanged(int score, int lines, int level) { infoPanel.repaint(); }

    @Override
    public void onGameOver(int finalScore) {
        gameTimer.stop();
        SoundManager.stopBgm();
        SoundManager.playGameOver();
        boardPanel.repaint();
        infoPanel.repaint();

        // Show custom Game Over dialog
        SwingUtilities.invokeLater(() ->
            GameOverDialog.show(parentFrame, finalScore,
                model.getLevel(), model.getLinesCleared())
        );
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
