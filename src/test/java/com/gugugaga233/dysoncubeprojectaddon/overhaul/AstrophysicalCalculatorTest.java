package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;
import sonar.fluxnetworks.api.energy.BigNumber;

class AstrophysicalCalculatorTest {

    @Test
    void exactPrimordialQiMultiplierSupportsExponentAboveDoubleRange() {
        BigNumber actual = AstrophysicalCalculator.calculateFinalPower(
                BigNumber.valueOf(3L), 7, 0, BigNumber.scientific("1", "1000"), 1.0);

        assertEquals(0, BigNumber.scientific("3", "1007").compareTo(actual));
    }

    @Test
    void legacyDoubleOverloadRetainsFiniteCallerBehavior() {
        BigNumber actual = AstrophysicalCalculator.calculateFinalPower(
                BigNumber.valueOf(2L), 3, 2, 10.0, 2.0);

        assertEquals(0, BigNumber.valueOf(160_000L).compareTo(actual));
    }

    @Test
    void exactWrappedCountBecomesAnExponentWithoutExpandingItsValue() {
        AbsoluteInteger wrapped = AbsoluteInteger.parse("9".repeat(10_000));

        BigNumber actual = assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
                AstrophysicalCalculator.calculateFinalPower(
                        BigNumber.valueOf(3L), wrapped, new AbsoluteInteger(),
                        BigNumber.valueOf(1L), 1.0D));

        assertEquals(0, FluxMath8.scientific("3", wrapped).compareTo(actual));
    }

    @Test
    void exactDarkMatterResonanceUsesLogarithmicConversion() {
        AbsoluteInteger resonance = AbsoluteInteger.parse("1000000000000000000000000");

        BigNumber actual = assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
                AstrophysicalCalculator.calculateFinalPower(
                        BigNumber.valueOf(1L), new AbsoluteInteger(), resonance,
                        BigNumber.valueOf(1L), 1.0D));

        assertEquals(0, actual.compareTo(BigNumber.valueOf(1L)) > 0 ? 0 : 1);
    }

    @Test
    void maximumLayerDarkMatterResonanceRecalculatesImmediately() {
        CompoundTag root = new CompoundTag();
        CompoundTag topLayer = new CompoundTag();
        topLayer.putLong("65535", Long.MAX_VALUE);
        root.put("layer3", topLayer);
        AbsoluteInteger resonance = AbsoluteInteger.fromTag(root);

        BigNumber actual = assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
                AstrophysicalCalculator.calculateFinalPower(
                        BigNumber.valueOf(1L), new AbsoluteInteger(), resonance,
                        BigNumber.valueOf(1L), 1.0D));

        assertEquals(0, actual.compareTo(BigNumber.valueOf(1L)) > 0 ? 0 : 1);
    }
}

