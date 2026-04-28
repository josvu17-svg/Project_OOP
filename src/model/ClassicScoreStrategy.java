package pattern;

/**
 * Classic scoring strategy based on the original NES Tetris scoring.
 * Single = 100, Double = 300, Triple = 500, Tetris = 800 (multiplied by level+1)
 */
public class ClassicScoreStrategy implements ScoreStrategy {
    private static final int[] LINE_SCORES = {0, 100, 300, 500, 800};

    @Override
    public int calculateScore(int linesCleared, int level) {
        if (linesCleared < 0 || linesCleared > 4) return 0;
        return LINE_SCORES[linesCleared] * (level + 1);
    }

    @Override
    public int calculateSoftDropScore(int cellsDropped) {
        return cellsDropped; // 1 point per cell
    }

    @Override
    public int calculateHardDropScore(int cellsDropped) {
        return cellsDropped * 2; // 2 points per cell
    }
}
