package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;

/**
 * 宇宙结构层级系统
 * <p>
 * 层级是 totalWrapped 达标后解锁的"批量包裹权限"，非随机池抽取。
 * 同时记录各层级出现概率（按 totalWrapped 区间）。
 */
public final class UniverseHierarchy {

    public static final long EXPECTATION_SCALE = 1_000L;
    private static final int STAR_UNIVERSE_BASE_BATCH_EXPONENT = 9;
    private static final int MAX_DYNAMIC_BATCH_EXPONENT = 65_536;
    private static final int MAX_EFFECT_DOUBLINGS = 217_706;
    private static final ConcurrentMap<Integer, AbsoluteInteger> POWERS_OF_TEN =
            new ConcurrentHashMap<>();
    private static final ConcurrentMap<Integer, AbsoluteInteger> FIVE_TIMES_POWERS_OF_TEN =
            new ConcurrentHashMap<>();
    private static final ConcurrentMap<BatchEffectKey, AbsoluteInteger> EFFECTIVE_BATCHES =
            new ConcurrentHashMap<>();

    /** 层级枚举 */
    public enum Level {
        SINGLE_STAR("单星体", 0L, 1, 100, 1.0,
                new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}),
        STAR_CLUSTER("星团", 5_000L, 2, 100, 1.0,
                new double[]{0.92, 0.08, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}),
        GALAXY("星系", 50_000L, 100, 10_000, 1.2,
                new double[]{0.85, 0.10, 0.05, 0.0, 0.0, 0.0, 0.0, 0.0}),
        STAR_RIVER("星河", 500_000L, 10_000, 100_000, 1.5,
                new double[]{0.78, 0.12, 0.07, 0.03, 0.0, 0.0, 0.0, 0.0}),
        STAR_HUB("星枢", 5_000_000L, 100_000, 1_000_000, 2.0,
                new double[]{0.72, 0.12, 0.08, 0.05, 0.03, 0.0, 0.0, 0.0}),
        STAR_DOME("星穹", 50_000_000L, 1_000_000, 10_000_000, 3.0,
                new double[]{0.65, 0.12, 0.09, 0.07, 0.05, 0.02, 0.0, 0.0}),
        STAR_DOMAIN("星域", 500_000_000L, 10_000_000, 100_000_000, 5.0,
                new double[]{0.55, 0.12, 0.10, 0.08, 0.07, 0.05, 0.03, 0.0}),
        STAR_UNIVERSE("星寰", 5_000_000_000L, 100_000_000, 1_000_000_000, 10.0,
                new double[]{0.45, 0.12, 0.10, 0.09, 0.08, 0.07, 0.05, 0.04});

        /** 显示名称 */
        public final String name;
        /** 解锁阈值（totalWrapped 达到此值解锁） */
        public final long unlockThreshold;
        /** 单次批量最小星体数 */
        public final int minBatch;
        /** 单次批量最大星体数 */
        public final int maxBatch;
        /** 消耗系数（总消耗放大倍数） */
        public final double costMultiplier;
        /** 各层级出现概率 [single,cluster,g,river,hub,dome,domain,universe] */
        public final double[] appearanceWeights;

        Level(String name, long unlockThreshold, int minBatch, int maxBatch,
              double costMultiplier, double[] weights) {
            this.name = name;
            this.unlockThreshold = unlockThreshold;
            this.minBatch = minBatch;
            this.maxBatch = maxBatch;
            this.costMultiplier = costMultiplier;
            this.appearanceWeights = weights;
        }
    }

    private UniverseHierarchy() {}

    /**
     * 获取当前 totalWrapped 已解锁的最高层级。
     */
    public static Level getUnlockedLevel(long totalWrapped) {
        return getUnlockedLevel(absolute(totalWrapped));
    }

    /** Resolves progression without narrowing an exact wrapped-star counter. */
    public static Level getUnlockedLevel(AbsoluteInteger totalWrapped) {
        AbsoluteInteger wrapped = totalWrapped == null ? new AbsoluteInteger() : totalWrapped;
        Level highest = Level.SINGLE_STAR;
        for (Level level : Level.values()) {
            if (wrapped.compareTo(absolute(level.unlockThreshold)) >= 0) {
                highest = level;
            }
        }
        return highest;
    }

    /**
     * 检查玩家是否已解锁指定层级。
     */
    public static boolean isUnlocked(Level level, long totalWrapped) {
        return isUnlocked(level, absolute(totalWrapped));
    }

    public static boolean isUnlocked(Level level, AbsoluteInteger totalWrapped) {
        return level != null && totalWrapped != null
                && totalWrapped.compareTo(absolute(level.unlockThreshold)) >= 0;
    }

    /**
     * 根据 totalWrapped 获取当前概率分布对应的最高可用层级列表。
     * <p>
     * 遍历所有层级，找到 totalWrapped ≥ unlockThreshold 的层级，
     * 使用其 appearanceWeights 计算各层级出现概率。
     *
     * @param totalWrapped 当前总包裹数
     * @return 当前已解锁的最高层级
     */
    public static Level getActiveLevel(long totalWrapped) {
        return getUnlockedLevel(totalWrapped);
    }

    public static Level getActiveLevel(AbsoluteInteger totalWrapped) {
        return getUnlockedLevel(totalWrapped);
    }

    /**
     * Expected structure-cost multiplier for the active hierarchy, scaled by 1000.
     * This is an integer projection of the configured probability pool.
     */
    public static long expectedCostPermille(AbsoluteInteger totalWrapped) {
        return expectedCostPermille(getUnlockedLevel(totalWrapped));
    }

    public static long expectedCostPermille(Level activeLevel) {
        Level effective = activeLevel == null ? Level.SINGLE_STAR : activeLevel;
        long weightedCost = 0L;
        Level[] levels = Level.values();
        for (int index = 0; index < levels.length; index++) {
            long weightPermille = Math.round(effective.appearanceWeights[index] * EXPECTATION_SCALE);
            long costTenths = Math.round(levels[index].costMultiplier * 10.0D);
            weightedCost += weightPermille * costTenths;
        }
        return weightedCost / 10L;
    }

    /** Returns the next hierarchy unlock, or zero after STAR_UNIVERSE is unlocked. */
    public static long nextUnlockThreshold(AbsoluteInteger totalWrapped) {
        AbsoluteInteger wrapped = totalWrapped == null ? new AbsoluteInteger() : totalWrapped;
        for (Level level : Level.values()) {
            if (wrapped.compareTo(absolute(level.unlockThreshold)) < 0) {
                return level.unlockThreshold;
            }
        }
        return 0L;
    }

    /**
     * 从当前概率分布中随机抽取一个层级（用于批量包裹选择）。
     *
     * @param totalWrapped 当前总包裹数
     * @return 抽取的层级，未解锁时返回 null
     */
    public static Level rollTier(long totalWrapped) {
        // 找到所有已解锁的层级
        java.util.List<Level> unlocked = new java.util.ArrayList<>();
        for (Level level : Level.values()) {
            if (totalWrapped >= level.unlockThreshold) {
                unlocked.add(level);
            }
        }
        if (unlocked.isEmpty()) return Level.SINGLE_STAR;

        // 使用最高已解锁层级的概率分布
        Level highest = unlocked.get(unlocked.size() - 1);
        double[] weights = highest.appearanceWeights;

        // 过滤掉未解锁层级的权重（置零）
        double total = 0;
        double[] adjusted = new double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            if (i < unlocked.size()) {
                adjusted[i] = weights[i];
                total += weights[i];
            }
        }
        if (total <= 0) return Level.SINGLE_STAR;

        double roll = Math.random() * total;
        double cumulative = 0;
        for (int i = 0; i < adjusted.length; i++) {
            cumulative += adjusted[i];
            if (roll < cumulative) {
                return Level.values()[i];
            }
        }
        return Level.values()[adjusted.length - 1];
    }

    /**
     * 计算批量包裹的总消耗（消耗系数 × N）。
     */
    public static double calculateTotalCost(Level level, int batchN) {
        return level.costMultiplier * batchN;
    }

    /**
     * 校验批量包裹数量是否在层级允许范围内。
     */
    public static int clampBatch(Level level, int batchN) {
        return Math.max(level.minBatch, Math.min(level.maxBatch, batchN));
    }

    /**
     * Exact per-settlement cap. Lv.8 remains the final named tier, while its
     * capacity grows tenfold whenever the wrapped count gains another decimal order.
     */
    public static AbsoluteInteger effectiveBatchSize(AbsoluteInteger totalWrapped) {
        AbsoluteInteger wrapped = totalWrapped == null ? new AbsoluteInteger() : totalWrapped;
        Level level = getUnlockedLevel(wrapped);
        if (level != Level.STAR_UNIVERSE) return absolute(level.maxBatch);
        return powerOfTen(resolveBatchExponent(wrapped)).copy();
    }

    /** Applies the repeatable hierarchy reward without looping once per reward layer. */
    public static AbsoluteInteger effectiveBatchSize(AbsoluteInteger totalWrapped,
                                                      AbsoluteInteger effectLayers) {
        AbsoluteInteger base = effectiveBatchSize(totalWrapped);
        if (effectLayers == null || effectLayers.isZero()) return base;
        AbsoluteInteger maximum = powerOfTen(MAX_DYNAMIC_BATCH_EXPONENT);
        if (base.compareTo(maximum) >= 0
                || effectLayers.compareTo(absolute(MAX_EFFECT_DOUBLINGS)) >= 0) {
            return maximum.copy();
        }

        int doublings = FluxMath8.toIntSaturated(effectLayers);
        int baseOrder = FluxMath8.decimalOrder(base);
        BatchEffectKey key = new BatchEffectKey(baseOrder, doublings);
        return EFFECTIVE_BATCHES.computeIfAbsent(key, ignored -> {
            BigInteger multiplied = FluxMath8.toBigInteger(base).shiftLeft(doublings);
            BigInteger cap = FluxMath8.toBigInteger(maximum);
            return multiplied.compareTo(cap) >= 0
                    ? maximum.copy() : FluxMath8.fromBigInteger(multiplied);
        }).copy();
    }

    /** Next tier unlock, or the next Lv.8 tenfold capacity threshold. */
    public static AbsoluteInteger nextBatchThreshold(AbsoluteInteger totalWrapped) {
        AbsoluteInteger wrapped = totalWrapped == null ? new AbsoluteInteger() : totalWrapped;
        Level level = getUnlockedLevel(wrapped);
        if (level != Level.STAR_UNIVERSE) {
            return absolute(nextUnlockThreshold(wrapped));
        }
        int batchExponent = resolveBatchExponent(wrapped);
        return batchExponent >= MAX_DYNAMIC_BATCH_EXPONENT
                ? new AbsoluteInteger()
                : fiveTimesPowerOfTen(batchExponent + 1).copy();
    }

    public static boolean isBatchSizeCapped(AbsoluteInteger totalWrapped) {
        AbsoluteInteger wrapped = totalWrapped == null ? new AbsoluteInteger() : totalWrapped;
        return getUnlockedLevel(wrapped) == Level.STAR_UNIVERSE
                && (FluxMath8.decimalOrder(wrapped) > MAX_DYNAMIC_BATCH_EXPONENT
                || wrapped.compareTo(fiveTimesPowerOfTen(MAX_DYNAMIC_BATCH_EXPONENT)) >= 0);
    }

    public static boolean isBatchSizeCapped(AbsoluteInteger totalWrapped,
                                            AbsoluteInteger effectLayers) {
        return isBatchSizeCapped(totalWrapped)
                || effectiveBatchSize(totalWrapped, effectLayers)
                .compareTo(powerOfTen(MAX_DYNAMIC_BATCH_EXPONENT)) >= 0;
    }

    /**
     * 获取层级的介绍文本。
     */
    public static String getDescription(Level level) {
        return switch (level) {
            case SINGLE_STAR -> "最基础的宇宙单元。一颗孤独的恒星，燃烧着，等待着。";
            case STAR_CLUSTER -> "数颗到上百颗星体在引力束缚下聚集成群。它们一同诞生，一同演化，在宇宙中形成一个微小的'家族'。";
            case GALAXY -> "数千亿颗恒星组成的巨大系统。中心通常盘踞着一头沉睡的巨兽——超大质量黑洞。旋臂在黑暗中缓慢转动，无数文明在其中升起又熄灭。";
            case STAR_RIVER -> "星系的集合。它们被暗物质细丝连接，形成宇宙中可见的最庞大结构之一。包裹星河，意味着你在重塑一片'宇宙网络'的节点。";
            case STAR_HUB -> "星系的枢纽。一个引力与能量极度集中的区域，无数星系围绕着它旋转。这里是宇宙的'轴心'，也是通往更深层结构的门户。";
            case STAR_DOME -> "可观测宇宙的穹顶。它包含数个星枢，将亿万星辰笼罩在一个巨大的时空范围内。包裹星穹，你正在触碰可观测宇宙的边界。";
            case STAR_DOMAIN -> "跨越多重宇宙尺度的疆域。不同的物理常数在这里共存，光速、引力、物质的性质……每一片星域都有自己的'法则'。包裹星域，你开始染指规则本身。";
            case STAR_UNIVERSE -> "所有可能的宇宙形态的终极集合。在星寰面前，'宇宙'一词已失去意义——这里是一切存在与非存在的边界。包裹星寰，是见证'终点之前'的最后一步。";
        };
    }

    private static AbsoluteInteger absolute(long value) {
        return AbsoluteInteger.parse(Long.toString(Math.max(0L, value)));
    }

    private static int resolveBatchExponent(AbsoluteInteger wrapped) {
        int order = FluxMath8.decimalOrder(wrapped);
        if (order > MAX_DYNAMIC_BATCH_EXPONENT) return MAX_DYNAMIC_BATCH_EXPONENT;
        order = Math.max(STAR_UNIVERSE_BASE_BATCH_EXPONENT, order);

        // The logarithmic estimate can only drift at a decimal boundary. Exact
        // comparisons correct it without walking through every lower exponent.
        if (order > STAR_UNIVERSE_BASE_BATCH_EXPONENT
                && wrapped.compareTo(powerOfTen(order)) < 0) {
            order--;
        } else if (order < MAX_DYNAMIC_BATCH_EXPONENT
                && wrapped.compareTo(powerOfTen(order + 1)) >= 0) {
            order++;
        }

        return wrapped.compareTo(fiveTimesPowerOfTen(order)) >= 0
                ? order
                : Math.max(STAR_UNIVERSE_BASE_BATCH_EXPONENT, order - 1);
    }

    private static AbsoluteInteger powerOfTen(int exponent) {
        return POWERS_OF_TEN.computeIfAbsent(exponent,
                key -> FluxMath8.fromBigInteger(BigInteger.TEN.pow(key)));
    }

    private static AbsoluteInteger fiveTimesPowerOfTen(int exponent) {
        return FIVE_TIMES_POWERS_OF_TEN.computeIfAbsent(exponent,
                key -> FluxMath8.fromBigInteger(BigInteger.TEN.pow(key)
                        .multiply(BigInteger.valueOf(5L))));
    }

    private record BatchEffectKey(int baseOrder, int doublings) {
    }
}

