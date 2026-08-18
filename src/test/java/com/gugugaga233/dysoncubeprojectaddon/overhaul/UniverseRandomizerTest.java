package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;

class UniverseRandomizerTest {

    @Test
    void darkMatterExpectationBeforeAntimatterUnlockIsExactForWholeProbabilityBlocks() {
        AbsoluteInteger hits = UniverseRandomizer.sampleDarkMatterHits(
                absolute(1_000_000L), absolute(3_000L));

        assertEquals(0, absolute(100L).compareTo(hits));
    }

    @Test
    void darkMatterExpectationIncludesTheEarlierAntimatterRoll() {
        AbsoluteInteger hits = UniverseRandomizer.sampleDarkMatterHits(
                absolute(1_000_000_000_000L), absolute(5_001L));

        assertEquals(0, absolute(99_995_000L).compareTo(hits));
    }

    @Test
    void expectedMaterialPoolChangesAtEveryRuleBoundary() {
        UniverseRandomizer.AverageStructureCost beforeBlackHoles = costAt(999L);
        UniverseRandomizer.AverageStructureCost afterBlackHoles = costAt(1_000L);
        UniverseRandomizer.AverageStructureCost afterDarkMatter = costAt(3_000L);
        UniverseRandomizer.AverageStructureCost afterAntimatter = costAt(5_000L);
        UniverseRandomizer.AverageStructureCost afterPlanck = costAt(10_000L);

        assertNotEqual(beforeBlackHoles, afterBlackHoles);
        assertNotEqual(afterBlackHoles, afterDarkMatter);
        assertNotEqual(afterDarkMatter, afterAntimatter);
        assertNotEqual(afterAntimatter, afterPlanck);
    }

    private static UniverseRandomizer.AverageStructureCost costAt(long wrapped) {
        return UniverseRandomizer.averageStructureCost(absolute(wrapped));
    }

    private static void assertNotEqual(UniverseRandomizer.AverageStructureCost first,
                                       UniverseRandomizer.AverageStructureCost second) {
        assertEquals(false, first.equals(second));
    }

    private static AbsoluteInteger absolute(long value) {
        return AbsoluteInteger.parse(Long.toString(value));
    }
}

