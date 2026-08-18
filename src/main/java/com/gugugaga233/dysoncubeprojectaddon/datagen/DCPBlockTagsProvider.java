/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.PackOutput
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.world.level.block.Block
 *  net.neoforged.neoforge.common.data.BlockTagsProvider
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 *  org.jetbrains.annotations.Nullable
 */
package com.gugugaga233.dysoncubeprojectaddon.datagen;

import com.gugugaga233.dysoncubeprojectaddon.DCPContent;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class DCPBlockTagsProvider
extends BlockTagsProvider {
    public DCPBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    protected void addTags(HolderLookup.Provider provider) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER.getBlock(),
                DCPContent.Blocks.RAY_RECEIVER_CONTROLLER.getBlock(),
                DCPContent.Blocks.MULTIBLOCK_STRUCTURE.getBlock(),
                // 魔改方块
                com.gugugaga233.dysoncubeprojectaddon.overhaul.OverhaulContent.getDysonHubBlock(),
                com.gugugaga233.dysoncubeprojectaddon.overhaul.OverhaulContent.getBlackHoleAmplifierBlock()
        );
    }
}


