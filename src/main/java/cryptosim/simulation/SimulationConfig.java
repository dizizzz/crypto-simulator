package cryptosim.simulation;

public record SimulationConfig(
        long seed, int numWallets, long initialBalancePerWallet, int difficultyBits,
        int totalTicks, int ticksPerTransaction, int ticksPerBlock, int mempoolMaxSize, int maxTransactionsPerBlock
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
        if (totalTicks <= 0) {
            throw new IllegalArgumentException("Total ticks must be positive");
        }
        if (ticksPerTransaction <= 0) {
            throw new IllegalArgumentException("Ticks per transaction must be positive");
        }
        if (ticksPerBlock <= 0) {
            throw new IllegalArgumentException("Ticks per block must be positive");
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
