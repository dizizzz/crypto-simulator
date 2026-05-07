package cryptosim.simulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NetworkStatsTest {
    private static NetworkStats sampleStats(
            long submitted, long accepted, long confirmed) {
        return new NetworkStats(
                submitted, accepted, confirmed,10,10000,100,50.0,4096.0,250.0
        );
    }

    @Test
    void validInputs_recordCreated() {
        NetworkStats stats = sampleStats(100, 80, 60);

        assertEquals(100, stats.submittedTransactions());
        assertEquals(80, stats.acceptedTransactions());
        assertEquals(60, stats.confirmedTransactions());
    }

    @Test
    void acceptedExceedsSubmitted_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> sampleStats(50, 100, 50));
    }

    @Test
    void confirmedExceedsAccepted_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> sampleStats(100, 80, 90));
    }

    @Test
    void negativeValues_throw() {
        assertThrows(IllegalArgumentException.class,
                () -> sampleStats(-1, 0, 0));
    }

    @Test
    void mempoolThroughput_correct() {
        NetworkStats stats = sampleStats(100, 80, 60);

        assertEquals(0.75, stats.mempoolThroughput(), 1e-9);
    }

    @Test
    void rejectionRate_correct() {
        NetworkStats stats = sampleStats(100, 80, 60);

        assertEquals(0.20, stats.rejectionRate(), 1e-9);
    }

    @Test
    void mempoolThroughput_emptyAccepted_returnsZero() {
        NetworkStats stats = sampleStats(100, 0, 0);

        assertEquals(0.0, stats.mempoolThroughput(), 1e-9);
    }
}
