package com.gugugaga233.dysoncubeprojectaddon.network;

import com.hrznstudio.titanium.network.Message;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientboundOpenDysonHubPacket extends Message {
    public String serializedStar;
    public double totalPower;
    public long totalWrapped;
    public String totalWrappedExact;
    public String hierarchyLevel;
    public String hierarchyBatchDisplay;
    public String hierarchyNextThresholdDisplay;
    public boolean hierarchyBatchCapped;
    public boolean universeAwakened;
    public String darkMatterResonanceExact;
    public boolean cosmicHeartActive;
    public double cosmicHeartProgress;
    public boolean cosmicHeartComplete;
    public String cosmicHeartBatchDisplay;
    public String queuedCosmicHeartDisplay;
    public String cosmicHeartBeamDisplay;
    public String cosmicHeartSailDisplay;
    public String powerDisplay;
    public String storedEnergyDisplay;
    public String powerCalculation;
    public String storedEnergyCalculation;

    public ClientboundOpenDysonHubPacket() {
        this("", 0, 0, "0", "SINGLE_STAR", "100", "5000", false,
                false, "0", false, 0, false, "0", "0",
                "0 / 0", "0 / 0", "0", "0", "0", "0");
    }

    public ClientboundOpenDysonHubPacket(
            String serializedStar,
            double totalPower,
            long totalWrapped,
            String totalWrappedExact,
            String hierarchyLevel,
            String hierarchyBatchDisplay,
            String hierarchyNextThresholdDisplay,
            boolean hierarchyBatchCapped,
            boolean universeAwakened,
            String darkMatterResonanceExact,
            boolean cosmicHeartActive,
            double cosmicHeartProgress,
            boolean cosmicHeartComplete,
            String cosmicHeartBatchDisplay,
            String queuedCosmicHeartDisplay,
            String cosmicHeartBeamDisplay,
            String cosmicHeartSailDisplay,
            String powerDisplay,
            String storedEnergyDisplay,
            String powerCalculation,
            String storedEnergyCalculation
    ) {
        this.serializedStar = serializedStar;
        this.totalPower = totalPower;
        this.totalWrapped = totalWrapped;
        this.totalWrappedExact = totalWrappedExact;
        this.hierarchyLevel = hierarchyLevel;
        this.hierarchyBatchDisplay = hierarchyBatchDisplay;
        this.hierarchyNextThresholdDisplay = hierarchyNextThresholdDisplay;
        this.hierarchyBatchCapped = hierarchyBatchCapped;
        this.universeAwakened = universeAwakened;
        this.darkMatterResonanceExact = darkMatterResonanceExact;
        this.cosmicHeartActive = cosmicHeartActive;
        this.cosmicHeartProgress = cosmicHeartProgress;
        this.cosmicHeartComplete = cosmicHeartComplete;
        this.cosmicHeartBatchDisplay = cosmicHeartBatchDisplay;
        this.queuedCosmicHeartDisplay = queuedCosmicHeartDisplay;
        this.cosmicHeartBeamDisplay = cosmicHeartBeamDisplay;
        this.cosmicHeartSailDisplay = cosmicHeartSailDisplay;
        this.powerDisplay = powerDisplay;
        this.storedEnergyDisplay = storedEnergyDisplay;
        this.powerCalculation = powerCalculation;
        this.storedEnergyCalculation = storedEnergyCalculation;
    }

    @Override
    protected void handleMessage(IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketDispatcher.handle(this));
    }
}

