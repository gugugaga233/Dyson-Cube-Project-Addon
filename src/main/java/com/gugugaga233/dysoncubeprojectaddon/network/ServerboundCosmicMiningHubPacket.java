package com.gugugaga233.dysoncubeprojectaddon.network;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.CosmicMiningHubBlockEntity;
import com.hrznstudio.titanium.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerboundCosmicMiningHubPacket extends Message {
    public long blockPos;
    public String action;
    public String value;
    public int slot;

    public ServerboundCosmicMiningHubPacket() {
        this(0, "refresh", "", -1);
    }

    public ServerboundCosmicMiningHubPacket(long blockPos, String action, String value, int slot) {
        this.blockPos = blockPos;
        this.action = action;
        this.value = value;
        this.slot = slot;
    }

    @Override
    protected void handleMessage(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            BlockPos pos = BlockPos.of(blockPos);
            if (player.blockPosition().distSqr(pos) > 64.0
                    || !(player.level().getBlockEntity(pos) instanceof CosmicMiningHubBlockEntity hub)) {
                return;
            }
            try {
                switch (action) {
                    case "apply" -> hub.setLaserPower(value);
                    case "toggle" -> hub.toggleRunning();
                    case "next" -> {
                        if (!hub.selectNextTarget()) {
                            player.displayClientMessage(Component.translatable(
                                    "gui.dysoncubeproject.cosmic_mining.next_locked"), true);
                        }
                    }
                    case "tier" -> {
                        if (!hub.setStarTier(Integer.parseInt(value))) {
                            player.displayClientMessage(Component.translatable(
                                    "gui.dysoncubeproject.cosmic_mining.tier_locked"), true);
                        }
                    }
                    case "mass" -> {
                        if (!hub.setStarEndMass(value)) {
                            player.displayClientMessage(Component.translatable(
                                    "gui.dysoncubeproject.cosmic_mining.custom_mass_locked"), true);
                        }
                    }
                    case "extract" -> hub.extractSlot(player, slot);
                    case "refresh" -> { }
                    default -> { return; }
                }
            } catch (IllegalArgumentException exception) {
                player.displayClientMessage(Component.translatable(action.equals("mass")
                        ? "gui.dysoncubeproject.cosmic_mining.invalid_custom_mass"
                        : "gui.dysoncubeproject.cosmic_mining.invalid_power"), true);
            }
            hub.sendSnapshot(player);
        });
    }
}

