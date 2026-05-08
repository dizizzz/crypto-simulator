const form = document.getElementById('config-form');
const resultsSection = document.getElementById('results-section');
const statusEl = document.getElementById('status');
const metricsEl = document.getElementById('metrics');
const runBtn = document.getElementById('run-btn');

form.addEventListener('submit', async (e) => {
    e.preventDefault();
    await runSimulation();
});

async function runSimulation() {
    const config = collectConfig();

    showStatus('Виконується симуляція...', 'loading');
    runBtn.disabled = true;
    metricsEl.innerHTML = '';
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
        renderMetrics(result);
        renderBalancesChart(result);
        renderBlocksChart(result);
    } catch (error) {
        showStatus(`Помилка: ${error.message}`, 'error');
    } finally {
        runBtn.disabled = false;
    }
}

function collectConfig() {
    return {
        seed: parseInt(document.getElementById('seed').value),
        numWallets: parseInt(document.getElementById('numWallets').value),
        initialBalancePerWallet: parseInt(document.getElementById('initialBalance').value),
        difficultyBits: parseInt(document.getElementById('difficultyBits').value),
        totalTicks: parseInt(document.getElementById('totalTicks').value),
        ticksPerTransaction: parseInt(document.getElementById('ticksPerTransaction').value),
        ticksPerBlock: parseInt(document.getElementById('ticksPerBlock').value),
        mempoolMaxSize: parseInt(document.getElementById('mempoolMaxSize').value),
        maxTransactionsPerBlock: 20
    };
}

function showStatus(message, type) {
    statusEl.textContent = message;
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
        { label: 'Середня латенсія', value: `${stats.averageConfirmationLatencyTicks.toFixed(1)} тіків` },
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

    new Chart(document.getElementById('balances-chart'), {
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

    new Chart(document.getElementById('blocks-chart'), {
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