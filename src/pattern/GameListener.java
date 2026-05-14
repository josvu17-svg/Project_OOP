package pattern;

import java.util.List;

/**
 * Observer Pattern: Interface for objects that want to be notified
 * of game state changes (score updates, game over, level up, etc.)
 */
public interface GameListener {
    void onScoreChanged(int score, int lines, int level);
    void onGameOver(int finalScore);
    void onPieceChanged();
    void onBoardChanged();
    void onLevelUp(int newLevel);

    /** Called when lines are cleared — provides row indices for animation */
    void onLinesCleared(List<Integer> clearedRows, int linesCount);
}
