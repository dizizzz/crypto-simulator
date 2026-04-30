package cryptosim.ledger;

import cryptosim.crypto.KeyPairFactory;
import cryptosim.domain.Address;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorldStateTest {
    private static Address randomAddress() {
        return Address.fromPublicKey(KeyPairFactory.generate().getPublic());
    }

    @Test
    void getAccount_unknownAddress_returnsEmpty() {
        WorldState state = new WorldState(Map.of());

        Account account = state.getAccount(randomAddress());

        assertEquals(Account.empty(), account);
        assertEquals(0, account.balance());
        assertEquals(0, account.nonce());
    }

    @Test
    void getBalance_returnsInitialBalance() {
        Address alice = randomAddress();
        Map<Address, Long> initial = Map.of(alice, 100L);

        WorldState state = new WorldState(initial);

        assertEquals(100, state.getBalance(alice));
        assertEquals(0, state.getNonce(alice));
    }

    @Test
    void totalSupply_returnsSumOfAllBalances() {
        // Σ B(wᵢ) з формальної моделі.
        Address alice = randomAddress();
        Address bob = randomAddress();
        Address jack = randomAddress();

        WorldState state = new WorldState(Map.of(
                alice, 100L, bob, 50L, jack, 30L
        ));

        assertEquals(180, state.totalSupply());
    }

    @Test
    void debit_decreasesBalance_keepsNonce() {
        Address alice = randomAddress();
        WorldState state = new WorldState(Map.of(alice, 100L));

        state.debit(alice, 30);

        assertEquals(70, state.getBalance(alice));
        assertEquals(0, state.getNonce(alice));
    }

    @Test
    void credit_increasesBalance_keepsNonce() {
        Address alice = randomAddress();
        WorldState state = new WorldState(Map.of(alice, 50L));

        state.credit(alice, 30);

        assertEquals(80, state.getBalance(alice));
        assertEquals(0, state.getNonce(alice));
    }

    @Test
    void incrementNonce_increases_keepsBalance() {
        Address alice = randomAddress();
        WorldState state = new WorldState(Map.of(alice, 100L));

        state.incrementNonce(alice);
        state.incrementNonce(alice);

        assertEquals(100, state.getBalance(alice));
        assertEquals(2, state.getNonce(alice));
    }

    @Test
    void debitAndCredit_preservesTotalSupply() {
        // sum B = const
        Address alice = randomAddress();
        Address bob = randomAddress();

        WorldState state = new WorldState(Map.of(
                alice, 100L,
                bob, 50L
        ));
        long initialTotal = state.totalSupply();

        // Симулюємо переказ Аліса → Боб 30
        state.debit(alice, 30);
        state.credit(bob, 30);

        assertEquals(initialTotal, state.totalSupply());
        assertEquals(150, state.totalSupply());
    }

    @Test
    void snapshot_isUnmodifiable() {
        Address alice = randomAddress();
        WorldState state = new WorldState(Map.of(alice, 100L));

        Map<Address, Account> snapshot = state.snapshot();

        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.put(alice, new Account(0, 0)));
    }

    @Test
    void constructor_negativeBalance_throwsException() {
        Address alice = randomAddress();

        Map<Address, Long> initial = new LinkedHashMap<>();
        initial.put(alice, -1L);

        assertThrows(IllegalArgumentException.class,
                () -> new WorldState(initial));
    }
}
