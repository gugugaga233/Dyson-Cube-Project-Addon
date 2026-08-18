package com.gugugaga233.dysoncubeprojectaddon.overhaul.block;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.DysonHubBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.OverhaulContent;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 戴森寰宇中枢方块
 */
public class DysonHubBlock extends Block implements EntityBlock {

    public static final MapCodec<DysonHubBlock> CODEC = simpleCodec(DysonHubBlock::new);

    public DysonHubBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DysonHubBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof DysonHubBlockEntity hub) {
                hub.tick();
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer sp) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof DysonHubBlockEntity hub) {
                hub.openGui(sp);
            }
        }
        return InteractionResult.SUCCESS;
    }
}

