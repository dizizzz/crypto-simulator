package cryptosim.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cryptosim.crypto.Hashing;
import cryptosim.crypto.Signer;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;

public record Transaction(
        Address from,
        Address to,
        long amount,
        long fee,
        long nonce,
        @JsonIgnore PublicKey senderPublicKey,
        byte[] signature
) {

    public Transaction {
        if (from == null || to == null
                || senderPublicKey == null || signature == null) {
            throw new IllegalArgumentException("Transaction fields must not be null");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        if (fee < 0) {
            throw new IllegalArgumentException("Fee must be non-negative");
        }
        if (nonce < 0) {
            throw new IllegalArgumentException("Nonce must be non-negative");
        }
    }

    public byte[] dataToSign() {
        String data = from.hex()
                + "|" + to.hex()
                + "|" + amount
                + "|" + fee
                + "|" + nonce;
        return data.getBytes(StandardCharsets.UTF_8);
    }

    public boolean verifySignature() {
        Address derivedFromKey = Address.fromPublicKey(senderPublicKey);
        if (!derivedFromKey.equals(from)) {
            return false;
        }
        return Signer.verify(dataToSign(), signature, senderPublicKey);
    }

    public String hash() {
        return Hashing.sha256(dataToSign());
    }
}
