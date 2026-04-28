package cryptosim.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public final class Signer {

    // Алгоритм підпису: SHA-256 + ECDSA
    private static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";

    private Signer() {
    }

    public static byte[] sign(byte[] data, PrivateKey privateKey) {
        try {
            Signature signer = Signature.getInstance(SIGNATURE_ALGORITHM);
            signer.initSign(privateKey);
            signer.update(data);
            return signer.sign();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA256withECDSA must be available ", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid private key for ECDSA", e);
        } catch (SignatureException e) {
            throw new IllegalStateException("Failed to produce ECDSA signature", e);
        }
    }

    public static boolean verify(byte[] data, byte[] signature, PublicKey publicKey) {
        try {
            Signature verifier = Signature.getInstance(SIGNATURE_ALGORITHM);
            verifier.initVerify(publicKey);
            verifier.update(data);
            return verifier.verify(signature);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA256withECDSA must be available ", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid public key for ECDSA", e);
        } catch (SignatureException e) {
            return false;
        }
    }
}