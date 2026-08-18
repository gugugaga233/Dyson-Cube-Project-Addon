package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;
import sonar.fluxnetworks.api.energy.BigNumber;

class CosmicHeartRewardPlanTest {

    @Test
    void rewardPlanClaimsOneChoicePerCompletedHeartAndSurvivesSave() {
        CosmicHeart heart = completedHeart();
        AbsoluteInteger[] plan = emptyPlan();
        plan[CosmicHeart.RuleModification.MULTIPLIER_BASE_X100.ordinal()] = absolute(3L);
        plan[CosmicHeart.RuleModification.TOTAL_WRAPPED_PLUS_1M.ordinal()] = absolute(1L);

        assertTrue(heart.setRewardPlan(plan));
        assertEquals(CosmicHeart.RuleModification.MULTIPLIER_BASE_X100,
                heart.claimNextPlannedReward());
        assertFalse(heart.isComplete());
        assertEquals(BigInteger.valueOf(3L), FluxMath8.toBigInteger(
                heart.getPlannedRewardCountExact()));
        assertEquals(1, heart.getModificationCount(
                CosmicHeart.RuleModification.MULTIPLIER_BASE_X100));

        CosmicHeart restored = CosmicHeart.deserializeNBT(heart.serializeNBT());
        assertEquals(BigInteger.valueOf(3L), FluxMath8.toBigInteger(
                restored.getPlannedRewardCountExact()));
        complete(restored);
        assertEquals(CosmicHeart.RuleModification.MULTIPLIER_BASE_X100,
                restored.claimNextPlannedReward());
        assertEquals(2, restored.getModificationCount(
                CosmicHeart.RuleModification.MULTIPLIER_BASE_X100));
    }

    @Test
    void everyRewardCanBePlannedRepeatedly() {
        CosmicHeart heart = completedHeart();
        AbsoluteInteger[] plan = emptyPlan();
        for (CosmicHeart.RuleModification reward : CosmicHeart.RuleModification.values()) {
            assertTrue(reward.repeatable);
            Arrays.setAll(plan, ignored -> new AbsoluteInteger());
            plan[reward.ordinal()] = absolute(2L);
            assertTrue(heart.setRewardPlan(plan));
        }
    }

    @Test
    void renamedRewardsKeepLegacyKeysAndExposeExactMultipliers() {
        CosmicHeart heart = new CosmicHeart();
        assertTrue(heart.applyModification(
                CosmicHeart.RuleModification.LAYER_UNLOCK_THRESHOLD_HALVE));
        assertTrue(heart.applyModification(
                CosmicHeart.RuleModification.ANTIMATTER_THRESHOLD_HALVE));
        assertTrue(heart.applyModification(
                CosmicHeart.RuleModification.ANTIMATTER_THRESHOLD_HALVE));

        CosmicHeart restored = CosmicHeart.deserializeNBT(heart.serializeNBT());
        assertEquals("层级解锁阈值 全部减半",
                CosmicHeart.RuleModification.LAYER_UNLOCK_THRESHOLD_HALVE.name);
        assertEquals("反物质解锁阈值 减半",
                CosmicHeart.RuleModification.ANTIMATTER_THRESHOLD_HALVE.name);
        assertEquals(BigInteger.ONE, FluxMath8.toBigInteger(
                restored.getHierarchyEffectLayersExact()));
        assertEquals(0, restored.getAntimatterEffectMultiplierExact()
                .compareTo(sonar.fluxnetworks.api.energy.BigNumber.valueOf(4L)));
    }

    @Test
    void emptyPlanIsRejectedAndThousandDigitPlanRemainsExact() {
        CosmicHeart heart = completedHeart();
        AbsoluteInteger[] plan = emptyPlan();
        assertFalse(heart.setRewardPlan(plan));

        BigInteger huge = BigInteger.TEN.pow(1_000);
        plan[CosmicHeart.RuleModification.ALL_STAR_OUTPUT_X2.ordinal()] =
                FluxMath8.fromBigInteger(huge);
        assertTrue(heart.setRewardPlan(plan));
        assertEquals(CosmicHeart.RuleModification.ALL_STAR_OUTPUT_X2,
                heart.claimNextPlannedReward());
        assertEquals(huge.subtract(BigInteger.ONE), FluxMath8.toBigInteger(
                heart.getPlannedRewardCountExact()));
    }

    @Test
    void aggregateHeartScalesRequirementsAndSurvivesSave() {
        CosmicHeart heart = new CosmicHeart();
        heart.activateBatch("sphere", BigNumber.valueOf(2L), BigNumber.valueOf(3L), absolute(7L));

        CosmicHeart restored = CosmicHeart.deserializeNBT(heart.serializeNBT());

        assertEquals(BigInteger.valueOf(7L), FluxMath8.toBigInteger(restored.getBatchCountExact()));
        assertEquals(0, heart.getRequiredBeams().compareTo(restored.getRequiredBeams()));
        assertEquals(0, heart.getRequiredSolarPanels().compareTo(restored.getRequiredSolarPanels()));
    }

    @Test
    void aggregateRewardRequiresAndClaimsEveryCurrentHitTogether() {
        CosmicHeart heart = new CosmicHeart();
        heart.activateBatch("sphere", BigNumber.valueOf(1L), BigNumber.valueOf(1L), absolute(5L));
        assertTrue(heart.acceptStructureMaterials(
                heart.getRequiredBeams(), heart.getRequiredSolarPanels()));

        AbsoluteInteger[] shortPlan = emptyPlan();
        shortPlan[CosmicHeart.RuleModification.ALL_STAR_OUTPUT_X2.ordinal()] = absolute(4L);
        assertFalse(heart.setRewardPlan(shortPlan));

        AbsoluteInteger[] completePlan = emptyPlan();
        completePlan[CosmicHeart.RuleModification.ALL_STAR_OUTPUT_X2.ordinal()] = absolute(3L);
        completePlan[CosmicHeart.RuleModification.TOTAL_WRAPPED_PLUS_1M.ordinal()] = absolute(4L);
        assertTrue(heart.setRewardPlan(completePlan));

        AbsoluteInteger[] claimed = heart.claimPlannedRewardBatch();
        assertEquals(BigInteger.valueOf(3L), FluxMath8.toBigInteger(claimed[
                CosmicHeart.RuleModification.ALL_STAR_OUTPUT_X2.ordinal()]));
        assertEquals(BigInteger.valueOf(2L), FluxMath8.toBigInteger(claimed[
                CosmicHeart.RuleModification.TOTAL_WRAPPED_PLUS_1M.ordinal()]));
        assertFalse(heart.isComplete());
        assertEquals(BigInteger.valueOf(2L), FluxMath8.toBigInteger(
                heart.getPlannedRewardCountExact()));
    }

    private static CosmicHeart completedHeart() {
        CosmicHeart heart = new CosmicHeart();
        complete(heart);
        return heart;
    }

    private static void complete(CosmicHeart heart) {
        heart.activate();
        heart.coverArea(CosmicHeart.TARGET_AREA);
        assertTrue(heart.isComplete());
    }

    private static AbsoluteInteger[] emptyPlan() {
        AbsoluteInteger[] plan = new AbsoluteInteger[CosmicHeart.RuleModification.values().length];
        Arrays.setAll(plan, ignored -> new AbsoluteInteger());
        return plan;
    }

    private static AbsoluteInteger absolute(long value) {
        return AbsoluteInteger.parse(Long.toString(value));
    }
}
