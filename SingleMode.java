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

    @Override
    public void handleLeftClick(int row, int col) {
        Cell cell = logic.getCell(row, col);
        if (cell.isFlagged() || logic.isGameOver()) return;

        if (cell.isMine()) {
            audio.playExplode();
            logic.revealAllMines();
            window.updateUI();   // 立即刷新显示爆炸结果
            return;
        }

        int revealed = logic.revealCell(row, col);
        if (revealed > 0) {
            if (revealed >= 5) {
                audio.playCombo(revealed);
            } else {
                audio.playClick();
            }
        }

        if (logic.isGameWin()) {
            audio.playWin();
            logic.flagAllMines();
        }

        window.updateUI();   // 确保每次翻开后刷新
    }

    @Override
    public void handleRightClick(int row, int col) {
        Cell cell = logic.getCell(row, col);
        if (cell.isRevealed() || logic.isGameOver()) return;
        cell.setFlagged(!cell.isFlagged());
        audio.playFlag();
        window.updateUI();   // 刷新旗子状态
    }

    @Override
    public void handleKeyPress(KeyEvent e) {
        // 单人模式不使用键盘
    }

    @Override
    public void updateUIComponents(GameWindow window) {
        window.clearCursor();
        window.setTip("💡 左键翻开，右键插旗");
    }

    @Override
    public void reset() {
        // 无需额外重置
    }
}
