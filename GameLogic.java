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
    private final Random rand = new Random();

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

    public void generateMines() {
        generateMines(-1, -1);
    }

    public void generateMines(int excludeR, int excludeC) {
        // 清除所有雷
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                board[r][c].setMine(false);
            }
        }

        int placed = 0;
        while (placed < totalMines) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            if (!board[r][c].isMine() && !(r == excludeR && c == excludeC)) {
                board[r][c].setMine(true);
                placed++;
            }
        }
        calculateNeighbors();
    }

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
                        int nr = r + dr;
                        int nc = c + dc;
                        if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && board[nr][nc].isMine()) {
                            count++;
                        }
                    }
                }
                board[r][c].setNeighborMines(count);
            }
        }
    }

    public int revealCell(int startR, int startC) {
        // 边界检查
        if (startR < 0 || startR >= rows || startC < 0 || startC >= cols) {
            return 0;
        }
        Cell cell = board[startR][startC];
        if (cell.isRevealed() || cell.isFlagged()) {
            return 0;
        }

        if (cell.isMine()) {
            cell.setRevealed(true);
            cell.setExploded(true);
            gameOver = true;
            return -1;
        }

        // BFS 翻开
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] visited = new boolean[rows][cols];
        queue.add(new int[]{startR, startC});
        visited[startR][startC] = true;
        int revealedCount = 0;

        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int r = pos[0];
            int c = pos[1];
            Cell cur = board[r][c];
            if (cur.isRevealed() || cur.isFlagged()) continue;

            cur.setRevealed(true);
            revealedCount++;
            cellsRevealed++;

            if (cur.getNeighborMines() > 0) continue;

            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    int nr = r + dr;
                    int nc = c + dc;
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

        if (cellsRevealed == rows * cols - totalMines) {
            gameWin = true;
            gameOver = true;
        }

        return revealedCount;
    }

    public void revealAllMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board[r][c].isMine()) {
                    board[r][c].setRevealed(true);
                }
            }
        }
    }

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