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

    public Simulator(SimulationConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config must not be null");
        }
        this.config = config;
    }

    public SimulationResult run() {
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

        for (long tick = 0; tick < config.totalTicks(); tick++) {
            if (tick % config.ticksPerTransaction() == 0) {
                Optional<Transaction> tx = txGenerator.next();
                tx.ifPresent(mempool::add);
            }

            if (tick > 0 && tick % config.ticksPerBlock() == 0) {
                mineBlock(blockchain, mempool, miner, minerAddress, tick);
            }
        }

        return new SimulationResult(blockchain, wallets, minerAddress);
    }

    private void mineBlock(Blockchain blockchain, Mempool mempool, Miner miner,
                           Address minerAddress, long tick) {
        List<Transaction> txs = mempool.drain(config.maxTransactionsPerBlock());

        Block tip = blockchain.getTip();
        BlockHeader template = new BlockHeader(
                tip.header().index() + 1,
                tip.hash(),
                Block.txRoot(txs),
                tick,
                tip.header().target(),
                0,
                minerAddress
        );

        Block mined = miner.mine(template, txs);
        blockchain.addBlock(mined);
    }
}
