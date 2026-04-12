package com.minesweeper.core;

public class AudioManager {
    private static AudioManager instance = new AudioManager();
    public static AudioManager getInstance() { return instance; }
    public void toggleBGM() {}
    public void toggleSFX() {}
    public boolean isBgmEnabled() { return false; }
    public boolean isSfxEnabled() { return false; }
    public void playClick() {}
    public void playFlag() {}
    public void playExplode() {}
    public void playWin() {}
    public void playCombo(int count) {}
}