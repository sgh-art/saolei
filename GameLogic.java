package com.minesweeper.core;

import com.minesweeper.entity.Cell;
import java.util.*;

public class GameLogic {
    private Cell[][] board;
    private int rows;
    private int cols;
    private int totalMines;
    private int cellsRevealed;
    private boolean gameOver;
    private boolean gameWin;
    private Random random = new Random();

    public GameLogic(int rows, int cols, int totalMines) {
        this.rows = rows;
        this.cols = cols;
        this.totalMines = totalMines;
        this.board = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                board[r][c] = new Cell();
            }
        }
        this.cellsRevealed = 0;
        this.gameOver = false;
        this.gameWin = false;
    }

    public Cell getCell(int r, int c) {
        return board[r][c];
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getTotalMines() {
        return totalMines;
    }

    public int getCellsRevealed() {
        return cellsRevealed;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isGameWin() {
        return gameWin;
    }

    // 重置棋盘状态（不清除雷）
    public void resetBoardState() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                board[r][c].reset();
            }
        }
        cellsRevealed = 0;
        gameOver = false;
        gameWin = false;
    }

    // 生成新雷区，可指定排除格（首次点击保护）
    public void generateMines(int excludeR, int excludeC) {
        // 先全部设为非雷
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                board[r][c].setMine(false);
            }
        }

        int placed = 0;
        while (placed < totalMines) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            if (!board[r][c].isMine() && !(r == excludeR && c == excludeC)) {
                board[r][c].setMine(true);
                placed++;
            }
        }
        calculateNeighbors();
    }

    public void generateMines() {
        generateMines(-1, -1);
    }

    // 计算每个格子的邻居雷数
    private void calculateNeighbors() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board[r][c].isMine()) {
                    board[r][c].setNeighborMines(-1);
                    continue;
                }
                int count = 0;
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        int nr = r + dr, nc = c + dc;
                        if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && board[nr][nc].isMine()) {
                            count++;
                        }
                    }
                }
                board[r][c].setNeighborMines(count);
            }
        }
    }

    /**
     * 翻开格子，返回翻开的格子数量。如果踩雷返回 -1。
     * 使用局部 visited 数组，不依赖 Cell 的任何临时字段。
     */
    public int revealCell(int startR, int startC) {
        Cell cell = board[startR][startC];
        // 已翻开或已插旗则忽略
        if (cell.isRevealed() || cell.isFlagged()) {
            return 0;
        }

        // 踩雷
        if (cell.isMine()) {
            cell.setRevealed(true);
            cell.setExploded(true);
            gameOver = true;
            return -1;
        }

        // BFS 翻开空白区域
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] visited = new boolean[rows][cols];
        queue.add(new int[]{startR, startC});
        visited[startR][startC] = true;
        int revealCount = 0;

        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int r = pos[0], c = pos[1];
            Cell cur = board[r][c];
            if (cur.isRevealed() || cur.isFlagged()) continue;

            cur.setRevealed(true);
            revealCount++;
            cellsRevealed++;

            // 如果周围有雷，不再继续扩展
            if (cur.getNeighborMines() > 0) continue;

            // 否则将周围未翻开、非雷、未插旗的格子加入队列
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    int nr = r + dr, nc = c + dc;
                    if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                        Cell neighbor = board[nr][nc];
                        if (!neighbor.isRevealed() && !neighbor.isFlagged() && !neighbor.isMine() && !visited[nr][nc]) {
                            visited[nr][nc] = true;
                            queue.add(new int[]{nr, nc});
                        }
                    }
                }
            }
        }

        // 检查是否胜利
        if (cellsRevealed == rows * cols - totalMines) {
            gameWin = true;
            gameOver = true;
        }

        return revealCount;
    }

    // 揭露所有地雷（游戏结束时）
    public void revealAllMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board[r][c].isMine()) {
                    board[r][c].setRevealed(true);
                }
            }
        }
    }

    // 自动为所有未翻开的地雷插旗（胜利时美化）
    public void flagAllMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board[r][c];
                if (cell.isMine() && !cell.isRevealed() && !cell.isFlagged()) {
                    cell.setFlagged(true);
                }
            }
        }
    }

    // 计算当前已插旗数量
    public int getFlaggedCount() {
        int count = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board[r][c].isFlagged()) count++;
            }
        }
        return count;
    }
}