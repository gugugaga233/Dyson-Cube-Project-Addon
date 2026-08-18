package com.gugugaga233.dysoncubeprojectaddon.network;

import com.hrznstudio.titanium.network.Message;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientboundOpenOreProcessingHubPacket extends Message {
    private static final long serialVersionUID = 1L;

    public long blockPos;
    public String status;
    public boolean enabled;
    public long costPerOre;
    public int connectedInputs;
    public String pendingOre;
    public String lastProcessed;
    public String totalProcessed;
    public String storedProducts;
    public String lastEnergyCost;
    public String inventory;

    public ClientboundOpenOreProcessingHubPacket() {
        this(0, "no_input", true, 12_000L, 0,
                "0", "0", "0", "0", "0", "");
    }

    public ClientboundOpenOreProcessingHubPacket(long blockPos, String status, boolean enabled,
                                                  long costPerOre, int connectedInputs,
                                                  String pendingOre, String lastProcessed,
                                                  String totalProcessed, String storedProducts,
                                                  String lastEnergyCost, String inventory) {
        this.blockPos = blockPos;
        this.status = status;
        this.enabled = enabled;
        this.costPerOre = costPerOre;
        this.connectedInputs = connectedInputs;
        this.pendingOre = pendingOre;
        this.lastProcessed = lastProcessed;
        this.totalProcessed = totalProcessed;
        this.storedProducts = storedProducts;
        this.lastEnergyCost = lastEnergyCost;
        this.inventory = inventory;
    }

    @Override
    protected void handleMessage(IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketDispatcher.handle(this));
    }
}

