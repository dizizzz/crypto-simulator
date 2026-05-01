package cryptosim.chain;

import cryptosim.crypto.Hashing;
import cryptosim.domain.Transaction;

import java.nio.charset.StandardCharsets;
import java.util.List;

public record Block(BlockHeader header, List<Transaction> transactions) {

    public Block {
        if (header == null) {
            throw new IllegalArgumentException("Block header must not be null");
        }
        if (transactions == null) {
            throw new IllegalArgumentException("Transactions list must not be null");
        }
        transactions = List.copyOf(transactions);
    }

    public String hash() {
        return header.hash();
    }

    public static String txRoot(List<Transaction> transactions) {
        StringBuilder concatenatedHashes = new StringBuilder();
        for (Transaction tx : transactions) {
            concatenatedHashes.append(tx.hash());
        }
        return Hashing.sha256(
                concatenatedHashes.toString().getBytes(StandardCharsets.UTF_8)
        );
    }
}
