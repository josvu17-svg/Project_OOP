# 🎮 Tetris Game — OOP Edition

> A fully-featured Tetris game built in Java, applying **MVC architecture** and classic **Design Patterns**: Observer, Factory, and Strategy. Includes menu screen, high score ranking, background music, sound effects, and a custom settings dialog.

---

## 👥 Authors

| Field | Member 1 | Member 2 |
|---|---|---|
| **Name** | Đậu Ngọc Anh Vũ | Nguyễn Văn Trung |
| **Student ID** | ITDSIU24059 | ITDSIU24054 |
| **Major** | Data Science | Data Science |
| **University** | Ho Chi Minh International University (HCMIU) | Ho Chi Minh International University (HCMIU) |
| **Course** | Object-Oriented Programming — Spring 2026 | Object-Oriented Programming — Spring 2026 |

---

## 🚀 How to Run

### Option 1: Build from source
```bash
cd src
javac -d ../out TetrisGame.java model/*.java view/*.java controller/*.java pattern/*.java
cd ../out
java TetrisGame
```

### Option 2: Download JAR
Download `TetrisGame.jar` from [Releases](../../releases) and run:
```bash
java -jar TetrisGame.jar
```

> **Requirements:** Java 8 or higher. No external libraries needed.

---

## 🕹️ Controls

| Key | Action |
|---|---|
| `← →` | Move piece left / right |
| `↑` | Rotate piece clockwise |
| `↓` | Soft drop |
| `Space` | Hard drop (instant) |
| `P` | Pause / Resume |
| `Enter` | Start / Restart |
| `ESC` | Back to main menu |

---

## 📜 Game Rules

1. **7 Tetromino shapes** (I, O, T, S, Z, J, L) fall from the top one at a time
2. **Fill a complete row** to clear it and earn points
3. **Clearing 4 rows at once** = TETRIS (highest bonus!)
4. **Level up** every 10 lines — pieces fall faster
5. **Game ends** when pieces stack to the top
6. After game over, **enter your name** to save your score to the ranking

---

## 📊 Scoring System (Classic NES Style)

| Action | Points |
|---|---|
| Single (1 line) | 100 × (level + 1) |
| Double (2 lines) | 300 × (level + 1) |
| Triple (3 lines) | 500 × (level + 1) |
| Tetris (4 lines) | 800 × (level + 1) |
| Soft drop | 1 point per cell |
| Hard drop | 2 points per cell |

---

## 🏗️ Project Architecture

### MVC Pattern

| Layer | Classes | Responsibility |
|---|---|---|
| **Model** | `GameModel`, `Board`, `Tetromino`, `TetrominoType`, `GameState` | All game logic. Zero UI dependency. |
| **View** | `BoardPanel`, `InfoPanel`, `MenuPanel`, `SettingsDialog`, `GameOverDialog` | Renders game using Java Swing |
| **Controller** | `GameController` | Keyboard input via KeyBindings + game loop timer |

---

## 🎨 Design Patterns

### 1. Observer Pattern (+5 pts)
- **Interface**: `GameListener` — callbacks: `onScoreChanged`, `onGameOver`, `onPieceChanged`, `onBoardChanged`, `onLevelUp`, `onLinesCleared`
- **Subject**: `GameModel` — maintains list of listeners, notifies on state changes
- **Observer**: `GameController` — receives callbacks and repaints views
- **Why**: Decouples model from view. Model never touches Swing directly.

### 2. Factory Pattern (+5 pts)
- **Class**: `TetrominoFactory` — creates Tetromino pieces using 7-bag randomizer
- **Algorithm**: Each bag contains all 7 piece types shuffled randomly — guarantees fair distribution
- **Why**: Centralizes piece creation logic, no long droughts of any piece type

### 3. Strategy Pattern (+5 pts)
- **Interface**: `ScoreStrategy` — defines `calculateScore()`, `calculateSoftDropScore()`, `calculateHardDropScore()`
- **Implementation**: `ClassicScoreStrategy` — NES-style scoring
- **Why**: Scoring algorithm is swappable at runtime without changing `GameModel`

---

## ✨ Extra Features

| Feature | Description | Bonus |
|---|---|---|
| Ghost Piece | Transparent preview of landing position | +2 pts |
| Wall Kick | Piece shifts when rotating near walls | +2 pts |
| 7-Bag Randomizer | Fair piece distribution | +2 pts |
| Hard Drop | Instant drop + 2x score bonus | +2 pts |
| Soft Drop Scoring | 1 point per cell dropped | +2 pts |
| Speed Scaling | Faster every 10 lines | +2 pts |
| Line Clear Explosion | Particle animation scales with lines cleared (1-4) | +2 pts |
| Sound Effects | Synthesized SFX for every action | +2 pts |
| Background Music | Bundled BGM with song selector + add custom songs | +2 pts |
| Menu Screen | Main menu with Start, Settings, Ranking | +2 pts |
| High Score Ranking | Top 6 scores saved to file, shown in menu | +2 pts |
| Settings Dialog | Volume sliders for BGM/SFX, restart, back to menu | +2 pts |

**Total Extra Features: 12 × +2pts = +24pts**
**Total Design Patterns: 3 × +5pts = +15pts**
**Total Bonus: +39pts**

---

## 📁 File Structure

```
Project_OOP/
├── README.md
└── src/
    ├── TetrisGame.java          # Main entry point (CardLayout: Menu ↔ Game)
    ├── model/
    │   ├── Board.java           # 10×20 grid, collision, line clearing (bug fixed)
    │   ├── GameModel.java       # Core game logic (Observer Subject)
    │   ├── GameState.java       # READY / PLAYING / PAUSED / GAME_OVER
    │   ├── Tetromino.java       # Single piece: position, rotation
    │   └── TetrominoType.java   # 7 piece types with all 4 rotations
    ├── view/
    │   ├── BoardPanel.java      # Renders board, ghost piece, particle explosions
    │   ├── InfoPanel.java       # Score, level, next piece, gear icon
    │   ├── MenuPanel.java       # Main menu with ranking table
    │   ├── SettingsDialog.java  # BGM/SFX volume, song selector, restart
    │   └── GameOverDialog.java  # Dark-themed game over + name input
    ├── controller/
    │   └── GameController.java  # KeyBindings + game timer + settings pause
    └── pattern/
        ├── GameListener.java          # Observer interface
        ├── TetrominoFactory.java      # Factory Pattern (7-bag randomizer)
        ├── ScoreStrategy.java         # Strategy interface
        ├── ClassicScoreStrategy.java  # NES scoring implementation
        ├── SoundManager.java          # BGM (Clip API) + synthesized SFX
        └── ScoreManager.java          # High score persistence (scores.dat)
```

---

## 🔧 Key Technical Details

- **KeyBindings** (`WHEN_IN_FOCUSED_WINDOW`) — keyboard works regardless of focus
- **Clip API** for BGM — gapless looping, loaded into RAM, no stuttering
- **Synthesized SFX** — all sound effects generated programmatically, no audio files needed
- **WAV bundled in JAR** — background music works without external files
- **CardLayout** — seamless switch between menu and game screens
- **scores.dat** — high scores saved next to JAR, persists between sessions
- **bgm/ folder** — users can add custom WAV songs at runtime

---

## 🐛 Bug Fix

**clearLines() index-shift bug:** When dropping an I-piece to fill 4 rows, only 2 rows cleared immediately. Fixed by rebuilding the grid from scratch — collecting non-full rows then refilling from bottom up. No index-shifting possible.

---

## 🏆 GitHub Repository

[github.com/josvu17-svg/Project_OOP](https://github.com/josvu17-svg/Project_OOP)
