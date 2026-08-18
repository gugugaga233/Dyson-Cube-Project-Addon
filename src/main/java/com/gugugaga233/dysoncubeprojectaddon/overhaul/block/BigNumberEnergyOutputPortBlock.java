package com.gugugaga233.dysoncubeprojectaddon.overhaul.block;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.BigNumberEnergyOutputPortBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

public class BigNumberEnergyOutputPortBlock extends Block implements EntityBlock {
    public static final MapCodec<BigNumberEnergyOutputPortBlock> CODEC =
            simpleCodec(BigNumberEnergyOutputPortBlock::new);

    public BigNumberEnergyOutputPortBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BigNumberEnergyOutputPortBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                   BlockEntityType<T> type) {
        return level.isClientSide ? null : (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof BigNumberEnergyOutputPortBlockEntity port) port.tick();
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                                BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BigNumberEnergyOutputPortBlockEntity port) {
            player.displayClientMessage(Component.translatable(
                    "message.dysoncubeproject.bignumber_energy_output_port.energy",
                    port.getEnergyDisplay(), port.isHubConnected(), port.isBigNumberNetworkConnected()), true);
        }
        return InteractionResult.SUCCESS;
    }
}

