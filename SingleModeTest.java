package com.minesweeper.mode;

import com.minesweeper.core.AudioManager;
import com.minesweeper.core.GameLogic;
import com.minesweeper.core.GameWindow;
import com.minesweeper.entity.Cell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SingleModeTest {

    private GameLogic logic;
    private GameWindow window;
    private AudioManager audio;
    private SingleMode singleMode;

    @BeforeEach
    void setUp() {
        logic = new GameLogic(8, 8, 10);
        logic.generateMines(0, 0);
        window = new GameWindow();
        audio = AudioManager.getInstance();
        singleMode = new SingleMode(logic, window, audio);
    }

    @Test
    void testLeftClickSafeCell() {
        singleMode.handleLeftClick(0, 0);
        assertTrue(logic.getCell(0, 0).isRevealed());
        assertFalse(logic.isGameOver());
    }

    @Test
    void testLeftClickMine() {
        // 找到一颗未被标记、未翻开的地雷
        int mineR = -1, mineC = -1;
        outer: for (int r = 0; r < logic.getRows(); r++) {
            for (int c = 0; c < logic.getCols(); c++) {
                Cell cell = logic.getCell(r, c);
                if (cell.isMine() && !cell.isFlagged() && !cell.isRevealed()) {
                    mineR = r;
                    mineC = c;
                    break outer;
                }
            }
        }
        assertNotEquals(-1, mineR, "棋盘上应该有地雷");

        // 点击地雷
        singleMode.handleLeftClick(mineR, mineC);

        // 验证游戏结束
        assertTrue(logic.isGameOver(), "踩雷后游戏应结束");
        assertTrue(logic.getCell(mineR, mineC).isExploded(), "地雷应标记为爆炸");
    }

    @Test
    void testRightClickToggleFlag() {
        Cell cell = logic.getCell(0, 0);
        assertFalse(cell.isFlagged());
        singleMode.handleRightClick(0, 0);
        assertTrue(cell.isFlagged());
        singleMode.handleRightClick(0, 0);
        assertFalse(cell.isFlagged());
    }

    @Test
    void testGameWinCondition() {
        GameLogic smallLogic = new GameLogic(2, 2, 1);
        smallLogic.generateMines(0, 0);
        SingleMode smallMode = new SingleMode(smallLogic, window, audio);
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 2; c++) {
                if (!smallLogic.getCell(r, c).isMine()) {
                    smallMode.handleLeftClick(r, c);
                }
            }
        }
        assertTrue(smallLogic.isGameWin());
    }
}