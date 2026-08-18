package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import java.math.BigDecimal;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;
import sonar.fluxnetworks.api.energy.BigNumber;
import java.math.RoundingMode;

/**
 * 物理引擎（AstrophysicalCalculator）
 * <p>
 * 根据星体类型计算其基础功率（FE/s）。
 * <p>
 * 数据类型策略：基础功率用 BigNumber（支持超大数，如普朗克星 10⁴² FE/s）。
 * 最终功率 = base × 10^totalWrapped × 暗物质共鸣(×2^resonance) × 鸿蒙之气 × 寰宇之心
 */
public final class AstrophysicalCalculator {
    private static final long LOG10_TWO_SCALED = 301_029_995_663_981_195L;
    private static final long LOG10_SCALE = 1_000_000_000_000_000_000L;
    private static final AbsoluteInteger EXACT_POWER_OF_TWO_LIMIT =
            AbsoluteInteger.parse("60");
    private static final double SIGMA = 5.67e-8;
    /** 1 太阳半径（米）R☉ = 6.96e8 m */
    public static final double SOLAR_RADIUS_M = 6.96e8;
    /** 1 太阳质量（千克）M☉ = 1.989e30 kg */
    public static final double SOLAR_MASS_KG = 1.989e30;
    /** 太阳光度 L☉（W） */
    public static final double SOLAR_LUMINOSITY = 3.828e26;
    /** 万有引力常数 G */
    private static final double G = 6.674e-11;
    /** 光速 c（m/s） */
    private static final double C = 2.99792458e8;
    /** 光速平方 c² */
    private static final BigDecimal C_SQUARED = new BigDecimal("8.9875517873681764E16");

    private AstrophysicalCalculator() {
    }

    // ====== 基础功率计算（返回 BigNumber）=====

    /**
     * 计算星体基础功率。
     *
     * @param star 星体数据
     * @return 基础功率（FE/s），超大数用 BigNumber 承载
     */
    public static BigNumber calculateBasePower(StarData star) {
        if (star == null) {
            return new BigNumber(SOLAR_LUMINOSITY);
        }
        String type = star.type();
        double radiusM = star.radiusInRSun() * SOLAR_RADIUS_M;
        double massM = star.massInMSun();
        double tempK = star.temperatureK();

        switch (type) {
            // 中子星/脉冲星：自转制动功率，固定 100 × L☉
            case "NEUTRON_STAR":
                return new BigNumber(100.0 * SOLAR_LUMINOSITY);

            // 类星体/耀变体/塞佛特：按黑洞质量插值，10¹²~10¹⁴ × L☉
            case "QUASAR":
            case "BLAZAR":
            case "SEYFERT":
                return quasarPower(massM);

            // 棕矮星（L/T/Y）：低产，10⁻⁶~10⁻³ × L☉
            case "L_DWARF":
                return new BigNumber(1e-6 * SOLAR_LUMINOSITY);
            case "T_DWARF":
                return new BigNumber(1e-5 * SOLAR_LUMINOSITY);
            case "Y_DWARF":
                return new BigNumber(1e-3 * SOLAR_LUMINOSITY);

            // 行星（热木星/超级地球）：产电 0
            case "HOT_JUPITER":
            case "SUPER_EARTH":
                return BigNumber.valueOf(0L);

            // 超新星/极超新星：固定峰值脉冲
            case "SUPERNOVA":
            case "HYPERNOVA":
                return new BigNumber(1e9 * SOLAR_LUMINOSITY);

            // 伽马射线暴（GRB）：极高脉冲
            case "GRB":
                return new BigNumber(1e12 * SOLAR_LUMINOSITY);

            // 黑洞/夸克星/Q星：吸积盘模式功率
            case "STELLAR_BH":
            case "SUPERMASSIVE_BH":
            case "Q_STAR":
            case "QUARK_STAR":
                return blackHolePower(massM);

            // 电弱星：极低功率
            case "ELECTROWEAK_STAR":
                return new BigNumber(1e-10 * SOLAR_LUMINOSITY);

            // 暗物质星：不发光
            case "DARK_MATTER":
                return BigNumber.valueOf(0L);

            // 普朗克星：极端高温，用 BigDecimal 精确计算
            case "PLANCK":
                return planckStarPower(radiusM, tempK);

            // 默认：标准黑体辐射
            default:
                return blackBodyPowerBig(radiusM, tempK);
        }
    }

    // ====== 辅助计算方法 ======

    /** 标准黑体辐射功率（BigNumber 版）：P = σ × 4πR² × T⁴ */
    public static BigNumber blackBodyPowerBig(double radiusM, double tempK) {
        if (tempK <= 0) return BigNumber.valueOf(0L);
        BigDecimal area = BigDecimal.valueOf(4.0 * Math.PI * radiusM * radiusM);
        BigDecimal temp4 = BigDecimal.valueOf(tempK).pow(4);
        BigDecimal power = BigDecimal.valueOf(SIGMA).multiply(area).multiply(temp4);
        return new BigNumber(power);
    }

    /** 类星体/耀变体/塞佛特功率：按黑洞质量 log10 插值，10¹²~10¹⁴ × L☉ */
    public static BigNumber quasarPower(double massMSun) {
        double logMass = Math.log10(Math.max(massMSun, 1e6));
        double t = Math.max(0.0, Math.min(1.0, (logMass - 6.0) / 5.0));
        double logPower = 12.0 + t * 2.0; // 10¹² ~ 10¹⁴
        return new BigNumber(Math.pow(10.0, logPower) * SOLAR_LUMINOSITY);
    }

    /**
     * 普朗克星功率：P = σ × 4πR² × T⁴
     * R ≈ 1.6e-35 m（普朗克长度），T ≈ 1.42e32 K
     * P ≈ 10⁴²~10⁴⁶ W（FE/s），必须用 BigDecimal 精确计算
     */
    public static BigNumber planckStarPower(double radiusM, double tempK) {
        if (tempK <= 0 || radiusM <= 0) return BigNumber.valueOf(0L);
        // 使用 BigDecimal 精确计算 T⁴
        BigDecimal r = BigDecimal.valueOf(radiusM);
        BigDecimal t = BigDecimal.valueOf(tempK);
        BigDecimal t2 = t.multiply(t);
        BigDecimal t4 = t2.multiply(t2);
        BigDecimal area = BigDecimal.valueOf(4.0 * Math.PI).multiply(r.multiply(r));
        BigDecimal power = BigDecimal.valueOf(SIGMA).multiply(area).multiply(t4);
        return new BigNumber(power);
    }

    /**
     * 黑洞吸积盘功率：P = η × Ṁ × c²
     * η = 0.1（10% 质能转换效率），Ṁ ≈ 10⁻⁸ M☉/年
     */
    public static BigNumber blackHolePower(double massMSun) {
        // 吸积率 Ṁ（kg/s）：10⁻⁸ M☉/年
        BigDecimal mdot = BigDecimal.valueOf(1e-8)
                .multiply(BigDecimal.valueOf(SOLAR_MASS_KG))
                .divide(BigDecimal.valueOf(3.15e7), 10, RoundingMode.HALF_UP);
        // P = η × Ṁ × c²
        BigDecimal power = BigDecimal.valueOf(0.1)
                .multiply(mdot)
                .multiply(C_SQUARED);
        // 质量越大，吸积盘功率越高
        if (massMSun > 10.0) {
            power = power.multiply(BigDecimal.valueOf(massMSun / 10.0));
        }
        return new BigNumber(power);
    }

    /**
     * 史瓦西半径（米）：r_s = 2GM/c²
     */
    public static double schwarzschildRadius(double massMSun) {
        double massKg = massMSun * SOLAR_MASS_KG;
        return 2.0 * G * massKg / (C * C);
    }

    /**
     * 视界表面积：A_H = 4π × r_s²
     */
    public static double eventHorizonArea(double massMSun) {
        double rs = schwarzschildRadius(massMSun);
        return 4.0 * Math.PI * rs * rs;
    }

    /**
     * 视界体积：V_H = 4/3 × π × r_s³
     */
    public static double eventHorizonVolume(double massMSun) {
        double rs = schwarzschildRadius(massMSun);
        return (4.0 / 3.0) * Math.PI * rs * rs * rs;
    }

    // ====== 功率合成（应用所有乘算因子）=====

    /**
     * 计算最终功率（BigNumber 版）。
     * <p>
     * 最终功率 = base × 10^totalWrapped × 暗物质共鸣(×2^resonance) × 鸿蒙之气输出 × 寰宇之心倍率
     *
     * @param basePower    基础功率
     * @param totalWrapped 已包裹星体总数
     * @param resonance    暗物质共鸣次数
     * @param qiOutput     鸿蒙之气输出倍率
     * @param heartMult    寰宇之心产出倍率
     * @return 最终功率（BigNumber）
     */
    public static BigNumber calculateFinalPower(BigNumber basePower, long totalWrapped,
                                                 int resonance, double qiOutput, double heartMult) {
        BigNumber exactQiOutput = Double.isFinite(qiOutput) && qiOutput > 1.0
                ? new BigNumber(qiOutput)
                : BigNumber.valueOf(1L);
        return calculateFinalPower(basePower, totalWrapped, resonance, exactQiOutput, heartMult);
    }

    /** Calculates final power without projecting the primordial-Qi multiplier through a double. */
    public static BigNumber calculateFinalPower(BigNumber basePower, long totalWrapped,
                                                 int resonance, BigNumber qiOutput, double heartMult) {
        // base × 10^totalWrapped
        BigNumber result = basePower == null ? new BigNumber(0) : basePower.deepCopy();
        if (totalWrapped > 0 && !result.isEmpty()) {
            result.multiply(BigNumber.scientific("1", Long.toString(totalWrapped)));
        }
        // 暗物质共鸣：×2^resonance
        if (resonance > 0) {
            result.multiply(powSmallInteger(2, resonance));
        }
        // 鸿蒙之气输出倍率
        if (qiOutput != null && qiOutput.compareTo(BigNumber.valueOf(1L)) > 0) {
            result.multiply(qiOutput.deepCopy());
        }
        // 寰宇之心倍率
        if (heartMult > 1.0) {
            result.multiply(new BigNumber(heartMult));
        }
        return result;
    }

    /**
     * Exact-count variant. Both counters stay in the Flux absolute-integer
     * representation, so runtime does not grow with the represented value.
     */
    public static BigNumber calculateFinalPower(BigNumber basePower,
                                                 AbsoluteInteger totalWrapped,
                                                 AbsoluteInteger resonance,
                                                 BigNumber qiOutput,
                                                 double heartMult) {
        BigNumber exactHeartMultiplier = Double.isFinite(heartMult) && heartMult > 1.0D
                ? new BigNumber(heartMult)
                : BigNumber.valueOf(1L);
        return calculateFinalPower(basePower, totalWrapped, resonance, qiOutput,
                exactHeartMultiplier);
    }

    public static BigNumber calculateFinalPower(BigNumber basePower,
                                                 AbsoluteInteger totalWrapped,
                                                 AbsoluteInteger resonance,
                                                 BigNumber qiOutput,
                                                 BigNumber heartMultiplier) {
        BigNumber result = basePower == null ? new BigNumber(0) : basePower.deepCopy();
        if (result.isEmpty()) return result;

        if (totalWrapped != null && !totalWrapped.isZero()) {
            FluxMath8.addExponent(result, totalWrapped);
        }
        if (resonance != null && !resonance.isZero()) {
            result.multiply(powerOfTwo(resonance));
        }
        if (qiOutput != null && qiOutput.compareTo(BigNumber.valueOf(1L)) > 0) {
            result.multiply(qiOutput.deepCopy());
        }
        if (heartMultiplier != null
                && heartMultiplier.compareTo(BigNumber.valueOf(1L)) > 0) {
            result.multiply(heartMultiplier.deepCopy());
        }
        return result;
    }

    /** Converts 2^n into coefficient * 10^exponent with an 18-digit log10(2). */
    public static BigNumber powerOfTwoMultiplier(AbsoluteInteger exponent) {
        return powerOfTwo(exponent);
    }

    private static BigNumber powerOfTwo(AbsoluteInteger exponent) {
        if (exponent == null || exponent.isZero()) return BigNumber.valueOf(1L);
        if (exponent.compareTo(EXACT_POWER_OF_TWO_LIMIT) <= 0) {
            return powSmallInteger(2, FluxMath8.toIntSaturated(exponent));
        }
        FluxMath8.ScaledDivision logarithm = FluxMath8.multiplyDivideExponent(
                exponent, LOG10_TWO_SCALED, LOG10_SCALE);
        if (!logarithm.exact()) {
            return FluxMath8.scientific("1", logarithm.quotient());
        }
        double fractionalLogarithm = (double) logarithm.remainder() / (double) LOG10_SCALE;
        String coefficient = BigDecimal.valueOf(Math.pow(10.0D, fractionalLogarithm))
                .stripTrailingZeros()
                .toPlainString();
        return FluxMath8.scientific(coefficient, logarithm.quotient());
    }

    private static BigNumber powSmallInteger(long base, int exponent) {
        BigNumber result = BigNumber.valueOf(1L);
        BigNumber factor = BigNumber.valueOf(base);
        int remaining = exponent;
        while (remaining > 0) {
            if ((remaining & 1) != 0) {
                result.multiply(factor.deepCopy());
            }
            remaining >>>= 1;
            if (remaining > 0) {
                factor.multiply(factor.deepCopy());
            }
        }
        return result;
    }

    /**
     * 计算最终功率（double 版，用于缓存显示）。
     *
     * @param basePower    基础功率（double）
     * @param totalWrapped 已包裹星体总数
     * @param resonance    暗物质共鸣次数
     * @param qiOutput     鸿蒙之气输出倍率
     * @param heartMult    寰宇之心产出倍率
     * @return 最终功率（FE/s，double）
     */
    public static double calculateFinalPowerDouble(double basePower, long totalWrapped,
                                                    int resonance, double qiOutput, double heartMult) {
        double result = basePower * Math.pow(10.0, totalWrapped);
        if (resonance > 0) {
            result *= Math.pow(2.0, resonance);
        }
        result *= qiOutput;
        result *= heartMult;
        return result;
    }
}

