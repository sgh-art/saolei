package com.minesweeper.mode;

import com.minesweeper.core.AudioManager;
import com.minesweeper.core.GameLogic;
import com.minesweeper.core.GameWindow;
import com.minesweeper.entity.Cell;
import com.minesweeper.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.event.KeyEvent;
import static org.mockito.Mockito.*;

class DoubleModeTest {

    private GameLogic logic;
    private GameWindow window;
    private AudioManager audio;
    private DoubleMode doubleMode;

    @BeforeEach
    void setUp() {
        logic = mock(GameLogic.class);
        when(logic.getRows()).thenReturn(16);
        when(logic.getCols()).thenReturn(16);
        window = mock(GameWindow.class);
        audio = mock(AudioManager.class);
        doubleMode = new DoubleMode(logic, window, audio);
    }

    @Test
    void testPlayerSwitchAfterMineHit() {
        // 模拟踩雷3次后回合结束
        Cell mineCell = new Cell();
        mineCell.setMine(true);
        when(logic.getCell(8, 8)).thenReturn(mineCell);

        doubleMode.handleLeftClick(8, 8);
        doubleMode.handleLeftClick(8, 8);
        doubleMode.handleLeftClick(8, 8);

        verify(window, atLeastOnce()).setMessage(contains("回合结束"));
        verify(window, atLeastOnce()).updateUI();
    }

    @Test
    void testKeyboardMovement() {
        KeyEvent rightKey = mock(KeyEvent.class);
        when(rightKey.getKeyCode()).thenReturn(KeyEvent.VK_D);
        doubleMode.handleKeyPress(rightKey);
        verify(window).setCursor(anyInt(), anyInt());
    }

    @Test
    void testRightClickFlag() {
        Cell cell = new Cell();
        when(logic.getCell(5, 5)).thenReturn(cell);
        doubleMode.handleRightClick(5, 5);
        verify(audio).playFlag();
        verify(window).updateUI();
    }
}