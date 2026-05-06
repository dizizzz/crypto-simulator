package cryptosim.simulation.scenarios;

import cryptosim.chain.Mempool;
import cryptosim.crypto.Signer;
import cryptosim.domain.Address;
import cryptosim.domain.Transaction;
import cryptosim.domain.Wallet;
import cryptosim.ledger.ValidationResult;
import cryptosim.ledger.Validator;
import cryptosim.ledger.WorldState;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

// Сценарій 1: транзакції з невалідним підписом
class InvalidSignatureScenarioTest {
    @Test
    void mempoolRejectsTransactionWithCorruptedSignature() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Mempool mempool = new Mempool(100);

        // Аліса створює валідну транзакцію
        Transaction valid = alice.createTransaction(bob.getAddress(), 10, 1, 0);

        // Зловмисник змінює суму
        Transaction tampered = new Transaction(
                valid.from(), valid.to(),
                999,
                valid.fee(), valid.nonce(),
                valid.senderPublicKey(), valid.signature()
        );

        boolean mempoolAccepted = mempool.add(tampered);

        System.out.println("[Scenario: Tampered Transaction]");
        System.out.println("  Original amount:  " + valid.amount());
        System.out.println("  Tampered amount:  " + tampered.amount());
        System.out.println("  Mempool accepted: " + mempoolAccepted);
        System.out.println("  Mempool size:     " + mempool.size());

        assertFalse(mempoolAccepted, "Mempool must reject tampered transaction");
        assertEquals(0, mempool.size());
    }

    @Test
    void validatorRejectsTransactionWithForgedSignature() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Wallet eve = Wallet.create();

        WorldState state = new WorldState(Map.of(
                alice.getAddress(), 100L,
                bob.getAddress(), 0L
        ));

        // Eve будує транзакцію з адресою Аліси. підписує своїм приватним ключем
        Transaction unsignedShell = new Transaction(
                alice.getAddress(),
                bob.getAddress(),
                50, 1, 0,
                eve.getPublicKey(),    // публічний ключ Eve
                new byte[]{0}
        );
        byte[] eveSignature = Signer.sign(unsignedShell.dataToSign(), getPrivateKeyOf(eve));

        Transaction forged = new Transaction(
                alice.getAddress(), bob.getAddress(),
                50, 1, 0,
                eve.getPublicKey(),
                eveSignature
        );

        ValidationResult result = Validator.validate(forged, state);

        System.out.println("[Scenario: Forged Signature by Eve]");
        System.out.println("  Claimed sender:  " + alice.getAddress().hex().substring(0, 8) + "...");
        System.out.println("  Real signer:     " + Address.fromPublicKey(eve.getPublicKey()).hex().substring(0, 8) + "...");
        System.out.println("  Validation:      " + result);

        assertEquals(ValidationResult.INVALID_SIGNATURE, result);
        assertEquals(100, state.getBalance(alice.getAddress()), "Alice balance must be untouched");
    }

     // отримання приватного ключа з Wallet(рефлексія)
    private static java.security.PrivateKey getPrivateKeyOf(Wallet wallet) {
        try {
            var field = Wallet.class.getDeclaredField("keyPair");
            field.setAccessible(true);
            java.security.KeyPair pair = (java.security.KeyPair) field.get(wallet);
            return pair.getPrivate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to access private key for test", e);
        }
    }
}
