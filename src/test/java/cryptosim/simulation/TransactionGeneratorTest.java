package cryptosim.simulation;

import cryptosim.domain.Transaction;
import cryptosim.domain.Wallet;
import cryptosim.ledger.WorldState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionGeneratorTest {
    private static record Setup(List<Wallet> wallets, WorldState worldState) {}

    private static Setup setup(int numWallets, long balancePerWallet) {
        List<Wallet> wallets = new ArrayList<>();
        Map<cryptosim.domain.Address, Long> balances = new HashMap<>();
        for (int i = 0; i < numWallets; i++) {
            Wallet w = Wallet.create();
            wallets.add(w);
            balances.put(w.getAddress(), balancePerWallet);
        }
        return new Setup(wallets, new WorldState(balances));
    }

    @Test
    void next_returnsValidTransaction() {
        Setup s = setup(5, 1000);
        TransactionGenerator gen = new TransactionGenerator(new Random(42), s.wallets(), s.worldState());

        Optional<Transaction> result = gen.next();

        assertTrue(result.isPresent());
        assertTrue(result.get().verifySignature());
    }

    @Test
    void next_senderDifferentFromReceiver() {
        Setup s = setup(5, 1000);
        TransactionGenerator gen = new TransactionGenerator(new Random(42), s.wallets(), s.worldState());

        for (int i = 0; i < 50; i++) {
            Optional<Transaction> tx = gen.next();
            assertTrue(tx.isPresent());
            assertNotEquals(tx.get().from(), tx.get().to());
        }
    }

    @Test
    void next_amountWithinSenderBalance() {
        Setup s = setup(3, 1000);
        TransactionGenerator gen = new TransactionGenerator(new Random(42), s.wallets(), s.worldState());

        for (int i = 0; i < 50; i++) {
            Optional<Transaction> tx = gen.next();
            assertTrue(tx.isPresent());

            long senderBalance = s.worldState().getBalance(tx.get().from());
            assertTrue(tx.get().amount() + tx.get().fee() <= senderBalance,
                    "Amount + fee must fit in sender balance");
            assertTrue(tx.get().amount() >= 1, "Amount must be at least 1");
        }
    }

    @Test
    void next_feeWithinLimits() {
        Setup s = setup(3, 1000);
        TransactionGenerator gen = new TransactionGenerator(new Random(42), s.wallets(), s.worldState());

        for (int i = 0; i < 50; i++) {
            Optional<Transaction> tx = gen.next();
            assertTrue(tx.isPresent());
            assertTrue(tx.get().fee() >= 0);
            assertTrue(tx.get().fee() <= 5);
        }
    }

    @Test
    void next_nonceMatchesWorldState() {
        Setup s = setup(3, 1000);
        TransactionGenerator gen = new TransactionGenerator(new Random(42), s.wallets(), s.worldState());

        Optional<Transaction> tx = gen.next();
        assertTrue(tx.isPresent());

        long expectedNonce = s.worldState().getNonce(tx.get().from());
        assertEquals(expectedNonce, tx.get().nonce());
    }

    @Test
    void next_emptyWhenAllBalancesAreLow() {
        Setup s = setup(3, 5);
        TransactionGenerator gen = new TransactionGenerator(new Random(42), s.wallets(), s.worldState());

        Optional<Transaction> tx = gen.next();

        assertFalse(tx.isPresent());
    }

    @Test
    void next_isDeterministic_withSameSeed() {
        Setup s = setup(5, 1000);

        TransactionGenerator gen1 = new TransactionGenerator(new Random(42), s.wallets(), s.worldState());
        TransactionGenerator gen2 = new TransactionGenerator(new Random(42), s.wallets(), s.worldState());

        for (int i = 0; i < 20; i++) {
            Optional<Transaction> tx1 = gen1.next();
            Optional<Transaction> tx2 = gen2.next();

            assertEquals(tx1.isPresent(), tx2.isPresent());
            if (tx1.isPresent()) {
                assertEquals(tx1.get().from(), tx2.get().from());
                assertEquals(tx1.get().to(), tx2.get().to());
                assertEquals(tx1.get().amount(), tx2.get().amount());
                assertEquals(tx1.get().fee(), tx2.get().fee());
                assertEquals(tx1.get().nonce(), tx2.get().nonce());
            }
        }
    }

    @Test
    void constructor_validation() {
        Setup s = setup(3, 1000);

        assertThrows(IllegalArgumentException.class,
                () -> new TransactionGenerator(null, s.wallets(), s.worldState()));
        assertThrows(IllegalArgumentException.class,
                () -> new TransactionGenerator(new Random(), null, s.worldState()));
        assertThrows(IllegalArgumentException.class,
                () -> new TransactionGenerator(new Random(), s.wallets(), null));
        assertThrows(IllegalArgumentException.class,
                () -> new TransactionGenerator(new Random(), List.of(), s.worldState()));
        assertThrows(IllegalArgumentException.class,
                () -> new TransactionGenerator(new Random(), List.of(Wallet.create()), s.worldState()));
    }
}
