package cryptosim.simulation;

import com.fasterxml.jackson.annotation.JsonProperty;
import cryptosim.chain.Blockchain;
import cryptosim.domain.Address;
import cryptosim.domain.Wallet;
import java.util.List;

public record SimulationResult(
        Blockchain blockchain,
        List<Wallet> wallets,
        Address minerAddress,
        NetworkStats stats
) {

    public SimulationResult {
        if (blockchain == null) {
            throw new IllegalArgumentException("Blockchain must not be null");
        }
        if (wallets == null || wallets.isEmpty()) {
            throw new IllegalArgumentException("Wallets must not be null or empty");
        }
        if (minerAddress == null) {
            throw new IllegalArgumentException("Miner address must not be null");
        }
        if (stats == null) {
            throw new IllegalArgumentException("Stats must not be null");
        }
        wallets = List.copyOf(wallets);
    }

    @JsonProperty("finalBalances")
    public List<ParticipantBalance> finalBalances() {
        List<ParticipantBalance> result = new java.util.ArrayList<>();
        for (int i = 0; i < wallets.size(); i++) {
            Wallet w = wallets.get(i);
            long balance = blockchain.worldState().getBalance(w.getAddress());
            result.add(new ParticipantBalance("Wallet " + (i + 1), balance));
        }
        long minerBalance = blockchain.worldState().getBalance(minerAddress);
        result.add(new ParticipantBalance("Miner", minerBalance));
        return result;
    }

    public record ParticipantBalance(String label, long balance) {}
}
