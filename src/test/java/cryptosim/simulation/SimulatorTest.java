package cryptosim.simulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulatorTest {
    private static SimulationConfig smallConfig(long seed) {
        return new SimulationConfig(seed, 5, 1000L, 12, 500,
                100, 50, 100, 10
        );
    }

    @Test
    void run_returnsValidResult() throws InterruptedException {
        Simulator simulator = new Simulator(smallConfig(42L));

        SimulationResult result = simulator.run();

        assertNotNull(result);
        assertNotNull(result.blockchain());
        assertEquals(5, result.wallets().size());
        assertNotNull(result.minerAddress());
    }

    @Test
    void run_producesAtLeastOneBlock() throws InterruptedException {
        Simulator simulator = new Simulator(smallConfig(42L));

        SimulationResult result = simulator.run();

        assertTrue(result.blockchain().height() >= 1,
                "Expected at least 1 block, got " + result.blockchain().height());
    }

    @Test
    void run_blockchainIsValid() throws InterruptedException {
        Simulator simulator = new Simulator(smallConfig(42L));

        SimulationResult result = simulator.run();

        assertTrue(result.blockchain().isValid(),
                "Resulting blockchain must be valid");
    }

    @Test
    void run_preservesTotalSupply() throws InterruptedException {
        SimulationConfig config = smallConfig(42L);
        long expectedTotal = (long) config.numWallets() * config.initialBalancePerWallet();

        Simulator simulator = new Simulator(config);
        SimulationResult result = simulator.run();

        assertEquals(expectedTotal, result.blockchain().worldState().totalSupply(),
                "Total supply must remain constant across full simulation");
    }

    @Test
    void run_minerAccumulatesFees() throws InterruptedException {
        Simulator simulator = new Simulator(smallConfig(42L));

        SimulationResult result = simulator.run();

        long minerBalance = result.blockchain().worldState().getBalance(result.minerAddress());
        assertTrue(minerBalance >= 0,
                "Miner balance should be non-negative");
    }

    @Test
    void run_emptyMempool_stillMinesBlocks() throws InterruptedException {
        SimulationConfig config = new SimulationConfig(42L, 5, 1000L, 12,
                500, 5000, 50, 100, 10
        );
        Simulator simulator = new Simulator(config);

        SimulationResult result = simulator.run();
        assertTrue(result.blockchain().height() >= 1);
    }

    @Test
    void run_statsHasConsistentCounts() throws InterruptedException {
        Simulator simulator = new Simulator(smallConfig(42L));

        SimulationResult result = simulator.run();
        NetworkStats stats = result.stats();

        assertTrue(stats.submittedTransactions() >= stats.acceptedTransactions());
        assertTrue(stats.acceptedTransactions() >= stats.confirmedTransactions());
    }

    @Test
    void run_statsAggregatesAreSensible() throws InterruptedException {
        Simulator simulator = new Simulator(smallConfig(42L));

        SimulationResult result = simulator.run();
        NetworkStats stats = result.stats();

        assertTrue(stats.totalBlocks() > 0, "Expected at least one mined block");
        assertTrue(stats.durationMs() > 0);
        assertTrue(stats.averageBlockMiningTimeMs() >= 0);
        assertTrue(stats.averageNonceAttempts() > 0, "Each block requires at least one attempt");
        assertTrue(stats.averageConfirmationLatencyMs() >= 0);
        assertTrue(stats.totalFeesPaid() >= 0);
    }
}
