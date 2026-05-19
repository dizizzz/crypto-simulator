const difficultyInput = document.getElementById('difficultyBits');
const difficultyHint = document.getElementById('difficultyHint');

difficultyInput.addEventListener('input', () => {
    const bits = parseInt(difficultyInput.value) || 0;
    const attempts = Math.pow(2, bits);
    difficultyHint.innerHTML = `≈ <code>${attempts.toLocaleString('uk')}</code> спроб nonce`;
});

const form = document.getElementById('config-form');
const resultsSection = document.getElementById('results-section');
const resultsContent = document.getElementById('results-content');
const statusEl = document.getElementById('status');
const metricsEl = document.getElementById('metrics');
const runBtn = document.getElementById('run-btn');
let balancesChart = null;
let blocksChart = null;

form.addEventListener('submit', async (e) => {
    e.preventDefault();
    await runSimulation();
});

async function runSimulation() {
    const config = collectConfig();

    showStatus('Виконується симуляція...', 'loading');
    runBtn.disabled = true;
    runBtn.innerHTML = '<span class="spinner"></span> Виконується...';
    metricsEl.innerHTML = '';
    resultsContent.hidden = true;
    resultsSection.hidden = false;

    try {
        const response = await fetch('/simulate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(config)
        });

        if (!response.ok) {
            throw new Error(`Сервер відповів з кодом ${response.status}`);
        }

        const result = await response.json();
        showStatus('Симуляція завершена', 'success');
        resultsContent.hidden = false;
        renderMetrics(result);
        renderBalancesChart(result);
        renderBlocksChart(result);
    } catch (error) {
        showStatus(`Помилка: ${error.message}`, 'error');
    } finally {
        runBtn.disabled = false;
        runBtn.innerHTML = '▶ Запустити симуляцію';
    }
}

function collectConfig() {
    return {
        seed: parseInt(document.getElementById('seed').value),
        numWallets: parseInt(document.getElementById('numWallets').value),
        initialBalancePerWallet: parseInt(document.getElementById('initialBalance').value),
        difficultyBits: parseInt(document.getElementById('difficultyBits').value),
        durationMs: parseInt(document.getElementById('durationMs').value),
        transactionIntervalMs: parseInt(document.getElementById('transactionIntervalMs').value),
        blockIntervalMs: parseInt(document.getElementById('blockIntervalMs').value),
        mempoolMaxSize: parseInt(document.getElementById('mempoolMaxSize').value),
        maxTransactionsPerBlock: 20
    };
}

function showStatus(message, type) {
    if (type === 'loading') {
        statusEl.innerHTML = `${message}<span class="status-loader"><span></span><span></span><span></span></span>`;
    } else {
        statusEl.textContent = message;
    }
    statusEl.className = type;
}

function renderMetrics(result) {
    const stats = result.stats;
    const cards = [
        { label: 'Блоків замайнено', value: stats.totalBlocks },
        { label: 'Транзакцій надіслано', value: stats.submittedTransactions },
        { label: 'Прийнято в мемпул', value: stats.acceptedTransactions },
        { label: 'Підтверджено в блок', value: stats.confirmedTransactions },
        { label: 'Сумарний fee', value: stats.totalFeesPaid },
        { label: 'Середня латентність', value: `${stats.averageConfirmationLatencyMs.toFixed(1)} мс` },
        { label: 'Середня кількість спроб nonce', value: stats.averageNonceAttempts.toFixed(0) },
        { label: 'Час майнінгу (середній)', value: `${stats.averageBlockMiningTimeMs.toFixed(1)} мс` },
    ];

    metricsEl.innerHTML = cards.map(card => `
        <div class="metric-card">
            <div class="metric-label">${card.label}</div>
            <div class="metric-value">${card.value}</div>
        </div>
    `).join('');
}

function renderBalancesChart(result) {
    const balances = result.finalBalances;
    const labels = balances.map(b => b.label);
    const values = balances.map(b => b.balance);

    if (balancesChart) {
        balancesChart.destroy();
    }

    balancesChart = new Chart(document.getElementById('balances-chart'), {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Баланс',
                data: values,
                backgroundColor: '#0071e3'
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } }
        }
    });
}

function renderBlocksChart(result) {
    const blocks = result.blockchain.blocks;
    const labels = [];
    const counts = [];

    for (let i = 0; i < blocks.length; i++) {
        labels.push(`Block ${blocks[i].header.index}`);
        counts.push(blocks[i].transactions.length);
    }

    if (blocksChart) {
        blocksChart.destroy();
    }

    blocksChart = new Chart(document.getElementById('blocks-chart'), {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Транзакцій',
                data: counts,
                backgroundColor: '#34c759'
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } }
        }
    });
}