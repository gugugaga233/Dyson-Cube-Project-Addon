package com.gugugaga233.dysoncubeprojectaddon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import sonar.fluxnetworks.api.energy.BigNumber;

class DysonCubeProjectCommandNumberTest {

    @Test
    void parsesScientificNotationBeyondPrimitiveAndDoubleRanges() {
        DysonCubeProject.ParsedBigNumber parsed = DysonCubeProject.parseStructureNumber("1.25e1000", false);

        assertEquals(0, BigNumber.scientific("1.25", "1000").compareTo(parsed.value()));
        assertFalse(parsed.negative());
    }

    @Test
    void parsesNegativeDeltaWithoutStoringNegativeBigNumber() {
        DysonCubeProject.ParsedBigNumber parsed = DysonCubeProject.parseStructureNumber("-3e500", true);

        assertEquals(0, BigNumber.scientific("3", "500").compareTo(parsed.value()));
        assertTrue(parsed.negative());
    }

    @Test
    void rejectsNegativeSetAndFractionalItemCounts() {
        assertThrows(IllegalArgumentException.class,
                () -> DysonCubeProject.parseStructureNumber("-1", false));
        assertThrows(IllegalArgumentException.class,
                () -> DysonCubeProject.parseStructureNumber("1.5e0", false));
    }

    @Test
    void normalizesSmallCoefficientAndRejectsMultipleExponentMarkers() {
        DysonCubeProject.ParsedBigNumber parsed = DysonCubeProject.parseStructureNumber("0.001e3", false);

        assertEquals(0, BigNumber.valueOf(1L).compareTo(parsed.value()));
        assertThrows(IllegalArgumentException.class,
                () -> DysonCubeProject.parseStructureNumber("1e2e3", false));
        assertThrows(IllegalArgumentException.class,
                () -> DysonCubeProject.parseStructureNumber("1e2E3", false));
    }

    @Test
    void parsesArbitraryCompressionLevelsWithoutPrimitiveConversion() {
        assertEquals(BigInteger.valueOf(10_000L),
                DysonCubeProject.parseCompressionLevel("10000"));

        String enormousLevel = "9".repeat(10_000);
        assertEquals(10_000, DysonCubeProject.parseCompressionLevel(enormousLevel).toString().length());
    }

    @Test
    void rejectsInvalidCompressionLevels() {
        assertThrows(IllegalArgumentException.class,
                () -> DysonCubeProject.parseCompressionLevel("0"));
        assertThrows(IllegalArgumentException.class,
                () -> DysonCubeProject.parseCompressionLevel("-1"));
        assertThrows(IllegalArgumentException.class,
                () -> DysonCubeProject.parseCompressionLevel("1e3"));
        assertThrows(IllegalArgumentException.class,
                () -> DysonCubeProject.parseCompressionLevel("9".repeat(10_001)));
    }
}

