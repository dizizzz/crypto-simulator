package cryptosim.simulation.scenarios;

import cryptosim.simulation.NetworkStats;
import cryptosim.simulation.SimulationConfig;
import cryptosim.simulation.Simulator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


//Сценарій 4: затримки підтвердження транзакцій
class ConfirmationLatencyScenarioTest {

    @Test
    void frequentBlocks_resultInLowerLatency() {
        // швидкі блоки: 1 блок кожні 200 тіків
        SimulationConfig fastConfig = new SimulationConfig(
                42L, 5, 1000L,12, 10000,
                100, 200, 100, 10
        );

        // 1 блок кожні 2000 тіків
        SimulationConfig slowConfig = new SimulationConfig(
                42L, 5, 1000L, 12, 10000,
                100, 2000, 100, 10
        );

        NetworkStats fastStats = new Simulator(fastConfig).run().stats();
        NetworkStats slowStats = new Simulator(slowConfig).run().stats();

        System.out.println("[Scenario: Confirmation Latency vs Block Interval]");
        System.out.println("  Fast config (block every 200 ticks):");
        System.out.println("    Confirmed transactions:   " + fastStats.confirmedTransactions());
        System.out.printf ("    Average latency:          %.1f ticks%n",
                fastStats.averageConfirmationLatencyTicks());
        System.out.println("  Slow config (block every 2000 ticks):");
        System.out.println("    Confirmed transactions:   " + slowStats.confirmedTransactions());
        System.out.printf ("    Average latency:          %.1f ticks%n",
                slowStats.averageConfirmationLatencyTicks());

        assertTrue(
                fastStats.averageConfirmationLatencyTicks() < slowStats.averageConfirmationLatencyTicks(),
                "Frequent blocks should result in lower average confirmation latency");
    }

    @Test
    void averageLatency_isApproximatelyHalfOfBlockInterval() {
        SimulationConfig config = new SimulationConfig(
                42L, 5, 1000L, 12,
                10000, 100, 500, 100, 10
        );

        NetworkStats stats = new Simulator(config).run().stats();

        double expectedLatency = config.ticksPerBlock() / 2.0;
        double actualLatency = stats.averageConfirmationLatencyTicks();

        System.out.println("[Scenario: Latency Theoretical Match]");
        System.out.println("  Block interval:           " + config.ticksPerBlock() + " ticks");
        System.out.printf ("  Expected (interval/2):    %.1f ticks%n", expectedLatency);
        System.out.printf ("  Actual:                   %.1f ticks%n", actualLatency);

        assertTrue(actualLatency > expectedLatency * 0.5,
                "Actual latency too low: " + actualLatency);
        assertTrue(actualLatency < expectedLatency * 1.5,
                "Actual latency too high: " + actualLatency);
    }
}
