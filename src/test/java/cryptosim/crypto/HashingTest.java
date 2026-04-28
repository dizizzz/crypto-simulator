package cryptosim.crypto;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class HashingTest {

    @Test
    void sha256_emptyInput_returnsKnownHash() {
        // Хеш порожнього набору байтів (публічно константа SHA-256)
        String expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        String actual = Hashing.sha256(new byte[0]);

        assertEquals(expected, actual);
    }

    @Test
    void sha256_knownInput_abc_returnsKnownHash() {
        // Офіційний тестовий вектор з NIST FIPS 180-4
        String expected = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

        String actual = Hashing.sha256("abc".getBytes(StandardCharsets.UTF_8));

        assertEquals(expected, actual);
    }

    @Test
    void sha256_smallChange_completelyDifferentHash() {
        // одна змінена буква -> інший хеш
        String hash1 = Hashing.sha256("hello".getBytes(StandardCharsets.UTF_8));
        String hash2 = Hashing.sha256("hellp".getBytes(StandardCharsets.UTF_8));

        assertNotEquals(hash1, hash2);
    }
}