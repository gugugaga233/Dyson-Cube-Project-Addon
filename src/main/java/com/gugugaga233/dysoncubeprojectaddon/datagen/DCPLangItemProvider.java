/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.data.DataGenerator
 *  net.minecraft.world.item.Item
 *  net.neoforged.neoforge.common.data.LanguageProvider
 *  org.apache.commons.lang3.text.WordUtils
 */
package com.gugugaga233.dysoncubeprojectaddon.datagen;

import com.gugugaga233.dysoncubeprojectaddon.DCPContent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.apache.commons.lang3.text.WordUtils;

public class DCPLangItemProvider
extends LanguageProvider {
    public DCPLangItemProvider(DataGenerator gen, String modid, String locale) {
        super(gen.getPackOutput(), modid, locale);
    }

    protected void addTranslations() {
        this.add("itemGroup.dyson_cube_project_addon", "Dyson Cube Project Addon");
        this.add(DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER.asItem(), "EM Rail Ejector Controller");
        this.add(DCPContent.Blocks.RAY_RECEIVER_CONTROLLER.asItem(), "Ray Receiver Controller");
        this.formatItem(DCPContent.Blocks.MULTIBLOCK_STRUCTURE.asItem());
        this.formatItem((net.minecraft.world.item.Item)DCPContent.Items.BEAM.get());
        this.formatItem((net.minecraft.world.item.Item)DCPContent.Items.BEAM_PACKAGE.get());
        this.formatItem((net.minecraft.world.item.Item)DCPContent.Items.SOLAR_SAIL.get());
        this.formatItem((net.minecraft.world.item.Item)DCPContent.Items.SOLAR_SAIL_PACKAGE.get());
        // V5.0 压缩物品翻译
        this.add(DCPContent.Items.COMPRESSED_SAIL.get(), "Compressed Solar Sail");
        this.add(DCPContent.Items.COMPRESSED_BEAM.get(), "Compressed Beam");
        // V5.2 矿物块压缩翻译
        this.add(DCPContent.Items.COMPRESSED_IRON_BLOCK.get(), "Compressed Iron Block");
        this.add(DCPContent.Items.COMPRESSED_GOLD_BLOCK.get(), "Compressed Gold Block");
        this.add(DCPContent.Items.COMPRESSED_NETHERITE_BLOCK.get(), "Compressed Netherite Block");
        this.add("gui.dysoncubeproject.dyson_information", "Dyson Information");
        this.add("gui.dysoncubeproject.progress", "Progress: %s%%");
        this.add("gui.dysoncubeproject.power_gen", "Power Gen: %s FE");
        this.add("gui.dysoncubeproject.power_con", "Power Con: %s FE");
        this.add("gui.dysoncubeproject.beams", "Beams: %s");
        this.add("gui.dysoncubeproject.sails", "Sails: %s/%s");
        this.add("gui.dysoncubeproject.needs_more_beams", "Needs more beams");
        this.add("gui.dysoncubeproject.subscribe", "Subscribe to this sphere");
        this.add("tooltip.dysoncubeproject.contains_solar_sails", "Contains %s solar sail(s)");
        this.add("tooltip.dysoncubeproject.contains_beams", "Contains %s beam(s)");
        this.add("tooltip.dysoncubeproject.power_optional", "*Power Optional, with power it allows to ramp up how many beams/sails are ejected*");
    }

    private void formatItem(Item item) {
        this.add(item, WordUtils.capitalize(BuiltInRegistries.ITEM.getKey(item).getPath().replace("_", " ")));
    }
}


