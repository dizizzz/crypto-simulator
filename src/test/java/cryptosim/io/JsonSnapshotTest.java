package cryptosim.io;

import cryptosim.chain.Block;
import cryptosim.chain.BlockHeader;
import cryptosim.domain.Address;
import cryptosim.domain.Transaction;
import cryptosim.domain.Wallet;
import cryptosim.ledger.Account;
import cryptosim.simulation.NetworkStats;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonSnapshotTest {

    @Test
    void toJson_simpleAddress() {
        Address addr = new Address("a".repeat(40));

        String json = JsonSnapshot.toJson(addr);

        System.out.println("[Address JSON]");
        System.out.println("  " + json);

        assertNotNull(json);
        assertTrue(json.contains("aaaa"), "JSON should contain hex");
    }

    @Test
    void toJson_account() {
        Account account = new Account(100L, 5L);

        String json = JsonSnapshot.toJson(account);

        System.out.println("[Account JSON]");
        System.out.println("  " + json);

        assertNotNull(json);
        assertTrue(json.contains("100"));
        assertTrue(json.contains("5"));
    }

    @Test
    void toJson_transaction() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Transaction tx = alice.createTransaction(bob.getAddress(), 50, 2, 0);

        String json = JsonSnapshot.toJson(tx);

        System.out.println("[Transaction JSON]");
        System.out.println("  " + json);

        assertNotNull(json);
        assertTrue(json.contains("50"));
    }

    @Test
    void toJson_block() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Transaction tx = alice.createTransaction(bob.getAddress(), 10, 1, 0);

        BlockHeader header = new BlockHeader(
                1,
                "0".repeat(64),
                Block.txRoot(List.of(tx)),
                1000L,
                BigInteger.TWO.pow(240),
                42L,
                new Address("0".repeat(40))
        );
        Block block = new Block(header, List.of(tx));

        String json = JsonSnapshot.toJson(block);

        System.out.println("[Block JSON]");
        System.out.println("  " + json);

        assertNotNull(json);
    }

    @Test
    void toJson_networkStats() {
        NetworkStats stats = new NetworkStats(
                100, 80, 60,
                10, 10000, 50,
                25.5, 4096.0, 250.0
        );

        String json = JsonSnapshot.toJson(stats);

        System.out.println("[NetworkStats JSON]");
        System.out.println("  " + json);

        assertNotNull(json);
        assertTrue(json.contains("100"));
        assertTrue(json.contains("80"));
    }

    @Test
    void toReadableJson_hasIndentation() {
        Account account = new Account(100L, 5L);

        String compact = JsonSnapshot.toJson(account);
        String readable = JsonSnapshot.toReadableJson(account);

        System.out.println("[Compact vs Readable]");
        System.out.println("  Compact:  " + compact);
        System.out.println("  Readable:");
        System.out.println(readable);

        assertNotNull(readable);
        assertTrue(readable.contains("\n"), "Readable JSON has newlines");
        assertTrue(readable.length() > compact.length());
    }

    @Test
    void toJson_null_throws() {
        String json = JsonSnapshot.toJson(null);
        assertTrue(json.equals("null"));
    }
}
