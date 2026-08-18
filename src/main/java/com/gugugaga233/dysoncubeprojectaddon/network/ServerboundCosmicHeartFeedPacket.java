package com.gugugaga233.dysoncubeprojectaddon.network;

import com.hrznstudio.titanium.network.Message;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Retained for network compatibility; Cosmic Hearts only accept launcher structure materials. */
public class ServerboundCosmicHeartFeedPacket extends Message {
    public ServerboundCosmicHeartFeedPacket() {
    }

    @Override
    protected void handleMessage(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            player.displayClientMessage(Component.translatable(
                    "message.dysoncubeproject.cosmic_heart.launcher_only"), true);
        });
    }
}

