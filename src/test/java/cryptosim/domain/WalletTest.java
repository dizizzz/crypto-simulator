package cryptosim.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WalletTest {

    @Test
    void create_returnsWalletWithValidAddress() {
        Wallet wallet = Wallet.create();

        assertNotNull(wallet);
        assertNotNull(wallet.getAddress());
        assertEquals(40, wallet.getAddress().hex().length());
        assertNotNull(wallet.getPublicKey());
    }

    @Test
    void create_twoWallets_haveDifferentAddresses() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();

        assertNotEquals(alice.getAddress(), bob.getAddress());
    }

    @Test
    void createTransaction_returnsValidSignedTransaction() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();

        Transaction tx = alice.createTransaction(bob.getAddress(), 5, 1, 1);

        assertTrue(tx.verifySignature());
    }

    @Test
    void createTransaction_setsCorrectFromAddress() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();

        Transaction tx = alice.createTransaction(bob.getAddress(), 5, 1, 1);

        assertEquals(alice.getAddress(), tx.from());
        assertEquals(bob.getAddress(), tx.to());
    }

    @Test
    void createTransaction_amountAndFeePreserved() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();

        Transaction tx = alice.createTransaction(bob.getAddress(), 100, 5, 42);

        assertEquals(100, tx.amount());
        assertEquals(5, tx.fee());
        assertEquals(42, tx.nonce());
    }

    @Test
    void noPublicGetPrivateKey() {
        boolean hasGetPrivateKey = Arrays.stream(Wallet.class.getMethods())
                .anyMatch(method -> method.getName().toLowerCase().contains("private")
                        && method.getName().toLowerCase().contains("key"));

        assertFalse(hasGetPrivateKey,
                "Wallet must not expose private key through public methods");
    }
}
