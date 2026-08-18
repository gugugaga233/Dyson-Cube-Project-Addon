package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import java.math.BigInteger;

/** Integer-only deterministic aggregation for very large independent event counts. */
public final class BigProbabilitySampler {
    private BigProbabilitySampler() {
    }

    public static BigInteger sample(BigInteger attempts, long numerator, long denominator,
                                    BigInteger sequenceStart, long salt) {
        if (attempts == null || attempts.signum() <= 0 || numerator <= 0 || denominator <= 0) {
            return BigInteger.ZERO;
        }
        if (numerator >= denominator) return attempts;

        BigInteger divisor = BigInteger.valueOf(denominator);
        BigInteger offset = deterministicOffset(sequenceStart, salt, divisor);
        return attempts.multiply(BigInteger.valueOf(numerator)).add(offset).divide(divisor);
    }

    private static BigInteger deterministicOffset(BigInteger sequenceStart, long salt,
                                                  BigInteger divisor) {
        BigInteger start = sequenceStart == null ? BigInteger.ZERO : sequenceStart;
        byte[] bytes = start.toByteArray();
        long hash = 0xcbf29ce484222325L ^ salt;
        for (byte value : bytes) {
            hash ^= value & 0xffL;
            hash *= 0x100000001b3L;
        }
        hash ^= hash >>> 33;
        hash *= 0xff51afd7ed558ccdL;
        hash ^= hash >>> 33;
        return unsignedLong(hash).mod(divisor);
    }

    private static BigInteger unsignedLong(long value) {
        BigInteger result = BigInteger.valueOf(value & Long.MAX_VALUE);
        return value < 0 ? result.setBit(Long.SIZE - 1) : result;
    }
}

