package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import com.gugugaga233.dysoncubeprojectaddon.network.ClientboundPrimordialQiChoicePacket;
import com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.server.level.ServerPlayer;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;

/**
 * 鸿蒙之气管理器
 * <p>
 * 鸿蒙之气是超越物理法则的玄之又玄之力，极难获得。
 * <ul>
 *   <li>触发条件：包裹"星寰"层级后（totalWrapped ≥ 5,000,000,000），每次包裹时极低概率触发</li>
 *   <li>概率：0.0000005%（5e-9），约每 2 亿次包裹触发一次</li>
 *   <li>效果：根源重构、法则浸润、造化感应、道痕烙印</li>
 * </ul>
 */
public final class PrimordialQiManager {

    /** 触发概率：5e-9（约每 2 亿次包裹触发一次） */
    public static final double PROBABILITY = 5e-9;
    private static final long EXPECTED_ATTEMPTS_PER_HIT = 200_000_000L;
    private static final long CHOICE_REMINDER_TICKS = 1_200L;
    private static final ConcurrentMap<UUID, Long> LAST_CHOICE_PROMPT = new ConcurrentHashMap<>();

    private PrimordialQiManager() {}

    /**
     * 获取鸿蒙之气解锁阈值（从配置读取）。
     */
    public static int getUnlockThreshold() {
        return com.gugugaga233.dysoncubeprojectaddon.Config.PRIMORDIAL_QI_UNLOCK_THRESHOLD;
    }

    /**
     * 尝试触发鸿蒙之气。
     * <p>
     * 在每次包裹完成后调用，若命中概率则应用效果。
     *
     * @param player         触发玩家（用于通知）
     * @param sphere         完成包裹的戴森球结构
     * @param totalWrapped   当前的 totalWrapped（本次包裹后）
     * @return 是否触发成功
     */
    public static boolean tryTrigger(ServerPlayer player, com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereStructure sphere, long totalWrapped) {
        return tryTrigger(player, sphere, totalWrapped, 1);
    }

    /** Preserves the per-star trigger probability when wraps are processed in a batch. */
    public static boolean tryTrigger(ServerPlayer player,
                                     com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereStructure sphere,
                                     long totalWrapped,
                                     int completedWraps) {
        return tryTrigger(player, sphere, absolute(totalWrapped), absolute(completedWraps));
    }

    /** Exact-count batch variant used by compressed structure processing. */
    public static boolean tryTrigger(ServerPlayer player,
                                     com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereStructure sphere,
                                     AbsoluteInteger totalWrapped,
                                     AbsoluteInteger completedWraps) {
        AbsoluteInteger eligibleAttempts = eligibleAttempts(
                totalWrapped, completedWraps, getUnlockThreshold());
        if (eligibleAttempts.isZero()) return false;

        FluxMath8.Division sampled = FluxMath8.divideAndRemainder(
                eligibleAttempts, EXPECTED_ATTEMPTS_PER_HIT);
        AbsoluteInteger triggerCount = sampled.quotient();
        if (sampled.remainder() > 0L
                && Math.random() < (double) sampled.remainder() / EXPECTED_ATTEMPTS_PER_HIT) {
            triggerCount.increment();
        }
        if (triggerCount.isZero()) return false;

        sphere.applyPrimordialQiEffects(triggerCount);
        AbsoluteInteger count = sphere.getPrimordialQiCountExact();

        // A large aggregate may cross several milestones; one screen represents this settlement.
        if (sphere.markPrimordialQiChoicePending()) {
            sendChoicePrompt(player, count);
        }

        return true;
    }

    private static AbsoluteInteger eligibleAttempts(AbsoluteInteger total,
                                                     AbsoluteInteger completed,
                                                     long threshold) {
        AbsoluteInteger thresholdValue = absolute(threshold);
        if (total == null || completed == null || completed.isZero()
                || total.compareTo(thresholdValue) < 0) {
            return new AbsoluteInteger();
        }

        AbsoluteInteger boundedCompleted = completed.compareTo(total) > 0
                ? total.copy()
                : completed.copy();
        AbsoluteInteger firstCompleted = total.copy();
        firstCompleted = FluxMath8.subtract(firstCompleted, boundedCompleted);
        firstCompleted.increment();
        if (firstCompleted.compareTo(thresholdValue) < 0) firstCompleted = thresholdValue;

        AbsoluteInteger eligible = total.copy();
        eligible = FluxMath8.subtract(eligible, firstCompleted);
        eligible.increment();
        return eligible;
    }

    private static AbsoluteInteger absolute(long value) {
        return AbsoluteInteger.parse(Long.toString(Math.max(0L, value)));
    }

    public static void remindPendingChoice(ServerPlayer player,
                                           com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereStructure sphere) {
        if (player == null || sphere == null || !sphere.hasPrimordialQiChoicePending()) return;
        long gameTime = player.level().getGameTime();
        long lastPrompt = LAST_CHOICE_PROMPT.getOrDefault(player.getUUID(), Long.MIN_VALUE / 2L);
        if (gameTime - lastPrompt >= CHOICE_REMINDER_TICKS) {
            sendChoicePrompt(player, sphere.getPrimordialQiCountExact());
        }
    }

    public static void clearChoicePrompt(ServerPlayer player) {
        if (player != null) LAST_CHOICE_PROMPT.remove(player.getUUID());
    }

    private static void sendChoicePrompt(ServerPlayer player, AbsoluteInteger count) {
        LAST_CHOICE_PROMPT.put(player.getUUID(), player.level().getGameTime());
        com.gugugaga233.dysoncubeprojectaddon.DysonCubeProject.NETWORK.sendTo(
                new ClientboundPrimordialQiChoicePacket(NumberUtils.getScientificInteger(count)), player);
    }
}

