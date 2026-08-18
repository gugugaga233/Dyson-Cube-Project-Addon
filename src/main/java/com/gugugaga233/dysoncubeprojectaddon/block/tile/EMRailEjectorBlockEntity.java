/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hrznstudio.titanium.annotation.Save
 *  com.hrznstudio.titanium.api.IFactory
 *  com.hrznstudio.titanium.api.client.IScreenAddon
 *  com.hrznstudio.titanium.api.client.IScreenAddonProvider
 *  com.hrznstudio.titanium.block.BasicTileBlock
 *  com.hrznstudio.titanium.block.tile.BasicTile
 *  com.hrznstudio.titanium.block.tile.ITickableBlockEntity
 *  com.hrznstudio.titanium.client.screen.asset.IAssetProvider
 *  com.hrznstudio.titanium.client.screen.asset.IHasAssetProvider
 *  com.hrznstudio.titanium.component.IComponentHarness
 *  com.hrznstudio.titanium.component.energy.EnergyStorageComponent
 *  com.hrznstudio.titanium.component.inventory.InventoryComponent
 *  com.hrznstudio.titanium.component.progress.ProgressBarComponent
 *  com.hrznstudio.titanium.component.progress.ProgressBarComponent$BarDirection
 *  com.hrznstudio.titanium.container.BasicAddonContainer
 *  com.hrznstudio.titanium.container.addon.IContainerAddon
 *  com.hrznstudio.titanium.container.addon.IContainerAddonProvider
 *  com.hrznstudio.titanium.network.IButtonHandler
 *  com.hrznstudio.titanium.network.locator.LocatorFactory
 *  com.hrznstudio.titanium.network.locator.LocatorInstance
 *  com.hrznstudio.titanium.network.locator.instance.TileEntityLocatorInstance
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Style
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.ItemInteractionResult
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerLevelAccess
 *  net.minecraft.world.item.DyeColor
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  org.jetbrains.annotations.NotNull
 */
package com.gugugaga233.dysoncubeprojectaddon.block.tile;

import com.gugugaga233.dysoncubeprojectaddon.Config;
import com.gugugaga233.dysoncubeprojectaddon.DCPAttachments;
import com.gugugaga233.dysoncubeprojectaddon.DCPContent;
import com.gugugaga233.dysoncubeprojectaddon.client.gui.DysonProgressGuiAddon;
import com.gugugaga233.dysoncubeprojectaddon.client.gui.SubscribeDysonGuiAddon;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.StarData;
import com.gugugaga233.dysoncubeprojectaddon.item.CompressedItem;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereProgressSavedData;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereStructure;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.api.client.IScreenAddonProvider;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.block.tile.BasicTile;
import com.hrznstudio.titanium.block.tile.ITickableBlockEntity;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.client.screen.asset.IHasAssetProvider;
import com.hrznstudio.titanium.component.IComponentHarness;
import com.hrznstudio.titanium.component.energy.EnergyStorageComponent;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import com.hrznstudio.titanium.component.progress.ProgressBarComponent;
import com.hrznstudio.titanium.container.BasicAddonContainer;
import com.hrznstudio.titanium.container.addon.IContainerAddon;
import com.hrznstudio.titanium.container.addon.IContainerAddonProvider;
import com.hrznstudio.titanium.network.IButtonHandler;
import com.hrznstudio.titanium.network.locator.LocatorFactory;
import com.hrznstudio.titanium.network.locator.LocatorInstance;
import com.hrznstudio.titanium.network.locator.instance.TileEntityLocatorInstance;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import sonar.fluxnetworks.api.energy.BigNumber;

public class EMRailEjectorBlockEntity
extends BasicTile<EMRailEjectorBlockEntity>
implements IScreenAddonProvider,
ITickableBlockEntity<EMRailEjectorBlockEntity>,
MenuProvider,
IButtonHandler,
IContainerAddonProvider,
IHasAssetProvider,
IComponentHarness {
    private static final long STRUCTURE_TIME_BUDGET_NANOS = 2_000_000L;

    @Save
    private float currentYaw = 180.0f;
    @Save
    private float currentPitch = 90.0f;
    @Save
    private float targetYaw = 180.0f;
    @Save
    private float targetPitch = 90.0f;
    @Save
    private long lastExecution = 0L;
    @Save
    private ProgressBarComponent<EMRailEjectorBlockEntity> progressBarComponent = new ProgressBarComponent<EMRailEjectorBlockEntity>(45, 21, 120).setCanIncrease(iComponentHarness -> this.canIncreaseV2()).setOnTickWork(() -> this.syncObject(this.progressBarComponent)).setOnFinishWork(this::onFinishWorkV2).setIncreaseType(true).setComponentHarness(this).setBarDirection(ProgressBarComponent.BarDirection.VERTICAL_UP).setColor(DyeColor.CYAN).setOnTickWork(this::onTickWork);
    @Save
    private InventoryComponent<EMRailEjectorBlockEntity> input = new InventoryComponent<EMRailEjectorBlockEntity>("input", 7, 42, 1).setInputFilter((itemStack, integer) -> {
        // 原模组物品：Solar Sail / Beam
        int sails = (Integer)itemStack.getOrDefault(DCPAttachments.SOLAR_SAIL.get(), (Object)0);
        int beams = (Integer)itemStack.getOrDefault(DCPAttachments.BEAM.get(), (Object)0);
        if (sails > 0 || beams > 0) return true;
        // V5.0：压缩物品（CompressedItem）
        if (itemStack.getItem() instanceof com.gugugaga233.dysoncubeprojectaddon.item.CompressedItem) {
            return true;
        }
        return false;
    }).setSlotToColorRender(0, DyeColor.CYAN);
    @Save
    private EnergyStorageComponent<EMRailEjectorBlockEntity> power = new EnergyStorageComponent<EMRailEjectorBlockEntity>(Config.RAIL_EJECTOR_POWER_BUFFER, Config.RAIL_EJECTOR_POWER_BUFFER, 0, 26, 21);
    @Save
    private String dysonSphereId = "";
    @Save
    private int rampupAmount = 0;
    private int cooldown = 0;

    public EMRailEjectorBlockEntity(BasicTileBlock<EMRailEjectorBlockEntity> base, BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType, pos, state);
    }

    private boolean canIncreaseV2() {
        if (this.cooldown > 0 || this.input.getStackInSlot(0).isEmpty()) {
            return false;
        }
        if (this.getLevel().isRaining() || this.getLevel().isNight()
                || !this.getLevel().canSeeSky(this.getBlockPos().above())) {
            return false;
        }
        float time = this.level.getTimeOfDay(1.0f) * 360.0f;
        if (time <= 10.0f || time >= 350.0f) {
            return false;
        }

        DysonSphereStructure dyson = DysonSphereProgressSavedData.get(this.level).getSpheres()
                .computeIfAbsent(this.dysonSphereId, id -> new DysonSphereStructure());
        if (dyson.getInteractionMode() == DysonSphereStructure.InteractionMode.ANNIHILATION) {
            if (dyson.getAntimatterData() == null
                    || com.gugugaga233.dysoncubeprojectaddon.overhaul.AntimatterStarData
                    .getItemMassKgCapped(this.input.getStackInSlot(0), java.math.BigDecimal.ONE)
                    .signum() <= 0) {
                return false;
            }
        } else {
            int sails = CompressedItem.getSolarSailCount(this.input.getStackInSlot(0));
            int beams = CompressedItem.getBeamCount(this.input.getStackInSlot(0));
            if (sails <= 0 && beams <= 0) {
                return false;
            }
        }

        if (this.rampupAmount > 1 && (double) this.getPower().getEnergyStored()
                < Math.pow(this.rampupAmount, 2.0) * (double) Config.RAIL_EJECTOR_CONSUME) {
            this.rampupAmount = 1;
            return false;
        }
        return true;
    }

    private void onTickWork() {
        this.power.setEnergyStored((int)Math.max(0.0, (double)this.power.getEnergyStored() - Math.pow(this.rampupAmount, 2.0) * (double)Config.RAIL_EJECTOR_CONSUME));
    }

    private void onFinishWorkV2() {
        DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(this.level);
        DysonSphereStructure configuredSphere = data.getSpheres()
                .computeIfAbsent(this.dysonSphereId, id -> new DysonSphereStructure());
        boolean annihilation = configuredSphere.getInteractionMode()
                == DysonSphereStructure.InteractionMode.ANNIHILATION;
        String structureTargetId = data.resolveStructureTargetSphereId(this.dysonSphereId);
        DysonSphereStructure dyson = annihilation ? configuredSphere
                : data.getSpheres().computeIfAbsent(structureTargetId, id -> new DysonSphereStructure());
        if (!annihilation && configuredSphere != dyson) {
            configuredSphere.transferStructureReservesTo(dyson);
            this.dysonSphereId = structureTargetId;
            this.syncObject(this.dysonSphereId);
        }
        boolean reset = false;

        for (int i = 0; i < this.rampupAmount; i++) {
            if (this.input.getStackInSlot(0).isEmpty()) {
                reset = true;
                break;
            }

            if (annihilation
                    && dyson.getAntimatterData() != null) {
                long wrappedBefore = dyson.getTotalWrapped();
                BigNumber output = dyson.feedAntimatterBigNumber(
                        null, this.input.getStackInSlot(0),
                        data.getCosmicHeart().getAntimatterEffectMultiplierExact());
                if (output == null) {
                    reset = true;
                    break;
                }
                syncGlobalProgress(data, dyson);
                if (dyson.getTotalWrapped() != wrappedBefore) {
                    break;
                }
                continue;
            }

            int sails = CompressedItem.getSolarSailCount(this.input.getStackInSlot(0));
            int beams = CompressedItem.getBeamCount(this.input.getStackInSlot(0));
            BigNumber multiplier = BigNumber.valueOf(1L);
            if (this.input.getStackInSlot(0).getItem()
                    instanceof com.gugugaga233.dysoncubeprojectaddon.item.CompressedItem) {
                multiplier = com.gugugaga233.dysoncubeprojectaddon.item.CompressedItem
                        .getFluxProgressMultiplier(this.input.getStackInSlot(0));
            }
            this.input.getStackInSlot(0).shrink(1);

            BigNumber beamAmount = beams > 0
                    ? multiplier.deepCopy().multiplySmallInteger(beams)
                    : new BigNumber(0);
            BigNumber sailAmount = sails > 0
                    ? multiplier.deepCopy().multiplySmallInteger(sails)
                    : new BigNumber(0);
            dyson.addStructureMaterials(beamAmount, sailAmount);
        }

        this.lastExecution = this.getLevel().getGameTime();
        this.cooldown = 30;
        this.syncObject(this.lastExecution);
        this.rampupAmount = reset ? 1 : Math.min(this.rampupAmount + 1, 64);
        data.setDirty();
    }

    public void serverTick(Level level, BlockPos pos, BlockState state, EMRailEjectorBlockEntity blockEntity) {
        DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(level);
        DysonSphereStructure configuredSphere = data.getSpheres()
                .computeIfAbsent(this.dysonSphereId, id -> new DysonSphereStructure());
        String structureTargetId = data.resolveStructureTargetSphereId(this.dysonSphereId);
        DysonSphereStructure structureTarget = configuredSphere.getInteractionMode()
                == DysonSphereStructure.InteractionMode.ANNIHILATION
                ? configuredSphere
                : data.getSpheres().computeIfAbsent(structureTargetId, id -> new DysonSphereStructure());
        if (configuredSphere != structureTarget) {
            configuredSphere.transferStructureReservesTo(structureTarget);
            this.dysonSphereId = structureTargetId;
            this.syncObject(this.dysonSphereId);
        }
        processStructureReserve(data, structureTarget, structureTargetId);
        if (this.progressBarComponent.getCanIncrease().test((EMRailEjectorBlockEntity)this.progressBarComponent.getComponentHarness())) {
            if (this.progressBarComponent.getIncreaseType() && this.progressBarComponent.getProgress() == 0) {
                this.progressBarComponent.onStart();
            }
            if (!this.progressBarComponent.getIncreaseType() && this.progressBarComponent.getProgress() == this.progressBarComponent.getMaxProgress()) {
                this.progressBarComponent.onStart();
            }
            this.progressBarComponent.tickBar();
        } else if (this.progressBarComponent.getCanReset().test((EMRailEjectorBlockEntity)this.progressBarComponent.getComponentHarness())) {
            this.progressBarComponent.setProgress(this.progressBarComponent.getIncreaseType() ? 0 : this.progressBarComponent.getMaxProgress());
        }
        if (this.cooldown > 0) {
            --this.cooldown;
        }
        this.targetPitch = level.getTimeOfDay(1.0f) * 360.0f;
        if (this.targetPitch <= 10.0f) {
            this.targetPitch = 10.0f;
        }
        if (this.targetPitch >= 350.0f) {
            this.targetPitch = 10.0f;
        }
        this.targetYaw = this.targetPitch <= 90.0f ? 0.0f : 180.0f;
        if (this.targetPitch >= 90.0f && this.targetPitch <= 270.0f) {
            this.targetPitch = 90.0f;
        }
        if (this.targetPitch >= 270.0f) {
            this.targetPitch = 360.0f - this.targetPitch;
        }
        if (level.isRaining()) {
            this.targetPitch = 90.0f;
        }
        if (this.currentPitch <= this.targetPitch) {
            this.currentPitch = Math.min(this.currentPitch + 1.0f, this.targetPitch);
        } else if (this.currentPitch > this.targetPitch) {
            this.currentPitch = Math.max(this.currentPitch - 1.0f, this.targetPitch);
        }
        if (this.currentYaw <= this.targetYaw) {
            this.currentYaw = Math.min(this.currentYaw + 1.0f, this.targetYaw);
        } else if (this.currentYaw > this.targetYaw) {
            this.currentYaw = Math.max(this.currentYaw - 1.0f, this.targetYaw);
        }
    }

    private void processStructureReserve(DysonSphereProgressSavedData data,
                                         DysonSphereStructure dyson,
                                         String structureTargetId) {
        if (dyson.getInteractionMode() == DysonSphereStructure.InteractionMode.ANNIHILATION) {
            return;
        }
        long deadlineNanos = System.nanoTime() + STRUCTURE_TIME_BUDGET_NANOS;
        boolean heartWasActive = data.getCosmicHeart().isActive();
        int completed = dyson.processStructureMaterials(
                data, structureTargetId, Integer.MAX_VALUE, deadlineNanos);
        sonar.fluxnetworks.api.energy.AbsoluteInteger completedExact = dyson.getLastProcessedWrapsExact();
        if (completedExact.isZero()) {
            return;
        }

        syncGlobalProgress(data, dyson);
        if (this.level.getServer() != null) {
            if (!heartWasActive && data.getCosmicHeart().isActive()) {
                com.gugugaga233.dysoncubeprojectaddon.overhaul.UniverseEvents
                        .notifyCosmicHeartAppeared(this.level.getServer());
            }
            com.gugugaga233.dysoncubeprojectaddon.overhaul.UniverseEvents.notifyWrapComplete(
                    this.level.getServer(), dyson.getCurrentStarData(),
                    dyson.getTotalWrappedExact(), completedExact);
            for (ServerPlayer player : this.level.getServer().getPlayerList().getPlayers()) {
                if (player.getUUID().toString().equals(structureTargetId)) {
                    com.gugugaga233.dysoncubeprojectaddon.overhaul.PrimordialQiManager.tryTrigger(
                            player, dyson, dyson.getTotalWrappedExact(), completedExact);
                    break;
                }
            }
        }
        data.setDirty();
    }

    private static void syncGlobalProgress(DysonSphereProgressSavedData data,
                                           DysonSphereStructure dyson) {
        data.setTotalWrappedExact(dyson.getTotalWrappedExact());
        data.setCurrentStarData(dyson.getCurrentStarData());
        if (dyson.getTotalWrapped() >= Config.BLACKHOLE_UNLOCK_THRESHOLD) {
            data.setUniverseAwakened(true);
        }
        if (dyson.getTotalWrapped() >= Config.ANTIMATTER_UNLOCK_THRESHOLD) {
            data.setAntimatterAwakened(true);
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public void clientTick(Level level, BlockPos pos, BlockState state, EMRailEjectorBlockEntity blockEntity) {
        if (level instanceof ClientLevel) {
            ClientLevel clientLevel = (ClientLevel)level;
            if (this.progressBarComponent.getProgress() == 7) {
                Minecraft.getInstance().getSoundManager().play((SoundInstance)new SimpleSoundInstance((SoundEvent)DCPContent.Sounds.RAILGUN.get(), SoundSource.BLOCKS, 1.0f, 1.0f, level.getRandom(), (double)pos.getX(), (double)pos.getY(), (double)pos.getZ()));
            }
        }
    }

    public ItemInteractionResult onActivated(Player player, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ) {
        this.openGui(player);
        return ItemInteractionResult.SUCCESS;
    }

    public void openGui(Player player) {
        if (player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)player;
            sp.openMenu((MenuProvider)this, buffer -> LocatorFactory.writePacketBuffer((RegistryFriendlyByteBuf)buffer, (LocatorInstance)new TileEntityLocatorInstance(this.worldPosition)));
        }
    }

    public float getCurrentPitch() {
        return this.currentPitch;
    }

    public float getCurrentYaw() {
        return this.currentYaw;
    }

    @OnlyIn(value=Dist.CLIENT)
    @NotNull
    public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
        ArrayList<IFactory<? extends IScreenAddon>> list = new ArrayList<IFactory<? extends IScreenAddon>>();
        list.addAll(this.input.getScreenAddons());
        list.add(() -> new DysonProgressGuiAddon(this.dysonSphereId, 62, 24));
        list.add(() -> new SubscribeDysonGuiAddon(this.dysonSphereId, 9, 84));
        list.addAll(this.power.getScreenAddons());
        list.addAll(this.progressBarComponent.getScreenAddons());
        return list;
    }

    public IAssetProvider getAssetProvider() {
        return IAssetProvider.DEFAULT_PROVIDER;
    }

    @NotNull
    public List<IFactory<? extends IContainerAddon>> getContainerAddons() {
        ArrayList<IFactory<? extends IContainerAddon>> list = new ArrayList<IFactory<? extends IContainerAddon>>();
        list.addAll(this.progressBarComponent.getContainerAddons());
        list.addAll(this.input.getContainerAddons());
        list.addAll(this.power.getContainerAddons());
        return list;
    }

    public void handleButtonMessage(int i, Player player, CompoundTag compoundTag) {
    }

    @Nullable
    public AbstractContainerMenu createMenu(int menu, Inventory inventoryPlayer, Player entityPlayer) {
        return new BasicAddonContainer((Object)this, (LocatorInstance)new TileEntityLocatorInstance(this.worldPosition), this.getWorldPosCallable(), inventoryPlayer, menu);
    }

    @Nonnull
    public Component getDisplayName() {
        return Component.translatable((String)this.getBasicTileBlock().getDescriptionId()).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY));
    }

    public ContainerLevelAccess getWorldPosCallable() {
        return this.getLevel() != null ? ContainerLevelAccess.create((Level)this.getLevel(), (BlockPos)this.getBlockPos()) : ContainerLevelAccess.NULL;
    }

    public Level getComponentWorld() {
        return this.level;
    }

    public void markComponentForUpdate(boolean b) {
        this.markForUpdate();
    }

    public void markComponentDirty() {
        this.markForUpdate();
    }

    public ProgressBarComponent<EMRailEjectorBlockEntity> getProgressBarComponent() {
        return this.progressBarComponent;
    }

    public long getLastExecution() {
        return this.lastExecution;
    }

    public String getDysonSphereId() {
        return this.dysonSphereId;
    }

    public void setDysonSphereId(String dysonSphereId) {
        this.dysonSphereId = dysonSphereId;
    }

    public InventoryComponent<EMRailEjectorBlockEntity> getInput() {
        return this.input;
    }

    public EnergyStorageComponent<EMRailEjectorBlockEntity> getPower() {
        return this.power;
    }
}

