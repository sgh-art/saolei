package com.minesweeper.core;

import javax.sound.sampled.*;

public class AudioManager {
    private static AudioManager instance = new AudioManager();
    private boolean bgmEnabled = true;
    private boolean sfxEnabled = true;
    private Thread bgmThread;

    private AudioManager() {}

    public static AudioManager getInstance() {
        return instance;
    }

    public boolean isBgmEnabled() { return bgmEnabled; }
    public boolean isSfxEnabled() { return sfxEnabled; }

    public void toggleBGM() {
        bgmEnabled = !bgmEnabled;
        if (bgmEnabled) startBGM();
        else stopBGM();
    }

    public void toggleSFX() {
        sfxEnabled = !sfxEnabled;
    }

    // 播放简单蜂鸣音
    private void playTone(int hz, int msecs, double vol) {
        if (!sfxEnabled) return;
        try {
            float sampleRate = 44100;
            byte[] buf = new byte[(int) (sampleRate * msecs / 1000)];
            for (int i = 0; i < buf.length; i++) {
                double angle = i / (sampleRate / hz) * 2.0 * Math.PI;
                buf[i] = (byte) (Math.sin(angle) * 127 * vol);
            }
            AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
            Clip clip = AudioSystem.getClip();
            clip.open(af, buf, 0, buf.length);
            clip.start();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) clip.close();
            });
        } catch (Exception ignored) {}
    }

    public void playClick() {
        playTone(880, 80, 0.5);
    }

    public void playFlag() {
        playTone(600, 50, 0.4);
    }

    public void playExplode() {
        playTone(400, 100, 0.8);
        sleep(50);
        playTone(200, 150, 0.7);
        sleep(120);
        playTone(100, 200, 0.6);
    }

    public void playWin() {
        playTone(523, 150, 0.6);
        sleep(160);
        playTone(659, 150, 0.6);
        sleep(160);
        playTone(784, 200, 0.6);
        sleep(200);
        playTone(1046, 300, 0.7);
    }

    public void playCombo(int count) {
        int base = 440 + Math.min(count * 10, 300);
        playTone(base, 150, 0.5);
        sleep(150);
        playTone(base + 100, 150, 0.5);
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    // 背景音乐：简单循环旋律
    public void startBGM() {
        if (!bgmEnabled) return;
        if (bgmThread != null && bgmThread.isAlive()) return;
        bgmThread = new Thread(() -> {
            int[] notes = {262, 294, 330, 349, 392, 440, 494, 523};
            while (bgmEnabled) {
                for (int note : notes) {
                    if (!bgmEnabled) break;
                    playTone(note, 400, 0.15);
                    sleep(500);
                }
            }
        });
        bgmThread.setDaemon(true);
        bgmThread.start();
    }

    public void stopBGM() {
        // 通过 bgmEnabled 标志让线程自然结束
    }
}