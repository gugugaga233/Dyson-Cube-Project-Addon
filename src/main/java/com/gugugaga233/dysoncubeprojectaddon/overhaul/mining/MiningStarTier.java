package com.gugugaga233.dysoncubeprojectaddon.overhaul.mining;

import java.math.BigDecimal;
import java.util.Random;
import sonar.fluxnetworks.api.energy.BigNumber;

/** Independent mining scale used by the Cosmic Mining Hub. */
public enum MiningStarTier {
    SINGLE_PLANET(1, "single_planet", "0.1", "1"),
    ASTEROID_GROUP(2, "asteroid_group", "1", "1E2"),
    PLANETARY_SYSTEM(3, "planetary_system", "1E2", "1E5"),
    STAR_REGION(4, "star_region", "1E5", "1E8"),
    STAR_FIELD(5, "star_field", "1E8", "1E13"),
    STAR_FRONTIER(6, "star_frontier", "1E13", "1E19"),
    STAR_REALM(7, "star_realm", "1E19", "1E26"),
    STAR_POLE(8, "star_pole", "1E26", "1E34"),
    STAR_ABYSS(9, "star_abyss", "1E34", "1E43"),
    STAR_RIVER(10, "star_river", "1E43", "1E53"),
    STAR_VAST(11, "star_vast", "1E53", "1E64"),
    STAR_UNIVERSE(12, "star_universe", "1E64", "1E76"),
    STAR_COSMOS(13, "star_cosmos", "1E76", "1E89"),
    STAR_ORIGIN(14, "star_origin", "1E89", "1E103"),
    STAR_BEGINNING(15, "star_beginning", "1E103", "1E118"),
    STAR_END(16, "star_end", "1E118", "1E134");

    private static final BigDecimal EARTH_MASS_KG = new BigDecimal("5.9722E24");
    private static final int SIGNIFICAND_DIGITS = 9;
    private static final int MAX_CUSTOM_MASS_INPUT_LENGTH = 128;

    private final int level;
    private final String id;
    private final BigDecimal minimumEarthMass;
    private final BigDecimal maximumEarthMass;

    MiningStarTier(int level, String id, String minimumEarthMass, String maximumEarthMass) {
        this.level = level;
        this.id = id;
        this.minimumEarthMass = new BigDecimal(minimumEarthMass);
        this.maximumEarthMass = new BigDecimal(maximumEarthMass);
    }

    public int level() {
        return level;
    }

    public String id() {
        return id;
    }

    public BigDecimal minimumEarthMass() {
        return minimumEarthMass;
    }

    public BigDecimal maximumEarthMass() {
        return maximumEarthMass;
    }

    public BigNumber energyMultiplier() {
        return BigNumber.scientific("1", level - 1);
    }

    public BigNumber energyCost() {
        return BigNumber.scientific("1", level + 2);
    }

    public BigDecimal parseCustomEarthMass(String input) {
        if (this != STAR_END || input == null) {
            throw new IllegalArgumentException("Custom mass is only available for Star End");
        }
        String normalized = input.trim();
        if (normalized.isEmpty() || normalized.length() > MAX_CUSTOM_MASS_INPUT_LENGTH) {
            throw new IllegalArgumentException("Invalid custom mass length");
        }
        BigDecimal mass;
        try {
            mass = new BigDecimal(normalized);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid custom mass", exception);
        }
        if (mass.compareTo(minimumEarthMass) < 0) {
            throw new IllegalArgumentException("Custom mass below Star End minimum");
        }
        return mass.stripTrailingZeros();
    }

    /**
     * Samples by decimal magnitude so huge tiers are not statistically pinned to their upper bound.
     * The resulting finite significand is still constructed and stored as an exact BigDecimal.
     */
    public BigDecimal rollMassKg(Random random) {
        BigDecimal earthMass;
        if (level == 1) {
            int tenths = 1 + random.nextInt(10);
            earthMass = BigDecimal.valueOf(tenths, 1);
        } else {
            int minimumExponent = decimalExponent(minimumEarthMass);
            int maximumExponent = decimalExponent(maximumEarthMass);
            int exponentSpan = Math.max(1, maximumExponent - minimumExponent);
            int exponent = minimumExponent + random.nextInt(exponentSpan);
            int significand = 100_000_000 + random.nextInt(900_000_000);
            earthMass = BigDecimal.valueOf(significand)
                    .scaleByPowerOfTen(exponent - SIGNIFICAND_DIGITS + 1);
            if (earthMass.compareTo(minimumEarthMass) < 0) earthMass = minimumEarthMass;
            if (earthMass.compareTo(maximumEarthMass) > 0) earthMass = maximumEarthMass;
        }
        return earthMass.multiply(EARTH_MASS_KG).stripTrailingZeros();
    }

    private static int decimalExponent(BigDecimal value) {
        BigDecimal normalized = value.stripTrailingZeros();
        return normalized.precision() - normalized.scale() - 1;
    }

    public static MiningStarTier byLevel(int level) {
        MiningStarTier[] tiers = values();
        return tiers[Math.max(1, Math.min(tiers.length, level)) - 1];
    }
}

