import controller.GameController;
import model.GameModel;
import model.GameState;
import pattern.SoundManager;
import view.BoardPanel;
import view.InfoPanel;
import view.MenuPanel;

import javax.swing.*;
import javax.swing.KeyStroke;
import java.awt.*;
<<<<<<< HEAD
=======
import java.awt.event.ActionEvent;
>>>>>>> 0b53345537e5a901027e11b7b838a1445412786b
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
<<<<<<< HEAD
            frame.setContentPane(mainPanel);
=======
            frame.setLayout(new BorderLayout());
            frame.add(boardPanel, BorderLayout.CENTER);
            frame.add(infoPanel, BorderLayout.EAST);
            // 25002500 Key Bindings (WHEN_IN_FOCUSED_WINDOW 2014 works regardless of focus) 25002500
            JRootPane root = frame.getRootPane();
            InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap am = root.getActionMap();
            im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT,  0), "left");
            im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), "right");
            im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP,    0), "rotate");
            im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN,  0), "softDrop");
            im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, 0), "hardDrop");
            im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P,     0), "pause");
            im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "start");
            am.put("left",     new javax.swing.AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { model.moveLeft();           boardPanel.repaint(); } });
            am.put("right",    new javax.swing.AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { model.moveRight();          boardPanel.repaint(); } });
            am.put("rotate",   new javax.swing.AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { model.rotate();             boardPanel.repaint(); } });
            am.put("softDrop", new javax.swing.AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { model.softDrop();           boardPanel.repaint(); } });
            am.put("hardDrop", new javax.swing.AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { model.hardDrop();           boardPanel.repaint(); } });
            am.put("pause",    new javax.swing.AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { controller.handlePauseToggle(); } });
            am.put("start",    new javax.swing.AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { controller.handleStartGame();   } });
>>>>>>> 0b53345537e5a901027e11b7b838a1445412786b
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
