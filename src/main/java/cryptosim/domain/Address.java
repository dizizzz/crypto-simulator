package cryptosim.domain;

import cryptosim.crypto.Hashing;

import java.security.PublicKey;

public record Address(String hex) {
    private static final int ADDRESS_LENGTH_BYTES = 20;

    public Address {
        if (hex == null) {
            throw new IllegalArgumentException("Address hex must not be null");
        }
        int expectedLength = ADDRESS_LENGTH_BYTES * 2;
        if (hex.length() != expectedLength) {
            throw new IllegalArgumentException(
                    "Address hex must be " + expectedLength + " characters, got " + hex.length());
        }
    }

    public static Address fromPublicKey(PublicKey publicKey) {
        byte[] publicKeyBytes = publicKey.getEncoded();

        // 32 байти - повертає hex-рядок з 64 символів
        String fullHash = Hashing.sha256(publicKeyBytes);

        // перші 20 байтів = 40 hex-символів
        String addressHex = fullHash.substring(0, ADDRESS_LENGTH_BYTES * 2);

        return new Address(addressHex);
    }
}
