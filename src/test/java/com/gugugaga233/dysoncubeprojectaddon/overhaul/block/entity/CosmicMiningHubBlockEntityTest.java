package com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;
import sonar.fluxnetworks.api.energy.BigNumber;

class CosmicMiningHubBlockEntityTest {

    @Test
    void comparesHugeExpandedMassWithoutConstructingHugeBigNumber() {
        BigDecimal limit = new BigDecimal("3.4E100000").subtract(BigDecimal.ONE);

        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            assertFalse(CosmicMiningHubBlockEntity.reachesMassLimit(
                    BigNumber.scientific("1", "99999"), limit));
            assertFalse(CosmicMiningHubBlockEntity.reachesMassLimit(
                    BigNumber.scientific("3", "100000"), limit));
            assertTrue(CosmicMiningHubBlockEntity.reachesMassLimit(
                    BigNumber.scientific("4", "100000"), limit));
        });
    }

    @Test
    void convertsHugeScientificOutputWithoutExpandingItsDigits() {
        BigDecimal output = new BigDecimal("3E100000");

        AbsoluteInteger converted = assertTimeoutPreemptively(Duration.ofSeconds(1),
                () -> CosmicMiningHubBlockEntity.floorToAbsoluteInteger(output));

        BigInteger expected = BigInteger.valueOf(3L).multiply(BigInteger.TEN.pow(100_000));
        assertEquals(0, com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8
                .fromBigInteger(expected).compareTo(converted));
    }
    @Test
    void comparesHugeMassLimitWithUnitMassUsingExponentFastPath() {
        BigDecimal remainingMass = new BigDecimal("1.25E100000");
        BigDecimal unitMass = BigDecimal.ONE;

        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            assertFalse(CosmicMiningHubBlockEntity.reachesMassLimit(
                    BigNumber.scientific("1", "99999"), remainingMass, unitMass));
            assertFalse(CosmicMiningHubBlockEntity.reachesMassLimit(
                    BigNumber.scientific("1", "100000"), remainingMass, unitMass));
            assertTrue(CosmicMiningHubBlockEntity.reachesMassLimit(
                    BigNumber.scientific("2", "100000"), remainingMass, unitMass));

            BigDecimal boundaryMass = new BigDecimal("3E100000");
            BigDecimal doubleUnitMass = new BigDecimal("2");
            assertFalse(CosmicMiningHubBlockEntity.reachesMassLimit(
                    BigNumber.scientific("1", "100000"), boundaryMass, doubleUnitMass));
            assertTrue(CosmicMiningHubBlockEntity.reachesMassLimit(
                    BigNumber.scientific("2", "100000"), boundaryMass, doubleUnitMass));
        });
    }
}
