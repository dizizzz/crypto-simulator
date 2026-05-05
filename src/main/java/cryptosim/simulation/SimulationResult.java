package cryptosim.simulation;

import cryptosim.chain.Blockchain;
import cryptosim.domain.Address;
import cryptosim.domain.Wallet;
import java.util.List;

public record SimulationResult(
        Blockchain blockchain,
        List<Wallet> wallets,
        Address minerAddress
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
        wallets = List.copyOf(wallets);
    }
}
