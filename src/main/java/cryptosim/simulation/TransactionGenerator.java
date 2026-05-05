package cryptosim.simulation;

import cryptosim.domain.Transaction;
import cryptosim.domain.Wallet;
import cryptosim.ledger.WorldState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class TransactionGenerator {
    private static final long MAX_FEE = 5;

    private final Random random;
    private final List<Wallet> wallets;
    private final WorldState worldState;

    public TransactionGenerator(Random random, List<Wallet> wallets, WorldState worldState) {
        if (random == null) {
            throw new IllegalArgumentException("Random must not be null");
        }
        if (wallets == null || wallets.size() < 2) {
            throw new IllegalArgumentException(
                    "Need at least 2 wallets, got " +
                            (wallets == null ? "null" : wallets.size()));
        }
        if (worldState == null) {
            throw new IllegalArgumentException("WorldState must not be null");
        }
        this.random = random;
        this.wallets = List.copyOf(wallets);
        this.worldState = worldState;
    }

    public Optional<Transaction> next() {
        List<Wallet> potentialSenders = new ArrayList<>();
        for (Wallet wallet : wallets) {
            if (worldState.getBalance(wallet.getAddress()) > MAX_FEE) {
                potentialSenders.add(wallet);
            }
        }
        if (potentialSenders.isEmpty()) {
            return Optional.empty();
        }

        Wallet sender = potentialSenders.get(random.nextInt(potentialSenders.size()));

        Wallet receiver;
        do {
            receiver = wallets.get(random.nextInt(wallets.size()));
        } while (receiver.getAddress().equals(sender.getAddress()));

        long senderBalance = worldState.getBalance(sender.getAddress());

        long fee = random.nextLong(MAX_FEE + 1);

        long maxAmount = Math.min(senderBalance - fee, senderBalance / 4);
        if (maxAmount < 1) {
            return Optional.empty();
        }
        long amount = 1 + random.nextLong(maxAmount);

        long nonce = worldState.getNonce(sender.getAddress());

        return Optional.of(sender.createTransaction(receiver.getAddress(), amount, fee, nonce));
    }
}
