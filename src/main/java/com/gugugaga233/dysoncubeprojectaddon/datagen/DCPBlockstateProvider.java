/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.data.DataGenerator
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.Block
 *  net.neoforged.neoforge.client.model.generators.BlockStateProvider
 *  net.neoforged.neoforge.client.model.generators.ModelFile
 *  net.neoforged.neoforge.client.model.generators.ModelFile$UncheckedModelFile
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 */
package com.gugugaga233.dysoncubeprojectaddon.datagen;

import com.gugugaga233.dysoncubeprojectaddon.DCPContent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class DCPBlockstateProvider
extends BlockStateProvider {
    public DCPBlockstateProvider(DataGenerator gen, String modid, ExistingFileHelper exFileHelper) {
        super(gen.getPackOutput(), modid, exFileHelper);
    }

    protected void registerStatesAndModels() {
        this.simpleBlock((Block)DCPContent.Blocks.MULTIBLOCK_STRUCTURE.getBlock(), (ModelFile)DCPBlockstateProvider.getUncheckedModel((Block)DCPContent.Blocks.MULTIBLOCK_STRUCTURE.getBlock()));
        this.simpleBlock((Block)DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER.getBlock(), (ModelFile)DCPBlockstateProvider.getUncheckedModel((Block)DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER.getBlock()));
        this.simpleBlock((Block)DCPContent.Blocks.RAY_RECEIVER_CONTROLLER.getBlock(), (ModelFile)DCPBlockstateProvider.getUncheckedModel((Block)DCPContent.Blocks.RAY_RECEIVER_CONTROLLER.getBlock()));
    }

    public static ModelFile.UncheckedModelFile getUncheckedModel(Block block) {
        return new ModelFile.UncheckedModelFile(DCPBlockstateProvider.getModel(block));
    }

    public static ResourceLocation getModel(Block block) {
        return ResourceLocation.fromNamespaceAndPath(BuiltInRegistries.BLOCK.getKey(block).getNamespace(), "block/" + BuiltInRegistries.BLOCK.getKey(block).getPath());
    }
}


