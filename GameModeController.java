package com.minesweeper.mode;

import com.minesweeper.core.GameWindow;
import java.awt.event.KeyEvent;

public interface GameModeController {
    void handleLeftClick(int row, int col);
    void handleRightClick(int row, int col);
    void handleKeyPress(KeyEvent e);
    void updateUIComponents(GameWindow window);
    void reset();
}