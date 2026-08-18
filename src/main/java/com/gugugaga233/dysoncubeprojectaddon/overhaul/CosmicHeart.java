package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;
import sonar.fluxnetworks.api.energy.BigNumber;

/**
 * 寰宇之心（CosmicHeart）— 覆盖进度系统
 * <p>
 * 玩家包裹"星寰"层级（totalWrapped ≥ 5B）后，极低概率（1/1亿）激活寰宇之心。
 * 激活后，玩家需投入海量材料覆盖其表面，覆盖面积达到目标后触发规则修改界面。
 * <p>
 * 设计要点：
 * <ul>
 *   <li>目标面积：π × (longmax 光年)² ≈ 2.39×10⁷⁰ m²（理论值）</li>
 *   <li>每投入 1 份材料 → +1 m² 覆盖</li>
 *   <li>单份消耗：long.MAX_VALUE 倍正常消耗（即极其昂贵）</li>
 *   <li>完成时弹出 7 种规则修改选项</li>
 * </ul>
 */
public final class CosmicHeart {

    private static final int STRUCTURE_REQUIREMENT_VERSION = 1;

    /** 出现概率：1/1亿 */
    public static final long APPEARANCE_PROBABILITY = 100_000_000L;
    /** 目标面积：≈ 2.39×10⁷⁰ m²（π × (Long.MAX_VALUE 光年)²） */
    public static final BigDecimal TARGET_AREA = new BigDecimal("2390000000000000000000000000000000000000000000000000000000000000000000000000");
    /** 每份投入覆盖面积（m²） */
    public static final double AREA_PER_UNIT = 1.0;
    /** 单份投入消耗倍数：long.max（理论值，实际用 BigDecimal 处理） */
    public static final BigDecimal SINGLE_INPUT_COST = BigDecimal.valueOf(Long.MAX_VALUE);
    /** Exact area and per-unit cost factor applied to the replaced star's normal materials. */
    private static final BigNumber STRUCTURE_REQUIREMENT_FACTOR =
            new BigNumber(TARGET_AREA).multiply(new BigNumber(SINGLE_INPUT_COST));

    /** 是否已激活（包裹星寰后概率触发） */
    private boolean active = false;
    /** 是否已完成覆盖 */
    private boolean complete = false;
    /** 是否已触发完成通知（防止重复触发） */
    private boolean triggeredComplete = false;
    /** Number of rare-event hits represented by the current aggregate structure target. */
    private AbsoluteInteger batchCount = new AbsoluteInteger();
    /** 当前覆盖面积（m²） */
    private BigDecimal coveredArea = BigDecimal.ZERO;
    /** Dyson sphere whose current target was replaced by the Cosmic Heart. */
    private String targetSphereId = "";
    /** Structure requirements and accepted materials use Flux's absolute exponent precision. */
    private BigNumber requiredBeams = new BigNumber(0);
    private BigNumber requiredSolarPanels = new BigNumber(0);
    private BigNumber installedBeams = new BigNumber(0);
    private BigNumber installedSolarPanels = new BigNumber(0);
    private int structureRequirementVersion = 0;
    /** 已应用的规则修改 */
    private final java.util.Map<String, AbsoluteInteger> appliedModifications = new java.util.HashMap<>();
    /** Future completed hearts consume one entry from this persisted reward plan. */
    private final AbsoluteInteger[] plannedRewards = createRewardCounters();

    public CosmicHeart() {}

    // ====== 激活与检查 ======

    /**
     * 尝试激活寰宇之心。
     * 仅在 totalWrapped ≥ 星寰阈值且随机数命中时返回 true。
     */
    public static boolean tryActivate(long totalWrapped) {
        if (totalWrapped < UniverseHierarchy.Level.STAR_UNIVERSE.unlockThreshold) {
            return false;
        }
        return Math.random() < (1.0 / APPEARANCE_PROBABILITY);
    }

    /** 激活寰宇之心 */
    public void activate() {
        if (this.active || this.complete) return;
        this.batchCount = absolute(1L);
        this.active = true;
    }

    public void activate(String sphereId, BigNumber beams, BigNumber solarPanels) {
        activateBatch(sphereId, beams, solarPanels, absolute(1L));
    }

    public void activateBatch(String sphereId, BigNumber beams, BigNumber solarPanels,
                              AbsoluteInteger count) {
        if (this.active || this.complete) return;
        if (count == null || count.isZero()) return;
        this.batchCount = count.copy();
        BigNumber countNumber = FluxMath8.toBigNumber(count);
        this.targetSphereId = sphereId == null ? "" : sphereId;
        this.requiredBeams = scaleStructureRequirement(beams).multiply(countNumber.deepCopy());
        this.requiredSolarPanels = scaleStructureRequirement(solarPanels).multiply(countNumber);
        this.installedBeams = new BigNumber(0);
        this.installedSolarPanels = new BigNumber(0);
        this.coveredArea = BigDecimal.ZERO;
        this.structureRequirementVersion = STRUCTURE_REQUIREMENT_VERSION;
        this.active = true;
    }

    /** 是否已激活 */
    public boolean isActive() { return this.active; }

    /** 是否已完成覆盖 */
    public boolean isComplete() { return this.complete; }

    public AbsoluteInteger getBatchCountExact() { return this.batchCount.copy(); }

    public String getBatchCountDisplay() {
        return NumberUtils.getScientificInteger(this.batchCount);
    }

    public String getTargetSphereId() { return this.targetSphereId; }

    public boolean isTargetFor(String sphereId) {
        String candidate = sphereId == null ? "" : sphereId;
        return this.targetSphereId.isEmpty() || this.targetSphereId.equals(candidate);
    }

    /** Claims legacy active saves that predate target ownership and material requirements. */
    public boolean ensureStructureTarget(String sphereId, BigNumber beams, BigNumber solarPanels) {
        if (!this.active || this.complete || !isTargetFor(sphereId)) return false;
        if (this.targetSphereId.isEmpty()) this.targetSphereId = sphereId == null ? "" : sphereId;
        if (this.structureRequirementVersion < STRUCTURE_REQUIREMENT_VERSION) {
            this.requiredBeams = this.requiredBeams.isZero()
                    ? scaleStructureRequirement(beams)
                    : scaleStructureRequirement(this.requiredBeams);
            this.requiredSolarPanels = this.requiredSolarPanels.isZero()
                    ? scaleStructureRequirement(solarPanels)
                    : scaleStructureRequirement(this.requiredSolarPanels);
            this.structureRequirementVersion = STRUCTURE_REQUIREMENT_VERSION;
        }
        return true;
    }

    /** Moves structure reserves into the heart and completes only when both normal costs are met. */
    public boolean acceptStructureMaterials(BigNumber beamReserve, BigNumber solarPanelReserve) {
        if (!this.active || this.complete) return false;
        this.installedBeams = transferMissing(this.requiredBeams, this.installedBeams, beamReserve);
        this.installedSolarPanels = transferMissing(
                this.requiredSolarPanels, this.installedSolarPanels, solarPanelReserve);
        if (this.installedBeams.compareTo(this.requiredBeams) >= 0
                && this.installedSolarPanels.compareTo(this.requiredSolarPanels) >= 0) {
            this.active = false;
            this.complete = true;
            this.coveredArea = TARGET_AREA;
            return true;
        }
        return false;
    }

    public BigNumber getRequiredBeams() { return this.requiredBeams.deepCopy(); }
    public BigNumber getRequiredSolarPanels() { return this.requiredSolarPanels.deepCopy(); }
    public BigNumber getInstalledBeams() { return this.installedBeams.deepCopy(); }
    public BigNumber getInstalledSolarPanels() { return this.installedSolarPanels.deepCopy(); }
    public String getBeamProgressDisplay() {
        return formatBigNumber(this.installedBeams) + " / " + formatBigNumber(this.requiredBeams);
    }
    public String getSolarPanelProgressDisplay() {
        return formatBigNumber(this.installedSolarPanels) + " / "
                + formatBigNumber(this.requiredSolarPanels);
    }

    // ====== 投料 ======

    /**
     * 投入材料，推进覆盖面积。
     *
     * @param units 投入份数
     * @return 本次推进的面积（m²），若材料不足或已完成则返回 0
     */
    public BigDecimal coverArea(long units) {
        return coverArea(BigDecimal.valueOf(units));
    }

    public BigDecimal coverArea(BigDecimal units) {
        if (!this.active || this.complete || units.signum() <= 0) return BigDecimal.ZERO;
        BigDecimal added = units.multiply(BigDecimal.valueOf(AREA_PER_UNIT)).min(getRemainingArea());
        this.coveredArea = this.coveredArea.add(added);
        if (this.coveredArea.compareTo(TARGET_AREA) >= 0) {
            this.coveredArea = TARGET_AREA;
            this.active = false;
            this.complete = true;
        }
        return added;
    }

    /** 当前覆盖进度（0~100） */
    public BigDecimal getProgress() {
        if (this.complete) return BigDecimal.valueOf(100);
        if (!this.requiredBeams.isZero() || !this.requiredSolarPanels.isZero()) {
            int beamProgress = scaledProgress(this.installedBeams, this.requiredBeams, 1_000_000);
            int sailProgress = scaledProgress(this.installedSolarPanels, this.requiredSolarPanels, 1_000_000);
            return BigDecimal.valueOf((long) beamProgress + sailProgress)
                    .divide(BigDecimal.valueOf(20_000L), 6, RoundingMode.HALF_UP);
        }
        if (TARGET_AREA.signum() == 0) return BigDecimal.ZERO;
        return this.coveredArea.multiply(BigDecimal.valueOf(100)).divide(
                TARGET_AREA, 6, RoundingMode.HALF_UP);
    }

    /** 已覆盖面积显示字符串 */
    public String getCoveredAreaDisplay() {
        if (!this.requiredBeams.isZero() || !this.requiredSolarPanels.isZero()) {
            return "Beams " + formatBigNumber(this.installedBeams) + " / " + formatBigNumber(this.requiredBeams)
                    + "; Sails " + formatBigNumber(this.installedSolarPanels) + " / "
                    + formatBigNumber(this.requiredSolarPanels);
        }
        return formatBigDecimal(this.coveredArea) + " / " + formatBigDecimal(TARGET_AREA) + " m²";
    }

    /** 剩余需覆盖面积 */
    public BigDecimal getRemainingArea() {
        return TARGET_AREA.subtract(this.coveredArea).max(BigDecimal.ZERO);
    }

    // ====== 规则修改 ======

    /** 规则修改选项 */
    public enum RuleModification {
        MULTIPLIER_BASE_X100("寰宇大道基数 ×10 → ×100", true),
        TOTAL_WRAPPED_PLUS_1M("totalWrapped 直接 +10⁶", "星体数量直接增加 100 万颗", true),
        ALL_STAR_OUTPUT_X2("所有星体产出 永久 ×2", true),
        RARE_STAR_PROBABILITY_X2("稀有星体概率 ×2", true),
        LAYER_UNLOCK_THRESHOLD_HALVE("层级解锁阈值 全部减半", "所有恒星层级效果 ×2", true),
        ANTIMATTER_THRESHOLD_HALVE("反物质解锁阈值 减半", "反物质效果 ×2", true),
        SELF_PROBABILITY_X10("自身出现概率 ×10", true);

        /** 存档兼容键。旧存档曾直接使用显示名称作为键，不可随意修改。 */
        public final String name;
        /** 玩家可见名称。 */
        public final String displayName;
        /** 是否可重复 */
        public final boolean repeatable;

        RuleModification(String name, boolean repeatable) {
            this(name, name, repeatable);
        }

        RuleModification(String name, String displayName, boolean repeatable) {
            this.name = name;
            this.displayName = displayName;
            this.repeatable = repeatable;
        }
    }

    /** 已应用的规则修改列表 */
    public java.util.Map<String, Integer> getAppliedModifications() {
        java.util.Map<String, Integer> projected = new java.util.HashMap<>();
        this.appliedModifications.forEach((key, value) ->
                projected.put(key, FluxMath8.toIntSaturated(value)));
        return projected;
    }

    /** 应用规则修改 */
    public boolean applyModification(RuleModification mod) {
        return applyModification(mod, absolute(1L));
    }

    public boolean applyModification(RuleModification mod, AbsoluteInteger amount) {
        if (mod == null || amount == null || amount.isZero()) return false;
        AbsoluteInteger current = this.appliedModifications.get(mod.name);
        if (!mod.repeatable && (amount.compareTo(absolute(1L)) > 0
                || current != null && !current.isZero())) return false;
        if (current == null) {
            this.appliedModifications.put(mod.name, amount.copy());
        } else {
            FluxMath8.addInPlace(current, amount);
        }
        return true;
    }

    /** Replaces the automatic reward plan. The current completed heart still pays for its first entry. */
    public boolean setRewardPlan(AbsoluteInteger[] counts) {
        if (!this.complete || counts == null || counts.length != this.plannedRewards.length) {
            return false;
        }
        AbsoluteInteger total = new AbsoluteInteger();
        RuleModification[] rules = RuleModification.values();
        for (int i = 0; i < counts.length; i++) {
            AbsoluteInteger count = counts[i];
            if (count == null) return false;
            if (!rules[i].repeatable
                    && (count.compareTo(absolute(1L)) > 0
                    || !count.isZero() && !getModificationCountExact(rules[i]).isZero())) {
                return false;
            }
            FluxMath8.addInPlace(total, count);
        }
        if (total.compareTo(this.batchCount) < 0) return false;
        for (int i = 0; i < counts.length; i++) this.plannedRewards[i] = counts[i].copy();
        return true;
    }

    /** Claims exactly one completed-heart reward from the plan and prepares the next heart cycle. */
    public RuleModification claimNextPlannedReward() {
        if (!this.complete || this.batchCount.isZero()) return null;
        RuleModification[] rules = RuleModification.values();
        for (int i = 0; i < this.plannedRewards.length; i++) {
            if (this.plannedRewards[i].isZero()) continue;
            RuleModification rule = rules[i];
            if (!rule.repeatable && !getModificationCountExact(rule).isZero()) {
                this.plannedRewards[i] = new AbsoluteInteger();
                continue;
            }
            this.plannedRewards[i].decrement();
            if (!applyModification(rule)) continue;
            this.batchCount.decrement();
            if (this.batchCount.isZero()) resetAfterReward();
            return rule;
        }
        return null;
    }

    /** Claims as many rewards as the current aggregate batch and saved plan allow. */
    public AbsoluteInteger[] claimPlannedRewardBatch() {
        AbsoluteInteger[] claimed = createRewardCounters();
        if (!this.complete || this.batchCount.isZero()) return claimed;

        AbsoluteInteger remaining = this.batchCount.copy();
        RuleModification[] rules = RuleModification.values();
        for (int index = 0; index < this.plannedRewards.length && !remaining.isZero(); index++) {
            AbsoluteInteger available = this.plannedRewards[index];
            if (available.isZero()) continue;
            AbsoluteInteger amount = available.compareTo(remaining) <= 0
                    ? available.copy() : remaining.copy();
            if (!applyModification(rules[index], amount)) continue;
            claimed[index] = amount.copy();
            this.plannedRewards[index] = FluxMath8.subtract(available, amount);
            remaining = FluxMath8.subtract(remaining, amount);
        }
        this.batchCount = remaining;
        if (this.batchCount.isZero()) resetAfterReward();
        return claimed;
    }

    public AbsoluteInteger getPlannedRewardCountExact() {
        AbsoluteInteger total = new AbsoluteInteger();
        for (AbsoluteInteger count : this.plannedRewards) FluxMath8.addInPlace(total, count);
        return total;
    }

    public String getPlannedRewardCountDisplay() {
        return NumberUtils.getScientificInteger(getPlannedRewardCountExact());
    }

    public AbsoluteInteger[] getPlannedRewards() {
        AbsoluteInteger[] copy = createRewardCounters();
        for (int i = 0; i < copy.length; i++) copy[i] = this.plannedRewards[i].copy();
        return copy;
    }

    public String getAppliedRewardCounts() {
        StringBuilder result = new StringBuilder();
        RuleModification[] rules = RuleModification.values();
        for (int i = 0; i < rules.length; i++) {
            if (i > 0) result.append(',');
            result.append(NumberUtils.getScientificInteger(getModificationCountExact(rules[i])));
        }
        return result.toString();
    }

    private void resetAfterReward() {
        this.active = false;
        this.complete = false;
        this.triggeredComplete = false;
        this.coveredArea = BigDecimal.ZERO;
        this.targetSphereId = "";
        this.requiredBeams = new BigNumber(0);
        this.requiredSolarPanels = new BigNumber(0);
        this.installedBeams = new BigNumber(0);
        this.installedSolarPanels = new BigNumber(0);
        this.structureRequirementVersion = STRUCTURE_REQUIREMENT_VERSION;
        this.batchCount = new AbsoluteInteger();
    }

    /** 检查某规则修改是否已应用 */
    public int getModificationCount(RuleModification mod) {
        return FluxMath8.toIntSaturated(getModificationCountExact(mod));
    }

    public AbsoluteInteger getModificationCountExact(RuleModification mod) {
        AbsoluteInteger count = this.appliedModifications.get(mod.name);
        return count == null ? new AbsoluteInteger() : count.copy();
    }

    /** 获取有效乘算基数（规则1：×100 每应用一次 ×10） */
    public double getEffectiveMultiplierBase(double base) {
        int count = getModificationCount(RuleModification.MULTIPLIER_BASE_X100);
        return base * Math.pow(10, count);
    }

    /** 获取所有星体产出倍率（规则3：×2 每应用一次） */
    public double getOutputMultiplier() {
        return FluxMath8.toDoubleSaturated(getOutputMultiplierExact());
    }

    public BigNumber getOutputMultiplierExact() {
        return AstrophysicalCalculator.powerOfTwoMultiplier(
                getModificationCountExact(RuleModification.ALL_STAR_OUTPUT_X2));
    }

    public AbsoluteInteger getHierarchyEffectLayersExact() {
        return getModificationCountExact(RuleModification.LAYER_UNLOCK_THRESHOLD_HALVE);
    }

    public BigNumber getAntimatterEffectMultiplierExact() {
        return AstrophysicalCalculator.powerOfTwoMultiplier(
                getModificationCountExact(RuleModification.ANTIMATTER_THRESHOLD_HALVE));
    }

    /**
     * 检查是否已完成覆盖并触发规则选择界面。
     *
     * @param player 触发玩家
     * @return 是否触发规则选择
     */
    public boolean checkAndNotifyComplete(ServerPlayer player) {
        if (this.complete && !this.triggeredComplete) {
            this.triggeredComplete = true;
            return true;
        }
        return false;
    }

    /** Allows a disconnected selector to be replaced without resetting the completed heart. */
    public void resetCompletionNotification() {
        if (this.complete) this.triggeredComplete = false;
    }

    // ====== 序列化 ======

    public String serializeToString() {
        StringBuilder sb = new StringBuilder();
        sb.append(active ? "1" : "0").append("|")
          .append(complete ? "1" : "0").append("|")
          .append(coveredArea.toString()).append("|");
        boolean first = true;
        for (java.util.Map.Entry<String, AbsoluteInteger> entry : this.appliedModifications.entrySet()) {
            if (!first) sb.append(";");
            sb.append(entry.getKey()).append("=")
                    .append(FluxMath8.toIntSaturated(entry.getValue()));
            first = false;
        }
        return sb.toString();
    }

    public static CosmicHeart deserializeFromString(String s) {
        CosmicHeart heart = new CosmicHeart();
        if (s == null || s.isEmpty()) return heart;
        String[] parts = s.split("\\|", 4);
        if (parts.length >= 1) {
            heart.active = parts[0].equals("1");
        }
        if (parts.length >= 2) {
            heart.complete = parts[1].equals("1");
            heart.triggeredComplete = false; // 重新激活时重置
        }
        if (parts.length >= 3) {
            try { heart.coveredArea = new BigDecimal(parts[2]); } catch (NumberFormatException ignored) {}
        }
        if (parts.length >= 4 && !parts[3].isEmpty()) {
            for (String part : parts[3].split(";")) {
                String[] kv = part.split("=");
                if (kv.length == 2) {
                    try {
                        heart.appliedModifications.put(kv[0],
                                absolute(Integer.parseInt(kv[1])));
                    } catch (NumberFormatException ignored) { }
                }
            }
        }
        if (heart.active || heart.complete) heart.batchCount = absolute(1L);
        return heart;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("active", this.active);
        tag.putBoolean("complete", this.complete);
        tag.put("batchCount", FluxMath8.toCompactTag(this.batchCount));
        tag.putString("coveredArea", this.coveredArea.toString());
        tag.putString("targetSphereId", this.targetSphereId);
        tag.put("requiredBeams", this.requiredBeams.toTag());
        tag.put("requiredSolarPanels", this.requiredSolarPanels.toTag());
        tag.put("installedBeams", this.installedBeams.toTag());
        tag.put("installedSolarPanels", this.installedSolarPanels.toTag());
        tag.putInt("structureRequirementVersion", this.structureRequirementVersion);
        CompoundTag modifications = new CompoundTag();
        CompoundTag exactModifications = new CompoundTag();
        this.appliedModifications.forEach((key, value) -> {
            modifications.putInt(key, FluxMath8.toIntSaturated(value));
            exactModifications.put(key, FluxMath8.toCompactTag(value));
        });
        tag.put("modifications", modifications);
        tag.put("modificationsExact", exactModifications);
        CompoundTag rewardPlan = new CompoundTag();
        RuleModification[] rules = RuleModification.values();
        for (int i = 0; i < rules.length; i++) {
            if (!this.plannedRewards[i].isZero()) {
                rewardPlan.put(rules[i].name(), FluxMath8.toCompactTag(this.plannedRewards[i]));
            }
        }
        tag.put("rewardPlan", rewardPlan);
        return tag;
    }

    public static CosmicHeart deserializeNBT(CompoundTag tag) {
        CosmicHeart heart = new CosmicHeart();
        if (tag == null) return heart;
        heart.active = tag.getBoolean("active");
        heart.complete = tag.getBoolean("complete");
        heart.batchCount = tag.contains("batchCount", Tag.TAG_COMPOUND)
                ? FluxMath8.fromCompactTag(tag.getCompound("batchCount"))
                : heart.active || heart.complete ? absolute(1L) : new AbsoluteInteger();
        try {
            heart.coveredArea = new BigDecimal(tag.getString("coveredArea"));
        } catch (NumberFormatException ignored) {
            heart.coveredArea = heart.complete ? TARGET_AREA : BigDecimal.ZERO;
        }
        heart.targetSphereId = tag.getString("targetSphereId");
        heart.requiredBeams = readBigNumber(tag, "requiredBeams");
        heart.requiredSolarPanels = readBigNumber(tag, "requiredSolarPanels");
        heart.installedBeams = readBigNumber(tag, "installedBeams");
        heart.installedSolarPanels = readBigNumber(tag, "installedSolarPanels");
        heart.structureRequirementVersion = tag.getInt("structureRequirementVersion");
        boolean migratedLegacyStructureRequirements =
                heart.structureRequirementVersion < STRUCTURE_REQUIREMENT_VERSION
                        && (!heart.requiredBeams.isZero() || !heart.requiredSolarPanels.isZero());
        if (migratedLegacyStructureRequirements) {
            heart.requiredBeams = scaleStructureRequirement(heart.requiredBeams);
            heart.requiredSolarPanels = scaleStructureRequirement(heart.requiredSolarPanels);
            heart.structureRequirementVersion = STRUCTURE_REQUIREMENT_VERSION;
        }
        CompoundTag exactModifications = tag.getCompound("modificationsExact");
        if (!exactModifications.isEmpty()) {
            for (String key : exactModifications.getAllKeys()) {
                heart.appliedModifications.put(key,
                        FluxMath8.fromCompactTag(exactModifications.getCompound(key)));
            }
        } else {
            CompoundTag modifications = tag.getCompound("modifications");
            for (String key : modifications.getAllKeys()) {
                heart.appliedModifications.put(key, absolute(modifications.getInt(key)));
            }
        }
        CompoundTag rewardPlan = tag.getCompound("rewardPlan");
        RuleModification[] rules = RuleModification.values();
        for (int i = 0; i < rules.length; i++) {
            String key = rules[i].name();
            heart.plannedRewards[i] = rewardPlan.contains(key, Tag.TAG_COMPOUND)
                    ? FluxMath8.fromCompactTag(rewardPlan.getCompound(key))
                    : absolute(Math.max(0, rewardPlan.getInt(key)));
        }
        if (migratedLegacyStructureRequirements && heart.complete
                && heart.appliedModifications.isEmpty()) {
            heart.complete = false;
            heart.active = true;
            heart.triggeredComplete = false;
            heart.coveredArea = BigDecimal.ZERO;
        }
        return heart;
    }

    private static BigNumber readBigNumber(CompoundTag tag, String key) {
        BigNumber value = tag.contains(key, Tag.TAG_COMPOUND)
                ? BigNumber.fromTag(tag.getCompound(key))
                : new BigNumber(0);
        return value.isImmutable() ? value.deepCopy() : value;
    }

    private static AbsoluteInteger[] createRewardCounters() {
        AbsoluteInteger[] counters = new AbsoluteInteger[RuleModification.values().length];
        java.util.Arrays.setAll(counters, ignored -> new AbsoluteInteger());
        return counters;
    }

    private static AbsoluteInteger absolute(long value) {
        return AbsoluteInteger.parse(Long.toString(Math.max(0L, value)));
    }

    private static BigNumber positiveCopy(BigNumber value) {
        return value == null || value.signum() <= 0 ? new BigNumber(0) : value.deepCopy();
    }

    private static BigNumber scaleStructureRequirement(BigNumber normalRequirement) {
        BigNumber positive = positiveCopy(normalRequirement);
        return positive.isZero() ? positive : positive.multiply(STRUCTURE_REQUIREMENT_FACTOR.deepCopy());
    }

    private static BigNumber transferMissing(BigNumber required, BigNumber installed, BigNumber reserve) {
        if (required.isZero() || reserve == null || reserve.isZero()
                || installed.compareTo(required) >= 0) {
            return installed;
        }
        BigNumber missing = required.deepCopy().subtract(installed);
        if (reserve.compareTo(missing) >= 0) {
            reserve.extractContainer(missing);
            return required.deepCopy();
        }

        BigNumber updated = installed.deepCopy().add(reserve.deepCopy());
        if (updated.compareTo(installed) <= 0) {
            return installed;
        }
        reserve.extractContainer(reserve.deepCopy());
        return updated;
    }

    private static int scaledProgress(BigNumber current, BigNumber required, int scale) {
        if (required.isZero() || current.compareTo(required) >= 0) return scale;
        if (current.isZero()) return 0;
        BigNumber scaledCurrent = current.multiplySmallInteger(scale);
        int low = 0;
        int high = scale;
        while (low < high) {
            int midpoint = (low + high + 1) >>> 1;
            if (scaledCurrent.compareTo(required.multiplySmallInteger(midpoint)) >= 0) {
                low = midpoint;
            } else {
                high = midpoint - 1;
            }
        }
        return low;
    }

    private static String formatBigNumber(BigNumber value) {
        return NumberUtils.getCompactBigNumber(value);
    }

    private static String formatBigDecimal(BigDecimal bd) {
        if (bd.signum() == 0) return "0";
        int digits = bd.toBigInteger().toString().length();
        if (digits <= 6) return bd.toBigInteger().toString();
        return bd.toEngineeringString();
    }
}

