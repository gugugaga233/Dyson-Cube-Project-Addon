package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import com.gugugaga233.dysoncubeprojectaddon.Config;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.math.BigDecimal;
import java.math.RoundingMode;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;

/**
 * 天体随机池（UniverseRandomizer）
 * <p>
 * 实现规格：总权重 1,000,000，加权随机生成星体。
 * totalWrapped < 1000 时，黑洞相关类型权重强制归零并重新分配。
 * 第1001颗强制触发"宇宙觉醒"事件，解禁完整黑洞池。
 * <p>
 * 首个星体默认太阳（黄矮星 G），由 {@link DysonSphereStructure} 调用。
 */
public final class UniverseRandomizer {

    private static final Random RANDOM = new Random();
    /** 总权重 */
    private static final int TOTAL_WEIGHT = 1_000_000;
    /** 黑洞解锁阈值 */
    public static final int BLACKHOLE_UNLOCK_THRESHOLD = 1000;
    /** 反物质星体解锁阈值 */
    public static final int ANTIMATTER_UNLOCK_THRESHOLD = 5000;
    private static final List<StarEntry> LOCKED_POOL = buildPool(true);
    private static final List<StarEntry> UNLOCKED_POOL = buildPool(false);
    private static final long DARK_MATTER_WEIGHT = 100L;
    private static final long ANTIMATTER_WEIGHT = 50L;
    private static final long PLANCK_WEIGHT = 50L;

    private UniverseRandomizer() {
    }

    /**
     * 随机生成一颗星体。
     *
     * @param totalWrapped 当前已包裹星体总数（用于判断是否解锁黑洞池/反物质池）
     * @return 新的星体数据
     */
    public static StarData roll(long totalWrapped) {
        return roll(AbsoluteInteger.parse(Long.toString(Math.max(0L, totalWrapped))));
    }

    /** Rolls one exact landing star without narrowing a huge wrapped count. */
    public static StarData roll(AbsoluteInteger totalWrapped) {
        AbsoluteInteger wrapped = totalWrapped == null ? new AbsoluteInteger() : totalWrapped;
        // 第1001颗：强制"宇宙觉醒"
        if (equalsLong(wrapped, BLACKHOLE_UNLOCK_THRESHOLD)) {
            return createQuasar(); // 宇宙觉醒时产出一颗类星体
        }
        // 第5001颗：强制"反物质觉醒"
        if (equalsLong(wrapped, ANTIMATTER_UNLOCK_THRESHOLD)) {
            return createAntimatterRedDwarf(); // 反物质觉醒时产出反物质红矮星
        }

        // 反物质池：totalWrapped >= 5000 时，50/1000000 权重
        if (atLeast(wrapped, ANTIMATTER_UNLOCK_THRESHOLD)) {
            int antimatterRoll = RANDOM.nextInt(1000000);
            if (antimatterRoll < 50) {
                return rollAntimatter();
            }
        }

        // 暗物质星：totalWrapped >= 3000，权重 100
        if (atLeast(wrapped, 3000L) && RANDOM.nextInt(1000000) < 100) {
            return createDarkMatterStar();
        }

        // 普朗克星：totalWrapped >= 10000，权重 50
        if (atLeast(wrapped, 10000L) && RANDOM.nextInt(1000000) < 50) {
            return createPlanckStar();
        }

        List<StarEntry> pool = atLeast(wrapped, BLACKHOLE_UNLOCK_THRESHOLD)
                ? UNLOCKED_POOL
                : LOCKED_POOL;
        int roll = RANDOM.nextInt(TOTAL_WEIGHT);
        int cumulative = 0;

        for (StarEntry entry : pool) {
            cumulative += entry.weight;
            if (roll < cumulative) {
                return entry.creator.create();
            }
        }

        // 回退：红矮星（最安全的选择）
        return createRedDwarf();
    }

    /** Average material cost for one ordinary completion in the current rule stage. */
    public static AverageStructureCost averageStructureCost(AbsoluteInteger totalWrapped) {
        AbsoluteInteger wrapped = totalWrapped == null ? new AbsoluteInteger() : totalWrapped;
        boolean blackHolesUnlocked = atLeast(wrapped, BLACKHOLE_UNLOCK_THRESHOLD);
        boolean darkMatterUnlocked = atLeast(wrapped, 3000L);
        boolean antimatterUnlocked = atLeast(wrapped, ANTIMATTER_UNLOCK_THRESHOLD);
        boolean planckUnlocked = atLeast(wrapped, 10000L);

        BigDecimal million = BigDecimal.valueOf(TOTAL_WEIGHT);
        BigDecimal remaining = BigDecimal.ONE;
        BigDecimal expectedRadiusSquared = BigDecimal.ZERO;

        if (antimatterUnlocked) {
            BigDecimal probability = BigDecimal.valueOf(ANTIMATTER_WEIGHT).divide(million);
            expectedRadiusSquared = expectedRadiusSquared.add(
                    remaining.multiply(probability).multiply(antimatterExpectedRadiusSquared()));
            remaining = remaining.multiply(BigDecimal.ONE.subtract(probability));
        }
        if (darkMatterUnlocked) {
            BigDecimal probability = BigDecimal.valueOf(DARK_MATTER_WEIGHT).divide(million);
            expectedRadiusSquared = expectedRadiusSquared.add(
                    remaining.multiply(probability).multiply(uniformSquare(0.5D, 2.5D)));
            remaining = remaining.multiply(BigDecimal.ONE.subtract(probability));
        }
        if (planckUnlocked) {
            BigDecimal probability = BigDecimal.valueOf(PLANCK_WEIGHT).divide(million);
            expectedRadiusSquared = expectedRadiusSquared.add(
                    remaining.multiply(probability).multiply(BigDecimal.valueOf(1.6E-35D).pow(2)));
            remaining = remaining.multiply(BigDecimal.ONE.subtract(probability));
        }
        expectedRadiusSquared = expectedRadiusSquared.add(
                remaining.multiply(normalPoolExpectedRadiusSquared(blackHolesUnlocked)));

        BigDecimal panelsDecimal = expectedRadiusSquared
                .multiply(BigDecimal.valueOf(Config.getBaseSolarSailRequirement()))
                .setScale(0, RoundingMode.CEILING)
                .max(BigDecimal.ONE);
        long panels = panelsDecimal.min(BigDecimal.valueOf(Long.MAX_VALUE)).longValue();
        long beams = Math.max(1L, (panels + Config.BEAM_TO_SOLAR_PANEL_RATIO - 1L)
                / Config.BEAM_TO_SOLAR_PANEL_RATIO);
        return new AverageStructureCost(panels, beams);
    }

    /** Pseudo-random binomial expectation with one stochastic correction for the remainder. */
    public static AbsoluteInteger sampleDarkMatterHits(AbsoluteInteger attempts,
                                                        AbsoluteInteger totalWrapped) {
        if (attempts == null || attempts.isZero() || !atLeast(totalWrapped, 3000L)) {
            return new AbsoluteInteger();
        }
        long numerator;
        long denominator;
        if (atLeast(totalWrapped, ANTIMATTER_UNLOCK_THRESHOLD)) {
            numerator = (TOTAL_WEIGHT - ANTIMATTER_WEIGHT) * DARK_MATTER_WEIGHT;
            denominator = (long) TOTAL_WEIGHT * TOTAL_WEIGHT;
        } else {
            numerator = DARK_MATTER_WEIGHT;
            denominator = TOTAL_WEIGHT;
        }
        FluxMath8.Division division = FluxMath8.divideAndRemainder(
                FluxMath8.multiply(attempts, numerator), denominator);
        AbsoluteInteger hits = division.quotient();
        if (division.remainder() > 0L
                && RANDOM.nextDouble() < (double) division.remainder() / denominator) {
            hits.increment();
        }
        return hits;
    }

    public record AverageStructureCost(long solarPanels, long beams) {
    }

    private static BigDecimal normalPoolExpectedRadiusSquared(boolean unlocked) {
        BigDecimal weighted = BigDecimal.ZERO;
        long weight = 0L;
        long redWeight = unlocked ? 730_000L : 737_000L;
        weighted = weighted.add(weightedRange(redWeight, 0.1D, 0.3D)); weight += redWeight;
        weighted = weighted.add(weightedRange(60_000L, 0.7D, 0.9D)); weight += 60_000L;
        weighted = weighted.add(weightedRange(50_000L, 0.9D, 1.2D)); weight += 50_000L;
        weighted = weighted.add(weightedRange(30_000L, 1.0D, 1.4D)); weight += 30_000L;
        weighted = weighted.add(weightedRange(25_000L, 1.4D, 2.1D)); weight += 25_000L;
        weighted = weighted.add(weightedRange(20_000L, 2.1D, 16.0D)); weight += 20_000L;
        long oWeight = unlocked ? 10_000L : 15_000L;
        weighted = weighted.add(weightedRange(oWeight, 16.0D, 100.0D)); weight += oWeight;
        weighted = weighted.add(weightedRange(2_000L, 0.06D, 0.08D)); weight += 2_000L;
        weighted = weighted.add(weightedRange(2_000L, 0.04D, 0.06D)); weight += 2_000L;
        weighted = weighted.add(weightedRange(1_000L, 0.01D, 0.04D)); weight += 1_000L;
        weighted = weighted.add(weightedRange(18_000L, 0.009D, 0.02D)); weight += 18_000L;
        weighted = weighted.add(weightedRange(10_000L, 1.4E-5D, 2.9E-5D)); weight += 10_000L;
        if (unlocked) {
            weighted = weighted.add(weightedRange(5_000L, 7.2E-6D, 1.4E-5D)); weight += 5_000L;
            weighted = weighted.add(weightedRange(3_000L, 1.3E-5D, 4.2E-4D)); weight += 3_000L;
        }
        weighted = weighted.add(weightedFixed(1_000L, 1.0E-14D)); weight += 1_000L;
        weighted = weighted.add(weightedRange(1_000L, 1.0D, 1000.0D)); weight += 1_000L;
        if (unlocked) {
            weighted = weighted.add(weightedFixed(1_000L, 1000.0D)); weight += 1_000L;
            weighted = weighted.add(weightedFixed(1_000L, 1000.0D)); weight += 1_000L;
            weighted = weighted.add(weightedFixed(500L, 18_600.0D)); weight += 500L;
        }
        weighted = weighted.add(weightedRange(2_000L, 100.0D, 1500.0D)); weight += 2_000L;
        long wolfWeight = unlocked ? 1_000L : 3_000L;
        weighted = weighted.add(weightedRange(wolfWeight, 10.0D, 100.0D)); weight += wolfWeight;
        weighted = weighted.add(weightedRange(1_000L, 0.1D, 0.15D)); weight += 1_000L;
        weighted = weighted.add(weightedRange(1_000L, 0.01D, 0.02D)); weight += 1_000L;
        weighted = weighted.add(weightedRange(2_000L, 1.0D, 10.0D)); weight += 2_000L;
        weight += 500L; // GRB radius is zero.
        weighted = weighted.add(weightedRange(1_000L, 10.0D, 100.0D)); weight += 1_000L;
        if (unlocked) weight += 17_000L; // Both black-hole entries have radius zero.

        if (weight < TOTAL_WEIGHT) {
            weighted = weighted.add(weightedRange(TOTAL_WEIGHT - weight, 0.1D, 0.3D));
        }
        return weighted.divide(BigDecimal.valueOf(TOTAL_WEIGHT), 24, RoundingMode.HALF_EVEN);
    }

    private static BigDecimal antimatterExpectedRadiusSquared() {
        BigDecimal weighted = BigDecimal.ZERO;
        weighted = weighted.add(weightedRange(15L, 0.1D, 0.3D));
        weighted = weighted.add(weightedRange(8L, 0.7D, 0.9D));
        weighted = weighted.add(weightedRange(6L, 0.9D, 1.2D));
        weighted = weighted.add(weightedRange(4L, 1.0D, 1.4D));
        weighted = weighted.add(weightedRange(3L, 1.4D, 2.1D));
        weighted = weighted.add(weightedRange(3L, 2.1D, 16.0D));
        weighted = weighted.add(weightedRange(2L, 16.0D, 100.0D));
        weighted = weighted.add(weightedRange(2L, 0.009D, 0.02D));
        weighted = weighted.add(weightedRange(1L, 1.4E-5D, 2.9E-5D));
        weighted = weighted.add(weightedRange(1L, 0.06D, 0.08D));
        weighted = weighted.add(weightedRange(1L, 0.04D, 0.06D));
        weighted = weighted.add(weightedRange(1L, 0.01D, 0.04D));
        weighted = weighted.add(weightedRange(1L, 7.2E-6D, 1.4E-5D));
        weighted = weighted.add(weightedRange(1L, 1.3E-5D, 4.2E-4D));
        weighted = weighted.add(weightedFixed(1L, 1.0E-14D));
        return weighted.divide(BigDecimal.valueOf(50L), 24, RoundingMode.HALF_EVEN);
    }

    private static BigDecimal weightedRange(long weight, double minimum, double maximum) {
        return uniformSquare(minimum, maximum).multiply(BigDecimal.valueOf(weight));
    }

    private static BigDecimal weightedFixed(long weight, double radius) {
        BigDecimal value = BigDecimal.valueOf(radius);
        return value.multiply(value).multiply(BigDecimal.valueOf(weight));
    }

    /** E[X^2] for a continuous uniform distribution on [minimum, maximum]. */
    private static BigDecimal uniformSquare(double minimum, double maximum) {
        BigDecimal a = BigDecimal.valueOf(minimum);
        BigDecimal b = BigDecimal.valueOf(maximum);
        return a.multiply(a).add(a.multiply(b)).add(b.multiply(b))
                .divide(BigDecimal.valueOf(3L), 30, RoundingMode.HALF_EVEN);
    }

    private static boolean atLeast(AbsoluteInteger value, long threshold) {
        return value != null && value.compareTo(AbsoluteInteger.parse(Long.toString(threshold))) >= 0;
    }

    private static boolean equalsLong(AbsoluteInteger value, long expected) {
        return value != null && value.compareTo(AbsoluteInteger.parse(Long.toString(expected))) == 0;
    }

    /**
     * 反物质星体随机池（总权重 50）。
     * 每种普通星体对应一种反物质镜像。
     */
    private static StarData rollAntimatter() {
        // 用 0~49 的随机数映射到反物质星体
        int roll = RANDOM.nextInt(50);
        return switch (roll) {
            // 权重 15.0 → 0~14（15个）
            case 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 -> createAntimatterRedDwarf();
            // 权重 8.0 → 15~22（8个）
            case 15, 16, 17, 18, 19, 20, 21, 22 -> createAntimatterOrangeDwarf();
            // 权重 6.0 → 23~28（6个）
            case 23, 24, 25, 26, 27, 28 -> createAntimatterYellowDwarf();
            // 权重 4.0 → 29~32（4个）
            case 29, 30, 31, 32 -> createAntimatterFStar();
            // 权重 3.0 → 33~35（3个）
            case 33, 34, 35 -> createAntimatterAStar();
            // 权重 3.0 → 36~38（3个）
            case 36, 37, 38 -> createAntimatterBStar();
            // 权重 2.0 → 39~40（2个）
            case 39, 40 -> createAntimatterOStar();
            // 权重 2.0 → 41~42（2个）
            case 41, 42 -> createAntimatterWhiteDwarf();
            // 权重 1.5 → 43（1个）
            case 43 -> createAntimatterNeutronStar();
            // 权重 0.8 → 44（1个）
            case 44 -> createAntimatterLDwarf();
            // 权重 0.8 → 45（1个）
            case 45 -> createAntimatterTDwarf();
            // 权重 0.5 → 46（1个）
            case 46 -> createAntimatterYDwarf();
            // 权重 0.5 → 47（1个）
            case 47 -> createAntimatterQuarkStar();
            // 权重 0.3 → 48（1个）
            case 48 -> createAntimatterQStar();
            // 权重 0.2 → 49（1个）
            default -> createAntimatterElectroweakStar();
        };
    }

    /**
     * 构建权重池。
     * <p>
     * totalWrapped < 1000 时：黑洞、类星体、耀变体、塞佛特、Q星、夸克星的权重强制归零，
     * 重新分配给 O 型星（+5000）和沃尔夫-拉叶星（+2000）。
     */
    private static List<StarEntry> buildPool(boolean locked) {
        List<StarEntry> pool = new ArrayList<>();

        // ====== 主序星 - 权重 850,000 ======
        // 红矮星(M)：730,000
        pool.add(new StarEntry(locked ? 730000 + 5000 + 2000 : 730000, UniverseRandomizer::createRedDwarf));
        // 橙矮星(K)：60,000
        pool.add(new StarEntry(60000, UniverseRandomizer::createOrangeDwarf));
        // 黄矮星(G)：50,000
        pool.add(new StarEntry(50000, UniverseRandomizer::createYellowDwarf));
        // F型星：30,000
        pool.add(new StarEntry(30000, UniverseRandomizer::createFStar));
        // A型星：25,000
        pool.add(new StarEntry(25000, UniverseRandomizer::createAStar));
        // B型星：20,000
        pool.add(new StarEntry(20000, UniverseRandomizer::createBStar));
        // O型星：10,000（锁定状态 +5000 → 15,000）
        pool.add(new StarEntry(locked ? 10000 + 5000 : 10000, UniverseRandomizer::createOStar));

        // ====== 棕矮星 - 权重 5,000 ======
        pool.add(new StarEntry(2000, UniverseRandomizer::createLDwarf));
        pool.add(new StarEntry(2000, UniverseRandomizer::createTDwarf));
        pool.add(new StarEntry(1000, UniverseRandomizer::createYDwarf));

        // ====== 致密星与假想星 - 权重 38,000 ======
        pool.add(new StarEntry(18000, UniverseRandomizer::createWhiteDwarf));
        pool.add(new StarEntry(10000, UniverseRandomizer::createNeutronStar));
        // 夸克星：5,000（锁定归零，分配给 O 型星和 WR 星）
        if (!locked) pool.add(new StarEntry(5000, UniverseRandomizer::createQuarkStar));
        // Q星/灰洞：3,000（锁定归零）
        if (!locked) pool.add(new StarEntry(3000, UniverseRandomizer::createQStar));
        // 电弱星：1,000
        pool.add(new StarEntry(1000, UniverseRandomizer::createElectroweakStar));
        // 玻色星：1,000
        pool.add(new StarEntry(1000, UniverseRandomizer::createBosonStar));

        // ====== 星系核与现象 - 权重 6,000（锁定部分归零） ======
        // 塞佛特星系核：1,000（锁定归零）
        if (!locked) pool.add(new StarEntry(1000, UniverseRandomizer::createSeyfert));
        // 耀变体：1,000（锁定归零）
        if (!locked) pool.add(new StarEntry(1000, UniverseRandomizer::createBlazar));
        // 类星体：500（锁定归零）
        if (!locked) pool.add(new StarEntry(500, UniverseRandomizer::createQuasar));
        // 红超巨星：2,000
        pool.add(new StarEntry(2000, UniverseRandomizer::createRedSupergiant));
        // 沃尔夫-拉叶星：1,000（锁定状态 +2000 → 3,000）
        pool.add(new StarEntry(locked ? 1000 + 2000 : 1000, UniverseRandomizer::createWolfRayet));
        // 热木星：1,000
        pool.add(new StarEntry(1000, UniverseRandomizer::createHotJupiter));
        // 超级地球：1,000
        pool.add(new StarEntry(1000, UniverseRandomizer::createSuperEarth));
        // 蓝离散星：2,000
        pool.add(new StarEntry(2000, UniverseRandomizer::createBlueStraggler));
        // 伽马射线暴(GRB)：500
        pool.add(new StarEntry(500, UniverseRandomizer::createGRB));
        // 超新星前身：2,000（已包含在红超巨星中）
        // 极超新星前身：1,000
        pool.add(new StarEntry(1000, UniverseRandomizer::createHypernova));

        // ====== 黑洞（解锁后） - 权重 17,000 ======
        if (!locked) {
            pool.add(new StarEntry(10000, UniverseRandomizer::createStellarBlackHole));
            pool.add(new StarEntry(7000, UniverseRandomizer::createSupermassiveBlackHole));
        }

        return List.copyOf(pool);
    }

    // ==================== 星体创建方法 ====================

    // ----- 主序星 -----
    private static StarData createRedDwarf() {
        double r = 0.1 + RANDOM.nextDouble() * 0.2;
        double m = 0.08 + RANDOM.nextDouble() * 0.52;
        double t = 2500 + RANDOM.nextDouble() * 1500;
        return new StarData("M_TYPE", "红矮星", r, m, t, 0, false);
    }

    private static StarData createOrangeDwarf() {
        double r = 0.7 + RANDOM.nextDouble() * 0.2;
        double m = 0.6 + RANDOM.nextDouble() * 0.3;
        double t = 4000 + RANDOM.nextDouble() * 1200;
        return new StarData("K_TYPE", "橙矮星", r, m, t, 0, false);
    }

    private static StarData createYellowDwarf() {
        double r = 0.9 + RANDOM.nextDouble() * 0.3;
        double m = 0.9 + RANDOM.nextDouble() * 0.3;
        double t = 5200 + RANDOM.nextDouble() * 800;
        return new StarData("G_TYPE", "黄矮星", r, m, t, 0, false);
    }

    private static StarData createFStar() {
        double r = 1.0 + RANDOM.nextDouble() * 0.4;
        double m = 1.0 + RANDOM.nextDouble() * 0.4;
        double t = 6000 + RANDOM.nextDouble() * 1500;
        return new StarData("F_TYPE", "F型星", r, m, t, 0, false);
    }

    private static StarData createAStar() {
        double r = 1.4 + RANDOM.nextDouble() * 0.7;
        double m = 1.4 + RANDOM.nextDouble() * 0.7;
        double t = 7500 + RANDOM.nextDouble() * 2500;
        return new StarData("A_TYPE", "A型星", r, m, t, 0, false);
    }

    private static StarData createBStar() {
        double r = 2.1 + RANDOM.nextDouble() * 13.9;
        double m = 2.1 + RANDOM.nextDouble() * 13.9;
        double t = 10000 + RANDOM.nextDouble() * 20000;
        return new StarData("B_TYPE", "B型星", r, m, t, 0, false);
    }

    private static StarData createOStar() {
        double r = 16 + RANDOM.nextDouble() * 84;
        double m = 16 + RANDOM.nextDouble() * 84;
        double t = 30000 + RANDOM.nextDouble() * 30000;
        return new StarData("O_TYPE", "O型蓝超巨星", r, m, t, 0, true);
    }

    // ----- 棕矮星 -----
    private static StarData createLDwarf() {
        double r = 0.06 + RANDOM.nextDouble() * 0.02;
        double m = 0.05 + RANDOM.nextDouble() * 0.03;
        double t = 1300 + RANDOM.nextDouble() * 1200;
        return new StarData("L_DWARF", "L型棕矮星", r, m, t, 0, false);
    }

    private static StarData createTDwarf() {
        double r = 0.04 + RANDOM.nextDouble() * 0.02;
        double m = 0.01 + RANDOM.nextDouble() * 0.04;
        double t = 700 + RANDOM.nextDouble() * 600;
        return new StarData("T_DWARF", "T型棕矮星", r, m, t, 0, false);
    }

    private static StarData createYDwarf() {
        double r = 0.01 + RANDOM.nextDouble() * 0.03;
        double m = 0.001 + RANDOM.nextDouble() * 0.009;
        double t = 100 + RANDOM.nextDouble() * 600;
        return new StarData("Y_DWARF", "Y型棕矮星", r, m, t, 0, false);
    }

    // ----- 致密星 -----
    private static StarData createWhiteDwarf() {
        double r = 0.009 + RANDOM.nextDouble() * 0.011;
        double m = 0.3 + RANDOM.nextDouble() * 1.1;
        double t = 10000 + RANDOM.nextDouble() * 90000;
        return new StarData("WHITE_DWARF", "白矮星", r, m, t, 0, false);
    }

    private static StarData createNeutronStar() {
        double r = 1.4e-5 + RANDOM.nextDouble() * 1.5e-5;
        double m = 1.4 + RANDOM.nextDouble() * 0.8;
        double t = 600000;
        return new StarData("NEUTRON_STAR", "中子星", r, m, t, 0, true);
    }

    private static StarData createQuarkStar() {
        double r = 7.2e-6 + RANDOM.nextDouble() * 6.8e-6;
        double m = 1.5 + RANDOM.nextDouble() * 1.0;
        double t = 1_000_000;
        return new StarData("QUARK_STAR", "夸克星", r, m, t, 0, true);
    }

    private static StarData createQStar() {
        double r = 1.3e-5 + RANDOM.nextDouble() * 4.07e-4;
        double m = 3 + RANDOM.nextDouble() * 97;
        double t = 500_000;
        return new StarData("Q_STAR", "Q星/灰洞", r, m, t, 0, true);
    }

    private static StarData createElectroweakStar() {
        double r = 1e-14;
        double m = 3.0e-6; // 约地球质量（M☉ 单位）
        double t = 0; // 温度不可用，特殊处理
        return new StarData("ELECTROWEAK_STAR", "电弱星", r, m, t, 0, true);
    }

    private static StarData createBosonStar() {
        double r = 1 + RANDOM.nextDouble() * 999;
        double m = 10 + RANDOM.nextDouble() * 990;
        double t = 3000;
        return new StarData("BOSON_STAR", "玻色星", r, m, t, 0, true);
    }

    // ----- 星系核与现象 -----
    private static StarData createSeyfert() {
        double m = 1e6 + RANDOM.nextDouble() * 9e8;
        double t = 0;
        return new StarData("SEYFERT", "塞佛特星系核", 1000, m, t, 0, true);
    }

    private static StarData createBlazar() {
        double m = 1e8 + RANDOM.nextDouble() * 9.9e9;
        double t = 0;
        return new StarData("BLAZAR", "耀变体", 1000, m, t, 0, true);
    }

    private static StarData createQuasar() {
        double m = 1e7 + RANDOM.nextDouble() * 9.99e10;
        double t = 0;
        return new StarData("QUASAR", "类星体", 18600, m, t, 0, true);
    }

    private static StarData createRedSupergiant() {
        double r = 100 + RANDOM.nextDouble() * 1400;
        double m = 8 + RANDOM.nextDouble() * 12;
        double t = 3000 + RANDOM.nextDouble() * 1000;
        return new StarData("RED_SUPERGIANT", "红超巨星", r, m, t, 0, true);
    }

    private static StarData createWolfRayet() {
        double r = 10 + RANDOM.nextDouble() * 90;
        double m = 25 + RANDOM.nextDouble() * 75;
        double t = 50000 + RANDOM.nextDouble() * 150000;
        return new StarData("WOLF_RAYET", "沃尔夫-拉叶星", r, m, t, 0, true);
    }

    private static StarData createHotJupiter() {
        double r = 0.1 + RANDOM.nextDouble() * 0.05;
        double m = 0.0005 + RANDOM.nextDouble() * 0.002; // 0.5~2 木星质量（M☉ 单位）
        double t = 2000;
        return new StarData("HOT_JUPITER", "热木星", r, m, t, 0, false);
    }

    private static StarData createSuperEarth() {
        double r = 0.01 + RANDOM.nextDouble() * 0.01;
        double m = 3e-6 + RANDOM.nextDouble() * 3e-5; // 1~10 地球质量（M☉ 单位）
        double t = 500;
        return new StarData("SUPER_EARTH", "超级地球", r, m, t, 0, false);
    }

    private static StarData createBlueStraggler() {
        double r = 1 + RANDOM.nextDouble() * 9;
        double m = 1 + RANDOM.nextDouble() * 2;
        double t = 7000 + RANDOM.nextDouble() * 3000;
        return new StarData("BLUE_STRAGGLER", "蓝离散星", r, m, t, 0, true);
    }

    private static StarData createGRB() {
        double r = 0;
        double m = 0;
        double t = 0;
        return new StarData("GRB", "伽马射线暴", r, m, t, 0, true);
    }

    private static StarData createHypernova() {
        double r = 10 + RANDOM.nextDouble() * 90;
        double m = 25 + RANDOM.nextDouble() * 75;
        double t = 50000 + RANDOM.nextDouble() * 150000;
        return new StarData("HYPERNOVA", "极超新星", r, m, t, 0, true);
    }

    // ----- 黑洞（解锁后） -----
    private static StarData createStellarBlackHole() {
        double m = 3 + RANDOM.nextDouble() * 97;
        double t = 0;
        return new StarData("STELLAR_BH", "恒星级黑洞", 0, m, t, 0, true);
    }

    private static StarData createSupermassiveBlackHole() {
        double m = 1e5 + RANDOM.nextDouble() * 9.99e5;
        double t = 0;
        return new StarData("SUPERMASSIVE_BH", "超大质量黑洞", 0, m, t, 0, true);
    }

    // ==================== 反物质星体创建方法 ====================
    // 反物质星体与对应普通星体质量/半径/温度完全相同，仅类型名加 "ANTI_" 前缀
    // 反物质星体不建造戴森球，通过投料湮灭释放能量

    private static StarData createAntimatterRedDwarf() {
        double r = 0.1 + RANDOM.nextDouble() * 0.2;
        double m = 0.08 + RANDOM.nextDouble() * 0.52;
        double t = 2500 + RANDOM.nextDouble() * 1500;
        return new StarData("ANTI_M_TYPE", "反物质红矮星", r, m, t, 0, true);
    }

    private static StarData createAntimatterOrangeDwarf() {
        double r = 0.7 + RANDOM.nextDouble() * 0.2;
        double m = 0.6 + RANDOM.nextDouble() * 0.3;
        double t = 4000 + RANDOM.nextDouble() * 1200;
        return new StarData("ANTI_K_TYPE", "反物质橙矮星", r, m, t, 0, true);
    }

    private static StarData createAntimatterYellowDwarf() {
        double r = 0.9 + RANDOM.nextDouble() * 0.3;
        double m = 0.9 + RANDOM.nextDouble() * 0.3;
        double t = 5200 + RANDOM.nextDouble() * 800;
        return new StarData("ANTI_G_TYPE", "反物质黄矮星", r, m, t, 0, true);
    }

    private static StarData createAntimatterFStar() {
        double r = 1.0 + RANDOM.nextDouble() * 0.4;
        double m = 1.0 + RANDOM.nextDouble() * 0.4;
        double t = 6000 + RANDOM.nextDouble() * 1500;
        return new StarData("ANTI_F_TYPE", "反物质F型星", r, m, t, 0, true);
    }

    private static StarData createAntimatterAStar() {
        double r = 1.4 + RANDOM.nextDouble() * 0.7;
        double m = 1.4 + RANDOM.nextDouble() * 0.7;
        double t = 7500 + RANDOM.nextDouble() * 2500;
        return new StarData("ANTI_A_TYPE", "反物质A型星", r, m, t, 0, true);
    }

    private static StarData createAntimatterBStar() {
        double r = 2.1 + RANDOM.nextDouble() * 13.9;
        double m = 2.1 + RANDOM.nextDouble() * 13.9;
        double t = 10000 + RANDOM.nextDouble() * 20000;
        return new StarData("ANTI_B_TYPE", "反物质B型星", r, m, t, 0, true);
    }

    private static StarData createAntimatterOStar() {
        double r = 16 + RANDOM.nextDouble() * 84;
        double m = 16 + RANDOM.nextDouble() * 84;
        double t = 30000 + RANDOM.nextDouble() * 30000;
        return new StarData("ANTI_O_TYPE", "反物质O型星", r, m, t, 0, true);
    }

    private static StarData createAntimatterLDwarf() {
        double r = 0.06 + RANDOM.nextDouble() * 0.02;
        double m = 0.05 + RANDOM.nextDouble() * 0.03;
        double t = 1300 + RANDOM.nextDouble() * 1200;
        return new StarData("ANTI_L_DWARF", "反物质L型棕矮星", r, m, t, 0, true);
    }

    private static StarData createAntimatterTDwarf() {
        double r = 0.04 + RANDOM.nextDouble() * 0.02;
        double m = 0.01 + RANDOM.nextDouble() * 0.04;
        double t = 700 + RANDOM.nextDouble() * 600;
        return new StarData("ANTI_T_DWARF", "反物质T型棕矮星", r, m, t, 0, true);
    }

    private static StarData createAntimatterYDwarf() {
        double r = 0.01 + RANDOM.nextDouble() * 0.03;
        double m = 0.001 + RANDOM.nextDouble() * 0.009;
        double t = 100 + RANDOM.nextDouble() * 600;
        return new StarData("ANTI_Y_DWARF", "反物质Y型棕矮星", r, m, t, 0, true);
    }

    private static StarData createAntimatterWhiteDwarf() {
        double r = 0.009 + RANDOM.nextDouble() * 0.011;
        double m = 0.3 + RANDOM.nextDouble() * 1.1;
        double t = 10000 + RANDOM.nextDouble() * 90000;
        return new StarData("ANTI_WHITE_DWARF", "反物质白矮星", r, m, t, 0, true);
    }

    private static StarData createAntimatterNeutronStar() {
        double r = 1.4e-5 + RANDOM.nextDouble() * 1.5e-5;
        double m = 1.4 + RANDOM.nextDouble() * 0.8;
        double t = 600000;
        return new StarData("ANTI_NEUTRON_STAR", "反物质中子星", r, m, t, 0, true);
    }

    private static StarData createAntimatterQuarkStar() {
        double r = 7.2e-6 + RANDOM.nextDouble() * 6.8e-6;
        double m = 1.5 + RANDOM.nextDouble() * 1.0;
        double t = 1_000_000;
        return new StarData("ANTI_QUARK_STAR", "反物质夸克星", r, m, t, 0, true);
    }

    private static StarData createAntimatterQStar() {
        double r = 1.3e-5 + RANDOM.nextDouble() * 4.07e-4;
        double m = 3 + RANDOM.nextDouble() * 97;
        double t = 500_000;
        return new StarData("ANTI_Q_STAR", "反物质Q星", r, m, t, 0, true);
    }

    private static StarData createAntimatterElectroweakStar() {
        double r = 1e-14;
        double m = 3.0e-6;
        double t = 0;
        return new StarData("ANTI_ELECTROWEAK_STAR", "反物质电弱星", r, m, t, 0, true);
    }

    // ==================== 内部辅助类 ====================

    /** 权重池条目 */
    private record StarEntry(int weight, StarCreator creator) {
    }

    /** 星体创建函数接口 */
    @FunctionalInterface
    private interface StarCreator {
        StarData create();
    }

    // ==================== 特殊星体创建 ====================

    /**
     * 暗物质星（totalWrapped >= 3000，权重 100）
     * 产出 0，但包裹后所有戴森球产出永久 ×2（可叠加）
     */
    private static StarData createDarkMatterStar() {
        double r = 0.5 + RANDOM.nextDouble() * 2;
        double m = 0.1 + RANDOM.nextDouble() * 10;
        double t = 0;
        return new StarData("DARK_MATTER", "暗物质星", r, m, t, 0, true);
    }

    /**
     * 普朗克星（totalWrapped >= 10000，权重 50）
     * 产出 10⁴²~10⁴⁶ FE/s，需量子结构光束 ×1000 组包裹
     */
    private static StarData createPlanckStar() {
        double r = 1.6e-35; // 普朗克长度
        double m = 1.6e-35 / (AstrophysicalCalculator.SOLAR_MASS_KG * AstrophysicalCalculator.SOLAR_RADIUS_M);
        double t = 1.42e32; // 普朗克温度
        return new StarData("PLANCK", "普朗克星", r, m, t, 0, true);
    }

    /**
     * 寰宇之心（totalWrapped >= 5,000,000,000，概率 1/1亿）
     * 不产能量，产规则修改权。
     * 注意：寰宇之心不再通过随机池生成，而是通过 CosmicHeart.tryActivate() 检查激活。
     */
    private static StarData createCosmicHeart() {
        return new StarData("COSMIC_HEART", "寰宇之心", 0, 0, 0, 0, true);
    }
}

