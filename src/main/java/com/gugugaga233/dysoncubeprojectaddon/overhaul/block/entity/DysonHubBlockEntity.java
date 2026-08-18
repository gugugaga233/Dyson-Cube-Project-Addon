package com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity;

import com.gugugaga233.dysoncubeprojectaddon.DysonCubeProject;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientboundOpenDysonHubPacket;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.OverhaulContent;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.UniverseHierarchy;
import com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils;
import sonar.fluxnetworks.api.energy.BigNumber;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.StarData;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereProgressSavedData;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import sonar.fluxnetworks.api.energy.IBigNumberEnergyStorage;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;

/**
 * 戴森寰宇中枢方块实体
 * <p>
 * 每 tick 同步最新星体数据。右键后由客户端网络处理器打开中枢界面。
 */
public class DysonHubBlockEntity extends BlockEntity {

    private String dysonSphereId = "";
    private StarData currentStar = StarData.SUN;
    private double totalPower = 0;
    private long totalWrapped = 0L;
    private String totalWrappedExact = "0";
    private AbsoluteInteger totalWrappedValue = new AbsoluteInteger();
    private String hierarchyLevel = "SINGLE_STAR";
    private boolean universeAwakened = false;
    private AbsoluteInteger darkMatterResonance = new AbsoluteInteger();
    private AbsoluteInteger hierarchyEffectLayers = new AbsoluteInteger();
    private boolean cosmicHeartActive = false;
    private double cosmicHeartProgress = 0;
    private boolean cosmicHeartComplete = false;
    private String cosmicHeartBatchDisplay = "0";
    private String queuedCosmicHeartDisplay = "0";
    private String cosmicHeartBeamDisplay = "0 / 0";
    private String cosmicHeartSailDisplay = "0 / 0";
    private String interactionMode = "DYSON_SPHERE";
    private double antimatterProgress = 0;

    /** 聚合缓存快照；真实能量仍由各个 DysonSphereStructure 持有，避免重复记账。 */
    public BigNumber rawEnergy = new BigNumber(0);
    private BigNumber generatedEnergy = new BigNumber(0);
    private BigNumber storedEnergy = new BigNumber(0);
    private final IBigNumberEnergyStorage bigEnergyStorage = new HubBigEnergyStorage();
    private final IFNEnergyStorage fluxEnergyStorage = new HubFluxEnergyStorage();
    private final IEnergyStorage forgeEnergyStorage = new HubForgeEnergyStorage();

    public DysonHubBlockEntity(BlockPos pos, BlockState state) {
        super(OverhaulContent.getDysonHubBEType(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;
        if (level.getGameTime() % 20L != 0L) return;
        DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(level);
        if (data != null) {
            this.totalWrapped = data.getTotalWrapped();
            this.totalWrappedValue = data.getTotalWrappedExact();
            this.totalWrappedExact = this.totalWrappedValue.toCalculationString();
            this.currentStar = data.getCurrentStarData();
            this.universeAwakened = data.isUniverseAwakened();
            BigNumber totalGeneratedEnergy = new BigNumber(0);
            BigNumber totalStoredEnergy = new BigNumber(0);
            double maxDoublePower = 0;
            for (DysonSphereStructure s : data.getSpheres().values()) {
                totalGeneratedEnergy.addEnergy(s.getRawEnergy());
                totalStoredEnergy.addEnergy(s.getStoredEnergy());
                maxDoublePower = Math.max(maxDoublePower, s.getCachedTotalPowerFE());
            }
            generatedEnergy = totalGeneratedEnergy;
            storedEnergy = totalStoredEnergy;
            rawEnergy = totalStoredEnergy.deepCopy();
            this.totalPower = maxDoublePower;
            // 取所有球中最大共鸣值
            this.darkMatterResonance = data.getMaximumDarkMatterResonanceExact();
            this.hierarchyEffectLayers = data.getCosmicHeart().getHierarchyEffectLayersExact();
            // 同步寰宇之心状态
            this.cosmicHeartActive = data.getCosmicHeart().isActive();
            this.cosmicHeartProgress = data.getCosmicHeart().getProgress().doubleValue();
            this.cosmicHeartComplete = data.isCosmicHeartComplete();
            this.cosmicHeartBatchDisplay = data.getCosmicHeart().getBatchCountDisplay();
            this.queuedCosmicHeartDisplay = NumberUtils.getScientificInteger(
                    data.getQueuedCosmicHeartsExact());
            this.cosmicHeartBeamDisplay = data.getCosmicHeart().getBeamProgressDisplay();
            this.cosmicHeartSailDisplay = data.getCosmicHeart().getSolarPanelProgressDisplay();
            // 同步交互模式和反物质进度（取第一个球）
            this.hierarchyLevel = UniverseHierarchy.getUnlockedLevel(totalWrappedValue).name();
            DysonSphereStructure firstSphere = data.getSpheres().values().stream().findFirst().orElse(null);
            if (firstSphere != null) {
                this.interactionMode = firstSphere.getInteractionMode().name();
                if (firstSphere.isAntimatterStar() && firstSphere.getAntimatterData() != null) {
                    this.antimatterProgress = firstSphere.getAntimatterData().getCurrentProgress();
                }
            }
        }
    }

    public Component getDisplayName() {
        return Component.translatable("block.dysoncubeproject.dyson_hub");
    }

    public void openGui(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            DysonCubeProject.NETWORK.sendTo(new ClientboundOpenDysonHubPacket(
                    currentStar.serializeToString(),
                    totalPower,
                    totalWrapped,
                    totalWrappedExact,
                    hierarchyLevel,
                    NumberUtils.getScientificInteger(
                            UniverseHierarchy.effectiveBatchSize(
                                    totalWrappedValue, hierarchyEffectLayers)),
                    NumberUtils.getScientificInteger(
                            UniverseHierarchy.nextBatchThreshold(totalWrappedValue)),
                    UniverseHierarchy.isBatchSizeCapped(
                            totalWrappedValue, hierarchyEffectLayers),
                    universeAwakened,
                    darkMatterResonance.toCalculationString(),
                    cosmicHeartActive,
                    cosmicHeartProgress,
                    cosmicHeartComplete,
                    cosmicHeartBatchDisplay,
                    queuedCosmicHeartDisplay,
                    cosmicHeartBeamDisplay,
                    cosmicHeartSailDisplay,
                    getTotalPowerDisplay(),
                    getStoredEnergyDisplay(),
                    NumberUtils.getCompactBigNumberStorageCalculation(generatedEnergy),
                    NumberUtils.getCompactBigNumberStorageCalculation(storedEnergy)
            ), serverPlayer);
        }
    }

    // ====== Getters ======
    public StarData getCurrentStar() { return currentStar; }
    public double getTotalPower() { return totalPower; }
    public String getTotalPowerDisplay() {
        return NumberUtils.getCompactBigNumber(generatedEnergy);
    }
    public String getStoredEnergyDisplay() {
        return NumberUtils.getCompactBigNumber(storedEnergy);
    }
    public long getTotalWrapped() { return totalWrapped; }
    public boolean isUniverseAwakened() { return universeAwakened; }
    public AbsoluteInteger getDarkMatterResonanceExact() { return darkMatterResonance.copy(); }
    public boolean isCosmicHeartActive() { return cosmicHeartActive; }
    public double getCosmicHeartProgress() { return cosmicHeartProgress; }
    public String getInteractionMode() { return interactionMode; }
    public double getAntimatterProgress() { return antimatterProgress; }

    // ==================== 能量接口 ====================

    /** 获取寰宇能量 BigNumber */
    public BigNumber getRawEnergy() { return rawEnergy.deepCopy(); }

    public IBigNumberEnergyStorage getBigEnergyStorage() { return bigEnergyStorage; }
    public IFNEnergyStorage getFluxEnergyStorage() { return fluxEnergyStorage; }
    public IEnergyStorage getForgeEnergyStorage() { return forgeEnergyStorage; }

    /** 在所有星体缓存中按顺序抽取能量；模拟抽取不会修改星体数据。 */
    private BigNumber extractEnergyFromSpheres(BigNumber maximum, boolean simulate) {
        if (maximum == null || maximum.signum() <= 0 || level == null || level.isClientSide) {
            return new BigNumber(0);
        }
        DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(level);
        if (data == null) return new BigNumber(0);
        BigNumber extractedTotal = data.extractStoredEnergy(maximum, simulate);
        if (!simulate && !extractedTotal.isEmpty()) {
            this.storedEnergy.subtract(extractedTotal.deepCopy());
            this.rawEnergy = this.storedEnergy.deepCopy();
        }
        return extractedTotal;
    }

    private final class HubBigEnergyStorage implements IBigNumberEnergyStorage {
        @Override public BigNumber receiveEnergy(BigNumber maximum, boolean simulate) { return new BigNumber(0); }
        @Override public BigNumber extractEnergy(BigNumber maximum, boolean simulate) {
            return extractEnergyFromSpheres(maximum, simulate);
        }
        @Override public BigNumber getEnergyStored() {
            return storedEnergy.deepCopy();
        }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return false; }
    }

    private final class HubFluxEnergyStorage implements IFNEnergyStorage {
        @Override public long receiveEnergyL(long maximum, boolean simulate) { return 0; }
        @Override public long extractEnergyL(long maximum, boolean simulate) {
            return extractEnergyFromSpheres(BigNumber.valueOf(maximum), simulate).getEnergyStoredLong();
        }
        @Override public long getEnergyStoredL() { return getBigEnergyStorage().getEnergyStored().getEnergyStoredLong(); }
        @Override public long getMaxEnergyStoredL() { return Long.MAX_VALUE; }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return false; }
    }

    private final class HubForgeEnergyStorage implements IEnergyStorage {
        @Override public int receiveEnergy(int maximum, boolean simulate) { return 0; }
        @Override public int extractEnergy(int maximum, boolean simulate) {
            long extracted = extractEnergyFromSpheres(BigNumber.valueOf(maximum), simulate).getEnergyStoredLong();
            return (int) Math.min(Integer.MAX_VALUE, extracted);
        }
        @Override public int getEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, getBigEnergyStorage().getEnergyStored().getEnergyStoredLong());
        }
        @Override public int getMaxEnergyStored() { return Integer.MAX_VALUE; }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return false; }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putString("dysonSphereId", dysonSphereId);
        tag.put("energy", storedEnergy.toTag());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.dysonSphereId = tag.getString("dysonSphereId");
        if (tag.contains("energy")) {
            storedEnergy = BigNumber.fromTag(tag.getCompound("energy"));
            if (storedEnergy.isImmutable()) storedEnergy = storedEnergy.deepCopy();
            rawEnergy = storedEnergy.deepCopy();
        }
    }
}

