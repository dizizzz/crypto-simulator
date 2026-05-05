package cryptosim.chain;

import cryptosim.domain.Transaction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

public final class Mempool {

    private final int maxSize;
    private final LinkedHashSet<Transaction> pool;

    public Mempool(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Max size must be positive, got " + maxSize);
        }
        this.maxSize = maxSize;
        this.pool = new LinkedHashSet<>();
    }

    public boolean add(Transaction tx) {
        if (tx == null) {
            return false;
        }
        if (pool.size() >= maxSize) {
            return false;
        }
        if (!tx.verifySignature()) {
            return false;
        }

        for (Transaction existing : pool) {
            if (existing.from().equals(tx.from())) {
                return false;
            }
        }

        return pool.add(tx);
    }

    public List<Transaction> drain(int n) {
        if (n <= 0) {
            return List.of();
        }

        List<Transaction> sorted = new ArrayList<>(pool);
        sorted.sort(Comparator.comparingLong(Transaction::fee).reversed());

        int actualCount = Math.min(n, sorted.size());
        List<Transaction> result = new ArrayList<>(sorted.subList(0, actualCount));
        pool.removeAll(result);

        return result;
    }

    public int size() {
        return pool.size();
    }

    public boolean isFull() {
        return pool.size() >= maxSize;
    }

}
