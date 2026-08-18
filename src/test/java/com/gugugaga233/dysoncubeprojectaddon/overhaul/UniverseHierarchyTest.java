package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.math.BigInteger;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;

class UniverseHierarchyTest {

    @Test
    void levelEightBatchCapacityGrowsAtDecimalThresholds() {
        assertEquals("1000000000", UniverseHierarchy.effectiveBatchSize(
                AbsoluteInteger.parse("5000000000")).toCalculationString());
        assertEquals("50000000000", UniverseHierarchy.nextBatchThreshold(
                AbsoluteInteger.parse("5000000000")).toCalculationString());
        assertEquals("10000000000", UniverseHierarchy.effectiveBatchSize(
                AbsoluteInteger.parse("50000000000")).toCalculationString());
        assertEquals("100000000000", UniverseHierarchy.effectiveBatchSize(
                AbsoluteInteger.parse("500000000000")).toCalculationString());
    }

    @Test
    void hugeCounterBatchLookupDoesNotWalkEveryDecimalOrder() {
        AbsoluteInteger enormous = FluxMath8.fromBigInteger(BigInteger.ONE.shiftLeft(1_000_000));

        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            AbsoluteInteger batch = UniverseHierarchy.effectiveBatchSize(enormous);
            assertEquals(65_536, FluxMath8.decimalOrder(batch));
            assertEquals(true, UniverseHierarchy.isBatchSizeCapped(enormous));
        });
    }

    @Test
    void exactCounterUnlocksEveryHierarchyAtItsBoundary() {
        for (UniverseHierarchy.Level level : UniverseHierarchy.Level.values()) {
            AbsoluteInteger boundary = exact(level.unlockThreshold);
            assertEquals(level, UniverseHierarchy.getUnlockedLevel(boundary));
        }
    }

    @Test
    void repeatableHierarchyEffectDoublesCapacityAndStopsAtSafetyCap() {
        AbsoluteInteger wrapped = AbsoluteInteger.parse("5000000000");
        assertEquals("8000000000", UniverseHierarchy.effectiveBatchSize(
                wrapped, exact(3L)).toCalculationString());

        AbsoluteInteger enormousReward = AbsoluteInteger.parse("1000000000000000000000");
        AbsoluteInteger capped = UniverseHierarchy.effectiveBatchSize(wrapped, enormousReward);
        assertEquals(65_536, FluxMath8.decimalOrder(capped));
        assertEquals(true, UniverseHierarchy.isBatchSizeCapped(wrapped, enormousReward));
    }

    @Test
    void expectedCostsUseTheConfiguredTierPool() {
        assertEquals(1_000L, UniverseHierarchy.expectedCostPermille(UniverseHierarchy.Level.SINGLE_STAR));
        assertEquals(1_000L, UniverseHierarchy.expectedCostPermille(UniverseHierarchy.Level.STAR_CLUSTER));
        assertEquals(1_010L, UniverseHierarchy.expectedCostPermille(UniverseHierarchy.Level.GALAXY));
        assertEquals(1_029L, UniverseHierarchy.expectedCostPermille(UniverseHierarchy.Level.STAR_RIVER));
        assertEquals(1_071L, UniverseHierarchy.expectedCostPermille(UniverseHierarchy.Level.STAR_HUB));
        assertEquals(1_143L, UniverseHierarchy.expectedCostPermille(UniverseHierarchy.Level.STAR_DOME));
        assertEquals(1_350L, UniverseHierarchy.expectedCostPermille(UniverseHierarchy.Level.STAR_DOMAIN));
        assertEquals(1_845L, UniverseHierarchy.expectedCostPermille(UniverseHierarchy.Level.STAR_UNIVERSE));
    }

    @Test
    void countersAboveLongStillResolveToTheHighestHierarchy() {
        AbsoluteInteger enormous = AbsoluteInteger.parse("9".repeat(10_000));

        assertEquals(UniverseHierarchy.Level.STAR_UNIVERSE,
                UniverseHierarchy.getUnlockedLevel(enormous));
        assertEquals(0L, UniverseHierarchy.nextUnlockThreshold(enormous));
    }

    private static AbsoluteInteger exact(long value) {
        return AbsoluteInteger.parse(Long.toString(value));
    }
}

