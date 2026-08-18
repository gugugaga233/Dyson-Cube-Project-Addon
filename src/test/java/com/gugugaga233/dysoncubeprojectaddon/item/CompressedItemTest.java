package com.gugugaga233.dysoncubeprojectaddon.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import sonar.fluxnetworks.api.energy.BigNumber;

class CompressedItemTest {

    @Test
    void cappedMassMatchesDirectCalculationBelowCap() {
        BigDecimal result = CompressionMath.scaleMassCapped(
                new BigDecimal("0.015625"), BigInteger.valueOf(5), new BigDecimal("100000"));

        assertEquals(0, new BigDecimal("1562.500000").compareTo(result));
    }

    @Test
    void baseMassBelowOneDoesNotSaturateTooEarly() {
        BigDecimal result = CompressionMath.scaleMassCapped(
                new BigDecimal("0.0005"), BigInteger.valueOf(3), BigDecimal.ONE);

        assertEquals(0, new BigDecimal("0.5").compareTo(result));
    }

    @Test
    void levelTenThousandUsesAnExactDecimalExponent() {
        var multiplier = CompressedItem.getFluxProgressMultiplier(BigInteger.valueOf(10_000L));

        assertEquals(0, BigNumber.scientific("1", "9999").compareTo(multiplier));
    }

    @Test
    void enormousCompressionLevelSaturatesQuickly() {
        BigInteger exponent = BigInteger.TEN.pow(1000);

        BigDecimal result = assertTimeoutPreemptively(Duration.ofSeconds(1),
                () -> CompressionMath.scaleMassCapped(
                        new BigDecimal("0.015625"), exponent, new BigDecimal("1E100")));

        assertEquals(0, new BigDecimal("1E100").compareTo(result));
    }
}

