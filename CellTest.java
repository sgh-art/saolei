package com.minesweeper.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CellTest {

    private Cell cell;

    @BeforeEach
    void setUp() {
        cell = new Cell();
    }

    @Test
    void testInitialState() {
        assertFalse(cell.isMine());
        assertFalse(cell.isRevealed());
        assertFalse(cell.isFlagged());
        assertFalse(cell.isExploded());
        assertEquals(0, cell.getNeighborMines());
    }

    @Test
    void testSettersAndGetters() {
        cell.setMine(true);
        assertTrue(cell.isMine());

        cell.setRevealed(true);
        assertTrue(cell.isRevealed());

        cell.setFlagged(true);
        assertTrue(cell.isFlagged());

        cell.setExploded(true);
        assertTrue(cell.isExploded());

        cell.setNeighborMines(5);
        assertEquals(5, cell.getNeighborMines());
    }

    @Test
    void testReset() {
        cell.setMine(true);
        cell.setRevealed(true);
        cell.setFlagged(true);
        cell.setExploded(true);
        cell.setNeighborMines(3);
        cell.reset();

        assertFalse(cell.isMine());
        assertFalse(cell.isRevealed());
        assertFalse(cell.isFlagged());
        assertFalse(cell.isExploded());
        assertEquals(0, cell.getNeighborMines());
    }
}
