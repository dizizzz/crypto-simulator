package cryptosim.simulation;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GiniCalculatorTest {
    @Test
    void equalDistribution_returnsZero() {
        double gini = GiniCalculator.compute(List.of(100L, 100L, 100L, 100L));

        assertEquals(0.0, gini, 1e-9);
    }

    @Test
    void oneHasAll_returnsHighGini() {
        double gini = GiniCalculator.compute(List.of(1000L, 0L, 0L, 0L));

        assertEquals(0.75, gini, 1e-9);
    }

    @Test
    void moderateInequality() {
        double gini = GiniCalculator.compute(List.of(300L, 100L, 0L, 0L));

        assertEquals(0.625, gini, 1e-9);
    }

    @Test
    void singleElement_returnsZero() {
        double gini = GiniCalculator.compute(List.of(100L));

        assertEquals(0.0, gini, 1e-9);
    }

    @Test
    void allZeros_returnsZero() {
        double gini = GiniCalculator.compute(List.of(0L, 0L, 0L));

        assertEquals(0.0, gini, 1e-9);
    }

    @Test
    void orderIndependence() {
        double a = GiniCalculator.compute(Arrays.asList(100L, 200L, 50L));
        double b = GiniCalculator.compute(Arrays.asList(50L, 100L, 200L));
        double c = GiniCalculator.compute(Arrays.asList(200L, 50L, 100L));

        assertEquals(a, b, 1e-9);
        assertEquals(b, c, 1e-9);
    }

    @Test
    void nullOrEmpty_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> GiniCalculator.compute(null));
        assertThrows(IllegalArgumentException.class,
                () -> GiniCalculator.compute(Collections.emptyList()));
    }

    @Test
    void negativeBalance_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> GiniCalculator.compute(List.of(100L, -50L)));
    }
}
