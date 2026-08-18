package com.gugugaga233.dysoncubeprojectaddon.overhaul.mining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import sonar.fluxnetworks.api.energy.BigNumber;

class LaserPowerSystemTest {

    @Test
    void parsesScientificInputDirectlyIntoBigNumber() {
        LaserPowerSystem system = new LaserPowerSystem();
        system.setInput("2.5E30");

        assertEquals(0, BigNumber.scientific("2.5", "30").compareTo(system.input()));
        assertEquals("30", system.zeroCount());
        assertEquals("97.0", system.efficiencyPercent());
    }

    @Test
    void handlesVeryLargeDecimalExponentWithoutExpandingTheValue() {
        LaserPowerSystem system = new LaserPowerSystem();
        system.setInput("1e1000000");

        assertEquals(0, BigNumber.scientific("1", "1000000").compareTo(system.input()));
        assertEquals("1000000", system.zeroCount());
        assertEquals("0.1", system.efficiencyPercent());
        assertEquals(10_000, system.boundedEffectivePower(10_000));
    }

    @Test
    void keepsMultiThousandDigitExponentInBigNumberArrays() {
        LaserPowerSystem system = new LaserPowerSystem();
        String exponent = "9".repeat(5_000);

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> system.setInput("1e" + exponent));

        assertEquals(1, system.input().compareTo(BigNumber.scientific("1", "1" + "0".repeat(4_999))));
        assertEquals("0.1", system.efficiencyPercent());
        assertEquals(576, system.boundedEffectivePower(576));
    }

    @Test
    void effectivePowerUsesBigNumberAtMinimumEfficiency() {
        LaserPowerSystem system = new LaserPowerSystem();
        system.setInput("1e1000");

        assertEquals(0, BigNumber.scientific("1", "997").compareTo(system.effectivePower()));
    }

    @Test
    void acceptsNegativeExponentOnlyWhenResultIsStillAnInteger() {
        LaserPowerSystem system = new LaserPowerSystem();
        system.setInput("1000e-2");

        assertEquals(0, BigNumber.valueOf(10L).compareTo(system.input()));
        assertThrows(IllegalArgumentException.class, () -> system.setInput("1e-1"));
    }

    @Test
    void rejectsInvalidZeroNegativeAndFractionalValues() {
        LaserPowerSystem system = new LaserPowerSystem();

        assertThrows(IllegalArgumentException.class, () -> system.setInput("0"));
        assertThrows(IllegalArgumentException.class, () -> system.setInput("-1"));
        assertThrows(IllegalArgumentException.class, () -> system.setInput("2.5"));
        assertThrows(IllegalArgumentException.class, () -> system.setInput("1e"));
    }

    @Test
    void outputProjectionOccursOnlyAfterApplyingSmallBufferLimit() {
        LaserPowerSystem system = new LaserPowerSystem();
        system.setInput("15");

        assertEquals(14, system.boundedEffectivePower(576));
        assertEquals(10, system.boundedEffectivePower(10));
    }
}

