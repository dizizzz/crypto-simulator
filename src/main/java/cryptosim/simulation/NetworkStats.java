package cryptosim.simulation;

public record NetworkStats(
        long submittedTransactions, long acceptedTransactions, long confirmedTransactions,
        long totalBlocks, long durationMs, long totalFeesPaid,
        double averageBlockMiningTimeMs, double averageNonceAttempts, double averageConfirmationLatencyMs
) {

    public NetworkStats {
        if (submittedTransactions < 0 || acceptedTransactions < 0
                || confirmedTransactions < 0 || totalBlocks < 0
                || durationMs < 0 || totalFeesPaid < 0) {
            throw new IllegalArgumentException("Counts must be non-negative");
        }
        if (acceptedTransactions > submittedTransactions) {
            throw new IllegalArgumentException(
                    "Accepted cannot exceed submitted");
        }
        if (confirmedTransactions > acceptedTransactions) {
            throw new IllegalArgumentException(
                    "Confirmed cannot exceed accepted");
        }
    }

    public double mempoolThroughput() {
        if (acceptedTransactions == 0) {
            return 0.0;
        }
        return (double) confirmedTransactions / acceptedTransactions;
    }

    public double rejectionRate() {
        if (submittedTransactions == 0) {
            return 0.0;
        }
        return (double) (submittedTransactions - acceptedTransactions) / submittedTransactions;
    }
}
