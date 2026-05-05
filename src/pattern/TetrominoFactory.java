package pattern;

import model.Tetromino;
import model.TetrominoType;
import model.Board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Factory Pattern: Creates Tetromino pieces using the "7-bag" randomizer.
 * Each bag contains one of each piece type, shuffled randomly.
 * This ensures fair distribution — no long droughts of a single piece.
 */
public class TetrominoFactory {
    private final Queue<TetrominoType> bag = new LinkedList<>();

    /** Create the next piece, starting at top-center of board */
    public Tetromino createPiece() {
        if (bag.isEmpty()) {
            refillBag();
        }
        TetrominoType type = bag.poll();
        int startX = (Board.COLS - 3) / 2; // center horizontally
        int startY = 0;                     // top of board (hidden area)
        return new Tetromino(type, startX, startY);
    }

    /** Peek at the next piece type without removing it */
    public TetrominoType peekNext() {
        if (bag.isEmpty()) {
            refillBag();
        }
        return bag.peek();
    }

    /** Fill bag with all 7 types, shuffled */
    private void refillBag() {
        List<TetrominoType> types = new ArrayList<>();
        for (TetrominoType t : TetrominoType.values()) {
            types.add(t);
        }
        Collections.shuffle(types);
        bag.addAll(types);
    }
}
