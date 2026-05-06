import controller.GameController;
import model.GameModel;
import view.BoardPanel;
import view.InfoPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Tetris Game — Main Entry Point
 * 
 * Architecture: MVC (Model-View-Controller)
 * Design Patterns Used:
 *   1. Observer Pattern  — GameListener interface notifies views of changes
 *   2. Factory Pattern   — TetrominoFactory creates pieces using 7-bag randomizer
 *   3. Strategy Pattern  — ScoreStrategy allows swappable scoring algorithms
 *   4. MVC Pattern       — Separation of game logic, rendering, and input handling
 * 
 * @author Tetris Project - OOP Course
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

            // ── Frame Setup ──
            JFrame frame = new JFrame("Tetris — OOP Project");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setLayout(new BorderLayout());

            // Board on the left, info on the right
            frame.add(boardPanel, BorderLayout.CENTER);
            frame.add(infoPanel, BorderLayout.EAST);

            // Keyboard input
            frame.addKeyListener(controller);

            frame.pack();
            frame.setLocationRelativeTo(null); // center on screen
            frame.setVisible(true);

            // Request focus for keyboard events
            frame.requestFocusInWindow();
        });
    }
}
