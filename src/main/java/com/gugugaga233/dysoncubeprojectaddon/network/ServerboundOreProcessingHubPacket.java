package com.gugugaga233.dysoncubeprojectaddon.network;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.OreProcessingHubBlockEntity;
import com.hrznstudio.titanium.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerboundOreProcessingHubPacket extends Message {
    private static final long serialVersionUID = 1L;

    public long blockPos;
    public String action;
    public int slot;

    public ServerboundOreProcessingHubPacket() {
        this(0, "refresh", -1);
    }

    public ServerboundOreProcessingHubPacket(long blockPos, String action, int slot) {
        this.blockPos = blockPos;
        this.action = action;
        this.slot = slot;
    }

    @Override
    protected void handleMessage(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            BlockPos pos = BlockPos.of(blockPos);
            if (player.blockPosition().distSqr(pos) > 64.0
                    || !(player.level().getBlockEntity(pos) instanceof OreProcessingHubBlockEntity hub)) {
                return;
            }
            if ("extract".equals(action)) {
                hub.extractSlot(player, slot);
            } else if (!"refresh".equals(action)) {
                return;
            }
            hub.sendSnapshot(player);
        });
    }
}

