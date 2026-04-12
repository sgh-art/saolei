package com.minesweeper.mode;

import com.minesweeper.core.AudioManager;
import com.minesweeper.core.GameLogic;
import com.minesweeper.core.GameWindow;
import com.minesweeper.entity.Cell;
import java.awt.event.KeyEvent;

public class SingleMode implements GameModeController {
    private GameLogic logic;
    private GameWindow window;
    private AudioManager audio;

    public SingleMode(GameLogic logic, GameWindow window, AudioManager audio) {
        this.logic = logic;
        this.window = window;
        this.audio = audio;
    }

    public void handleLeftClick(int row, int col) {
        Cell cell = logic.getCell(row, col);
        if (cell == null) return;
        if (cell.isFlagged() || logic.isGameOver()) return;
        if (cell.isMine()) {
            audio.playExplode();
            logic.revealAllMines();
        } else {
            int cnt = logic.revealCell(row, col);
            if (cnt > 0) audio.playClick();
            if (logic.isGameWin()) {
                audio.playWin();
                logic.flagAllMines();
            }
        }
        window.updateUI();
    }

    public void handleRightClick(int row, int col) {
        Cell cell = logic.getCell(row, col);
        if (cell == null) return;
        if (!cell.isRevealed() && !logic.isGameOver()) {
            cell.setFlagged(!cell.isFlagged());
            audio.playFlag();
            window.updateUI();
        }
    }

    public void handleKeyPress(KeyEvent e) {}
    public void updateUIComponents(GameWindow w) {
        w.clearCursor();
        w.setTip("💡 左键翻开，右键插旗");
    }
    public void reset() {}
}