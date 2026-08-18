/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hrznstudio.titanium.datagenerator.loot.TitaniumLootTableProvider
 *  com.hrznstudio.titanium.datagenerator.loot.block.BasicBlockLootTables
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.DataGenerator
 *  net.minecraft.world.level.block.Block
 */
package com.gugugaga233.dysoncubeprojectaddon.datagen;

import com.hrznstudio.titanium.datagenerator.loot.TitaniumLootTableProvider;
import com.hrznstudio.titanium.datagenerator.loot.block.BasicBlockLootTables;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Block;

public class DCPLootTableDataProvider
extends TitaniumLootTableProvider {
    private final Supplier<List<Block>> blocksToProcess;

    public DCPLootTableDataProvider(DataGenerator dataGenerator, Supplier<List<Block>> blocks, CompletableFuture<HolderLookup.Provider> providerCompletableFuture) {
        super(dataGenerator, blocks, providerCompletableFuture);
        this.blocksToProcess = blocks;
    }

    protected BasicBlockLootTables createBlockLootTables(HolderLookup.Provider prov) {
        return new BasicBlockLootTables(this.blocksToProcess, prov){

            protected void generate() {
                super.generate();
            }
        };
    }
}


