package com.minesweeper.core;

import com.minesweeper.entity.Cell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import static org.junit.jupiter.api.Assertions.*;

class GameLogicTest {

    private GameLogic logic;

    @BeforeEach
    void setUp() {
        logic = new GameLogic(8, 8, 10);
    }

    // ----------------- 雷区生成 -----------------
    @Test
    void testGenerateMinesCorrectCount() {
        logic.generateMines();
        assertEquals(10, countMines());
    }

    @Test
    void testGenerateMinesWithExclude() {
        int excludeR = 0, excludeC = 0;
        logic.generateMines(excludeR, excludeC);
        assertFalse(logic.getCell(excludeR, excludeC).isMine());
        assertEquals(10, countMines());
    }

    @RepeatedTest(5)
    void testRandomMinePlacement() {
        GameLogic logic1 = new GameLogic(8, 8, 10);
        GameLogic logic2 = new GameLogic(8, 8, 10);
        logic1.generateMines();
        logic2.generateMines();
        // 极小概率相同，但重复5次足够说明随机性
        assertNotEquals(layoutString(logic1), layoutString(logic2));
    }

    // ----------------- 邻居计算 -----------------
    @Test
    void testNeighborCountWithNoMines() {
        GameLogic noMine = new GameLogic(3, 3, 0);
        noMine.generateMines();
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                assertEquals(0, noMine.getCell(r, c).getNeighborMines());
    }

    // ----------------- 翻开安全格 -----------------
    @Test
    void testRevealSingleSafeCell() {
        logic.generateMines(0, 0); // 确保(0,0)不是雷
        int revealed = logic.revealCell(0, 0);
        assertTrue(revealed > 0);
        assertTrue(logic.getCell(0, 0).isRevealed());
    }

    @Test
    void testRevealFlaggedCellDoesNothing() {
        logic.generateMines(0, 0);
        logic.getCell(0, 0).setFlagged(true);
        int revealed = logic.revealCell(0, 0);
        assertEquals(0, revealed);
        assertFalse(logic.getCell(0, 0).isRevealed());
    }

    @Test
    void testRevealBlankAreaExpands() {
        GameLogic noMine = new GameLogic(5, 5, 0);
        noMine.generateMines();
        int revealed = noMine.revealCell(2, 2);
        assertEquals(25, revealed);
        assertTrue(noMine.isGameWin());
    }

    // ----------------- 踩雷 -----------------
    @Test
    void testRevealMineCausesGameOver() {
        logic.generateMines();
        int[] minePos = findFirstMine();
        int result = logic.revealCell(minePos[0], minePos[1]);
        assertEquals(-1, result);
        assertTrue(logic.isGameOver());
        assertTrue(logic.getCell(minePos[0], minePos[1]).isExploded());
    }

    // ----------------- 胜利条件 -----------------
    @Test
    void testWinWhenAllSafeCellsRevealed() {
        GameLogic small = new GameLogic(2, 2, 1);
        small.generateMines(0, 0); // 确保(0,0)不是雷
        // 找到雷并翻开其他安全格
        for (int r = 0; r < 2; r++)
            for (int c = 0; c < 2; c++)
                if (!small.getCell(r, c).isMine())
                    small.revealCell(r, c);
        assertTrue(small.isGameWin());
        assertTrue(small.isGameOver());
    }

    // ----------------- 插旗功能 -----------------
    @Test
    void testFlagCount() {
        logic.generateMines();
        assertEquals(0, logic.getFlaggedCount());
        logic.getCell(0, 0).setFlagged(true);
        assertEquals(1, logic.getFlaggedCount());
    }

    @Test
    void testFlagAllMines() {
        logic.generateMines();
        logic.flagAllMines();
        int flaggedMines = 0;
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                if (logic.getCell(r, c).isMine() && logic.getCell(r, c).isFlagged())
                    flaggedMines++;
        assertEquals(10, flaggedMines);
    }

    @Test
    void testRevealAllMines() {
        logic.generateMines();
        logic.revealAllMines();
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                if (logic.getCell(r, c).isMine())
                    assertTrue(logic.getCell(r, c).isRevealed());
    }

    // ----------------- 辅助方法 -----------------
    private int countMines() {
        int cnt = 0;
        for (int r = 0; r < logic.getRows(); r++)
            for (int c = 0; c < logic.getCols(); c++)
                if (logic.getCell(r, c).isMine()) cnt++;
        return cnt;
    }

    private int[] findFirstMine() {
        for (int r = 0; r < logic.getRows(); r++)
            for (int c = 0; c < logic.getCols(); c++)
                if (logic.getCell(r, c).isMine())
                    return new int[]{r, c};
        throw new IllegalStateException("No mine found");
    }

    private String layoutString(GameLogic logic) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < logic.getRows(); r++) {
            for (int c = 0; c < logic.getCols(); c++)
                sb.append(logic.getCell(r, c).isMine() ? 'X' : '.');
            sb.append('|');
        }
        return sb.toString();
    }
}