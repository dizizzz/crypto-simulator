package cryptosim.ledger;

public record Account(long balance, long nonce) {
    public Account {
        if (balance < 0) {
            throw new IllegalArgumentException("Balance must be non-negative, got " + balance);
        }
        if (nonce < 0) {
            throw new IllegalArgumentException("Nonce must be non-negative, got " + nonce);
        }
    }

    public static Account empty() {
        return new Account(0, 0);
    }
}
