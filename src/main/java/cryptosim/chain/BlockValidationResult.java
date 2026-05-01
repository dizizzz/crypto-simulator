package cryptosim.chain;

public enum BlockValidationResult {
    OK,
    BAD_INDEX,
    BAD_PREVIOUS_HASH,
    BAD_TX_ROOT,
    BAD_POW,
    INVALID_TRANSACTION
}
