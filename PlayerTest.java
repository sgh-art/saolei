package com.minesweeper.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player(1, "红方", "红色");
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals(1, player.getId());
        assertEquals("红方", player.getName());
        assertEquals("红色", player.getColorName());
        assertEquals(0, player.getTotalScore());
        assertEquals(0, player.getRoundScore());
        assertEquals(0, player.getRoundsPlayed());
        assertEquals(0, player.getHitCount());
        assertFalse(player.isActive());
    }

    @Test
    void testScoreAccumulation() {
        player.addRoundScore(5);
        assertEquals(5, player.getRoundScore());
        player.addRoundScore(-5);
        assertEquals(0, player.getRoundScore());

        player.setRoundScore(10);
        player.addTotalScore(player.getRoundScore());
        assertEquals(10, player.getTotalScore());
    }

    @Test
    void testHitCountAndRounds() {
        player.incrementHitCount();
        player.incrementHitCount();
        assertEquals(2, player.getHitCount());

        player.setHitCount(0);
        player.incrementRounds();
        assertEquals(1, player.getRoundsPlayed());
    }

    @Test
    void testResetForNewRound() {
        player.addRoundScore(20);
        player.incrementHitCount();
        player.resetForNewRound();
        assertEquals(0, player.getRoundScore());
        assertEquals(0, player.getHitCount());
        // 总分不受影响
        assertEquals(0, player.getTotalScore());
    }

    @Test
    void testResetForNewGame() {
        player.addTotalScore(30);
        player.addRoundScore(10);
        player.incrementRounds();
        player.setActive(true);
        player.resetForNewGame();

        assertEquals(0, player.getTotalScore());
        assertEquals(0, player.getRoundScore());
        assertEquals(0, player.getRoundsPlayed());
        assertEquals(0, player.getHitCount());
        assertFalse(player.isActive());
    }

    @Test
    void testActiveState() {
        player.setActive(true);
        assertTrue(player.isActive());
        player.setActive(false);
        assertFalse(player.isActive());
    }
}