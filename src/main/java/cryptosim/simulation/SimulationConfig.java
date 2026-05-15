package cryptosim.simulation;

public record SimulationConfig(
        long seed, int numWallets, long initialBalancePerWallet, int difficultyBits,
        int durationMs, int transactionIntervalMs, int blockIntervalMs, int mempoolMaxSize, int maxTransactionsPerBlock
) {

    public SimulationConfig {
        if (numWallets < 2) {
            throw new IllegalArgumentException(
                    "Need at least 2 wallets, got " + numWallets);
        }
        if (initialBalancePerWallet < 0) {
            throw new IllegalArgumentException("Initial balance must be non-negative");
        }
        if (difficultyBits < 0 || difficultyBits > 256) {
            throw new IllegalArgumentException(
                    "Difficulty bits must be in [0, 256], got " + difficultyBits);
        }
        if (durationMs <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        if (transactionIntervalMs <= 0) {
            throw new IllegalArgumentException("Transaction interval must be positive");
        }
        if (blockIntervalMs <= 0) {
            throw new IllegalArgumentException("Block interval must be positive");
        }
        if (mempoolMaxSize <= 0) {
            throw new IllegalArgumentException("Mempool max size must be positive");
        }
        if (maxTransactionsPerBlock <= 0) {
            throw new IllegalArgumentException("Max transactions per block must be positive");
        }
    }

    public static SimulationConfig defaults() {
        return new SimulationConfig(
                42L,
                10,
                1000L,
                12,
                10000,
                100,
                1000,
                100,
                20
        );
    }
}
