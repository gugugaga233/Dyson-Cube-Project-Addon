package com.gugugaga233.dysoncubeprojectaddon.network;

import com.hrznstudio.titanium.network.Message;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientboundOpenCosmicMiningHubPacket extends Message {
    private static final int MAX_SYNC_STRING_LENGTH = 30_000;
    public long blockPos;
    public String planetType;
    public String initialMass;
    public String remainingMass;
    public String remainingPercent;
    public boolean depleted;
    public String laserInput;
    public String zeroCount;
    public String efficiencyPercent;
    public String effectivePower;
    public String storedEnergy;
    public String externalEnergy;
    public int connectedPorts;
    public int starTier;
    public String starTierId;
    public String tierMinimumMass;
    public String tierMaximumMass;
    public String tierEnergyMultiplier;
    public String energyCost;
    public boolean canChangeTier;
    public boolean running;
    public boolean canSelectNext;
    public String status;
    public String inventory;
    public String fluids;
    public String resourceItems;
    public String resourceFluids;

    public ClientboundOpenCosmicMiningHubPacket() {
        this(0, "silicate", "0", "0", "0", false, "1", "0", "100.0", "1", "0", "0", 0,
                1, "single_planet", "0.1", "1", "1", "1000", true,
                false, true, "stopped", "", "", "", "");
    }

    public ClientboundOpenCosmicMiningHubPacket(long blockPos, String planetType, String initialMass,
                                                 String remainingMass, String remainingPercent, boolean depleted,
                                                 String laserInput, String zeroCount, String efficiencyPercent,
                                                 String effectivePower, String storedEnergy, String externalEnergy,
                                                 int connectedPorts, int starTier, String starTierId,
                                                 String tierMinimumMass, String tierMaximumMass,
                                                 String tierEnergyMultiplier, String energyCost,
                                                 boolean canChangeTier, boolean running,
                                                 boolean canSelectNext,
                                                 String status, String inventory, String fluids,
                                                 String resourceItems, String resourceFluids) {
        this.blockPos = blockPos;
        this.planetType = bounded(planetType);
        this.initialMass = bounded(initialMass);
        this.remainingMass = bounded(remainingMass);
        this.remainingPercent = bounded(remainingPercent);
        this.depleted = depleted;
        this.laserInput = bounded(laserInput);
        this.zeroCount = bounded(zeroCount);
        this.efficiencyPercent = bounded(efficiencyPercent);
        this.effectivePower = bounded(effectivePower);
        this.storedEnergy = bounded(storedEnergy);
        this.externalEnergy = bounded(externalEnergy);
        this.connectedPorts = connectedPorts;
        this.starTier = starTier;
        this.starTierId = bounded(starTierId);
        this.tierMinimumMass = bounded(tierMinimumMass);
        this.tierMaximumMass = bounded(tierMaximumMass);
        this.tierEnergyMultiplier = bounded(tierEnergyMultiplier);
        this.energyCost = bounded(energyCost);
        this.canChangeTier = canChangeTier;
        this.running = running;
        this.canSelectNext = canSelectNext;
        this.status = bounded(status);
        this.inventory = bounded(inventory);
        this.fluids = bounded(fluids);
        this.resourceItems = bounded(resourceItems);
        this.resourceFluids = bounded(resourceFluids);
    }

    private static String bounded(String value) {
        if (value == null) return "";
        return value.length() <= MAX_SYNC_STRING_LENGTH
                ? value : value.substring(0, MAX_SYNC_STRING_LENGTH);
    }

    @Override
    protected void handleMessage(IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketDispatcher.handle(this));
    }
}

