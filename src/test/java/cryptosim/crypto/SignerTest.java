package cryptosim.crypto;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignerTest {

    private static final byte[] DATA =
            "transaction: alice -> bob, 5".getBytes(StandardCharsets.UTF_8);

    @Test
    void sign_thenVerify_returnsTrue() {
        KeyPair pair = KeyPairFactory.generate();

        byte[] signature = Signer.sign(DATA, pair.getPrivate());

        assertTrue(Signer.verify(DATA, signature, pair.getPublic()));
    }

    @Test
    void verify_tamperedData_returnsFalse() {
        KeyPair pair = KeyPairFactory.generate();
        byte[] signature = Signer.sign(DATA, pair.getPrivate());

        byte[] tampered = "transaction: alice -> bob, 6"
                .getBytes(StandardCharsets.UTF_8);

        assertFalse(Signer.verify(tampered, signature, pair.getPublic()));
    }

    @Test
    void verify_wrongPublicKey_returnsFalse() {
        KeyPair alice = KeyPairFactory.generate();
        KeyPair bob = KeyPairFactory.generate();

        byte[] signatureByAlice = Signer.sign(DATA, alice.getPrivate());

        assertFalse(Signer.verify(DATA, signatureByAlice, bob.getPublic()));
    }

    @Test
    void verify_corruptedSignature_returnsFalse() {
        KeyPair pair = KeyPairFactory.generate();
        byte[] signature = Signer.sign(DATA, pair.getPrivate());

        // Пошкоджуємо підпис(змінюємо перший байт)
        byte[] corrupted = signature.clone();
        corrupted[0] = (byte) (corrupted[0] ^ 0xFF);

        assertFalse(Signer.verify(DATA, corrupted, pair.getPublic()));
    }

    @Test
    void keyPairFactory_publicKeyMatchesPrivateKey() {
        KeyPair pair = KeyPairFactory.generate();

        byte[] signature = Signer.sign(DATA, pair.getPrivate());

        assertTrue(Signer.verify(DATA, signature, pair.getPublic()));
    }
}
