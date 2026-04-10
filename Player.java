package com.minesweeper.entity;

public class Player {
    private int id;
    private String name;
    private String colorName;       // 用于显示的颜色文字
    private int totalScore;
    private int roundScore;
    private int roundsPlayed;
    private int hitCount;           // 当前回合踩雷次数
    private boolean isActive;

    public Player(int id, String name, String colorName) {
        this.id = id;
        this.name = name;
        this.colorName = colorName;
        this.totalScore = 0;
        this.roundScore = 0;
        this.roundsPlayed = 0;
        this.hitCount = 0;
        this.isActive = false;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColorName() {
        return colorName;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public void addTotalScore(int add) {
        this.totalScore += add;
    }

    public int getRoundScore() {
        return roundScore;
    }

    public void setRoundScore(int roundScore) {
        this.roundScore = roundScore;
    }

    public void addRoundScore(int add) {
        this.roundScore += add;
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public void setRoundsPlayed(int roundsPlayed) {
        this.roundsPlayed = roundsPlayed;
    }

    public void incrementRounds() {
        this.roundsPlayed++;
    }

    public int getHitCount() {
        return hitCount;
    }

    public void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }

    public void incrementHitCount() {
        this.hitCount++;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void resetForNewRound() {
        roundScore = 0;
        hitCount = 0;
    }

    public void resetForNewGame() {
        totalScore = 0;
        roundScore = 0;
        roundsPlayed = 0;
        hitCount = 0;
        isActive = false;
    }
}