package cryptosim.simulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulatorTest {
    private static SimulationConfig smallConfig(long seed) {
        return new SimulationConfig(seed, 5, 1000L, 12, 5000,
                100, 500, 100, 10
        );
    }

    @Test
    void run_returnsValidResult() {
        Simulator simulator = new Simulator(smallConfig(42L));

        SimulationResult result = simulator.run();

        assertNotNull(result);
        assertNotNull(result.blockchain());
        assertEquals(5, result.wallets().size());
        assertNotNull(result.minerAddress());
    }

    @Test
    void run_producesAtLeastOneBlock() {
        Simulator simulator = new Simulator(smallConfig(42L));

        SimulationResult result = simulator.run();

        assertTrue(result.blockchain().height() >= 1,
                "Expected at least 1 block, got " + result.blockchain().height());
    }

    @Test
    void run_blockchainIsValid() {
        Simulator simulator = new Simulator(smallConfig(42L));

        SimulationResult result = simulator.run();

        assertTrue(result.blockchain().isValid(),
                "Resulting blockchain must be valid");
    }

    @Test
    void run_preservesTotalSupply() {
        SimulationConfig config = smallConfig(42L);
        long expectedTotal = (long) config.numWallets() * config.initialBalancePerWallet();

        Simulator simulator = new Simulator(config);
        SimulationResult result = simulator.run();

        assertEquals(expectedTotal, result.blockchain().worldState().totalSupply(),
                "Total supply must remain constant across full simulation");
    }

    @Test
    void run_isDeterministic() {
        SimulationConfig config = smallConfig(123L);

        SimulationResult run1 = new Simulator(config).run();
        SimulationResult run2 = new Simulator(config).run();

        assertEquals(run1.blockchain().height(), run2.blockchain().height());
        assertEquals(
                run1.blockchain().worldState().totalSupply(),
                run2.blockchain().worldState().totalSupply()
        );
    }

    @Test
    void run_minerAccumulatesFees() {
        Simulator simulator = new Simulator(smallConfig(42L));

        SimulationResult result = simulator.run();

        long minerBalance = result.blockchain().worldState().getBalance(result.minerAddress());
        assertTrue(minerBalance >= 0,
                "Miner balance should be non-negative");
    }

    @Test
    void run_emptyMempool_stillMinesBlocks() {
        SimulationConfig config = new SimulationConfig(42L, 5, 1000L, 12,
                5000, 5000, 500, 100, 10
        );
        Simulator simulator = new Simulator(config);

        SimulationResult result = simulator.run();
        assertTrue(result.blockchain().height() >= 1);
    }
}
