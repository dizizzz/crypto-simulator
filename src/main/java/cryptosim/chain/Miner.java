package cryptosim.chain;

import cryptosim.domain.Transaction;

import java.math.BigInteger;
import java.util.List;

public final class Miner {
    public static final long DEFAULT_MAX_ATTEMPTS = 10_000_000L;

    private final long maxAttempts;

    public Miner() {
        this(DEFAULT_MAX_ATTEMPTS);
    }

    public Miner(long maxAttempts) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException(
                    "Max attempts must be positive, got " + maxAttempts);
        }
        this.maxAttempts = maxAttempts;
    }

    public Block mine(BlockHeader template, List<Transaction> transactions) {
        if (template == null) {
            throw new IllegalArgumentException("Template header must not be null");
        }
        if (transactions == null) {
            throw new IllegalArgumentException("Transactions list must not be null");
        }

        BigInteger target = template.target();

        for (long nonce = 0; nonce < maxAttempts; nonce++) {
            BlockHeader candidate = template.withNoncee(nonce);
            BigInteger hashAsNumber = new BigInteger(candidate.hash(), 16);

            if (hashAsNumber.compareTo(target) < 0) {
                return new Block(candidate, transactions);
            }
        }

        throw new MiningTimeoutException(maxAttempts);
    }
}
