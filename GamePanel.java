package com.minesweeper.core;

import com.minesweeper.entity.Cell;
import com.minesweeper.mode.GameModeController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

public class GamePanel extends JPanel {
    private GameLogic logic;
    private GameModeController modeController;
    private BiConsumer<Integer, Integer> onCellLeftClick;
    private BiConsumer<Integer, Integer> onCellRightClick;

    private int cellSize = GameConfig.CELL_SIZE;
    private int padding = GameConfig.BOARD_PADDING;

    private int cursorRow = -1;
    private int cursorCol = -1;

    public GamePanel(GameLogic logic) {
        this.logic = logic;
        setBackground(new Color(0x8D9EAE));
        setPreferredSize(new Dimension(
                logic.getCols() * cellSize + padding * 2,
                logic.getRows() * cellSize + padding * 2
        ));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int col = (p.x - padding) / cellSize;
                int row = (p.y - padding) / cellSize;
                if (row < 0 || row >= logic.getRows() || col < 0 || col >= logic.getCols()) return;

                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (onCellLeftClick != null) {
                        onCellLeftClick.accept(row, col);
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    if (onCellRightClick != null) {
                        onCellRightClick.accept(row, col);
                    }
                }
                repaint(); // 确保点击后立即重绘
            }
        });
        setFocusable(true);
    }

    public GameLogic getLogic() {
        return logic;
    }

    public void setLogic(GameLogic logic) {
        this.logic = logic;
        updateBoardSize();
    }

    public void setModeController(GameModeController controller) {
        this.modeController = controller;
    }

    public void setOnCellLeftClick(BiConsumer<Integer, Integer> handler) {
        this.onCellLeftClick = handler;
    }

    public void setOnCellRightClick(BiConsumer<Integer, Integer> handler) {
        this.onCellRightClick = handler;
    }

    public void setCursorPosition(int row, int col) {
        this.cursorRow = row;
        this.cursorCol = col;
        repaint();
    }

    public void clearCursor() {
        this.cursorRow = -1;
        this.cursorCol = -1;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int rows = logic.getRows();
        int cols = logic.getCols();

        // 绘制棋盘背景（深色凹陷感）
        g2.setColor(new Color(0x7A8B9B));
        g2.fillRoundRect(padding - 2, padding - 2, cols * cellSize + 4, rows * cellSize + 4, 20, 20);

        // 绘制所有格子
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = padding + c * cellSize;
                int y = padding + r * cellSize;
                drawCell(g2, r, c, x, y);
            }
        }

        // 绘制光标（多人模式）
        if (cursorRow >= 0 && cursorRow < rows && cursorCol >= 0 && cursorCol < cols) {
            int x = padding + cursorCol * cellSize;
            int y = padding + cursorRow * cellSize;
            g2.setColor(new Color(255, 215, 0, 180));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(x + 2, y + 2, cellSize - 4, cellSize - 4, 6, 6);
            g2.setColor(new Color(255, 215, 0, 80));
            g2.setStroke(new BasicStroke(6));
            g2.drawRoundRect(x - 2, y - 2, cellSize + 4, cellSize + 4, 10, 10);
        }
    }

    private void drawCell(Graphics2D g2, int r, int c, int x, int y) {
        Cell cell = logic.getCell(r, c);
        boolean revealed = cell.isRevealed();
        boolean flagged = cell.isFlagged();
        boolean mine = cell.isMine();
        boolean exploded = cell.isExploded();

        // 背景色：未翻开（深色），已翻开（亮色）—— 对比度极大提升
        if (!revealed) {
            // 未翻开：金属灰蓝色
            g2.setColor(new Color(0x9AACBD));
            g2.fillRoundRect(x, y, cellSize, cellSize, 6, 6);
            // 立体阴影效果
            g2.setColor(new Color(0x6C7E8E));
            g2.drawLine(x + 2, y + cellSize - 3, x + cellSize - 3, y + cellSize - 3);
            g2.drawLine(x + cellSize - 3, y + 2, x + cellSize - 3, y + cellSize - 3);
            g2.setColor(new Color(0xE6F0FA));
            g2.drawLine(x + 2, y + 2, x + cellSize - 3, y + 2);
            g2.drawLine(x + 2, y + 2, x + 2, y + cellSize - 3);
        } else {
            // 已翻开：明亮的米白色，与未翻开形成鲜明对比
            g2.setColor(new Color(0xF5F8FC));
            g2.fillRoundRect(x, y, cellSize, cellSize, 6, 6);
            // 凹陷内边框
            g2.setColor(new Color(0xB0C0D0));
            g2.drawRoundRect(x, y, cellSize, cellSize, 6, 6);
        }

        // 爆炸特效（覆盖背景）
        if (exploded) {
            g2.setColor(new Color(0xFF6B6B));
            g2.fillRoundRect(x, y, cellSize, cellSize, 6, 6);
        }

        // 绘制旗子（未翻开时）
        if (!revealed && flagged) {
            g2.setColor(new Color(0xD32F2F));
            g2.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));
            g2.drawString("⚑", x + 5, y + 28);
        }

        // 绘制内容（已翻开）
        if (revealed) {
            if (mine) {
                // 地雷：显眼的黑色炸弹
                g2.setColor(Color.BLACK);
                g2.fillOval(x + 6, y + 6, 22, 22);
                g2.setColor(new Color(0x333333));
                g2.fillOval(x + 9, y + 9, 16, 16);
                g2.setColor(Color.RED);
                g2.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
                g2.drawString("💣", x + 5, y + 28);
            } else {
                int neighbor = cell.getNeighborMines();
                if (neighbor > 0) {
                    // 数字颜色鲜艳，并添加阴影提高可读性
                    g2.setFont(new Font("Arial", Font.BOLD, 20));
                    Color textColor;
                    switch (neighbor) {
                        case 1: textColor = new Color(0x1E88E5); break;
                        case 2: textColor = new Color(0x43A047); break;
                        case 3: textColor = new Color(0xE53935); break;
                        case 4: textColor = new Color(0x8E24AA); break;
                        case 5: textColor = new Color(0xD81B60); break;
                        case 6: textColor = new Color(0x00ACC1); break;
                        case 7: textColor = Color.BLACK; break;
                        default: textColor = new Color(0x5D4037);
                    }
                    // 文字阴影
                    g2.setColor(new Color(0, 0, 0, 40));
                    String text = String.valueOf(neighbor);
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = x + (cellSize - fm.stringWidth(text)) / 2;
                    int textY = y + (cellSize + fm.getAscent()) / 2 - 2;
                    g2.drawString(text, textX + 1, textY + 1);
                    // 文字本体
                    g2.setColor(textColor);
                    g2.drawString(text, textX, textY);
                }
            }
        }
    }

    public void refresh() {
        repaint();
    }

    public void updateBoardSize() {
        setPreferredSize(new Dimension(
                logic.getCols() * cellSize + padding * 2,
                logic.getRows() * cellSize + padding * 2
        ));
        revalidate();
        repaint();
    }
}