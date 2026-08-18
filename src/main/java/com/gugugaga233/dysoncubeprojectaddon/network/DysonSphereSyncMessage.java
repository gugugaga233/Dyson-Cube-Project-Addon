package com.gugugaga233.dysoncubeprojectaddon.network;

import com.hrznstudio.titanium.network.Message;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class DysonSphereSyncMessage
extends Message {
    public CompoundTag tag;

    public DysonSphereSyncMessage() {
    }

    public DysonSphereSyncMessage(CompoundTag tag) {
        this.tag = tag;
    }

    protected void handleMessage(IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketDispatcher.handle(this));
    }
}

