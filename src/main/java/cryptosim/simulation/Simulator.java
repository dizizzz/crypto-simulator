package cryptosim.simulation;

import cryptosim.chain.Block;
import cryptosim.chain.BlockHeader;
import cryptosim.chain.Blockchain;
import cryptosim.chain.Difficulty;
import cryptosim.chain.Mempool;
import cryptosim.chain.Miner;
import cryptosim.crypto.KeyPairFactory;
import cryptosim.domain.Address;
import cryptosim.domain.Transaction;
import cryptosim.domain.Wallet;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public final class Simulator {

    private final SimulationConfig config;

    // Лічильники метрик
    private long submittedCount = 0;
    private long acceptedCount = 0;
    private long confirmedCount = 0;
    private long totalLatencyMs = 0;
    private long totalMiningTimeMs = 0;
    private long totalNonceAttempts = 0;
    private long totalFeesCollected = 0;


    private final Map<Transaction, Long> submittedAtMs = new HashMap<>();

    public Simulator(SimulationConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config must not be null");
        }
        this.config = config;
    }

    public SimulationResult run() throws InterruptedException {
        Random random = new Random(config.seed());

        List<Wallet> wallets = new ArrayList<>();
        Map<Address, Long> initialBalances = new HashMap<>();
        for (int i = 0; i < config.numWallets(); i++) {
            Wallet w = Wallet.create();
            wallets.add(w);
            initialBalances.put(w.getAddress(), config.initialBalancePerWallet());
        }

        Address minerAddress = Address.fromPublicKey(
                KeyPairFactory.generate().getPublic());

        BigInteger target = Difficulty.targetFromLeadingZeroBits(config.difficultyBits());
        Blockchain blockchain = new Blockchain(initialBalances, target);

        Mempool mempool = new Mempool(config.mempoolMaxSize());
        TransactionGenerator txGenerator = new TransactionGenerator(
                random, wallets, blockchain.worldState());
        Miner miner = new Miner();

        for (long ms = 0; ms < config.durationMs(); ms++) {
            if (ms % config.transactionIntervalMs() == 0) {
                Optional<Transaction> tx = txGenerator.next();
                if (tx.isPresent()) {
                    submittedCount++;
                    if (mempool.add(tx.get())) {
                        acceptedCount++;
                        submittedAtMs.put(tx.get(), ms);
                    }
                }
            }

            if (ms > 0 && ms % config.blockIntervalMs() == 0) {
                mineBlock(blockchain, mempool, miner, minerAddress, ms);
            }

            Thread.sleep(1);
        }

        // Будуємо NetworkStats з лічильникі
        long totalBlocks = blockchain.height();
        double avgMiningTimeMs = totalBlocks > 0 ? (double) totalMiningTimeMs / totalBlocks : 0.0;
        double avgNonceAttempts = totalBlocks > 0 ? (double) totalNonceAttempts / totalBlocks : 0.0;
        double avgLatency = confirmedCount > 0 ? (double) totalLatencyMs / confirmedCount : 0.0;

        NetworkStats stats = new NetworkStats(
                submittedCount, acceptedCount, confirmedCount,
                totalBlocks, config.durationMs(), totalFeesCollected,
                avgMiningTimeMs, avgNonceAttempts, avgLatency
        );

        return new SimulationResult(blockchain, wallets, minerAddress, stats);
    }

    private void mineBlock(Blockchain blockchain, Mempool mempool, Miner miner,
                           Address minerAddress, long currentMs) {
        List<Transaction> txs = mempool.drain(config.maxTransactionsPerBlock());

        Block tip = blockchain.getTip();
        BlockHeader template = new BlockHeader(
                tip.header().index() + 1,
                tip.hash(),
                Block.txRoot(txs),
                currentMs,
                tip.header().target(),
                0,
                minerAddress
        );

        long miningStart = System.currentTimeMillis();
        Block mined = miner.mine(template, txs);
        long miningDuration = System.currentTimeMillis() - miningStart;
        blockchain.addBlock(mined);

        totalMiningTimeMs += miningDuration;
        totalNonceAttempts += mined.header().nonce() + 1;
        for (Transaction tx : txs) {
            Long submitted = submittedAtMs.remove(tx);
            if (submitted != null) {
                totalLatencyMs += currentMs - submitted;
            }
            totalFeesCollected += tx.fee();
        }
        confirmedCount += txs.size();
    }
}
