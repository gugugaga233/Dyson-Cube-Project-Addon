package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.math.BigInteger;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import sonar.fluxnetworks.api.energy.BigNumber;

class AntimatterStarDataTest {
    private static final StarData ANTIMATTER_STAR = new StarData(
            "ANTI_G_TYPE", "Antimatter test star", 1.0, 1.0, 5778.0, 3.828e26, true);

    @Test
    void enormousWrappedCountDoesNotExpandAVisibleBigInteger() {
        AntimatterStarData data = new AntimatterStarData(ANTIMATTER_STAR);

        BigNumber result = assertTimeoutPreemptively(Duration.ofSeconds(1),
                () -> data.calculateOutputRateBigNumber(50.0, 100_000));

        assertFalse(result.isZero());
        assertEquals(1, result.compareTo(BigNumber.scientific("1", "100000")));
    }

    @Test
    void legacyStringSaveStillMigrates() {
        AntimatterStarData original = new AntimatterStarData(ANTIMATTER_STAR);
        original.setCurrentProgress(25.0);
        original.setCumulativeOutput(BigInteger.valueOf(123_456_789L));

        AntimatterStarData restored = AntimatterStarData.deserializeFromString(
                original.serializeToString());

        assertNotNull(restored);
        assertEquals(25.0, restored.getCurrentProgress());
        assertEquals(BigInteger.valueOf(123_456_789L), restored.getCumulativeOutput());
    }
}

