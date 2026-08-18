package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import java.math.BigDecimal;
import java.math.BigInteger;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;

/** Parses reward counts without allowing values larger than Flux 8 can store. */
public final class CosmicHeartRewardCount {
    /** Four Flux layers of 65,536 base-2^63 digits, with a small conversion margin. */
    public static final int MAX_DECIMAL_DIGITS = 4_971_500;
    /** Limits a packet made from plain decimal digits while keeping scientific notation short. */
    public static final int MAX_INPUT_CHARACTERS = 100_000;

    private CosmicHeartRewardCount() {
    }

    public static BigInteger parseBigInteger(String input) {
        BigDecimal decimal = parseDecimal(input);
        try {
            return decimal.toBigIntegerExact();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("reward count must be an integer", exception);
        }
    }

    public static AbsoluteInteger parseAbsoluteInteger(String input) {
        return FluxMath8.fromBigInteger(parseBigInteger(input));
    }

    private static BigDecimal parseDecimal(String input) {
        if (input == null || input.isBlank() || input.length() > MAX_INPUT_CHARACTERS) {
            throw new IllegalArgumentException("invalid reward count");
        }
        final BigDecimal decimal;
        try {
            decimal = new BigDecimal(input.strip());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("invalid reward count", exception);
        }
        if (decimal.signum() < 0) {
            throw new IllegalArgumentException("negative reward count");
        }
        if (decimal.signum() == 0) return decimal;

        long expandedDigits = decimal.precision() - (long) decimal.scale();
        if (expandedDigits > MAX_DECIMAL_DIGITS) {
            throw new IllegalArgumentException("reward count exceeds Flux capacity");
        }
        return decimal;
    }
}
