/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hrznstudio.titanium.module.BlockWithTile
 *  com.hrznstudio.titanium.module.DeferredRegistryHelper
 *  com.hrznstudio.titanium.tab.TitaniumTab
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.item.Item
 *  net.neoforged.neoforge.registries.DeferredHolder
 */
package com.gugugaga233.dysoncubeprojectaddon;

import com.gugugaga233.dysoncubeprojectaddon.item.CompressedItem;
import com.hrznstudio.titanium.module.BlockWithTile;
import com.hrznstudio.titanium.module.DeferredRegistryHelper;
import com.hrznstudio.titanium.tab.TitaniumTab;
import java.math.BigInteger;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DCPContent {
    // New content keeps the historical namespace so worlds made with the overhaul remain compatible.
    // Every original entry below is a reference, not a second registration.
    public static DeferredRegistryHelper REGISTRY = new DeferredRegistryHelper("dysoncubeproject");
    public static TitaniumTab TAB = new TitaniumTab(ResourceLocation.fromNamespaceAndPath(
            DysonCubeProject.MODID, "main"));
    public static int CYAN_COLOR = -8329498;

    public static class Sounds {
        public static DeferredHolder<SoundEvent, SoundEvent> RAILGUN =
                com.buuz135.dysoncubeproject.DCPContent.Sounds.RAILGUN;
        public static DeferredHolder<SoundEvent, SoundEvent> RAY =
                com.buuz135.dysoncubeproject.DCPContent.Sounds.RAY;

        public static void init() {
        }
    }

    public static class Items {
        public static DeferredHolder<Item, Item> SOLAR_SAIL =
                com.buuz135.dysoncubeproject.DCPContent.Items.SOLAR_SAIL;
        public static DeferredHolder<Item, Item> SOLAR_SAIL_PACKAGE =
                com.buuz135.dysoncubeproject.DCPContent.Items.SOLAR_SAIL_PACKAGE;
        public static DeferredHolder<Item, Item> BEAM =
                com.buuz135.dysoncubeproject.DCPContent.Items.BEAM;
        public static DeferredHolder<Item, Item> BEAM_PACKAGE =
                com.buuz135.dysoncubeproject.DCPContent.Items.BEAM_PACKAGE;

        // ====== V5.0 压缩物品（5个实例 + DataComponent 等级）=====
        public static DeferredHolder<Item, Item> COMPRESSED_SAIL = REGISTRY.registerGeneric(Registries.ITEM, "compressed_sail",
                () -> new CompressedItem(
                        new Item.Properties()
                                .component(DCPAttachments.COMPRESSION_LEVEL.get(), BigInteger.ONE)
                                .component(DCPAttachments.SOLAR_SAIL.get(),
                                        CompressedItem.COMPRESSED_SAIL_BASE_COUNT),
                        "0.036"));
        public static DeferredHolder<Item, Item> COMPRESSED_BEAM = REGISTRY.registerGeneric(Registries.ITEM, "compressed_beam",
                () -> new CompressedItem(
                        new Item.Properties()
                                .component(DCPAttachments.COMPRESSION_LEVEL.get(), BigInteger.ONE)
                                .component(DCPAttachments.BEAM.get(),
                                        CompressedItem.COMPRESSED_BEAM_BASE_COUNT),
                        "0.036"));

        // ====== V5.2 矿物块压缩物品（铁/金/下界合金）=====
        // 基础质量（kg/个）：一组(64个)=1kg → 单个=1/64≈0.015625
        public static DeferredHolder<Item, Item> COMPRESSED_IRON_BLOCK = REGISTRY.registerGeneric(Registries.ITEM, "compressed_iron_block",
                () -> new com.gugugaga233.dysoncubeprojectaddon.item.CompressedItem(
                        new Item.Properties().component(DCPAttachments.COMPRESSION_LEVEL.get(), BigInteger.ONE), "0.015625"));
        public static DeferredHolder<Item, Item> COMPRESSED_GOLD_BLOCK = REGISTRY.registerGeneric(Registries.ITEM, "compressed_gold_block",
                () -> new com.gugugaga233.dysoncubeprojectaddon.item.CompressedItem(
                        new Item.Properties().component(DCPAttachments.COMPRESSION_LEVEL.get(), BigInteger.ONE), "0.0275"));
        public static DeferredHolder<Item, Item> COMPRESSED_NETHERITE_BLOCK = REGISTRY.registerGeneric(Registries.ITEM, "compressed_netherite_block",
                () -> new com.gugugaga233.dysoncubeprojectaddon.item.CompressedItem(
                        new Item.Properties().component(DCPAttachments.COMPRESSION_LEVEL.get(), BigInteger.ONE), "0.0390625"));

        public static void init() {
            // 5 个压缩物品已在注册时指定初始等级（Lv.1），无需循环注册
        }
    }

    public static class Blocks {
        public static BlockWithTile MULTIBLOCK_STRUCTURE =
                com.buuz135.dysoncubeproject.DCPContent.Blocks.MULTIBLOCK_STRUCTURE;
        public static BlockWithTile EM_RAILEJECTOR_CONTROLLER =
                com.buuz135.dysoncubeproject.DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER;
        public static BlockWithTile RAY_RECEIVER_CONTROLLER =
                com.buuz135.dysoncubeproject.DCPContent.Blocks.RAY_RECEIVER_CONTROLLER;

        public static void init() {
        }
    }
}

