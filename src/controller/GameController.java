package controller;

import model.GameModel;
import model.GameState;
import pattern.GameListener;
import pattern.SoundManager;
import view.BoardPanel;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class GameController extends KeyAdapter implements GameListener {
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
        // Start BGM when game launches
        SoundManager.startBgm();
    }

    private void setupTimer() {
        gameTimer = new Timer(model.getDropInterval(), e -> model.tick());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                model.moveLeft();
                SoundManager.playMove();
                break;
            case KeyEvent.VK_RIGHT:
                model.moveRight();
                SoundManager.playMove();
                break;
            case KeyEvent.VK_DOWN:
                model.softDrop();
                break;
            case KeyEvent.VK_UP:
                model.rotate();
                SoundManager.playRotate();
                break;
            case KeyEvent.VK_SPACE:
                model.hardDrop();
                SoundManager.playHardDrop();
                break;
            case KeyEvent.VK_P:
                model.togglePause();
                if (model.getState() == GameState.PAUSED) gameTimer.stop();
                else if (model.getState() == GameState.PLAYING) gameTimer.start();
                break;
            case KeyEvent.VK_ENTER:
                if (model.getState() == GameState.READY ||
                    model.getState() == GameState.GAME_OVER) {
                    model.startGame();
                    gameTimer.setDelay(model.getDropInterval());
                    gameTimer.start();
                }
                break;
        }
    }

    @Override public void onScoreChanged(int score, int lines, int level) { infoPanel.repaint(); }

    @Override
    public void onGameOver(int finalScore) {
        gameTimer.stop();
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
            case 1: SoundManager.playSingle();  break;
            case 2: SoundManager.playDouble();  break;
            case 3: SoundManager.playTriple();  break;
            case 4: SoundManager.playTetris();  break;
        }
    }
}
