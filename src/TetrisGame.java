import controller.GameController;
import model.GameModel;
import model.GameState;
import pattern.SoundManager;
import view.BoardPanel;
import view.InfoPanel;
import view.MenuPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Main entry point — CardLayout switches between Menu and Game.
 */
public class TetrisGame {

    private static JFrame        frame;
    private static CardLayout    cardLayout;
    private static JPanel        mainPanel;
    private static GameModel     model;
    private static GameController controller;
    private static BoardPanel    boardPanel;
    private static InfoPanel     infoPanel;
    private static MenuPanel     menuPanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            model      = new GameModel();
            boardPanel = new BoardPanel(model);
            infoPanel  = new InfoPanel(model);
            controller = new GameController(model, boardPanel, infoPanel);

            menuPanel = new MenuPanel(TetrisGame::startGame);

            JPanel gamePanel = new JPanel(new BorderLayout());
            gamePanel.add(boardPanel, BorderLayout.CENTER);
            gamePanel.add(infoPanel,  BorderLayout.EAST);

            cardLayout = new CardLayout();
            mainPanel  = new JPanel(cardLayout);
            mainPanel.add(menuPanel,  "MENU");
            mainPanel.add(gamePanel,  "GAME");

            frame = new JFrame("Tetris — OOP Edition");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setContentPane(mainPanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            controller.setParentFrame(frame);

            // InfoPanel callbacks
            infoPanel.setParentFrame(frame);
            infoPanel.setOnSettings(() ->
                controller.openSettings(frame, () -> {
                    menuPanel.refreshScores();
                }, () -> showMenu())
            );
            infoPanel.setOnRestart(() -> {
                model.startGame();
                controller.handleStartGame();
                SoundManager.startBgm();
            });

            setupKeyBindings();
            cardLayout.show(mainPanel, "MENU");
            SoundManager.startBgm();
        });
    }

    public static void startGame() {
        cardLayout.show(mainPanel, "GAME");
        model.startGame();
        controller.handleStartGame();
        SwingUtilities.invokeLater(frame::requestFocusInWindow);
    }

    public static void showMenu() {
        controller.stopTimer();
        cardLayout.show(mainPanel, "MENU");
        menuPanel.refreshScores();
    }

    private static void setupKeyBindings() {
        InputMap  im = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = mainPanel.getActionMap();

        bind(im, am, KeyEvent.VK_LEFT,   "left",   () -> { if(inGame()) { model.moveLeft();  SoundManager.playMove(); }});
        bind(im, am, KeyEvent.VK_RIGHT,  "right",  () -> { if(inGame()) { model.moveRight(); SoundManager.playMove(); }});
        bind(im, am, KeyEvent.VK_DOWN,   "down",   () -> { if(inGame()) model.softDrop(); });
        bind(im, am, KeyEvent.VK_UP,     "up",     () -> { if(inGame()) { model.rotate();    SoundManager.playRotate(); }});
        bind(im, am, KeyEvent.VK_SPACE,  "space",  () -> { if(inGame()) { model.hardDrop();  SoundManager.playHardDrop(); }});
        bind(im, am, KeyEvent.VK_P,      "pause",  () -> { if(inGame()) controller.handlePauseToggle(); });
        bind(im, am, KeyEvent.VK_ENTER,  "enter",  () -> {
            if (inGame() && (model.getState() == GameState.READY ||
                             model.getState() == GameState.GAME_OVER)) {
                startGame();
            }
        });
        bind(im, am, KeyEvent.VK_ESCAPE, "escape", () -> { if(inGame()) showMenu(); });
    }

    private static boolean inGame() { return boardPanel.isShowing(); }

    private static void bind(InputMap im, ActionMap am, int key, String name, Runnable action) {
        im.put(KeyStroke.getKeyStroke(key, 0), name);
        am.put(name, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { action.run(); }
        });
    }
}
