package com.gugugaga233.dysoncubeprojectaddon.overhaul.block;

import com.gugugaga233.dysoncubeprojectaddon.Config;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.BlackHoleAmplifierBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
 * 视界放大器方块
 * <p>
 * 放置在黑洞光子球（r = 1.5 × r_s）位置，放大吸积盘功率 173 倍。
 * 损坏度 0~100，达 100 时停机。可用建造材料修复（每次 -5%）。
 */
public class BlackHoleAmplifierBlock extends Block implements EntityBlock {

    public static final MapCodec<BlackHoleAmplifierBlock> CODEC = simpleCodec(BlackHoleAmplifierBlock::new);

    public BlackHoleAmplifierBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlackHoleAmplifierBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof BlackHoleAmplifierBlockEntity amp) {
                amp.tick();
            }
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BlackHoleAmplifierBlockEntity amp) {
            // 修复：使用 Config.AMPLIFIER_REPAIR_ITEM 指定材料
            if (amp.canRepair(stack)) {
                amp.repair(player, stack);
                return ItemInteractionResult.CONSUME;
            }
            // 右击无物：显示信息
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(
                            "损坏度: " + String.format("%.1f", amp.getDamage()) + "%"
                    ), true);
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}

