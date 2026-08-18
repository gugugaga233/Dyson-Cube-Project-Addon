package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * SafeBigDecimal - BigDecimal 安全解析与验证工具
 */
public final class SafeBigDecimal {
    public static final int MAX_PRECISION = 4096;
    public static final int MAX_ABS_SCALE = 4096;

    private SafeBigDecimal() {}

    public static BigDecimal requireValid(BigDecimal value) {
        if (value == null) throw new IllegalArgumentException("BigDecimal cannot be null");
        if (value.signum() < 0) throw new ArithmeticException("Energy exponent cannot be negative");
        if (value.precision() > MAX_PRECISION || Math.abs((long) value.scale()) > MAX_ABS_SCALE) {
            throw new ArithmeticException("BigDecimal exceeds safe precision");
        }
        if (value.toPlainString().getBytes(StandardCharsets.UTF_8).length > 16384) {
            throw new IllegalArgumentException("Serialized BigDecimal too large");
        }
        return value;
    }

    public static BigDecimal requirePositive(BigDecimal value) {
        requireValid(value);
        if (value.signum() <= 0) throw new IllegalArgumentException("Exponent must be positive");
        return value;
    }

    public static BigDecimal parse(String value) {
        if (value == null || value.isBlank()) return BigDecimal.ZERO;
        try { return requireValid(new BigDecimal(value.trim())); }
        catch (NumberFormatException e) { return BigDecimal.ZERO; }
    }
}

