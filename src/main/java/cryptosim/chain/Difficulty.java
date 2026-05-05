package cryptosim.chain;

import java.math.BigInteger;

public final class Difficulty {
    private Difficulty() {
    }

    public static BigInteger targetFromLeadingZeroBits(int bits) {
        if (bits < 0 || bits > 256) {
            throw new IllegalArgumentException(
                    "Leading zero bits must be in [0, 256], got " + bits);
        }
        return BigInteger.TWO.pow(256 - bits);
    }

    public static int leadingZeroBits(BigInteger target) {
        validateTarget(target);
        return 256 - target.bitLength() + 1;
    }

    public static double probability(BigInteger target) {
        validateTarget(target);
        int bitsBelow256 = 256 - (target.bitLength() - 1);
        return Math.pow(2, -bitsBelow256);
    }

    public static double expectedAttempts(BigInteger target) {
        validateTarget(target);
        return 1.0 / probability(target);
    }

    private static void validateTarget(BigInteger target) {
        if (target == null) {
            throw new IllegalArgumentException("Target must not be null");
        }
        if (target.signum() <= 0) {
            throw new IllegalArgumentException("Target must be positive, got " + target);
        }
    }
}
