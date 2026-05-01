package cryptosim.chain;

import cryptosim.domain.Address;
import cryptosim.domain.Wallet;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinerTest {
    private static final BigInteger EASY_TARGET =
            BigInteger.TWO.pow(256).subtract(BigInteger.ONE);

    private static BlockHeader templateWithTarget(BigInteger target) {
        return new BlockHeader(
                1,
                "0".repeat(64),
                Block.txRoot(List.of()),
                System.currentTimeMillis(),
                target,
                0,
                new Address("0".repeat(40))
        );
    }

    @Test
    void mine_easyTarget_findsNonceImmediately() {
        Miner miner = new Miner();
        BlockHeader template = templateWithTarget(EASY_TARGET);

        Block result = miner.mine(template, List.of());

        assertEquals(0L, result.header().nonce());
    }

    @Test
    void mine_returnsBlockWithValidPow() {
        Miner miner = new Miner();
        BlockHeader template = templateWithTarget(EASY_TARGET);

        Block result = miner.mine(template, List.of());

        BigInteger hashAsNumber = new BigInteger(result.hash(), 16);
        assertTrue(hashAsNumber.compareTo(EASY_TARGET) < 0,
                "Block hash must be less than target");
    }

    @Test
    void mine_preservesAllHeaderFields_exceptNonce() {
        Miner miner = new Miner();
        BlockHeader template = templateWithTarget(EASY_TARGET);

        Block result = miner.mine(template, List.of());
        BlockHeader resultHeader = result.header();

        assertEquals(template.index(), resultHeader.index());
        assertEquals(template.previousHash(), resultHeader.previousHash());
        assertEquals(template.txRoot(), resultHeader.txRoot());
        assertEquals(template.timestamp(), resultHeader.timestamp());
        assertEquals(template.target(), resultHeader.target());
        assertEquals(template.minerAddress(), resultHeader.minerAddress());
    }

    @Test
    void mine_preservesTransactions() {
        Miner miner = new Miner();
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        var tx1 = alice.createTransaction(bob.getAddress(), 5, 0, 0);
        var tx2 = alice.createTransaction(bob.getAddress(), 10, 0, 1);

        BlockHeader template = new BlockHeader(
                1, "0".repeat(64),
                Block.txRoot(List.of(tx1, tx2)),
                System.currentTimeMillis(),
                EASY_TARGET, 0,
                new Address("0".repeat(40))
        );

        Block result = miner.mine(template, List.of(tx1, tx2));

        assertEquals(2, result.transactions().size());
        assertEquals(tx1, result.transactions().get(0));
        assertEquals(tx2, result.transactions().get(1));
    }

    @Test
    void mine_tooSmallTarget_throwsMiningTimeout() {
        Miner miner = new Miner(100);
        BlockHeader template = templateWithTarget(BigInteger.ONE);

        assertThrows(MiningTimeoutException.class,
                () -> miner.mine(template, List.of()));
    }

    @Test
    void mine_realisticTarget_findsNonceWithinReasonableAttempts() {
        Miner miner = new Miner(500_000);
        BigInteger realisticTarget = BigInteger.TWO.pow(240);
        BlockHeader template = templateWithTarget(realisticTarget);

        Block result = miner.mine(template, List.of());

        BigInteger hashAsNumber = new BigInteger(result.hash(), 16);
        assertTrue(hashAsNumber.compareTo(realisticTarget) < 0);
        assertNotEquals(0L, result.header().nonce());
    }

    @Test
    void constructor_invalidMaxAttempts_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Miner(0));
        assertThrows(IllegalArgumentException.class, () -> new Miner(-1));
    }
}
