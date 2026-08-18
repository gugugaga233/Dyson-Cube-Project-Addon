package com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.OverhaulContent;
import com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import sonar.fluxnetworks.api.FluxCapabilities;
import sonar.fluxnetworks.api.energy.BigNumber;
import sonar.fluxnetworks.api.energy.IBigNumberEnergyStorage;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

public class BigNumberEnergyOutputPortBlockEntity extends BlockEntity {
    private final IBigNumberEnergyStorage bigEnergyStorage = new OutputBigEnergyStorage();
    private final IFNEnergyStorage fluxEnergyStorage = new OutputFluxEnergyStorage();
    private final IEnergyStorage forgeEnergyStorage = new OutputForgeEnergyStorage();

    public BigNumberEnergyOutputPortBlockEntity(BlockPos pos, BlockState state) {
        super(OverhaulContent.getBigNumberEnergyOutputPortBEType(), pos, state);
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

    public boolean isHubConnected() {
        return findHub() != null;
    }

    public boolean isBigNumberNetworkConnected() {
        if (level == null) return false;
        for (Direction direction : Direction.values()) {
            BlockPos targetPos = worldPosition.relative(direction);
            BlockEntity targetEntity = level.getBlockEntity(targetPos);
            IBigNumberEnergyStorage target = level.getCapability(FluxCapabilities.BIG_NUMBER_BLOCK,
                    targetPos, level.getBlockState(targetPos), targetEntity, direction.getOpposite());
            if (target != null && target.canReceive()) return true;
        }
        return false;
    }

    public void tick() {
        if (level == null || level.isClientSide || !isHubConnected()) return;
        for (Direction direction : Direction.values()) {
            BlockPos targetPos = worldPosition.relative(direction);
            BlockEntity targetEntity = level.getBlockEntity(targetPos);
            IBigNumberEnergyStorage target = level.getCapability(FluxCapabilities.BIG_NUMBER_BLOCK,
                    targetPos, level.getBlockState(targetPos), targetEntity, direction.getOpposite());
            if (target == null || !target.canReceive()) continue;

            BigNumber offered = getStoredEnergy();
            if (offered.isEmpty()) return;
            BigNumber accepted = target.receiveEnergy(offered.deepCopy(), true);
            if (accepted == null || accepted.signum() <= 0) continue;

            BigNumber transferable = offered.quote(accepted.deepCopy());
            BigNumber extracted = extractEnergy(transferable, false);
            if (extracted.isEmpty()) return;
            target.receiveEnergy(extracted.deepCopy(), false);
            return;
        }
    }

    public String getEnergyDisplay() {
        return NumberUtils.getCompactBigNumber(getStoredEnergy());
    }

    private BigNumber getStoredEnergy() {
        DysonHubBlockEntity hub = findHub();
        return hub == null ? new BigNumber(0) : hub.getBigEnergyStorage().getEnergyStored();
    }

    private BigNumber extractEnergy(BigNumber maximum, boolean simulate) {
        DysonHubBlockEntity hub = findHub();
        return hub == null ? new BigNumber(0) : hub.getBigEnergyStorage().extractEnergy(maximum, simulate);
    }

    @org.jetbrains.annotations.Nullable
    private DysonHubBlockEntity findHub() {
        if (level == null) return null;
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof DysonHubBlockEntity hub) {
                return hub;
            }
        }
        return null;
    }

    private final class OutputBigEnergyStorage implements IBigNumberEnergyStorage {
        @Override
        public BigNumber receiveEnergy(BigNumber maximum, boolean simulate) {
            return new BigNumber(0);
        }

        @Override
        public BigNumber extractEnergy(BigNumber maximum, boolean simulate) {
            if (maximum == null || maximum.signum() <= 0) return new BigNumber(0);
            return BigNumberEnergyOutputPortBlockEntity.this.extractEnergy(maximum, simulate);
        }

        @Override
        public BigNumber getEnergyStored() {
            return getStoredEnergy();
        }

        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return false; }
    }

    private final class OutputFluxEnergyStorage implements IFNEnergyStorage {
        @Override public long receiveEnergyL(long maximum, boolean simulate) { return 0; }

        @Override
        public long extractEnergyL(long maximum, boolean simulate) {
            if (maximum <= 0) return 0;
            return BigNumberEnergyOutputPortBlockEntity.this
                    .extractEnergy(BigNumber.valueOf(maximum), simulate)
                    .getEnergyStoredLong();
        }

        @Override public long getEnergyStoredL() { return getStoredEnergy().getEnergyStoredLong(); }
        @Override public long getMaxEnergyStoredL() { return Long.MAX_VALUE; }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return false; }
    }

    private final class OutputForgeEnergyStorage implements IEnergyStorage {
        @Override public int receiveEnergy(int maximum, boolean simulate) { return 0; }

        @Override
        public int extractEnergy(int maximum, boolean simulate) {
            if (maximum <= 0) return 0;
            return (int) Math.min(Integer.MAX_VALUE,
                    BigNumberEnergyOutputPortBlockEntity.this
                            .extractEnergy(BigNumber.valueOf(maximum), simulate)
                            .getEnergyStoredLong());
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, getStoredEnergy().getEnergyStoredLong());
        }

        @Override public int getMaxEnergyStored() { return Integer.MAX_VALUE; }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return false; }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
    }
}

