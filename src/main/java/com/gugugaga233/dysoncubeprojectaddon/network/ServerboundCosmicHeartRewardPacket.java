package com.gugugaga233.dysoncubeprojectaddon.network;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.CosmicHeart;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.CosmicHeartRewardCount;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8;
import com.gugugaga233.dysoncubeprojectaddon.DysonCubeProject;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereProgressSavedData;
import com.hrznstudio.titanium.network.Message;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;

/**
 * 客户端 → 服务端：寰宇之心规则选择
 *
 * The comma-separated plan allocates current and future completed-heart rewards.
 */
public class ServerboundCosmicHeartRewardPacket extends Message {
    public String rewardPlan;

    public ServerboundCosmicHeartRewardPacket() {
        this.rewardPlan = "";
    }

    public ServerboundCosmicHeartRewardPacket(String rewardPlan) {
        this.rewardPlan = rewardPlan;
    }

    @Override
    protected void handleMessage(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(player.level());
            if (data == null) {
                DysonCubeProject.endCosmicHeartSelection(player);
                return;
            }

            CosmicHeart heart = data.getCosmicHeart();
            if (!heart.isComplete()) {
                DysonCubeProject.endCosmicHeartSelection(player);
                return;
            }

            AbsoluteInteger[] counts = parsePlan(
                    rewardPlan, CosmicHeart.RuleModification.values().length);
            if (counts == null || !heart.setRewardPlan(counts)) {
                DysonCubeProject.endCosmicHeartSelection(player);
                player.displayClientMessage(
                        Component.translatable("message.dysoncubeproject.cosmic_heart.reward_invalid"),
                        true);
                return;
            }

            String required = heart.getBatchCountDisplay();
            AbsoluteInteger[] claimed = heart.claimPlannedRewardBatch();
            AbsoluteInteger claimedCount = new AbsoluteInteger();
            for (AbsoluteInteger count : claimed) FluxMath8.addInPlace(claimedCount, count);
            if (claimedCount.isZero()) {
                DysonCubeProject.endCosmicHeartSelection(player);
                player.displayClientMessage(
                        Component.translatable("message.dysoncubeproject.cosmic_heart.reward_invalid"),
                        true);
                return;
            }
            data.applyCosmicHeartRewardBatch(claimed);
            DysonCubeProject.endCosmicHeartSelection(player);
            player.displayClientMessage(Component.translatable(
                    "message.dysoncubeproject.cosmic_heart.reward_batch_saved",
                    required, heart.getPlannedRewardCountDisplay()), true);
            data.setDirty();
        });
    }

    private static AbsoluteInteger[] parsePlan(String serialized, int size) {
        if (serialized == null) return null;
        String[] parts = serialized.split(";", -1);
        if (parts.length != size) return null;
        AbsoluteInteger[] result = new AbsoluteInteger[size];
        try {
            for (int i = 0; i < size; i++) result[i] = parseCount(parts[i]);
        } catch (IllegalArgumentException exception) {
            return null;
        }
        return result;
    }

    private static AbsoluteInteger parseCount(String input) {
        return CosmicHeartRewardCount.parseAbsoluteInteger(input);
    }
}

