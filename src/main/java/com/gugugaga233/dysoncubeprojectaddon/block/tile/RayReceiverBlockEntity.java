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
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.neoforged.neoforge.capabilities.Capabilities$EnergyStorage
 *  net.neoforged.neoforge.energy.IEnergyStorage
 *  org.jetbrains.annotations.NotNull
 */
package com.gugugaga233.dysoncubeprojectaddon.block.tile;

import com.gugugaga233.dysoncubeprojectaddon.Config;
import com.gugugaga233.dysoncubeprojectaddon.DCPContent;
import com.gugugaga233.dysoncubeprojectaddon.client.gui.DysonProgressGuiAddon;
import com.gugugaga233.dysoncubeprojectaddon.client.gui.SubscribeDysonGuiAddon;
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
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import sonar.fluxnetworks.api.energy.BigNumber;
import sonar.fluxnetworks.api.energy.IBigNumberEnergyStorage;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;
import sonar.fluxnetworks.api.FluxCapabilities;

public class RayReceiverBlockEntity
extends BasicTile<RayReceiverBlockEntity>
implements IScreenAddonProvider,
ITickableBlockEntity<RayReceiverBlockEntity>,
MenuProvider,
IButtonHandler,
IContainerAddonProvider,
IHasAssetProvider,
IComponentHarness {
    @Save
    private String dysonSphereId = "";
    @Save
    private EnergyStorageComponent<RayReceiverBlockEntity> energyStorageComponent = new EnergyStorageComponent(Config.RAY_RECEIVER_POWER_BUFFER, 0, Integer.MAX_VALUE, 19, 22);
    @Save
    private float currentPitch = 270.0f;
    private BigNumber bigEnergyBuffer = new BigNumber(0);
    private final IBigNumberEnergyStorage bigEnergyStorage = new BigEnergyStorage();
    private final IFNEnergyStorage fluxEnergyStorage = new FluxEnergyStorage();
    private final IEnergyStorage forgeEnergyStorage = new ForgeEnergyStorage();

    public RayReceiverBlockEntity(BasicTileBlock<RayReceiverBlockEntity> base, BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType, pos, state);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state, RayReceiverBlockEntity blockEntity) {
        float targetPitch;
        if (level.isDay() && !level.isRaining() && level.canSeeSky(pos.above())) {
            DysonSphereProgressSavedData dyson = DysonSphereProgressSavedData.get(level);
            DysonSphereStructure sphere = dyson.getSpheres()
                    .computeIfAbsent(this.dysonSphereId, s -> new DysonSphereStructure());
            BigNumber extracted = sphere.extractEnergy(sphere.getStoredEnergy(), false);
            this.bigEnergyBuffer.addEnergy(extracted);
        }
        pushEnergy(level, pos, state);
        this.energyStorageComponent.setEnergyStored(
                (int) Math.min(Integer.MAX_VALUE, this.bigEnergyBuffer.getEnergyStoredLong()));
        if ((targetPitch = level.getTimeOfDay(1.0f) * 360.0f) >= 90.0f && targetPitch <= 270.0f) {
            targetPitch = 270.0f;
        }
        if (this.currentPitch % 360.0f <= targetPitch) {
            this.currentPitch = Math.min((this.currentPitch + 1.0f) % 360.0f, targetPitch);
        } else if (this.currentPitch > targetPitch) {
            this.currentPitch = Math.max(this.currentPitch - 1.0f, targetPitch);
        }
        this.syncObject(Float.valueOf(this.currentPitch));
    }

    private void pushEnergy(Level level, BlockPos pos, BlockState state) {
        BlockPos targetPos = pos.below();
        BlockEntity targetEntity = level.getBlockEntity(targetPos);
        IBigNumberEnergyStorage bigCapability = level.getCapability(
                FluxCapabilities.BIG_NUMBER_BLOCK, targetPos, level.getBlockState(targetPos), targetEntity, Direction.UP);
        if (bigCapability != null && bigCapability.canReceive()) {
            BigNumber offered = bigEnergyBuffer.deepCopy();
            BigNumber accepted = bigCapability.receiveEnergy(offered.deepCopy(), true);
            if (accepted != null && !accepted.isEmpty()) {
                BigNumber extracted = bigEnergyBuffer.extractContainer(accepted.deepCopy());
                if (!extracted.isEmpty()) {
                    BigNumber committed = bigCapability.receiveEnergy(extracted.deepCopy(), false);
                    BigNumber committedClamped = committed == null || committed.signum() <= 0
                            ? new BigNumber(0)
                            : extracted.quote(committed.deepCopy());
                    BigNumber refund = extracted.deepCopy().subtract(committedClamped);
                    if (!refund.isEmpty()) bigEnergyBuffer.addEnergy(refund);
                }
            }
            return;
        }

        IFNEnergyStorage fluxCapability = level.getCapability(
                FluxCapabilities.BLOCK, targetPos, level.getBlockState(targetPos), targetEntity, Direction.UP);
        if (fluxCapability != null && fluxCapability.canReceive()) {
            long offered = bigEnergyBuffer.quoteChunk(Config.RAY_RECEIVER_EXTRACT_POWER);
            long accepted = fluxCapability.receiveEnergyL(offered, true);
            if (accepted > 0) {
                long extracted = bigEnergyBuffer.extractChunk(accepted);
                if (extracted > 0) {
                    long committed = Math.max(0, Math.min(extracted,
                            fluxCapability.receiveEnergyL(extracted, false)));
                    if (committed < extracted) bigEnergyBuffer.addEnergy(extracted - committed);
                }
            }
            return;
        }

        IEnergyStorage forgeCapability = level.getCapability(
                Capabilities.EnergyStorage.BLOCK, targetPos, level.getBlockState(targetPos), targetEntity, Direction.UP);
        if (forgeCapability != null && forgeCapability.canReceive()) {
            int offered = (int) Math.min(Integer.MAX_VALUE,
                    bigEnergyBuffer.quoteChunk(Config.RAY_RECEIVER_EXTRACT_POWER));
            int accepted = forgeCapability.receiveEnergy(offered, true);
            if (accepted > 0) {
                long extracted = bigEnergyBuffer.extractChunk(accepted);
                if (extracted > 0) {
                    int committed = Math.max(0, Math.min((int) extracted,
                            forgeCapability.receiveEnergy((int) extracted, false)));
                    if (committed < extracted) bigEnergyBuffer.addEnergy(extracted - committed);
                }
            }
        }
    }

    private final class BigEnergyStorage implements IBigNumberEnergyStorage {
        @Override
        public BigNumber receiveEnergy(BigNumber maximum, boolean simulate) {
            if (maximum == null || maximum.signum() <= 0) return new BigNumber(0);
            BigNumber accepted = maximum.deepCopy();
            if (!simulate) bigEnergyBuffer.addEnergy(accepted.deepCopy());
            return accepted;
        }

        @Override
        public BigNumber extractEnergy(BigNumber maximum, boolean simulate) {
            if (maximum == null || maximum.signum() <= 0) return new BigNumber(0);
            return simulate ? bigEnergyBuffer.quoteContainer(maximum.deepCopy())
                    : bigEnergyBuffer.extractContainer(maximum.deepCopy());
        }

        @Override public BigNumber getEnergyStored() { return bigEnergyBuffer.deepCopy(); }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return true; }
    }

    private final class FluxEnergyStorage implements IFNEnergyStorage {
        @Override public long receiveEnergyL(long maximum, boolean simulate) {
            if (maximum <= 0) return 0;
            if (!simulate) bigEnergyBuffer.addEnergy(maximum);
            return maximum;
        }
        @Override public long extractEnergyL(long maximum, boolean simulate) {
            return simulate ? bigEnergyBuffer.quoteChunk(maximum) : bigEnergyBuffer.extractChunk(maximum);
        }
        @Override public long getEnergyStoredL() { return bigEnergyBuffer.getEnergyStoredLong(); }
        @Override public long getMaxEnergyStoredL() { return Long.MAX_VALUE; }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return true; }
    }

    private final class ForgeEnergyStorage implements IEnergyStorage {
        @Override public int receiveEnergy(int maximum, boolean simulate) {
            if (maximum <= 0) return 0;
            if (!simulate) bigEnergyBuffer.addEnergy(maximum);
            return maximum;
        }
        @Override public int extractEnergy(int maximum, boolean simulate) {
            long amount = simulate ? bigEnergyBuffer.quoteChunk(maximum) : bigEnergyBuffer.extractChunk(maximum);
            return (int) Math.min(Integer.MAX_VALUE, amount);
        }
        @Override public int getEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, bigEnergyBuffer.getEnergyStoredLong());
        }
        @Override public int getMaxEnergyStored() { return Integer.MAX_VALUE; }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return true; }
    }

    @OnlyIn(value=Dist.CLIENT)
    public void clientTick(Level level, BlockPos pos, BlockState state, RayReceiverBlockEntity blockEntity) {
        if (level instanceof ClientLevel) {
            ClientLevel clientLevel = (ClientLevel)level;
            if ((level.getGameTime() + pos.asLong()) % 340L == 0L && level.dayTime() % 24000L < 12000L && !level.isRaining()) {
                Minecraft.getInstance().getSoundManager().play((SoundInstance)new SimpleSoundInstance((SoundEvent)DCPContent.Sounds.RAY.get(), SoundSource.BLOCKS, 0.5f, 1.0f, level.getRandom(), (double)pos.getX(), (double)pos.getY(), (double)pos.getZ()));
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

    @OnlyIn(value=Dist.CLIENT)
    @NotNull
    public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
        ArrayList<IFactory<? extends IScreenAddon>> list = new ArrayList<IFactory<? extends IScreenAddon>>();
        list.addAll(this.energyStorageComponent.getScreenAddons());
        list.add(() -> new DysonProgressGuiAddon(this.dysonSphereId, 56, 24));
        list.add(() -> new SubscribeDysonGuiAddon(this.dysonSphereId, 9, 84));
        return list;
    }

    public IAssetProvider getAssetProvider() {
        return IAssetProvider.DEFAULT_PROVIDER;
    }

    @NotNull
    public List<IFactory<? extends IContainerAddon>> getContainerAddons() {
        ArrayList<IFactory<? extends IContainerAddon>> list = new ArrayList<IFactory<? extends IContainerAddon>>();
        list.addAll(this.energyStorageComponent.getContainerAddons());
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

    public String getDysonSphereId() {
        return this.dysonSphereId;
    }

    public void setDysonSphereId(String dysonSphereId) {
        this.dysonSphereId = dysonSphereId;
    }

    public EnergyStorageComponent<RayReceiverBlockEntity> getEnergyStorageComponent() {
        return this.energyStorageComponent;
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
        tag.put("bigEnergyBuffer", bigEnergyBuffer.toTag());
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("bigEnergyBuffer")) {
            bigEnergyBuffer = BigNumber.fromTag(tag.getCompound("bigEnergyBuffer"));
            if (bigEnergyBuffer.isImmutable()) bigEnergyBuffer = bigEnergyBuffer.deepCopy();
        } else {
            bigEnergyBuffer = BigNumber.valueOf(energyStorageComponent.getEnergyStored());
        }
    }

    public float getCurrentPitch() {
        return this.currentPitch;
    }
}

