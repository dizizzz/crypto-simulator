package cryptosim.simulation.scenarios;

import cryptosim.chain.Mempool;
import cryptosim.domain.Wallet;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Сценарій 3: перевантаження мережі
class MempoolOverflowScenarioTest {

    @Test
    void mempoolFillsExactlyToMaxSize() {
        int mempoolSize = 10;
        int submittedCount = 50;
        Mempool mempool = new Mempool(mempoolSize);
        Wallet receiver = Wallet.create();

        int accepted = 0;
        int rejected = 0;

        for (int i = 0; i < submittedCount; i++) {
            Wallet sender = Wallet.create();
            var tx = sender.createTransaction(receiver.getAddress(), 5, 1, 0);
            if (mempool.add(tx)) {
                accepted++;
            } else {
                rejected++;
            }
        }

        double rejectionRate = (double) rejected / submittedCount;

        System.out.println("[Scenario: Mempool Overflow]");
        System.out.println("  Mempool capacity:    " + mempoolSize);
        System.out.println("  Submitted:           " + submittedCount);
        System.out.println("  Accepted:            " + accepted);
        System.out.println("  Rejected:            " + rejected);
        System.out.printf ("  Rejection rate:      %.1f%%%n", rejectionRate * 100);
        System.out.println("  Final mempool size:  " + mempool.size());

        assertEquals(mempoolSize, mempool.size(),
                "Mempool must fill exactly to maxSize");
        assertEquals(mempoolSize, accepted, "Only first N should be accepted");
        assertEquals(submittedCount - mempoolSize, rejected);
    }

    @Test
    void mempoolRecoveryAfterDrain() {
        int mempoolSize = 5;
        Mempool mempool = new Mempool(mempoolSize);
        Wallet receiver = Wallet.create();

        // Заповнюємо мемпул.
        List<Wallet> firstBatch = new ArrayList<>();
        for (int i = 0; i < mempoolSize; i++) {
            Wallet sender = Wallet.create();
            firstBatch.add(sender);
            mempool.add(sender.createTransaction(receiver.getAddress(), 5, 1, 0));
        }

        assertTrue(mempool.isFull());
        int sizeBeforeDrain = mempool.size();

        int drainCount = 3;
        var drained = mempool.drain(drainCount);

        // Тепер можемо додати нові.
        int addedAfterDrain = 0;
        for (int i = 0; i < drainCount; i++) {
            Wallet sender = Wallet.create();
            if (mempool.add(sender.createTransaction(receiver.getAddress(), 5, 1, 0))) {
                addedAfterDrain++;
            }
        }

        System.out.println("[Scenario: Mempool Recovery After Drain]");
        System.out.println("  Capacity:            " + mempoolSize);
        System.out.println("  Size before drain:   " + sizeBeforeDrain);
        System.out.println("  Drained by miner:    " + drained.size());
        System.out.println("  Added after drain:   " + addedAfterDrain);
        System.out.println("  Final size:          " + mempool.size());

        assertEquals(mempoolSize, sizeBeforeDrain);
        assertEquals(drainCount, drained.size());
        assertEquals(drainCount, addedAfterDrain);
        assertEquals(mempoolSize, mempool.size(),
                "Mempool returned to full capacity");
    }
}
