package com.minesweeper.core;

public class GameConfig {
    // 难度枚举
    public enum Difficulty {
        EASY(8, 8, 10),
        MEDIUM(16, 16, 40),
        HARD(16, 30, 99);

        public final int rows;
        public final int cols;
        public final int mines;

        Difficulty(int rows, int cols, int mines) {
            this.rows = rows;
            this.cols = cols;
            this.mines = mines;
        }
    }

    // 游戏模式
    public enum GameMode {
        SINGLE,
        DOUBLE,
        TRIPLE
    }

    // 对战回合数
    public static final int MAX_ROUNDS = 3;
    // 每回合最多踩雷次数（前两次不结束回合）
    public static final int MAX_HITS_BEFORE_END = 3;

    // UI 常量
    public static final int CELL_SIZE = 34;
    public static final int BOARD_PADDING = 12;

    // 连击消息
    public static final String[] COMBO_MESSAGES = {
            "🎉 连击！", "✨ 漂亮！", "⭐ 太棒了！", "💥 连锁反应！",
            "🌟 完美！", "🎈 哇哦！", "🏆 高手！", "💪 厉害！"
    };
}