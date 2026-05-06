package cryptosim.simulation.scenarios;

import cryptosim.chain.Block;
import cryptosim.chain.BlockHeader;
import cryptosim.chain.BlockValidationResult;
import cryptosim.chain.Blockchain;
import cryptosim.chain.Mempool;
import cryptosim.domain.Address;
import cryptosim.domain.Transaction;
import cryptosim.domain.Wallet;
import cryptosim.ledger.ValidationResult;
import cryptosim.ledger.Validator;
import cryptosim.ledger.WorldState;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Сценарій 2: подвійна витрата (double-spend)
class DoubleSpendScenarioTest {
    @Test
    void mempoolBlocksDoubleSpend() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Wallet charlie = Wallet.create();
        Mempool mempool = new Mempool(100);

        // однаковий nonce
        Transaction txToBob = alice.createTransaction(bob.getAddress(), 80, 1, 0);
        Transaction txToCharlie = alice.createTransaction(charlie.getAddress(), 80, 1, 0);

        boolean firstAccepted = mempool.add(txToBob);
        boolean secondAccepted = mempool.add(txToCharlie);

        System.out.println("[Scenario: Double-Spend via Mempool]");
        System.out.println("  Alice creates 2 transactions with same nonce=0");
        System.out.println("  First (-> Bob):     accepted=" + firstAccepted);
        System.out.println("  Second (-> Charlie): accepted=" + secondAccepted);
        System.out.println("  Mempool size:       " + mempool.size());

        assertTrue(firstAccepted, "First transaction should pass");
        assertFalse(secondAccepted, "Second (double-spend) should be rejected by mempool");
        assertEquals(1, mempool.size());
    }

    @Test
    void validatorBlocksDoubleSpend_whenMempoolBypassed() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Wallet charlie = Wallet.create();

        WorldState state = new WorldState(Map.of(
                alice.getAddress(), 100L,
                bob.getAddress(), 0L,
                charlie.getAddress(), 0L
        ));

        Transaction txToBob = alice.createTransaction(bob.getAddress(), 80, 1, 0);
        Transaction txToCharlie = alice.createTransaction(charlie.getAddress(), 80, 1, 0);

        ValidationResult firstResult = Validator.apply(txToBob, state);

        ValidationResult secondResult = Validator.apply(txToCharlie, state);

        System.out.println("[Scenario: Double-Spend via Direct Validator]");
        System.out.println("  After applying first tx:  Alice.nonce=" + state.getNonce(alice.getAddress()));
        System.out.println("  Validating second tx:     " + secondResult);
        System.out.println("  Alice final balance:      " + state.getBalance(alice.getAddress()));
        System.out.println("  Bob final balance:        " + state.getBalance(bob.getAddress()));
        System.out.println("  Charlie final balance:    " + state.getBalance(charlie.getAddress()));

        assertEquals(ValidationResult.OK, firstResult);
        assertEquals(ValidationResult.BAD_NONCE, secondResult);

        assertEquals(19, state.getBalance(alice.getAddress()),
                "Alice should have spent only one transaction, not two");
        assertEquals(80, state.getBalance(bob.getAddress()));
        assertEquals(0, state.getBalance(charlie.getAddress()));
    }

    @Test
    void blockchainRejectsBlockWithDoubleSpend() {
        Wallet alice = Wallet.create();
        Wallet bob = Wallet.create();
        Wallet charlie = Wallet.create();

        BigInteger easyTarget = BigInteger.TWO.pow(256).subtract(BigInteger.ONE);
        Blockchain chain = new Blockchain(Map.of(
                alice.getAddress(), 100L,
                bob.getAddress(), 0L,
                charlie.getAddress(), 0L
        ), easyTarget);

        Transaction txToBob = alice.createTransaction(bob.getAddress(), 80, 1, 0);
        Transaction txToCharlie = alice.createTransaction(charlie.getAddress(), 80, 1, 0);

        BlockHeader header = new BlockHeader(
                chain.getTip().header().index() + 1,
                chain.getTip().hash(),
                Block.txRoot(List.of(txToBob, txToCharlie)),
                System.currentTimeMillis(),
                easyTarget,
                0,
                new Address("0".repeat(40))
        );
        Block badBlock = new Block(header, List.of(txToBob, txToCharlie));

        BlockValidationResult result = chain.addBlock(badBlock);

        System.out.println("[Scenario: Malicious Block with Double-Spend]");
        System.out.println("  Block validation result:  " + result);
        System.out.println("  Chain height after:       " + chain.height());
        System.out.println("  Alice balance after:      " + chain.worldState().getBalance(alice.getAddress()));

        assertEquals(BlockValidationResult.INVALID_TRANSACTION, result);
        assertEquals(0, chain.height(), "Block should not be added");
        assertEquals(100, chain.worldState().getBalance(alice.getAddress()),
                "Alice balance untouched — атомарне відхилення спрацювало");
    }
}
