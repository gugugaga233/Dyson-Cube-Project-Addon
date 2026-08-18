package com.gugugaga233.dysoncubeprojectaddon.network;

import com.hrznstudio.titanium.network.Message;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientboundAE2ExactAmountsPacket extends Message {
    public int containerId;
    public CompoundTag payload;

    public ClientboundAE2ExactAmountsPacket() {
        this(0, new CompoundTag());
    }

    public ClientboundAE2ExactAmountsPacket(int containerId, CompoundTag payload) {
        this.containerId = containerId;
        this.payload = payload;
    }

    @Override
    protected void handleMessage(IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketDispatcher.handle(this));
    }
}

