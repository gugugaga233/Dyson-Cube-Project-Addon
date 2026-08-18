package com.gugugaga233.dysoncubeprojectaddon.overhaul.mining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Random;
import org.junit.jupiter.api.Test;
import sonar.fluxnetworks.api.energy.BigNumber;

class MiningStarTierTest {
    private static final BigDecimal EARTH_MASS_KG = new BigDecimal("5.9722E24");

    @Test
    void definesAllSixteenIndependentMiningTiers() {
        assertEquals(16, MiningStarTier.values().length);
        for (int level = 1; level <= 16; level++) {
            assertEquals(level, MiningStarTier.byLevel(level).level());
        }
    }

    @Test
    void energyMultiplierAndCostUseBigNumber() {
        for (MiningStarTier tier : MiningStarTier.values()) {
            assertEquals(0, BigNumber.scientific("1", tier.level() - 1)
                    .compareTo(tier.energyMultiplier()));
            assertEquals(0, BigNumber.scientific("1", tier.level() + 2)
                    .compareTo(tier.energyCost()));
        }
    }

    @Test
    void generatedMassAlwaysStaysInsideTierRange() {
        for (MiningStarTier tier : MiningStarTier.values()) {
            for (int seed = 0; seed < 100; seed++) {
                BigDecimal earthMass = tier.rollMassKg(new Random(seed)).divide(EARTH_MASS_KG);
                assertTrue(earthMass.compareTo(tier.minimumEarthMass()) >= 0,
                        () -> tier + " generated below its minimum: " + earthMass);
                assertTrue(earthMass.compareTo(tier.maximumEarthMass()) <= 0,
                        () -> tier + " generated above its maximum: " + earthMass);
            }
        }
    }

    @Test
    void targetGenerationIsDeterministicForSeedAndTier() {
        MiningTargetData first = MiningTargetData.create(918273645L, MiningStarTier.STAR_END);
        MiningTargetData second = MiningTargetData.create(918273645L, MiningStarTier.STAR_END);

        assertEquals(first.planetType(), second.planetType());
        assertEquals(first.starTier(), second.starTier());
        assertEquals(first.initialMassKg(), second.initialMassKg());
        assertTrue(first.initialMassKg().precision() >= 5);
    }

    @Test
    void starEndCustomMassHasMinimumButNoMaximum() {
        assertEquals(new BigDecimal("1E+118"),
                MiningStarTier.STAR_END.parseCustomEarthMass("1E118"));
        assertEquals(new BigDecimal("2.5E+1000000"),
                MiningStarTier.STAR_END.parseCustomEarthMass("2.5E1000000"));
        assertThrows(IllegalArgumentException.class,
                () -> MiningStarTier.STAR_END.parseCustomEarthMass("9.999E117"));
        assertThrows(IllegalArgumentException.class,
                () -> MiningStarTier.STAR_BEGINNING.parseCustomEarthMass("1E120"));
    }

    @Test
    void customStarEndMassConvertsExactlyToKilograms() {
        BigDecimal earthMass = MiningStarTier.STAR_END.parseCustomEarthMass("2.5E250");
        MiningTargetData target = MiningTargetData.createWithEarthMass(
                918273645L, MiningStarTier.STAR_END, earthMass);

        assertEquals(0, earthMass.multiply(EARTH_MASS_KG).compareTo(target.initialMassKg()));
        assertEquals(target.initialMassKg(), target.remainingMassKg());
        assertEquals(target.initialMassKg(), MiningTargetData.load(target.save()).initialMassKg());
    }
}

