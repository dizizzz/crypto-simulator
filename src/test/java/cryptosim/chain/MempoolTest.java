package cryptosim.chain;

import cryptosim.domain.Transaction;
import cryptosim.domain.Wallet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MempoolTest {

    @Test
    void add_validTransaction_returnsTrue() {
        Mempool mempool = new Mempool(100);
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Transaction tx = alice.createTransaction(bob.getAddress(), 5, 1, 0);

        boolean added = mempool.add(tx);

        assertTrue(added);
        assertEquals(1, mempool.size());
    }

    @Test
    void add_invalidSignature_returnsFalse() {
        Mempool mempool = new Mempool(100);
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Transaction valid = alice.createTransaction(bob.getAddress(), 5, 1, 0);

        // неправильний підпис
        Transaction tampered = new Transaction(
                valid.from(), valid.to(), 999,
                valid.fee(), valid.nonce(),
                valid.senderPublicKey(), valid.signature()
        );

        boolean added = mempool.add(tampered);

        assertFalse(added);
        assertEquals(0, mempool.size());
    }

    @Test
    void add_duplicate_returnsFalse() {
        Mempool mempool = new Mempool(100);
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Transaction tx = alice.createTransaction(bob.getAddress(), 5, 1, 0);

        assertTrue(mempool.add(tx));
        assertFalse(mempool.add(tx));
        assertEquals(1, mempool.size());
    }

    @Test
    void add_whenFull_returnsFalse() {
        Mempool mempool = new Mempool(2);
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Wallet alice2 = Wallet.create();
        Wallet alice3 = Wallet.create();

        Transaction tx1 = alice.createTransaction(bob.getAddress(), 5, 1, 0);
        Transaction tx2 = alice2.createTransaction(bob.getAddress(), 5, 1, 1);
        Transaction tx3 = alice3.createTransaction(bob.getAddress(), 5, 1, 2);

        assertTrue(mempool.add(tx1));
        assertTrue(mempool.add(tx2));
        assertTrue(mempool.isFull());
        assertFalse(mempool.add(tx3));
        assertEquals(2, mempool.size());
    }

    @Test
    void add_null_returnsFalse() {
        Mempool mempool = new Mempool(100);

        assertFalse(mempool.add(null));
        assertEquals(0, mempool.size());
    }

    @Test
    void drain_emptyPool_returnsEmptyList() {
        Mempool mempool = new Mempool(100);

        List<Transaction> drained = mempool.drain(10);

        assertEquals(0, drained.size());
    }

    @Test
    void drain_returnsHighestFeeFirst() {
        Mempool mempool = new Mempool(100);
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Wallet alice2 = Wallet.create();
        Wallet alice3 = Wallet.create();

        Transaction lowFee = alice.createTransaction(bob.getAddress(), 5, 1, 0);
        Transaction midFee = alice2.createTransaction(bob.getAddress(), 5, 5, 1);
        Transaction highFee = alice3.createTransaction(bob.getAddress(), 5, 10, 2);

        mempool.add(lowFee);
        mempool.add(highFee);
        mempool.add(midFee);

        List<Transaction> drained = mempool.drain(3);

        assertEquals(highFee, drained.get(0));
        assertEquals(midFee, drained.get(1));
        assertEquals(lowFee, drained.get(2));
    }

    @Test
    void drain_removesDrainedTransactions() {
        Mempool mempool = new Mempool(100);
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Wallet alice2 = Wallet.create();
        Wallet alice3 = Wallet.create();

        Transaction tx1 = alice.createTransaction(bob.getAddress(), 5, 1, 0);
        Transaction tx2 = alice2.createTransaction(bob.getAddress(), 5, 2, 1);
        Transaction tx3 = alice3.createTransaction(bob.getAddress(), 5, 3, 2);

        mempool.add(tx1);
        mempool.add(tx2);
        mempool.add(tx3);

        mempool.drain(2);

        assertEquals(1, mempool.size());
    }

    @Test
    void drain_moreThanAvailable_returnsAll() {
        Mempool mempool = new Mempool(100);
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Wallet alice2 = Wallet.create();

        mempool.add(alice.createTransaction(bob.getAddress(), 5, 1, 0));
        mempool.add(alice2.createTransaction(bob.getAddress(), 5, 2, 1));

        List<Transaction> drained = mempool.drain(100);

        assertEquals(2, drained.size());
        assertEquals(0, mempool.size());
    }

    @Test
    void constructor_invalidMaxSize_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new Mempool(0));
        assertThrows(IllegalArgumentException.class, () -> new Mempool(-1));
    }

    @Test
    void add_secondTransactionFromSameSender_returnsFalse() {
        Mempool mempool = new Mempool(100);
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();

        Transaction tx1 = alice.createTransaction(bob.getAddress(), 5, 1, 0);
        Transaction tx2 = alice.createTransaction(bob.getAddress(), 10, 1, 1);

        assertTrue(mempool.add(tx1));
        assertFalse(mempool.add(tx2),
                "Second tx from same sender should be rejected");
        assertEquals(1, mempool.size());
    }
}
