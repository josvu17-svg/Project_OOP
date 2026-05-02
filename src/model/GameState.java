package model;

/**
 * Represents the possible states of the Tetris game.
 * Uses State Pattern concept — game behavior changes based on current state.
 */
public enum GameState {
    READY,      // Before game starts
    PLAYING,    // Game in progress
    PAUSED,     // Game paused
    GAME_OVER   // Game ended
}
