package cryptosim.chain;

import com.fasterxml.jackson.annotation.JsonProperty;
import cryptosim.domain.Address;
import cryptosim.domain.Transaction;
import cryptosim.ledger.Validator;
import cryptosim.ledger.ValidationResult;
import cryptosim.ledger.WorldState;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class Blockchain {
    private static final String GENESIS_PREVIOUS_HASH = "0".repeat(64);
    private static final long GENESIS_TIMESTAMP = 0L;

    private final BigInteger genesisTarget;
    private final WorldState worldState;
    private final List<Block> blocks;
    private static final Address GENESIS_MINER_ADDRESS = new Address("0".repeat(40));

    public Blockchain(Map<Address, Long> initialBalances, BigInteger genesisTarget) {
        if (initialBalances == null) {
            throw new IllegalArgumentException("Initial balances must not be null");
        }
        if (genesisTarget == null || genesisTarget.signum() <= 0) {
            throw new IllegalArgumentException("Genesis target must be positive");
        }

        this.genesisTarget = genesisTarget;
        this.worldState = new WorldState(initialBalances);
        this.blocks = new ArrayList<>();
        this.blocks.add(createGenesisBlock());
    }

    private Block createGenesisBlock() {
        BlockHeader header = new BlockHeader(
                0,
                GENESIS_PREVIOUS_HASH,
                Block.txRoot(List.of()),
                GENESIS_TIMESTAMP,
                genesisTarget,
                0,
                GENESIS_MINER_ADDRESS
        );
        return new Block(header, List.of());
    }

    public BlockValidationResult addBlock(Block block) {
        if (block == null) {
            throw new IllegalArgumentException("Block must not be null");
        }

        Block tip = getTip();

        if (block.header().index() != tip.header().index() + 1) {
            return BlockValidationResult.BAD_INDEX;
        }

        if (!block.header().previousHash().equals(tip.hash())) {
            return BlockValidationResult.BAD_PREVIOUS_HASH;
        }

        String expectedTxRoot = Block.txRoot(block.transactions());
        if (!block.header().txRoot().equals(expectedTxRoot)) {
            return BlockValidationResult.BAD_TX_ROOT;
        }

        BigInteger blockHashAsNumber = new BigInteger(block.hash(), 16);
        if (blockHashAsNumber.compareTo(block.header().target()) >= 0) {
            return BlockValidationResult.BAD_POW;
        }

        WorldState trial = worldState.copy();
        for (Transaction tx : block.transactions()) {
            ValidationResult result = Validator.apply(tx, trial);
            if (result != ValidationResult.OK) {
                return BlockValidationResult.INVALID_TRANSACTION;
            }
        }

        for (Transaction tx : block.transactions()) {
            Validator.apply(tx, worldState);
        }

        long totalFees = 0;
        for (Transaction tx : block.transactions()) {
            totalFees += tx.fee();
        }
        worldState.rewardMiner(block.header().minerAddress(), totalFees);

        blocks.add(block);
        return BlockValidationResult.OK;
    }

    public Block getGenesis() {
        return blocks.get(0);
    }

    public Block getTip() {
        return blocks.get(blocks.size() - 1);
    }

    public int height() {
        return blocks.size() - 1;
    }

    @JsonProperty("blocks")
    public List<Block> blocks() {
        return Collections.unmodifiableList(blocks);
    }

    public WorldState worldState() {
        return worldState;
    }

    public boolean isValid() {
        for (int i = 1; i < blocks.size(); i++) {
            Block current = blocks.get(i);
            Block previous = blocks.get(i - 1);

            if (current.header().index() != previous.header().index() + 1) {
                return false;
            }
            if (!current.header().previousHash().equals(previous.hash())) {
                return false;
            }
            if (!current.header().txRoot().equals(Block.txRoot(current.transactions()))) {
                return false;
            }
        }
        return true;
    }
}
