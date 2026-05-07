package cryptosim.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cryptosim.crypto.KeyPairFactory;
import cryptosim.crypto.Signer;

import java.security.KeyPair;
import java.security.PublicKey;

public final class Wallet {

    private final KeyPair keyPair;
    private final Address address;

    private Wallet(KeyPair keyPair) {
        this.keyPair = keyPair;
        this.address = Address.fromPublicKey(keyPair.getPublic());
    }

    public static Wallet create() {
        KeyPair keyPair = KeyPairFactory.generate();
        return new Wallet(keyPair);
    }

    public Address getAddress() {
        return address;
    }

    @JsonIgnore
    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public Transaction createTransaction(Address to, long amount, long fee, long nonce) {
        Transaction unsigned = new Transaction(
                this.address, to, amount, fee, nonce,
                keyPair.getPublic(), new byte[]{0}
        );

        byte[] signature = Signer.sign(unsigned.dataToSign(), keyPair.getPrivate());

        return new Transaction(
                this.address, to, amount, fee, nonce,
                keyPair.getPublic(), signature
        );
    }
}
