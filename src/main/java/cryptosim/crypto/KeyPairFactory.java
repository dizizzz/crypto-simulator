package cryptosim.crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.InvalidAlgorithmParameterException;

public final class KeyPairFactory {

    // Стандартна крива NIST P-256 (secp256r1)
    private static final String CURVE_NAME = "secp256r1";

    // Алгоритм (Java Cryptography Architecture)
    private static final String ALGORITHM = "EC";

    private KeyPairFactory() {
    }

    public static KeyPair generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
            AlgorithmParameterSpec spec = new ECGenParameterSpec(CURVE_NAME);
            generator.initialize(spec);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException(
                    "EC with " + CURVE_NAME + " must be available ", e);
        }
    }
}
