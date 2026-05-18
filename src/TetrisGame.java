import controller.GameController;
import model.GameModel;
import pattern.SoundManager;
import view.BoardPanel;
import view.InfoPanel;

import javax.swing.*;
import javax.swing.KeyStroke;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Tetris Game — Main Entry Point
 *
 * Architecture: MVC (Model-View-Controller)
 * Design Patterns Used:
 *   1. Observer Pattern  — GameListener interface notifies views of changes
 *   2. Factory Pattern   — TetrominoFactory creates pieces using 7-bag randomizer
 *   3. Strategy Pattern  — ScoreStrategy allows swappable scoring algorithms
 */
public class TetrisGame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // ── Model ──
            GameModel model = new GameModel();

            // ── Views ──
            BoardPanel boardPanel = new BoardPanel(model);
            InfoPanel infoPanel = new InfoPanel(model);

            // ── Controller ──
            GameController controller = new GameController(model, boardPanel, infoPanel);

            // ── Frame ──
            JFrame frame = new JFrame("Tetris — OOP Project");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
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
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.requestFocusInWindow();

            // ── Pass frame + restart callback to InfoPanel ──
            infoPanel.setParentFrame(frame);
            infoPanel.setOnRestart(() -> {
                model.startGame();
                // Re-focus so keyboard works after dialog closes
                SwingUtilities.invokeLater(frame::requestFocusInWindow);
            });

            // ── Start BGM ──
            SoundManager.startBgm();
        });
    }
}
