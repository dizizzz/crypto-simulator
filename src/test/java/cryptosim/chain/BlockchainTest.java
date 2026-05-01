package cryptosim.chain;

import cryptosim.domain.Address;
import cryptosim.domain.Transaction;
import cryptosim.domain.Wallet;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockchainTest {
    private static final BigInteger EASY_TARGET =
            BigInteger.TWO.pow(256).subtract(BigInteger.ONE);

    private static Block buildBlockOnTop(Block tip, List<Transaction> txs) {
        BlockHeader header = new BlockHeader(
                tip.header().index() + 1,
                tip.hash(),
                Block.txRoot(txs),
                System.currentTimeMillis(),
                EASY_TARGET,
                0,
                new Address("0".repeat(40))
        );
        return new Block(header, txs);
    }

    @Test
    void genesis_isCreatedAutomatically() {
        Blockchain chain = new Blockchain(Map.of(), EASY_TARGET);

        assertEquals(0, chain.height());
        assertEquals(chain.getGenesis(), chain.getTip());
    }

    @Test
    void genesis_hasIndexZeroAndZerosPreviousHash() {
        Blockchain chain = new Blockchain(Map.of(), EASY_TARGET);
        Block genesis = chain.getGenesis();

        assertEquals(0, genesis.header().index());
        assertEquals("0".repeat(64), genesis.header().previousHash());
        assertEquals(0, genesis.transactions().size());
    }

    @Test
    void addBlock_validBlock_returnsOkAndAdded() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Blockchain chain = new Blockchain(Map.of(
                alice.getAddress(), 100L,
                bob.getAddress(), 0L
        ), EASY_TARGET);

        Transaction tx = alice.createTransaction(bob.getAddress(), 30, 0, 0);
        Block block = buildBlockOnTop(chain.getTip(), List.of(tx));

        BlockValidationResult result = chain.addBlock(block);

        assertEquals(BlockValidationResult.OK, result);
        assertEquals(1, chain.height());
        assertEquals(block, chain.getTip());
    }

    @Test
    void addBlock_appliesTransactionsToWorldState() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Blockchain chain = new Blockchain(Map.of(
                alice.getAddress(), 100L,
                bob.getAddress(), 0L
        ), EASY_TARGET);

        Transaction tx = alice.createTransaction(bob.getAddress(), 30, 0, 0);
        Block block = buildBlockOnTop(chain.getTip(), List.of(tx));

        chain.addBlock(block);

        assertEquals(70, chain.worldState().getBalance(alice.getAddress()));
        assertEquals(30, chain.worldState().getBalance(bob.getAddress()));
        assertEquals(1, chain.worldState().getNonce(alice.getAddress()));
    }

    @Test
    void addBlock_badIndex_returnsBadIndex() {
        Blockchain chain = new Blockchain(Map.of(), EASY_TARGET);

        BlockHeader badHeader = new BlockHeader(
                5,
                chain.getTip().hash(),
                Block.txRoot(List.of()),
                System.currentTimeMillis(),
                EASY_TARGET,
                0,
                new Address("0".repeat(40))
        );
        Block badBlock = new Block(badHeader, List.of());

        assertEquals(BlockValidationResult.BAD_INDEX, chain.addBlock(badBlock));
    }

    @Test
    void addBlock_badPreviousHash_returnsBadPreviousHash() {
        Blockchain chain = new Blockchain(Map.of(), EASY_TARGET);

        BlockHeader badHeader = new BlockHeader(
                1,
                "f".repeat(64),
                Block.txRoot(List.of()),
                System.currentTimeMillis(),
                EASY_TARGET,
                0,
                new Address("0".repeat(40))
        );
        Block badBlock = new Block(badHeader, List.of());

        assertEquals(BlockValidationResult.BAD_PREVIOUS_HASH, chain.addBlock(badBlock));
    }

    @Test
    void addBlock_badTxRoot_returnsBadTxRoot() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Blockchain chain = new Blockchain(Map.of(
                alice.getAddress(), 100L
        ), EASY_TARGET);

        Transaction realTx = alice.createTransaction(bob.getAddress(), 5, 0, 0);
        Transaction fakeTx = alice.createTransaction(bob.getAddress(), 99, 0, 0);

        BlockHeader header = new BlockHeader(
                1,
                chain.getTip().hash(),
                Block.txRoot(List.of(realTx)),
                System.currentTimeMillis(),
                EASY_TARGET,
                0,
                new Address("0".repeat(40))
        );
        Block badBlock = new Block(header, List.of(fakeTx));

        assertEquals(BlockValidationResult.BAD_TX_ROOT, chain.addBlock(badBlock));
    }

    @Test
    void addBlock_invalidTransaction_returnsInvalidTransactionAndDoesNotChangeState() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Blockchain chain = new Blockchain(Map.of(
                alice.getAddress(), 50L,
                bob.getAddress(), 0L
        ), EASY_TARGET);

        Transaction validTx = alice.createTransaction(bob.getAddress(), 30, 0, 0);
        Transaction invalidTx = alice.createTransaction(bob.getAddress(), 999, 0, 1);  // забагато

        Block block = buildBlockOnTop(chain.getTip(), List.of(validTx, invalidTx));

        BlockValidationResult result = chain.addBlock(block);

        assertEquals(BlockValidationResult.INVALID_TRANSACTION, result);
        assertEquals(50, chain.worldState().getBalance(alice.getAddress()));
        assertEquals(0, chain.worldState().getBalance(bob.getAddress()));
        assertEquals(0, chain.worldState().getNonce(alice.getAddress()));
        assertEquals(0, chain.height());
    }

    @Test
    void isValid_emptyChain_returnsTrue() {
        Blockchain chain = new Blockchain(Map.of(), EASY_TARGET);

        assertTrue(chain.isValid());
    }

    @Test
    void isValid_validChain_returnsTrue() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Blockchain chain = new Blockchain(Map.of(
                alice.getAddress(), 100L,
                bob.getAddress(), 0L
        ), EASY_TARGET);

        for (int i = 0; i < 3; i++) {
            Transaction tx = alice.createTransaction(bob.getAddress(), 5, 0, i);
            Block block = buildBlockOnTop(chain.getTip(), List.of(tx));
            chain.addBlock(block);
        }

        assertEquals(3, chain.height());
        assertTrue(chain.isValid());
    }

    @Test
    void addBlock_preservesTotalSupply_acrossMultipleBlocks() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Wallet charlie = Wallet.create();

        Blockchain chain = new Blockchain(Map.of(
                alice.getAddress(), 100L,
                bob.getAddress(), 50L,
                charlie.getAddress(), 30L
        ), EASY_TARGET);

        long initialTotal = chain.worldState().totalSupply();
        assertEquals(180L, initialTotal);

        Block block1 = buildBlockOnTop(chain.getTip(), List.of(
                alice.createTransaction(bob.getAddress(), 30, 0, 0),
                bob.createTransaction(charlie.getAddress(), 20, 0, 0)
        ));
        chain.addBlock(block1);

        Block block2 = buildBlockOnTop(chain.getTip(), List.of(
                charlie.createTransaction(alice.getAddress(), 10, 0, 0),
                alice.createTransaction(bob.getAddress(), 5, 0, 1)
        ));
        chain.addBlock(block2);

        assertEquals(2, chain.height());
        assertEquals(initialTotal, chain.worldState().totalSupply());
    }

    @Test
    void addBlock_withFees_creditsMinerWithCoinbase() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Wallet miner = Wallet.create();

        Blockchain chain = new Blockchain(Map.of(
                alice.getAddress(), 100L,
                bob.getAddress(), 0L
        ), EASY_TARGET);

        Transaction tx1 = alice.createTransaction(bob.getAddress(), 10, 2, 0);
        Transaction tx2 = alice.createTransaction(bob.getAddress(), 10, 3, 1);

        BlockHeader header = new BlockHeader(
                chain.getTip().header().index() + 1,
                chain.getTip().hash(),
                Block.txRoot(List.of(tx1, tx2)),
                System.currentTimeMillis(),
                EASY_TARGET,
                0,
                miner.getAddress()
        );
        Block block = new Block(header, List.of(tx1, tx2));

        chain.addBlock(block);

        // Аліса: 100 - 10 - 2 - 10 - 3 = 75
        // Боб:   0 + 10 + 10 = 20
        // Miner: 0 + 2 + 3 = 5
        assertEquals(75, chain.worldState().getBalance(alice.getAddress()));
        assertEquals(20, chain.worldState().getBalance(bob.getAddress()));
        assertEquals(5, chain.worldState().getBalance(miner.getAddress()));
    }

    @Test
    void addBlock_preservesTotalSupply_withNonZeroFees() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Wallet charlie = Wallet.create();
        Wallet miner = Wallet.create();

        Blockchain chain = new Blockchain(Map.of(
                alice.getAddress(), 100L,
                bob.getAddress(), 50L,
                charlie.getAddress(), 30L
        ), EASY_TARGET);

        long initialTotal = chain.worldState().totalSupply();
        assertEquals(180L, initialTotal);

        Transaction tx1 = alice.createTransaction(bob.getAddress(), 30, 2, 0);
        Transaction tx2 = bob.createTransaction(charlie.getAddress(), 20, 1, 0);
        Transaction tx3 = charlie.createTransaction(alice.getAddress(), 10, 3, 0);

        BlockHeader header = new BlockHeader(
                chain.getTip().header().index() + 1,
                chain.getTip().hash(),
                Block.txRoot(List.of(tx1, tx2, tx3)),
                System.currentTimeMillis(),
                EASY_TARGET,
                0,
                miner.getAddress()
        );
        Block block = new Block(header, List.of(tx1, tx2, tx3));

        chain.addBlock(block);

        // sum B = const
        assertEquals(initialTotal, chain.worldState().totalSupply());
    }
}
