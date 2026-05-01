package cryptosim.ledger;

import cryptosim.domain.Address;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WorldState {

    private final Map<Address, Account> accounts;

    public WorldState(Map<Address, Long> initialBalances) {
        this.accounts = new LinkedHashMap<>();
        for (Map.Entry<Address, Long> entry : initialBalances.entrySet()) {
            if (entry.getValue() < 0) {
                throw new IllegalArgumentException(
                        "Initial balance must be non-negative for " + entry.getKey().hex());
            }
            this.accounts.put(entry.getKey(), new Account(entry.getValue(), 0));
        }
    }

    public Account getAccount(Address address) {
        return accounts.getOrDefault(address, Account.empty());
    }

    public long getBalance(Address address) {
        return getAccount(address).balance();
    }

    public long getNonce(Address address) {
        return getAccount(address).nonce();
    }

    public long totalSupply() {
        long sum = 0;
        for (Account account : accounts.values()) {
            sum += account.balance();
        }
        return sum;
    }

    public void rewardMiner(Address minerAddress, long totalFees) {
        if (minerAddress == null) {
            throw new IllegalArgumentException("Miner address must not be null");
        }
        if (totalFees < 0) {
            throw new IllegalArgumentException("Total fees must be non-negative");
        }
        if (totalFees == 0) {
            return;
        }
        credit(minerAddress, totalFees);
    }

    public Map<Address, Account> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(accounts));
    }

    void debit(Address address, long amount) {
        Account current = getAccount(address);
        accounts.put(address, new Account(current.balance() - amount, current.nonce()));
    }

    void credit(Address address, long amount) {
        Account current = getAccount(address);
        accounts.put(address, new Account(current.balance() + amount, current.nonce()));
    }

    void incrementNonce(Address address) {
        Account current = getAccount(address);
        accounts.put(address, new Account(current.balance(), current.nonce() + 1));
    }

    public WorldState copy() {
        Map<Address, Long> balances = new LinkedHashMap<>();
        for (Map.Entry<Address, Account> entry : accounts.entrySet()) {
            balances.put(entry.getKey(), entry.getValue().balance());
        }
        WorldState copy = new WorldState(balances);

        for (Map.Entry<Address, Account> entry : accounts.entrySet()) {
            long currentNonce = entry.getValue().nonce();
            for (long i = 0; i < currentNonce; i++) {
                copy.incrementNonce(entry.getKey());
            }
        }

        return copy;
    }
}
