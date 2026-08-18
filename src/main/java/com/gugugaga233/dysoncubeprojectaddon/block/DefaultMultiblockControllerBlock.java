/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hrznstudio.titanium.block.RotatableBlock
 *  com.hrznstudio.titanium.block.tile.BasicTile
 *  com.hrznstudio.titanium.event.handler.EventManager
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.neoforge.event.level.BlockEvent$EntityPlaceEvent
 *  org.jetbrains.annotations.Nullable
 */
package com.gugugaga233.dysoncubeprojectaddon.block;

import com.gugugaga233.dysoncubeprojectaddon.multiblock.MultiblockStructure;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.block.tile.BasicTile;
import com.hrznstudio.titanium.event.handler.EventManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

public abstract class DefaultMultiblockControllerBlock<T extends BasicTile<T>>
extends RotatableBlock<T> {
    public DefaultMultiblockControllerBlock(String name, BlockBehaviour.Properties properties, Class<T> tileClass) {
        super(name, properties, tileClass);
    }

    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    public abstract MultiblockStructure getMultiblockStructure();

    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (this.getMultiblockStructure().validateSpace((LevelAccessor)context.getLevel(), context.getClickedPos())) {
            return this.defaultBlockState();
        }
        return null;
    }

    static {
        EventManager.forge(BlockEvent.EntityPlaceEvent.class, (EventPriority)EventPriority.HIGHEST).process(event -> {}).subscribe();
    }
}


