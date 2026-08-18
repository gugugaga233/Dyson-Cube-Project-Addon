package com.gugugaga233.dysoncubeprojectaddon.mixin;

import com.buuz135.dysoncubeproject.block.tile.EMRailEjectorBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.Config;
import com.gugugaga233.dysoncubeprojectaddon.item.CompressedItem;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.AntimatterStarData;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.PrimordialQiManager;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.UniverseEvents;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereProgressSavedData;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereStructure;
import com.hrznstudio.titanium.block.BasicTileBlock;
import java.math.BigDecimal;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;
import sonar.fluxnetworks.api.energy.BigNumber;

@Mixin(EMRailEjectorBlockEntity.class)
public abstract class EMRailEjectorBlockEntityMixin {
    @Unique
    private static final long DCP_ADDON_STRUCTURE_TIME_BUDGET_NANOS = 2_000_000L;
    @Unique
    private long dcpAddon$lastStructureSettlementTick = Long.MIN_VALUE;

    @Shadow
    private long lastExecution;
    @Shadow
    private int rampupAmount;
    @Shadow
    private int cooldown;

    @Unique
    private EMRailEjectorBlockEntity dcpAddon$self() {
        return (EMRailEjectorBlockEntity) (Object) this;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void dcpAddon$acceptCompressedItems(BasicTileBlock<EMRailEjectorBlockEntity> base,
                                                 BlockEntityType<?> type,
                                                 BlockPos pos,
                                                 BlockState state,
                                                 CallbackInfo ci) {
        dcpAddon$self().getInput().setInputFilter((stack, slot) ->
                CompressedItem.getSolarSailCount(stack) > 0
                        || CompressedItem.getBeamCount(stack) > 0
                        || stack.getItem() instanceof CompressedItem);
    }

    /** @author gugugaga233 @reason Route original machine work into the addon's exact structure ledger. */
    @Overwrite
    private boolean canIncrease() {
        EMRailEjectorBlockEntity self = dcpAddon$self();
        Level level = self.getLevel();
        if (level == null || this.cooldown > 0 || self.getInput().getStackInSlot(0).isEmpty()) return false;
        if (level.isRaining() || level.isNight() || !level.canSeeSky(self.getBlockPos().above())) return false;
        float time = level.getTimeOfDay(1.0F) * 360.0F;
        if (time <= 10.0F || time >= 350.0F) return false;

        DysonSphereStructure sphere = DysonSphereProgressSavedData.get(level).getSpheres()
                .computeIfAbsent(self.getDysonSphereId(), id -> new DysonSphereStructure());
        if (sphere.getInteractionMode() == DysonSphereStructure.InteractionMode.ANNIHILATION) {
            if (sphere.getAntimatterData() == null
                    || AntimatterStarData.getItemMassKgCapped(
                    self.getInput().getStackInSlot(0), BigDecimal.ONE).signum() <= 0) {
                return false;
            }
        } else if (CompressedItem.getSolarSailCount(self.getInput().getStackInSlot(0)) <= 0
                && CompressedItem.getBeamCount(self.getInput().getStackInSlot(0)) <= 0) {
            return false;
        }

        if (this.rampupAmount > 1 && self.getPower().getEnergyStored()
                < Math.pow(this.rampupAmount, 2.0D) * Config.RAIL_EJECTOR_CONSUME) {
            this.rampupAmount = 1;
            return false;
        }
        return true;
    }

    /** @author gugugaga233 @reason Consume compressed inputs without narrowing their represented amount. */
    @Overwrite
    private void onFinishWork() {
        EMRailEjectorBlockEntity self = dcpAddon$self();
        Level level = self.getLevel();
        if (level == null) return;
        DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(level);
        DysonSphereStructure configured = data.getSpheres()
                .computeIfAbsent(self.getDysonSphereId(), id -> new DysonSphereStructure());
        boolean annihilation = configured.getInteractionMode()
                == DysonSphereStructure.InteractionMode.ANNIHILATION;
        String targetId = data.resolveStructureTargetSphereId(self.getDysonSphereId());
        DysonSphereStructure sphere = annihilation ? configured
                : data.getSpheres().computeIfAbsent(targetId, id -> new DysonSphereStructure());
        if (!annihilation && configured != sphere) {
            configured.transferStructureReservesTo(sphere);
            self.setDysonSphereId(targetId);
            self.syncObject(targetId);
        }

        boolean reset = false;
        for (int i = 0; i < this.rampupAmount; i++) {
            if (self.getInput().getStackInSlot(0).isEmpty()) {
                reset = true;
                break;
            }
            if (annihilation && sphere.getAntimatterData() != null) {
                long wrappedBefore = sphere.getTotalWrapped();
                BigNumber output = sphere.feedAntimatterBigNumber(
                        null, self.getInput().getStackInSlot(0),
                        data.getCosmicHeart().getAntimatterEffectMultiplierExact());
                if (output == null) {
                    reset = true;
                    break;
                }
                dcpAddon$syncGlobalProgress(data, sphere);
                if (sphere.getTotalWrapped() != wrappedBefore) break;
                continue;
            }

            int sails = CompressedItem.getSolarSailCount(self.getInput().getStackInSlot(0));
            int beams = CompressedItem.getBeamCount(self.getInput().getStackInSlot(0));
            BigNumber multiplier = self.getInput().getStackInSlot(0).getItem() instanceof CompressedItem
                    ? CompressedItem.getFluxProgressMultiplier(self.getInput().getStackInSlot(0))
                    : BigNumber.valueOf(1L);
            self.getInput().getStackInSlot(0).shrink(1);
            sphere.addStructureMaterials(
                    beams > 0 ? multiplier.deepCopy().multiplySmallInteger(beams) : new BigNumber(0),
                    sails > 0 ? multiplier.deepCopy().multiplySmallInteger(sails) : new BigNumber(0));
        }

        this.lastExecution = level.getGameTime();
        this.cooldown = 30;
        self.syncObject(this.lastExecution);
        this.rampupAmount = reset ? 1 : Math.min(this.rampupAmount + 1, 64);
        data.setDirty();
    }

    @Inject(method = "serverTick", at = @At("HEAD"))
    private void dcpAddon$processStructureReserve(Level level, BlockPos pos, BlockState state,
                                                    EMRailEjectorBlockEntity blockEntity,
                                                    CallbackInfo ci) {
        long gameTime = level.getGameTime();
        if (this.dcpAddon$lastStructureSettlementTick == gameTime) return;
        this.dcpAddon$lastStructureSettlementTick = gameTime;

        DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(level);
        DysonSphereStructure configured = data.getSpheres()
                .computeIfAbsent(blockEntity.getDysonSphereId(), id -> new DysonSphereStructure());
        String targetId = data.resolveStructureTargetSphereId(blockEntity.getDysonSphereId());
        DysonSphereStructure target = configured.getInteractionMode()
                == DysonSphereStructure.InteractionMode.ANNIHILATION
                ? configured : data.getSpheres().computeIfAbsent(targetId, id -> new DysonSphereStructure());
        if (configured != target) {
            configured.transferStructureReservesTo(target);
            blockEntity.setDysonSphereId(targetId);
            blockEntity.syncObject(targetId);
        }
        if (target.getInteractionMode() == DysonSphereStructure.InteractionMode.ANNIHILATION) return;

        boolean heartWasActive = data.getCosmicHeart().isActive();
        int completed = target.processStructureMaterials(data, targetId, Integer.MAX_VALUE,
                System.nanoTime() + DCP_ADDON_STRUCTURE_TIME_BUDGET_NANOS);
        AbsoluteInteger completedExact = target.getLastProcessedWrapsExact();
        if (completedExact.isZero()) return;
        dcpAddon$syncGlobalProgress(data, target);
        if (level.getServer() != null) {
            if (!heartWasActive && data.getCosmicHeart().isActive()) {
                UniverseEvents.notifyCosmicHeartAppeared(level.getServer());
            }
            UniverseEvents.notifyWrapComplete(level.getServer(), target.getCurrentStarData(),
                    target.getTotalWrappedExact(), completedExact);
            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                if (player.getUUID().toString().equals(targetId)) {
                    PrimordialQiManager.tryTrigger(player, target,
                            target.getTotalWrappedExact(), completedExact);
                    break;
                }
            }
        }
        data.setDirty();
    }

    @Unique
    private static void dcpAddon$syncGlobalProgress(DysonSphereProgressSavedData data,
                                                     DysonSphereStructure sphere) {
        data.setTotalWrappedExact(sphere.getTotalWrappedExact());
        data.setCurrentStarData(sphere.getCurrentStarData());
        if (sphere.getTotalWrapped() >= Config.BLACKHOLE_UNLOCK_THRESHOLD) data.setUniverseAwakened(true);
        if (sphere.getTotalWrapped() >= Config.ANTIMATTER_UNLOCK_THRESHOLD) data.setAntimatterAwakened(true);
    }
}
