package com.gugugaga233.dysoncubeprojectaddon.overhaul.mining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;

class PlanetaryResourceTest {
    @Test
    void silicatePreviewContainsStoneAndNoFluids() {
        PlanetaryResource.ResourcePreview preview = PlanetaryResource.preview(PlanetType.SILICATE);

        assertTrue(preview.items().contains(BuiltInRegistries.ITEM.getKey(Items.STONE)));
        assertTrue(preview.fluids().isEmpty());
    }

    @Test
    void fluidPlanetsExposeTheirFluidResources() {
        PlanetaryResource.ResourcePreview lava = PlanetaryResource.preview(PlanetType.LAVA_PLANET);
        PlanetaryResource.ResourcePreview ocean = PlanetaryResource.preview(PlanetType.OCEAN_PLANET);

        assertTrue(lava.fluids().contains(BuiltInRegistries.FLUID.getKey(Fluids.LAVA)));
        assertTrue(ocean.fluids().contains(BuiltInRegistries.FLUID.getKey(Fluids.WATER)));
    }

    @Test
    void previewsAreDeduplicatedAndNotLimitedToUiPageSize() {
        for (PlanetType type : PlanetType.values()) {
            PlanetaryResource.ResourcePreview preview = PlanetaryResource.preview(type);
            assertEquals(preview.items().size(), new HashSet<>(preview.items()).size(),
                    () -> type + " contains duplicate item candidates");
            assertEquals(preview.fluids().size(), new HashSet<>(preview.fluids()).size(),
                    () -> type + " contains duplicate fluid candidates");
        }

        assertTrue(PlanetaryResource.preview(PlanetType.SYMBIOTIC).items().size() > 10,
                "The server preview must retain entries beyond the UI's default ten-item page");
    }
}

