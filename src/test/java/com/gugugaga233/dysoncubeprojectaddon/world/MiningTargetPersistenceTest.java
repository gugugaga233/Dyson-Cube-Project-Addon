package com.gugugaga233.dysoncubeprojectaddon.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.MiningTargetData;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.MiningStarTier;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.PlanetType;

class MiningTargetPersistenceTest {
    @Test
    void unminedPreviewIsNeverRegistered() {
        DysonSphereProgressSavedData data = new DysonSphereProgressSavedData();
        MiningTargetData preview = MiningTargetData.create(12345L);

        assertSame(preview, data.persistMiningTarget("hub:0", preview));
        assertNull(data.getMiningTarget("hub:0"));
        assertEquals(0, data.miningTargetCount());
    }

    @Test
    void firstConsumedMassRegistersAndLocksTarget() {
        DysonSphereProgressSavedData data = new DysonSphereProgressSavedData();
        MiningTargetData target = MiningTargetData.create(12345L);
        target.consumeMass(new BigDecimal("0.001"));

        assertSame(target, data.persistMiningTarget("hub:0", target));
        assertSame(target, data.getMiningTarget("hub:0"));
        assertEquals(1, data.miningTargetCount());
    }

    @Test
    void completedTargetCanBeRemovedInsteadOfAccumulating() {
        DysonSphereProgressSavedData data = new DysonSphereProgressSavedData();
        MiningTargetData target = MiningTargetData.create(12345L);
        target.consumeMass(target.remainingMassKg());
        data.persistMiningTarget("hub:0", target);

        data.removeMiningTarget("hub:0");

        assertNull(data.getMiningTarget("hub:0"));
        assertEquals(0, data.miningTargetCount());
    }

    @Test
    void deterministicPreviewDoesNotNeedPersistence() {
        MiningTargetData first = MiningTargetData.create(99887766L);
        MiningTargetData second = MiningTargetData.create(99887766L);

        assertEquals(first.planetType(), second.planetType());
        assertEquals(first.initialMassKg(), second.initialMassKg());
        assertEquals(first.seed(), second.seed());
    }

    @Test
    void legacyHistoryIsPrunedToTheCurrentTarget() {
        DysonSphereProgressSavedData data = new DysonSphereProgressSavedData();
        MiningTargetData oldTarget = MiningTargetData.create(1L);
        oldTarget.consumeMass(BigDecimal.ONE);
        MiningTargetData currentTarget = MiningTargetData.create(2L);
        currentTarget.consumeMass(BigDecimal.ONE);
        data.persistMiningTarget("overworld:hub:0", oldTarget);
        data.persistMiningTarget("overworld:hub:1", currentTarget);

        data.removeMiningTargetsExcept("overworld:hub:", "overworld:hub:1");

        assertNull(data.getMiningTarget("overworld:hub:0"));
        assertSame(currentTarget, data.getMiningTarget("overworld:hub:1"));
        assertEquals(1, data.miningTargetCount());
    }

    @Test
    void subUnitTailMassCanBeDiscardedAndPersistsAsDepleted() {
        MiningTargetData target = new MiningTargetData(
                PlanetType.ROGUE, 12345L, new BigDecimal("1000"), new BigDecimal("0.3"), false);

        assertEquals(new BigDecimal("0.3"), target.discardRemainingMass());
        assertEquals(BigDecimal.ZERO, target.remainingMassKg());
        assertTrue(target.depleted());

        MiningTargetData restored = MiningTargetData.load(target.save());
        assertEquals(BigDecimal.ZERO, restored.remainingMassKg());
        assertTrue(restored.depleted());
    }

    @Test
    void completeUnitsAreConsumedBeforeTheTailIsDiscarded() {
        MiningTargetData target = new MiningTargetData(
                PlanetType.ROGUE, 12345L, new BigDecimal("4.3"), new BigDecimal("4.3"), false);

        assertEquals(new BigDecimal("4.0"), target.consumeMass(new BigDecimal("4.0")));
        assertEquals(new BigDecimal("0.3"), target.remainingMassKg());
        assertEquals(new BigDecimal("0.3"), target.discardRemainingMass());
        assertEquals(BigDecimal.ZERO, target.remainingMassKg());
        assertTrue(target.depleted());
    }

    @Test
    void miningTierSurvivesExactNbtRoundTrip() {
        MiningTargetData target = MiningTargetData.create(12345L, MiningStarTier.STAR_END);
        target.consumeMass(new BigDecimal("1E100"));

        MiningTargetData restored = MiningTargetData.load(target.save());

        assertEquals(MiningStarTier.STAR_END, restored.starTier());
        assertEquals(target.initialMassKg(), restored.initialMassKg());
        assertEquals(target.remainingMassKg(), restored.remainingMassKg());
    }

    @Test
    void legacyTargetWithoutTierMigratesToSinglePlanet() {
        MiningTargetData legacy = MiningTargetData.create(54321L);
        net.minecraft.nbt.CompoundTag tag = legacy.save();
        tag.remove("starTierLevel");

        MiningTargetData restored = MiningTargetData.load(tag);

        assertEquals(MiningStarTier.SINGLE_PLANET, restored.starTier());
    }

    @Test
    void hugeSubtractedMassUsesExactBinaryNbtInsteadOfOversizedUtf() {
        MiningTargetData target = new MiningTargetData(
                PlanetType.ROGUE, MiningStarTier.STAR_END, 12345L,
                new BigDecimal("3.4E100000"), new BigDecimal("3.4E100000"), false);
        target.consumeMass(BigDecimal.ONE);

        net.minecraft.nbt.CompoundTag tag = target.save();
        MiningTargetData restored = MiningTargetData.load(tag);

        assertTrue(tag.contains("initialMassKgExact", net.minecraft.nbt.Tag.TAG_COMPOUND));
        assertTrue(tag.contains("remainingMassKgExact", net.minecraft.nbt.Tag.TAG_COMPOUND));
        assertTrue(!tag.contains("initialMassKg"));
        assertTrue(!tag.contains("remainingMassKg"));
        assertEquals(target.initialMassKg(), restored.initialMassKg());
        assertEquals(target.remainingMassKg(), restored.remainingMassKg());
    }

    @Test
    void clientSyncOmitsExactMiningMassPayload() {
        DysonSphereProgressSavedData data = new DysonSphereProgressSavedData();
        MiningTargetData target = new MiningTargetData(
                PlanetType.ROGUE, MiningStarTier.STAR_END, 12345L,
                new BigDecimal("3.4E100000"), new BigDecimal("3.4E100000"), false);
        target.consumeMass(BigDecimal.ONE);
        data.persistMiningTarget("hub:0", target);

        net.minecraft.nbt.CompoundTag sync = data.saveForClientSync(
                new net.minecraft.nbt.CompoundTag(), null);

        assertTrue(!sync.contains("cosmicMiningTargets"));
    }
}

