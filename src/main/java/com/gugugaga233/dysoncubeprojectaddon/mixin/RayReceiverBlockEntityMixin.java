package com.gugugaga233.dysoncubeprojectaddon.mixin;

import com.buuz135.dysoncubeproject.block.tile.RayReceiverBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.Config;
import com.gugugaga233.dysoncubeprojectaddon.bridge.OriginalRayReceiverBridge;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereProgressSavedData;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sonar.fluxnetworks.api.FluxCapabilities;
import sonar.fluxnetworks.api.energy.BigNumber;
import sonar.fluxnetworks.api.energy.IBigNumberEnergyStorage;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

@Mixin(RayReceiverBlockEntity.class)
public abstract class RayReceiverBlockEntityMixin implements OriginalRayReceiverBridge {
    @Shadow
    private float currentPitch;

    @Unique
    private BigNumber dcpAddon$bigEnergyBuffer = new BigNumber(0);
    @Unique
    private IBigNumberEnergyStorage dcpAddon$bigStorage;
    @Unique
    private IFNEnergyStorage dcpAddon$fluxStorage;
    @Unique
    private IEnergyStorage dcpAddon$forgeStorage;

    @Unique
    private RayReceiverBlockEntity dcpAddon$self() {
        return (RayReceiverBlockEntity) (Object) this;
    }

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private void dcpAddon$serverTick(Level level, BlockPos pos, BlockState state,
                                     RayReceiverBlockEntity blockEntity, CallbackInfo ci) {
        if (level.isDay() && !level.isRaining() && level.canSeeSky(pos.above())) {
            DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(level);
            DysonSphereStructure sphere = data.getSpheres()
                    .computeIfAbsent(blockEntity.getDysonSphereId(), id -> new DysonSphereStructure());
            BigNumber extracted = sphere.extractEnergy(sphere.getStoredEnergy(), false);
            this.dcpAddon$bigEnergyBuffer.addEnergy(extracted);
            data.setDirty();
        }

        dcpAddon$pushEnergy(level, pos);
        blockEntity.getEnergyStorageComponent().setEnergyStored((int) Math.min(
                Integer.MAX_VALUE, this.dcpAddon$bigEnergyBuffer.getEnergyStoredLong()));

        float targetPitch = level.getTimeOfDay(1.0F) * 360.0F;
        if (targetPitch >= 90.0F && targetPitch <= 270.0F) targetPitch = 270.0F;
        if (this.currentPitch % 360.0F <= targetPitch) {
            this.currentPitch = Math.min((this.currentPitch + 1.0F) % 360.0F, targetPitch);
        } else {
            this.currentPitch = Math.max(this.currentPitch - 1.0F, targetPitch);
        }
        blockEntity.syncObject(this.currentPitch);
        ci.cancel();
    }

    @Unique
    private void dcpAddon$pushEnergy(Level level, BlockPos pos) {
        BlockPos targetPos = pos.below();
        BlockState targetState = level.getBlockState(targetPos);
        BlockEntity targetEntity = level.getBlockEntity(targetPos);
        IBigNumberEnergyStorage big = level.getCapability(
                FluxCapabilities.BIG_NUMBER_BLOCK, targetPos, targetState, targetEntity, Direction.UP);
        if (big != null && big.canReceive()) {
            BigNumber accepted = big.receiveEnergy(this.dcpAddon$bigEnergyBuffer.deepCopy(), true);
            if (accepted != null && !accepted.isEmpty()) {
                BigNumber extracted = this.dcpAddon$bigEnergyBuffer.extractContainer(accepted.deepCopy());
                BigNumber committed = big.receiveEnergy(extracted.deepCopy(), false);
                BigNumber clamped = committed == null || committed.signum() <= 0
                        ? new BigNumber(0) : extracted.quote(committed.deepCopy());
                BigNumber refund = extracted.deepCopy().subtract(clamped);
                if (!refund.isEmpty()) this.dcpAddon$bigEnergyBuffer.addEnergy(refund);
            }
            return;
        }

        IFNEnergyStorage flux = level.getCapability(
                FluxCapabilities.BLOCK, targetPos, targetState, targetEntity, Direction.UP);
        if (flux != null && flux.canReceive()) {
            long offered = this.dcpAddon$bigEnergyBuffer.quoteChunk(Config.RAY_RECEIVER_EXTRACT_POWER);
            long accepted = flux.receiveEnergyL(offered, true);
            if (accepted > 0L) {
                long extracted = this.dcpAddon$bigEnergyBuffer.extractChunk(accepted);
                long committed = Math.max(0L, Math.min(extracted,
                        flux.receiveEnergyL(extracted, false)));
                if (committed < extracted) this.dcpAddon$bigEnergyBuffer.addEnergy(extracted - committed);
            }
            return;
        }

        IEnergyStorage forge = level.getCapability(
                Capabilities.EnergyStorage.BLOCK, targetPos, targetState, targetEntity, Direction.UP);
        if (forge != null && forge.canReceive()) {
            int offered = (int) Math.min(Integer.MAX_VALUE,
                    this.dcpAddon$bigEnergyBuffer.quoteChunk(Config.RAY_RECEIVER_EXTRACT_POWER));
            int accepted = forge.receiveEnergy(offered, true);
            if (accepted > 0) {
                long extracted = this.dcpAddon$bigEnergyBuffer.extractChunk(accepted);
                int committed = Math.max(0, Math.min((int) extracted,
                        forge.receiveEnergy((int) extracted, false)));
                if (committed < extracted) this.dcpAddon$bigEnergyBuffer.addEnergy(extracted - committed);
            }
        }
    }

    @Override
    public IBigNumberEnergyStorage dcpAddon$getBigEnergyStorage() {
        if (this.dcpAddon$bigStorage == null) {
            this.dcpAddon$bigStorage = new IBigNumberEnergyStorage() {
                @Override
                public BigNumber receiveEnergy(BigNumber maximum, boolean simulate) {
                    if (maximum == null || maximum.signum() <= 0) return new BigNumber(0);
                    BigNumber accepted = maximum.deepCopy();
                    if (!simulate) dcpAddon$bigEnergyBuffer.addEnergy(accepted.deepCopy());
                    return accepted;
                }

                @Override
                public BigNumber extractEnergy(BigNumber maximum, boolean simulate) {
                    if (maximum == null || maximum.signum() <= 0) return new BigNumber(0);
                    return simulate ? dcpAddon$bigEnergyBuffer.quoteContainer(maximum.deepCopy())
                            : dcpAddon$bigEnergyBuffer.extractContainer(maximum.deepCopy());
                }

                @Override public BigNumber getEnergyStored() { return dcpAddon$bigEnergyBuffer.deepCopy(); }
                @Override public boolean canExtract() { return true; }
                @Override public boolean canReceive() { return true; }
            };
        }
        return this.dcpAddon$bigStorage;
    }

    @Override
    public IFNEnergyStorage dcpAddon$getFluxEnergyStorage() {
        if (this.dcpAddon$fluxStorage == null) {
            this.dcpAddon$fluxStorage = new IFNEnergyStorage() {
                @Override public long receiveEnergyL(long maximum, boolean simulate) {
                    if (maximum <= 0L) return 0L;
                    if (!simulate) dcpAddon$bigEnergyBuffer.addEnergy(maximum);
                    return maximum;
                }
                @Override public long extractEnergyL(long maximum, boolean simulate) {
                    return simulate ? dcpAddon$bigEnergyBuffer.quoteChunk(maximum)
                            : dcpAddon$bigEnergyBuffer.extractChunk(maximum);
                }
                @Override public long getEnergyStoredL() { return dcpAddon$bigEnergyBuffer.getEnergyStoredLong(); }
                @Override public long getMaxEnergyStoredL() { return Long.MAX_VALUE; }
                @Override public boolean canExtract() { return true; }
                @Override public boolean canReceive() { return true; }
            };
        }
        return this.dcpAddon$fluxStorage;
    }

    @Override
    public IEnergyStorage dcpAddon$getForgeEnergyStorage() {
        if (this.dcpAddon$forgeStorage == null) {
            this.dcpAddon$forgeStorage = new IEnergyStorage() {
                @Override public int receiveEnergy(int maximum, boolean simulate) {
                    if (maximum <= 0) return 0;
                    if (!simulate) dcpAddon$bigEnergyBuffer.addEnergy(maximum);
                    return maximum;
                }
                @Override public int extractEnergy(int maximum, boolean simulate) {
                    long amount = simulate ? dcpAddon$bigEnergyBuffer.quoteChunk(maximum)
                            : dcpAddon$bigEnergyBuffer.extractChunk(maximum);
                    return (int) Math.min(Integer.MAX_VALUE, amount);
                }
                @Override public int getEnergyStored() {
                    return (int) Math.min(Integer.MAX_VALUE, dcpAddon$bigEnergyBuffer.getEnergyStoredLong());
                }
                @Override public int getMaxEnergyStored() { return Integer.MAX_VALUE; }
                @Override public boolean canExtract() { return true; }
                @Override public boolean canReceive() { return true; }
            };
        }
        return this.dcpAddon$forgeStorage;
    }
}
