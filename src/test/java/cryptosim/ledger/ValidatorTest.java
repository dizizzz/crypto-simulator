package cryptosim.ledger;

import cryptosim.domain.Transaction;
import cryptosim.domain.Wallet;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ValidatorTest {

    private static WorldState stateWithBalances(Wallet alice, long aliceBalance,
                                                Wallet bob, long bobBalance) {
        return new WorldState(Map.of(
                alice.getAddress(), aliceBalance,
                bob.getAddress(), bobBalance
        ));
    }

    @Test
    void validate_validTransaction_returnsOk() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        WorldState state = stateWithBalances(alice, 100, bob, 0);

        Transaction tx = alice.createTransaction(bob.getAddress(), 30, 0, 0);

        assertEquals(ValidationResult.OK, Validator.validate(tx, state));
    }

    @Test
    void validate_amountZero_returnsInvalidAmount() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        WorldState state = stateWithBalances(alice, 100, bob, 0);

        Transaction tx = alice.createTransaction(bob.getAddress(), 0, 0, 0);

        assertEquals(ValidationResult.INVALID_AMOUNT, Validator.validate(tx, state));
    }

    @Test
    void validate_badNonce_returnsBadNonce() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        WorldState state = stateWithBalances(alice, 100, bob, 0);

        Transaction tx = alice.createTransaction(bob.getAddress(), 30, 0, 5);

        assertEquals(ValidationResult.BAD_NONCE, Validator.validate(tx, state));
    }

    @Test
    void validate_insufficientBalance_returnsInsufficientBalance() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        WorldState state = stateWithBalances(alice, 10, bob, 0);

        Transaction tx = alice.createTransaction(bob.getAddress(), 100, 0, 0);

        assertEquals(ValidationResult.INSUFFICIENT_BALANCE, Validator.validate(tx, state));
    }

    @Test
    void validate_insufficientForFee_returnsInsufficientBalance() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        WorldState state = stateWithBalances(alice, 100, bob, 0);

        Transaction tx = alice.createTransaction(bob.getAddress(), 100, 1, 0);

        assertEquals(ValidationResult.INSUFFICIENT_BALANCE, Validator.validate(tx, state));
    }

    @Test
    void validate_invalidSignature_returnsInvalidSignature() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        WorldState state = stateWithBalances(alice, 100, bob, 0);

        Transaction valid = alice.createTransaction(bob.getAddress(), 30, 0, 0);
        Transaction tampered = new Transaction(
                valid.from(), valid.to(), 50,
                valid.fee(), valid.nonce(),
                valid.senderPublicKey(), valid.signature()
        );

        assertEquals(ValidationResult.INVALID_SIGNATURE, Validator.validate(tampered, state));
    }

    @Test
    void validate_doesNotMutateState() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        WorldState state = stateWithBalances(alice, 100, bob, 0);

        Transaction tx = alice.createTransaction(bob.getAddress(), 30, 0, 0);

        Validator.validate(tx, state);
        Validator.validate(tx, state);
        Validator.validate(tx, state);

        assertEquals(100, state.getBalance(alice.getAddress()));
        assertEquals(0, state.getBalance(bob.getAddress()));
        assertEquals(0, state.getNonce(alice.getAddress()));
    }

    @Test
    void apply_validTransaction_updatesBalancesAndNonce() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        WorldState state = stateWithBalances(alice, 100, bob, 0);

        Transaction tx = alice.createTransaction(bob.getAddress(), 30, 0, 0);

        ValidationResult result = Validator.apply(tx, state);

        assertEquals(ValidationResult.OK, result);
        assertEquals(70, state.getBalance(alice.getAddress()));
        assertEquals(30, state.getBalance(bob.getAddress()));
        assertEquals(1, state.getNonce(alice.getAddress()));
    }

    @Test
    void apply_invalidTransaction_doesNotChangeState() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        WorldState state = stateWithBalances(alice, 10, bob, 0);

        Transaction tx = alice.createTransaction(bob.getAddress(), 100, 0, 0);
        ValidationResult result = Validator.apply(tx, state);

        assertNotEquals(ValidationResult.OK, result);
        assertEquals(10, state.getBalance(alice.getAddress()));
        assertEquals(0, state.getBalance(bob.getAddress()));
        assertEquals(0, state.getNonce(alice.getAddress()));
    }

    @Test
    void apply_preservesTotalSupply_whenFeeIsZero() {
        // sum B = const
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Wallet charlie = Wallet.create();

        WorldState state = new WorldState(Map.of(
                alice.getAddress(), 100L,
                bob.getAddress(), 50L,
                charlie.getAddress(), 30L
        ));
        long initialTotal = state.totalSupply();

        Validator.apply(alice.createTransaction(bob.getAddress(), 30, 0, 0), state);
        Validator.apply(bob.createTransaction(charlie.getAddress(), 20, 0, 0), state);
        Validator.apply(charlie.createTransaction(alice.getAddress(), 10, 0, 0), state);
        Validator.apply(alice.createTransaction(bob.getAddress(), 5, 0, 1), state);

        assertEquals(initialTotal, state.totalSupply());
        assertEquals(180, state.totalSupply());
    }

    @Test
    void apply_doubleSpendSameNonce_secondFails() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        WorldState state = stateWithBalances(alice, 100, bob, 0);

        Transaction tx = alice.createTransaction(bob.getAddress(), 30, 0, 0);

        ValidationResult firstResult = Validator.apply(tx, state);
        ValidationResult secondResult = Validator.apply(tx, state);

        assertEquals(ValidationResult.OK, firstResult);
        assertEquals(ValidationResult.BAD_NONCE, secondResult);

        assertEquals(70, state.getBalance(alice.getAddress()));
        assertEquals(30, state.getBalance(bob.getAddress()));
        assertEquals(1, state.getNonce(alice.getAddress()));
    }
}
