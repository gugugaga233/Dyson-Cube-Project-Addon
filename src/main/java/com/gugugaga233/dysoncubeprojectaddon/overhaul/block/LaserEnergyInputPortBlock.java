package com.gugugaga233.dysoncubeprojectaddon.overhaul.block;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.LaserEnergyInputPortBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class LaserEnergyInputPortBlock extends Block implements EntityBlock {
    public static final MapCodec<LaserEnergyInputPortBlock> CODEC = simpleCodec(LaserEnergyInputPortBlock::new);

    public LaserEnergyInputPortBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LaserEnergyInputPortBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                                BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof LaserEnergyInputPortBlockEntity port) {
            player.displayClientMessage(Component.translatable(
                    "message.dysoncubeproject.laser_energy_input_port.energy", port.getEnergyDisplay()), true);
        }
        return InteractionResult.SUCCESS;
    }
}

