package pattern;

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
}
