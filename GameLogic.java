package com.minesweeper.core;

import com.minesweeper.entity.Cell;
import java.util.*;

public class GameLogic {
    private Cell[][] board;
    private int rows, cols, totalMines;
    private int cellsRevealed;
    private boolean gameOver, gameWin;
    private Random rand = new Random();

    public GameLogic(int rows, int cols, int mines) {
        this.rows = rows;
        this.cols = cols;
        this.totalMines = mines;
        board = new Cell[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                board[r][c] = new Cell();
        cellsRevealed = 0;
        gameOver = gameWin = false;
    }

    // 安全获取格子，防止越界
    public Cell getCell(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) return null;
        return board[r][c];
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getTotalMines() { return totalMines; }
    public boolean isGameOver() { return gameOver; }
    public boolean isGameWin() { return gameWin; }

    public void resetBoardState() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                board[r][c].reset();
        cellsRevealed = 0;
        gameOver = gameWin = false;
    }

    public void generateMines() {
        generateMines(-1, -1);
    }

    public void generateMines(int excludeR, int excludeC) {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                board[r][c].setMine(false);
        int placed = 0;
        while (placed < totalMines) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            if (!board[r][c].isMine() && !(r == excludeR && c == excludeC)) {
                board[r][c].setMine(true);
                placed++;
            }
        }
        calcNeighbors();
    }

    private void calcNeighbors() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board[r][c].isMine()) continue;
                int cnt = 0;
                for (int dr = -1; dr <= 1; dr++)
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        int nr = r + dr, nc = c + dc;
                        if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && board[nr][nc].isMine())
                            cnt++;
                    }
                board[r][c].setNeighborMines(cnt);
            }
        }
    }

    public int revealCell(int startR, int startC) {
        if (startR < 0 || startR >= rows || startC < 0 || startC >= cols) return 0;
        Cell cell = board[startR][startC];
        if (cell.isRevealed() || cell.isFlagged()) return 0;
        if (cell.isMine()) {
            cell.setRevealed(true);
            cell.setExploded(true);
            gameOver = true;
            return -1;
        }

        Queue<int[]> q = new LinkedList<>();
        boolean[][] vis = new boolean[rows][cols];
        q.add(new int[]{startR, startC});
        vis[startR][startC] = true;
        int count = 0;

        while (!q.isEmpty()) {
            int[] p = q.poll();
            int r = p[0], c = p[1];
            Cell cur = board[r][c];
            if (cur.isRevealed() || cur.isFlagged()) continue;
            cur.setRevealed(true);
            count++;
            cellsRevealed++;
            if (cur.getNeighborMines() > 0) continue;
            for (int dr = -1; dr <= 1; dr++)
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    int nr = r + dr, nc = c + dc;
                    if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                        Cell nb = board[nr][nc];
                        if (!nb.isRevealed() && !nb.isFlagged() && !nb.isMine() && !vis[nr][nc]) {
                            vis[nr][nc] = true;
                            q.add(new int[]{nr, nc});
                        }
                    }
                }
        }

        if (cellsRevealed == rows * cols - totalMines) {
            gameWin = true;
            gameOver = true;
        }
        return count;
    }

    public void revealAllMines() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (board[r][c].isMine())
                    board[r][c].setRevealed(true);
    }

    public void flagAllMines() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                Cell cell = board[r][c];
                if (cell.isMine() && !cell.isRevealed() && !cell.isFlagged())
                    cell.setFlagged(true);
            }
    }

    public int getFlaggedCount() {
        int cnt = 0;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (board[r][c].isFlagged()) cnt++;
        return cnt;
    }
}