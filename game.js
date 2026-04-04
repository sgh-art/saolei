// ========== 游戏配置 ==========
const DIFFICULTY = {
    easy: { rows: 8, cols: 8, mines: 10 },
    medium: { rows: 16, cols: 16, mines: 40 },
    hard: { rows: 16, cols: 30, mines: 99 }
};

// ========== 连击提示消息库 ==========
const comboMessages = [
    "🎉 连击！", "✨ 漂亮！", "⭐ 太棒了！", "💥 连锁反应！",
    "🌟 完美！", "🎈 哇哦！", "🏆 高手！", "💪 厉害！",
    "🔥 连击！", "⭐ 好厉害！", "🎯 精准！", "⚡ 一气呵成！"
];

function showComboMessage(count) {
    if (count < 5) return;

    let message;
    if (count >= 30) {
        message = `🏆 超级连击！一口气翻开了 ${count} 个格子！🏆`;
    } else if (count >= 20) {
        message = `⭐ 太厉害了！一口气翻开了 ${count} 个格子！⭐`;
    } else if (count >= 10) {
        message = `🎉 精彩连击！翻开了 ${count} 个格子！🎉`;
    } else {
        let randomIndex = Math.floor(Math.random() * comboMessages.length);
        message = `${comboMessages[randomIndex]} 翻开了 ${count} 个格子！`;
    }

    let toast = document.createElement('div');
    toast.className = 'combo-toast';
    toast.innerText = message;
    toast.style.position = 'fixed';
    toast.style.top = '30%';
    toast.style.left = '50%';
    toast.style.transform = 'translate(-50%, -50%)';
    toast.style.background = 'linear-gradient(135deg, #ffd966, #ffa502)';
    toast.style.color = '#2c3e50';
    toast.style.padding = '15px 30px';
    toast.style.borderRadius = '50px';
    toast.style.fontSize = '1.5rem';
    toast.style.fontWeight = 'bold';
    toast.style.boxShadow = '0 10px 30px rgba(0,0,0,0.3)';
    toast.style.zIndex = '9999';
    toast.style.animation = 'comboPop 0.5s ease-out';
    toast.style.textAlign = 'center';
    toast.style.whiteSpace = 'nowrap';

    document.body.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.5s';
        setTimeout(() => toast.remove(), 500);
    }, 2000);

    let oldMsg = dom.msg.innerText;
    dom.msg.innerText = message;
    setTimeout(() => {
        if (dom.msg.innerText === message) {
            dom.msg.innerText = oldMsg.includes('+') ? oldMsg : '';
        }
    }, 2000);
}

// ========== 游戏状态 ==========
let game = {
    rows: 8, cols: 8, totalMines: 10,
    board: [], cellsRevealed: 0,
    gameOver: false, gameWin: false,
    cellElements: []
};

// ========== 多人模式 ==========
let multiMode = {
    active: false,
    playerCount: 2,
    currentPlayer: 1,
    scores: { 1: 0, 2: 0, 3: 0 },
    currentRoundScore: 0,
    maxRounds: 3,
    playerRounds: { 1: 0, 2: 0, 3: 0 },
    gameEnded: false,
    roundActive: true,
    cursor: { r: 0, c: 0 },
    mineHitCount: { 1: 0, 2: 0, 3: 0 }  // 每个玩家踩雷次数记录
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
        triple: document.getElementById('tripleModeBtn'),
        players: document.getElementById('playersPanel'),
        tip: document.getElementById('doubleTip'),
        p1Score: document.getElementById('player1Score'),
        p2Score: document.getElementById('player2Score'),
        p3Score: document.getElementById('player3Score'),
        p1Turn: document.getElementById('player1Turn'),
        p2Turn: document.getElementById('player2Turn'),
        p3Turn: document.getElementById('player3Turn'),
        p1Card: document.getElementById('player1Card'),
        p2Card: document.getElementById('player2Card'),
        p3Card: document.getElementById('player3Card')
    };
}

function formatCounter(n) { return n.toString().padStart(3, '0').slice(0, 3); }

function updateMine() {
    if (!game.board.flat) return;
    let flagged = game.board.flat().filter(c => c.flagged).length;
    dom.mine.innerText = formatCounter(game.totalMines - flagged);
}

function updateMultiUI() {
    dom.p1Score.innerText = `总分: ${multiMode.scores[1]}`;
    dom.p2Score.innerText = `总分: ${multiMode.scores[2]}`;
    if (multiMode.playerCount === 3 && dom.p3Score) {
        dom.p3Score.innerText = `总分: ${multiMode.scores[3]}`;
    }

    if (!multiMode.active || multiMode.gameEnded) {
        if (multiMode.gameEnded) {
            let maxScore = Math.max(multiMode.scores[1], multiMode.scores[2], multiMode.scores[3]);
            let winners = [];
            if (multiMode.scores[1] === maxScore) winners.push(1);
            if (multiMode.scores[2] === maxScore) winners.push(2);
            if (multiMode.playerCount === 3 && multiMode.scores[3] === maxScore) winners.push(3);
            let winnerText = winners.map(w => w === 1 ? '🔴' : (w === 2 ? '🔵' : '🟢')).join(', ');
            dom.msg.innerText = winners.length === 1 ? `${winnerText} 获得最终胜利！ 🏆` : `${winnerText} 平局！`;
        }
        return;
    }

    let p1Info = `回合 ${multiMode.playerRounds[1]}/${multiMode.maxRounds}`;
    let p2Info = `回合 ${multiMode.playerRounds[2]}/${multiMode.maxRounds}`;
    let p3Info = multiMode.playerCount === 3 ? `回合 ${multiMode.playerRounds[3]}/${multiMode.maxRounds}` : '';

    dom.p1Card.classList.remove('active-turn');
    dom.p2Card.classList.remove('active-turn');
    if (multiMode.playerCount === 3 && dom.p3Card) dom.p3Card.classList.remove('active-turn');

    if (multiMode.currentPlayer === 1) {
        dom.p1Turn.innerText = `👑 ${p1Info} | 本轮: ${multiMode.currentRoundScore}`;
        dom.p2Turn.innerText = `⏳ ${p2Info}`;
        if (multiMode.playerCount === 3 && dom.p3Turn) dom.p3Turn.innerText = `⏳ ${p3Info}`;
        dom.p1Card.classList.add('active-turn');
        dom.tip.innerText = '💡 玩家1: 使用 WASD 移动光标，空格翻开格子';
    } else if (multiMode.currentPlayer === 2) {
        dom.p1Turn.innerText = `⏳ ${p1Info}`;
        dom.p2Turn.innerText = `👑 ${p2Info} | 本轮: ${multiMode.currentRoundScore}`;
        if (multiMode.playerCount === 3 && dom.p3Turn) dom.p3Turn.innerText = `⏳ ${p3Info}`;
        dom.p2Card.classList.add('active-turn');
        dom.tip.innerText = '💡 玩家2: 使用 方向键 移动光标，空格翻开格子';
    } else if (multiMode.currentPlayer === 3) {
        dom.p1Turn.innerText = `⏳ ${p1Info}`;
        dom.p2Turn.innerText = `⏳ ${p2Info}`;
        dom.p3Turn.innerText = `👑 ${p3Info} | 本轮: ${multiMode.currentRoundScore}`;
        dom.p3Card.classList.add('active-turn');
        dom.tip.innerText = '💡 玩家3: 直接点击格子翻开（鼠标控制）';
    }
}

function showCursor() {
    document.querySelectorAll('.cell').forEach(el => el.classList.remove('cursor'));
    let el = game.cellElements[multiMode.cursor.r]?.[multiMode.cursor.c];
    if (el) el.classList.add('cursor');
}

function moveCursor(dr, dc) {
    if (!multiMode.active || multiMode.gameEnded || !multiMode.roundActive) return;
    if (multiMode.currentPlayer !== 1 && multiMode.currentPlayer !== 2) return;
    let nr = multiMode.cursor.r + dr;
    let nc = multiMode.cursor.c + dc;
    if (nr >= 0 && nr < game.rows && nc >= 0 && nc < game.cols) {
        multiMode.cursor = { r: nr, c: nc };
        showCursor();
    }
}

function generateNewBoard() {
    game.board = [];
    game.cellsRevealed = 0;
    game.gameOver = false;
    game.gameWin = false;

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

    multiMode.cursor = { r: Math.floor(game.rows / 2), c: Math.floor(game.cols / 2) };
    renderBoard();
    updateMine();
}

function endCurrentTurn() {
    let player = multiMode.currentPlayer;
    multiMode.scores[player] += multiMode.currentRoundScore;
    multiMode.playerRounds[player]++;

    // 重置该玩家的踩雷计数（新回合重新开始）
    multiMode.mineHitCount[player] = 0;

    let playerName = player === 1 ? '红方' : (player === 2 ? '蓝方' : '绿方');
    dom.msg.innerText = `🏁 ${playerName} 第${multiMode.playerRounds[player]}回合结束！获得 ${multiMode.currentRoundScore} 分！总分: ${multiMode.scores[player]}`;
    setTimeout(() => {
        if (dom.msg.innerText.includes('回合结束')) dom.msg.innerText = '';
    }, 2500);

    let allFinished = true;
    for (let i = 1; i <= multiMode.playerCount; i++) {
        if (multiMode.playerRounds[i] < multiMode.maxRounds) allFinished = false;
    }

    if (allFinished) {
        multiMode.gameEnded = true;
        updateMultiUI();
        return;
    }

    multiMode.currentPlayer = multiMode.currentPlayer % multiMode.playerCount + 1;
    multiMode.currentRoundScore = 0;
    multiMode.roundActive = true;
    generateNewBoard();

    let nextPlayerName = multiMode.currentPlayer === 1 ? '红方' : (multiMode.currentPlayer === 2 ? '蓝方' : '绿方');
    dom.msg.innerText = `🔄 换人！轮到 ${nextPlayerName} 开始第${multiMode.playerRounds[multiMode.currentPlayer] + 1}回合！`;
    setTimeout(() => {
        if (dom.msg.innerText.includes('换人')) dom.msg.innerText = '';
    }, 2000);

    updateMultiUI();
    showCursor();
}

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
    if (multiMode.active && !multiMode.gameEnded && multiMode.roundActive &&
        (multiMode.currentPlayer === 1 || multiMode.currentPlayer === 2) &&
        multiMode.cursor.r === r && multiMode.cursor.c === c) {
        el.classList.add('cursor');
    }
}

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

            if (!multiMode.active) {
                div.addEventListener('click', onLeftClick);
                div.addEventListener('contextmenu', onRightClick);
            } else {
                if (multiMode.currentPlayer === 3 && multiMode.roundActive && !multiMode.gameEnded) {
                    div.addEventListener('click', onMouseClick);
                }
                div.addEventListener('contextmenu', onMultiRightClick);
            }
            dom.board.appendChild(div);
            rowEls.push(div);
        }
        game.cellElements.push(rowEls);
    }
    if (multiMode.active && !multiMode.gameEnded && multiMode.roundActive &&
        (multiMode.currentPlayer === 1 || multiMode.currentPlayer === 2)) {
        showCursor();
    }
}

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

    for (let r = 0; r < game.rows; r++) {
        for (let c = 0; c < game.cols; c++) {
            if (game.board[r][c].floodVisited) delete game.board[r][c].floodVisited;
        }
    }

    if (revealedCount >= 5) {
        showComboMessage(revealedCount);
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

function openCursorCell() {
    if (!multiMode.active || multiMode.gameEnded || !multiMode.roundActive) return;
    if (multiMode.currentPlayer !== 1 && multiMode.currentPlayer !== 2) return;
    let { r, c } = multiMode.cursor;
    revealCellMulti(r, c);
}

function revealCellMulti(row, col) {
    if (multiMode.gameEnded || !multiMode.roundActive) return;
    let cell = game.board[row][col];
    if (cell.revealed || cell.flagged) return;

    let playerName = multiMode.currentPlayer === 1 ? '红方' : (multiMode.currentPlayer === 2 ? '蓝方' : '绿方');
    let player = multiMode.currentPlayer;

    if (cell.mine) {
        // 记录本次踩雷
        multiMode.mineHitCount[player]++;
        let hitCount = multiMode.mineHitCount[player];

        // 显示踩雷效果
        cell.revealed = true;
        cell.exploded = true;
        updateCell(row, col);

        // 扣5分
        multiMode.currentRoundScore = Math.max(0, multiMode.currentRoundScore - 5);

        // 判断是否是第三次踩雷
        if (hitCount >= 3) {
            // 第三次踩雷，结束回合
            dom.msg.innerText = `💥 ${playerName} 第${hitCount}次踩雷！本回合结束，获得 ${multiMode.currentRoundScore} 分`;
            setTimeout(() => {
                if (dom.msg.innerText.includes('踩雷')) dom.msg.innerText = '';
            }, 2000);

            // 重置该玩家的踩雷计数（下一回合重新开始）
            multiMode.mineHitCount[player] = 0;
            multiMode.roundActive = false;
            endCurrentTurn();
            return;
        } else {
            // 前两次踩雷，只扣分，回合继续
            dom.msg.innerText = `⚠️ ${playerName} 踩到雷！第${hitCount}次，扣5分！还剩${3 - hitCount}次机会。本轮累计: ${multiMode.currentRoundScore}分`;
            setTimeout(() => {
                if (dom.msg.innerText.includes('踩到雷')) dom.msg.innerText = '';
            }, 2000);

            updateMultiUI();
            updateMine();
            return;
        }
    }

    // 安全格，正常计分
    let queue = [{ r: row, c: col }];
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

    // 连击特效
    if (revealedCount >= 5) {
        showComboMessage(revealedCount);

        let bonus = 0;
        if (revealedCount >= 30) bonus = 10;
        else if (revealedCount >= 20) bonus = 6;
        else if (revealedCount >= 10) bonus = 3;
        else if (revealedCount >= 5) bonus = 1;

        if (bonus > 0) {
            multiMode.currentRoundScore += bonus;
            dom.msg.innerText = `✨ ${playerName} +${bonus} 连击奖励！ 本轮累计: ${multiMode.currentRoundScore}分`;
            setTimeout(() => {
                if (dom.msg.innerText.includes('连击奖励')) dom.msg.innerText = '';
            }, 1500);
        }
    }

    let gain = revealedCount;
    if (game.board[row][col].neighborMines > 0) gain += 1;
    multiMode.currentRoundScore += gain;

    dom.msg.innerText = `✨ ${playerName} +${gain}分！ 本轮累计: ${multiMode.currentRoundScore}分`;
    setTimeout(() => {
        if (dom.msg.innerText.includes('+')) dom.msg.innerText = '';
    }, 1000);

    updateMultiUI();
    updateMine();

    let allSafe = true;
    for (let i = 0; i < game.rows; i++) {
        for (let j = 0; j < game.cols; j++) {
            if (!game.board[i][j].mine && !game.board[i][j].revealed) {
                allSafe = false;
                break;
            }
        }
    }

    if (allSafe) {
        dom.msg.innerText = `🎉 ${playerName} 翻完所有安全格！本回合结束，获得 ${multiMode.currentRoundScore} 分`;
        setTimeout(() => {
            if (dom.msg.innerText.includes('安全格')) dom.msg.innerText = '';
        }, 2000);
        multiMode.mineHitCount[player] = 0;
        multiMode.roundActive = false;
        endCurrentTurn();
    }
}

// ========== 鼠标点击处理（玩家3专用） ==========
function onMouseClick(e) {
    e.preventDefault();
    if (!multiMode.active || multiMode.gameEnded || !multiMode.roundActive) return;
    if (multiMode.currentPlayer !== 3) return;
    let r = parseInt(e.currentTarget.dataset.row);
    let c = parseInt(e.currentTarget.dataset.col);
    let cell = game.board[r][c];
    if (cell.flagged) return;
    revealCellMulti(r, c);
}

function onMultiRightClick(e) {
    e.preventDefault();
    if (!multiMode.active || multiMode.gameEnded || !multiMode.roundActive) return;
    let r = parseInt(e.currentTarget.dataset.row);
    let c = parseInt(e.currentTarget.dataset.col);
    let cell = game.board[r][c];
    if (cell.revealed) return;
    cell.flagged = !cell.flagged;
    updateCell(r, c);
    updateMine();
}

function onLeftClick(e) {
    e.preventDefault();
    if (multiMode.active || game.gameOver || game.gameWin) return;
    let r = parseInt(e.currentTarget.dataset.row);
    let c = parseInt(e.currentTarget.dataset.col);
    if (game.board[r][c].flagged) return;
    revealCell(r, c);
    updateMine();
}

function onRightClick(e) {
    e.preventDefault();
    if (multiMode.active || game.gameOver || game.gameWin) return;
    let r = parseInt(e.currentTarget.dataset.row);
    let c = parseInt(e.currentTarget.dataset.col);
    let cell = game.board[r][c];
    if (cell.revealed) return;
    cell.flagged = !cell.flagged;
    updateCell(r, c);
    updateMine();
}

function onKeyDown(e) {
    if (!multiMode.active || multiMode.gameEnded || !multiMode.roundActive) return;
    let key = e.key;

    if (multiMode.currentPlayer === 1) {
        switch(key) {
            case 'a': case 'A': e.preventDefault(); moveCursor(0, -1); break;
            case 'd': case 'D': e.preventDefault(); moveCursor(0, 1); break;
            case 'w': case 'W': e.preventDefault(); moveCursor(-1, 0); break;
            case 's': case 'S': e.preventDefault(); moveCursor(1, 0); break;
            case ' ': case 'Space': e.preventDefault(); openCursorCell(); break;
        }
    }
    else if (multiMode.currentPlayer === 2) {
        switch(key) {
            case 'ArrowLeft': e.preventDefault(); moveCursor(0, -1); break;
            case 'ArrowRight': e.preventDefault(); moveCursor(0, 1); break;
            case 'ArrowUp': e.preventDefault(); moveCursor(-1, 0); break;
            case 'ArrowDown': e.preventDefault(); moveCursor(1, 0); break;
            case ' ': case 'Space': e.preventDefault(); openCursorCell(); break;
        }
    }
}

function setMode(mode, playerCount = 2) {
    if (mode === 'single') {
        multiMode.active = false;
        dom.players.style.display = 'none';
        dom.tip.style.display = 'none';
        dom.single.classList.add('active');
        dom.double.classList.remove('active');
        if (dom.triple) dom.triple.classList.remove('active');
        document.removeEventListener('keydown', onKeyDown);
        generateNewBoard();
    } else if (mode === 'double') {
        multiMode = {
            active: true,
            playerCount: 2,
            currentPlayer: 1,
            scores: { 1: 0, 2: 0, 3: 0 },
            currentRoundScore: 0,
            maxRounds: 3,
            playerRounds: { 1: 0, 2: 0, 3: 0 },
            gameEnded: false,
            roundActive: true,
            cursor: { r: Math.floor(game.rows / 2), c: Math.floor(game.cols / 2) },
            mineHitCount: { 1: 0, 2: 0, 3: 0 }
        };
        dom.players.style.display = 'flex';
        dom.tip.style.display = 'block';
        dom.single.classList.remove('active');
        dom.double.classList.add('active');
        if (dom.triple) dom.triple.classList.remove('active');
        if (dom.p3Card) dom.p3Card.style.display = 'none';
        generateNewBoard();
        updateMultiUI();
        document.removeEventListener('keydown', onKeyDown);
        document.addEventListener('keydown', onKeyDown);
        dom.msg.innerText = '🎮 双人对战开始！每回合新棋盘，每人3回合，前两次踩雷只扣分不结束！🔴 红方先开始！';
        setTimeout(() => {
            if (dom.msg.innerText.includes('对战')) dom.msg.innerText = '';
        }, 5000);
    } else if (mode === 'triple' && dom.triple) {
        multiMode = {
            active: true,
            playerCount: 3,
            currentPlayer: 1,
            scores: { 1: 0, 2: 0, 3: 0 },
            currentRoundScore: 0,
            maxRounds: 3,
            playerRounds: { 1: 0, 2: 0, 3: 0 },
            gameEnded: false,
            roundActive: true,
            cursor: { r: Math.floor(game.rows / 2), c: Math.floor(game.cols / 2) },
            mineHitCount: { 1: 0, 2: 0, 3: 0 }
        };
        dom.players.style.display = 'flex';
        dom.tip.style.display = 'block';
        dom.single.classList.remove('active');
        dom.double.classList.remove('active');
        dom.triple.classList.add('active');
        if (dom.p3Card) dom.p3Card.style.display = 'block';
        generateNewBoard();
        updateMultiUI();
        document.removeEventListener('keydown', onKeyDown);
        document.addEventListener('keydown', onKeyDown);
        dom.msg.innerText = '🎮 三人对战开始！每回合新棋盘，每人3回合，前两次踩雷只扣分不结束！🔴 红方先开始！';
        setTimeout(() => {
            if (dom.msg.innerText.includes('对战')) dom.msg.innerText = '';
        }, 5000);
    }
}

function setDifficulty(level) {
    let cfg = DIFFICULTY[level];
    game.rows = cfg.rows;
    game.cols = cfg.cols;
    game.totalMines = cfg.mines;

    if (multiMode.active) {
        multiMode.cursor = { r: Math.floor(game.rows / 2), c: Math.floor(game.cols / 2) };
        multiMode.currentPlayer = 1;
        multiMode.scores = { 1: 0, 2: 0, 3: 0 };
        multiMode.currentRoundScore = 0;
        multiMode.playerRounds = { 1: 0, 2: 0, 3: 0 };
        multiMode.mineHitCount = { 1: 0, 2: 0, 3: 0 };
        multiMode.gameEnded = false;
        multiMode.roundActive = true;
        generateNewBoard();
        updateMultiUI();
        dom.msg.innerText = `📊 难度已切换！🔴 红方先开始！`;
        setTimeout(() => {
            if (dom.msg.innerText.includes('难度')) dom.msg.innerText = '';
        }, 2000);
    } else {
        generateNewBoard();
    }
}

function resetGame() {
    if (multiMode.active) {
        multiMode.currentPlayer = 1;
        multiMode.scores = { 1: 0, 2: 0, 3: 0 };
        multiMode.currentRoundScore = 0;
        multiMode.playerRounds = { 1: 0, 2: 0, 3: 0 };
        multiMode.mineHitCount = { 1: 0, 2: 0, 3: 0 };
        multiMode.gameEnded = false;
        multiMode.roundActive = true;
        multiMode.cursor = { r: Math.floor(game.rows / 2), c: Math.floor(game.cols / 2) };
        generateNewBoard();
        updateMultiUI();
        dom.msg.innerText = '🔄 游戏已重置！🔴 红方先开始新回合！';
        setTimeout(() => {
            if (dom.msg.innerText.includes('重置')) dom.msg.innerText = '';
        }, 2000);
    } else {
        generateNewBoard();
    }
}

function start() {
    initDOM();
    dom.reset.addEventListener('click', resetGame);
    dom.easy.addEventListener('click', () => setDifficulty('easy'));
    dom.medium.addEventListener('click', () => setDifficulty('medium'));
    dom.hard.addEventListener('click', () => setDifficulty('hard'));
    dom.single.addEventListener('click', () => setMode('single'));
    dom.double.addEventListener('click', () => setMode('double'));
    if (dom.triple) dom.triple.addEventListener('click', () => setMode('triple'));
    document.addEventListener('contextmenu', (e) => {
        if (e.target.closest('.cell') && !multiMode.active) e.preventDefault();
    });
    setMode('single');
}

start();