package pattern;

/**
 * Strategy Pattern: Defines how scores are calculated.
 * Different strategies can be swapped in for different game modes.
 */
public interface ScoreStrategy {
    /**
     * Calculate score for clearing lines.
     * @param linesCleared number of lines cleared at once (1-4)
     * @param level current game level
     * @return score to add
     */
    int calculateScore(int linesCleared, int level);

    /**
     * Calculate score for soft drop.
     * @param cellsDropped number of cells dropped
     * @return score to add
     */
    int calculateSoftDropScore(int cellsDropped);

    /**
     * Calculate score for hard drop.
     * @param cellsDropped number of cells dropped
     * @return score to add
     */
    int calculateHardDropScore(int cellsDropped);
}
