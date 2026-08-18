package com.gugugaga233.dysoncubeprojectaddon.overhaul.mining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.math.BigInteger;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;
import sonar.fluxnetworks.api.energy.BigNumber;

class OreProcessingMathTest {
    @Test
    void preservesExactTwoHundredFortyDigitBatch() {
        BigInteger raw = new BigInteger("9".repeat(240));
        AbsoluteInteger input = com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8.fromBigInteger(raw);

        assertEquals(raw.multiply(BigInteger.valueOf(4)),
                com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8.toBigInteger(
                        OreProcessingMath.outputAmount(input, 1)));
        assertEquals(raw, com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8.toBigInteger(
                com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8.floorDivide(
                        OreProcessingMath.energyCost(input, 12_000), 12_000)));
    }

    @Test
    void recipeOutputCountIsAppliedBeforeFourfoldYield() {
        AbsoluteInteger input = AbsoluteInteger.parse("25");
        assertEquals(new BigInteger("300"),
                com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8.toBigInteger(
                        OreProcessingMath.outputAmount(input, 3)));
    }

    @Test
    void hugeEnergyCostAvoidsExpandedDecimalNormalization() {
        AbsoluteInteger input = AbsoluteInteger.parse("1E100000");

        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> assertEquals(0,
                BigNumber.scientific("1.2", "100004")
                        .compareTo(OreProcessingMath.energyCost(input, 12_000))));
    }
}

