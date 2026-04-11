package com.minesweeper.entity;

public class Cell {
    private boolean mine;
    private boolean revealed;
    private boolean flagged;
    private boolean exploded;
    private int neighborMines;

    public Cell() {
        reset();
    }

    public void reset() {
        mine = false;
        revealed = false;
        flagged = false;
        exploded = false;
        neighborMines = 0;
    }

    public boolean isMine() { return mine; }
    public void setMine(boolean mine) { this.mine = mine; }
    public boolean isRevealed() { return revealed; }
    public void setRevealed(boolean revealed) { this.revealed = revealed; }
    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }
    public boolean isExploded() { return exploded; }
    public void setExploded(boolean exploded) { this.exploded = exploded; }
    public int getNeighborMines() { return neighborMines; }
    public void setNeighborMines(int neighborMines) { this.neighborMines = neighborMines; }
}