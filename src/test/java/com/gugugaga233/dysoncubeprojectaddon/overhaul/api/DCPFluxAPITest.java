package com.gugugaga233.dysoncubeprojectaddon.overhaul.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import sonar.fluxnetworks.api.energy.BigNumber;

class DCPFluxAPITest {

    @Test
    void ordinaryScientificValueDoesNotProjectAsInfinity() {
        BigNumber value = BigNumber.scientific("1", 3);

        assertEquals(1000.0D, DCPFluxAPI.toDouble(value));
        assertFalse(DCPFluxAPI.isInfinite(value));
    }

    @Test
    void valuesAboveDoubleRangeProjectToInfinity() {
        BigNumber value = BigNumber.scientific("1", 309);

        assertEquals(Double.POSITIVE_INFINITY, DCPFluxAPI.toDouble(value));
        assertTrue(DCPFluxAPI.isInfinite(value));
    }

    @Test
    void nonFiniteDoubleInputIsRejectedExplicitly() {
        assertThrows(IllegalArgumentException.class, () -> DCPFluxAPI.of(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> DCPFluxAPI.of(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> DCPFluxAPI.of(Double.NEGATIVE_INFINITY));
    }
}

