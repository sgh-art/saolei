package com.minesweeper.core;

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

    private JLabel mineCounter;
    private JLabel messageLabel;
    private JLabel tipLabel;
    private JPanel statusPanel;
    private JPanel playersPanel;
    private JLabel p1Score, p2Score, p1Turn, p2Turn;
    private JPanel p1Card, p2Card;

    private GameConfig.Difficulty currentDifficulty = GameConfig.Difficulty.EASY;
    private GameConfig.GameMode currentMode = GameConfig.GameMode.SINGLE;

    public GameWindow() {
        setTitle("扫雷 · 对战版");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        initLogic();
        initUI();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (modeController != null) modeController.handleKeyPress(e);
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
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(new Color(0xB0C2D0));
        main.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        statusPanel = createStatusPanel();
        main.add(statusPanel);
        main.add(Box.createRigidArea(new Dimension(0,20)));

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.CENTER,15,0));
        modePanel.setOpaque(false);
        JButton singleBtn = new JButton("单人");
        JButton doubleBtn = new JButton("双人");
        styleButton(singleBtn); styleButton(doubleBtn);
        singleBtn.addActionListener(e -> switchMode(GameConfig.GameMode.SINGLE));
        doubleBtn.addActionListener(e -> switchMode(GameConfig.GameMode.DOUBLE));
        modePanel.add(singleBtn); modePanel.add(doubleBtn);
        main.add(modePanel);
        main.add(Box.createRigidArea(new Dimension(0,20)));

        playersPanel = createPlayersPanel();
        main.add(playersPanel);
        main.add(Box.createRigidArea(new Dimension(0,20)));

        gamePanel = new GamePanel(logic);
        gamePanel.setOnLeftClick(this::onLeftClick);
        gamePanel.setOnRightClick(this::onRightClick);
        main.add(gamePanel);

        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        messageLabel.setForeground(new Color(0xFFE66D));
        messageLabel.setBackground(new Color(0x2C3E50));
        messageLabel.setOpaque(true);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(8,20,8,20));
        messageLabel.setAlignmentX(CENTER_ALIGNMENT);
        main.add(Box.createRigidArea(new Dimension(0,15)));
        main.add(messageLabel);

        tipLabel = new JLabel("💡 左键翻开，右键插旗");
        tipLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tipLabel.setForeground(Color.WHITE);
        tipLabel.setBackground(new Color(0xE67E22));
        tipLabel.setOpaque(true);
        tipLabel.setBorder(BorderFactory.createEmptyBorder(6,20,6,20));
        tipLabel.setAlignmentX(CENTER_ALIGNMENT);
        main.add(Box.createRigidArea(new Dimension(0,10)));
        main.add(tipLabel);

        add(main);
        switchMode(GameConfig.GameMode.SINGLE);
    }

    private JPanel createStatusPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(0xC0D0DE));
        p.setBorder(BorderFactory.createEmptyBorder(12,20,12,20));
        mineCounter = new JLabel("000");
        mineCounter.setFont(new Font("Courier New", Font.BOLD, 28));
        mineCounter.setForeground(new Color(0xFF4757));
        mineCounter.setBackground(new Color(0x1E2B38));
        mineCounter.setOpaque(true);
        mineCounter.setBorder(BorderFactory.createEmptyBorder(5,15,5,15));
        p.add(mineCounter, BorderLayout.WEST);

        JButton reset = new JButton("😊");
        reset.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        reset.addActionListener(e -> resetGame());
        p.add(reset, BorderLayout.CENTER);

        JPanel diff = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        diff.setOpaque(false);
        JButton easy = new JButton("初级"), medium = new JButton("中级"), hard = new JButton("高级");
        easy.addActionListener(e -> setDifficulty(GameConfig.Difficulty.EASY));
        medium.addActionListener(e -> setDifficulty(GameConfig.Difficulty.MEDIUM));
        hard.addActionListener(e -> setDifficulty(GameConfig.Difficulty.HARD));
        diff.add(easy); diff.add(medium); diff.add(hard);
        p.add(diff, BorderLayout.EAST);
        return p;
    }

    private JPanel createPlayersPanel() {
        JPanel panel = new JPanel(new GridLayout(1,2,20,0));
        panel.setOpaque(false);
        p1Card = createCard("🔴 红方", "WASD");
        p2Card = createCard("🔵 蓝方", "方向键");
        panel.add(p1Card); panel.add(p2Card);
        p1Score = (JLabel) p1Card.getClientProperty("score");
        p2Score = (JLabel) p2Card.getClientProperty("score");
        p1Turn = (JLabel) p1Card.getClientProperty("turn");
        p2Turn = (JLabel) p2Card.getClientProperty("turn");
        panel.setVisible(false);
        return panel;
    }

    private JPanel createCard(String name, String ctrl) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(0xD0DEE8));
        card.setBorder(BorderFactory.createEmptyBorder(15,25,15,25));
        JLabel n = new JLabel(name); n.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel c = new JLabel(ctrl); c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel s = new JLabel("得分:0"); s.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel t = new JLabel("等待"); t.setFont(new Font("Segoe UI", Font.BOLD, 12));
        card.add(n); card.add(c); card.add(s); card.add(t);
        card.putClientProperty("score", s);
        card.putClientProperty("turn", t);
        return card;
    }

    private void styleButton(JButton b) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBackground(new Color(0xB9CCDD));
        b.setFocusPainted(false);
    }

    private void onLeftClick(int r, int c) {
        if (modeController != null) modeController.handleLeftClick(r, c);
        updateUI();
    }

    private void onRightClick(int r, int c) {
        if (modeController != null) modeController.handleRightClick(r, c);
        updateUI();
    }

    public void updateUI() {
        mineCounter.setText(String.format("%03d", logic.getTotalMines() - logic.getFlaggedCount()));
        if (modeController != null) modeController.updateUIComponents(this);
        if (logic.isGameOver())
            messageLabel.setText(logic.isGameWin() ? "🎉 胜利！" : "💥 失败...");
        else
            messageLabel.setText(" ");
        gamePanel.repaint();
    }

    public void setMessage(String s) { messageLabel.setText(s); }
    public void setTip(String s) { tipLabel.setText(s); }
    public void setPlayerScore(int id, int score) {
        if (id==1) p1Score.setText("得分:"+score); else p2Score.setText("得分:"+score);
    }
    public void setPlayerTurn(int id, String text, boolean active) {
        JLabel l = (id==1)? p1Turn : p2Turn;
        l.setText(text);
        JPanel card = (id==1)? p1Card : p2Card;
        card.setBackground(active ? new Color(0xFFD966) : new Color(0xD0DEE8));
    }
    public void setCursor(int r, int c) { gamePanel.setCursor(r, c); }
    public void clearCursor() { gamePanel.clearCursor(); }

    private void switchMode(GameConfig.GameMode mode) {
        currentMode = mode;
        playersPanel.setVisible(mode == GameConfig.GameMode.DOUBLE);
        tipLabel.setVisible(mode == GameConfig.GameMode.DOUBLE);
        logic = new GameLogic(currentDifficulty.rows, currentDifficulty.cols, currentDifficulty.mines);
        logic.generateMines();
        gamePanel.setLogic(logic);
        modeController = (mode == GameConfig.GameMode.SINGLE) ? new SingleMode(logic, this, audio)
                : new DoubleMode(logic, this, audio);
        gamePanel.revalidate();
        pack();
        setLocationRelativeTo(null);
        updateUI();
    }

    private void setDifficulty(GameConfig.Difficulty d) {
        currentDifficulty = d;
        logic = new GameLogic(d.rows, d.cols, d.mines);
        logic.generateMines();
        gamePanel.setLogic(logic);
        if (modeController != null) modeController.reset();
        statusPanel.setBackground(switch (d) {
            case EASY -> new Color(0x2ECC71);
            case MEDIUM -> new Color(0x3498DB);
            case HARD -> new Color(0xE74C3C);
        });
        gamePanel.revalidate();
        pack();
        setLocationRelativeTo(null);
        updateUI();
    }

    private void resetGame() {
        logic.resetBoardState();
        logic.generateMines();
        if (modeController != null) modeController.reset();
        updateUI();
    }
}