package com.gugugaga233.dysoncubeprojectaddon.overhaul.mining;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Random;
import net.minecraft.nbt.CompoundTag;

public final class MiningTargetData {
    private static final BigDecimal EARTH_MASS_KG = new BigDecimal("5.9722E24");

    private final PlanetType planetType;
    private final MiningStarTier starTier;
    private final long seed;
    private final BigDecimal initialMassKg;
    private BigDecimal remainingMassKg;
    private boolean depleted;

    public MiningTargetData(PlanetType planetType, long seed, BigDecimal initialMassKg,
                            BigDecimal remainingMassKg, boolean depleted) {
        this(planetType, MiningStarTier.SINGLE_PLANET, seed, initialMassKg, remainingMassKg, depleted);
    }

    public MiningTargetData(PlanetType planetType, MiningStarTier starTier, long seed,
                            BigDecimal initialMassKg, BigDecimal remainingMassKg, boolean depleted) {
        this.planetType = planetType;
        this.starTier = starTier == null ? MiningStarTier.SINGLE_PLANET : starTier;
        this.seed = seed;
        this.initialMassKg = initialMassKg.max(BigDecimal.ZERO);
        this.remainingMassKg = remainingMassKg.max(BigDecimal.ZERO);
        this.depleted = depleted || this.remainingMassKg.signum() == 0;
    }

    public static MiningTargetData create(long seed) {
        return create(seed, MiningStarTier.SINGLE_PLANET);
    }

    public static MiningTargetData create(long seed, MiningStarTier starTier) {
        Random random = new Random(seed);
        PlanetType[] types = PlanetType.values();
        PlanetType type = types[random.nextInt(types.length)];
        MiningStarTier selectedTier = starTier == null ? MiningStarTier.SINGLE_PLANET : starTier;
        BigDecimal mass = selectedTier.rollMassKg(random);
        return new MiningTargetData(type, selectedTier, seed, mass, mass, false);
    }

    public static MiningTargetData createWithEarthMass(long seed, MiningStarTier starTier,
                                                        BigDecimal earthMass) {
        MiningStarTier selectedTier = starTier == null ? MiningStarTier.SINGLE_PLANET : starTier;
        if (selectedTier != MiningStarTier.STAR_END || earthMass == null) {
            throw new IllegalArgumentException("Custom mass is only available for Star End");
        }
        Random random = new Random(seed);
        PlanetType[] types = PlanetType.values();
        PlanetType type = types[random.nextInt(types.length)];
        BigDecimal mass = earthMass.multiply(EARTH_MASS_KG).stripTrailingZeros();
        return new MiningTargetData(type, selectedTier, seed, mass, mass, false);
    }

    public PlanetType planetType() {
        return planetType;
    }

    public MiningStarTier starTier() {
        return starTier;
    }

    public long seed() {
        return seed;
    }

    public BigDecimal initialMassKg() {
        return initialMassKg;
    }

    public BigDecimal remainingMassKg() {
        return remainingMassKg;
    }

    public boolean depleted() {
        return depleted;
    }

    public boolean hasMiningStarted() {
        return depleted || remainingMassKg.compareTo(initialMassKg) < 0;
    }

    public BigDecimal consumeMass(BigDecimal requestedKg) {
        if (depleted || requestedKg == null || requestedKg.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal consumed = requestedKg.min(remainingMassKg);
        remainingMassKg = remainingMassKg.subtract(consumed);
        if (remainingMassKg.signum() == 0) {
            remainingMassKg = BigDecimal.ZERO;
            depleted = true;
        }
        return consumed;
    }

    public BigDecimal discardRemainingMass() {
        if (depleted) {
            return BigDecimal.ZERO;
        }
        BigDecimal discarded = remainingMassKg;
        remainingMassKg = BigDecimal.ZERO;
        depleted = true;
        return discarded;
    }

    public String percentRemaining() {
        if (initialMassKg.signum() == 0) {
            return "0.0000";
        }
        return remainingMassKg.multiply(BigDecimal.valueOf(100))
                .divide(initialMassKg, 4, RoundingMode.HALF_UP).toPlainString();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("planetType", planetType.id());
        tag.putInt("starTierLevel", starTier.level());
        tag.putLong("seed", seed);
        tag.put("initialMassKgExact", saveDecimal(initialMassKg));
        tag.put("remainingMassKgExact", saveDecimal(remainingMassKg));
        tag.putBoolean("depleted", depleted);
        return tag;
    }

    public static MiningTargetData load(CompoundTag tag) {
        try {
            BigDecimal initial = loadDecimal(tag, "initialMassKgExact", "initialMassKg");
            BigDecimal remaining = loadDecimal(tag, "remainingMassKgExact", "remainingMassKg");
            MiningStarTier tier = tag.contains("starTierLevel")
                    ? MiningStarTier.byLevel(tag.getInt("starTierLevel"))
                    : MiningStarTier.SINGLE_PLANET;
            return new MiningTargetData(PlanetType.byId(tag.getString("planetType")), tier,
                    tag.getLong("seed"), initial, remaining, tag.getBoolean("depleted"));
        } catch (NumberFormatException ignored) {
            return create(tag.getLong("seed"));
        }
    }

    private static CompoundTag saveDecimal(BigDecimal value) {
        CompoundTag tag = new CompoundTag();
        tag.putByteArray("unscaled", value.unscaledValue().toByteArray());
        tag.putInt("scale", value.scale());
        return tag;
    }

    private static BigDecimal loadDecimal(CompoundTag root, String exactKey, String legacyKey) {
        if (root.contains(exactKey, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            CompoundTag tag = root.getCompound(exactKey);
            byte[] unscaled = tag.getByteArray("unscaled");
            if (unscaled.length == 0) return BigDecimal.ZERO;
            return new BigDecimal(new BigInteger(unscaled), tag.getInt("scale"));
        }
        return new BigDecimal(root.getString(legacyKey));
    }
}

