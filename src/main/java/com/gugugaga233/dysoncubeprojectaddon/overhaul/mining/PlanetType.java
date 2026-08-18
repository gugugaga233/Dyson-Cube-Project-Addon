package com.gugugaga233.dysoncubeprojectaddon.overhaul.mining;

import java.math.BigDecimal;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;

public enum PlanetType {
    SILICATE("silicate", Category.ROCKY, 500, 1500, 35),
    CARBON("carbon", Category.ROCKY, 300, 1200, 80),
    IRON_PLANET("iron_planet", Category.ROCKY, 400, 1800, 120),
    LAVA_PLANET("lava_planet", Category.ROCKY, 500, 2000, 90),
    DESERT_PLANET("desert_planet", Category.ROCKY, 300, 1300, 45),
    OCEAN_PLANET("ocean_planet", Category.ROCKY, 600, 2500, 40),
    ICE_PLANET("ice_planet", Category.ROCKY, 300, 1600, 55),
    SUPER_EARTH("super_earth", Category.ROCKY, 2000, 10000, 150),
    SUB_EARTH("sub_earth", Category.ROCKY, 100, 500, 25),
    HOT_JUPITER("hot_jupiter", Category.GAS_GIANT, 50000, 400000, 75),
    COLD_JUPITER("cold_jupiter", Category.GAS_GIANT, 50000, 500000, 65),
    SUPER_JUPITER("super_jupiter", Category.GAS_GIANT, 400000, 3000000, 180),
    ICE_GIANT("ice_giant", Category.ICE_GIANT, 10000, 70000, 70),
    HOT_NEPTUNE("hot_neptune", Category.ICE_GIANT, 10000, 90000, 85),
    MINI_NEPTUNE("mini_neptune", Category.ICE_GIANT, 3000, 20000, 50),
    SYMBIOTIC("symbiotic", Category.SPECIAL, 500, 5000, 220),
    BINARY_RING("binary_ring", Category.SPECIAL, 800, 8000, 140),
    ROGUE("rogue", Category.SPECIAL, 200, 3000, 30);

    private static final BigDecimal EARTH_MASS_KG = new BigDecimal("5.9722E24");

    private final String id;
    private final ResourceLocation texture;
    private final Category category;
    private final int minMilliEarthMass;
    private final int maxMilliEarthMass;
    private final int abundanceBasisPoints;

    PlanetType(String id, Category category, int minMilliEarthMass, int maxMilliEarthMass,
               int abundanceBasisPoints) {
        this.id = id;
        this.texture = ResourceLocation.fromNamespaceAndPath("dysoncubeproject",
                "textures/gui/planets/" + id + ".png");
        this.category = category;
        this.minMilliEarthMass = minMilliEarthMass;
        this.maxMilliEarthMass = maxMilliEarthMass;
        this.abundanceBasisPoints = abundanceBasisPoints;
    }

    public String id() {
        return id;
    }

    public ResourceLocation texture() {
        return texture;
    }

    public Category category() {
        return category;
    }

    public int abundanceBasisPoints() {
        return abundanceBasisPoints;
    }

    public BigDecimal rollMassKg(Random random) {
        int span = maxMilliEarthMass - minMilliEarthMass + 1;
        int milliEarthMass = minMilliEarthMass + random.nextInt(span);
        return EARTH_MASS_KG.multiply(BigDecimal.valueOf(milliEarthMass))
                .movePointLeft(3).stripTrailingZeros();
    }

    public static PlanetType byId(String id) {
        for (PlanetType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return SILICATE;
    }

    public enum Category {
        ROCKY,
        GAS_GIANT,
        ICE_GIANT,
        SPECIAL
    }
}

