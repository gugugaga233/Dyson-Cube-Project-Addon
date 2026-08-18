/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hrznstudio.titanium.network.Message
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.network.handling.IPayloadContext
 */
package com.gugugaga233.dysoncubeprojectaddon.network;

import com.gugugaga233.dysoncubeprojectaddon.DysonCubeProject;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereProgressSavedData;
import com.gugugaga233.dysoncubeprojectaddon.network.DysonSphereSyncMessage;
import net.minecraft.core.HolderLookup;
import com.hrznstudio.titanium.network.Message;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientSubscribeSphereMessage
extends Message {
    public String sphereId;

    public ClientSubscribeSphereMessage() {
    }

    public ClientSubscribeSphereMessage(String sphereId) {
        this.sphereId = sphereId;
    }

    protected void handleMessage(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer sp;
            Level level;
            DysonSphereProgressSavedData data;
            Player patt0$temp = context.player();
            if (patt0$temp instanceof ServerPlayer && (data = DysonSphereProgressSavedData.get(level = (sp = (ServerPlayer)patt0$temp).level())) != null && this.sphereId != null) {
                String playerId = sp.getStringUUID();
                String previous = data.getSubscribedPlayers().put(playerId, this.sphereId);
                if (!this.sphereId.equals(previous)) data.setDirty();
                data.subscribeForClientSync(playerId, this.sphereId);
                if (sp.level().getServer() != null) {
                    DysonCubeProject.NETWORK.sendTo(
                            new DysonSphereSyncMessage(data.getCachedClientSyncTag(
                                    (HolderLookup.Provider) sp.level().getServer().registryAccess(), this.sphereId)), sp);
                }
            }
        });
    }
}


