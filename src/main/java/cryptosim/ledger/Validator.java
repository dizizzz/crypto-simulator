package cryptosim.ledger;

import cryptosim.domain.Transaction;

public final class Validator {

    private Validator() {
    }

    public static ValidationResult validate(Transaction tx, WorldState state) {
        if (tx.amount() == 0) {
            return ValidationResult.INVALID_AMOUNT;
        }

        Account senderAccount = state.getAccount(tx.from());

        if (tx.nonce() != senderAccount.nonce()) {
            return ValidationResult.BAD_NONCE;
        }

        long required = tx.amount() + tx.fee();
        if (senderAccount.balance() < required) {
            return ValidationResult.INSUFFICIENT_BALANCE;
        }

        if (!tx.verifySignature()) {
            return ValidationResult.INVALID_SIGNATURE;
        }

        return ValidationResult.OK;
    }

    public static ValidationResult apply(Transaction tx, WorldState state) {
        ValidationResult result = validate(tx, state);
        if (result != ValidationResult.OK) {
            return result;
        }

        state.debit(tx.from(), tx.amount() + tx.fee());
        state.credit(tx.to(), tx.amount());
        state.incrementNonce(tx.from());

        return ValidationResult.OK;
    }
}
