// ========== 游戏配置 ==========
const DIFFICULTY = {
    easy: { rows: 8, cols: 8, mines: 10 },
    medium: { rows: 16, cols: 16, mines: 40 },
    hard: { rows: 16, cols: 30, mines: 99 }
};

// ========== 游戏状态 ==========
let game = {
    rows: 8, cols: 8, totalMines: 10,
    board: [], cellsRevealed: 0,
    gameOver: false, gameWin: false,
    cellElements: []
};

// ========== 双人回合制模式 ==========
let turnMode = {
    active: false,           // 是否启用双人模式
    currentPlayer: 1,        // 当前玩家: 1或2
    scores: { 1: 0, 2: 0 },  // 总分
    currentRound: {          // 当前回合信息
        player: 1,
        roundNum: 1,
        turnScore: 0,        // 当前回合得分
        isActive: false      // 回合是否进行中
    },
    maxRounds: 3,            // 每人最多3个回合
    playerRounds: { 1: 0, 2: 0 },  // 已完成回合数
    gameEnded: false
};

// ========== DOM元素 ==========
let dom = {};

function initDOM() {
    dom = {
        board: document.getElementById('board'),
        mine: document.getElementById('mineDisplay'),
        reset: document.getElementById('resetButton'),
        msg: document.getElementById('messageArea'),
        easy: document.getElementById('easyBtn'),
        medium: document.getElementById('mediumBtn'),
        hard: document.getElementById('hardBtn'),
        single: document.getElementById('singleModeBtn'),
        double: document.getElementById('doubleModeBtn'),
        players: document.getElementById('playersPanel'),
        tip: document.getElementById('doubleTip'),
        p1Score: document.getElementById('player1Score'),
        p2Score: document.getElementById('player2Score'),
        p1Turn: document.getElementById('player1Turn'),
        p2Turn: document.getElementById('player2Turn'),
        p1Card: document.getElementById('player1Card'),
        p2Card: document.getElementById('player2Card')
    };
}

// ========== 工具函数 ==========
function formatCounter(n) { return n.toString().padStart(3, '0').slice(0, 3); }

function updateMine() {
    let flagged = game.board.flat().filter(c => c.flagged).length;
    dom.mine.innerText = formatCounter(game.totalMines - flagged);
}

function updateTurnUI() {
    dom.p1Score.innerText = `总分: ${turnMode.scores[1]}`;
    dom.p2Score.innerText = `总分: ${turnMode.scores[2]}`;

    if (!turnMode.active || turnMode.gameEnded) {
        dom.p1Turn.innerText = turnMode.gameEnded ? (turnMode.scores[1] > turnMode.scores[2] ? '🏆 胜利！' : '') : '';
        dom.p2Turn.innerText = turnMode.gameEnded ? (turnMode.scores[2] > turnMode.scores[1] ? '🏆 胜利！' : '') : '';
        return;
    }

    // 显示当前回合信息
    let p1Info = `回合 ${turnMode.playerRounds[1]}/${turnMode.maxRounds}`;
    let p2Info = `回合 ${turnMode.playerRounds[2]}/${turnMode.maxRounds}`;

    if (turnMode.currentPlayer === 1) {
        dom.p1Turn.innerText = `👑 ${p1Info} | 本回合得分: ${turnMode.currentRound.turnScore}`;
        dom.p2Turn.innerText = `⏳ ${p2Info}`;
        dom.p1Card.classList.add('active-turn');
        dom.p2Card.classList.remove('active-turn');
    } else {
        dom.p1Turn.innerText = `⏳ ${p1Info}`;
        dom.p2Turn.innerText = `👑 ${p2Info} | 本回合得分: ${turnMode.currentRound.turnScore}`;
        dom.p1Card.classList.remove('active-turn');
        dom.p2Card.classList.add('active-turn');
    }
}

function endTurn() {
    // 当前回合结束，累加得分
    let player = turnMode.currentPlayer;
    turnMode.scores[player] += turnMode.currentRound.turnScore;
    turnMode.playerRounds[player]++;
    turnMode.currentRound.turnScore = 0;

    // 检查游戏是否结束
    if (turnMode.playerRounds[1] >= turnMode.maxRounds && turnMode.playerRounds[2] >= turnMode.maxRounds) {
        turnMode.gameEnded = true;
        let winner = turnMode.scores[1] > turnMode.scores[2] ? 1 : (turnMode.scores[2] > turnMode.scores[1] ? 2 : 0);
        if (winner === 1) {
            dom.msg.innerText = '🏆 🔴 红方玩家获得最终胜利！ 🏆';
        } else if (winner === 2) {
            dom.msg.innerText = '🏆 🔵 蓝方玩家获得最终胜利！ 🏆';
        } else {
            dom.msg.innerText = '🤝 平局！';
        }
        updateTurnUI();
        return;
    }

    // 切换到对方玩家
    turnMode.currentPlayer = turnMode.currentPlayer === 1 ? 2 : 1;
    turnMode.currentRound.player = turnMode.currentPlayer;

    // 显示换人提示
    dom.msg.innerText = `🔄 换人！轮到 ${turnMode.currentPlayer === 1 ? '红方' : '蓝方'} 开始新回合！`;
    setTimeout(() => {
        if (dom.msg.innerText.includes('换人')) dom.msg.innerText = '';
    }, 2000);

    updateTurnUI();
}

function checkEndTurnByMine() {
    // 踩到雷，结束当前回合
    dom.msg.innerText = `💥 ${turnMode.currentPlayer === 1 ? '红方' : '蓝方'} 踩到雷！本回合结束，得分 ${turnMode.currentRound.turnScore} 分`;
    setTimeout(() => {
        if (dom.msg.innerText.includes('踩到雷')) dom.msg.innerText = '';
    }, 2000);
    endTurn();
}

function checkGameWin() {
    let allSafe = true;
    for (let r = 0; r < game.rows; r++) {
        for (let c = 0; c < game.cols; c++) {
            if (!game.board[r][c].mine && !game.board[r][c].revealed) {
                allSafe = false;
                break;
            }
        }
    }
    if (allSafe && !turnMode.gameEnded) {
        // 所有安全格都被翻开，游戏提前结束
        turnMode.gameEnded = true;
        let winner = turnMode.scores[1] > turnMode.scores[2] ? 1 : (turnMode.scores[2] > turnMode.scores[1] ? 2 : 0);
        if (winner === 1) {
            dom.msg.innerText = '🎉 所有格子已翻开！🔴 红方玩家获胜！ 🎉';
        } else if (winner === 2) {
            dom.msg.innerText = '🎉 所有格子已翻开！🔵 蓝方玩家获胜！ 🎉';
        } else {
            dom.msg.innerText = '🎉 所有格子已翻开！🤝 平局！';
        }
        updateTurnUI();
        return true;
    }
    return false;
}

// ========== 更新单个格子 ==========
function updateCell(r, c) {
    let cell = game.board[r][c];
    let el = game.cellElements[r]?.[c];
    if (!el) return;
    el.className = 'cell';
    if (cell.revealed) {
        el.classList.add('revealed');
        if (cell.mine) {
            el.classList.add('mine-icon');
            if (cell.exploded) el.classList.add('exploded');
        } else if (cell.neighborMines > 0) el.innerText = cell.neighborMines;
    } else {
        if (cell.flagged) el.classList.add('flagged');
        el.innerText = '';
    }
}

// ========== 显示所有雷 ==========
function revealAllMines(er, ec) {
    for (let r = 0; r < game.rows; r++) {
        for (let c = 0; c < game.cols; c++) {
            let cell = game.board[r][c];
            if (cell.mine) {
                cell.revealed = true;
                if (r === er && c === ec) cell.exploded = true;
            }
            if (cell.flagged && cell.mine) cell.flagged = false;
            updateCell(r, c);
        }
    }
}

// ========== 绘制棋盘 ==========
function renderBoard() {
    dom.board.innerHTML = '';
    dom.board.style.gridTemplateColumns = `repeat(${game.cols}, 34px)`;
    game.cellElements = [];
    for (let r = 0; r < game.rows; r++) {
        let rowEls = [];
        for (let c = 0; c < game.cols; c++) {
            let cell = game.board[r][c];
            let div = document.createElement('div');
            div.className = 'cell';
            if (cell.revealed) {
                div.classList.add('revealed');
                if (cell.mine) {
                    div.classList.add('mine-icon');
                    if (cell.exploded) div.classList.add('exploded');
                } else if (cell.neighborMines > 0) div.innerText = cell.neighborMines;
            } else if (cell.flagged) div.classList.add('flagged');
            div.dataset.row = r;
            div.dataset.col = c;
            if (!turnMode.active) {
                div.addEventListener('click', onLeftClick);
                div.addEventListener('contextmenu', onRightClick);
            } else {
                div.addEventListener('click', onTurnClick);
                div.addEventListener('contextmenu', onTurnRightClick);
            }
            dom.board.appendChild(div);
            rowEls.push(div);
        }
        game.cellElements.push(rowEls);
    }
}

// ========== 初始化棋盘 ==========
function initBoard() {
    game.board = [];
    game.cellsRevealed = 0;
    game.gameOver = false;
    game.gameWin = false;
    dom.msg.innerText = '';
    for (let r = 0; r < game.rows; r++) {
        let row = [];
        for (let c = 0; c < game.cols; c++) {
            row.push({ mine: false, revealed: false, flagged: false, neighborMines: 0, exploded: false });
        }
        game.board.push(row);
    }
    let placed = 0;
    while (placed < game.totalMines) {
        let r = Math.floor(Math.random() * game.rows);
        let c = Math.floor(Math.random() * game.cols);
        if (!game.board[r][c].mine) {
            game.board[r][c].mine = true;
            placed++;
        }
    }
    for (let r = 0; r < game.rows; r++) {
        for (let c = 0; c < game.cols; c++) {
            if (game.board[r][c].mine) continue;
            let cnt = 0;
            for (let dr = -1; dr <= 1; dr++) {
                for (let dc = -1; dc <= 1; dc++) {
                    if (dr === 0 && dc === 0) continue;
                    let nr = r + dr, nc = c + dc;
                    if (nr >= 0 && nr < game.rows && nc >= 0 && nc < game.cols && game.board[nr][nc].mine) cnt++;
                }
            }
            game.board[r][c].neighborMines = cnt;
        }
    }
    renderBoard();
    updateMine();
}

// ========== 单人模式翻开格子 ==========
function revealCell(r, c) {
    if (game.gameOver || game.gameWin) return;
    let cell = game.board[r][c];
    if (cell.revealed || cell.flagged) return;
    if (cell.mine) {
        cell.revealed = true;
        cell.exploded = true;
        game.gameOver = true;
        revealAllMines(r, c);
        updateCell(r, c);
        dom.msg.innerText = '💥 踩雷了... 重新开始吧';
        return;
    }
    let queue = [{ r, c }];
    while (queue.length) {
        let { r, c } = queue.shift();
        let cur = game.board[r][c];
        if (cur.revealed || cur.flagged) continue;
        cur.revealed = true;
        game.cellsRevealed++;
        if (cur.neighborMines > 0) { updateCell(r, c); continue; }
        updateCell(r, c);
        for (let dr = -1; dr <= 1; dr++) {
            for (let dc = -1; dc <= 1; dc++) {
                if (dr === 0 && dc === 0) continue;
                let nr = r + dr, nc = c + dc;
                if (nr >= 0 && nr < game.rows && nc >= 0 && nc < game.cols) {
                    let nb = game.board[nr][nc];
                    if (!nb.revealed && !nb.flagged && !nb.mine && !nb.floodVisited) {
                        nb.floodVisited = true;
                        queue.push({ r: nr, c: nc });
                    }
                }
            }
        }
    }
    for (let r = 0; r < game.rows; r++) {
        for (let c = 0; c < game.cols; c++) {
            if (game.board[r][c].floodVisited) delete game.board[r][c].floodVisited;
        }
    }
    let total = game.rows * game.cols - game.totalMines;
    if (game.cellsRevealed === total && !game.gameOver) {
        game.gameWin = true;
        game.gameOver = true;
        dom.msg.innerText = '🎉 恭喜！你赢了！ 🎉';
        for (let r = 0; r < game.rows; r++) {
            for (let c = 0; c < game.cols; c++) {
                let cell = game.board[r][c];
                if (cell.mine && !cell.flagged && !cell.revealed) {
                    cell.flagged = true;
                    updateCell(r, c);
                }
            }
        }
        updateMine();
    }
}

// ========== 回合制翻开格子 ==========
function revealCellTurn(r, c) {
    if (turnMode.gameEnded) return;
    let cell = game.board[r][c];
    if (cell.revealed || cell.flagged) return;

    if (cell.mine) {
        // 踩到雷，显示雷，结束回合
        cell.revealed = true;
        cell.exploded = true;
        updateCell(r, c);
        checkEndTurnByMine();
        return;
    }

    // 安全格，计算得分
    let queue = [{ r, c }];
    let revealedCount = 0;

    while (queue.length) {
        let { r, c } = queue.shift();
        let cur = game.board[r][c];
        if (cur.revealed || cur.flagged) continue;
        cur.revealed = true;
        revealedCount++;
        game.cellsRevealed++;
        if (cur.neighborMines > 0) { updateCell(r, c); continue; }
        updateCell(r, c);
        for (let dr = -1; dr <= 1; dr++) {
            for (let dc = -1; dc <= 1; dc++) {
                if (dr === 0 && dc === 0) continue;
                let nr = r + dr, nc = c + dc;
                if (nr >= 0 && nr < game.rows && nc >= 0 && nc < game.cols) {
                    let nb = game.board[nr][nc];
                    if (!nb.revealed && !nb.flagged && !nb.mine && !nb.floodVisited) {
                        nb.floodVisited = true;
                        queue.push({ r: nr, c: nc });
                    }
                }
            }
        }
    }

    for (let i = 0; i < game.rows; i++) {
        for (let j = 0; j < game.cols; j++) {
            if (game.board[i][j].floodVisited) delete game.board[i][j].floodVisited;
        }
    }

    // 计算得分：每个翻开格子得1分，数字格额外+1分
    let gain = revealedCount;
    if (game.board[r][c].neighborMines > 0) gain += 1;
    turnMode.currentRound.turnScore += gain;

    // 显示得分提示
    dom.msg.innerText = `✨ ${turnMode.currentPlayer === 1 ? '红方' : '蓝方'} +${gain}分！ 本回合累计: ${turnMode.currentRound.turnScore}分`;
    setTimeout(() => {
        if (dom.msg.innerText.includes('+')) dom.msg.innerText = '';
    }, 1000);

    updateTurnUI();
    updateMine();

    // 检查是否所有安全格都被翻开
    if (checkGameWin()) return;
}

// ========== 回合制点击处理 ==========
function onTurnClick(e) {
    e.preventDefault();
    if (!turnMode.active || turnMode.gameEnded) return;
    let r = parseInt(e.currentTarget.dataset.row);
    let c = parseInt(e.currentTarget.dataset.col);
    let cell = game.board[r][c];
    if (cell.flagged) return;
    revealCellTurn(r, c);
}

function onTurnRightClick(e) {
    e.preventDefault();
    if (!turnMode.active || turnMode.gameEnded) return;
    let r = parseInt(e.currentTarget.dataset.row);
    let c = parseInt(e.currentTarget.dataset.col);
    let cell = game.board[r][c];
    if (cell.revealed) return;
    cell.flagged = !cell.flagged;
    updateCell(r, c);
    updateMine();
}

// ========== 单人模式事件处理 ==========
function onLeftClick(e) {
    e.preventDefault();
    if (turnMode.active || game.gameOver || game.gameWin) return;
    let r = parseInt(e.currentTarget.dataset.row);
    let c = parseInt(e.currentTarget.dataset.col);
    if (game.board[r][c].flagged) return;
    revealCell(r, c);
    updateMine();
}

function onRightClick(e) {
    e.preventDefault();
    if (turnMode.active || game.gameOver || game.gameWin) return;
    let r = parseInt(e.currentTarget.dataset.row);
    let c = parseInt(e.currentTarget.dataset.col);
    let cell = game.board[r][c];
    if (cell.revealed) return;
    cell.flagged = !cell.flagged;
    updateCell(r, c);
    updateMine();
}

// ========== 模式切换 ==========
function setMode(mode) {
    if (mode === 'single') {
        turnMode.active = false;
        dom.players.style.display = 'none';
        dom.tip.style.display = 'none';
        dom.single.classList.add('active');
        dom.double.classList.remove('active');
        initBoard();
    } else {
        // 重置回合制数据
        turnMode = {
            active: true,
            currentPlayer: 1,
            scores: { 1: 0, 2: 0 },
            currentRound: { player: 1, roundNum: 1, turnScore: 0, isActive: true },
            maxRounds: 3,
            playerRounds: { 1: 0, 2: 0 },
            gameEnded: false
        };
        dom.players.style.display = 'flex';
        dom.tip.style.display = 'block';
        dom.single.classList.remove('active');
        dom.double.classList.add('active');
        initBoard();
        updateTurnUI();
        dom.msg.innerText = '🎮 回合制双人对战开始！🔴 红方先开始，踩到雷换人，每人3回合！';
        setTimeout(() => {
            if (dom.msg.innerText.includes('回合制')) dom.msg.innerText = '';
        }, 3000);
    }
}

function setDifficulty(level) {
    let cfg = DIFFICULTY[level];
    game.rows = cfg.rows;
    game.cols = cfg.cols;
    game.totalMines = cfg.mines;
    if (turnMode.active) {
        // 重置回合制数据
        turnMode = {
            active: true,
            currentPlayer: 1,
            scores: { 1: 0, 2: 0 },
            currentRound: { player: 1, roundNum: 1, turnScore: 0, isActive: true },
            maxRounds: 3,
            playerRounds: { 1: 0, 2: 0 },
            gameEnded: false
        };
        initBoard();
        updateTurnUI();
    } else {
        initBoard();
    }
}

// ========== 重置游戏 ==========
function resetGame() {
    if (turnMode.active) {
        turnMode = {
            active: true,
            currentPlayer: 1,
            scores: { 1: 0, 2: 0 },
            currentRound: { player: 1, roundNum: 1, turnScore: 0, isActive: true },
            maxRounds: 3,
            playerRounds: { 1: 0, 2: 0 },
            gameEnded: false
        };
        initBoard();
        updateTurnUI();
        dom.msg.innerText = '🔄 游戏已重置！🔴 红方先开始！';
        setTimeout(() => {
            if (dom.msg.innerText.includes('重置')) dom.msg.innerText = '';
        }, 2000);
    } else {
        initBoard();
    }
}

// ========== 启动游戏 ==========
function start() {
    initDOM();
    dom.reset.addEventListener('click', resetGame);
    dom.easy.addEventListener('click', () => setDifficulty('easy'));
    dom.medium.addEventListener('click', () => setDifficulty('medium'));
    dom.hard.addEventListener('click', () => setDifficulty('hard'));
    dom.single.addEventListener('click', () => setMode('single'));
    dom.double.addEventListener('click', () => setMode('double'));
    document.addEventListener('contextmenu', (e) => {
        if (e.target.closest('.cell') && !turnMode.active) e.preventDefault();
    });
    setMode('single');
}

start();