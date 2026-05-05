package cryptosim.chain;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DifficultyTest {

    @Test
    void targetFromLeadingZeroBits_zero_returnsMaxValue() {
        BigInteger target = Difficulty.targetFromLeadingZeroBits(0);

        assertEquals(BigInteger.TWO.pow(256), target);
    }

    @Test
    void targetFromLeadingZeroBits_16_returns2pow240() {
        BigInteger target = Difficulty.targetFromLeadingZeroBits(16);

        assertEquals(BigInteger.TWO.pow(240), target);
    }

    @Test
    void targetFromLeadingZeroBits_invalidBits_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> Difficulty.targetFromLeadingZeroBits(-1));
        assertThrows(IllegalArgumentException.class,
                () -> Difficulty.targetFromLeadingZeroBits(257));
    }

    @Test
    void leadingZeroBits_isInverseOfTargetFromBits() {
        for (int n : new int[]{0, 8, 16, 20, 24, 100, 200, 256}) {
            BigInteger target = Difficulty.targetFromLeadingZeroBits(n);
            int back = Difficulty.leadingZeroBits(target);
            assertEquals(n, back, "Round-trip failed for bits=" + n);
        }
    }

    @Test
    void probability_easyTarget_isOne() {
        BigInteger target = Difficulty.targetFromLeadingZeroBits(0);

        double p = Difficulty.probability(target);

        assertEquals(1.0, p, 1e-15);
    }

    @Test
    void probability_target2pow240_isOneOver65k() {
        BigInteger target = Difficulty.targetFromLeadingZeroBits(16);

        double p = Difficulty.probability(target);

        assertEquals(1.0 / 65536, p, 1e-15);
    }

    @Test
    void expectedAttempts_isReciprocalOfProbability() {
        BigInteger target = Difficulty.targetFromLeadingZeroBits(20);

        double p = Difficulty.probability(target);
        double e = Difficulty.expectedAttempts(target);

        assertEquals(1.0 / p, e, 1e-9);
    }

    @Test
    void expectedAttempts_target2pow240_is65k() {
        BigInteger target = Difficulty.targetFromLeadingZeroBits(16);

        double e = Difficulty.expectedAttempts(target);

        assertEquals(65536.0, e, 1e-6);
    }

    @Test
    void validateTarget_nullOrNegative_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> Difficulty.probability(null));
        assertThrows(IllegalArgumentException.class,
                () -> Difficulty.probability(BigInteger.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> Difficulty.probability(BigInteger.valueOf(-1)));
        assertThrows(IllegalArgumentException.class,
                () -> Difficulty.expectedAttempts(BigInteger.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> Difficulty.leadingZeroBits(BigInteger.ZERO));
    }
}
