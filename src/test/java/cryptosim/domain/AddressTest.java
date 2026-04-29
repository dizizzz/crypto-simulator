package cryptosim.domain;

import cryptosim.crypto.KeyPairFactory;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AddressTest {

    @Test
    void fromPublicKey_returnsAddressWith40HexChars() {
        KeyPair pair = KeyPairFactory.generate();

        Address address = Address.fromPublicKey(pair.getPublic());

        assertEquals(40, address.hex().length());
    }

    @Test
    void fromPublicKey_samePublicKey_givesSameAddress() {
        // один публічний ключ → завжди одна адреса
        KeyPair pair = KeyPairFactory.generate();

        Address first = Address.fromPublicKey(pair.getPublic());
        Address second = Address.fromPublicKey(pair.getPublic());

        assertEquals(first, second);
    }

    @Test
    void fromPublicKey_differentPublicKeys_giveDifferentAddresses() {
        // Різні гаманці - різні адреси
        KeyPair alice = KeyPairFactory.generate();
        KeyPair bob = KeyPairFactory.generate();

        Address aliceAddress = Address.fromPublicKey(alice.getPublic());
        Address bobAddress = Address.fromPublicKey(bob.getPublic());

        assertNotEquals(aliceAddress, bobAddress);
    }

    @Test
    void constructor_invalidLength_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Address("too_short"));
        assertThrows(IllegalArgumentException.class,
                () -> new Address("a".repeat(50)));
        assertThrows(IllegalArgumentException.class,
                () -> new Address(null));
    }

}
