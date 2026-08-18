package com.gugugaga233.dysoncubeprojectaddon.world;

import com.gugugaga233.dysoncubeprojectaddon.Config;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.AntimatterStarData;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.AstrophysicalCalculator;
import sonar.fluxnetworks.api.energy.BigNumber;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.CosmicHeart;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.StarData;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.UniverseHierarchy;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.UniverseRandomizer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.math.BigDecimal;
import java.math.RoundingMode;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;

/**
 * 戴森球结构数据（魔改版）
 * <p>
 * 原模组：存储 beams、solarPanels、storedPower（int），功率 = solarPanels × POWER_PER_SAIL。
 * <p>
 * 魔改扩展：
 * <ul>
 *   <li>totalWrapped — 已包裹星体总数（每完成一颗 +1）</li>
 *   <li>currentStarData — 当前星体完整参数（首个默认太阳）</li>
 *   <li>generatePower() 改为 基础功率 × 10^totalWrapped（double 承载，大数类占位）</li>
 *   <li>completeWrap() — 包裹完成回调，生成新星体</li>
 * </ul>
 * <p>
 * <b>数据类型策略：</b>保留原 int storedPower 用于兼容 Titanium 电网（FE 单位）。
 * 新增 double 缓存功率用于 UI 显示和后续大数类接入。
 * 通量大数类占位：generatePower() 中 double 放大，后续接入大数类时替换转换层。
 */
public class DysonSphereStructure implements INBTSerializable<CompoundTag> {
    private static final int PROGRESS_SCALE = 10_000;
    private static final int DEADLINE_CHECK_INTERVAL = 32;

    private BigNumber beams = new BigNumber(0);
    private BigNumber solarPanels = new BigNumber(0);
    private BigNumber storedEnergy = new BigNumber(0);
    private BigNumber lastConsumedEnergy = new BigNumber(0);
    private BigNumber beamReserve = new BigNumber(0);
    private BigNumber solarPanelReserve = new BigNumber(0);
    private BigNumber requiredSolarPanelsCache;
    private BigNumber requiredBeamsCache;
    private int cachedSailRequirement = -1;
    private int cachedBeamRatio = -1;
    /** One-shot command flag: the next generated star must reveal the Cosmic Heart. */
    private boolean forceCosmicHeartOnNextStar = false;
    /** Persisted hand-off state between star generation and SavedData activation. */
    private boolean forcedCosmicHeartReady = false;
    /** Cosmic Hearts found by aggregate settlements but not yet promoted to the global target. */
    private AbsoluteInteger pendingCosmicHearts = new AbsoluteInteger();

    // ====== 魔改新增字段 ======
    /** 已包裹星体总数（每完成一颗 +1） */
    private AbsoluteInteger totalWrapped = new AbsoluteInteger();
    /** Exact number completed by the latest material-processing call. */
    private AbsoluteInteger lastProcessedWraps = new AbsoluteInteger();
    /** Progress of the current time-budgeted settlement, kept exact for the client overlay. */
    private AbsoluteInteger calculationCompleted = new AbsoluteInteger();
    private AbsoluteInteger calculationCapacity = new AbsoluteInteger();
    /** 0 idle, 1 processing, 2 waiting for materials, 3 complete, 4 paused by Cosmic Heart. */
    private int calculationState;
    /** 当前星体数据（首个默认太阳） */
    private StarData currentStarData = StarData.SUN;
    /** 缓存：当前总功率（FE/s，double），供 UI 显示 */
    private double cachedTotalPowerFE = 0;
    /** 原始 BigNumber（供 Flux-Networks 兼容存储） */
    private BigNumber rawEnergy = BigNumber.valueOf(0L);
    /** 标记：cachedTotalPowerFE 是否需要重算 */
    private boolean powerDirty = true;
    /** Global Cosmic Heart output reward, derived from SavedData rather than persisted per sphere. */
    private AbsoluteInteger cosmicHeartOutputLayers = new AbsoluteInteger();
    /** 批量包裹模式：本次包裹完成后应继续包裹的星体数（>=1 时激活） */
    private int batchRemaining = 0;
    /** 当前使用的层级（用于批量包裹） */
    private String currentHierarchyLevel = "SINGLE_STAR";
    /** 暗物质共鸣累积值（每包裹一颗暗物质星 +1，功率 ×2^count） */
    private AbsoluteInteger darkMatterResonance = new AbsoluteInteger();
    /** 鸿蒙之气累积次数 */
    private AbsoluteInteger primordialQiCount = new AbsoluteInteger();
    private boolean primordialQiChoicePending;
    private AbsoluteInteger primordialQiNextChoiceAt = absolute(10L);
    /** 鸿蒙之气根源重构倍率：10^count */
    private double primordialQiBaseMultiplier = 1.0;
    /** 鸿蒙之气法则浸润倍率：10^count */
    private double primordialQiOutputMultiplier = 1.0;
    /** Exact generation multiplier; the doubles above remain legacy API projections. */
    private BigNumber primordialQiMultiplier = BigNumber.valueOf(1L);
    /**
     * 交互模式枚举。
     * DYSON_SPHERE：正常戴森球包裹模式（默认）
     * ANNIHILATION：反物质湮灭模式（仅反物质星体可切换）
     */
    public enum InteractionMode {
        DYSON_SPHERE, ANNIHILATION
    }
    private boolean primordialQiStructureBuff = false;
    /** 交互模式：戴森球包裹 vs 反物质湮灭 */
    private InteractionMode interactionMode = InteractionMode.DYSON_SPHERE;
    /** 反物质星体数据（仅当前星体为反物质时有效） */
    private AntimatterStarData antimatterData = null;
    /** 反物质湮灭累计产出。 */
    private BigNumber antimatterCumulativeOutput = new BigNumber(0);

    public DysonSphereStructure() {
        this(0, 0);
    }

    public DysonSphereStructure(int beams, int solarPanels) {
        this.beams = BigNumber.valueOf(Math.max(0, beams));
        this.solarPanels = BigNumber.valueOf(Math.max(0, solarPanels));
    }

    // ====== 原模组 Getter/Setter ======

    public BigNumber getBeams() { return this.beams.deepCopy(); }
    public void setBeams(int beams) { setBeams(BigNumber.valueOf(Math.max(0, beams))); }
    public void setBeams(BigNumber beams) {
        this.beams = clampToRequirement(beams, getRequiredBeams());
    }
    public BigNumber getSolarPanels() { return this.solarPanels.deepCopy(); }
    public void setSolarPanels(int solarPanels) {
        setSolarPanels(BigNumber.valueOf(Math.max(0, solarPanels)));
    }
    public void setSolarPanels(BigNumber solarPanels) {
        this.solarPanels = clampToRequirement(solarPanels, getSupportedSolarPanels());
        markPowerDirty();
    }

    /** Total sails needed to enclose the current star, independent of installed beams. */
    public BigNumber getRequiredSolarPanels() {
        refreshStructureRequirementCache();
        return requiredSolarPanelsCache.deepCopy();
    }

    /** Total beams needed to support the current star's sail requirement. */
    public BigNumber getRequiredBeams() {
        refreshStructureRequirementCache();
        return requiredBeamsCache.deepCopy();
    }

    /** Sails that can currently be installed with the beams already launched. */
    public BigNumber getSupportedSolarPanels() {
        BigNumber supported = this.beams.multiplySmallInteger(Config.BEAM_TO_SOLAR_PANEL_RATIO);
        BigNumber required = getRequiredSolarPanels();
        return supported.compareTo(required) > 0 ? required : supported;
    }

    /** Legacy name retained for source compatibility; this is not a fixed cap. */
    public BigNumber getMaxSolarPanels() { return getSupportedSolarPanels(); }

    /** Legacy name retained for source compatibility; this is the current star requirement. */
    public BigNumber getMaxBeams() { return getRequiredBeams(); }
    public int getStoredPower() { return saturatingInt(this.storedEnergy); }
    public int getLastConsumedPower() { return saturatingInt(this.lastConsumedEnergy); }

    public BigNumber getStoredEnergy() { return this.storedEnergy.deepCopy(); }
    public BigNumber getLastConsumedEnergy() { return this.lastConsumedEnergy.deepCopy(); }

    public double getProgress() {
        BigNumber required = getRequiredSolarPanels();
        if (required.isZero() || this.solarPanels.compareTo(required) >= 0) return 1.0D;
        if (this.solarPanels.isZero()) return 0.0D;

        BigNumber scaledCurrent = this.solarPanels.multiplySmallInteger(PROGRESS_SCALE);
        int low = 0;
        int high = PROGRESS_SCALE;
        while (low < high) {
            int midpoint = (low + high + 1) >>> 1;
            BigNumber threshold = required.multiplySmallInteger(midpoint);
            if (scaledCurrent.compareTo(threshold) >= 0) {
                low = midpoint;
            } else {
                high = midpoint - 1;
            }
        }
        return (double) low / PROGRESS_SCALE;
    }

    public void increaseBeams(int amount) {
        if (amount <= 0) return;
        setBeams(this.beams.deepCopy().add(BigNumber.valueOf(amount)));
    }

    public void increaseSolarPanels(int amount) {
        if (amount <= 0) return;
        setSolarPanels(this.solarPanels.deepCopy().add(BigNumber.valueOf(amount)));
    }

    public BigNumber getBeamReserve() { return this.beamReserve.deepCopy(); }
    public BigNumber getSolarPanelReserve() { return this.solarPanelReserve.deepCopy(); }

    public boolean armForcedCosmicHeart() {
        if (this.forceCosmicHeartOnNextStar || this.forcedCosmicHeartReady) return false;
        this.forceCosmicHeartOnNextStar = true;
        return true;
    }

    public boolean isForcedCosmicHeartPending() {
        return this.forceCosmicHeartOnNextStar || this.forcedCosmicHeartReady;
    }

    public boolean isForcedCosmicHeartReady() {
        return this.forcedCosmicHeartReady;
    }

    /** Activates and consumes a generated one-shot Cosmic Heart target. */
    public boolean activateForcedCosmicHeart(DysonSphereProgressSavedData data) {
        return activateForcedCosmicHeart(data, "");
    }

    public boolean activateForcedCosmicHeart(DysonSphereProgressSavedData data, String sphereId) {
        if (!this.forcedCosmicHeartReady || data == null) return false;

        this.forcedCosmicHeartReady = false;
        queueCosmicHearts(absolute(1L));
        boolean activated = activateQueuedCosmicHeart(data, sphereId);
        data.setDirty();
        return activated;
    }

    public AbsoluteInteger getPendingCosmicHeartsExact() {
        return this.pendingCosmicHearts.copy();
    }

    private void queueCosmicHearts(AbsoluteInteger amount) {
        if (amount == null || amount.isZero()) return;
        FluxMath8.addInPlace(this.pendingCosmicHearts, amount);
    }

    private boolean activateQueuedCosmicHeart(DysonSphereProgressSavedData data, String sphereId) {
        if (data == null || this.pendingCosmicHearts.isZero()) return false;
        CosmicHeart heart = data.getCosmicHeart();
        if (heart.isActive() || heart.isComplete()) return false;
        AbsoluteInteger batchCount = this.pendingCosmicHearts.copy();
        this.pendingCosmicHearts = new AbsoluteInteger();
        heart.activateBatch(sphereId, getRequiredBeams(), getRequiredSolarPanels(), batchCount);
        com.gugugaga233.dysoncubeprojectaddon.overhaul.UniverseEvents.notifyCosmicHeartAppeared();
        data.setDirty();
        return true;
    }

    public void addStructureMaterials(BigNumber beams, BigNumber solarPanels) {
        if (beams != null && beams.signum() > 0) {
            this.beamReserve.addEnergy(beams.deepCopy());
        }
        if (solarPanels != null && solarPanels.signum() > 0) {
            this.solarPanelReserve.addEnergy(solarPanels.deepCopy());
        }
    }

    /** Moves pending launcher input to another sphere without changing its exact quantity. */
    public void transferStructureReservesTo(DysonSphereStructure target) {
        if (target == null || target == this) return;
        target.addStructureMaterials(this.beamReserve, this.solarPanelReserve);
        this.beamReserve = new BigNumber(0);
        this.solarPanelReserve = new BigNumber(0);
    }

    /**
     * Applies saved structure materials and advances across completed stars.
     * Work is bounded because one compressed stack can represent an enormous value.
     *
     * @return number of stars completed by this call
     */
    public int processStructureMaterials(int maximumWraps) {
        return processStructureMaterials(maximumWraps, Long.MAX_VALUE);
    }

    /** Applies structure reserves until the wrap limit or caller deadline is reached. */
    public int processStructureMaterials(int maximumWraps, long deadlineNanos) {
        return processStructureMaterials(null, "", maximumWraps, deadlineNanos);
    }

    /** Production path that can stop a batch at a Cosmic Heart target. */
    public int processStructureMaterials(DysonSphereProgressSavedData data, String sphereId,
                                         int maximumWraps, long deadlineNanos) {
        this.lastProcessedWraps = new AbsoluteInteger();
        if (maximumWraps <= 0) {
            this.calculationState = 0;
            return 0;
        }
        if (this.forcedCosmicHeartReady && data == null) {
            this.calculationState = 4;
            return 0;
        }
        boolean queuedHeartActivated = activatePendingCosmicHeart(data, sphereId)
                || this.calculationState != 1 && activateQueuedCosmicHeart(data, sphereId);
        if (queuedHeartActivated) {
            this.calculationState = 4;
            return 0;
        }
        if (processCosmicHeartTarget(data, sphereId)) {
            this.calculationState = 4;
            recordCalculationProgress();
            finishProcessedBatch();
            return FluxMath8.toIntSaturated(this.lastProcessedWraps);
        }

        // Integer.MAX_VALUE means "use the normal settlement limit", not an
        // unbounded operation. Keeping this finite is essential: the material
        // estimate below must never attempt to divide a million-digit reserve.
        AbsoluteInteger settlementLimit = settlementLimit(data);
        AbsoluteInteger requestedBudget = maximumWraps == Integer.MAX_VALUE
                ? settlementLimit.copy()
                : minimum(absolute(maximumWraps), settlementLimit);
        if (this.calculationState != 1) {
            this.calculationCompleted = new AbsoluteInteger();
            this.calculationCapacity = estimateAffordableSettlement(requestedBudget);
            this.calculationState = 1;
        }
        AbsoluteInteger remainingBudget = this.calculationCapacity.compareTo(
                this.calculationCompleted) > 0
                ? FluxMath8.subtract(this.calculationCapacity, this.calculationCompleted)
                : new AbsoluteInteger();

        // The star that was already visible before this call always consumes its exact cost.
        if (applyCurrentStarMaterials() && completeWrap(true) != null) {
            this.lastProcessedWraps.increment();
            decrementIfFinite(remainingBudget);
            if (activatePendingCosmicHeart(data, sphereId)) {
                recordCalculationProgress();
                this.calculationState = 4;
                finishProcessedBatch();
                return FluxMath8.toIntSaturated(this.lastProcessedWraps);
            }
            queueCosmicHeartAfterSingleWrap(data);
        } else {
            this.calculationState = 2;
            return 0;
        }

        boolean waitingForMaterials = false;
        boolean deadlineReached = false;
        while (!this.forcedCosmicHeartReady && hasBudget(remainingBudget)) {
            // Deterministic threshold stars keep their actual radius and material requirement.
            if (isCriticalCurrentStar()) {
                if (!applyCurrentStarMaterials() || completeWrap(true) == null) {
                    waitingForMaterials = true;
                    break;
                }
                this.lastProcessedWraps.increment();
                decrementIfFinite(remainingBudget);
                continue;
            }

            UniverseRandomizer.AverageStructureCost average =
                    UniverseRandomizer.averageStructureCost(this.totalWrapped);
            long hierarchyCostPermille = UniverseHierarchy.expectedCostPermille(this.totalWrapped);
            AbsoluteInteger candidate = affordableExpectedCount(
                    this.solarPanelReserve, average.solarPanels(), hierarchyCostPermille,
                    remainingBudget);
            AbsoluteInteger byBeams = affordableExpectedCount(
                    this.beamReserve, average.beams(), hierarchyCostPermille,
                    remainingBudget);
            if (byBeams.compareTo(candidate) < 0) candidate = byBeams;
            if (candidate.compareTo(remainingBudget) > 0) {
                candidate = remainingBudget.copy();
            }
            candidate = capAtNextCriticalBoundary(candidate);
            BigNumber candidateNumber = FluxMath8.toBigNumber(candidate);
            if (candidate.isZero()) {
                waitingForMaterials = true;
                break;
            }

            AbsoluteInteger heartHits = sampleCosmicHeartHits(data, candidate);

            consumeAverageMaterials(candidateNumber, average, hierarchyCostPermille);
            completeAverageBatch(candidate);
            FluxMath8.addInPlace(this.lastProcessedWraps, candidate);
            remainingBudget = FluxMath8.subtract(remainingBudget, candidate);

            if (!heartHits.isZero()) {
                queueCosmicHearts(heartHits);
            }

            if (System.nanoTime() >= deadlineNanos) {
                deadlineReached = true;
                break;
            }
        }

        boolean settlementFinished = waitingForMaterials || remainingBudget.isZero();
        if (settlementFinished && activateQueuedCosmicHeart(data, sphereId)) {
            this.calculationState = 4;
        } else if (remainingBudget.isZero()) {
            this.calculationState = 3;
        } else if (this.forcedCosmicHeartReady) {
            this.calculationState = 4;
        } else if (data != null && data.getCosmicHeart().isActive()) {
            this.calculationState = 4;
        } else if (waitingForMaterials && this.lastProcessedWraps.isZero()) {
            this.calculationState = 2;
        } else if (waitingForMaterials) {
            this.calculationState = 3;
        } else if (deadlineReached) {
            this.calculationState = 1;
        } else {
            this.calculationState = 3;
        }
        recordCalculationProgress();
        finishProcessedBatch();
        return FluxMath8.toIntSaturated(this.lastProcessedWraps);
    }

    public AbsoluteInteger getLastProcessedWrapsExact() {
        return this.lastProcessedWraps.copy();
    }

    public AbsoluteInteger getCalculationCompletedExact() {
        return this.calculationCompleted.copy();
    }

    public AbsoluteInteger getCalculationCapacityExact() {
        return this.calculationCapacity.copy();
    }

    public int getCalculationState() {
        return this.calculationState;
    }

    private void recordCalculationProgress() {
        if (!this.lastProcessedWraps.isZero()) {
            FluxMath8.addInPlace(this.calculationCompleted, this.lastProcessedWraps);
            if (this.calculationCapacity.compareTo(this.calculationCompleted) < 0) {
                this.calculationCapacity = this.calculationCompleted.copy();
            }
        }
        if (this.calculationState == 3 && !this.calculationCompleted.isZero()) {
            this.calculationCapacity = this.calculationCompleted.copy();
        }
    }

    private boolean processCosmicHeartTarget(DysonSphereProgressSavedData data, String sphereId) {
        if (data == null || !data.getCosmicHeart().isActive()) {
            return false;
        }
        CosmicHeart heart = data.getCosmicHeart();
        if (!heart.isTargetFor(sphereId)) return true;

        heart.ensureStructureTarget(sphereId, getRequiredBeams(), getRequiredSolarPanels());
        if (heart.acceptStructureMaterials(this.beamReserve, this.solarPanelReserve)) {
            StarData completedTarget = this.currentStarData;
            AbsoluteInteger completedHearts = heart.getBatchCountExact();
            FluxMath8.addInPlace(this.totalWrapped, completedHearts);
            FluxMath8.addInPlace(this.lastProcessedWraps, completedHearts);
            StarData nextStar = UniverseRandomizer.roll(this.totalWrapped);
            this.currentStarData = nextStar;
            invalidateStructureRequirementCache();
            resetGeneratedStarState(nextStar);
            if (completedTarget != null && "DARK_MATTER".equals(completedTarget.type())) {
                addDarkMatterResonance(absolute(1L));
            }
            checkAndUpdateLevel();
            markPowerDirty();
        }
        data.setDirty();
        return true;
    }

    private boolean activatePendingCosmicHeart(DysonSphereProgressSavedData data, String sphereId) {
        return this.forcedCosmicHeartReady && activateForcedCosmicHeart(data, sphereId);
    }

    private boolean queueCosmicHeartAfterSingleWrap(DysonSphereProgressSavedData data) {
        if (data == null || this.totalWrapped.compareTo(absolute(
                UniverseHierarchy.Level.STAR_UNIVERSE.unlockThreshold)) < 0) return false;
        if (Math.random() >= 1.0D / cosmicHeartChanceDenominator(data)) return false;
        queueCosmicHearts(absolute(1L));
        return true;
    }

    private AbsoluteInteger sampleCosmicHeartHits(DysonSphereProgressSavedData data,
                                                   AbsoluteInteger candidate) {
        if (data == null || candidate == null || candidate.isZero()) return new AbsoluteInteger();
        AbsoluteInteger threshold = absolute(UniverseHierarchy.Level.STAR_UNIVERSE.unlockThreshold);
        AbsoluteInteger eligibleAttempts = candidate.copy();
        if (this.totalWrapped.compareTo(threshold) < 0) {
            AbsoluteInteger firstEligibleOffset = FluxMath8.subtract(threshold, this.totalWrapped);
            if (firstEligibleOffset.compareTo(candidate) > 0) return new AbsoluteInteger();
            eligibleAttempts = FluxMath8.subtract(candidate, firstEligibleOffset);
            eligibleAttempts.increment();
        }
        long denominator = cosmicHeartChanceDenominator(data);
        FluxMath8.Division sampled = FluxMath8.divideAndRemainder(eligibleAttempts, denominator);
        AbsoluteInteger hits = sampled.quotient();
        if (sampled.remainder() > 0L
                && Math.random() < sampled.remainder() / (double) denominator) {
            hits.increment();
        }
        return hits;
    }

    private static long cosmicHeartChanceDenominator(DysonSphereProgressSavedData data) {
        int boosts = Math.min(8, FluxMath8.toIntSaturated(data.getCosmicHeart()
                .getModificationCountExact(CosmicHeart.RuleModification.SELF_PROBABILITY_X10)));
        long denominator = CosmicHeart.APPEARANCE_PROBABILITY;
        for (int index = 0; index < boosts && denominator > 1L; index++) {
            denominator = Math.max(1L, denominator / 10L);
        }
        return denominator;
    }

    private void finishProcessedBatch() {
        if (this.lastProcessedWraps.isZero()) return;
        checkAndUpdateLevel();
        recalculateWrappedStarPower(this.currentStarData);
    }

    private boolean applyCurrentStarMaterials() {
        refreshStructureRequirementCache();
        BigNumber requiredPanels = requiredSolarPanelsCache;
        BigNumber requiredBeams = requiredBeamsCache;

        BigNumber missingBeams = positiveDifference(requiredBeams, this.beams);
        if (!missingBeams.isEmpty() && !this.beamReserve.isEmpty()) {
            this.beams.add(this.beamReserve.extractContainer(missingBeams));
        }
        BigNumber supportedPanels = this.beams.multiplySmallInteger(Config.BEAM_TO_SOLAR_PANEL_RATIO);
        if (supportedPanels.compareTo(requiredPanels) > 0) supportedPanels = requiredPanels;
        BigNumber missingPanels = positiveDifference(supportedPanels, this.solarPanels);
        if (!missingPanels.isEmpty() && !this.solarPanelReserve.isEmpty()) {
            this.solarPanels.add(this.solarPanelReserve.extractContainer(missingPanels));
            markPowerDirty();
        }
        return this.solarPanels.compareTo(requiredPanels) >= 0;
    }

    private void consumeAverageMaterials(BigNumber countNumber,
                                          UniverseRandomizer.AverageStructureCost average,
                                          long hierarchyCostPermille) {
        BigNumber panels = expectedMaterialCost(
                countNumber, average.solarPanels(), hierarchyCostPermille);
        BigNumber beams = expectedMaterialCost(
                countNumber, average.beams(), hierarchyCostPermille);
        this.solarPanelReserve.extractContainer(panels);
        this.beamReserve.extractContainer(beams);
    }

    private AbsoluteInteger settlementLimit(DysonSphereProgressSavedData data) {
        AbsoluteInteger effectLayers = data == null
                ? new AbsoluteInteger()
                : data.getCosmicHeart().getHierarchyEffectLayersExact();
        return UniverseHierarchy.effectiveBatchSize(this.totalWrapped, effectLayers);
    }

    private static AbsoluteInteger affordableExpectedCount(BigNumber reserve, long baseCost,
                                                            long hierarchyCostPermille,
                                                            AbsoluteInteger maximum) {
        if (reserve == null || reserve.isZero()) return new AbsoluteInteger();

        // Compare in Flux's sparse BigNumber representation first. If the
        // reserve covers the whole finite batch, returning the batch directly
        // avoids converting a huge exponent into a JVM-sized BigInteger.
        if (maximum != null && !maximum.isZero()) {
            BigNumber maximumNumber = FluxMath8.toBigNumber(maximum);
            BigNumber requiredForMaximum = expectedMaterialCost(
                    maximumNumber, baseCost, hierarchyCostPermille);
            if (reserve.compareTo(requiredForMaximum) >= 0) return maximum.copy();
        }

        BigNumber scaledReserve = reserve.multiplySmallInteger(UniverseHierarchy.EXPECTATION_SCALE);
        AbsoluteInteger byBaseCost = FluxMath8.floorDivide(scaledReserve, baseCost);
        AbsoluteInteger result = FluxMath8.divideAndRemainder(byBaseCost, hierarchyCostPermille)
                .quotient();
        return maximum == null ? result : minimum(result, maximum);
    }

    /** Estimates the current finite settlement without expanding oversized reserves. */
    private AbsoluteInteger estimateAffordableSettlement(AbsoluteInteger maximum) {
        UniverseRandomizer.AverageStructureCost average =
                UniverseRandomizer.averageStructureCost(this.totalWrapped);
        long hierarchyCostPermille = UniverseHierarchy.expectedCostPermille(this.totalWrapped);
        AbsoluteInteger panels = affordableExpectedCount(
                this.solarPanelReserve, average.solarPanels(), hierarchyCostPermille, maximum);
        AbsoluteInteger beams = affordableExpectedCount(
                this.beamReserve, average.beams(), hierarchyCostPermille, maximum);
        AbsoluteInteger estimate = minimum(panels, beams);
        if (maximum != null) estimate = minimum(estimate, maximum);
        if (estimate.isZero()
                && (!this.beamReserve.isZero() || !this.solarPanelReserve.isZero())) {
            return absolute(1L);
        }
        return estimate;
    }

    private static BigNumber expectedMaterialCost(BigNumber count, long baseCost,
                                                  long hierarchyCostPermille) {
        BigNumber scaled = count.multiplySmallInteger(baseCost)
                .multiplySmallInteger(hierarchyCostPermille);
        BigNumber result = scaled.divideSmallInteger(UniverseHierarchy.EXPECTATION_SCALE);
        if (result.multiplySmallInteger(UniverseHierarchy.EXPECTATION_SCALE).compareTo(scaled) < 0) {
            result.add(BigNumber.valueOf(1L));
        }
        return result;
    }

    private void completeAverageBatch(AbsoluteInteger count) {
        AbsoluteInteger previous = this.totalWrapped.copy();
        if ("DARK_MATTER".equals(this.currentStarData.type())) {
            addDarkMatterResonance(absolute(1L));
        }
        FluxMath8.addInPlace(this.totalWrapped, count);

        AbsoluteInteger intermediateAttempts = count.copy();
        if (!intermediateAttempts.isZero()) intermediateAttempts.decrement();
        AbsoluteInteger darkHits = UniverseRandomizer.sampleDarkMatterHits(
                intermediateAttempts, previous);
        addDarkMatterResonance(darkHits);

        StarData landing = UniverseRandomizer.roll(this.totalWrapped);
        this.currentStarData = landing;
        invalidateStructureRequirementCache();
        resetGeneratedStarState(landing);
        markPowerDirty();
    }

    private void resetGeneratedStarState(StarData star) {
        if (!star.type().startsWith("ANTI_")) {
            this.antimatterData = null;
            this.interactionMode = InteractionMode.DYSON_SPHERE;
            this.antimatterCumulativeOutput = new BigNumber(0);
        } else {
            this.antimatterData = new AntimatterStarData(star);
        }
        this.beams = new BigNumber(0);
        this.solarPanels = new BigNumber(0);
    }

    private boolean isCriticalCurrentStar() {
        return equalsWrapped(1000L) || equalsWrapped(5000L);
    }

    private boolean equalsWrapped(long expected) {
        return this.totalWrapped.compareTo(absolute(expected)) == 0;
    }

    private AbsoluteInteger capAtNextCriticalBoundary(AbsoluteInteger requested) {
        for (long boundary : new long[]{
                1000L, 3000L, 5000L, 10000L,
                50_000L, 500_000L, 5_000_000L, 50_000_000L,
                500_000_000L, 5_000_000_000L}) {
            AbsoluteInteger boundaryValue = absolute(boundary);
            if (this.totalWrapped.compareTo(boundaryValue) < 0) {
                AbsoluteInteger distance = boundaryValue.copy();
                distance = FluxMath8.subtract(distance, this.totalWrapped);
                return minimum(requested, distance);
            }
        }
        return requested;
    }

    private static AbsoluteInteger minimum(AbsoluteInteger first, AbsoluteInteger second) {
        return first.compareTo(second) <= 0 ? first.copy() : second.copy();
    }

    private static boolean hasBudget(AbsoluteInteger budget) {
        return budget == null || !budget.isZero();
    }

    private static void decrementIfFinite(AbsoluteInteger budget) {
        if (budget != null && !budget.isZero()) budget.decrement();
    }

    private static AbsoluteInteger absolute(long value) {
        return AbsoluteInteger.parse(Long.toString(Math.max(0L, value)));
    }

    private void refreshStructureRequirementCache() {
        int sailRequirement = Config.getBaseSolarSailRequirement();
        int beamRatio = Config.BEAM_TO_SOLAR_PANEL_RATIO;
        if (requiredSolarPanelsCache != null
                && cachedSailRequirement == sailRequirement
                && cachedBeamRatio == beamRatio) {
            return;
        }
        requiredSolarPanelsCache = calculateRequiredSolarPanels(this.currentStarData);
        requiredBeamsCache = divideCeiling(requiredSolarPanelsCache, beamRatio);
        cachedSailRequirement = sailRequirement;
        cachedBeamRatio = beamRatio;
    }

    private void invalidateStructureRequirementCache() {
        requiredSolarPanelsCache = null;
        requiredBeamsCache = null;
    }

    public static BigNumber calculateRequiredSolarPanels(StarData star) {
        double radius = star == null ? 1.0D : star.radiusInRSun();
        if (!Double.isFinite(radius) || radius <= 0.0D) radius = 1.0D;
        BigDecimal radiusDecimal = BigDecimal.valueOf(radius);
        BigDecimal requirement = radiusDecimal.multiply(radiusDecimal)
                .multiply(BigDecimal.valueOf(Config.getBaseSolarSailRequirement()))
                .setScale(0, RoundingMode.CEILING)
                .max(BigDecimal.ONE);
        return new BigNumber(requirement);
    }

    private static BigNumber divideCeiling(BigNumber value, long divisor) {
        BigNumber quotient = value.divideSmallInteger(divisor);
        if (quotient.multiplySmallInteger(divisor).compareTo(value) < 0) {
            quotient.add(BigNumber.valueOf(1L));
        }
        return quotient;
    }

    private static BigNumber positiveDifference(BigNumber target, BigNumber current) {
        if (target.compareTo(current) <= 0) return new BigNumber(0);
        return target.deepCopy().subtract(current);
    }

    private static BigNumber clampToRequirement(BigNumber value, BigNumber requirement) {
        if (value == null || value.signum() <= 0) return new BigNumber(0);
        return value.compareTo(requirement) > 0 ? requirement.deepCopy() : value.deepCopy();
    }

    // ====== 魔改新增 Getter/Setter ======

    public long getTotalWrapped() { return FluxMath8.toLongSaturated(totalWrapped); }
    public AbsoluteInteger getTotalWrappedExact() { return totalWrapped.copy(); }
    public void setTotalWrapped(long totalWrapped) {
        setTotalWrappedExact(absolute(totalWrapped));
    }
    public void setTotalWrappedExact(AbsoluteInteger totalWrapped) {
        this.totalWrapped = totalWrapped == null ? new AbsoluteInteger() : totalWrapped.copy();
        markPowerDirty();
    }

    public StarData getCurrentStarData() { return currentStarData; }
    public void setCurrentStarData(StarData starData) {
        this.currentStarData = starData != null ? starData : StarData.SUN;
        invalidateStructureRequirementCache();
        markPowerDirty();
    }

    /** 获取缓存的总功率（FE/s，double），用于 UI 显示 */
    public double getCachedTotalPowerFE() {
        if (powerDirty) {
            recalculatePower();
        }
        return cachedTotalPowerFE;
    }

    /** 获取原始 BigNumber（） */
    public BigNumber getRawEnergy() {
        if (powerDirty) {
            recalculatePower();
        }
        return rawEnergy.deepCopy();
    }

    public void markPowerDirty() { this.powerDirty = true; }

    public void setCosmicHeartOutputLayers(AbsoluteInteger layers) {
        AbsoluteInteger next = layers == null ? new AbsoluteInteger() : layers;
        if (this.cosmicHeartOutputLayers.compareTo(next) == 0) return;
        this.cosmicHeartOutputLayers = next.copy();
        markPowerDirty();
    }

    /**
     * 能量生成（魔改版）
     * <p>
     * 原逻辑：storedPower = min(solarPanels * POWER_PER_SAIL, storedPower + solarPanels * POWER_PER_SAIL)
     * <p>
     * 魔改逻辑：基础功率 = solarPanels * POWER_PER_SAIL，再 × 10^totalWrapped（寰宇大道之力）。
     * 保留 int storedPower 用于 Titanium 电网兼容（FE 单位）。
     * <p>
     * <b>通量大数类占位：</b>当前使用 double 乘算。后续接入大数类时替换此处。
     */
    public void generatePower() {
        generatePower(1L);
    }

    /** Adds several elapsed ticks in one Flux operation without changing total generation. */
    public void generatePower(long elapsedTicks) {
        if (elapsedTicks <= 0L) return;
        this.lastConsumedEnergy = new BigNumber(0);
        if (this.powerDirty) {
            recalculatePower();
        }
        BigNumber generated = elapsedTicks == 1L
                ? this.rawEnergy.deepCopy()
                : this.rawEnergy.multiplySmallInteger(elapsedTicks);
        this.storedEnergy.addEnergy(generated);
    }

    /**
     * 提取能量（保持原接口兼容）
     */
    public int extractPower(int amount) {
        if (amount <= 0) return 0;
        long extracted = this.storedEnergy.extractChunk(amount);
        this.lastConsumedEnergy.addEnergy(extracted);
        return (int) extracted;
    }

    public BigNumber extractEnergy(BigNumber maximum, boolean simulate) {
        if (maximum == null || maximum.isEmpty() || this.storedEnergy.isEmpty()) {
            return new BigNumber(0);
        }
        BigNumber extracted = simulate
                ? this.storedEnergy.quoteContainer(maximum.deepCopy())
                : this.storedEnergy.extractContainer(maximum.deepCopy());
        if (!simulate && !extracted.isEmpty()) {
            this.lastConsumedEnergy.addEnergy(extracted.deepCopy());
        }
        return extracted;
    }

    /**
     * 包裹完成回调（单颗）。
     * <p>
     * 当戴森球进度达到 100% 时触发：
     * <ol>
     *   <li>totalWrapped++</li>
     *   <li>调用 UniverseRandomizer.roll() 生成新星体</li>
     *   <li>计算新星体的基础功率</li>
     *   <li>标记功率缓存为脏</li>
     *   <li>检测并更新层级</li>
     *   <li>检测寰宇之心激活</li>
     * </ol>
     * <p>
     * 若当前星体为反物质且处于湮灭模式，则阻止包裹（返回 null）。
     *
     * @return 新生成的星体数据，湮灭模式下返回 null
     */
    public StarData completeWrap() {
        return completeWrap(false);
    }

    /** Batch processing defers derived state that only depends on the final star. */
    private StarData completeWrap(boolean deferDerivedState) {
        // 反物质湮灭模式：阻止戴森球包裹
        if (this.interactionMode == InteractionMode.ANNIHILATION && this.antimatterData != null) {
            return null;
        }
        if (this.forcedCosmicHeartReady) {
            return null;
        }

        StarData completedStar = this.currentStarData;
        this.totalWrapped.increment();
        // 生成新星体
        StarData newStar = UniverseRandomizer.roll(totalWrapped);
        this.currentStarData = newStar;
        invalidateStructureRequirementCache();
        if (this.forceCosmicHeartOnNextStar) {
            this.forceCosmicHeartOnNextStar = false;
            this.forcedCosmicHeartReady = true;
        }

        // 普朗克星：检查量子结构光束（待物品注册后启用）
        if ("PLANCK".equals(newStar.type())) {
            // TODO: 检测玩家背包中 Config.QUANTUM_BEAM_ITEM 数量 >= Config.QUANTUM_BEAM_REQUIRED
            // 不足则阻止包裹并提示玩家
        }

        // 重置反物质数据（新星体可能不是反物质）
        if (!newStar.type().startsWith("ANTI_")) {
            this.antimatterData = null;
            this.interactionMode = InteractionMode.DYSON_SPHERE;
            this.antimatterCumulativeOutput = new BigNumber(0);
        } else {
            this.antimatterData = new AntimatterStarData(newStar);
        }

        // Resonance belongs to the star just completed, not the new landing target.
        if (completedStar != null && "DARK_MATTER".equals(completedStar.type())) {
            addDarkMatterResonance(absolute(1L));
        }

        if (deferDerivedState) {
            markPowerDirty();
        } else {
            // Preserve the public single-wrap behavior; batches calculate only their final state.
            recalculateWrappedStarPower(newStar);
        }

        // 重置戴森球进度（开始包裹新星体）
        this.beams = new BigNumber(0);
        this.solarPanels = new BigNumber(0);

        if (!deferDerivedState) {
            checkAndUpdateLevel();
        }

        // ====== 魔改：寰宇之心激活检测 ======
        // totalWrapped >= 50亿时，每次包裹有极低概率(1/1亿)激活寰宇之心
        // 通过 LevelTickEvent 广播的 data 对象调用 tryActivateCosmicHeart 实现
        // 此处仅记录阈值，实际激活由 DysonCubeProject.LevelTickEvent 监听触发

        return newStar;
    }

    /**
     * 检测并尝试激活寰宇之心（需在 completeWrap() 调用后由外部传入存档数据调用）。
     *
     * @param data 存档数据
     */
    public void tryActivateCosmicHeart(DysonSphereProgressSavedData data) {
        tryActivateCosmicHeart(data, 1);
    }

    /** Performs the same independent chance as one roll per newly wrapped eligible star. */
    public void tryActivateCosmicHeart(DysonSphereProgressSavedData data, int completedWraps) {
        tryActivateCosmicHeart(data, absolute(completedWraps));
    }

    /** Preserves the per-star chance for exact batch sizes without per-star loops. */
    public void tryActivateCosmicHeart(DysonSphereProgressSavedData data,
                                       AbsoluteInteger completedWraps) {
        if (data == null) return;
        if (completedWraps == null || completedWraps.isZero()) return;
        AbsoluteInteger eligibleAttempts = eligibleAttempts(
                this.totalWrapped, completedWraps, UniverseHierarchy.Level.STAR_UNIVERSE.unlockThreshold);
        if (eligibleAttempts.isZero()) return;

        long denominator = cosmicHeartChanceDenominator(data);
        FluxMath8.Division sampled = FluxMath8.divideAndRemainder(eligibleAttempts, denominator);
        AbsoluteInteger hits = sampled.quotient();
        if (sampled.remainder() > 0L
                && Math.random() < sampled.remainder() / (double) denominator) {
            hits.increment();
        }
        queueCosmicHearts(hits);
        activateQueuedCosmicHeart(data, "");
    }

    private static AbsoluteInteger eligibleAttempts(AbsoluteInteger total,
                                                     AbsoluteInteger completed,
                                                     long threshold) {
        AbsoluteInteger thresholdValue = absolute(threshold);
        if (total == null || completed == null || completed.isZero()
                || total.compareTo(thresholdValue) < 0) {
            return new AbsoluteInteger();
        }

        AbsoluteInteger boundedCompleted = minimum(completed, total);
        AbsoluteInteger firstCompleted = total.copy();
        firstCompleted = FluxMath8.subtract(firstCompleted, boundedCompleted);
        firstCompleted.increment();
        if (firstCompleted.compareTo(thresholdValue) < 0) firstCompleted = thresholdValue;

        AbsoluteInteger eligible = total.copy();
        eligible = FluxMath8.subtract(eligible, firstCompleted);
        eligible.increment();
        return eligible;
    }

    /**
     * 批量包裹：连续生成 N 颗星体。
     * <p>
     * 每次循环调用单次包裹逻辑，最后重置进度，返回最后一颗星体。
     * 若当前为单星体层级（batchN ≤ 1），自动降级为单次包裹。
     *
     * @param batchN 本次批量包裹的星体数
     * @return 最后一颗生成的星体数据
     */
    public StarData completeWrappedBatch(int batchN) {
        if (batchN <= 1) {
            return completeWrap();
        }
        StarData lastStar = StarData.SUN;
        for (int i = 0; i < batchN; i++) {
            lastStar = completeWrap();
            if (lastStar == null || this.forcedCosmicHeartReady) break;
        }
        // 批量完成后检查层级和鸿蒙之气
        checkAndUpdateLevel();
        return lastStar;
    }

    /**
     * 检测并更新层级：若 totalWrapped 达到更高层级解锁阈值，则更新并广播通知。
     */
    public void checkAndUpdateLevel() {
        UniverseHierarchy.Level newLevel = UniverseHierarchy.getUnlockedLevel(getTotalWrappedExact());
        String newLevelName = newLevel.name();
        if (!newLevelName.equals(this.currentHierarchyLevel)) {
            this.currentHierarchyLevel = newLevelName;
        }
    }

    /**
     * 获取当前已解锁的最高层级。
     */
    public UniverseHierarchy.Level getCurrentLevel() {
        return UniverseHierarchy.getUnlockedLevel(getTotalWrappedExact());
    }

    /**
     * 设置批量包裹模式（保留，供寰宇中枢 GUI 调用）。
     */
    public void startBatchWrap(String hierarchyLevel, int batchN) {
        UniverseHierarchy.Level level;
        try {
            level = UniverseHierarchy.Level.valueOf(hierarchyLevel);
        } catch (IllegalArgumentException e) {
            level = UniverseHierarchy.Level.SINGLE_STAR;
        }
        this.currentHierarchyLevel = hierarchyLevel;
        if (!UniverseHierarchy.isUnlocked(level, getTotalWrappedExact())) {
            this.batchRemaining = 0;
            return;
        }
        this.batchRemaining = UniverseHierarchy.clampBatch(level, batchN);
    }

    /** 是否处于批量包裹模式 */
    public boolean isBatching() { return this.batchRemaining > 0; }

    /** 取消批量包裹 */
    public void cancelBatchWrap() { this.batchRemaining = 0; }

    /** 获取剩余批量包裹数 */
    public int getBatchRemaining() { return this.batchRemaining; }

    /** 获取当前层级名称 */
    public String getCurrentHierarchyLevel() { return this.currentHierarchyLevel; }
    public void setCurrentHierarchyLevel(String level) { this.currentHierarchyLevel = level; }

    /** 获取暗物质共鸣值 */
    public int getDarkMatterResonance() { return FluxMath8.toIntSaturated(this.darkMatterResonance); }
    public AbsoluteInteger getDarkMatterResonanceExact() { return this.darkMatterResonance.copy(); }

    private void addDarkMatterResonance(AbsoluteInteger amount) {
        if (amount != null && !amount.isZero()) {
            FluxMath8.addInPlace(this.darkMatterResonance, amount);
        }
    }

    // ====== 鸿蒙之气 ======

    /** 鸿蒙之气累积次数 */
    public int getPrimordialQiCount() { return FluxMath8.toIntSaturated(this.primordialQiCount); }
    public AbsoluteInteger getPrimordialQiCountExact() { return this.primordialQiCount.copy(); }
    public boolean hasPrimordialQiChoicePending() { return this.primordialQiChoicePending; }
    public boolean isPrimordialQiChoiceDue() {
        return !this.primordialQiChoicePending
                && this.primordialQiCount.compareTo(this.primordialQiNextChoiceAt) >= 0;
    }
    public boolean markPrimordialQiChoicePending() {
        if (!isPrimordialQiChoiceDue()) return false;
        this.primordialQiChoicePending = true;
        return true;
    }
    public boolean consumePrimordialQiChoicePending() {
        if (!this.primordialQiChoicePending) return false;
        this.primordialQiChoicePending = false;
        this.primordialQiNextChoiceAt = FluxMath8.multiply(this.primordialQiCount, 10L);
        return true;
    }
    public void setPrimordialQiCount(int count) {
        this.primordialQiCount = absolute(Math.max(0, count));
        refreshPrimordialQiMultiplier();
        markPowerDirty();
    }
    public double getPrimordialQiBaseMultiplier() { return this.primordialQiBaseMultiplier; }
    public double getPrimordialQiOutputMultiplier() { return this.primordialQiOutputMultiplier; }
    public BigNumber getPrimordialQiBaseMultiplierBigNumber() { return this.primordialQiMultiplier.deepCopy(); }
    public BigNumber getPrimordialQiOutputMultiplierBigNumber() { return this.primordialQiMultiplier.deepCopy(); }
    public boolean isPrimordialQiStructureBuff() { return this.primordialQiStructureBuff; }

    /**
     * 触发鸿蒙之气：累计次数+1，应用效果。
     * <p>
     * 效果1：根源重构 — 基数 × 10^count
     * 效果2：法则浸润 — 输出 × 10^count
     * 效果4：道痕烙印 — 每10次触发觉醒界面（由调用方处理）
     */
    public void applyPrimordialQiEffect() {
        applyPrimordialQiEffects(1);
    }

    public void applyPrimordialQiEffects(int amount) {
        applyPrimordialQiEffects(absolute(amount));
    }

    public void applyPrimordialQiEffects(AbsoluteInteger amount) {
        if (amount == null || amount.isZero()) return;
        AbsoluteInteger previousMilestones = this.primordialQiStructureBuff
                ? null : FluxMath8.divideAndRemainder(this.primordialQiCount, 10L).quotient();
        FluxMath8.addInPlace(this.primordialQiCount, amount);
        refreshPrimordialQiMultiplier();
        markPowerDirty();
        if (previousMilestones != null && FluxMath8.divideAndRemainder(
                this.primordialQiCount, 10L).quotient().compareTo(previousMilestones) > 0) {
            this.primordialQiStructureBuff = true;
        }
    }

    // ====== NBT 序列化 ======

    private static final String TAG_WRAPPED = "totalWrapped";
    private static final String TAG_WRAPPED_EXACT = "totalWrappedExact";
    private static final String TAG_STAR_DATA = "currentStarData";
    private static final String TAG_BATCH_REMAINING = "batchRemaining";
    private static final String TAG_HIERARCHY = "currentHierarchyLevel";
    private static final String TAG_DARK_MATTER = "darkMatterResonance";
    private static final String TAG_DARK_MATTER_EXACT = "darkMatterResonanceExact";
    private static final String TAG_QI_COUNT = "primordialQiCount";
    private static final String TAG_QI_COUNT_EXACT = "primordialQiCountExact";
    private static final String TAG_QI_CHOICE_PENDING = "primordialQiChoicePending";
    private static final String TAG_QI_NEXT_CHOICE = "primordialQiNextChoiceAt";
    private static final String TAG_QI_BASE = "primordialQiBaseMultiplier";
    private static final String TAG_QI_OUTPUT = "primordialQiOutputMultiplier";
    private static final String TAG_QI_BUFF = "primordialQiStructureBuff";
    private static final String TAG_MODE = "interactionMode";
    private static final String TAG_ANTI_DATA = "antimatterData";
    private static final String TAG_ANTI_CUM = "antimatterCumulativeOutput";
    private static final String TAG_ANTI_CUM_BIG = "antimatterCumulativeOutputBig";
    private static final String TAG_BEAMS_BIG = "beamsBigNumber";
    private static final String TAG_SOLAR_PANELS_BIG = "solarPanelsBigNumber";
    private static final String TAG_FORCE_COSMIC_HEART_NEXT = "forceCosmicHeartOnNextStar";
    private static final String TAG_FORCED_COSMIC_HEART_READY = "forcedCosmicHeartReady";
    private static final String TAG_PENDING_COSMIC_HEARTS = "pendingCosmicHearts";
    private static final String TAG_CALCULATION_COMPLETED = "calculationCompleted";
    private static final String TAG_CALCULATION_CAPACITY = "calculationCapacity";
    private static final String TAG_CALCULATION_STATE = "calculationState";

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put(TAG_BEAMS_BIG, this.beams.toTag());
        tag.put(TAG_SOLAR_PANELS_BIG, this.solarPanels.toTag());
        tag.put("storedEnergy", this.storedEnergy.toTag());
        tag.put("lastConsumedEnergy", this.lastConsumedEnergy.toTag());
        tag.put("beamReserve", this.beamReserve.toTag());
        tag.put("solarPanelReserve", this.solarPanelReserve.toTag());
        tag.putBoolean(TAG_FORCE_COSMIC_HEART_NEXT, this.forceCosmicHeartOnNextStar);
        tag.putBoolean(TAG_FORCED_COSMIC_HEART_READY, this.forcedCosmicHeartReady);
        tag.put(TAG_PENDING_COSMIC_HEARTS, FluxMath8.toCompactTag(this.pendingCosmicHearts));
        tag.put(TAG_CALCULATION_COMPLETED, FluxMath8.toCompactTag(this.calculationCompleted));
        tag.put(TAG_CALCULATION_CAPACITY, FluxMath8.toCompactTag(this.calculationCapacity));
        tag.putInt(TAG_CALCULATION_STATE, this.calculationState);
        // 魔改新增字段
        tag.putLong(TAG_WRAPPED, getTotalWrapped());
        tag.put(TAG_WRAPPED_EXACT, FluxMath8.toCompactTag(this.totalWrapped));
        if (this.currentStarData != null) {
            tag.putString(TAG_STAR_DATA, this.currentStarData.serializeToString());
        }
        tag.putInt(TAG_BATCH_REMAINING, this.batchRemaining);
        tag.putString(TAG_HIERARCHY, this.currentHierarchyLevel);
        tag.putInt(TAG_DARK_MATTER, getDarkMatterResonance());
        tag.put(TAG_DARK_MATTER_EXACT, FluxMath8.toCompactTag(this.darkMatterResonance));
        tag.putInt(TAG_QI_COUNT, getPrimordialQiCount());
        tag.put(TAG_QI_COUNT_EXACT, FluxMath8.toCompactTag(this.primordialQiCount));
        tag.putBoolean(TAG_QI_CHOICE_PENDING, this.primordialQiChoicePending);
        tag.put(TAG_QI_NEXT_CHOICE, FluxMath8.toCompactTag(this.primordialQiNextChoiceAt));
        tag.putDouble(TAG_QI_BASE, this.primordialQiBaseMultiplier);
        tag.putDouble(TAG_QI_OUTPUT, this.primordialQiOutputMultiplier);
        tag.putBoolean(TAG_QI_BUFF, this.primordialQiStructureBuff);
        tag.putString(TAG_MODE, this.interactionMode.name());
        if (this.antimatterData != null) {
            tag.put(TAG_ANTI_DATA, this.antimatterData.toTag());
        }
        tag.put(TAG_ANTI_CUM_BIG, this.antimatterCumulativeOutput.toTag());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.beams = tag.contains(TAG_BEAMS_BIG, Tag.TAG_COMPOUND)
                ? BigNumber.fromTag(tag.getCompound(TAG_BEAMS_BIG))
                : BigNumber.valueOf(Math.max(0, tag.getInt("beams")));
        this.solarPanels = tag.contains(TAG_SOLAR_PANELS_BIG, Tag.TAG_COMPOUND)
                ? BigNumber.fromTag(tag.getCompound(TAG_SOLAR_PANELS_BIG))
                : BigNumber.valueOf(Math.max(0, tag.getInt("solarPanels")));
        this.storedEnergy = tag.contains("storedEnergy")
                ? BigNumber.fromTag(tag.getCompound("storedEnergy"))
                : BigNumber.valueOf(tag.getInt("storedPower"));
        this.lastConsumedEnergy = tag.contains("lastConsumedEnergy")
                ? BigNumber.fromTag(tag.getCompound("lastConsumedEnergy"))
                : BigNumber.valueOf(tag.getInt("lastConsumedPower"));
        this.beamReserve = tag.contains("beamReserve")
                ? BigNumber.fromTag(tag.getCompound("beamReserve"))
                : new BigNumber(0);
        this.solarPanelReserve = tag.contains("solarPanelReserve")
                ? BigNumber.fromTag(tag.getCompound("solarPanelReserve"))
                : new BigNumber(0);
        this.forceCosmicHeartOnNextStar = tag.getBoolean(TAG_FORCE_COSMIC_HEART_NEXT);
        this.forcedCosmicHeartReady = tag.getBoolean(TAG_FORCED_COSMIC_HEART_READY);
        this.pendingCosmicHearts = tag.contains(TAG_PENDING_COSMIC_HEARTS, Tag.TAG_COMPOUND)
                ? FluxMath8.fromCompactTag(tag.getCompound(TAG_PENDING_COSMIC_HEARTS))
                : new AbsoluteInteger();
        this.calculationCompleted = tag.contains(TAG_CALCULATION_COMPLETED, Tag.TAG_COMPOUND)
                ? FluxMath8.fromCompactTag(tag.getCompound(TAG_CALCULATION_COMPLETED))
                : new AbsoluteInteger();
        this.calculationCapacity = tag.contains(TAG_CALCULATION_CAPACITY, Tag.TAG_COMPOUND)
                ? FluxMath8.fromCompactTag(tag.getCompound(TAG_CALCULATION_CAPACITY))
                : new AbsoluteInteger();
        this.calculationState = Math.max(0, Math.min(4, tag.getInt(TAG_CALCULATION_STATE)));
        if (this.storedEnergy.isImmutable()) this.storedEnergy = this.storedEnergy.deepCopy();
        if (this.lastConsumedEnergy.isImmutable()) this.lastConsumedEnergy = this.lastConsumedEnergy.deepCopy();
        if (this.beamReserve.isImmutable()) this.beamReserve = this.beamReserve.deepCopy();
        if (this.solarPanelReserve.isImmutable()) this.solarPanelReserve = this.solarPanelReserve.deepCopy();
        if (this.beams.isImmutable()) this.beams = this.beams.deepCopy();
        if (this.solarPanels.isImmutable()) this.solarPanels = this.solarPanels.deepCopy();
        // 魔改新增字段
        this.totalWrapped = tag.contains(TAG_WRAPPED_EXACT, Tag.TAG_COMPOUND)
                ? FluxMath8.fromCompactTag(tag.getCompound(TAG_WRAPPED_EXACT))
                : absolute(tag.getLong(TAG_WRAPPED));
        this.currentStarData = StarData.deserializeFromString(tag.getString(TAG_STAR_DATA));
        invalidateStructureRequirementCache();
        this.batchRemaining = tag.getInt(TAG_BATCH_REMAINING);
        this.currentHierarchyLevel = tag.getString(TAG_HIERARCHY);
        this.darkMatterResonance = tag.contains(TAG_DARK_MATTER_EXACT, Tag.TAG_COMPOUND)
                ? FluxMath8.fromCompactTag(tag.getCompound(TAG_DARK_MATTER_EXACT))
                : absolute(Math.max(0, tag.getInt(TAG_DARK_MATTER)));
        this.primordialQiCount = tag.contains(TAG_QI_COUNT_EXACT, Tag.TAG_COMPOUND)
                ? FluxMath8.fromCompactTag(tag.getCompound(TAG_QI_COUNT_EXACT))
                : absolute(Math.max(0, tag.getInt(TAG_QI_COUNT)));
        this.primordialQiChoicePending = tag.getBoolean(TAG_QI_CHOICE_PENDING);
        this.primordialQiNextChoiceAt = tag.contains(TAG_QI_NEXT_CHOICE, Tag.TAG_COMPOUND)
                ? FluxMath8.fromCompactTag(tag.getCompound(TAG_QI_NEXT_CHOICE))
                : absolute(10L);
        refreshPrimordialQiMultiplier();
        this.primordialQiStructureBuff = tag.getBoolean(TAG_QI_BUFF);
        try { this.interactionMode = InteractionMode.valueOf(tag.getString(TAG_MODE)); } catch (IllegalArgumentException ignored) {}
        if (tag.contains(TAG_ANTI_DATA, Tag.TAG_COMPOUND)) {
            this.antimatterData = AntimatterStarData.fromTag(tag.getCompound(TAG_ANTI_DATA));
        } else {
            String antiStr = tag.getString(TAG_ANTI_DATA);
            if (!antiStr.isEmpty()) {
                this.antimatterData = AntimatterStarData.deserializeFromString(antiStr);
            }
        }
        if (tag.contains(TAG_ANTI_CUM_BIG, Tag.TAG_COMPOUND)) {
            this.antimatterCumulativeOutput = BigNumber.fromTag(tag.getCompound(TAG_ANTI_CUM_BIG));
            if (this.antimatterCumulativeOutput.isImmutable()) {
                this.antimatterCumulativeOutput = this.antimatterCumulativeOutput.deepCopy();
            }
        } else {
            String cumStr = tag.getString(TAG_ANTI_CUM);
            if (!cumStr.isEmpty()) {
                try { this.antimatterCumulativeOutput = BigNumber.valueOf(new java.math.BigInteger(cumStr)); }
                catch (NumberFormatException ignored) {}
            }
        }
        if (this.currentHierarchyLevel == null || this.currentHierarchyLevel.isEmpty()) {
            this.currentHierarchyLevel = "SINGLE_STAR";
        }
        markPowerDirty();
    }

    /**
     * 重算缓存功率。
     * PERFORMANCE: 仅在 powerDirty 时调用，避免每帧重复计算。
     */
    private void recalculatePower() {
        BigNumber basePower = calculateGenerationBasePower();
        // 通量大数类：使用 BigNumber 计算最终功率
        BigNumber energy = AstrophysicalCalculator.calculateFinalPower(
                basePower, this.totalWrapped,
                this.darkMatterResonance, this.primordialQiMultiplier,
                AstrophysicalCalculator.powerOfTwoMultiplier(this.cosmicHeartOutputLayers));
        this.cachedTotalPowerFE = energy.getEnergyStoredLong();
        this.rawEnergy = energy.deepCopy();
        this.powerDirty = false;
    }

    private void recalculateWrappedStarPower(StarData star) {
        BigNumber basePower = AstrophysicalCalculator.calculateBasePower(star);
        BigNumber energy = AstrophysicalCalculator.calculateFinalPower(
                basePower, this.totalWrapped,
                this.darkMatterResonance, this.primordialQiMultiplier,
                AstrophysicalCalculator.powerOfTwoMultiplier(this.cosmicHeartOutputLayers));
        this.cachedTotalPowerFE = energy.getEnergyStoredLong();
        this.rawEnergy = energy.deepCopy();
        this.powerDirty = false;
    }

    // ====== 反物质交互 ======

    /** 获取交互模式 */
    public InteractionMode getInteractionMode() { return interactionMode; }
    /** 是否处于湮灭模式 */
    public boolean isAnnihilationMode() { return interactionMode == InteractionMode.ANNIHILATION; }
    /** 是否为反物质星体 */
    public boolean isAntimatterStar() {
        return currentStarData != null && currentStarData.type().startsWith("ANTI_");
    }
    /** 获取反物质数据 */
    public AntimatterStarData getAntimatterData() { return antimatterData; }
    /** 获取反物质累计产出 */
    public java.math.BigInteger getAntimatterCumulativeOutput() {
        return antimatterCumulativeOutput.getExactEnergy()
                .orElse(java.math.BigInteger.valueOf(Long.MAX_VALUE));
    }
    public BigNumber getAntimatterCumulativeOutputBigNumber() {
        return antimatterCumulativeOutput.deepCopy();
    }

    /**
     * 切换交互模式（仅反物质星体有效）。
     *
     * @param player 玩家（用于通知）
     * @return 是否切换成功
     */
    public boolean toggleAntimatterMode(net.minecraft.server.level.ServerPlayer player) {
        if (!isAntimatterStar()) {
            if (player != null) player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "message.dysoncubeproject.antimatter.not_antimatter"), true);
            return false;
        }
        if (this.antimatterData == null) {
            this.antimatterData = new AntimatterStarData(this.currentStarData);
        }
        if (this.interactionMode == InteractionMode.DYSON_SPHERE) {
            this.interactionMode = InteractionMode.ANNIHILATION;
            if (player != null) player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "message.dysoncubeproject.antimatter.mode_annihilation"), true);
        } else {
            this.interactionMode = InteractionMode.DYSON_SPHERE;
            if (player != null) player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "message.dysoncubeproject.antimatter.mode_dyson"), true);
        }
        markPowerDirty();
        return true;
    }

    /**
     * 投料触发反物质湮灭。
     *
     * @param player 投料玩家
     * @param stack  手持物品
     * @return 本次产出能量（BigInteger），无效操作返回 null
     */
    public java.math.BigInteger feedAntimatter(net.minecraft.server.level.ServerPlayer player,
                                               net.minecraft.world.item.ItemStack stack) {
        DysonSphereProgressSavedData data = player == null
                ? null : DysonSphereProgressSavedData.get(player.level());
        BigNumber multiplier = data == null ? BigNumber.valueOf(1L)
                : data.getCosmicHeart().getAntimatterEffectMultiplierExact();
        BigNumber output = feedAntimatterBigNumber(player, stack, multiplier);
        return output == null ? null : output.getExactEnergy()
                .orElse(java.math.BigInteger.valueOf(Long.MAX_VALUE));
    }

    public BigNumber feedAntimatterBigNumber(net.minecraft.server.level.ServerPlayer player,
                                             net.minecraft.world.item.ItemStack stack) {
        return feedAntimatterBigNumber(player, stack, BigNumber.valueOf(1L));
    }

    public BigNumber feedAntimatterBigNumber(net.minecraft.server.level.ServerPlayer player,
                                             net.minecraft.world.item.ItemStack stack,
                                             BigNumber effectMultiplier) {
        if (this.interactionMode != InteractionMode.ANNIHILATION || this.antimatterData == null) {
            if (player != null) player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "message.dysoncubeproject.antimatter.not_in_annihilation"), true);
            return null;
        }
        if (stack.isEmpty()) return null;
        java.math.BigDecimal starMassKg = java.math.BigDecimal.valueOf(this.currentStarData != null
                ? this.currentStarData.massInMSun() * AstrophysicalCalculator.SOLAR_MASS_KG
                : AstrophysicalCalculator.SOLAR_MASS_KG);
        double increase = this.antimatterData.getProgressIncrease(stack, 1, starMassKg);
        if (increase <= 0) {
            if (player != null) player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "message.dysoncubeproject.antimatter.invalid_item"), true);
            return null;
        }
        double oldProgress = this.antimatterData.getCurrentProgress();
        BigNumber output = this.antimatterData.feedBigNumber(stack, 1, getTotalWrapped(), starMassKg);
        if (effectMultiplier != null
                && effectMultiplier.compareTo(BigNumber.valueOf(1L)) > 0) {
            output.multiply(effectMultiplier.deepCopy());
        }
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        this.antimatterCumulativeOutput.addEnergy(output.deepCopy());
        if (player != null) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.dysoncubeproject.antimatter.progress",
                    String.format("%.1f", this.antimatterData.getCurrentProgress() - oldProgress),
                    formatBig(output)), true);
        }
        markPowerDirty();
        // 湮灭完成
        if (this.antimatterData.isComplete()) {
            onAntimatterComplete(player);
        }
        return output;
    }

    /** 湮灭完成：totalWrapped+1，生成新星体 */
    private void onAntimatterComplete(net.minecraft.server.level.ServerPlayer player) {
        this.totalWrapped.increment();
        StarData newStar = UniverseRandomizer.roll(this.totalWrapped);
        this.currentStarData = newStar;
        invalidateStructureRequirementCache();
        if (!newStar.type().startsWith("ANTI_")) {
            this.antimatterData = null;
            this.interactionMode = InteractionMode.DYSON_SPHERE;
        } else {
            this.antimatterData = new AntimatterStarData(newStar);
        }
        this.antimatterCumulativeOutput = new BigNumber(0);
        BigNumber basePower = AstrophysicalCalculator.calculateBasePower(newStar);
        BigNumber finalEnergy = AstrophysicalCalculator.calculateFinalPower(
                basePower, this.totalWrapped, this.darkMatterResonance,
                this.primordialQiMultiplier,
                AstrophysicalCalculator.powerOfTwoMultiplier(this.cosmicHeartOutputLayers));
        this.cachedTotalPowerFE = finalEnergy.getEnergyStoredLong();
        this.rawEnergy = finalEnergy.deepCopy();
        this.powerDirty = false;
        this.beams = new BigNumber(0);
        this.solarPanels = new BigNumber(0);
        checkAndUpdateLevel();
        markPowerDirty();
        // 湮灭完成通知由调用方（EMRailEjectorBlockEntity）处理
    }

    private static String formatBig(BigNumber value) {
        return value == null || value.isZero() ? "0" : value.toCalculationString();
    }

    private void refreshPrimordialQiMultiplier() {
        this.primordialQiMultiplier = this.primordialQiCount.isZero()
                ? BigNumber.valueOf(1L)
                : FluxMath8.scientific("1", this.primordialQiCount);
        double legacyProjection = this.primordialQiCount.compareTo(absolute(308L)) > 0
                ? Double.MAX_VALUE : Math.pow(10.0, FluxMath8.toIntSaturated(this.primordialQiCount));
        this.primordialQiBaseMultiplier = legacyProjection;
        this.primordialQiOutputMultiplier = legacyProjection;
    }

    private BigNumber calculateSailPower() {
        return Config.POWER_PER_SAIL <= 0 || this.solarPanels.isZero()
                ? new BigNumber(0)
                : this.solarPanels.multiplySmallInteger(Config.POWER_PER_SAIL);
    }

    /** Completed spheres keep producing while the next star has no installed sails yet. */
    private BigNumber calculateGenerationBasePower() {
        BigNumber sailPower = calculateSailPower();
        if (!sailPower.isZero() || this.totalWrapped.isZero()) return sailPower;
        return AstrophysicalCalculator.calculateBasePower(this.currentStarData);
    }

    private static int saturatingInt(BigNumber value) {
        return (int) Math.min(Integer.MAX_VALUE, value.getEnergyStoredLong());
    }

    private static long saturatedIncrement(long value) {
        return value == Long.MAX_VALUE ? Long.MAX_VALUE : value + 1L;
    }
}

