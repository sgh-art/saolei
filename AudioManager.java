package com.minesweeper.core;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class AudioManager {
    private static AudioManager instance;
    private boolean bgmEnabled = true;
    private boolean sfxEnabled = true;
    private Clip bgmClip;

    private AudioManager() {}

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public boolean isBgmEnabled() { return bgmEnabled; }
    public boolean isSfxEnabled() { return sfxEnabled; }

    public void toggleBGM() {
        bgmEnabled = !bgmEnabled;
        if (bgmEnabled) {
            startBGM();
        } else {
            stopBGM();
        }
    }

    public void toggleSFX() {
        sfxEnabled = !sfxEnabled;
    }

    // 生成简单方波音效（类似蜂鸣）
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
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        } catch (LineUnavailableException e) {
            // 忽略
        }
    }

    public void playClick() {
        playTone(880, 80, 0.5);
    }

    public void playFlag() {
        playTone(600, 50, 0.4);
    }

    public void playExplode() {
        playTone(400, 100, 0.8);
        try { Thread.sleep(50); } catch (InterruptedException e) {}
        playTone(200, 150, 0.7);
        try { Thread.sleep(120); } catch (InterruptedException e) {}
        playTone(100, 200, 0.6);
    }

    public void playWin() {
        playTone(523, 150, 0.6);
        try { Thread.sleep(160); } catch (InterruptedException e) {}
        playTone(659, 150, 0.6);
        try { Thread.sleep(160); } catch (InterruptedException e) {}
        playTone(784, 200, 0.6);
        try { Thread.sleep(200); } catch (InterruptedException e) {}
        playTone(1046, 300, 0.7);
    }

    public void playCombo(int count) {
        int base = 440 + Math.min(count * 10, 300);
        playTone(base, 150, 0.5);
        try { Thread.sleep(150); } catch (InterruptedException e) {}
        playTone(base + 100, 150, 0.5);
    }

    // 简易背景音乐（循环播放简单旋律）
    public void startBGM() {
        if (!bgmEnabled) return;
        if (bgmClip != null && bgmClip.isRunning()) return;
        // 简单起见，使用循环播放一个短片段
        new Thread(() -> {
            int[] notes = {262, 294, 330, 349, 392, 440, 494, 523};
            while (bgmEnabled) {
                for (int note : notes) {
                    if (!bgmEnabled) break;
                    playTone(note, 400, 0.15);
                    try { Thread.sleep(500); } catch (InterruptedException e) { break; }
                }
            }
        }).start();
    }

    public void stopBGM() {
        // 通过标志位停止，线程会自然结束
    }
}