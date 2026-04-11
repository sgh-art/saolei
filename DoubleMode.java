package com.minesweeper.mode;

import com.minesweeper.core.AudioManager;
import com.minesweeper.core.GameConfig;
import com.minesweeper.core.GameLogic;
import com.minesweeper.core.GameWindow;
import com.minesweeper.entity.Cell;
import com.minesweeper.entity.Player;

import java.awt.event.KeyEvent;

public class DoubleMode implements GameModeController {
    private GameLogic logic;
    private GameWindow window;
    private AudioManager audio;

    private Player player1, player2;
    private Player currentPlayer;
    private boolean roundActive = true;
    private boolean gameEnded = false;

    private int cursorRow, cursorCol;

    public DoubleMode(GameLogic logic, GameWindow window, AudioManager audio) {
        this.logic = logic;
        this.window = window;
        this.audio = audio;
        initPlayers();
        cursorRow = logic.getRows() / 2;
        cursorCol = logic.getCols() / 2;
        window.setCursor(cursorRow, cursorCol);
        updateUI();
    }

    private void initPlayers() {
        player1 = new Player(1, "红方", "红色");
        player2 = new Player(2, "蓝方", "蓝色");
        player1.setActive(true);
        currentPlayer = player1;
    }

    @Override
    public void handleLeftClick(int row, int col) {
        if (!roundActive || gameEnded) return;
        cursorRow = row;
        cursorCol = col;
        window.setCursor(row, col);
        revealCurrentCell();
    }

    @Override
    public void handleRightClick(int row, int col) {
        if (!roundActive || gameEnded) return;
        Cell cell = logic.getCell(row, col);
        if (cell.isRevealed()) return;
        cell.setFlagged(!cell.isFlagged());
        audio.playFlag();
        window.updateUI();
    }

    @Override
    public void handleKeyPress(KeyEvent e) {
        if (!roundActive || gameEnded) return;
        int key = e.getKeyCode();
        int dr = 0, dc = 0;
        boolean isAction = false;

        if (currentPlayer.getId() == 1) {
            switch (key) {
                case KeyEvent.VK_W: dr = -1; break;
                case KeyEvent.VK_S: dr = 1; break;
                case KeyEvent.VK_A: dc = -1; break;
                case KeyEvent.VK_D: dc = 1; break;
                case KeyEvent.VK_SPACE: isAction = true; break;
            }
        } else {
            switch (key) {
                case KeyEvent.VK_UP: dr = -1; break;
                case KeyEvent.VK_DOWN: dr = 1; break;
                case KeyEvent.VK_LEFT: dc = -1; break;
                case KeyEvent.VK_RIGHT: dc = 1; break;
                case KeyEvent.VK_SPACE: isAction = true; break;
            }
        }

        if (dr != 0 || dc != 0) {
            int nr = cursorRow + dr;
            int nc = cursorCol + dc;
            if (nr >= 0 && nr < logic.getRows() && nc >= 0 && nc < logic.getCols()) {
                cursorRow = nr;
                cursorCol = nc;
                window.setCursor(cursorRow, cursorCol);
            }
            e.consume();
        } else if (isAction) {
            revealCurrentCell();
            e.consume();
        }
    }

    private void revealCurrentCell() {
        if (!roundActive || gameEnded) return;
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

        int revealed = logic.revealCell(cursorRow, cursorCol);
        if (revealed > 0) {
            int gain = revealed + (logic.getCell(cursorRow, cursorCol).getNeighborMines() > 0 ? 1 : 0);
            currentPlayer.addRoundScore(gain);

            if (revealed >= 5) {
                audio.playCombo(revealed);
                int bonus = 0;
                if (revealed >= 30) bonus = 10;
                else if (revealed >= 20) bonus = 6;
                else if (revealed >= 10) bonus = 3;
                else if (revealed >= 5) bonus = 1;
                if (bonus > 0) {
                    currentPlayer.addRoundScore(bonus);
                    window.setMessage(String.format("✨ %s +%d连击奖励！本轮:%d分",
                            currentPlayer.getColorName(), bonus, currentPlayer.getRoundScore()));
                }
            } else {
                audio.playClick();
            }

            window.setMessage(String.format("✨ %s +%d分！本轮累计:%d分",
                    currentPlayer.getColorName(), gain, currentPlayer.getRoundScore()));
        }

        boolean allSafeRevealed = true;
        outer:
        for (int r = 0; r < logic.getRows(); r++) {
            for (int c = 0; c < logic.getCols(); c++) {
                Cell c2 = logic.getCell(r, c);
                if (!c2.isMine() && !c2.isRevealed()) {
                    allSafeRevealed = false;
                    break outer;
                }
            }
        }
        if (allSafeRevealed) {
            audio.playWin();
            window.setMessage(String.format("🎉 %s 翻完所有安全格！得%d分",
                    currentPlayer.getColorName(), currentPlayer.getRoundScore()));
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
            String winner = player1.getTotalScore() > player2.getTotalScore() ? "红方" :
                    (player2.getTotalScore() > player1.getTotalScore() ? "蓝方" : "平局");
            window.setMessage(String.format("🏆 游戏结束！%s 获胜！ 红:%d  蓝:%d",
                    winner, player1.getTotalScore(), player2.getTotalScore()));
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

        window.setMessage(String.format("🔄 换人！轮到 %s 第%d回合！",
                currentPlayer.getColorName(), currentPlayer.getRoundsPlayed() + 1));
        window.updateUI();
    }

    @Override
    public void updateUIComponents(GameWindow window) {
        window.setPlayerScore(1, player1.getTotalScore());
        window.setPlayerScore(2, player2.getTotalScore());

        String p1Turn = player1.isActive() ?
                String.format("👑 回合 %d/%d | 本轮:%d", player1.getRoundsPlayed()+1, GameConfig.MAX_ROUNDS, player1.getRoundScore()) :
                String.format("⏳ 回合 %d/%d", player1.getRoundsPlayed(), GameConfig.MAX_ROUNDS);
        String p2Turn = player2.isActive() ?
                String.format("👑 回合 %d/%d | 本轮:%d", player2.getRoundsPlayed()+1, GameConfig.MAX_ROUNDS, player2.getRoundScore()) :
                String.format("⏳ 回合 %d/%d", player2.getRoundsPlayed(), GameConfig.MAX_ROUNDS);

        window.setPlayerTurn(1, p1Turn, player1.isActive());
        window.setPlayerTurn(2, p2Turn, player2.isActive());

        String tip = player1.isActive() ? "💡 玩家1: WASD移动，空格翻开" : "💡 玩家2: 方向键移动，空格翻开";
        window.setTip(tip);
    }

    @Override
    public void reset() {
        initPlayers();
        roundActive = true;
        gameEnded = false;
        logic.resetBoardState();
        logic.generateMines();
        cursorRow = logic.getRows() / 2;
        cursorCol = logic.getCols() / 2;
        window.setCursor(cursorRow, cursorCol);
        updateUI();
        window.setMessage("🔄 游戏重置！🔴 红方先！");
    }

    private void updateUI() {
        window.updateUI();
    }
}