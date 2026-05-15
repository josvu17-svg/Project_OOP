import controller.GameController;
import model.GameModel;
import pattern.SoundManager;
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
            frame.addKeyListener(controller);
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
