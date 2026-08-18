package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.math.BigInteger;
import java.time.Duration;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;
import sonar.fluxnetworks.api.energy.BigNumber;

class FluxMath8Test {

    @Test
    void sparseDecimalOrderTracksHugeBinaryValues() {
        AbsoluteInteger value = FluxMath8.fromBigInteger(BigInteger.TEN.pow(10_000).multiply(
                BigInteger.valueOf(7L)));

        assertEquals(10_000, FluxMath8.decimalOrder(value));
    }

    @Test
    void addingSmallBatchToHugeCounterDoesNotScanTheCounterMagnitude() {
        AbsoluteInteger original = FluxMath8.fromBigInteger(BigInteger.ONE.shiftLeft(1_000_000));
        AbsoluteInteger target = original.copy();
        AbsoluteInteger batch = AbsoluteInteger.parse("1000000000");

        assertTimeoutPreemptively(Duration.ofSeconds(1),
                () -> FluxMath8.addInPlace(target, batch));

        assertEquals(0, batch.compareTo(FluxMath8.subtract(target, original)));
    }

    @Test
    void hugeAbsoluteIntegerConvertsToBigNumberWithinOneSecond() {
        BigInteger exact = BigInteger.TEN.pow(10_000);
        AbsoluteInteger value = FluxMath8.fromBigInteger(exact);

        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            BigNumber converted = FluxMath8.toBigNumber(value);
            assertEquals(0, converted.compareTo(BigNumber.valueOf(exact)));
        });
    }

    @Test
    void compactTagRoundTripsHugeCountsWithoutFluxLayerKeys() {
        AbsoluteInteger source = FluxMath8.fromBigInteger(BigInteger.TEN.pow(100_000));
        CompoundTag compact = FluxMath8.toCompactTag(source);
        AbsoluteInteger restored = FluxMath8.fromCompactTag(compact);

        assertEquals(0, source.compareTo(restored));
        assertEquals(1, compact.getInt("compactAbsoluteIntegerVersion"));
        assertEquals(0, compact.getCompound("layer0").getAllKeys().size());
        assertEquals(true, compact.getByteArray("digits").length < 100_000);
    }
}
