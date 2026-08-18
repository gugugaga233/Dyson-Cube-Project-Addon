package com.gugugaga233.dysoncubeprojectaddon.network;

import com.hrznstudio.titanium.network.Message;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 服务端 → 客户端：更新寰宇之心投料进度
 * <p>
 * 在投料后由服务端发送，客户端更新 DysonHub 中的寰宇之心进度显示。
 */
public class ClientboundCosmicHeartFeedPacket extends Message {
    public final double progress;
    public final boolean active;
    public final boolean complete;

    public ClientboundCosmicHeartFeedPacket() {
        this.progress = 0.0;
        this.active = false;
        this.complete = false;
    }

    public ClientboundCosmicHeartFeedPacket(double progress, boolean active, boolean complete) {
        this.progress = progress;
        this.active = active;
        this.complete = complete;
    }

    @Override
    protected void handleMessage(IPayloadContext context) {
        // 进度已通过 DysonSphereSyncMessage 同步，此包仅用于投料后的即时反馈
        // 实际进度更新由 DysonSphereSyncMessage 携带
    }
}

