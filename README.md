# Project_OOP
# 🎮 Tetris Game — OOP Project

> A fully-featured Tetris game built in Java, applying **MVC architecture** and three classic **Design Patterns**: Observer, Factory, and Strategy.

---

## 👥 Authors

| Field | Member 1 | Member 2 |
|---|---|---|
| **Name** | Đậu Ngọc Anh Vũ | Nguyễn Văn Trung |
| **Student ID** | ITDSIU24059 | ITDSIU24054 |
| **Major** | Data Science | Data Science |
| **University** | Ho Chi Minh International University (HCMIU) | Ho Chi Minh International University (HCMIU) |
| **Course** | Object-Oriented Programming | Object-Oriented Programming |

---

## 🚀 How to Run

### Option 1: Run the JAR file (Recommended)
```bash
java -jar TetrisGame.jar
```

### Option 2: Compile from source
```bash
# Step 1: Navigate to the src folder
cd src

# Step 2: Compile all Java files
javac -d ../out TetrisGame.java model/*.java view/*.java controller/*.java pattern/*.java

# Step 3: Run the compiled output
cd ../out
java TetrisGame
```

> **Requirements:** Java 8 or higher

---

## 🕹️ Controls

| Key | Action |
|---|---|
| `← →` | Move piece left / right |
| `↑` | Rotate piece clockwise |
| `↓` | Soft drop (move down faster) |
| `Space` | Hard drop (instant drop to bottom) |
| `P` | Pause / Resume |
| `Enter` | Start game / Restart after Game Over |

---

## 📜 Game Rules

1. **Pieces (Tetrominoes):** Seven standard shapes — **I, O, T, S, Z, J, L** — fall from the top of the board one at a time.
2. **Objective:** Arrange falling pieces to complete full horizontal lines. Each completed line is cleared and points are awarded.
3. **Line Clear:** When a horizontal row is completely filled with blocks, it disappears and all blocks above fall down.
4. **Multi-line Bonus:** Clearing multiple lines simultaneously gives bonus points. Clearing 4 lines at once is called a **"Tetris"** and gives the highest reward.
5. **Leveling:** Every **10 lines** cleared increases the level by 1. Higher levels make pieces fall faster.
6. **Ghost Piece:** A transparent preview shows where the current piece will land.
7. **Game Over:** The game ends when newly spawned pieces cannot be placed because the stack has reached the top of the board.

---

## 📊 Scoring System (Classic NES Style)

| Action | Points |
|---|---|
| Single (1 line) | 100 × (level + 1) |
| Double (2 lines) | 300 × (level + 1) |
| Triple (3 lines) | 500 × (level + 1) |
| Tetris (4 lines) | 800 × (level + 1) |
| Soft drop | 1 point per cell dropped |
| Hard drop | 2 points per cell dropped |

### Speed Curve

| Level | Drop Interval |
|---|---|
| 0 | 800 ms |
| 1 | 740 ms |
| 5 | 500 ms |
| 10 | 200 ms |
| 11+ | 100 ms (minimum) |

---

## 🏗️ Project Architecture

### MVC Pattern

The project strictly follows the **Model-View-Controller (MVC)** architectural pattern:

```
┌──────────────────┐        ┌──────────────────┐        ┌──────────────────┐
│   CONTROLLER     │──────▶ │     MODEL        │ ──────▶│      VIEW        │
│                  │        │                  │        │                  │
│ GameController   │  calls │  GameModel       │notifies│  BoardPanel      │
│ (KeyAdapter)     │        │  Board           │        │  InfoPanel       │
│                  │        │  Tetromino       │        │                  │
└──────────────────┘        └──────────────────┘        └──────────────────┘
```

- **Model** — Contains all game logic: board state, piece movement, scoring, level management. Zero dependency on the UI.
- **View** — Renders the game visually using Java Swing (`BoardPanel` for the grid, `InfoPanel` for score/next piece).
- **Controller** — Listens to keyboard input and drives the game loop timer. Bridges user actions to model operations.

---

## 🎨 Design Patterns

### 1. Observer Pattern (+5 pts)

**Purpose:** Decouple the model from the view. The model does not need to know anything about Swing components.

| Role | Class |
|---|---|
| Subject | `GameModel` — maintains a list of `GameListener` and notifies them on events |
| Observer Interface | `GameListener` — defines callbacks: `onScoreChanged`, `onGameOver`, `onPieceChanged`, `onBoardChanged`, `onLevelUp` |
| Concrete Observer | `GameController` — receives callbacks and triggers repaints on the view panels |

**How it works:**
```
GameModel ──notifies──▶ GameListener (interface)
                              ▲
                              │ implements
                        GameController ──repaints──▶ BoardPanel / InfoPanel
```

When a piece locks or a line is cleared, `GameModel` calls `notifyBoardChanged()` which fires `onBoardChanged()` on all registered listeners — `GameController` then calls `boardPanel.repaint()`. The model never touches the UI directly.

---

### 2. Factory Pattern (+5 pts)

**Purpose:** Centralize piece creation logic. Easily swap piece generation strategies without modifying game logic.

| Role | Class |
|---|---|
| Factory | `TetrominoFactory` — produces `Tetromino` instances using a 7-bag randomizer |

**7-Bag Randomizer Algorithm:**
The factory maintains a queue. When the queue is empty, it creates one bag containing all 7 piece types (`I, O, T, S, Z, J, L`), shuffles them randomly, then fills the queue. This guarantees that in every 7 pieces, each type appears exactly once — no long droughts of a single piece type.

```java
// Usage
TetrominoFactory factory = new TetrominoFactory();
Tetromino piece = factory.createPiece(); // always fair distribution
```

---

### 3. Strategy Pattern (+5 pts)

**Purpose:** Allow different scoring algorithms to be swapped at runtime without changing `GameModel`.

| Role | Class |
|---|---|
| Strategy Interface | `ScoreStrategy` — defines `calculateScore()`, `calculateSoftDropScore()`, `calculateHardDropScore()` |
| Concrete Strategy | `ClassicScoreStrategy` — implements NES-style scoring |

**How it works:**
```java
// Swap scoring strategy at runtime
gameModel.setScoreStrategy(new ClassicScoreStrategy()); // or any future strategy
```

A future `BlitzScoreStrategy` or `TimeAttackScoreStrategy` can be added by simply implementing `ScoreStrategy` — `GameModel` requires zero modification.

---

## 📐 Class Diagram

```
                         ┌──────────────────────┐
                         │     TetrisGame        │  ← Entry Point (main)
                         └──────────┬───────────┘
                                    │ creates
              ┌─────────────────────┼────────────────────┐
              ▼                     ▼                     ▼
   ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
   │  GameController  │  │   BoardPanel     │  │   InfoPanel      │
   │  (Controller)    │  │   (View)         │  │   (View)         │
   └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘
            │                     │                      │
            │ implements          └──────────────────────┘
            │                               │ reads
            ▼                               ▼
   ┌──────────────────┐         ┌──────────────────────┐
   │  GameListener    │◀────────│     GameModel        │
   │  (interface)     │notifies │  (Subject/Observer)  │
   └──────────────────┘         └────────┬─────────────┘
                                         │ uses / has-a
                    ┌────────────────────┼────────────────────┐
                    ▼                    ▼                     ▼
         ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
         │  ScoreStrategy   │  │     Board        │  │  TetrominoFactory│
         │  (interface)     │  │  (10×20 grid)    │  │  (Factory)       │
         └────────┬─────────┘  └──────────────────┘  └──────────────────┘
                  │ implements
                  ▼
         ┌──────────────────┐
         │ ClassicScore     │
         │ Strategy         │
         └──────────────────┘

         Tetromino ──has-a──▶ TetrominoType (enum: I,O,T,S,Z,J,L)
         GameModel ──has-a──▶ GameState (enum: READY, PLAYING, PAUSED, GAME_OVER)
```

---

## ✨ Extra Features

| Feature | Description | Points |
|---|---|---|
| **Ghost Piece** | Transparent preview showing exactly where the piece will land | +2 pts |
| **Wall Kick** | When rotating near a wall, the piece automatically shifts to fit | +2 pts |
| **7-Bag Randomizer** | Fair piece distribution — every 7 pieces contains one of each type | +2 pts |
| **Hard Drop** | Instantly drops piece to bottom with 2× score bonus | +2 pts |
| **Soft Drop Scoring** | Extra points for manually pushing pieces down | +2 pts |
| **Speed Scaling** | Game speed increases every 10 lines cleared | +2 pts |

---

## 📁 File Structure

```
Project_OOP/
├── TetrisGame.jar              ← Executable JAR (run directly)
├── README.md                   ← This file
└── src/
    ├── TetrisGame.java         ← Main entry point
    │
    ├── model/
    │   ├── Board.java          ← 10×20 grid, collision detection, line clearing
    │   ├── GameModel.java      ← Core game logic (Subject in Observer Pattern)
    │   ├── GameState.java      ← State enum: READY, PLAYING, PAUSED, GAME_OVER
    │   ├── Tetromino.java      ← A single piece instance (position, rotation)
    │   └── TetrominoType.java  ← 7 piece types with all 4 rotation states
    │
    ├── view/
    │   ├── BoardPanel.java     ← Renders the game board, ghost piece, overlays
    │   └── InfoPanel.java      ← Renders score, level, lines, next piece preview
    │
    ├── controller/
    │   └── GameController.java ← Keyboard input + game loop timer (Observer)
    │
    └── pattern/
        ├── GameListener.java           ← Observer interface
        ├── TetrominoFactory.java       ← Factory Pattern (7-bag randomizer)
        ├── ScoreStrategy.java          ← Strategy interface
        └── ClassicScoreStrategy.java   ← Classic NES scoring implementation
```

---

## 🔧 Technical Highlights

- **Language:** Java (Swing GUI)
- **Architecture:** MVC (Model-View-Controller)
- **Patterns:** Observer, Factory, Strategy
- **Piece generation:** 7-bag randomizer for fair distribution
- **Rendering:** Custom `paintComponent` with anti-aliasing, ghost piece, block shading
- **Game loop:** `javax.swing.Timer` driving tick-based gravity
- **Collision detection:** All 4 directions + rotation with wall kick correction
- **Line clearing:** Gravity-based — rows above cleared lines fall down correctly

---

## 📝 Known Bug Fix

**Bug:** When dropping an I-piece (4 cells tall) to fill 4 rows, only 2 rows were cleared immediately. The remaining 2 rows required placing another piece before clearing.

**Root Cause:** The original `clearLines()` in `Board.java` collected full-row indices first, then removed them one by one. After each removal, rows shifted down — making the stored indices of the remaining full rows point to the wrong positions.

**Fix:** Rebuilt the entire grid from scratch by collecting all non-full rows (bottom to top), then filling the grid back (bottom to top), padding the top with empty rows. No index-shifting issue possible.

```java
// OLD (buggy): index shifts after each removal
for (int idx = fullRows.size() - 1; idx >= 0; idx--) {
    int row = fullRows.get(idx); // ← this index becomes wrong after first removal
    ...
}

// NEW (fixed): rebuild grid, skip full rows entirely
List<Color[]> remaining = new ArrayList<>();
for (int row = totalRows - 1; row >= 0; row--) {
    if (!isFullRow(row)) remaining.add(grid[row].clone());
}
// Refill grid from bottom using kept rows
```
