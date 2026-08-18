package com.gugugaga233.dysoncubeprojectaddon.item;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

final class CompressionMath {
    private static final BigDecimal TEN = BigDecimal.TEN;

    private CompressionMath() {
    }

    static BigDecimal scaleMassCapped(BigDecimal baseMass, BigInteger exponent, BigDecimal cap) {
        if (baseMass.signum() <= 0 || cap.signum() <= 0) return BigDecimal.ZERO;
        if (baseMass.compareTo(cap) >= 0) return cap;
        if (exponent.signum() <= 0) return baseMass;

        MathContext limitContext = new MathContext(
                Math.max(34, Math.max(baseMass.precision(), cap.precision()) + 8),
                RoundingMode.CEILING);
        BigDecimal multiplierLimit = cap.divide(baseMass, limitContext);
        BigDecimal multiplier = BigDecimal.ONE;
        BigDecimal factor = TEN;
        BigInteger remaining = exponent;
        while (remaining.signum() > 0) {
            if (remaining.testBit(0)) {
                multiplier = multiplyCapped(multiplier, factor, multiplierLimit);
                if (multiplier.compareTo(multiplierLimit) >= 0) return cap;
            }
            remaining = remaining.shiftRight(1);
            if (remaining.signum() > 0) {
                factor = multiplyCapped(factor, factor, multiplierLimit);
                if (factor.compareTo(multiplierLimit) >= 0) return cap;
            }
        }
        return baseMass.multiply(multiplier).min(cap);
    }

    private static BigDecimal multiplyCapped(BigDecimal left, BigDecimal right, BigDecimal cap) {
        if (left.compareTo(cap) >= 0 || right.compareTo(cap) >= 0) return cap;
        return left.multiply(right).min(cap);
    }
}

