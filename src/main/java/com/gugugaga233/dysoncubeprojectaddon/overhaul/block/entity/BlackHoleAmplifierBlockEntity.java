package com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity;

import com.gugugaga233.dysoncubeprojectaddon.Config;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.OverhaulContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * 视界放大器方块实体
 * <p>
 * 损坏度 0~100，低损时快速损耗（0~50: +0.1/tick），高损时慢速损耗（50~100: +0.005/tick）。
 * 达 100 时停机，输出为 0。
 * 修复：右击消耗指定材料，每次 -5% 损坏度。
 */
public class BlackHoleAmplifierBlockEntity extends BlockEntity {

    /** 损坏度（0~100，float） */
    private float amplifierDamage = 0;
    /** 上次损耗 tick 时间 */
    private long lastDamageTick = 0;

    public BlackHoleAmplifierBlockEntity(BlockPos pos, BlockState state) {
        super(OverhaulContent.getBlackHoleAmplifierBEType(), pos, state);
    }

    /**
     * 每 tick 更新损坏度。
     * - 损坏度 0~50：每 tick +Config.AMPLIFIER_DAMAGE_RATE_LOW
     * - 损坏度 50~100：每 tick +Config.AMPLIFIER_DAMAGE_RATE_HIGH
     * - 损坏度达 100：停机
     */
    public void tick() {
        if (level == null || level.isClientSide) return;
        long gameTime = level.getGameTime();
        // 每 20 tick（1秒）更新一次
        if (gameTime - lastDamageTick < 20) return;
        lastDamageTick = gameTime;

        if (amplifierDamage >= 100) return; // 已停机

        double rate = amplifierDamage < 50
                ? Config.AMPLIFIER_DAMAGE_RATE_LOW
                : Config.AMPLIFIER_DAMAGE_RATE_HIGH;

        this.amplifierDamage = Math.min(100, amplifierDamage + (float) rate);
        if (this.amplifierDamage >= 100) {
            // 停机：通知附近玩家
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 16);
        }
        setChanged();
    }

    /** 判断是否可修复 */
    public boolean canRepair(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (amplifierDamage >= 100) return false;
        if (amplifierDamage <= 0) return false;
        // 严格校验：必须是 Config.AMPLIFIER_REPAIR_ITEM 指定的物品
        String repairItemId = Config.AMPLIFIER_REPAIR_ITEM;
        String stackId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return repairItemId.equals(stackId);
    }

    /**
     * 修复：消耗 1 个物品，减少 Config.AMPLIFIER_REPAIR_AMOUNT% 损坏度。
     */
    public void repair(Player player, ItemStack stack) {
        if (!canRepair(stack)) return;
        this.amplifierDamage = Math.max(0, amplifierDamage - (float) Config.AMPLIFIER_REPAIR_AMOUNT);
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        player.displayClientMessage(
                Component.translatable("message.dysoncubeproject.black_hole_amplifier.repaired",
                        String.format("%.1f%%", amplifierDamage)),
                true);
        setChanged();
    }

    public float getDamage() { return amplifierDamage; }
    public boolean isStopped() { return amplifierDamage >= 100; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putFloat("amplifierDamage", amplifierDamage);
        tag.putLong("lastDamageTick", lastDamageTick);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.amplifierDamage = tag.getFloat("amplifierDamage");
        this.lastDamageTick = tag.getLong("lastDamageTick");
    }
}

