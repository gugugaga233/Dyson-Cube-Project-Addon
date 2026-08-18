package com.gugugaga233.dysoncubeprojectaddon.overhaul.mining;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public final class PlanetaryResource {
    private static final String C = "c";

    private PlanetaryResource() {
    }

    public static Output roll(PlanetType type, Random random) {
        List<WeightedOutput> pool = pool(type);
        int total = pool.stream().mapToInt(WeightedOutput::weight).sum();
        int selected = random.nextInt(Math.max(1, total));
        for (WeightedOutput entry : pool) {
            selected -= entry.weight();
            if (selected < 0) {
                return entry.output().resolve(random);
            }
        }
        return item(C, "stone", Items.STONE).resolve(random);
    }

    public static ResourcePreview preview(PlanetType type) {
        Set<ResourceLocation> items = new LinkedHashSet<>();
        Set<ResourceLocation> fluids = new LinkedHashSet<>();
        for (WeightedOutput entry : pool(type)) {
            for (Output output : entry.output().candidates()) {
                if (!output.item().isEmpty()) {
                    items.add(BuiltInRegistries.ITEM.getKey(output.item().getItem()));
                }
                if (!output.fluid().isEmpty()) {
                    fluids.add(BuiltInRegistries.FLUID.getKey(output.fluid().getFluid()));
                }
            }
        }
        return new ResourcePreview(List.copyOf(items), List.copyOf(fluids));
    }

    private static List<WeightedOutput> pool(PlanetType type) {
        return switch (type) {
            case SILICATE -> list(
                    w(40, item(C, "stone", Items.STONE)),
                    w(20, item(C, "cobblestone", Items.COBBLESTONE)),
                    w(10, item(C, "granite", Items.GRANITE)),
                    w(10, item(C, "diorite", Items.DIORITE)),
                    w(10, item(C, "andesite", Items.ANDESITE)),
                    w(5, item(C, "dirt", Items.DIRT)),
                    w(5, item(C, "sand", Items.SAND)));
            case CARBON -> list(
                    w(2, ore("diamond", Items.DIAMOND_ORE)),
                    w(8, ore("lapis", Items.LAPIS_ORE)),
                    w(60, item(C, "stone", Items.STONE)),
                    w(30, item(C, "deepslate", Items.DEEPSLATE)));
            case IRON_PLANET -> list(
                    w(50, ore("iron", Items.IRON_ORE)),
                    w(20, ore("gold", Items.GOLD_ORE)),
                    w(30, item(C, "stone", Items.STONE)));
            case LAVA_PLANET -> list(
                    w(30, fluid(Fluids.LAVA)),
                    w(20, item(C, "obsidian", Items.OBSIDIAN)),
                    w(20, item(C, "basalt", Items.BASALT)),
                    w(10, item(C, "tuff", Items.TUFF)),
                    w(10, ore("redstone", Items.REDSTONE_ORE)),
                    w(10, item(C, "netherrack", Items.NETHERRACK)));
            case DESERT_PLANET -> list(
                    w(40, item(C, "sand", Items.SAND)),
                    w(20, item(C, "red_sand", Items.RED_SAND)),
                    w(15, item(C, "gravel", Items.GRAVEL)),
                    w(15, item(C, "sandstone", Items.SANDSTONE)),
                    w(10, ore("redstone", Items.REDSTONE_ORE)));
            case OCEAN_PLANET -> list(
                    w(50, fluid(Fluids.WATER)),
                    w(15, item(C, "clay", Items.CLAY)),
                    w(15, item(C, "sand", Items.SAND)),
                    w(10, item(C, "dirt", Items.DIRT)),
                    w(10, item(C, "stone", Items.STONE)));
            case ICE_PLANET -> list(
                    w(30, direct(Items.ICE)), w(15, direct(Items.PACKED_ICE)),
                    w(5, direct(Items.BLUE_ICE)), w(20, direct(Items.SNOW_BLOCK)),
                    w(20, item(C, "stone", Items.STONE)), w(10, item(C, "dirt", Items.DIRT)));
            case SUPER_EARTH -> list(
                    w(10, ore("iron", Items.IRON_ORE)), w(5, ore("gold", Items.GOLD_ORE)),
                    w(2, ore("diamond", Items.DIAMOND_ORE)), w(5, ore("redstone", Items.REDSTONE_ORE)),
                    w(5, ore("lapis", Items.LAPIS_ORE)), w(40, item(C, "stone", Items.STONE)),
                    w(20, item(C, "deepslate", Items.DEEPSLATE)), w(10, item(C, "dirt", Items.DIRT)),
                    w(3, item(C, "cobblestone", Items.COBBLESTONE)));
            case SUB_EARTH -> list(
                    w(50, item(C, "stone", Items.STONE)), w(20, item(C, "cobblestone", Items.COBBLESTONE)),
                    w(20, item(C, "dirt", Items.DIRT)), w(10, ore("iron", Items.IRON_ORE)));
            case HOT_JUPITER -> giantPool(30, 20, 10, 40);
            case COLD_JUPITER -> giantPool(30, 20, 0, 50);
            case SUPER_JUPITER -> giantPool(20, 12, 8, 60);
            case ICE_GIANT -> list(
                    w(35, fluid(Fluids.WATER)), w(20, direct(Items.ICE)), w(25, item(C, "stone", Items.STONE)),
                    w(2, ore("diamond", Items.DIAMOND_ORE)), w(8, ore("lapis", Items.LAPIS_ORE)),
                    w(10, ore("iron", Items.IRON_ORE)), w(10, optionalGas()));
            case HOT_NEPTUNE -> list(
                    w(40, fluid(Fluids.WATER)), w(25, ore("iron", Items.IRON_ORE)),
                    w(15, ore("lapis", Items.LAPIS_ORE)), w(20, item(C, "sand", Items.SAND)),
                    w(5, optionalGas()));
            case MINI_NEPTUNE -> list(
                    w(35, fluid(Fluids.WATER)), w(20, ore("iron", Items.IRON_ORE)),
                    w(10, ore("lapis", Items.LAPIS_ORE)), w(25, item(C, "stone", Items.STONE)),
                    w(10, item(C, "dirt", Items.DIRT)), w(5, optionalGas()));
            case SYMBIOTIC -> list(
                    w(5, ore("iron", Items.IRON_ORE)), w(5, ore("gold", Items.GOLD_ORE)),
                    w(5, ore("copper", Items.COPPER_ORE)), w(5, ore("redstone", Items.REDSTONE_ORE)),
                    w(5, ore("lapis", Items.LAPIS_ORE)), w(5, ore("diamond", Items.DIAMOND_ORE)),
                    w(5, ore("emerald", Items.EMERALD_ORE)), w(5, ore("coal", Items.COAL_ORE)),
                    w(30, item(C, "stone", Items.STONE)), w(20, item(C, "deepslate", Items.DEEPSLATE)),
                    w(10, item(C, "cobblestone", Items.COBBLESTONE)), w(10, item(C, "dirt", Items.DIRT)));
            case BINARY_RING -> list(
                    w(30, ore("iron", Items.IRON_ORE)), w(20, ore("gold", Items.GOLD_ORE)),
                    w(5, ore("diamond", Items.DIAMOND_ORE)), w(45, item(C, "stone", Items.STONE)));
            case ROGUE -> roguePool(type);
        };
    }

    private static List<WeightedOutput> giantPool(int iron, int gold, int redstone, int gas) {
        List<WeightedOutput> pool = new ArrayList<>();
        pool.add(w(iron, ore("iron", Items.IRON_ORE)));
        pool.add(w(gold, ore("gold", Items.GOLD_ORE)));
        if (redstone > 0) pool.add(w(redstone, ore("redstone", Items.REDSTONE_ORE)));
        pool.add(w(gas, optionalGas()));
        return pool;
    }

    private static List<WeightedOutput> roguePool(PlanetType ignored) {
        return list(w(1, item(C, "stone", Items.STONE)), w(1, item(C, "deepslate", Items.DEEPSLATE)),
                w(1, ore("iron", Items.IRON_ORE)), w(1, ore("gold", Items.GOLD_ORE)),
                w(1, ore("coal", Items.COAL_ORE)), w(1, direct(Items.ICE)));
    }

    private static OutputTemplate optionalGas() {
        return template(random -> {
            List<Fluid> fluids = optionalGasFluids();
            return fluids.isEmpty()
                    ? item(C, "stone", Items.STONE).resolve(random)
                    : new Output(ItemStack.EMPTY, new FluidStack(fluids.get(random.nextInt(fluids.size())), 1),
                    MassKind.FLUID);
        }, () -> {
            List<Fluid> fluids = optionalGasFluids();
            if (fluids.isEmpty()) return item(C, "stone", Items.STONE).candidates();
            return fluids.stream().map(fluid -> new Output(ItemStack.EMPTY, new FluidStack(fluid, 1),
                    MassKind.FLUID)).toList();
        });
    }

    private static OutputTemplate item(String namespace, String path, Item fallback) {
        return template(random -> {
            List<Item> matches = matchingItems(namespace, path, fallback);
            return new Output(new ItemStack(matches.get(random.nextInt(matches.size()))),
                    FluidStack.EMPTY, massKind(fallback));
        }, () -> matchingItems(namespace, path, fallback).stream()
                .map(item -> new Output(new ItemStack(item), FluidStack.EMPTY, massKind(fallback)))
                .toList());
    }

    private static OutputTemplate ore(String name, Item fallback) {
        return item(C, "ores/" + name, fallback);
    }

    private static OutputTemplate direct(Item item) {
        return template(random -> new Output(new ItemStack(item), FluidStack.EMPTY, massKind(item)),
                () -> List.of(new Output(new ItemStack(item), FluidStack.EMPTY, massKind(item))));
    }

    private static OutputTemplate fluid(Fluid fluid) {
        return template(random -> new Output(ItemStack.EMPTY, new FluidStack(fluid, 1), MassKind.FLUID),
                () -> List.of(new Output(ItemStack.EMPTY, new FluidStack(fluid, 1), MassKind.FLUID)));
    }

    private static OutputTemplate template(Function<Random, Output> resolver, Supplier<List<Output>> candidates) {
        return new OutputTemplate() {
            @Override
            public Output resolve(Random random) {
                return resolver.apply(random);
            }

            @Override
            public List<Output> candidates() {
                return candidates.get();
            }
        };
    }

    private static List<Item> matchingItems(String namespace, String path, Item fallback) {
        TagKey<Item> tag = TagKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(namespace, path));
        Optional<? extends Iterable<Holder<Item>>> holders = BuiltInRegistries.ITEM.getTag(tag).map(named -> named);
        if (holders.isPresent()) {
            List<Item> matches = new ArrayList<>();
            holders.get().forEach(holder -> matches.add(holder.value()));
            if (!matches.isEmpty()) return matches;
        }
        return List.of(fallback);
    }

    private static List<Fluid> optionalGasFluids() {
        String[] ids = {
                "mekanism:hydrogen", "mekanism:oxygen", "mekanism:chlorine",
                "gtceu:hydrogen", "gtceu:helium", "gtceu:oxygen", "gtceu:nitrogen", "gtceu:methane"
        };
        List<Fluid> fluids = new ArrayList<>();
        for (String id : ids) {
            ResourceLocation location = ResourceLocation.tryParse(id);
            if (location != null) {
                Fluid candidate = BuiltInRegistries.FLUID.get(location);
                if (candidate != Fluids.EMPTY) fluids.add(candidate);
            }
        }
        return fluids;
    }

    private static MassKind massKind(Item item) {
        if (item == Items.DIRT || item == Items.SAND || item == Items.RED_SAND || item == Items.GRAVEL
                || item == Items.CLAY) return MassKind.SOIL;
        if (item == Items.ICE || item == Items.PACKED_ICE || item == Items.BLUE_ICE || item == Items.SNOW_BLOCK)
            return MassKind.ICE;
        return MassKind.ROCK;
    }

    @SafeVarargs
    private static List<WeightedOutput> list(WeightedOutput... entries) {
        return List.of(entries);
    }

    private static WeightedOutput w(int weight, OutputTemplate output) {
        return new WeightedOutput(weight, output);
    }

    private record WeightedOutput(int weight, OutputTemplate output) {
    }

    private interface OutputTemplate {
        Output resolve(Random random);

        List<Output> candidates();
    }

    public record ResourcePreview(List<ResourceLocation> items, List<ResourceLocation> fluids) {
    }

    public record Output(ItemStack item, FluidStack fluid, MassKind massKind) {
        public boolean isFluid() {
            return !fluid.isEmpty();
        }
    }

    public enum MassKind {
        ROCK("2.0"), SOIL("1.5"), ICE("0.9"), FLUID("0.001");

        private final java.math.BigDecimal kgPerUnit;

        MassKind(String kgPerUnit) {
            this.kgPerUnit = new java.math.BigDecimal(kgPerUnit);
        }

        public java.math.BigDecimal kgPerUnit() {
            return kgPerUnit;
        }
    }
}

