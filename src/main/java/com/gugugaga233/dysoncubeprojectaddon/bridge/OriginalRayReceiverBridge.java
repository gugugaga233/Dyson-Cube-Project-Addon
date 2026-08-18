package com.gugugaga233.dysoncubeprojectaddon.bridge;

import net.neoforged.neoforge.energy.IEnergyStorage;
import sonar.fluxnetworks.api.energy.IBigNumberEnergyStorage;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

public interface OriginalRayReceiverBridge {
    IBigNumberEnergyStorage dcpAddon$getBigEnergyStorage();

    IFNEnergyStorage dcpAddon$getFluxEnergyStorage();

    IEnergyStorage dcpAddon$getForgeEnergyStorage();
}
