package com.minesweeper.core;

import com.minesweeper.entity.Player;
import com.minesweeper.mode.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GameWindow extends JFrame {
    private GameLogic logic;
    private GamePanel gamePanel;
    private GameModeController modeController;
    private AudioManager audio = AudioManager.getInstance();

    private JLabel mineCounterLabel;
    private JButton resetButton;
    private JButton easyBtn, mediumBtn, hardBtn;
    private JButton singleBtn, doubleBtn;
    private JPanel playersPanel;
    private JLabel p1ScoreLabel, p2ScoreLabel;
    private JLabel p1TurnLabel, p2TurnLabel;
    private JPanel p1Card, p2Card;
    private JLabel messageLabel;
    private JLabel tipLabel;
    private JButton bgmBtn, sfxBtn;

    private JPanel statusPanel; // 保存状态栏引用，用于难度变色

    private GameConfig.GameMode currentMode = GameConfig.GameMode.SINGLE;
    private GameConfig.Difficulty currentDifficulty = GameConfig.Difficulty.EASY;

    public GameWindow() {
        setTitle("扫雷 · 对战版");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        initLogic();
        initUI();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (modeController != null) {
                    modeController.handleKeyPress(e);
                }
            }
        });
        setFocusable(true);
        requestFocusInWindow();
    }

    private void initLogic() {
        logic = new GameLogic(currentDifficulty.rows, currentDifficulty.cols, currentDifficulty.mines);
        logic.generateMines();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(0xB0C2D0));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        statusPanel = createStatusPanel();
        mainPanel.add(statusPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel modePanel = createModePanel();
        mainPanel.add(modePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        playersPanel = createPlayersPanel();
        mainPanel.add(playersPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        gamePanel = new GamePanel(logic);
        gamePanel.setOnCellLeftClick(this::handleLeftClick);
        gamePanel.setOnCellRightClick(this::handleRightClick);
        mainPanel.add(gamePanel);

        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        messageLabel.setForeground(new Color(0xFFE66D));
        messageLabel.setBackground(new Color(0x2C3E50));
        messageLabel.setOpaque(true);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        messageLabel.setAlignmentX(CENTER_ALIGNMENT);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(messageLabel);

        tipLabel = new JLabel("💡 提示");
        tipLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tipLabel.setForeground(Color.WHITE);
        tipLabel.setBackground(new Color(0xE67E22));
        tipLabel.setOpaque(true);
        tipLabel.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));
        tipLabel.setAlignmentX(CENTER_ALIGNMENT);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(tipLabel);

        add(mainPanel);

        JPanel audioPanel = new JPanel();
        audioPanel.setBackground(new Color(0, 0, 0, 180));
        audioPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bgmBtn = new JButton("🎵 背景音乐");
        sfxBtn = new JButton("🔊 音效");
        styleAudioButton(bgmBtn);
        styleAudioButton(sfxBtn);
        bgmBtn.addActionListener(e -> {
            audio.toggleBGM();
            bgmBtn.setText(audio.isBgmEnabled() ? "🎵 背景音乐" : "🔇 背景音乐");
            bgmBtn.setForeground(audio.isBgmEnabled() ? Color.WHITE : Color.LIGHT_GRAY);
        });
        sfxBtn.addActionListener(e -> {
            audio.toggleSFX();
            sfxBtn.setText(audio.isSfxEnabled() ? "🔊 音效" : "🔇 音效");
            sfxBtn.setForeground(audio.isSfxEnabled() ? Color.WHITE : Color.LIGHT_GRAY);
        });
        audioPanel.add(bgmBtn);
        audioPanel.add(sfxBtn);
        add(audioPanel, BorderLayout.SOUTH);

        switchToMode(GameConfig.GameMode.SINGLE);
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(0xC0D0DE));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        panel.setLayout(new BorderLayout());

        mineCounterLabel = new JLabel("000");
        mineCounterLabel.setFont(new Font("Courier New", Font.BOLD, 28));
        mineCounterLabel.setForeground(new Color(0xFF4757));
        mineCounterLabel.setBackground(new Color(0x1E2B38));
        mineCounterLabel.setOpaque(true);
        mineCounterLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        panel.add(mineCounterLabel, BorderLayout.WEST);

        resetButton = new JButton("😊");
        resetButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        resetButton.setBackground(new Color(0xD9E2EC));
        resetButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> resetGame());
        panel.add(resetButton, BorderLayout.CENTER);

        JPanel diffPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        diffPanel.setOpaque(false);
        easyBtn = createDiffButton("初级");
        mediumBtn = createDiffButton("中级");
        hardBtn = createDiffButton("高级");
        easyBtn.addActionListener(e -> setDifficulty(GameConfig.Difficulty.EASY));
        mediumBtn.addActionListener(e -> setDifficulty(GameConfig.Difficulty.MEDIUM));
        hardBtn.addActionListener(e -> setDifficulty(GameConfig.Difficulty.HARD));
        diffPanel.add(easyBtn);
        diffPanel.add(mediumBtn);
        diffPanel.add(hardBtn);
        panel.add(diffPanel, BorderLayout.EAST);

        return panel;
    }

    private JButton createDiffButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(0xB9CCDD));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
        btn.setFocusPainted(false);
        return btn;
    }

    private JPanel createModePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panel.setOpaque(false);
        singleBtn = new JButton("🎮 单人模式");
        doubleBtn = new JButton("👥 双人对战");
        styleModeButton(singleBtn);
        styleModeButton(doubleBtn);
        singleBtn.addActionListener(e -> switchToMode(GameConfig.GameMode.SINGLE));
        doubleBtn.addActionListener(e -> switchToMode(GameConfig.GameMode.DOUBLE));
        panel.add(singleBtn);
        panel.add(doubleBtn);
        return panel;
    }

    private void styleModeButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(new Color(0xB9CCDD));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btn.setFocusPainted(false);
    }

    private JPanel createPlayersPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        p1Card = createPlayerCard("🔴 红方玩家", "WASD移动，空格翻开");
        p2Card = createPlayerCard("🔵 蓝方玩家", "方向键移动，空格翻开");
        p1Card.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, new Color(0xFF4757)));
        p2Card.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, new Color(0x1E90FF)));

        panel.add(p1Card);
        panel.add(p2Card);

        p1ScoreLabel = (JLabel) p1Card.getClientProperty("scoreLabel");
        p2ScoreLabel = (JLabel) p2Card.getClientProperty("scoreLabel");
        p1TurnLabel = (JLabel) p1Card.getClientProperty("turnLabel");
        p2TurnLabel = (JLabel) p2Card.getClientProperty("turnLabel");

        return panel;
    }

    private JPanel createPlayerCard(String name, String controls) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color start = new Color(0xD9E2EC);
                Color end = new Color(0xB0C2D0);
                GradientPaint gp = new GradientPaint(0, 0, start, 0, getHeight(), end);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(nameLabel);

        JLabel ctrlLabel = new JLabel("<html><div style='text-align:center;'>按键: " + controls + "</div></html>");
        ctrlLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ctrlLabel.setForeground(Color.DARK_GRAY);
        ctrlLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(ctrlLabel);

        JLabel scoreLabel = new JLabel("得分: 0");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        scoreLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(scoreLabel);

        JLabel turnLabel = new JLabel("👑 当前回合");
        turnLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        turnLabel.setForeground(new Color(0xE67E22));
        turnLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(turnLabel);

        card.putClientProperty("scoreLabel", scoreLabel);
        card.putClientProperty("turnLabel", turnLabel);

        return card;
    }

    private void styleAudioButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(new Color(0x34495E));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setFocusPainted(false);
    }

    private void handleLeftClick(int row, int col) {
        if (modeController != null) {
            modeController.handleLeftClick(row, col);
        }
        gamePanel.refresh();
        updateUI();
    }

    private void handleRightClick(int row, int col) {
        if (modeController != null) {
            modeController.handleRightClick(row, col);
        }
        gamePanel.refresh();
        updateUI();
    }

    public void updateUI() {
        int flagged = logic.getFlaggedCount();
        int remaining = logic.getTotalMines() - flagged;
        mineCounterLabel.setText(String.format("%03d", Math.max(0, remaining)));

        if (modeController != null) {
            modeController.updateUIComponents(this);
        }

        if (logic.isGameOver()) {
            if (logic.isGameWin()) {
                messageLabel.setText("🎉 恭喜！你赢了！");
            } else {
                messageLabel.setText("💥 游戏结束");
            }
        } else {
            messageLabel.setText(" ");
        }

        gamePanel.refresh();
    }

    public void setMessage(String msg) {
        messageLabel.setText(msg);
    }

    public void setTip(String tip) {
        tipLabel.setText(tip);
    }

    public void setPlayerScore(int playerId, int score) {
        if (playerId == 1) p1ScoreLabel.setText("总分: " + score);
        else if (playerId == 2) p2ScoreLabel.setText("总分: " + score);
    }

    public void setPlayerTurn(int playerId, String turnText, boolean isActive) {
        JLabel turnLabel = (playerId == 1) ? p1TurnLabel : p2TurnLabel;
        turnLabel.setText(turnText);

        if (playerId == 1) {
            if (isActive) {
                p1Card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0xFFA502), 3),
                        BorderFactory.createEmptyBorder(12, 22, 12, 22)));
                p1Card.setBackground(new Color(0xFFD966));
            } else {
                p1Card.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
                p1Card.setBackground(new Color(0xC0D0DE));
            }
        } else if (playerId == 2) {
            if (isActive) {
                p2Card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0xFFA502), 3),
                        BorderFactory.createEmptyBorder(12, 22, 12, 22)));
                p2Card.setBackground(new Color(0xFFD966));
            } else {
                p2Card.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
                p2Card.setBackground(new Color(0xC0D0DE));
            }
        }
        p1Card.repaint();
        p2Card.repaint();
    }

    public void setCursorPosition(int row, int col) {
        gamePanel.setCursorPosition(row, col);
    }

    public void clearCursor() {
        gamePanel.clearCursor();
    }

    private void switchToMode(GameConfig.GameMode mode) {
        currentMode = mode;
        singleBtn.setBackground(mode == GameConfig.GameMode.SINGLE ? new Color(0xFFA502) : new Color(0xB9CCDD));
        doubleBtn.setBackground(mode == GameConfig.GameMode.DOUBLE ? new Color(0xFFA502) : new Color(0xB9CCDD));
        playersPanel.setVisible(mode == GameConfig.GameMode.DOUBLE);
        tipLabel.setVisible(mode == GameConfig.GameMode.DOUBLE);

        logic = new GameLogic(currentDifficulty.rows, currentDifficulty.cols, currentDifficulty.mines);
        logic.generateMines();
        gamePanel.setLogic(logic);
        gamePanel.updateBoardSize();
        gamePanel.revalidate();
        pack();
        setLocationRelativeTo(null);

        if (mode == GameConfig.GameMode.SINGLE) {
            modeController = new SingleMode(logic, this, audio);
        } else {
            modeController = new DoubleMode(logic, this, audio);
        }
        gamePanel.setModeController(modeController);
        gamePanel.clearCursor();
        updateUI();
        requestFocusInWindow();
    }

    private void setDifficulty(GameConfig.Difficulty diff) {
        currentDifficulty = diff;
        logic = new GameLogic(diff.rows, diff.cols, diff.mines);
        logic.generateMines();
        gamePanel.setLogic(logic);
        gamePanel.updateBoardSize();
        gamePanel.revalidate();
        pack();
        setLocationRelativeTo(null);

        // 状态栏变色
        Color diffColor;
        switch (diff) {
            case EASY: diffColor = new Color(0x2ECC71); break;
            case MEDIUM: diffColor = new Color(0x3498DB); break;
            default: diffColor = new Color(0xE74C3C);
        }
        statusPanel.setBackground(diffColor);

        // ★★★ 修复：重新创建模式控制器，确保使用新的 logic ★★★
        if (currentMode == GameConfig.GameMode.SINGLE) {
            modeController = new SingleMode(logic, this, audio);
        } else {
            modeController = new DoubleMode(logic, this, audio);
        }
        gamePanel.setModeController(modeController);
        gamePanel.clearCursor();

        updateUI();
        requestFocusInWindow();
    }

    private void resetGame() {
        logic.resetBoardState();
        logic.generateMines();
        if (modeController != null) {
            modeController.reset();
        }
        gamePanel.clearCursor();
        updateUI();
    }

    public GameLogic getLogic() {
        return logic;
    }
}