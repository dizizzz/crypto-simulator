package cryptosim.domain;

import cryptosim.crypto.KeyPairFactory;
import cryptosim.crypto.Signer;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {
    private static Transaction validTxAliceToBob(KeyPair alice, Address bobAddress,
                                                 long amount, long fee, long nonce) {
        Address aliceAddress = Address.fromPublicKey(alice.getPublic());

        // тільки дані(без підпису)
        Transaction unsigned = new Transaction(
                aliceAddress, bobAddress, amount, fee, nonce,
                alice.getPublic(), new byte[]{0}
        );
        byte[] signature = Signer.sign(unsigned.dataToSign(), alice.getPrivate());

        return new Transaction(
                aliceAddress, bobAddress, amount, fee, nonce,
                alice.getPublic(), signature
        );
    }

    @Test
    void verifySignature_validTransaction_returnsTrue() {
        KeyPair alice = KeyPairFactory.generate();
        KeyPair bob = KeyPairFactory.generate();
        Address bobAddress = Address.fromPublicKey(bob.getPublic());

        Transaction tx = validTxAliceToBob(alice, bobAddress, 5, 1, 1);

        assertTrue(tx.verifySignature());
    }

    @Test
    void verifySignature_tamperedAmount_returnsFalse() {
        KeyPair alice = KeyPairFactory.generate();
        KeyPair bob = KeyPairFactory.generate();
        Address bobAddress = Address.fromPublicKey(bob.getPublic());

        Transaction original = validTxAliceToBob(alice, bobAddress, 5, 1, 1);

        //нова транзакція(інша сума) зі старим підписом
        Transaction tampered = new Transaction(
                original.from(), original.to(), 9999, original.fee(), original.nonce(),
                original.senderPublicKey(), original.signature()
        );

        assertFalse(tampered.verifySignature());
    }

    @Test
    void verifySignature_tamperedRecipient_returnsFalse() {
        KeyPair alice = KeyPairFactory.generate();
        KeyPair bob = KeyPairFactory.generate();
        KeyPair eve = KeyPairFactory.generate();
        Address bobAddress = Address.fromPublicKey(bob.getPublic());
        Address eveAddress = Address.fromPublicKey(eve.getPublic());

        Transaction original = validTxAliceToBob(alice, bobAddress, 5, 1, 1);

        Transaction tampered = new Transaction(
                original.from(), eveAddress, original.amount(),
                original.fee(), original.nonce(),
                original.senderPublicKey(), original.signature()
        );

        assertFalse(tampered.verifySignature());
    }

    @Test
    void verifySignature_addressFromKeyMismatch_returnsFalse() {
        // атака чужі ключі
        KeyPair alice = KeyPairFactory.generate();
        KeyPair bob = KeyPairFactory.generate();
        KeyPair eve = KeyPairFactory.generate();

        Address aliceAddress = Address.fromPublicKey(alice.getPublic());
        Address bobAddress = Address.fromPublicKey(bob.getPublic());

        Transaction unsignedShell = new Transaction(
                aliceAddress, bobAddress, 5, 1, 1,
                eve.getPublic(), new byte[]{0}
        );
        byte[] eveSignature = Signer.sign(unsignedShell.dataToSign(), eve.getPrivate());

        Transaction forged = new Transaction(
                aliceAddress, bobAddress, 5, 1, 1,
                eve.getPublic(), eveSignature
        );

        assertFalse(forged.verifySignature());
    }

    @Test
    void dataToSign_isDeterministic() {
        KeyPair alice = KeyPairFactory.generate();
        KeyPair bob = KeyPairFactory.generate();
        Address aliceAddress = Address.fromPublicKey(alice.getPublic());
        Address bobAddress = Address.fromPublicKey(bob.getPublic());

        Transaction tx1 = new Transaction(
                aliceAddress, bobAddress, 5, 1, 1,
                alice.getPublic(), new byte[]{0}
        );
        Transaction tx2 = new Transaction(
                aliceAddress, bobAddress, 5, 1, 1,
                alice.getPublic(), new byte[]{0}
        );

        assertArrayEquals(tx1.dataToSign(), tx2.dataToSign());
    }

    @Test
    void constructor_negativeAmount_throwsException() {
        KeyPair alice = KeyPairFactory.generate();
        KeyPair bob = KeyPairFactory.generate();
        Address aliceAddress = Address.fromPublicKey(alice.getPublic());
        Address bobAddress = Address.fromPublicKey(bob.getPublic());

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(aliceAddress, bobAddress, -1, 1, 1,
                        alice.getPublic(), new byte[]{0})
        );
    }

    @Test
    void constructor_negativeFee_throwsException() {
        KeyPair alice = KeyPairFactory.generate();
        KeyPair bob = KeyPairFactory.generate();
        Address aliceAddress = Address.fromPublicKey(alice.getPublic());
        Address bobAddress = Address.fromPublicKey(bob.getPublic());

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(aliceAddress, bobAddress, 5, -1, 1,
                        alice.getPublic(), new byte[]{0})
        );
    }

    @Test
    void constructor_nullField_throwsException() {
        KeyPair alice = KeyPairFactory.generate();
        Address aliceAddress = Address.fromPublicKey(alice.getPublic());

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(aliceAddress, null, 5, 1, 1,
                        alice.getPublic(), new byte[]{0})
        );
    }
}
