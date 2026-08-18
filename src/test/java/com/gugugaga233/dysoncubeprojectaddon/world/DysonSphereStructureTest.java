package com.gugugaga233.dysoncubeprojectaddon.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gugugaga233.dysoncubeprojectaddon.Config;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.CosmicHeart;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.StarData;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.UniverseHierarchy;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.UniverseRandomizer;
import java.time.Duration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;
import sonar.fluxnetworks.api.energy.BigNumber;

class DysonSphereStructureTest {

    @Test
    void solarRequirementUsesRadiusSquaredInsteadOfFixedFiftyMillionCap() {
        assertBigNumberEquals("50000000", DysonSphereStructure.calculateRequiredSolarPanels(StarData.SUN));

        StarData radiusTen = starWithRadius(10.0D);
        assertBigNumberEquals("5000000000", DysonSphereStructure.calculateRequiredSolarPanels(radiusTen));
    }

    @Test
    void enormousRadiusRequirementDoesNotOverflowPrimitiveStorage() {
        BigNumber requirement = DysonSphereStructure.calculateRequiredSolarPanels(starWithRadius(1.0E200D));

        assertEquals(0, BigNumber.scientific("5", "407").compareTo(requirement));
        assertTrue(requirement.compareTo(BigNumber.valueOf(Integer.MAX_VALUE)) > 0);
    }

    @Test
    void beamRequirementRoundsUp() {
        DysonSphereStructure structure = new DysonSphereStructure();

        BigNumber expected = BigNumber.valueOf(
                (Config.getBaseSolarSailRequirement() + Config.BEAM_TO_SOLAR_PANEL_RATIO - 1L)
                        / Config.BEAM_TO_SOLAR_PANEL_RATIO);
        assertEquals(0, expected.compareTo(structure.getRequiredBeams()));
    }

    @Test
    void enormousProgressUsesBigNumberRatioInsteadOfDoubleProjection() {
        DysonSphereStructure structure = new DysonSphereStructure();
        structure.setCurrentStarData(starWithRadius(1.0E200D));
        BigNumber halfway = structure.getRequiredSolarPanels().divideSmallInteger(2L);
        structure.setBeams(structure.getRequiredBeams());
        structure.setSolarPanels(halfway);

        assertEquals(0.5D, structure.getProgress(), 0.0001D);
    }

    @Test
    void oversizedInputCrossesStarsAndKeepsBigNumberRemainder() {
        DysonSphereStructure structure = new DysonSphereStructure();
        BigNumber oversized = BigNumber.scientific("1", "100");
        structure.addStructureMaterials(oversized, oversized);

        int completed = structure.processStructureMaterials(64);

        assertEquals(64, completed);
        assertEquals(64, structure.getTotalWrapped());
        assertFalse(structure.getBeamReserve().isZero());
        assertFalse(structure.getSolarPanelReserve().isZero());
        assertTrue(structure.getBeamReserve().compareTo(BigNumber.valueOf(Integer.MAX_VALUE)) > 0);
        assertTrue(structure.getSolarPanelReserve().compareTo(BigNumber.valueOf(Integer.MAX_VALUE)) > 0);
    }

    @Test
    void oversizedInputStopsAtCurrentHierarchySettlementCap() {
        DysonSphereStructure structure = new DysonSphereStructure();
        BigNumber oversized = BigNumber.scientific("1", "100");
        structure.addStructureMaterials(oversized, oversized);

        int completed = structure.processStructureMaterials(4_096);

        assertEquals(100, completed);
        assertEquals(100, structure.getTotalWrapped());
        assertFalse(structure.getBeamReserve().isZero());
        assertFalse(structure.getSolarPanelReserve().isZero());
        assertFalse(structure.getRawEnergy().isZero());
    }

    @Test
    void expiredDeadlineStopsAfterOneMathematicalStageAndKeepsRemainder() {
        DysonSphereStructure structure = new DysonSphereStructure();
        BigNumber oversized = BigNumber.scientific("1", "100");
        structure.addStructureMaterials(oversized, oversized);

        int completed = structure.processStructureMaterials(4_096, 0L);

        assertEquals(100, completed);
        assertEquals(100, structure.getTotalWrapped());
        assertFalse(structure.getBeamReserve().isZero());
        assertFalse(structure.getSolarPanelReserve().isZero());
    }

    @Test
    void explicitMaximumStillHonorsHierarchySettlementCap() {
        DysonSphereStructure structure = new DysonSphereStructure();
        BigNumber oversized = BigNumber.scientific("1", "100");
        structure.addStructureMaterials(oversized, oversized);

        int completed = structure.processStructureMaterials(10_001);

        assertEquals(100, completed);
        assertEquals(100, structure.getTotalWrapped());
        assertEquals(0, absolute(100).compareTo(structure.getLastProcessedWrapsExact()));
    }

    @Test
    void levelEightUsesItsExactDynamicSettlementCap() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            DysonSphereStructure structure = new DysonSphereStructure();
            structure.setTotalWrappedExact(absolute(5_000_000_000L));
            BigNumber oversized = BigNumber.scientific("1", "100");
            structure.addStructureMaterials(oversized, oversized);

            int completed = structure.processStructureMaterials(Integer.MAX_VALUE);

            assertEquals(1_000_000_000, completed);
            assertEquals(UniverseHierarchy.Level.STAR_UNIVERSE, structure.getCurrentLevel());
            assertEquals(0, structure.getLastProcessedWrapsExact()
                    .compareTo(absolute(1_000_000_000L)));
        });
    }

    @Test
    void finalLandingStarIsGeneratedButDoesNotConsumeMaterialsEarly() {
        DysonSphereStructure structure = new DysonSphereStructure();
        UniverseRandomizer.AverageStructureCost average =
                UniverseRandomizer.averageStructureCost(absolute(1));
        BigNumber beams = structure.getRequiredBeams().add(BigNumber.valueOf(average.beams()));
        BigNumber panels = structure.getRequiredSolarPanels().add(BigNumber.valueOf(average.solarPanels()));
        structure.addStructureMaterials(beams, panels);

        assertEquals(2, structure.processStructureMaterials(2));
        assertTrue(structure.getBeamReserve().isZero());
        assertTrue(structure.getSolarPanelReserve().isZero());
        assertTrue(structure.getBeams().isZero());
        assertTrue(structure.getSolarPanels().isZero());
    }

    @Test
    void completedDarkMatterStarOwnsTheResonanceIncrease() {
        DysonSphereStructure structure = new DysonSphereStructure();
        structure.setCurrentStarData(new StarData(
                "DARK_MATTER", "Dark Matter", 1.0D, 1.0D, 0.0D, 0.0D, true));
        structure.addStructureMaterials(
                structure.getRequiredBeams(), structure.getRequiredSolarPanels());

        assertEquals(1, structure.processStructureMaterials(1));
        assertEquals(1, structure.getDarkMatterResonance());
    }

    @Test
    void lv100Lv1000AndLv10000ReservesSettleWithinOneSecondEach() {
        for (int level : new int[]{100, 1_000, 10_000}) {
            assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
                DysonSphereStructure structure = new DysonSphereStructure();
                BigNumber reserve = BigNumber.scientific("1", Integer.toString(level));
                structure.addStructureMaterials(reserve, reserve);

                structure.processStructureMaterials(Integer.MAX_VALUE);

                assertEquals(0, structure.getLastProcessedWrapsExact().compareTo(absolute(100)));
                assertFalse(structure.getBeamReserve().isZero());
                assertFalse(structure.getSolarPanelReserve().isZero());
            }, "LV" + level + " settlement regressed to quantity-dependent iteration");
        }
    }

    @Test
    void fourMillionExponentReserveIsCappedBeforeExactDivision() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            DysonSphereStructure structure = new DysonSphereStructure();
            BigNumber reserve = BigNumber.scientific("1", "4000000");
            structure.addStructureMaterials(reserve, reserve);

            int completed = structure.processStructureMaterials(Integer.MAX_VALUE);

            assertEquals(100, completed);
            assertEquals(0, structure.getLastProcessedWrapsExact().compareTo(absolute(100)));
            assertFalse(structure.getBeamReserve().isZero());
            assertFalse(structure.getSolarPanelReserve().isZero());
        });
    }

    @Test
    void requirementCacheInvalidatesWhenCurrentStarChanges() {
        DysonSphereStructure structure = new DysonSphereStructure();
        BigNumber solarRequirement = structure.getRequiredSolarPanels();

        structure.setCurrentStarData(starWithRadius(10.0D));

        assertEquals(0, solarRequirement.multiplySmallInteger(100L)
                .compareTo(structure.getRequiredSolarPanels()));
    }

    @Test
    void requirementCacheRefreshesWhenConfigurationChanges() {
        int originalBase = Config.BASE_SOLAR_SAIL_REQUIREMENT;
        int originalLegacy = Config.MAX_SOLAR_PANELS;
        try {
            Config.BASE_SOLAR_SAIL_REQUIREMENT = 50_000_001;
            DysonSphereStructure structure = new DysonSphereStructure();
            assertBigNumberEquals("50000001", structure.getRequiredSolarPanels());

            Config.BASE_SOLAR_SAIL_REQUIREMENT = 50_000_002;
            assertBigNumberEquals("50000002", structure.getRequiredSolarPanels());
        } finally {
            Config.BASE_SOLAR_SAIL_REQUIREMENT = originalBase;
            Config.MAX_SOLAR_PANELS = originalLegacy;
        }
    }

    @Test
    void bigNumberNbtRoundTripPreservesInstalledAndReserveValues() {
        DysonSphereStructure original = new DysonSphereStructure();
        original.setCurrentStarData(starWithRadius(1.0E200D));
        original.setBeams(BigNumber.scientific("2", "300"));
        original.setSolarPanels(BigNumber.scientific("3", "300"));
        original.addStructureMaterials(BigNumber.scientific("4", "500"), BigNumber.scientific("5", "500"));

        CompoundTag tag = original.serializeNBT(null);
        DysonSphereStructure restored = new DysonSphereStructure();
        restored.deserializeNBT(null, tag);

        assertTrue(tag.contains("beamsBigNumber", Tag.TAG_COMPOUND));
        assertTrue(tag.contains("solarPanelsBigNumber", Tag.TAG_COMPOUND));
        assertEquals(0, original.getBeams().compareTo(restored.getBeams()));
        assertEquals(0, original.getSolarPanels().compareTo(restored.getSolarPanels()));
        assertEquals(0, original.getBeamReserve().compareTo(restored.getBeamReserve()));
        assertEquals(0, original.getSolarPanelReserve().compareTo(restored.getSolarPanelReserve()));
    }

    @Test
    void absoluteCountersSurviveNbtAboveLongAndBigIntegerUiRanges() {
        AbsoluteInteger huge = AbsoluteInteger.parse("9".repeat(10_000));
        DysonSphereStructure original = new DysonSphereStructure();
        original.setTotalWrappedExact(huge);

        DysonSphereStructure restored = new DysonSphereStructure();
        restored.deserializeNBT(null, original.serializeNBT(null));

        assertEquals(0, huge.compareTo(restored.getTotalWrappedExact()));
        assertEquals(Long.MAX_VALUE, restored.getTotalWrapped());
    }

    @Test
    void primordialQiBatchCountUsesExactStorageAndSurvivesNbt() {
        AbsoluteInteger huge = AbsoluteInteger.parse("9".repeat(1_000));
        DysonSphereStructure original = new DysonSphereStructure();
        original.applyPrimordialQiEffects(huge);

        DysonSphereStructure restored = new DysonSphereStructure();
        restored.deserializeNBT(null, original.serializeNBT(null));

        assertEquals(0, huge.compareTo(restored.getPrimordialQiCountExact()));
        assertEquals(Integer.MAX_VALUE, restored.getPrimordialQiCount());
        assertFalse(restored.getPrimordialQiOutputMultiplierBigNumber().isZero());
    }

    @Test
    void primordialQiChoicePendingSurvivesNbtAndIsConsumedOnce() {
        DysonSphereStructure original = new DysonSphereStructure();
        original.applyPrimordialQiEffects(10);
        assertTrue(original.markPrimordialQiChoicePending());
        assertFalse(original.markPrimordialQiChoicePending());

        DysonSphereStructure restored = new DysonSphereStructure();
        restored.deserializeNBT(null, original.serializeNBT(null));

        assertTrue(restored.hasPrimordialQiChoicePending());
        assertTrue(restored.consumePrimordialQiChoicePending());
        assertFalse(restored.consumePrimordialQiChoicePending());
    }

    @Test
    void forcedCosmicHeartFlagSurvivesNbtRoundTrip() {
        DysonSphereStructure original = new DysonSphereStructure();
        assertTrue(original.armForcedCosmicHeart());

        DysonSphereStructure restored = new DysonSphereStructure();
        restored.deserializeNBT(null, original.serializeNBT(null));

        assertTrue(restored.isForcedCosmicHeartPending());
        assertFalse(restored.isForcedCosmicHeartReady());
        assertFalse(restored.armForcedCosmicHeart());
    }

    @Test
    void forcedCosmicHeartStopsHugeReserveAtNextStarAndKeepsRemainder() {
        DysonSphereStructure structure = new DysonSphereStructure();
        BigNumber oversized = BigNumber.scientific("1", "100");
        structure.addStructureMaterials(oversized, oversized);
        assertTrue(structure.armForcedCosmicHeart());

        int completed = structure.processStructureMaterials(4_096);

        assertEquals(1, completed);
        assertEquals(1, structure.getTotalWrapped());
        assertTrue(structure.isForcedCosmicHeartReady());
        assertFalse(structure.getBeamReserve().isZero());
        assertFalse(structure.getSolarPanelReserve().isZero());
        assertEquals(0, structure.processStructureMaterials(4_096));
    }

    @Test
    void forcedCosmicHeartActivatesOnceThenAllowsLaterStars() {
        DysonSphereStructure structure = new DysonSphereStructure();
        BigNumber oversized = BigNumber.scientific("1", "10000");
        structure.addStructureMaterials(oversized, oversized);
        assertTrue(structure.armForcedCosmicHeart());
        assertEquals(1, structure.processStructureMaterials(4_096));

        DysonSphereProgressSavedData data = new DysonSphereProgressSavedData();
        assertTrue(structure.activateForcedCosmicHeart(data));
        assertTrue(data.getCosmicHeart().isActive());
        assertFalse(structure.isForcedCosmicHeartPending());
        assertFalse(structure.activateForcedCosmicHeart(data));

        assertEquals(1, structure.processStructureMaterials(data, "", 2, Long.MAX_VALUE));
        assertEquals(2, structure.getTotalWrapped());
        assertTrue(data.getCosmicHeart().isComplete());
        assertEquals(2, structure.processStructureMaterials(data, "", 2, Long.MAX_VALUE));
        assertEquals(4, structure.getTotalWrapped());
    }

    @Test
    void cosmicHeartScalesTheCurrentStarsNormalRequirementsByAreaAndInputCost() {
        DysonSphereStructure structure = new DysonSphereStructure();
        structure.setCurrentStarData(starWithRadius(10.0D));
        DysonSphereProgressSavedData data = new DysonSphereProgressSavedData();
        BigNumber normalBeams = structure.getRequiredBeams();
        BigNumber normalPanels = structure.getRequiredSolarPanels();

        data.getCosmicHeart().activate("sphere",
                normalBeams, normalPanels);

        BigNumber factor = new BigNumber(CosmicHeart.TARGET_AREA)
                .multiply(new BigNumber(CosmicHeart.SINGLE_INPUT_COST));

        assertEquals(0, normalBeams.multiply(factor.deepCopy())
                .compareTo(data.getCosmicHeart().getRequiredBeams()));
        assertEquals(0, normalPanels.multiply(factor.deepCopy())
                .compareTo(data.getCosmicHeart().getRequiredSolarPanels()));
    }

    @Test
    void oneLevelTwelvePackagePerMaterialCannotCompleteCosmicHeart() {
        CosmicHeart heart = new CosmicHeart();
        heart.activate("sphere", BigNumber.valueOf(1L), BigNumber.valueOf(1L));
        BigNumber beams = BigNumber.scientific("36", "11");
        BigNumber panels = BigNumber.scientific("72", "11");

        assertFalse(heart.acceptStructureMaterials(beams.deepCopy(), panels.deepCopy()));

        assertTrue(heart.isActive());
        assertFalse(heart.isComplete());
        assertEquals(0, beams.compareTo(heart.getInstalledBeams()));
        assertEquals(0, panels.compareTo(heart.getInstalledSolarPanels()));
        assertTrue(heart.getRequiredBeams().compareTo(beams) > 0);
        assertTrue(heart.getRequiredSolarPanels().compareTo(panels) > 0);
    }

    @Test
    void legacyCosmicHeartNbtRequirementMigrationRunsOnlyOnce() {
        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putBoolean("active", true);
        legacyTag.put("requiredBeams", BigNumber.valueOf(2L).toTag());
        legacyTag.put("requiredSolarPanels", BigNumber.valueOf(3L).toTag());

        CosmicHeart migrated = CosmicHeart.deserializeNBT(legacyTag);
        BigNumber migratedBeams = migrated.getRequiredBeams();
        BigNumber migratedPanels = migrated.getRequiredSolarPanels();
        CosmicHeart restoredAgain = CosmicHeart.deserializeNBT(migrated.serializeNBT());

        assertEquals(0, migratedBeams.compareTo(restoredAgain.getRequiredBeams()));
        assertEquals(0, migratedPanels.compareTo(restoredAgain.getRequiredSolarPanels()));
    }

    @Test
    void legacyPrematureCompletionReopensUntilItsRewardWasClaimed() {
        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putBoolean("complete", true);
        legacyTag.put("requiredBeams", BigNumber.valueOf(2L).toTag());
        legacyTag.put("requiredSolarPanels", BigNumber.valueOf(3L).toTag());
        legacyTag.put("installedBeams", BigNumber.valueOf(2L).toTag());
        legacyTag.put("installedSolarPanels", BigNumber.valueOf(3L).toTag());

        CosmicHeart migrated = CosmicHeart.deserializeNBT(legacyTag);

        assertTrue(migrated.isActive());
        assertFalse(migrated.isComplete());
        assertEquals(0, BigNumber.valueOf(2L).compareTo(migrated.getInstalledBeams()));
        assertEquals(0, BigNumber.valueOf(3L).compareTo(migrated.getInstalledSolarPanels()));
        assertTrue(migrated.getRequiredBeams().compareTo(migrated.getInstalledBeams()) > 0);
        assertTrue(migrated.getRequiredSolarPanels()
                .compareTo(migrated.getInstalledSolarPanels()) > 0);
    }

    @Test
    void legacyCompletionWithClaimedRewardIsNotRolledBack() {
        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putBoolean("complete", true);
        legacyTag.put("requiredBeams", BigNumber.valueOf(2L).toTag());
        legacyTag.put("requiredSolarPanels", BigNumber.valueOf(3L).toTag());
        CompoundTag modifications = new CompoundTag();
        modifications.putInt(CosmicHeart.RuleModification.ALL_STAR_OUTPUT_X2.name, 1);
        legacyTag.put("modifications", modifications);

        CosmicHeart migrated = CosmicHeart.deserializeNBT(legacyTag);

        assertFalse(migrated.isActive());
        assertTrue(migrated.isComplete());
        assertEquals(1, migrated.getModificationCount(
                CosmicHeart.RuleModification.ALL_STAR_OUTPUT_X2));
    }

    @Test
    void activeCosmicHeartConsumesBothMaterialsBeforeAnyOrdinaryStar() {
        DysonSphereStructure structure = new DysonSphereStructure();
        DysonSphereProgressSavedData data = new DysonSphereProgressSavedData();
        BigNumber requiredBeams = structure.getRequiredBeams();
        BigNumber requiredPanels = structure.getRequiredSolarPanels();
        data.getCosmicHeart().activate("sphere", requiredBeams, requiredPanels);

        BigNumber partialBeams = requiredBeams.divideSmallInteger(2L);
        structure.addStructureMaterials(partialBeams, requiredPanels);

        assertEquals(0, structure.processStructureMaterials(
                data, "sphere", Integer.MAX_VALUE, Long.MAX_VALUE));
        assertEquals(0, structure.getTotalWrapped());
        assertEquals(0, partialBeams.compareTo(data.getCosmicHeart().getInstalledBeams()));
        assertEquals(0, requiredPanels.compareTo(data.getCosmicHeart().getInstalledSolarPanels()));
        assertTrue(structure.getBeamReserve().isZero());
        assertTrue(structure.getSolarPanelReserve().isZero());
        assertTrue(data.getCosmicHeart().isActive());
    }

    @Test
    void repeatableWrappedStarRewardAddsOneMillionEachTime() {
        DysonSphereProgressSavedData data = new DysonSphereProgressSavedData();
        DysonSphereStructure sphere = new DysonSphereStructure();
        sphere.setTotalWrapped(7L);
        data.setTotalWrapped(7L);
        data.getSpheres().put("sphere", sphere);

        data.applyCosmicHeartReward(CosmicHeart.RuleModification.TOTAL_WRAPPED_PLUS_1M);
        data.applyCosmicHeartReward(CosmicHeart.RuleModification.TOTAL_WRAPPED_PLUS_1M);

        assertEquals(2_000_007L, data.getTotalWrapped());
        assertEquals(2_000_007L, sphere.getTotalWrapped());
    }

    @Test
    void completingCosmicHeartCountsOneTargetAndKeepsOversizedRemainderForNextTick() {
        DysonSphereStructure structure = new DysonSphereStructure();
        DysonSphereProgressSavedData data = new DysonSphereProgressSavedData();
        BigNumber requiredBeams = structure.getRequiredBeams();
        BigNumber requiredPanels = structure.getRequiredSolarPanels();
        BigNumber oversized = BigNumber.scientific("1", "10000");
        data.getCosmicHeart().activate("sphere", requiredBeams, requiredPanels);
        requiredBeams = data.getCosmicHeart().getRequiredBeams();
        requiredPanels = data.getCosmicHeart().getRequiredSolarPanels();
        structure.addStructureMaterials(oversized, oversized);

        BigNumber expectedBeams = oversized.deepCopy().subtract(requiredBeams);
        BigNumber expectedPanels = oversized.deepCopy().subtract(requiredPanels);
        assertEquals(1, structure.processStructureMaterials(
                data, "sphere", Integer.MAX_VALUE, Long.MAX_VALUE));

        assertEquals(1, structure.getTotalWrapped());
        assertTrue(data.getCosmicHeart().isComplete());
        assertEquals(0, expectedBeams.compareTo(structure.getBeamReserve()));
        assertEquals(0, expectedPanels.compareTo(structure.getSolarPanelReserve()));
        assertEquals(1, com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8
                .toIntSaturated(structure.getLastProcessedWrapsExact()));

        assertTrue(structure.processStructureMaterials(
                data, "sphere", Integer.MAX_VALUE, 0L) > 0);
        assertTrue(structure.getTotalWrappedExact().compareTo(absolute(1L)) > 0);
    }

    @Test
    void completingAggregateCosmicHeartCreditsEveryMergedHitAtOnce() {
        DysonSphereStructure structure = new DysonSphereStructure();
        DysonSphereProgressSavedData data = new DysonSphereProgressSavedData();
        data.getCosmicHeart().activateBatch("sphere",
                structure.getRequiredBeams(), structure.getRequiredSolarPanels(), absolute(6L));
        structure.addStructureMaterials(
                data.getCosmicHeart().getRequiredBeams(),
                data.getCosmicHeart().getRequiredSolarPanels());

        assertEquals(6, structure.processStructureMaterials(
                data, "sphere", Integer.MAX_VALUE, Long.MAX_VALUE));
        assertEquals(6L, structure.getTotalWrapped());
        assertEquals(6, com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8
                .toIntSaturated(structure.getLastProcessedWrapsExact()));
        assertTrue(data.getCosmicHeart().isComplete());
    }

    @Test
    void activeCosmicHeartBlocksNonTargetDysonSpheresGlobally() {
        DysonSphereProgressSavedData data = new DysonSphereProgressSavedData();
        DysonSphereStructure target = new DysonSphereStructure();
        DysonSphereStructure other = new DysonSphereStructure();
        data.getCosmicHeart().activate("target",
                target.getRequiredBeams(), target.getRequiredSolarPanels());
        BigNumber reserve = BigNumber.scientific("1", "100");
        other.addStructureMaterials(reserve, reserve);

        assertEquals(0, other.processStructureMaterials(
                data, "other", Integer.MAX_VALUE, Long.MAX_VALUE));
        assertEquals(0, other.getTotalWrapped());
        assertEquals(0, reserve.compareTo(other.getBeamReserve()));
        assertEquals(0, reserve.compareTo(other.getSolarPanelReserve()));
    }

    @Test
    void activeCosmicHeartRoutesLaunchersToItsOwningSphereAndPreservesOldReserves() {
        DysonSphereProgressSavedData data = new DysonSphereProgressSavedData();
        DysonSphereStructure target = new DysonSphereStructure();
        DysonSphereStructure oldLauncherSphere = new DysonSphereStructure();
        data.getSpheres().put("target", target);
        data.getSpheres().put("old", oldLauncherSphere);
        data.getCosmicHeart().activate("target",
                target.getRequiredBeams(), target.getRequiredSolarPanels());
        BigNumber oldBeams = BigNumber.scientific("36", "999");
        BigNumber oldSails = BigNumber.scientific("72", "999");
        oldLauncherSphere.addStructureMaterials(oldBeams, oldSails);

        assertEquals("target", data.resolveStructureTargetSphereId("old"));
        oldLauncherSphere.transferStructureReservesTo(target);

        assertTrue(oldLauncherSphere.getBeamReserve().isZero());
        assertTrue(oldLauncherSphere.getSolarPanelReserve().isZero());
        assertEquals(0, oldBeams.compareTo(target.getBeamReserve()));
        assertEquals(0, oldSails.compareTo(target.getSolarPanelReserve()));
        assertEquals(1, target.processStructureMaterials(
                data, "target", Integer.MAX_VALUE, Long.MAX_VALUE));
        assertTrue(data.getCosmicHeart().isComplete());
    }

    @Test
    void cosmicHeartProgressMovesPastFiftyWhenMissingMaterialArrives() {
        CosmicHeart heart = new CosmicHeart();
        heart.activate("target", BigNumber.valueOf(1L), BigNumber.valueOf(1L));
        BigNumber requiredBeams = heart.getRequiredBeams();
        BigNumber requiredSails = heart.getRequiredSolarPanels();

        heart.acceptStructureMaterials(requiredBeams.deepCopy(), new BigNumber(0));
        assertEquals("50.000000", heart.getProgress().toPlainString());

        heart.acceptStructureMaterials(new BigNumber(0), requiredSails.divideSmallInteger(2L));
        assertTrue(heart.getProgress().compareTo(new java.math.BigDecimal("50.000000")) > 0);
    }

    @Test
    void cosmicHeartOnlyCompletesAfterTheFinalExactMaterialUnitArrives() {
        CosmicHeart heart = new CosmicHeart();
        heart.activate("target", BigNumber.valueOf(1L), BigNumber.valueOf(1L));
        BigNumber requiredBeams = heart.getRequiredBeams();
        BigNumber requiredSails = heart.getRequiredSolarPanels();
        BigNumber almostAllBeams = BigNumber.valueOf(exactInteger(requiredBeams)
                .subtract(java.math.BigInteger.ONE));
        BigNumber almostAllSails = BigNumber.valueOf(exactInteger(requiredSails)
                .subtract(java.math.BigInteger.ONE));

        assertFalse(heart.acceptStructureMaterials(almostAllBeams, almostAllSails));
        assertTrue(heart.isActive());
        assertFalse(heart.isComplete());
        assertTrue(heart.getProgress().compareTo(new java.math.BigDecimal("100")) < 0);
        assertEquals(0, BigNumber.valueOf(1L).compareTo(
                requiredBeams.deepCopy().subtract(heart.getInstalledBeams())));
        assertEquals(0, BigNumber.valueOf(1L).compareTo(
                requiredSails.deepCopy().subtract(heart.getInstalledSolarPanels())));

        assertTrue(heart.acceptStructureMaterials(BigNumber.valueOf(1L), BigNumber.valueOf(1L)));
        assertFalse(heart.isActive());
        assertTrue(heart.isComplete());
        assertEquals("100", heart.getProgress().toPlainString());
    }

    @Test
    void cosmicHeartDoesNotConsumeAReserveTooSmallToChangeInstalledMaterials() {
        CosmicHeart heart = new CosmicHeart();
        heart.activate("target", BigNumber.valueOf(1L), BigNumber.valueOf(1L));
        BigNumber requiredBeams = heart.getRequiredBeams();
        BigNumber requiredSails = heart.getRequiredSolarPanels();
        BigNumber initialBeams = BigNumber.valueOf(exactInteger(requiredBeams)
                .subtract(java.math.BigInteger.TWO));
        BigNumber initialSails = BigNumber.valueOf(exactInteger(requiredSails)
                .subtract(java.math.BigInteger.TWO));
        heart.acceptStructureMaterials(initialBeams, initialSails);
        BigNumber oneBeam = BigNumber.valueOf(1L);
        BigNumber oneSail = BigNumber.valueOf(1L);

        assertFalse(heart.acceptStructureMaterials(oneBeam, oneSail));
        assertEquals(0, BigNumber.valueOf(1L).compareTo(oneBeam));
        assertEquals(0, BigNumber.valueOf(1L).compareTo(oneSail));
        assertFalse(heart.isComplete());

        oneBeam.addEnergy(1L);
        oneSail.addEnergy(1L);
        assertTrue(heart.acceptStructureMaterials(oneBeam, oneSail));
        assertTrue(oneBeam.isZero());
        assertTrue(oneSail.isZero());
        assertTrue(heart.isComplete());
    }

    @Test
    void darkMatterResonanceAggregationNeverSaturatesToInt() {
        DysonSphereProgressSavedData data = new DysonSphereProgressSavedData();
        DysonSphereStructure lower = new DysonSphereStructure();
        DysonSphereStructure higher = new DysonSphereStructure();
        AbsoluteInteger huge = AbsoluteInteger.parse("9".repeat(240));
        CompoundTag lowerTag = lower.serializeNBT(null);
        lowerTag.put("darkMatterResonanceExact",
                AbsoluteInteger.parse("2147483648").toTag());
        lower.deserializeNBT(null, lowerTag);
        CompoundTag higherTag = higher.serializeNBT(null);
        higherTag.put("darkMatterResonanceExact", huge.toTag());
        higher.deserializeNBT(null, higherTag);
        data.getSpheres().put("lower", lower);
        data.getSpheres().put("higher", higher);

        assertEquals(0, huge.compareTo(data.getMaximumDarkMatterResonanceExact()));

        DysonSphereStructure restored = new DysonSphereStructure();
        restored.deserializeNBT(null, higher.serializeNBT(null));
        assertEquals(0, huge.compareTo(restored.getDarkMatterResonanceExact()));
    }

    @Test
    void cosmicHeartBigNumberProgressSurvivesNbtWithoutExpandingHugeExponent() {
        CosmicHeart original = new CosmicHeart();
        BigNumber required = BigNumber.scientific("9", "10000");
        BigNumber installed = BigNumber.scientific("4", "9999");
        original.activate("sphere", required, required);
        BigNumber scaledRequired = original.getRequiredBeams();
        original.acceptStructureMaterials(installed.deepCopy(), installed.deepCopy());

        CompoundTag tag = original.serializeNBT();
        CosmicHeart restored = CosmicHeart.deserializeNBT(tag);

        assertEquals("sphere", restored.getTargetSphereId());
        assertEquals(0, scaledRequired.compareTo(restored.getRequiredBeams()));
        assertEquals(0, scaledRequired.compareTo(restored.getRequiredSolarPanels()));
        assertEquals(0, installed.compareTo(restored.getInstalledBeams()));
        assertEquals(0, installed.compareTo(restored.getInstalledSolarPanels()));
        assertTrue(tag.getCompound("requiredBeams").toString().length() < 1_000);
    }

    @Test
    void legacyActiveCosmicHeartClaimsCurrentTargetAndScaledRequirements() {
        CosmicHeart legacy = CosmicHeart.deserializeFromString("1|0|123|");
        DysonSphereStructure structure = new DysonSphereStructure();
        BigNumber normalBeams = structure.getRequiredBeams();
        BigNumber normalPanels = structure.getRequiredSolarPanels();

        assertTrue(legacy.ensureStructureTarget("sphere",
                normalBeams, normalPanels));

        BigNumber factor = new BigNumber(CosmicHeart.TARGET_AREA)
                .multiply(new BigNumber(CosmicHeart.SINGLE_INPUT_COST));

        assertEquals("sphere", legacy.getTargetSphereId());
        assertEquals(0, normalBeams.multiply(factor.deepCopy())
                .compareTo(legacy.getRequiredBeams()));
        assertEquals(0, normalPanels.multiply(factor.deepCopy())
                .compareTo(legacy.getRequiredSolarPanels()));
    }

    @Test
    void generatedForcedTargetSurvivesNbtAndBlocksAnotherWrapUntilActivation() {
        DysonSphereStructure original = new DysonSphereStructure();
        assertTrue(original.armForcedCosmicHeart());
        assertTrue(original.completeWrap() != null);

        DysonSphereStructure restored = new DysonSphereStructure();
        restored.deserializeNBT(null, original.serializeNBT(null));

        assertTrue(restored.isForcedCosmicHeartReady());
        assertNull(restored.completeWrap());
        assertTrue(restored.activateForcedCosmicHeart(new DysonSphereProgressSavedData()));
        assertFalse(restored.isForcedCosmicHeartPending());
    }

    @Test
    void legacyIntNbtMigratesToBigNumberFields() {
        CompoundTag legacy = new CompoundTag();
        legacy.putInt("beams", 123456789);
        legacy.putInt("solarPanels", 987654321);

        DysonSphereStructure restored = new DysonSphereStructure();
        restored.deserializeNBT(null, legacy);

        assertBigNumberEquals("123456789", restored.getBeams());
        assertBigNumberEquals("987654321", restored.getSolarPanels());
    }

    @Test
    void generatedPowerUsesBigNumberSailCount() {
        DysonSphereStructure structure = new DysonSphereStructure();
        structure.setCurrentStarData(starWithRadius(1.0E200D));
        structure.setBeams(structure.getRequiredBeams());
        structure.setSolarPanels(BigNumber.scientific("1", "300"));

        structure.generatePower();

        BigNumber expected = BigNumber.scientific(Integer.toString(Config.POWER_PER_SAIL), "300");
        assertEquals(0, expected.compareTo(structure.getRawEnergy()));
    }

    @Test
    void cosmicHeartOutputRewardMultipliesActualGeneratedPower() {
        DysonSphereStructure baseline = new DysonSphereStructure();
        DysonSphereStructure boosted = new DysonSphereStructure();
        baseline.setSolarPanels(BigNumber.valueOf(1L));
        boosted.setSolarPanels(BigNumber.valueOf(1L));
        boosted.setCosmicHeartOutputLayers(absolute(3L));

        baseline.generatePower();
        boosted.generatePower();

        assertEquals(0, baseline.getRawEnergy().multiplySmallInteger(8L)
                .compareTo(boosted.getRawEnergy()));
    }

    @Test
    void completedSphereKeepsGeneratingWhileNextStarIsEmpty() {
        DysonSphereStructure structure = new DysonSphereStructure();
        structure.addStructureMaterials(
                structure.getRequiredBeams(), structure.getRequiredSolarPanels());

        assertEquals(1, structure.processStructureMaterials(1));
        assertTrue(structure.getSolarPanels().isZero());

        structure.generatePower();

        assertFalse(structure.getRawEnergy().isZero());
        assertFalse(structure.getStoredEnergy().isZero());
    }

    @Test
    void cachedPowerStillAccumulatesEveryTick() {
        DysonSphereStructure structure = poweredStructure();
        BigNumber afterFirstTick = structure.getStoredEnergy();

        structure.generatePower();

        assertEquals(0, afterFirstTick.multiplySmallInteger(2)
                .compareTo(structure.getStoredEnergy()));
    }

    @Test
    void batchedGenerationMatchesIndividualTicks() {
        DysonSphereStructure individual = poweredStructure();
        DysonSphereStructure batched = poweredStructure();

        for (int tick = 0; tick < 5; tick++) individual.generatePower();
        batched.generatePower(5L);

        assertEquals(0, individual.getStoredEnergy().compareTo(batched.getStoredEnergy()));
    }

    @Test
    void savedDataExposesOneSharedCacheForDysonAndMiningHubs() {
        DysonSphereProgressSavedData data = new DysonSphereProgressSavedData();
        data.getSpheres().put("first", poweredStructure());
        data.getSpheres().put("second", poweredStructure());

        BigNumber initial = data.getStoredEnergy();
        BigNumber simulated = data.extractStoredEnergy(BigNumber.valueOf(1_500L), true);
        assertBigNumberEquals("2000000000", initial);
        assertBigNumberEquals("1500", simulated);
        assertEquals(0, initial.compareTo(data.getStoredEnergy()));

        BigNumber extracted = data.extractStoredEnergy(BigNumber.valueOf(1_500L), false);
        assertBigNumberEquals("1500", extracted);
        assertBigNumberEquals("1999998500", data.getStoredEnergy());
    }

    private static DysonSphereStructure poweredStructure() {
        DysonSphereStructure structure = new DysonSphereStructure();
        structure.setBeams(structure.getRequiredBeams());
        structure.setSolarPanels(structure.getRequiredSolarPanels());
        structure.generatePower();
        return structure;
    }

    private static StarData starWithRadius(double radius) {
        return new StarData("TEST", "Test", radius, 1.0D, 1.0D, 0.0D, false);
    }

    private static void assertBigNumberEquals(String expected, BigNumber actual) {
        assertEquals(0, new BigNumber(expected).compareTo(actual));
    }

    private static AbsoluteInteger absolute(long value) {
        return AbsoluteInteger.parse(Long.toString(value));
    }

    private static java.math.BigInteger exactInteger(BigNumber value) {
        return value.getCoefficient()
                .scaleByPowerOfTen(value.toBigIntegerExponent().intValueExact())
                .toBigIntegerExact();
    }
}

