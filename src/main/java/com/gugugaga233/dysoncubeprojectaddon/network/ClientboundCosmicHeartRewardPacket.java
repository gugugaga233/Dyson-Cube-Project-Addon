package com.gugugaga233.dysoncubeprojectaddon.network;

import com.hrznstudio.titanium.network.Message;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 服务端 → 客户端：触发寰宇之心规则选择界面
 */
public class ClientboundCosmicHeartRewardPacket extends Message {
    public String appliedCounts;
    public String requiredChoices;

    public ClientboundCosmicHeartRewardPacket() {
        this("0,0,0,0,0,0,0", "1");
    }

    public ClientboundCosmicHeartRewardPacket(String appliedCounts) {
        this(appliedCounts, "1");
    }

    public ClientboundCosmicHeartRewardPacket(String appliedCounts, String requiredChoices) {
        this.appliedCounts = appliedCounts;
        this.requiredChoices = requiredChoices;
    }

    @Override
    protected void handleMessage(IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketDispatcher.handle(this));
    }
}

