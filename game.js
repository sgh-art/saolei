// ========== 游戏配置 ==========
const DIFFICULTY = {
    easy: { rows: 8, cols: 8, mines: 10 },
    medium: { rows: 16, cols: 16, mines: 40 },
    hard: { rows: 16, cols: 30, mines: 99 }
};

// ========== 连击提示消息库 ==========
const comboMessages = [
    "🎉 连击！", "✨ 漂亮！", "⭐ 太棒了！", "💥 连锁反应！",
    "🌟 完美！", "🎈 哇哦！", "🏆 高手！", "💪 厉害！"
];

// ========== 音效系统 ==========
let audio = {
    bgm: null, click: null, explode: null, win: null, combo: null,
    bgmEnabled: true, sfxEnabled: true, bgmPlaying: false
};

function initAudio() {
    audio.bgm = document.getElementById('bgmAudio');
    audio.click = document.getElementById('clickAudio');
    audio.explode = document.getElementById('explodeAudio');
    audio.win = document.getElementById('winAudio');
    audio.combo = document.getElementById('comboAudio');
    if (audio.bgm) audio.bgm.volume = 0.3;
}

function playSound(snd, vol) {
    if (!audio.sfxEnabled || !snd) return;
    let clone = snd.cloneNode();
    clone.volume = vol || 0.4;
    clone.play().catch(e => {});
    clone.onended = () => clone.remove();
}

function playBGM() {
    if (!audio.bgmEnabled || !audio.bgm || audio.bgmPlaying) return;
    audio.bgm.play().catch(e => {});
    audio.bgmPlaying = true;
}

function stopBGM() {
    if (!audio.bgm) return;
    audio.bgm.pause();
    audio.bgm.currentTime = 0;
    audio.bgmPlaying = false;
}

function toggleBGM() {
    audio.bgmEnabled = !audio.bgmEnabled;
    let btn = document.getElementById('toggleBgmBtn');
    if (audio.bgmEnabled) { playBGM(); if(btn) btn.textContent = '🎵 背景音乐'; if(btn) btn.classList.remove('muted'); }
    else { stopBGM(); if(btn) btn.textContent = '🔇 背景音乐'; if(btn) btn.classList.add('muted'); }
}

function toggleSFX() {
    audio.sfxEnabled = !audio.sfxEnabled;
    let btn = document.getElementById('toggleSfxBtn');
    if(btn) btn.textContent = audio.sfxEnabled ? '🔊 音效' : '🔇 音效';
    if(btn) btn.classList.toggle('muted', !audio.sfxEnabled);
}

function showCombo(count) {
    if (count < 5) return;
    let msg = count >= 30 ? `🏆 超级连击！翻开了 ${count} 个格子！🏆` :
              count >= 20 ? `⭐ 太厉害了！翻开了 ${count} 个格子！⭐` :
              count >= 10 ? `🎉 精彩连击！翻开了 ${count} 个格子！🎉` :
              `${comboMessages[Math.floor(Math.random() * comboMessages.length)]} 翻开了 ${count} 个格子！`;
    let toast = document.createElement('div');
    toast.className = 'combo-toast';
    toast.innerText = msg;
    toast.style.cssText = 'position:fixed;top:30%;left:50%;transform:translate(-50%,-50%);background:linear-gradient(135deg,#ffd966,#ffa502);color:#2c3e50;padding:15px 30px;border-radius:50px;font-size:1.5rem;font-weight:bold;box-shadow:0 10px 30px rgba(0,0,0,0.3);z-index:9999;text-align:center;white-space:nowrap';
    document.body.appendChild(toast);
    setTimeout(() => { toast.style.opacity = '0'; toast.style.transition = 'opacity 0.5s'; setTimeout(() => toast.remove(), 500); }, 2000);
    let old = dom.msg.innerText;
    dom.msg.innerText = msg;
    setTimeout(() => { if (dom.msg.innerText === msg) dom.msg.innerText = old.includes('+') ? old : ''; }, 2000);
}

// ========== 游戏状态 ==========
let game = {
    rows: 8, cols: 8, totalMines: 10,
    board: [], cellsRevealed: 0,
    gameOver: false, gameWin: false,
    cellElements: []
};

let multi = {
    active: false, playerCount: 2, current: 1,
    scores: {1:0,2:0,3:0}, roundScore: 0,
    rounds: {1:0,2:0,3:0}, maxRounds: 3,
    ended: false, roundActive: true,
    cursor: {r:0,c:0}, hitCount: {1:0,2:0,3:0}
};

let dom = {};

function initDOM() {
    dom = {
        board: document.getElementById('board'), mine: document.getElementById('mineDisplay'),
        reset: document.getElementById('resetButton'), msg: document.getElementById('messageArea'),
        easy: document.getElementById('easyBtn'), medium: document.getElementById('mediumBtn'), hard: document.getElementById('hardBtn'),
        single: document.getElementById('singleModeBtn'), double: document.getElementById('doubleModeBtn'), triple: document.getElementById('tripleModeBtn'),
        players: document.getElementById('playersPanel'), tip: document.getElementById('doubleTip'),
        p1Score: document.getElementById('player1Score'), p2Score: document.getElementById('player2Score'), p3Score: document.getElementById('player3Score'),
        p1Turn: document.getElementById('player1Turn'), p2Turn: document.getElementById('player2Turn'), p3Turn: document.getElementById('player3Turn'),
        p1Card: document.getElementById('player1Card'), p2Card: document.getElementById('player2Card'), p3Card: document.getElementById('player3Card')
    };
}

function format(n) { return n.toString().padStart(3,'0').slice(0,3); }

function updateMine() {
    let f = game.board.flat().filter(c => c.flagged).length;
    dom.mine.innerText = format(game.totalMines - f);
}

function updateUI() {
    dom.p1Score.innerText = `总分: ${multi.scores[1]}`;
    dom.p2Score.innerText = `总分: ${multi.scores[2]}`;
    if (multi.playerCount === 3 && dom.p3Score) dom.p3Score.innerText = `总分: ${multi.scores[3]}`;
    if (!multi.active || multi.ended) return;
    let p1r = `回合 ${multi.rounds[1]}/${multi.maxRounds}`;
    let p2r = `回合 ${multi.rounds[2]}/${multi.maxRounds}`;
    let p3r = multi.playerCount === 3 ? `回合 ${multi.rounds[3]}/${multi.maxRounds}` : '';
    [dom.p1Card, dom.p2Card, dom.p3Card].forEach(c => c?.classList.remove('active-turn'));
    if (multi.current === 1) {
        dom.p1Turn.innerText = `👑 ${p1r} | 本轮: ${multi.roundScore}`;
        dom.p2Turn.innerText = `⏳ ${p2r}`;
        if (multi.playerCount === 3) dom.p3Turn.innerText = `⏳ ${p3r}`;
        dom.p1Card.classList.add('active-turn');
        dom.tip.innerText = '💡 玩家1: WASD移动光标，空格翻开';
    } else if (multi.current === 2) {
        dom.p1Turn.innerText = `⏳ ${p1r}`;
        dom.p2Turn.innerText = `👑 ${p2r} | 本轮: ${multi.roundScore}`;
        if (multi.playerCount === 3) dom.p3Turn.innerText = `⏳ ${p3r}`;
        dom.p2Card.classList.add('active-turn');
        dom.tip.innerText = '💡 玩家2: 方向键移动光标，空格翻开';
    } else if (multi.current === 3) {
        dom.p1Turn.innerText = `⏳ ${p1r}`;
        dom.p2Turn.innerText = `⏳ ${p2r}`;
        dom.p3Turn.innerText = `👑 ${p3r} | 本轮: ${multi.roundScore}`;
        dom.p3Card.classList.add('active-turn');
        dom.tip.innerText = '💡 玩家3: 鼠标点击翻开';
    }
}

function showCursor() {
    document.querySelectorAll('.cell').forEach(el => el.classList.remove('cursor'));
    let el = game.cellElements[multi.cursor.r]?.[multi.cursor.c];
    if (el) el.classList.add('cursor');
}

function moveCursor(dr, dc) {
    if (!multi.active || multi.ended || !multi.roundActive) return;
    if (multi.current !== 1 && multi.current !== 2) return;
    let nr = multi.cursor.r + dr, nc = multi.cursor.c + dc;
    if (nr >= 0 && nr < game.rows && nc >= 0 && nc < game.cols) {
        multi.cursor = {r:nr, c:nc};
        showCursor();
    }
}

function newBoard() {
    game.board = []; game.cellsRevealed = 0; game.gameOver = false; game.gameWin = false;
    for (let r = 0; r < game.rows; r++) {
        let row = [];
        for (let c = 0; c < game.cols; c++) row.push({mine:false, revealed:false, flagged:false, neighborMines:0, exploded:false});
        game.board.push(row);
    }
    let p = 0;
    while (p < game.totalMines) {
        let r = Math.floor(Math.random() * game.rows), c = Math.floor(Math.random() * game.cols);
        if (!game.board[r][c].mine) { game.board[r][c].mine = true; p++; }
    }
    for (let r = 0; r < game.rows; r++) {
        for (let c = 0; c < game.cols; c++) {
            if (game.board[r][c].mine) continue;
            let cnt = 0;
            for (let dr = -1; dr <= 1; dr++) for (let dc = -1; dc <= 1; dc++) {
                if (dr===0 && dc===0) continue;
                let nr = r+dr, nc = c+dc;
                if (nr>=0 && nr<game.rows && nc>=0 && nc<game.cols && game.board[nr][nc].mine) cnt++;
            }
            game.board[r][c].neighborMines = cnt;
        }
    }
    multi.cursor = {r:Math.floor(game.rows/2), c:Math.floor(game.cols/2)};
    renderBoard();
    updateMine();
}

function endTurn() {
    let p = multi.current;
    multi.scores[p] += multi.roundScore;
    multi.rounds[p]++;
    multi.hitCount[p] = 0;
    let name = p===1?'红方':(p===2?'蓝方':'绿方');
    dom.msg.innerText = `🏁 ${name} 第${multi.rounds[p]}回合结束！得${multi.roundScore}分！总分:${multi.scores[p]}`;
    setTimeout(() => { if(dom.msg.innerText.includes('回合结束')) dom.msg.innerText = ''; }, 2500);
    let all = true;
    for(let i=1;i<=multi.playerCount;i++) if(multi.rounds[i] < multi.maxRounds) all=false;
    if(all) { multi.ended = true; playSound(audio.win,0.6); updateUI(); return; }
    multi.current = multi.current % multi.playerCount + 1;
    multi.roundScore = 0; multi.roundActive = true;
    newBoard();
    let next = multi.current===1?'红方':(multi.current===2?'蓝方':'绿方');
    dom.msg.innerText = `🔄 换人！轮到 ${next} 第${multi.rounds[multi.current]+1}回合！`;
    setTimeout(() => { if(dom.msg.innerText.includes('换人')) dom.msg.innerText = ''; }, 2000);
    updateUI(); showCursor();
}

function updateCell(r,c) {
    let cell = game.board[r][c], el = game.cellElements[r]?.[c];
    if(!el) return;
    el.className = 'cell';
    if(cell.revealed) {
        el.classList.add('revealed');
        if(cell.mine) { el.classList.add('mine-icon'); if(cell.exploded) el.classList.add('exploded'); }
        else if(cell.neighborMines>0) el.innerText = cell.neighborMines;
    } else { if(cell.flagged) el.classList.add('flagged'); el.innerText = ''; }
    if(multi.active && !multi.ended && multi.roundActive && (multi.current===1||multi.current===2) && multi.cursor.r===r && multi.cursor.c===c) el.classList.add('cursor');
}

function renderBoard() {
    dom.board.innerHTML = '';
    dom.board.style.gridTemplateColumns = `repeat(${game.cols}, 34px)`;
    game.cellElements = [];
    for(let r=0; r<game.rows; r++) {
        let rowEls = [];
        for(let c=0; c<game.cols; c++) {
            let cell = game.board[r][c];
            let div = document.createElement('div');
            div.className = 'cell';
            if(cell.revealed) {
                div.classList.add('revealed');
                if(cell.mine) { div.classList.add('mine-icon'); if(cell.exploded) div.classList.add('exploded'); }
                else if(cell.neighborMines>0) div.innerText = cell.neighborMines;
            } else if(cell.flagged) div.classList.add('flagged');
            div.dataset.row = r; div.dataset.col = c;
            if(multi.active) div.addEventListener('click', onMultiClick);
            else { div.addEventListener('click', onSingleClick); div.addEventListener('contextmenu', onSingleRight); }
            dom.board.appendChild(div);
            rowEls.push(div);
        }
        game.cellElements.push(rowEls);
    }
    if(multi.active && !multi.ended && multi.roundActive && (multi.current===1||multi.current===2)) showCursor();
}

function onMultiClick(e) {
    e.preventDefault();
    if(!multi.active || multi.ended || !multi.roundActive) return;
    let r = parseInt(e.currentTarget.dataset.row), c = parseInt(e.currentTarget.dataset.col);
    if(game.board[r][c].flagged) return;
    if(multi.current === 3) revealMulti(r,c);
    else { multi.cursor = {r,c}; showCursor(); openCursor(); }
}

function onMultiRight(e) {
    e.preventDefault();
    if(!multi.active || multi.ended || !multi.roundActive) return;
    let r = parseInt(e.currentTarget.dataset.row), c = parseInt(e.currentTarget.dataset.col);
    let cell = game.board[r][c];
    if(cell.revealed) return;
    cell.flagged = !cell.flagged;
    updateCell(r,c); updateMine();
}

function onSingleClick(e) {
    e.preventDefault();
    if(multi.active || game.gameOver || game.gameWin) return;
    let r = parseInt(e.currentTarget.dataset.row), c = parseInt(e.currentTarget.dataset.col);
    if(game.board[r][c].flagged) return;
    revealSingle(r,c); updateMine();
}

function onSingleRight(e) {
    e.preventDefault();
    if(multi.active || game.gameOver || game.gameWin) return;
    let r = parseInt(e.currentTarget.dataset.row), c = parseInt(e.currentTarget.dataset.col);
    let cell = game.board[r][c];
    if(cell.revealed) return;
    cell.flagged = !cell.flagged;
    updateCell(r,c); updateMine();
}

function revealSingle(r,c) {
    if(game.gameOver || game.gameWin) return;
    let cell = game.board[r][c];
    if(cell.revealed || cell.flagged) return;
    if(cell.mine) {
        cell.revealed = true; cell.exploded = true; game.gameOver = true;
        for(let i=0;i<game.rows;i++) for(let j=0;j<game.cols;j++) { let c2=game.board[i][j]; if(c2.mine) { c2.revealed=true; if(i===r&&j===c) c2.exploded=true; } if(c2.flagged&&c2.mine) c2.flagged=false; updateCell(i,j); }
        dom.msg.innerText = '💥 踩雷了...';
        return;
    }
    let q=[{r,c}], cnt=0;
    while(q.length) {
        let {r,c}=q.shift(), cur=game.board[r][c];
        if(cur.revealed||cur.flagged) continue;
        cur.revealed=true; cnt++; game.cellsRevealed++;
        if(cur.neighborMines>0) { updateCell(r,c); continue; }
        updateCell(r,c);
        for(let dr=-1;dr<=1;dr++) for(let dc=-1;dc<=1;dc++) {
            if(dr===0&&dc===0) continue;
            let nr=r+dr, nc=c+dc;
            if(nr>=0&&nr<game.rows&&nc>=0&&nc<game.cols) {
                let nb=game.board[nr][nc];
                if(!nb.revealed&&!nb.flagged&&!nb.mine&&!nb.floodVisited) { nb.floodVisited=true; q.push({r:nr,c:nc}); }
            }
        }
    }
    for(let i=0;i<game.rows;i++) for(let j=0;j<game.cols;j++) delete game.board[i][j].floodVisited;
    if(cnt>=5) { showCombo(cnt); playSound(audio.combo,0.4); }
    let total=game.rows*game.cols-game.totalMines;
    if(game.cellsRevealed===total && !game.gameOver) {
        game.gameWin=true; game.gameOver=true; playSound(audio.win,0.6);
        dom.msg.innerText = '🎉 恭喜！你赢了！ 🎉';
        for(let i=0;i<game.rows;i++) for(let j=0;j<game.cols;j++) { let c2=game.board[i][j]; if(c2.mine && !c2.flagged && !c2.revealed) { c2.flagged=true; updateCell(i,j); } }
        updateMine();
    }
}

function openCursor() {
    if(!multi.active || multi.ended || !multi.roundActive) return;
    if(multi.current!==1 && multi.current!==2) return;
    revealMulti(multi.cursor.r, multi.cursor.c);
}

function revealMulti(r,c) {
    if(multi.ended || !multi.roundActive) return;
    let cell = game.board[r][c];
    if(cell.revealed || cell.flagged) return;
    let p = multi.current, name = p===1?'红方':(p===2?'蓝方':'绿方');
    if(cell.mine) {
        playSound(audio.explode,0.6);
        multi.hitCount[p]++;
        let hit = multi.hitCount[p];
        cell.revealed=true; cell.exploded=true; updateCell(r,c);
        multi.roundScore = Math.max(0, multi.roundScore - 5);
        if(hit >= 3) {
            dom.msg.innerText = `💥 ${name} 第${hit}次踩雷！回合结束，得${multi.roundScore}分`;
            setTimeout(()=>{if(dom.msg.innerText.includes('踩雷')) dom.msg.innerText='';},2000);
            multi.hitCount[p]=0; multi.roundActive=false; endTurn();
            return;
        } else {
            dom.msg.innerText = `⚠️ ${name} 踩雷！第${hit}次，扣5分！剩${3-hit}次机会。本轮:${multi.roundScore}分`;
            setTimeout(()=>{if(dom.msg.innerText.includes('踩雷')) dom.msg.innerText='';},2000);
            updateUI(); updateMine();
            return;
        }
    }
    let q=[{r,c}], cnt=0;
    while(q.length) {
        let {r,c}=q.shift(), cur=game.board[r][c];
        if(cur.revealed||cur.flagged) continue;
        cur.revealed=true; cnt++; game.cellsRevealed++;
        if(cur.neighborMines>0) { updateCell(r,c); continue; }
        updateCell(r,c);
        for(let dr=-1;dr<=1;dr++) for(let dc=-1;dc<=1;dc++) {
            if(dr===0&&dc===0) continue;
            let nr=r+dr, nc=c+dc;
            if(nr>=0&&nr<game.rows&&nc>=0&&nc<game.cols) {
                let nb=game.board[nr][nc];
                if(!nb.revealed&&!nb.flagged&&!nb.mine&&!nb.floodVisited) { nb.floodVisited=true; q.push({r:nr,c:nc}); }
            }
        }
    }
    for(let i=0;i<game.rows;i++) for(let j=0;j<game.cols;j++) delete game.board[i][j].floodVisited;
    if(cnt>=5) { showCombo(cnt); playSound(audio.combo,0.4); let bonus = cnt>=30?10:(cnt>=20?6:(cnt>=10?3:(cnt>=5?1:0))); if(bonus>0) { multi.roundScore += bonus; dom.msg.innerText = `✨ ${name} +${bonus}连击奖励！ 本轮:${multi.roundScore}分`; setTimeout(()=>{if(dom.msg.innerText.includes('连击')) dom.msg.innerText='';},1500); } }
    else playSound(audio.click,0.3);
    let gain = cnt + (game.board[r][c].neighborMines>0?1:0);
    multi.roundScore += gain;
    dom.msg.innerText = `✨ ${name} +${gain}分！ 本轮累计:${multi.roundScore}分`;
    setTimeout(()=>{if(dom.msg.innerText.includes('+')) dom.msg.innerText='';},1000);
    updateUI(); updateMine();
    let all = true;
    for(let i=0;i<game.rows;i++) for(let j=0;j<game.cols;j++) if(!game.board[i][j].mine && !game.board[i][j].revealed) { all=false; break; }
    if(all) {
        dom.msg.innerText = `🎉 ${name} 翻完所有安全格！得${multi.roundScore}分`;
        setTimeout(()=>{if(dom.msg.innerText.includes('安全格')) dom.msg.innerText='';},2000);
        multi.hitCount[p]=0; multi.roundActive=false; endTurn();
    }
}

function onKey(e) {
    if(!multi.active || multi.ended || !multi.roundActive) return;
    let key = e.key;
    if(multi.current === 1) {
        if(key==='a'||key==='A') { e.preventDefault(); moveCursor(0,-1); }
        else if(key==='d'||key==='D') { e.preventDefault(); moveCursor(0,1); }
        else if(key==='w'||key==='W') { e.preventDefault(); moveCursor(-1,0); }
        else if(key==='s'||key==='S') { e.preventDefault(); moveCursor(1,0); }
        else if(key===' '||key==='Space') { e.preventDefault(); openCursor(); }
    } else if(multi.current === 2) {
        if(key==='ArrowLeft') { e.preventDefault(); moveCursor(0,-1); }
        else if(key==='ArrowRight') { e.preventDefault(); moveCursor(0,1); }
        else if(key==='ArrowUp') { e.preventDefault(); moveCursor(-1,0); }
        else if(key==='ArrowDown') { e.preventDefault(); moveCursor(1,0); }
        else if(key===' '||key==='Space') { e.preventDefault(); openCursor(); }
    }
}

function setMode(mode) {
    if(mode === 'single') {
        multi.active = false;
        dom.players.style.display = 'none'; dom.tip.style.display = 'none';
        dom.single.classList.add('active'); dom.double.classList.remove('active'); if(dom.triple) dom.triple.classList.remove('active');
        document.removeEventListener('keydown', onKey);
        newBoard();
    } else if(mode === 'double') {
        multi = { active:true, playerCount:2, current:1, scores:{1:0,2:0,3:0}, roundScore:0, rounds:{1:0,2:0,3:0}, maxRounds:3, ended:false, roundActive:true, cursor:{r:Math.floor(game.rows/2),c:Math.floor(game.cols/2)}, hitCount:{1:0,2:0,3:0} };
        dom.players.style.display = 'flex'; dom.tip.style.display = 'block';
        dom.single.classList.remove('active'); dom.double.classList.add('active'); if(dom.triple) dom.triple.classList.remove('active');
        if(dom.p3Card) dom.p3Card.style.display = 'none';
        newBoard(); updateUI();
        document.removeEventListener('keydown', onKey); document.addEventListener('keydown', onKey);
        dom.msg.innerText = '🎮 双人对战！每人3回合，前两次踩雷只扣分！🔴 红方先！';
        setTimeout(()=>{if(dom.msg.innerText.includes('对战')) dom.msg.innerText='';},4000);
    } else if(mode === 'triple' && dom.triple) {
        multi = { active:true, playerCount:3, current:1, scores:{1:0,2:0,3:0}, roundScore:0, rounds:{1:0,2:0,3:0}, maxRounds:3, ended:false, roundActive:true, cursor:{r:Math.floor(game.rows/2),c:Math.floor(game.cols/2)}, hitCount:{1:0,2:0,3:0} };
        dom.players.style.display = 'flex'; dom.tip.style.display = 'block';
        dom.single.classList.remove('active'); dom.double.classList.remove('active'); dom.triple.classList.add('active');
        if(dom.p3Card) dom.p3Card.style.display = 'block';
        newBoard(); updateUI();
        document.removeEventListener('keydown', onKey); document.addEventListener('keydown', onKey);
        dom.msg.innerText = '🎮 三人对战！每人3回合，前两次踩雷只扣分！🔴 红方先！';
        setTimeout(()=>{if(dom.msg.innerText.includes('对战')) dom.msg.innerText='';},4000);
    }
}

function setDifficulty(level) {
    let cfg = DIFFICULTY[level];
    game.rows = cfg.rows; game.cols = cfg.cols; game.totalMines = cfg.mines;
    if(multi.active) {
        multi.current=1; multi.scores={1:0,2:0,3:0}; multi.roundScore=0; multi.rounds={1:0,2:0,3:0}; multi.ended=false; multi.roundActive=true; multi.hitCount={1:0,2:0,3:0};
        multi.cursor = {r:Math.floor(game.rows/2),c:Math.floor(game.cols/2)};
        newBoard(); updateUI();
        dom.msg.innerText = `📊 难度切换！🔴 红方先！`;
        setTimeout(()=>{if(dom.msg.innerText.includes('难度')) dom.msg.innerText='';},2000);
    } else newBoard();
}

function resetGame() {
    if(multi.active) {
        multi.current=1; multi.scores={1:0,2:0,3:0}; multi.roundScore=0; multi.rounds={1:0,2:0,3:0}; multi.ended=false; multi.roundActive=true; multi.hitCount={1:0,2:0,3:0};
        multi.cursor = {r:Math.floor(game.rows/2),c:Math.floor(game.cols/2)};
        newBoard(); updateUI();
        dom.msg.innerText = '🔄 游戏重置！🔴 红方先！';
        setTimeout(()=>{if(dom.msg.innerText.includes('重置')) dom.msg.innerText='';},2000);
    } else newBoard();
}

function start() {
    initDOM(); initAudio();
    dom.reset.addEventListener('click', resetGame);
    dom.easy.addEventListener('click', () => setDifficulty('easy'));
    dom.medium.addEventListener('click', () => setDifficulty('medium'));
    dom.hard.addEventListener('click', () => setDifficulty('hard'));
    dom.single.addEventListener('click', () => setMode('single'));
    dom.double.addEventListener('click', () => setMode('double'));
    if(dom.triple) dom.triple.addEventListener('click', () => setMode('triple'));
    document.getElementById('toggleBgmBtn')?.addEventListener('click', toggleBGM);
    document.getElementById('toggleSfxBtn')?.addEventListener('click', toggleSFX);
    document.addEventListener('contextmenu', (e) => { if(e.target.closest('.cell') && !multi.active) e.preventDefault(); });
    document.body.addEventListener('click', () => { if(audio.bgmEnabled && !audio.bgmPlaying) playBGM(); }, { once: true });
    setMode('single');
}

start();