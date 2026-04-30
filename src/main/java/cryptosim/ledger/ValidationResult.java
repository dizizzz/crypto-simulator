package cryptosim.ledger;

public enum ValidationResult {
    OK,
    INVALID_SIGNATURE,
    INSUFFICIENT_BALANCE,
    BAD_NONCE,
    INVALID_AMOUNT
}