package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class BigProbabilitySamplerTest {
    @Test
    void levelTenThousandScaleDoesNotOverflowOrCollapseToZero() {
        BigInteger attempts = BigInteger.TEN.pow(10_000);
        BigInteger hits = BigProbabilitySampler.sample(
                attempts, 5L, 1_000_000_000L, BigInteger.ZERO, 17L);

        assertTrue(hits.signum() > 0);
        assertEquals(BigInteger.TEN.pow(10_000).multiply(BigInteger.valueOf(5L))
                .divide(BigInteger.valueOf(1_000_000_000L)), hits);
    }

    @Test
    void sameSequenceProducesSamePseudoRandomRounding() {
        BigInteger start = new BigInteger("123456789012345678901234567890");
        assertEquals(
                BigProbabilitySampler.sample(BigInteger.valueOf(12_345L), 50L, 1_000_000L, start, 99L),
                BigProbabilitySampler.sample(BigInteger.valueOf(12_345L), 50L, 1_000_000L, start, 99L));
    }
}

