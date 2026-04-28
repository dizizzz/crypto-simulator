package cryptosim.crypto;

import org.junit.jupiter.api.Test;

import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class KeyPairFactoryTest {

    @Test
    void generate_returnsNonNullKeyPair() {
        KeyPair pair = KeyPairFactory.generate();

        assertNotNull(pair);
        assertNotNull(pair.getPrivate());
        assertNotNull(pair.getPublic());
    }

    @Test
    void generate_returnsEcKeys() {
        // Перевіряємо, що алгоритм пари EC
        KeyPair pair = KeyPairFactory.generate();

        assertEquals("EC", pair.getPrivate().getAlgorithm());
        assertEquals("EC", pair.getPublic().getAlgorithm());
    }

    @Test
    void generate_twoCallsProduceDifferentKeys() {
        KeyPair first = KeyPairFactory.generate();
        KeyPair second = KeyPairFactory.generate();

        assertNotEquals(first.getPrivate(), second.getPrivate());
        assertNotEquals(first.getPublic(), second.getPublic());
    }
}
