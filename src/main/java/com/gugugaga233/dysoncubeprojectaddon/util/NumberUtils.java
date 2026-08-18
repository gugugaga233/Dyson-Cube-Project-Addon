/*
 * Decompiled with CFR 0.152.
 */
package com.gugugaga233.dysoncubeprojectaddon.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;
import sonar.fluxnetworks.api.energy.BigNumber;

public class NumberUtils {
    public static final int DISPLAY_SIGNIFICANT_DIGITS = 10;
    private static final int MAX_PLAIN_DISPLAY_LENGTH = 18;
    private static final int MAX_LITERAL_EXPONENT_LENGTH = 9;
    private static final int MAX_NESTED_EXPONENT_DEPTH = 3;
    private static final double LOG10_TWO = 0.3010299956639812D;
    private static final Pattern ABSOLUTE_CALCULATION =
            Pattern.compile("^(\\d+) x 2\\^([0-9]+)");
    private static final Pattern DECIMAL_AMOUNT =
            Pattern.compile("^([+-]?)(\\d+)(?:\\.(\\d+))?(?:[eE]([+-]?\\d+))?$");
    private static final MathContext COMPACT_BIG_NUMBER_CONTEXT =
            new MathContext(DISPLAY_SIGNIFICANT_DIGITS, RoundingMode.HALF_UP);
    private static final String[] suffixes = new String[]{"", "K", "M", "B", "T", "Q", "Qi", "Sx", "Sp", "O"};
    private static final String[] AE_POSTFIXES = new String[]{"", "K", "M", "G", "T", "P", "E"};
    private static DecimalFormat formatterWithUnits = new DecimalFormat("####0.#");

    public static String getFormatedBigNumber(double value) {
        if (value < 1000.0) {
            return String.valueOf((int)Math.ceil(value));
        }
        int exp = (int)(Math.log(value) / Math.log(1000.0));
        if (exp >= suffixes.length) {
            return "Err";
        }
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        return decimalFormat.format(value / Math.pow(1000.0, exp)) + suffixes[exp];
    }

    /** Formats Flux values without reducing their exponent to double or long. */
    public static String getFormatedBigNumber(BigNumber value) {
        return getCompactBigNumber(value);
    }

    /** Formats an exact amount like AE2's item-slot renderer: K, M, G, T, P and E. */
    public static String getAeCompactAmount(AbsoluteInteger value) {
        if (value == null || value.isZero()) return "0";
        return getAeCompactAmount(value.toCalculationString());
    }

    /**
     * Formats a decimal or Flux calculation string for a small AE2 slot. Values beyond
     * AE2's postfix range keep bounded scientific notation; the exact value remains in
     * the tooltip.
     */
    public static String getAeCompactAmount(String value) {
        if (value == null || value.isBlank()) return "0";
        String display = stripFe(value.strip());
        if (display.contains("E(")) return display;
        if (display.contains(" x 2^")) {
            String decimal = getCompactAbsoluteCalculation(display);
            return decimal.equals(display) ? display : getAeCompactAmount(decimal);
        }

        Matcher matcher = DECIMAL_AMOUNT.matcher(display);
        if (!matcher.matches()) return getCompactDecimal(display);

        String integerPart = matcher.group(2);
        String fractionPart = matcher.group(3) == null ? "" : matcher.group(3);
        String digits = integerPart + fractionPart;
        int firstNonZero = 0;
        while (firstNonZero < digits.length() && digits.charAt(firstNonZero) == '0') firstNonZero++;
        if (firstNonZero == digits.length()) return "0";
        digits = digits.substring(firstNonZero);

        long explicitExponent;
        try {
            explicitExponent = matcher.group(4) == null ? 0L : Long.parseLong(matcher.group(4));
        } catch (NumberFormatException ignored) {
            return compactBigNumberDisplay(display);
        }

        long decimalExponent = explicitExponent + integerPart.length() - firstNonZero - 1L;
        if (decimalExponent < 3L) {
            return display;
        }

        int unitIndex = (int) (decimalExponent / 3L);
        if (unitIndex >= AE_POSTFIXES.length) {
            return compactBigNumberDisplay(display);
        }

        int significantLength = Math.min(16, digits.length());
        BigDecimal leading = new BigDecimal((matcher.group(1).equals("-") ? "-" : "")
                + digits.substring(0, significantLength));
        long scaledPower = decimalExponent - (significantLength - 1L) - unitIndex * 3L;
        if (scaledPower > Integer.MAX_VALUE || scaledPower < Integer.MIN_VALUE) {
            return compactBigNumberDisplay(display);
        }
        BigDecimal scaled = leading.movePointRight((int) scaledPower);
        int integerDigits = Math.max(1, scaled.precision() - scaled.scale());
        int fractionDigits = integerDigits < 2 ? 1 : 0;
        String result = scaled.setScale(fractionDigits, RoundingMode.DOWN)
                .stripTrailingZeros().toPlainString();
        return result + AE_POSTFIXES[unitIndex];
    }

    /** Keeps very large Flux values on one GUI line without losing their exponent. */
    public static String getCompactBigNumber(BigNumber value) {
        if (value == null || value.isZero()) return "0";
        String display = value.toDisplayString();
        if (display.contains("E[")) {
            String exponent = getCompactAbsoluteCalculation(value.toExponentCalculationString());
            return (value.signum() < 0 ? "-1E(" : "1E(") + exponent + ")";
        }
        return compactBigNumberDisplay(display);
    }

    /** Bounded Flux calculation form for detailed number tooltips. */
    public static String getCompactBigNumberStorageCalculation(BigNumber value) {
        if (value == null || value.isZero()) return "0";
        BigDecimal coefficient = value.getCoefficient()
                .round(COMPACT_BIG_NUMBER_CONTEXT)
                .stripTrailingZeros();
        String exponent = value.toExponentCalculationString();
        if (exponent.matches("[+-]?\\d{1,9}")) {
            return coefficient.toPlainString() + " x 10^" + exponent;
        }
        return coefficient.toPlainString() + " x 10^("
                + getCompactAbsoluteStorageCalculation(exponent) + ")";
    }

    /** Returns the expanded meaning of nested scientific notation, or null for ordinary values. */
    public static String getScientificNotationExplanation(String display) {
        if (display == null) return null;
        int marker = display.indexOf("E(");
        if (marker > 0 && display.endsWith(")")) {
            String mantissa = display.substring(0, marker);
            String exponent = display.substring(marker + 2, display.length() - 1);
            return mantissa + " x 10^(" + exponent + ")";
        }

        marker = Math.max(display.indexOf('E'), display.indexOf('e'));
        if (marker <= 0 || marker >= display.length() - 1) return null;
        String exponent = display.substring(marker + 1);
        if (!exponent.matches("[+-]?\\d+")) return null;
        return display.substring(0, marker) + " x 10^" + exponent;
    }

    /**
     * Converts Flux's internal AbsoluteInteger calculation text into a short
     * GUI value without expanding it into a decimal BigInteger.
     */
    public static String getCompactAbsoluteCalculation(String calculation) {
        if (calculation == null || calculation.isBlank()) return "0";
        String value = calculation.strip();
        if (value.matches("[0-9]+")) return getScientificInteger(value);
        String compact = compactAbsoluteCalculation(value);
        return compact.equals(value) && value.length() > 24
                ? value.substring(0, 21) + "..." : compact;
    }

    /** Keeps Flux's exact binary representation bounded for hover text. */
    public static String getCompactAbsoluteStorageCalculation(String calculation) {
        if (calculation == null || calculation.isBlank()) return "0";
        String value = calculation.strip();
        if (value.matches("[0-9]+")) return getScientificInteger(value);

        Matcher matcher = ABSOLUTE_CALCULATION.matcher(value);
        if (matcher.find()) {
            if (value.length() <= 64 && matcher.start() == 0 && matcher.end() == value.length()) {
                return value;
            }
            return getScientificInteger(matcher.group(1))
                    + " x 2^" + compactExponent(matcher.group(2));
        }
        return value.length() <= 64 ? value : value.substring(0, 61) + "...";
    }

    static String compactBigNumberDisplay(String rawDisplay) {
        String display = stripFe(rawDisplay);
        int exponentMarker = Math.max(display.indexOf('E'), display.indexOf('e'));
        if (exponentMarker <= 0) return compactPlainDecimal(display);

        try {
            BigDecimal mantissa = new BigDecimal(display.substring(0, exponentMarker))
                    .round(COMPACT_BIG_NUMBER_CONTEXT)
                    .stripTrailingZeros();
            String exponent = display.substring(exponentMarker + 1);
            if (exponent.matches("[+-]?\\d+")) {
                if (mantissa.abs().compareTo(BigDecimal.TEN) >= 0) {
                    mantissa = mantissa.movePointLeft(1).stripTrailingZeros();
                    exponent = incrementDecimalExponent(exponent);
                }
            }
            String compactedExponent = compactExponent(exponent);
            if (compactedExponent.startsWith("(")) {
                return (mantissa.signum() < 0 ? "-1E" : "1E") + compactedExponent;
            }
            return mantissa.toPlainString() + "E" + compactedExponent;
        } catch (NumberFormatException ignored) {
            return display;
        }
    }

    private static String incrementDecimalExponent(String exponent) {
        boolean negative = exponent.startsWith("-");
        String digits = exponent.startsWith("+") || negative
                ? exponent.substring(1) : exponent;
        digits = stripLeadingZeros(digits);
        if (negative) {
            if (digits.equals("0")) return "0";
            return digits.equals("1") ? "0" : "-" + decrementDecimalDigits(digits);
        }
        return incrementDecimalDigits(digits);
    }

    private static String incrementDecimalDigits(String digits) {
        char[] characters = digits.toCharArray();
        for (int index = characters.length - 1; index >= 0; index--) {
            if (characters[index] != '9') {
                characters[index]++;
                return new String(characters);
            }
            characters[index] = '0';
        }
        return "1" + new String(characters);
    }

    private static String decrementDecimalDigits(String digits) {
        char[] characters = digits.toCharArray();
        for (int index = characters.length - 1; index >= 0; index--) {
            if (characters[index] != '0') {
                characters[index]--;
                return stripLeadingZeros(new String(characters));
            }
            characters[index] = '9';
        }
        return "0";
    }

    private static String stripLeadingZeros(String digits) {
        int first = 0;
        while (first < digits.length() - 1 && digits.charAt(first) == '0') first++;
        return digits.substring(first);
    }

    /** Uses nested scientific notation when the exponent itself is too wide for a GUI line. */
    private static String compactExponent(String exponent) {
        return compactExponent(exponent, 0);
    }

    private static String compactExponent(String exponent, int depth) {
        String sign = "";
        String digits = exponent;
        if (digits.startsWith("+") || digits.startsWith("-")) {
            sign = digits.substring(0, 1);
            digits = digits.substring(1);
        }
        digits = stripLeadingZeros(digits);
        if (digits.length() <= MAX_LITERAL_EXPONENT_LENGTH) {
            return sign + digits;
        }
        if (depth >= MAX_NESTED_EXPONENT_DEPTH) {
            return "(" + sign + digits.substring(0, Math.min(3, digits.length()))
                    + "...E" + digits.length() + ")";
        }
        return "(" + sign + compactScientificDigits(digits, depth + 1, 3) + ")";
    }

    private static String compactPlainDecimal(String display) {
        try {
            BigDecimal number = new BigDecimal(display);
            String plain = number.stripTrailingZeros().toPlainString();
            if (plain.length() <= MAX_PLAIN_DISPLAY_LENGTH) return plain;

            BigDecimal rounded = number.round(COMPACT_BIG_NUMBER_CONTEXT).stripTrailingZeros();
            int exponent = rounded.precision() - rounded.scale() - 1;
            BigDecimal mantissa = rounded.movePointLeft(exponent).stripTrailingZeros();
            return mantissa.toPlainString() + "E" + exponent;
        } catch (NumberFormatException ignored) {
            return display;
        }
    }

    /** Compacts decimal text for a GUI without changing the stored number. */
    public static String getCompactDecimal(String value) {
        if (value == null || value.isBlank()) return "0";
        try {
            return getCompactDecimal(new BigDecimal(value));
        } catch (NumberFormatException ignored) {
            return value.length() <= 18 ? value : value.substring(0, 15) + "...";
        }
    }

    /** Compacts a decimal directly, avoiding creation of a potentially enormous plain string. */
    public static String getCompactDecimal(BigDecimal value) {
        if (value == null || value.signum() == 0) return "0";
        int integerDigits = value.precision() - value.scale();
        if (value.precision() <= 18 && integerDigits <= 18 && value.scale() <= 18) {
            return value.stripTrailingZeros().toPlainString();
        }
        return value.round(COMPACT_BIG_NUMBER_CONTEXT).stripTrailingZeros().toEngineeringString();
    }

    /** Formats an exact Flux integer for compact GUI display without changing the stored value. */
    public static String getScientificInteger(AbsoluteInteger value) {
        if (value == null || value.isZero()) return "0";
        String calculation = value.toCalculationString();
        for (int index = 0; index < calculation.length(); index++) {
            if (!Character.isDigit(calculation.charAt(index))) {
                return compactAbsoluteCalculation(calculation);
            }
        }
        return getScientificInteger(calculation);
    }

    /** Converts Flux's layered binary calculation text into a readable decimal estimate. */
    private static String compactAbsoluteCalculation(String calculation) {
        Matcher matcher = ABSOLUTE_CALCULATION.matcher(calculation);
        if (!matcher.find()) return getCompactDecimal(calculation);

        try {
            double binaryExponent = Double.parseDouble(matcher.group(2));
            double coefficient = new BigDecimal(matcher.group(1)).doubleValue();
            double logarithm = Math.log10(coefficient) + binaryExponent * LOG10_TWO;
            if (!Double.isFinite(logarithm)) return compactBinaryCalculation(matcher);

            long decimalExponent = (long) Math.floor(logarithm);
            BigDecimal mantissa = BigDecimal.valueOf(Math.pow(10.0, logarithm - decimalExponent))
                    .round(COMPACT_BIG_NUMBER_CONTEXT)
                    .stripTrailingZeros();
            if (mantissa.compareTo(BigDecimal.TEN) >= 0) {
                mantissa = mantissa.movePointLeft(1).stripTrailingZeros();
                decimalExponent++;
            }

            // The display-only value still uses Flux's AbsoluteInteger exponent path.
            BigNumber scientific = BigNumber.scientific(
                    mantissa.toPlainString(), Long.toString(decimalExponent));
            return compactBigNumberDisplay(scientific.toDisplayString());
        } catch (NumberFormatException | ArithmeticException ignored) {
            return compactBinaryCalculation(matcher);
        }
    }

    private static String compactBinaryCalculation(Matcher matcher) {
        String coefficient = getScientificInteger(matcher.group(1));
            return coefficient + " x 2^" + compactExponent(matcher.group(2));
    }

    static String getScientificInteger(String value) {
        if (value == null || value.isBlank()) return "0";
        return compactScientificDigits(value.strip(), 0);
    }

    private static String compactScientificDigits(String digits, int depth) {
        return compactScientificDigits(digits, depth, DISPLAY_SIGNIFICANT_DIGITS);
    }

    private static String compactScientificDigits(String digits, int depth, int significantDigits) {
        int firstNonZero = 0;
        while (firstNonZero < digits.length() && digits.charAt(firstNonZero) == '0') firstNonZero++;
        if (firstNonZero == digits.length()) return "0";
        digits = digits.substring(firstNonZero);
        if (digits.length() <= significantDigits) return digits;

        int exponent = digits.length() - 1;
        BigInteger coefficient = new BigInteger(digits.substring(0, significantDigits));
        if (digits.charAt(significantDigits) >= '5') coefficient = coefficient.add(BigInteger.ONE);
        String rounded = coefficient.toString();
        if (rounded.length() > significantDigits) {
            exponent++;
            rounded = rounded.substring(0, significantDigits);
        }

        String mantissa = rounded.charAt(0) + "." + rounded.substring(1);
        mantissa = mantissa.replaceFirst("0+$", "").replaceFirst("\\.$", "");
        String exponentText = Integer.toString(exponent);
        return mantissa + "E" + (exponentText.length() > MAX_LITERAL_EXPONENT_LENGTH
                ? compactExponent(exponentText, depth + 1) : exponentText);
    }

    private static String stripFe(String display) {
        return display.endsWith(" FE") ? display.substring(0, display.length() - 3) : display;
    }

    public static double customCeil(double value) {
        if (value == (double)((long)value)) {
            return value;
        }
        return value > 0.0 ? (double)((long)value + 1L) : (double)((long)value);
    }
}

