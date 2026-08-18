/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hrznstudio.titanium.block.BasicTileBlock
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.entity.BlockEntityType$BlockEntitySupplier
 *  net.minecraft.world.level.block.state.BlockBehaviour
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.shapes.BooleanOp
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  org.jetbrains.annotations.Nullable
 */
package com.gugugaga233.dysoncubeprojectaddon.block;

import com.gugugaga233.dysoncubeprojectaddon.DCPContent;
import com.gugugaga233.dysoncubeprojectaddon.block.DefaultMultiblockControllerBlock;
import com.gugugaga233.dysoncubeprojectaddon.block.tile.MultiblockStructureBlockEntity;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MultiblockStructureBlock
extends BasicTileBlock<MultiblockStructureBlockEntity> {
    public MultiblockStructureBlock() {
        super(BlockBehaviour.Properties.ofFullCopy((BlockBehaviour)Blocks.IRON_BLOCK).noOcclusion().noLootTable(), MultiblockStructureBlockEntity.class);
    }

    public BlockEntityType.BlockEntitySupplier<?> getTileEntityFactory() {
        return (blockPos, blockState) -> new MultiblockStructureBlockEntity((BasicTileBlock<MultiblockStructureBlockEntity>)((BasicTileBlock)DCPContent.Blocks.MULTIBLOCK_STRUCTURE.block().get()), (BlockEntityType)DCPContent.Blocks.MULTIBLOCK_STRUCTURE.type().get(), blockPos, blockState);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context);
    }

    public static void createStructure(Level level, BlockPos controllerPos, BlockPos at) {
        level.setBlockAndUpdate(at, DCPContent.Blocks.MULTIBLOCK_STRUCTURE.getBlock().defaultBlockState());
        BlockEntity blockEntity = level.getBlockEntity(at);
        if (blockEntity instanceof MultiblockStructureBlockEntity) {
            MultiblockStructureBlockEntity blockEntity2 = (MultiblockStructureBlockEntity)blockEntity;
            blockEntity2.setControllerPos(controllerPos);
        }
    }

    public static VoxelShape getSplitShapeRelativeToController(BlockGetter level, BlockPos controllerPos, BlockPos currentPos, CollisionContext context) {
        double maxZ;
        double maxY;
        double maxX;
        int dz;
        double minZ;
        int dy;
        double minY;
        if (level == null || controllerPos == null || currentPos == null) {
            return Shapes.empty();
        }
        BlockState controllerState = level.getBlockState(controllerPos);
        Block controllerBlock = controllerState.getBlock();
        if (!(controllerBlock instanceof DefaultMultiblockControllerBlock)) {
            return Shapes.empty();
        }
        DefaultMultiblockControllerBlock controller = (DefaultMultiblockControllerBlock)controllerBlock;
        VoxelShape controllerShape = ((DefaultMultiblockControllerBlock)controllerBlock).getMultiblockStructure().getShape();
        if (controllerShape.isEmpty()) {
            return Shapes.empty();
        }
        int dx = currentPos.getX() - controllerPos.getX();
        double minX = (double)dx * 16.0;
        VoxelShape cutter = Block.box((double)minX, (double)(minY = (double)(dy = currentPos.getY() - controllerPos.getY()) * 16.0), (double)(minZ = (double)(dz = currentPos.getZ() - controllerPos.getZ()) * 16.0), (double)(maxX = minX + 16.0), (double)(maxY = minY + 16.0), (double)(maxZ = minZ + 16.0));
        VoxelShape piece = Shapes.join((VoxelShape)controllerShape, (VoxelShape)cutter, (BooleanOp)BooleanOp.AND);
        if (piece.isEmpty()) {
            return Shapes.empty();
        }
        return MultiblockStructureBlock.translateShape(piece, -minX, -minY, -minZ);
    }

    private static VoxelShape translateShape(VoxelShape shape, double offX, double offY, double offZ) {
        if (shape.isEmpty()) {
            return shape;
        }
        VoxelShape result = Shapes.empty();
        for (AABB bb : shape.toAabbs()) {
            VoxelShape moved = Block.box((double)(bb.minX * 16.0 + offX), (double)(bb.minY * 16.0 + offY), (double)(bb.minZ * 16.0 + offZ), (double)(bb.maxX * 16.0 + offX), (double)(bb.maxY * 16.0 + offY), (double)(bb.maxZ * 16.0 + offZ));
            result = Shapes.or((VoxelShape)result, (VoxelShape)moved);
        }
        return result;
    }

    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        MultiblockStructureBlockEntity blockEntity;
        BlockPos controllerPos;
        BlockEntity blockEntity2 = worldIn.getBlockEntity(pos);
        if (blockEntity2 instanceof MultiblockStructureBlockEntity && (controllerPos = (blockEntity = (MultiblockStructureBlockEntity)blockEntity2).getControllerPos()) != null && worldIn.getBlockState(controllerPos).getBlock() instanceof DefaultMultiblockControllerBlock) {
            worldIn.destroyBlock(controllerPos, true);
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        MultiblockStructureBlockEntity be;
        BlockPos controllerPos;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MultiblockStructureBlockEntity && (controllerPos = (be = (MultiblockStructureBlockEntity)blockEntity).getControllerPos()) != null) {
            VoxelShape cached = be.getCachedShape();
            if (cached != null && !cached.isEmpty()) {
                return cached;
            }
            VoxelShape computed = MultiblockStructureBlock.getSplitShapeRelativeToController(level, controllerPos, pos, context);
            be.setCachedShape(computed);
            return computed;
        }
        return Shapes.empty();
    }

    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getShape(state, level, pos, context);
    }

    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        MultiblockStructureBlockEntity be;
        BlockPos controllerPos;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MultiblockStructureBlockEntity && (controllerPos = (be = (MultiblockStructureBlockEntity)blockEntity).getControllerPos()) != null) {
            return new ItemStack((ItemLike)level.getBlockState(controllerPos).getBlock());
        }
        return ItemStack.EMPTY;
    }
}


