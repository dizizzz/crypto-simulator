package cryptosim.simulation.scenarios;

import cryptosim.simulation.NetworkStats;
import cryptosim.simulation.SimulationConfig;
import cryptosim.simulation.Simulator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


//Сценарій 4: затримки підтвердження транзакцій
class ConfirmationLatencyScenarioTest {

    @Test
    void frequentBlocks_resultInLowerLatency() throws InterruptedException {
        // швидкі блоки: 1 блок кожні 20 мс
        SimulationConfig fastConfig = new SimulationConfig(
                42L, 5, 1000L,12, 1000,
                10, 20, 100, 10
        );

        // 1 блок кожні 200 мс
        SimulationConfig slowConfig = new SimulationConfig(
                42L, 5, 1000L, 12, 1000,
                10, 200, 100, 10
        );

        NetworkStats fastStats = new Simulator(fastConfig).run().stats();
        NetworkStats slowStats = new Simulator(slowConfig).run().stats();

        System.out.println("[Scenario: Confirmation Latency vs Block Interval]");
        System.out.println("  Fast config (block every 20 ms):");
        System.out.println("    Confirmed transactions:   " + fastStats.confirmedTransactions());
        System.out.printf ("    Average latency:          %.1f ms%n",
                fastStats.averageConfirmationLatencyMs());
        System.out.println("  Slow config (block every 200 ms):");
        System.out.println("    Confirmed transactions:   " + slowStats.confirmedTransactions());
        System.out.printf ("    Average latency:          %.1f ms%n",
                slowStats.averageConfirmationLatencyMs());

        assertTrue(
                fastStats.averageConfirmationLatencyMs() < slowStats.averageConfirmationLatencyMs(),
                "Frequent blocks should result in lower average confirmation latency");
    }

    @Test
    void averageLatency_isApproximatelyHalfOfBlockInterval() throws InterruptedException {
        SimulationConfig config = new SimulationConfig(
                42L, 5, 1000L, 12,
                1000, 10, 50, 100, 10
        );

        NetworkStats stats = new Simulator(config).run().stats();

        double expectedLatency = config.blockIntervalMs() / 2.0;
        double actualLatency = stats.averageConfirmationLatencyMs();

        System.out.println("[Scenario: Latency Theoretical Match]");
        System.out.println("  Block interval:           " + config.blockIntervalMs() + " ms");
        System.out.printf ("  Expected (interval/2):    %.1f ms%n", expectedLatency);
        System.out.printf ("  Actual:                   %.1f ms%n", actualLatency);

        assertTrue(actualLatency > expectedLatency * 0.5,
                "Actual latency too low: " + actualLatency);
        assertTrue(actualLatency < expectedLatency * 1.5,
                "Actual latency too high: " + actualLatency);
    }
}
