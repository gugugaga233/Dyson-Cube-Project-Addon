/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hrznstudio.titanium.annotation.Save
 *  com.hrznstudio.titanium.block.BasicTileBlock
 *  com.hrznstudio.titanium.block.tile.BasicTile
 *  com.hrznstudio.titanium.block.tile.ITickableBlockEntity
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.BlockDestructionProgress
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.ItemInteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package com.gugugaga233.dysoncubeprojectaddon.block.tile;

import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.block.tile.BasicTile;
import com.hrznstudio.titanium.block.tile.ITickableBlockEntity;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class MultiblockStructureBlockEntity
extends BasicTile<MultiblockStructureBlockEntity>
implements ITickableBlockEntity<MultiblockStructureBlockEntity> {
    @Save
    private BlockPos controllerPos;
    private VoxelShape cachedShape;

    public MultiblockStructureBlockEntity(BasicTileBlock<MultiblockStructureBlockEntity> base, BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType, pos, state);
    }

    public BlockPos getControllerPos() {
        return this.controllerPos;
    }

    @OnlyIn(value=Dist.CLIENT)
    public void clientTick(Level level, BlockPos pos, BlockState state, MultiblockStructureBlockEntity blockEntity) {
        if (this.controllerPos != null) {
            Int2ObjectMap destroyingBlocks = Minecraft.getInstance().levelRenderer.destroyingBlocks;
            for (Integer i : destroyingBlocks.keySet()) {
                BlockDestructionProgress progress = (BlockDestructionProgress)destroyingBlocks.get((Object)i);
                if (!progress.getPos().equals((Object)pos)) continue;
                Minecraft.getInstance().levelRenderer.destroyBlockProgress(i.intValue(), this.controllerPos, progress.getProgress());
            }
        }
    }

    public ItemInteractionResult onActivated(Player player, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ) {
        BlockEntity blockEntity;
        if (this.controllerPos != null && (blockEntity = this.level.getBlockEntity(this.controllerPos)) instanceof BasicTile) {
            BasicTile controller = (BasicTile)blockEntity;
            return controller.onActivated(player, hand, facing, hitX, hitY, hitZ);
        }
        return super.onActivated(player, hand, facing, hitX, hitY, hitZ);
    }

    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos = controllerPos;
        this.cachedShape = null;
        this.markForUpdate();
    }

    public VoxelShape getCachedShape() {
        return this.cachedShape;
    }

    public void setCachedShape(VoxelShape cachedShape) {
        this.cachedShape = cachedShape;
    }
}


