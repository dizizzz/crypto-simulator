package cryptosim.chain;

import cryptosim.domain.Transaction;
import cryptosim.domain.Wallet;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BlockTest {
    private static BlockHeader simpleHeader(String txRoot, long nonce) {
        return new BlockHeader(
                1,
                "0".repeat(64),
                txRoot,
                1000L,
                BigInteger.TWO.pow(240),
                nonce
        );
    }

    @Test
    void txRoot_emptyList_returnsConsistentHash() {
        String root1 = Block.txRoot(List.of());
        String root2 = Block.txRoot(List.of());

        assertEquals(64, root1.length());
        assertEquals(root1, root2);
    }

    @Test
    void txRoot_isDeterministic() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();

        Transaction tx1 = alice.createTransaction(bob.getAddress(), 5, 0, 0);
        Transaction tx2 = alice.createTransaction(bob.getAddress(), 10, 0, 1);

        String root1 = Block.txRoot(List.of(tx1, tx2));
        String root2 = Block.txRoot(List.of(tx1, tx2));

        assertEquals(root1, root2);
    }

    @Test
    void txRoot_orderMatters() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();

        Transaction tx1 = alice.createTransaction(bob.getAddress(), 5, 0, 0);
        Transaction tx2 = alice.createTransaction(bob.getAddress(), 10, 0, 1);

        String root12 = Block.txRoot(List.of(tx1, tx2));
        String root21 = Block.txRoot(List.of(tx2, tx1));

        assertNotEquals(root12, root21);
    }

    @Test
    void txRoot_changedTx_changesRoot() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();

        Transaction tx1 = alice.createTransaction(bob.getAddress(), 5, 0, 0);
        Transaction tx2different = alice.createTransaction(bob.getAddress(), 999, 0, 0);

        String root1 = Block.txRoot(List.of(tx1));
        String root2 = Block.txRoot(List.of(tx2different));

        assertNotEquals(root1, root2);
    }

    @Test
    void hash_delegatesToHeader() {
        BlockHeader header = simpleHeader(Block.txRoot(List.of()), 0);
        Block block = new Block(header, List.of());

        assertEquals(header.hash(), block.hash());
    }

    @Test
    void hash_changingNonce_changesBlockHash() {
        BlockHeader h0 = simpleHeader(Block.txRoot(List.of()), 0);
        BlockHeader h1 = h0.withNoncee(1);

        Block b0 = new Block(h0, List.of());
        Block b1 = new Block(h1, List.of());

        assertNotEquals(b0.hash(), b1.hash());
    }

    @Test
    void transactions_isImmutable() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Transaction tx = alice.createTransaction(bob.getAddress(), 5, 0, 0);

        List<Transaction> mutableList = new ArrayList<>();
        mutableList.add(tx);

        BlockHeader header = simpleHeader(Block.txRoot(mutableList), 0);
        Block block = new Block(header, mutableList);

        mutableList.clear();
        assertEquals(1, block.transactions().size());

        assertThrows(UnsupportedOperationException.class,
                () -> block.transactions().clear());
    }

    @Test
    void constructor_nullField_throwsException() {
        BlockHeader header = simpleHeader(Block.txRoot(List.of()), 0);

        assertThrows(IllegalArgumentException.class,
                () -> new Block(null, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Block(header, null));
    }
}
