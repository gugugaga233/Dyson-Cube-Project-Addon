package com.gugugaga233.dysoncubeprojectaddon.network;

import com.hrznstudio.titanium.network.Message;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 服务端 → 客户端：触发鸿蒙之气道痕烙印选择界面
 */
public class ClientboundPrimordialQiChoicePacket extends Message {
    public String qiCount;

    public ClientboundPrimordialQiChoicePacket() {
        this.qiCount = "0";
    }

    public ClientboundPrimordialQiChoicePacket(String qiCount) {
        this.qiCount = qiCount;
    }

    @Override
    protected void handleMessage(IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketDispatcher.handle(this));
    }
}

