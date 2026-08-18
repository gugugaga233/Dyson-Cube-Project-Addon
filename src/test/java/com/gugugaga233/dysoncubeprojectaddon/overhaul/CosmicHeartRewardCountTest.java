package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class CosmicHeartRewardCountTest {
    @Test
    void acceptsTheLargeScientificNotationUsedByTheRewardScreen() {
        BigInteger value = CosmicHeartRewardCount.parseBigInteger("8.82253374E199979");

        assertEquals(new BigInteger("882253374").multiply(BigInteger.TEN.pow(199971)), value);
    }

    @Test
    void smallerMantissaDoesNotCoverTheRequiredCount() {
        BigInteger required = CosmicHeartRewardCount.parseBigInteger("8.82253374E199979");
        BigInteger entered = CosmicHeartRewardCount.parseBigInteger("8E199979");

        assertEquals(-1, entered.compareTo(required));
    }

    @Test
    void rejectsValuesBeyondFluxCapacity() {
        assertThrows(IllegalArgumentException.class,
                () -> CosmicHeartRewardCount.parseBigInteger("1E4971500"));
    }
}
