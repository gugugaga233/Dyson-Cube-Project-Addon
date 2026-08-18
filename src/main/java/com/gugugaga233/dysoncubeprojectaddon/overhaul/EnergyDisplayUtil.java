package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * 能量数值格式化器（适配 Flux-Networks EnergyDisplayUtil 设计）
 * <p>
 * 支持 100+ 进制后缀（K/M/B/T/Qa/Qi/Sx/Sp/Oc/No/Dc/Uc/Dd/Td...），
 * 超大数自动降级为科学计数法。
 * <p>
 * 用于替代原 BigEnergy.toDisplayString() 的 12 后缀限制。
 */
public final class EnergyDisplayUtil {

    private static final NavigableMap<BigInteger, String> SUFFIXES = new TreeMap<>();

    private static final String[] SUFFIX_NAMES = {
            "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No",
            "Dc", "Ud", "Dd", "Td", "Qad", "Qid", "Sxd", "Spd", "Ocdd", "Nod",
            "Vg", "Uvg", "Dvg", "Tvg", "Qavg", "Qivg", "Sxvg", "Spvg", "Ocvg", "Novg",
            "Tg", "Utg", "Dtg", "Ttg", "Qatg", "Qitg", "Sxtg", "Sptg", "Octg", "Notg",
            "Qag", "Uqag", "Dqag", "Tqag", "Qaqag", "Qiqag", "Sxqag", "Spqag", "Ocqag", "Noqag",
            "Qig", "UQig", "DQig", "TQig", "QaQig", "QiQig", "SxQig", "SpQig", "OcQig", "NoQig",
            "Sxg", "USxg", "DSxg", "TSxg", "QaSxg", "QiSxg", "SxSxg", "SpSxg", "OcSxg", "NoSxg",
            "Spg", "USpg", "DSpg", "TSpg", "QaSpg", "QiSpg", "SxSpg", "SpSpg", "OcSpg", "NoSpg",
            "Ocg", "UOcg", "DOcg", "TOcg", "QaOcg", "QiOcg", "SxOcg", "SpOcg", "OcOcg", "NoOcg",
            "Nog", "UNog", "DNog", "TNog", "QaNog", "QiNog", "SxNog", "SpNog", "OcNog", "NoNog",
            "C", "Uc"
    };

    static {
        BigInteger thousand = BigInteger.valueOf(1000);
        BigInteger current = thousand;
        for (String suffix : SUFFIX_NAMES) {
            SUFFIXES.put(current, suffix);
            current = current.multiply(thousand);
        }
    }

    // 超过此值时改用科学计数法
    private static final BigInteger UC_THRESHOLD = new BigInteger(
            "999" + "0".repeat(301)); // ≈ 10^303

    private EnergyDisplayUtil() {}

    /**
     * 格式化 BigInteger 能量值（默认后缀 FE）。
     */
    public static String format(BigInteger value) {
        return format(value, false);
    }

    /**
     * 格式化 BigInteger 能量值，支持科学计数法。
     */
    public static String format(BigInteger value, boolean scientificNotation) {
        return format(value, scientificNotation, "FE");
    }

    /**
     * 格式化 BigInteger 能量值，指定后缀。
     */
    public static String format(BigInteger value, boolean scientificNotation, String suffix) {
        if (value == null || value.signum() == 0) {
            return "0" + (suffix == null ? "" : " " + suffix);
        }
        if (scientificNotation) {
            return formatScientific(value, suffix);
        }
        if (value.abs().compareTo(BigInteger.valueOf(1_000_000)) < 0) {
            return value + " " + suffix;
        }
        if (value.abs().compareTo(UC_THRESHOLD) > 0) {
            return formatScientific(value, suffix);
        }
        BigInteger threshold = SUFFIXES.floorKey(value.abs());
        if (threshold != null) {
            BigInteger[] divRem = value.abs().divideAndRemainder(threshold);
            BigInteger whole = divRem[0];
            BigInteger fraction = divRem[1].multiply(BigInteger.valueOf(100)).divide(threshold);
            String suffixName = SUFFIXES.get(threshold);
            if (fraction.intValue() == 0) {
                return whole + " " + suffixName + " " + suffix;
            }
            return whole + "." + (fraction.intValue() < 10 ? "0" : "")
                    + fraction + " " + suffixName + " " + suffix;
        }
        return formatScientific(value, suffix);
    }

    /**
     * 科学计数法格式化（用于超大数）。
     */
    public static String formatScientific(BigInteger value) {
        return formatScientific(value, "FE");
    }

    public static String formatScientific(BigInteger value, String suffix) {
        if (value.signum() == 0) return "0" + (suffix == null ? "" : " " + suffix);
        int exponent = value.toString().length() - 1;
        BigDecimal bd = new BigDecimal(value.abs());
        BigDecimal mantissa = bd.divide(BigDecimal.TEN.pow(exponent), 2, RoundingMode.HALF_UP);
        return mantissa.stripTrailingZeros().toPlainString()
                + "E" + exponent + (suffix == null ? "" : " " + suffix);
    }

    /**
     * 快速格式化（12 后缀，兼容旧 BigEnergy.toDisplayString() 调用）。
     */
    public static String formatCompact(BigInteger value) {
        if (value == null || value.signum() == 0) return "0";
        int digits = value.toString().length();
        if (digits <= 6) return value.toString();
        String[] units = {"", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No", "Dc", "Uc"};
        int exp = (digits - 1) / 3;
        if (exp >= units.length) return formatScientific(value);
        String s = value.toString();
        String mantissa = s.substring(0, Math.min(4, digits));
        if (mantissa.length() > 1) {
            mantissa = mantissa.charAt(0) + "." + mantissa.charAt(1);
        }
        return mantissa + units[exp];
    }
}

