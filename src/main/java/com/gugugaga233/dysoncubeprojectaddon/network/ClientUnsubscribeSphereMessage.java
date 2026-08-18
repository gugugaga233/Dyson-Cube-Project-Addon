package com.gugugaga233.dysoncubeprojectaddon.network;

import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereProgressSavedData;
import com.hrznstudio.titanium.network.Message;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Stops periodic Dyson snapshot delivery when the client closes its viewing screen. */
public class ClientUnsubscribeSphereMessage extends Message {
    public ClientUnsubscribeSphereMessage() {
    }

    @Override
    protected void handleMessage(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(player.level());
                if (data != null) data.unsubscribeFromClientSync(player.getStringUUID());
            }
        });
    }
}
