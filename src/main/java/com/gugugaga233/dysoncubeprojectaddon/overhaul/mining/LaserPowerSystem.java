package com.gugugaga233.dysoncubeprojectaddon.overhaul.mining;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sonar.fluxnetworks.api.energy.BigNumber;

public final class LaserPowerSystem {
    private static final Pattern POWER_PATTERN = Pattern.compile(
            "^\\+?(?:(\\d+)(?:\\.(\\d*))?|\\.(\\d+))(?:[eE]([+-]?\\d+))?$");
    private static final BigNumber ONE = BigNumber.valueOf(1L);
    private static final BigNumber MINIMUM_EFFICIENCY_THRESHOLD = BigNumber.scientific("1", "999");

    private BigNumber input = BigNumber.valueOf(1L);
    private String inputText = "1";

    public BigNumber input() {
        return input.deepCopy();
    }

    public String inputText() {
        return inputText;
    }

    public void setInput(String value) {
        String normalized = value == null ? "" : value.trim();
        Matcher matcher = POWER_PATTERN.matcher(normalized);
        if (!matcher.matches()) throw invalidPower();

        String integerPart = matcher.group(1);
        String fractionPart = integerPart == null ? matcher.group(3) : matcher.group(2);
        if (integerPart == null) integerPart = "";
        if (fractionPart == null) fractionPart = "";
        String digits = integerPart + fractionPart;
        int firstNonZero = firstNonZero(digits);
        if (firstNonZero == digits.length()) throw invalidPower();

        String exponent = normalizeSignedInteger(matcher.group(4));
        int trailingZeros = trailingZeroCount(digits);
        int decimalPlaces = fractionPart.length();
        BigNumber parsed;
        if (exponent.startsWith("-")) {
            int negativeExponent = parseBoundedNegativeExponent(exponent, trailingZeros - decimalPlaces);
            BigDecimal mantissa = new BigDecimal(integerPart + "." + fractionPart);
            parsed = new BigNumber(mantissa.scaleByPowerOfTen(-negativeExponent));
        } else {
            if (compareUnsignedDecimal(exponent, Integer.toString(decimalPlaces - trailingZeros)) < 0) {
                throw invalidPower();
            }
            String mantissa = integerPart + (fractionPart.isEmpty() ? "" : "." + fractionPart);
            parsed = BigNumber.scientific(mantissa, exponent);
        }
        parsed.normalize();
        if (parsed.compareTo(ONE) < 0) throw invalidPower();

        input = parsed;
        inputText = normalized.startsWith("+") ? normalized.substring(1) : normalized;
    }

    public void restore(BigNumber stored, String storedInputText) {
        if (stored == null || stored.compareTo(ONE) < 0) {
            setInput("1");
            return;
        }
        if (storedInputText != null && !storedInputText.isBlank()) {
            try {
                setInput(storedInputText);
                if (input.compareTo(stored) == 0) return;
            } catch (IllegalArgumentException ignored) {
            }
        }
        input = stored.deepCopy();
        inputText = stripFe(stored.toScientificString());
    }

    public String zeroCount() {
        return input.toExponentCalculationString();
    }

    public BigDecimal efficiency() {
        return BigDecimal.valueOf(efficiencyPermille(), 3);
    }

    public BigNumber effectivePower() {
        BigNumber result = input.deepCopy();
        result.multiply(new BigNumber(efficiency()));
        return result;
    }

    /** Whole output units represented without applying an ordinary integer cap. */
    public BigNumber effectiveOutput() {
        BigNumber effective = effectivePower();
        if (effective.compareTo(ONE) < 0) return new BigNumber(0);
        if (effective.getExactEnergy().isPresent()) {
            return BigNumber.valueOf(effective.getExactEnergy().orElseThrow());
        }
        return effective;
    }

    public int boundedEffectivePower(int maximum) {
        if (maximum <= 0) return 0;
        BigNumber effective = effectivePower();
        BigNumber limit = BigNumber.valueOf(maximum);
        return effective.compareTo(limit) >= 0 ? maximum : (int) effective.getEnergyStoredLong();
    }

    public String efficiencyPercent() {
        return efficiency().movePointRight(2).setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    private int efficiencyPermille() {
        if (input.compareTo(MINIMUM_EFFICIENCY_THRESHOLD) >= 0) return 1;
        int exponent = input.toBigIntegerExponent().intValueExact();
        return Math.max(1, 1000 - exponent);
    }

    private static int parseBoundedNegativeExponent(String exponent, int availableDecimalZeros) {
        if (availableDecimalZeros <= 0) throw invalidPower();
        String magnitude = exponent.substring(1);
        String limit = Integer.toString(availableDecimalZeros);
        if (compareUnsignedDecimal(magnitude, limit) > 0) throw invalidPower();
        return Integer.parseInt(magnitude);
    }

    private static String normalizeSignedInteger(String exponent) {
        if (exponent == null || exponent.isEmpty()) return "0";
        boolean negative = exponent.charAt(0) == '-';
        int start = exponent.charAt(0) == '+' || negative ? 1 : 0;
        while (start < exponent.length() && exponent.charAt(start) == '0') start++;
        String magnitude = start == exponent.length() ? "0" : exponent.substring(start);
        return negative && !magnitude.equals("0") ? "-" + magnitude : magnitude;
    }

    private static int compareUnsignedDecimal(String left, String right) {
        if (right.startsWith("-")) return 1;
        int leftStart = firstNonZero(left);
        int rightStart = firstNonZero(right);
        int leftLength = left.length() - leftStart;
        int rightLength = right.length() - rightStart;
        if (leftLength != rightLength) return Integer.compare(leftLength, rightLength);
        return left.substring(leftStart).compareTo(right.substring(rightStart));
    }

    private static int firstNonZero(String value) {
        int index = 0;
        while (index < value.length() && value.charAt(index) == '0') index++;
        return index;
    }

    private static int trailingZeroCount(String value) {
        int index = value.length();
        while (index > 0 && value.charAt(index - 1) == '0') index--;
        return value.length() - index;
    }

    private static String stripFe(String value) {
        return value.endsWith(" FE") ? value.substring(0, value.length() - 3) : value;
    }

    private static IllegalArgumentException invalidPower() {
        return new IllegalArgumentException("Laser power must be a positive integer in decimal or scientific notation");
    }
}

