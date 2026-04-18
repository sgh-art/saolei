package com.minesweeper.mode;

import com.minesweeper.core.AudioManager;
import com.minesweeper.core.GameLogic;
import com.minesweeper.core.GameWindow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class SingleModeTest {

    private GameLogic logic;
    private GameWindow window;
    private AudioManager audio;
    private SingleMode singleMode;

    @BeforeEach
    void setUp() {
        logic = mock(GameLogic.class);
        window = mock(GameWindow.class);
        audio = mock(AudioManager.class);
        singleMode = new SingleMode(logic, window, audio);
    }

    @Test
    void testLeftClickSafeCell() {
        when(logic.getCell(0, 0)).thenReturn(new com.minesweeper.entity.Cell());
        when(logic.isGameOver()).thenReturn(false);
        when(logic.revealCell(0, 0)).thenReturn(1);

        singleMode.handleLeftClick(0, 0);

        verify(audio).playClick();
        verify(window).updateUI();
    }

    @Test
    void testLeftClickMine() {
        com.minesweeper.entity.Cell mineCell = new com.minesweeper.entity.Cell();
        mineCell.setMine(true);
        when(logic.getCell(0, 0)).thenReturn(mineCell);
        when(logic.isGameOver()).thenReturn(false);

        singleMode.handleLeftClick(0, 0);

        verify(audio).playExplode();
        verify(logic).revealAllMines();
        verify(window).updateUI();
    }

    @Test
    void testRightClickToggleFlag() {
        com.minesweeper.entity.Cell cell = new com.minesweeper.entity.Cell();
        when(logic.getCell(0, 0)).thenReturn(cell);
        when(logic.isGameOver()).thenReturn(false);

        singleMode.handleRightClick(0, 0);
        verify(audio).playFlag();
        verify(window).updateUI();
    }
}