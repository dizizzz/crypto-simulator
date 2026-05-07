package cryptosim.simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

//коефіцієнт Джині
// G=[sum(2i-n-1)*x_i]/n*sum(x_i)
// G =0 повна рівність
// G = 1 повна нерівність(один має все)

public final class GiniCalculator {
    private GiniCalculator() {
    }

    public static double compute(Collection<Long> balances) {
        if (balances == null) {
            throw new IllegalArgumentException("Balances must not be null");
        }
        if (balances.isEmpty()) {
            throw new IllegalArgumentException("Balances must not be empty");
        }
        for (Long b : balances) {
            if (b == null) {
                throw new IllegalArgumentException("Balances must not contain null");
            }
            if (b < 0) {
                throw new IllegalArgumentException(
                        "Negative balance is not supported, got " + b);
            }
        }

        // Сортуємо за зрост
        List<Long> sortedList = new ArrayList<>(balances);
        Collections.sort(sortedList);

        int n = sortedList.size();
        long totalSum = 0;
        for (long x_i : sortedList) {
            totalSum += x_i;
        }

        if (totalSum == 0) {
            return 0.0;
        }

        double numeratorSum = 0;
        for (int i = 0; i < n; i++) {
            numeratorSum += (2.0 * i + 1 - n) * sortedList.get(i);
        }

        return numeratorSum / ((double) n * totalSum);
    }
}
