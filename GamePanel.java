package com.minesweeper.core;

import com.minesweeper.entity.Cell;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

public class GamePanel extends JPanel {
    private GameLogic logic;
    private BiConsumer<Integer, Integer> onLeftClick;
    private BiConsumer<Integer, Integer> onRightClick;
    private int cellSize = GameConfig.CELL_SIZE;
    private int padding = GameConfig.BOARD_PADDING;
    private int cursorRow = -1, cursorCol = -1;

    public GamePanel(GameLogic logic) {
        this.logic = logic;
        setBackground(new Color(0x8D9EAE));
        updateSize();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 每次点击都使用当前最新的 logic
                GameLogic currentLogic = GamePanel.this.logic;
                if (currentLogic == null) return;
                int row = (e.getY() - padding) / cellSize;
                int col = (e.getX() - padding) / cellSize;
                if (row < 0 || row >= currentLogic.getRows() || col < 0 || col >= currentLogic.getCols()) {
                    return; // 严格边界检查
                }
                if (SwingUtilities.isLeftMouseButton(e) && onLeftClick != null) {
                    onLeftClick.accept(row, col);
                } else if (SwingUtilities.isRightMouseButton(e) && onRightClick != null) {
                    onRightClick.accept(row, col);
                }
                repaint();
            }
        });
        setFocusable(true);
    }

    public void setLogic(GameLogic logic) {
        this.logic = logic;
        updateSize();      // 重新计算首选尺寸
        revalidate();      // 通知布局管理器
        repaint();         // 立即重绘
    }

    public void setOnLeftClick(BiConsumer<Integer, Integer> h) { onLeftClick = h; }
    public void setOnRightClick(BiConsumer<Integer, Integer> h) { onRightClick = h; }
    public void setCursor(int r, int c) { cursorRow = r; cursorCol = c; repaint(); }
    public void clearCursor() { cursorRow = cursorCol = -1; repaint(); }

    private void updateSize() {
        if (logic != null) {
            setPreferredSize(new Dimension(
                    logic.getCols() * cellSize + padding * 2,
                    logic.getRows() * cellSize + padding * 2
            ));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (logic == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0x7A8B9B));
        g2.fillRoundRect(padding-2, padding-2, logic.getCols()*cellSize+4, logic.getRows()*cellSize+4, 20, 20);

        for (int r = 0; r < logic.getRows(); r++) {
            for (int c = 0; c < logic.getCols(); c++) {
                int x = padding + c * cellSize;
                int y = padding + r * cellSize;
                drawCell(g2, logic.getCell(r, c), x, y);
            }
        }

        if (cursorRow >= 0 && cursorRow < logic.getRows() && cursorCol >= 0 && cursorCol < logic.getCols()) {
            int x = padding + cursorCol * cellSize;
            int y = padding + cursorRow * cellSize;
            g2.setColor(new Color(255, 215, 0, 150));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(x+2, y+2, cellSize-4, cellSize-4, 6, 6);
        }
    }

    private void drawCell(Graphics2D g2, Cell cell, int x, int y) {
        boolean revealed = cell.isRevealed();
        boolean flagged = cell.isFlagged();
        boolean mine = cell.isMine();

        if (!revealed) {
            g2.setColor(new Color(0x9AACBD));
            g2.fillRoundRect(x, y, cellSize, cellSize, 6, 6);
            g2.setColor(new Color(0xE6F0FA));
            g2.drawLine(x+2, y+2, x+cellSize-3, y+2);
            g2.drawLine(x+2, y+2, x+2, y+cellSize-3);
            g2.setColor(new Color(0x6C7E8E));
            g2.drawLine(x+2, y+cellSize-3, x+cellSize-3, y+cellSize-3);
            g2.drawLine(x+cellSize-3, y+2, x+cellSize-3, y+cellSize-3);
        } else {
            g2.setColor(new Color(0xF0F5FA));
            g2.fillRoundRect(x, y, cellSize, cellSize, 6, 6);
            g2.setColor(new Color(0xB0C0D0));
            g2.drawRoundRect(x, y, cellSize, cellSize, 6, 6);
        }

        if (!revealed && flagged) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));
            g2.drawString("⚑", x+5, y+27);
        }

        if (revealed) {
            if (mine) {
                g2.setColor(Color.BLACK);
                g2.fillOval(x+6, y+6, 22, 22);
                g2.setColor(Color.RED);
                g2.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
                g2.drawString("💣", x+5, y+27);
            } else {
                int cnt = cell.getNeighborMines();
                if (cnt > 0) {
                    g2.setFont(new Font("Arial", Font.BOLD, 20));
                    g2.setColor(switch (cnt) {
                        case 1 -> Color.BLUE;
                        case 2 -> new Color(0x008000);
                        case 3 -> Color.RED;
                        default -> Color.BLACK;
                    });
                    String s = String.valueOf(cnt);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(s, x + (cellSize-fm.stringWidth(s))/2, y + (cellSize+fm.getAscent())/2 - 2);
                }
            }
        }
    }
}