package com.minesweeper.mode;

import com.minesweeper.core.AudioManager;
import com.minesweeper.core.GameConfig;
import com.minesweeper.core.GameLogic;
import com.minesweeper.core.GameWindow;
import com.minesweeper.entity.Cell;
import com.minesweeper.entity.Player;

import java.awt.event.KeyEvent;

public class DoubleModeTest implements GameModeController {
    private GameLogic logic;
    private GameWindow window;
    private AudioManager audio;
    private Player player1, player2, currentPlayer;
    private boolean roundActive = true, gameEnded = false;
    private int cursorRow, cursorCol;

    public DoubleModeTest(GameLogic logic, GameWindow window, AudioManager audio) {
        this.logic = logic;
        this.window = window;
        this.audio = audio;
        player1 = new Player(1, "红方", "红色");
        player2 = new Player(2, "蓝方", "蓝色");
        currentPlayer = player1;
        player1.setActive(true);
        cursorRow = logic.getRows() / 2;
        cursorCol = logic.getCols() / 2;
        window.setCursor(cursorRow, cursorCol);
        updateUI();
    }

    @Override
    public void handleLeftClick(int r, int c) {
        if (!roundActive || gameEnded) return;
        cursorRow = r;
        cursorCol = c;
        window.setCursor(r, c);
        reveal();
    }

    @Override
    public void handleRightClick(int r, int c) {
        if (!roundActive || gameEnded) return;
        Cell cell = logic.getCell(r, c);
        if (cell.isRevealed()) return;
        cell.setFlagged(!cell.isFlagged());
        audio.playFlag();
        window.updateUI();
    }

    @Override
    public void handleKeyPress(KeyEvent e) {
        if (!roundActive || gameEnded) return;
        int k = e.getKeyCode();
        int dr = 0, dc = 0;
        boolean act = false;
        if (currentPlayer.getId() == 1) {
            switch (k) {
                case KeyEvent.VK_W: dr = -1; break;
                case KeyEvent.VK_S: dr = 1; break;
                case KeyEvent.VK_A: dc = -1; break;
                case KeyEvent.VK_D: dc = 1; break;
                case KeyEvent.VK_SPACE: act = true; break;
            }
        } else {
            switch (k) {
                case KeyEvent.VK_UP: dr = -1; break;
                case KeyEvent.VK_DOWN: dr = 1; break;
                case KeyEvent.VK_LEFT: dc = -1; break;
                case KeyEvent.VK_RIGHT: dc = 1; break;
                case KeyEvent.VK_SPACE: act = true; break;
            }
        }
        if (dr != 0 || dc != 0) {
            int nr = cursorRow + dr, nc = cursorCol + dc;
            if (nr >= 0 && nr < logic.getRows() && nc >= 0 && nc < logic.getCols()) {
                cursorRow = nr;
                cursorCol = nc;
                window.setCursor(nr, nc);
            }
            e.consume();
        } else if (act) {
            reveal();
            e.consume();
        }
    }

    private void reveal() {
        Cell cell = logic.getCell(cursorRow, cursorCol);
        if (cell.isRevealed() || cell.isFlagged()) return;
        if (cell.isMine()) {
            audio.playExplode();
            cell.setRevealed(true);
            cell.setExploded(true);
            currentPlayer.incrementHitCount();
            currentPlayer.addRoundScore(-5);
            if (currentPlayer.getRoundScore() < 0) currentPlayer.setRoundScore(0);
            if (currentPlayer.getHitCount() >= GameConfig.MAX_HITS_BEFORE_END) {
                window.setMessage(String.format("💥 %s 第%d次踩雷！回合结束，得%d分",
                        currentPlayer.getColorName(), currentPlayer.getHitCount(), currentPlayer.getRoundScore()));
                endRound();
            } else {
                window.setMessage(String.format("⚠️ %s 踩雷！第%d次，扣5分！剩%d次机会。本轮:%d分",
                        currentPlayer.getColorName(), currentPlayer.getHitCount(),
                        GameConfig.MAX_HITS_BEFORE_END - currentPlayer.getHitCount(),
                        currentPlayer.getRoundScore()));
            }
            window.updateUI();
            return;
        }
        int rev = logic.revealCell(cursorRow, cursorCol);
        if (rev > 0) {
            int gain = rev + (logic.getCell(cursorRow, cursorCol).getNeighborMines() > 0 ? 1 : 0);
            currentPlayer.addRoundScore(gain);
            if (rev >= 5) {
                audio.playCombo(rev);
                int bonus = 0;
                if (rev >= 30) bonus = 10;
                else if (rev >= 20) bonus = 6;
                else if (rev >= 10) bonus = 3;
                else if (rev >= 5) bonus = 1;
                if (bonus > 0) currentPlayer.addRoundScore(bonus);
            } else {
                audio.playClick();
            }
            window.setMessage(String.format("✨ %s +%d分！本轮:%d", currentPlayer.getColorName(), gain, currentPlayer.getRoundScore()));
        }
        boolean win = true;
        outer: for (int r = 0; r < logic.getRows(); r++)
            for (int c = 0; c < logic.getCols(); c++)
                if (!logic.getCell(r, c).isMine() && !logic.getCell(r, c).isRevealed()) {
                    win = false;
                    break outer;
                }
        if (win) {
            audio.playWin();
            window.setMessage(String.format("🎉 %s 翻完安全格！得%d分", currentPlayer.getColorName(), currentPlayer.getRoundScore()));
            endRound();
        }
        window.updateUI();
    }

    private void endRound() {
        currentPlayer.addTotalScore(currentPlayer.getRoundScore());
        currentPlayer.incrementRounds();
        roundActive = false;
        if (player1.getRoundsPlayed() >= GameConfig.MAX_ROUNDS && player2.getRoundsPlayed() >= GameConfig.MAX_ROUNDS) {
            gameEnded = true;
            String win = player1.getTotalScore() > player2.getTotalScore() ? "红方" :
                    (player2.getTotalScore() > player1.getTotalScore() ? "蓝方" : "平局");
            window.setMessage(String.format("🏆 游戏结束！%s 获胜！ 红:%d 蓝:%d", win, player1.getTotalScore(), player2.getTotalScore()));
            window.clearCursor();
            updateUI();
            return;
        }
        currentPlayer.setActive(false);
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
        currentPlayer.setActive(true);
        currentPlayer.resetForNewRound();
        roundActive = true;
        logic.resetBoardState();
        logic.generateMines();
        cursorRow = logic.getRows() / 2;
        cursorCol = logic.getCols() / 2;
        window.setCursor(cursorRow, cursorCol);
        window.setMessage(String.format("🔄 换人！轮到 %s 第%d回合！", currentPlayer.getColorName(), currentPlayer.getRoundsPlayed() + 1));
        window.updateUI();
    }

    @Override
    public void updateUIComponents(GameWindow w) {
        w.setPlayerScore(1, player1.getTotalScore());
        w.setPlayerScore(2, player2.getTotalScore());
        String t1 = player1.isActive() ?
                String.format("👑 回合 %d/%d | 本轮:%d", player1.getRoundsPlayed() + 1, GameConfig.MAX_ROUNDS, player1.getRoundScore()) :
                String.format("⏳ 回合 %d/%d", player1.getRoundsPlayed(), GameConfig.MAX_ROUNDS);
        String t2 = player2.isActive() ?
                String.format("👑 回合 %d/%d | 本轮:%d", player2.getRoundsPlayed() + 1, GameConfig.MAX_ROUNDS, player2.getRoundScore()) :
                String.format("⏳ 回合 %d/%d", player2.getRoundsPlayed(), GameConfig.MAX_ROUNDS);
        w.setPlayerTurn(1, t1, player1.isActive());
        w.setPlayerTurn(2, t2, player2.isActive());
        w.setTip(player1.isActive() ? "💡 玩家1: WASD移动，空格翻开" : "💡 玩家2: 方向键移动，空格翻开");
    }

    @Override
    public void reset() {
        player1.resetForNewGame();
        player2.resetForNewGame();
        currentPlayer = player1;
        player1.setActive(true);
        roundActive = true;
        gameEnded = false;
        logic.resetBoardState();
        logic.generateMines();
        cursorRow = logic.getRows() / 2;
        cursorCol = logic.getCols() / 2;
        window.setCursor(cursorRow, cursorCol);
        updateUI();
    }

    private void updateUI() {
        window.updateUI();
    }

    // ========== 以下方法仅供测试使用 ==========
    int getCurrentPlayerId() {
        return currentPlayer.getId();
    }

    boolean isRoundActive() {
        return roundActive;
    }

    boolean isGameEnded() {
        return gameEnded;
    }
}