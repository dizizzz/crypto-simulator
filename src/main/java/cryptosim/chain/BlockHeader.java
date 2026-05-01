package cryptosim.chain;

import cryptosim.crypto.Hashing;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public record BlockHeader(
        long index,
        String previousHash,
        String txRoot,
        long timestamp,
        BigInteger target,
        long nonce
) {

    public BlockHeader {
        if (index < 0) {
            throw new IllegalArgumentException("Block index must be non-negative");
        }
        if (previousHash == null) {
            throw new IllegalArgumentException("Previous hash must not be null");
        }
        if (txRoot == null) {
            throw new IllegalArgumentException("Transaction root must not be null");
        }
        if (target == null || target.signum() <= 0) {
            throw new IllegalArgumentException("Target must be a positive integer");
        }
        if (nonce < 0) {
            throw new IllegalArgumentException("Nonce must be non-negative");
        }
    }

    public String hash() {
        String s = index
                + "|" + previousHash
                + "|" + txRoot
                + "|" + timestamp
                + "|" + target.toString(16)
                + "|" + nonce;
        return Hashing.sha256(s.getBytes(StandardCharsets.UTF_8));
    }

    public BlockHeader withNoncee(long newNonce) {
        return new BlockHeader(index, previousHash, txRoot, timestamp, target, newNonce);
    }
}
