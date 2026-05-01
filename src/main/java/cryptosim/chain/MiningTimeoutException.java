package cryptosim.chain;

// майнер перевищив дозволену кількість спроб
public class MiningTimeoutException extends RuntimeException {

    public MiningTimeoutException(long maxAttempts) {
        super("Mining timed out after " + maxAttempts + " attempts");
    }
}
