/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hrznstudio.titanium.block.BasicTileBlock
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.NonNullList
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.entity.BlockEntityType$BlockEntitySupplier
 *  net.minecraft.world.level.block.state.BlockBehaviour
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.shapes.BooleanOp
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.gugugaga233.dysoncubeprojectaddon.block;

import com.gugugaga233.dysoncubeprojectaddon.DCPContent;
import com.gugugaga233.dysoncubeprojectaddon.block.DefaultMultiblockControllerBlock;
import com.gugugaga233.dysoncubeprojectaddon.block.MultiblockStructureBlock;
import com.gugugaga233.dysoncubeprojectaddon.block.tile.EMRailEjectorBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.multiblock.MultiblockStructure;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereProgressSavedData;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereStructure;
import com.hrznstudio.titanium.block.BasicTileBlock;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EMRailEjectorControllerBlock
extends DefaultMultiblockControllerBlock<EMRailEjectorBlockEntity> {
    public static VoxelShape SHAPE = Stream.of(Block.box((double)-9.0, (double)0.0, (double)-9.0, (double)25.0, (double)6.0, (double)25.0), Block.box((double)-2.0, (double)6.0, (double)-2.0, (double)18.0, (double)12.0, (double)18.0), Block.box((double)2.0, (double)12.0, (double)2.0, (double)14.0, (double)32.0, (double)14.0)).reduce((v1, v2) -> Shapes.join((VoxelShape)v1, (VoxelShape)v2, (BooleanOp)BooleanOp.OR)).get();
    private static final VoxelShape CONTROLLER_LOCAL_SHAPE = Shapes.join((VoxelShape)SHAPE, (VoxelShape)Block.box((double)0.0, (double)0.0, (double)0.0, (double)16.0, (double)16.0, (double)16.0), (BooleanOp)BooleanOp.AND);
    public static MultiblockStructure MULTIBLOCK_STRUCTURE = new MultiblockStructure(3, 3, 3, SHAPE);

    public EMRailEjectorControllerBlock() {
        super("em_railejector_controller", BlockBehaviour.Properties.ofFullCopy((BlockBehaviour)Blocks.IRON_BLOCK), EMRailEjectorBlockEntity.class);
    }

    @Override
    public MultiblockStructure getMultiblockStructure() {
        return MULTIBLOCK_STRUCTURE;
    }

    public BlockEntityType.BlockEntitySupplier<?> getTileEntityFactory() {
        return (blockPos, blockState) -> new EMRailEjectorBlockEntity((BasicTileBlock<EMRailEjectorBlockEntity>)((BasicTileBlock)DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER.getBlock()), (BlockEntityType)DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER.type().get(), blockPos, blockState);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (placer != null) {
                DysonSphereProgressSavedData dyson = DysonSphereProgressSavedData.get(level);
                String subscribedSphere = dyson.getSubscribedFor(placer.getStringUUID());
                dyson.getSpheres().computeIfAbsent(subscribedSphere, s -> new DysonSphereStructure());
                dyson.setDirty();
                BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
                if (blockEntity instanceof EMRailEjectorBlockEntity) {
                    EMRailEjectorBlockEntity blockEntity2 = (EMRailEjectorBlockEntity)blockEntity;
                    blockEntity2.setDysonSphereId(subscribedSphere);
                }
                dyson.setDirty();
            }
            BlockPos lowerCorner = pos.offset(-1, 0, -1);
            for (int x = 0; x < 3; ++x) {
                for (int z = 0; z < 3; ++z) {
                    BlockPos updatedAt = lowerCorner.offset(x, 0, z);
                    if (pos.equals((Object)updatedAt)) continue;
                    MultiblockStructureBlock.createStructure(level, pos, updatedAt);
                }
            }
            MultiblockStructureBlock.createStructure(level, pos, pos.above());
            MultiblockStructureBlock.createStructure(level, pos, pos.above(2));
        }
    }

    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if (level instanceof ServerLevel) {
            BlockPos lowerCorner = pos.offset(-1, 0, -1);
            for (int x = 0; x < 3; ++x) {
                for (int z = 0; z < 3; ++z) {
                    BlockPos updatedAt = lowerCorner.offset(x, 0, z);
                    if (pos.equals((Object)updatedAt)) continue;
                    level.setBlockAndUpdate(updatedAt, Blocks.AIR.defaultBlockState());
                }
            }
            level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(pos.above(2), Blocks.AIR.defaultBlockState());
        }
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CONTROLLER_LOCAL_SHAPE;
    }

    @NotNull
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        return CONTROLLER_LOCAL_SHAPE;
    }

    public NonNullList<ItemStack> getDynamicDrops(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        EMRailEjectorBlockEntity emRailEjectorBlock;
        NonNullList stacks = NonNullList.create();
        BlockEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof EMRailEjectorBlockEntity && (emRailEjectorBlock = (EMRailEjectorBlockEntity)tileentity).getInput() != null) {
            for (int i = 0; i < emRailEjectorBlock.getInput().getSlots(); ++i) {
                stacks.add((Object)emRailEjectorBlock.getInput().getStackInSlot(i));
            }
        }
        return stacks;
    }
}


