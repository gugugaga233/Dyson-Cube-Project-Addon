package com.gugugaga233.dysoncubeprojectaddon.overhaul.block;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.MiningItemOutputPortBlockEntity;
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

public class MiningItemOutputPortBlock extends Block implements EntityBlock {
    public static final MapCodec<MiningItemOutputPortBlock> CODEC = simpleCodec(MiningItemOutputPortBlock::new);

    public MiningItemOutputPortBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MiningItemOutputPortBlockEntity(pos, state);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MiningItemOutputPortBlockEntity port) {
            port.refreshExternalStorage();
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                                BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MiningItemOutputPortBlockEntity port) {
            player.displayClientMessage(Component.translatable(
                    "message.dysoncubeproject.mining_item_output_port.status",
                    Component.translatable(port.isConnected()
                            ? "message.dysoncubeproject.port.connected"
                            : "message.dysoncubeproject.port.disconnected"),
                    port.getStoredItemCount()), true);
        }
        return InteractionResult.SUCCESS;
    }
}

