package com.minesweeper.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameConfigTest {

    @Test
    void testDifficultyEnumValues() {
        assertEquals(8, GameConfig.Difficulty.EASY.rows);
        assertEquals(8, GameConfig.Difficulty.EASY.cols);
        assertEquals(10, GameConfig.Difficulty.EASY.mines);

        assertEquals(16, GameConfig.Difficulty.MEDIUM.rows);
        assertEquals(16, GameConfig.Difficulty.MEDIUM.cols);
        assertEquals(40, GameConfig.Difficulty.MEDIUM.mines);

        assertEquals(16, GameConfig.Difficulty.HARD.rows);
        assertEquals(30, GameConfig.Difficulty.HARD.cols);
        assertEquals(99, GameConfig.Difficulty.HARD.mines);
    }

    @Test
    void testGameModeEnum() {
        assertNotNull(GameConfig.GameMode.SINGLE);
        assertNotNull(GameConfig.GameMode.DOUBLE);
    }

    @Test
    void testConstants() {
        assertEquals(3, GameConfig.MAX_ROUNDS);
        assertEquals(3, GameConfig.MAX_HITS_BEFORE_END);
        assertEquals(34, GameConfig.CELL_SIZE);
        assertEquals(12, GameConfig.BOARD_PADDING);
    }
}