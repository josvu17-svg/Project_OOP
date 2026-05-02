package controller;

import model.GameModel;
import model.GameState;
import pattern.GameListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Controller in MVC pattern.
 * Handles keyboard input and the game loop timer.
 * Translates user input into model operations.
 */
public class GameController extends KeyAdapter implements GameListener {
    private final GameModel model;
    private final JPanel boardPanel;
    private final JPanel infoPanel;
    private Timer gameTimer;

    public GameController(GameModel model, JPanel boardPanel, JPanel infoPanel) {
        this.model = model;
        this.boardPanel = boardPanel;
        this.infoPanel = infoPanel;
        model.addListener(this);
        setupTimer();
    }

    private void setupTimer() {
        gameTimer = new Timer(model.getDropInterval(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.tick();
            }
        });
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                model.moveLeft();
                break;
            case KeyEvent.VK_RIGHT:
                model.moveRight();
                break;
            case KeyEvent.VK_DOWN:
                model.softDrop();
                break;
            case KeyEvent.VK_UP:
                model.rotate();
                break;
            case KeyEvent.VK_SPACE:
                model.hardDrop();
                break;
            case KeyEvent.VK_P:
                model.togglePause();
                if (model.getState() == GameState.PAUSED) {
                    gameTimer.stop();
                } else if (model.getState() == GameState.PLAYING) {
                    gameTimer.start();
                }
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

    // ── GameListener (Observer Pattern) callbacks ────────────────
    @Override
    public void onScoreChanged(int score, int lines, int level) {
        infoPanel.repaint();
    }

    @Override
    public void onGameOver(int finalScore) {
        gameTimer.stop();
        boardPanel.repaint();
        infoPanel.repaint();
    }

    @Override
    public void onPieceChanged() {
        boardPanel.repaint();
        infoPanel.repaint();
    }

    @Override
    public void onBoardChanged() {
        boardPanel.repaint();
    }

    @Override
    public void onLevelUp(int newLevel) {
        gameTimer.setDelay(model.getDropInterval());
        infoPanel.repaint();
    }
}
