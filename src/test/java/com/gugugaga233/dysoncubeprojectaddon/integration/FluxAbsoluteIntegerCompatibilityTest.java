package com.gugugaga233.dysoncubeprojectaddon.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;

class FluxAbsoluteIntegerCompatibilityTest {

    @Test
    void byteConversionIsExactAcrossFluxDigitBoundaries() {
        for (int bitLength : new int[]{1, 7, 8, 62, 63, 64, 125, 126, 127, 511}) {
            BigInteger lowerMask = BigInteger.ONE.shiftLeft(bitLength - 1)
                    .subtract(BigInteger.ONE);
            BigInteger source = BigInteger.ONE.shiftLeft(bitLength - 1)
                    .or(BigInteger.valueOf(bitLength * 37L).and(lowerMask));

            AbsoluteInteger converted = com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8
                    .fromBigInteger(source);

            assertEquals(source, com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8
                    .toBigInteger(converted), "bit length " + bitLength);
        }
    }

    @Test
    void tenThousandDigitConversionIsFastAndExact() {
        BigInteger source = BigInteger.TEN.pow(10_000).subtract(BigInteger.valueOf(37L));

        AbsoluteInteger converted = assertTimeoutPreemptively(
                Duration.ofSeconds(1), () -> com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8.fromBigInteger(source));

        assertEquals(source, com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8.toBigInteger(converted));
    }

    @Test
    void layeredExponentCanBeAppliedWithoutDecimalExpansion() {
        BigInteger exponent = BigInteger.valueOf(2_488_126_415_135_961L).shiftLeft(33_138);
        AbsoluteInteger layered = com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8
                .fromBigInteger(exponent);

        sonar.fluxnetworks.api.energy.BigNumber actual =
                sonar.fluxnetworks.api.energy.BigNumber.valueOf(7L);
        com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8.addExponent(actual, layered);
        sonar.fluxnetworks.api.energy.BigNumber expected =
                sonar.fluxnetworks.api.energy.BigNumber.scientific("7", exponent.toString());

        assertEquals(0, expected.compareTo(actual));
    }

    @Test
    void smallConstantArithmeticStaysExactAcrossFluxDigits() {
        Random random = new Random(0xD150L);
        long[] factors = {2L, 10L, 301_029_995_663_981_195L};
        long[] divisors = {2L, 10L, 1_000_000_000_000_000_000L, Long.MAX_VALUE};
        for (int bits : new int[]{63, 64, 126, 511, 4095}) {
            BigInteger source = new BigInteger(bits, random).setBit(bits - 1);
            AbsoluteInteger value = com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8
                    .fromBigInteger(source);
            for (long factor : factors) {
                assertEquals(source.multiply(BigInteger.valueOf(factor)),
                        com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8.toBigInteger(
                                com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8
                                        .multiply(value, factor)));
            }
            for (long divisor : divisors) {
                BigInteger[] expected = source.divideAndRemainder(BigInteger.valueOf(divisor));
                var actual = com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8
                        .divideAndRemainder(value, divisor);
                assertEquals(expected[0], com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8
                        .toBigInteger(actual.quotient()));
                assertEquals(expected[1].longValueExact(), actual.remainder());
            }
        }
    }

    @Test
    void maximumLayerExponentScalingDoesNotExpandMillionsOfBits() {
        CompoundTag root = new CompoundTag();
        CompoundTag topLayer = new CompoundTag();
        topLayer.putLong("65535", Long.MAX_VALUE);
        root.put("layer3", topLayer);
        AbsoluteInteger value = AbsoluteInteger.fromTag(root);

        var scaled = assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
                com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8
                        .multiplyDivideExponent(value, 301_029_995_663_981_195L,
                                1_000_000_000_000_000_000L));

        assertEquals(false, scaled.exact());
        assertEquals(false, scaled.quotient().isZero());
    }

    @Test
    void maximumLayerCanBeAppliedDirectlyToBigNumber() {
        CompoundTag root = new CompoundTag();
        CompoundTag topLayer = new CompoundTag();
        topLayer.putLong("65535", Long.MAX_VALUE);
        root.put("layer3", topLayer);
        AbsoluteInteger value = AbsoluteInteger.fromTag(root);

        sonar.fluxnetworks.api.energy.BigNumber actual = assertTimeoutPreemptively(
                Duration.ofSeconds(1), () ->
                        com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8
                                .scientific("1", value));

        assertEquals(Long.MAX_VALUE, actual.getMaxCounter(65535));
    }
}

