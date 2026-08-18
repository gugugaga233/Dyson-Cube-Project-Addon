package com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.OverhaulContent;
import com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import sonar.fluxnetworks.api.energy.BigNumber;
import sonar.fluxnetworks.api.energy.IBigNumberEnergyStorage;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

public class LaserEnergyInputPortBlockEntity extends BlockEntity {
    private BigNumber energy = new BigNumber(0);
    private final IBigNumberEnergyStorage bigEnergyStorage = new PortBigEnergyStorage();
    private final IFNEnergyStorage fluxEnergyStorage = new PortFluxEnergyStorage();
    private final IEnergyStorage forgeEnergyStorage = new PortForgeEnergyStorage();

    public LaserEnergyInputPortBlockEntity(BlockPos pos, BlockState state) {
        super(OverhaulContent.getLaserEnergyInputPortBEType(), pos, state);
    }

    public long extractForHub(long maximum, boolean simulate) {
        if (maximum <= 0) return 0;
        long extracted = energy.quoteChunk(maximum);
        if (!simulate && extracted > 0) {
            energy.extractChunk(extracted);
            setChanged();
        }
        return extracted;
    }

    public BigNumber extractForHub(BigNumber maximum, boolean simulate) {
        if (maximum == null || maximum.signum() <= 0) return new BigNumber(0);
        BigNumber extracted = energy.quote(maximum.deepCopy());
        if (!simulate && extracted.signum() > 0) {
            extracted = energy.extract(maximum.deepCopy());
            setChanged();
        }
        return extracted;
    }

    public BigNumber getStoredEnergy() {
        return energy.deepCopy();
    }

    public String getEnergyDisplay() {
        return NumberUtils.getCompactBigNumber(energy);
    }

    public IBigNumberEnergyStorage getBigEnergyStorage() {
        return bigEnergyStorage;
    }

    public IFNEnergyStorage getFluxEnergyStorage() {
        return fluxEnergyStorage;
    }

    public IEnergyStorage getForgeEnergyStorage() {
        return forgeEnergyStorage;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("energy", energy.toTag());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("energy")) energy = BigNumber.fromTag(tag.getCompound("energy"));
        if (energy.isImmutable()) energy = energy.deepCopy();
    }

    private final class PortBigEnergyStorage implements IBigNumberEnergyStorage {
        @Override
        public BigNumber receiveEnergy(BigNumber maximum, boolean simulate) {
            if (maximum == null || maximum.signum() <= 0) return new BigNumber(0);
            BigNumber accepted = maximum.deepCopy();
            if (!simulate) {
                energy.addEnergy(accepted.deepCopy());
                setChanged();
            }
            return accepted;
        }

        @Override public BigNumber extractEnergy(BigNumber maximum, boolean simulate) { return new BigNumber(0); }
        @Override public BigNumber getEnergyStored() { return energy.deepCopy(); }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    }

    private final class PortFluxEnergyStorage implements IFNEnergyStorage {
        @Override
        public long receiveEnergyL(long maximum, boolean simulate) {
            if (maximum <= 0) return 0;
            if (!simulate) {
                energy.addEnergy(maximum);
                setChanged();
            }
            return maximum;
        }

        @Override public long extractEnergyL(long maximum, boolean simulate) { return 0; }
        @Override public long getEnergyStoredL() { return energy.getEnergyStoredLong(); }
        @Override public long getMaxEnergyStoredL() { return Long.MAX_VALUE; }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    }

    private final class PortForgeEnergyStorage implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maximum, boolean simulate) {
            if (maximum <= 0) return 0;
            if (!simulate) {
                energy.addEnergy(maximum);
                setChanged();
            }
            return maximum;
        }

        @Override public int extractEnergy(int maximum, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return (int) Math.min(Integer.MAX_VALUE, energy.getEnergyStoredLong()); }
        @Override public int getMaxEnergyStored() { return Integer.MAX_VALUE; }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    }
}

