package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;

/**
 * 视觉反馈通知中心
 * <p>
 * 管理宇宙觉醒、反物质觉醒、包裹完成、湮灭完成等事件通知。
 */
public final class UniverseEvents {

    /**
     * 包裹完成通知（普通星体或反物质星体包裹完成时触发）
     */
    public static void notifyWrapComplete(MinecraftServer server, StarData newStar, long totalWrapped) {
        notifyWrapComplete(server, newStar, absolute(totalWrapped), absolute(1L));
    }

    /** Emits one batch completion message while retaining crossed milestone notifications. */
    public static void notifyWrapComplete(MinecraftServer server, StarData newStar,
                                          long totalWrapped, int completedWraps) {
        notifyWrapComplete(server, newStar, absolute(totalWrapped), absolute(completedWraps));
    }

    public static void notifyWrapComplete(MinecraftServer server, StarData newStar,
                                          AbsoluteInteger totalWrapped,
                                          AbsoluteInteger completedWraps) {
        if (server == null || totalWrapped == null || completedWraps == null) return;

        AbsoluteInteger previousWrapped = totalWrapped.copy();
        previousWrapped = completedWraps.compareTo(previousWrapped) >= 0
                ? new AbsoluteInteger() : FluxMath8.subtract(previousWrapped, completedWraps);
        if (crossed(previousWrapped, totalWrapped, UniverseRandomizer.BLACKHOLE_UNLOCK_THRESHOLD)) {
            Component awakeMsg = Component.translatable("message.dysoncubeproject.universe_awakened")
                    .withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE);
            server.getPlayerList().broadcastSystemMessage(awakeMsg, false);
        }

        if (crossed(previousWrapped, totalWrapped, UniverseRandomizer.ANTIMATTER_UNLOCK_THRESHOLD)) {
            Component antiMsg = Component.translatable("message.dysoncubeproject.antimatter_awakened")
                    .withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE);
            server.getPlayerList().broadcastSystemMessage(antiMsg, false);
        }
    }

    /**
     * 反物质星体湮灭完成通知
     */
    public static void notifyAntimatterAnnihilated(MinecraftServer server, StarData star, double totalOutput) {
        Component msg = Component.translatable("message.dysoncubeproject.antimatter_annihilated",
                        formatBigDouble(totalOutput)).withStyle(ChatFormatting.GOLD);
        server.getPlayerList().broadcastSystemMessage(msg, false);
    }

    /**
     * 反物质星体遭遇通知
     */
    public static void notifyAntimatterEncountered(MinecraftServer server, StarData star) {
        Component msg = Component.translatable("message.dysoncubeproject.antimatter_encountered")
                .withStyle(ChatFormatting.LIGHT_PURPLE);
        server.getPlayerList().broadcastSystemMessage(msg, false);
    }

    /** 寰宇之心浮现通知 */
    public static void notifyCosmicHeartAppeared() {
        // 全服广播 — 无 Server 上下文，通过 DysonCubeProject 广播
    }

    public static void notifyCosmicHeartAppeared(MinecraftServer server) {
        if (server == null) return;
        server.getPlayerList().broadcastSystemMessage(
                Component.translatable("message.dysoncubeproject.cosmic_heart_batch")
                        .withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD), false);
    }

    /**
     * 寰宇之心覆盖完成通知（V5.3）
     * 当覆盖进度达到 100% 时，向所有玩家广播消息并发送规则选择界面。
     */
    public static void notifyCosmicHeartComplete(net.minecraft.server.level.ServerPlayer player) {
        Component msg = Component.translatable("message.dysoncubeproject.cosmic_heart_complete")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
        player.sendSystemMessage(msg);
    }

    private static String formatBigDouble(double value) {
        if (value < 1000) return String.format("%.1f", value);
        String[] units = {"", "K", "M", "B", "T", "Q", "Qi", "Sx", "Sp", "O", "N", "Dc"};
        int exp = (int) (Math.log(value) / Math.log(1000));
        if (exp >= units.length) return "1.23e+" + (int)(Math.log10(value));
        return String.format("%.2f", value / Math.pow(1000, exp)) + units[exp];
    }

    /** 鸿蒙之气触发通知 */
    public static void notifyPrimordialQiObtained(net.minecraft.server.level.ServerPlayer player, int count) {
        notifyPrimordialQiObtained(player, absolute(count), absolute(1L));
    }

    public static void notifyPrimordialQiObtained(net.minecraft.server.level.ServerPlayer player,
                                                  AbsoluteInteger count,
                                                  AbsoluteInteger batchHits) {
        player.sendSystemMessage(Component.translatable("message.dysoncubeproject.qi_batch",
                        NumberUtils.getScientificInteger(batchHits),
                        NumberUtils.getScientificInteger(count))
                .withStyle(ChatFormatting.GOLD));
    }

    /** 鸿蒙之气觉醒通知（每10次） */
    public static void notifyPrimordialQiAwakening(net.minecraft.server.level.ServerPlayer player, int count) {
        notifyPrimordialQiAwakening(player, absolute(count), absolute(1L));
    }

    public static void notifyPrimordialQiAwakening(net.minecraft.server.level.ServerPlayer player,
                                                   AbsoluteInteger count,
                                                   AbsoluteInteger batchHits) {
        player.sendSystemMessage(Component.translatable("message.dysoncubeproject.qi_batch_awaken",
                        NumberUtils.getScientificInteger(batchHits),
                        NumberUtils.getScientificInteger(count))
                .withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
    }

    private static boolean crossed(AbsoluteInteger previous, AbsoluteInteger current, long threshold) {
        AbsoluteInteger boundary = absolute(threshold);
        return previous.compareTo(boundary) < 0 && current.compareTo(boundary) >= 0;
    }

    private static AbsoluteInteger absolute(long value) {
        return AbsoluteInteger.parse(Long.toString(Math.max(0L, value)));
    }
}

