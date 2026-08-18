package com.gugugaga233.dysoncubeprojectaddon.world;

import com.gugugaga233.dysoncubeprojectaddon.Config;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.BlackHoleData;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.CosmicHeart;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.StarData;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.MiningTargetData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;
import sonar.fluxnetworks.api.energy.BigNumber;

/**
 * 戴森球进度存档（魔改版 — 唯一存档入口）
 * <p>
 * 原模组：存储 spheres（Map<String, DysonSphereStructure>）和 subscribedPlayers。
 * <p>
 * 魔改合并：所有新增字段（totalWrapped、currentStarData、黑洞数据、宇宙觉醒标志）
 * 统一合并到此存档类中，避免双存档不一致。
 * <p>
 * 根据已确认的数据类型策略：totalWrapped 用 int，黑洞数据用 Map。
 */
public class DysonSphereProgressSavedData
extends SavedData {
    public static final String ID = "dyson_sphere_progress_addon";
    private HashMap<String, DysonSphereStructure> spheres = new HashMap<>();
    private HashMap<String, String> subscribedPlayers = new HashMap<>();
    /** Runtime-only viewers. Persistent sphere selection must not imply continuous network sync. */
    private final HashMap<String, String> activeSyncSubscribers = new HashMap<>();

    /** Monotonically increasing state revision used to avoid rebuilding identical client snapshots. */
    private long clientSyncRevision;
    private long cachedClientSyncRevision = Long.MIN_VALUE;
    private final HashMap<String, CompoundTag> cachedClientSyncTags = new HashMap<>();

    // ====== 魔改新增全局字段 ======
    /** 全局已包裹星体总数（所有戴森球共享） */
    private AbsoluteInteger totalWrapped = new AbsoluteInteger();
    /** 当前星体数据（首个默认太阳） */
    private StarData currentStarData = StarData.SUN;
    /** 宇宙觉醒标志（totalWrapped >= 1000 时触发） */
    private boolean universeAwakened = false;
    /** 反物质觉醒标志（totalWrapped >= 5000 时触发） */
    private boolean antimatterAwakened = false;
    /** 黑洞数据映射（UUID -> BlackHoleData，每个黑洞独立） */
    private HashMap<UUID, BlackHoleData> blackHoleData = new HashMap<>();
    /** 寰宇之心覆盖进度 */
    private CosmicHeart cosmicHeart = new CosmicHeart();
    private HashMap<String, MiningTargetData> miningTargets = new HashMap<>();

    // ====== 单例获取 ======

    public static DysonSphereProgressSavedData get(Level level) {
        if (level == null) return null;
        // 无论当前维度，始终从 Overworld 读取存档（全局共享）
        if (level.getServer() == null) return null;
        ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return null;
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        DysonSphereProgressSavedData::new,
                        (compoundTag, provider) -> DysonSphereProgressSavedData.load(provider, compoundTag)
                ),
                ID
        );
    }

    /** 供其他维度直接获取 Overworld 存档的便捷方法 */
    public static DysonSphereProgressSavedData getFromOverworld(ServerLevel overworld) {
        if (overworld == null) return null;
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        DysonSphereProgressSavedData::new,
                        (compoundTag, provider) -> DysonSphereProgressSavedData.load(provider, compoundTag)
                ),
                ID
        );
    }

    // ====== 加载/保存 ======

    public static DysonSphereProgressSavedData load(HolderLookup.Provider provider, CompoundTag compoundTag) {
        DysonSphereProgressSavedData data = new DysonSphereProgressSavedData();

        // 原模组字段
        CompoundTag spheresTag = compoundTag.getCompound("spheres");
        for (String key : spheresTag.getAllKeys()) {
            data.spheres.put(key, new DysonSphereStructure());
            data.spheres.get(key).deserializeNBT(provider, spheresTag.getCompound(key));
        }
        CompoundTag subscribedTag = compoundTag.getCompound("subscribedPlayers");
        for (String key : subscribedTag.getAllKeys()) {
            data.subscribedPlayers.put(key, subscribedTag.getString(key));
        }

        // 魔改新增字段
        data.totalWrapped = compoundTag.contains("totalWrappedExact", Tag.TAG_COMPOUND)
                ? FluxMath8.fromCompactTag(compoundTag.getCompound("totalWrappedExact"))
                : absolute(compoundTag.getLong("totalWrapped"));
        data.currentStarData = StarData.deserializeFromString(compoundTag.getString("currentStarData"));
        data.universeAwakened = compoundTag.getBoolean("universeAwakened");
        data.antimatterAwakened = compoundTag.getBoolean("antimatterAwakened");

        // 加载黑洞数据
        CompoundTag bhTag = compoundTag.getCompound("blackHoleData");
        if (bhTag != null) {
            for (String key : bhTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    data.blackHoleData.put(uuid, BlackHoleData.deserializeNBT(provider, bhTag.getCompound(key)));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        // 加载寰宇之心
        if (compoundTag.contains("cosmicHeartData", Tag.TAG_COMPOUND)) {
            data.cosmicHeart = CosmicHeart.deserializeNBT(compoundTag.getCompound("cosmicHeartData"));
        } else {
            String cosmicHeartStr = compoundTag.getString("cosmicHeart");
            if (!cosmicHeartStr.isEmpty()) {
                data.cosmicHeart = CosmicHeart.deserializeFromString(cosmicHeartStr);
            }
        }

        CompoundTag miningTag = compoundTag.getCompound("cosmicMiningTargets");
        for (String key : miningTag.getAllKeys()) {
            MiningTargetData target = MiningTargetData.load(miningTag.getCompound(key));
            if (target.hasMiningStarted()) data.miningTargets.put(key, target);
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        return saveData(compoundTag, provider, true, null);
    }

    /** Client rendering does not use mining targets; omit their potentially huge exact mass data. */
    public CompoundTag saveForClientSync(CompoundTag compoundTag, HolderLookup.Provider provider) {
        return saveData(compoundTag, provider, false, null);
    }

    /** Serializes only the sphere currently viewed by the receiving player. */
    public CompoundTag saveForClientSync(CompoundTag compoundTag, HolderLookup.Provider provider,
                                         String sphereId) {
        return saveData(compoundTag, provider, false, sphereId);
    }

    /**
     * Returns a snapshot that is encoded at most once per changed SavedData state and viewed sphere.
     * The returned tag is treated as immutable by the networking path.
     */
    public CompoundTag getCachedClientSyncTag(HolderLookup.Provider provider) {
        return getCachedClientSyncTag(provider, null);
    }

    public CompoundTag getCachedClientSyncTag(HolderLookup.Provider provider, String sphereId) {
        if (cachedClientSyncRevision != clientSyncRevision) {
            cachedClientSyncTags.clear();
            cachedClientSyncRevision = clientSyncRevision;
        }
        String cacheKey = sphereId == null ? "" : sphereId;
        return cachedClientSyncTags.computeIfAbsent(cacheKey,
                key -> saveForClientSync(new CompoundTag(), provider, sphereId));
    }

    @Override
    public void setDirty() {
        super.setDirty();
        clientSyncRevision++;
        cachedClientSyncTags.clear();
    }

    private CompoundTag saveData(CompoundTag compoundTag, HolderLookup.Provider provider,
                                 boolean includeMiningTargets, String sphereId) {
        // 原模组字段
        CompoundTag spheresTag = new CompoundTag();
        if (sphereId == null) {
            for (String key : this.spheres.keySet()) {
                spheresTag.put(key, this.spheres.get(key).serializeNBT(provider));
            }
        } else {
            DysonSphereStructure sphere = this.spheres.get(sphereId);
            if (sphere != null) spheresTag.put(sphereId, sphere.serializeNBT(provider));
        }
        CompoundTag subscribedTag = new CompoundTag();
        for (String key : this.subscribedPlayers.keySet()) {
            subscribedTag.putString(key, this.subscribedPlayers.get(key));
        }
        compoundTag.put("spheres", spheresTag);
        compoundTag.put("subscribedPlayers", subscribedTag);

        // 魔改新增字段
        compoundTag.putLong("totalWrapped", getTotalWrapped());
        compoundTag.put("totalWrappedExact", FluxMath8.toCompactTag(this.totalWrapped));
        if (this.currentStarData != null) {
            compoundTag.putString("currentStarData", this.currentStarData.serializeToString());
        }
        compoundTag.putBoolean("universeAwakened", this.universeAwakened);
        compoundTag.putBoolean("antimatterAwakened", this.antimatterAwakened);

        // 保存黑洞数据
        CompoundTag bhTag = new CompoundTag();
        for (Map.Entry<UUID, BlackHoleData> entry : this.blackHoleData.entrySet()) {
            bhTag.put(entry.getKey().toString(), entry.getValue().serializeNBT(provider));
        }
        compoundTag.put("blackHoleData", bhTag);

        // 保存寰宇之心
        compoundTag.putString("cosmicHeart", this.cosmicHeart.serializeToString());
        compoundTag.put("cosmicHeartData", this.cosmicHeart.serializeNBT());

        if (includeMiningTargets) {
            CompoundTag miningTag = new CompoundTag();
            for (Map.Entry<String, MiningTargetData> entry : miningTargets.entrySet()) {
                if (entry.getValue().hasMiningStarted()) {
                    miningTag.put(entry.getKey(), entry.getValue().save());
                }
            }
            compoundTag.put("cosmicMiningTargets", miningTag);
        }

        return compoundTag;
    }

    // ====== Getter/Setter ======

    public HashMap<String, DysonSphereStructure> getSpheres() { return this.spheres; }
    public HashMap<String, String> getSubscribedPlayers() { return this.subscribedPlayers; }
    public HashMap<String, String> getActiveSyncSubscribers() { return this.activeSyncSubscribers; }
    public String getSubscribedFor(String playerUUID) { return this.subscribedPlayers.getOrDefault(playerUUID, playerUUID); }

    public void subscribeForClientSync(String playerUUID, String sphereId) {
        if (playerUUID != null && sphereId != null) activeSyncSubscribers.put(playerUUID, sphereId);
    }

    public void unsubscribeFromClientSync(String playerUUID) {
        if (playerUUID != null) activeSyncSubscribers.remove(playerUUID);
    }

    /** Returns the single shared Dyson energy cache used by every hub and integration port. */
    public BigNumber getStoredEnergy() {
        BigNumber total = new BigNumber(0);
        for (DysonSphereStructure sphere : spheres.values()) {
            total.addEnergy(sphere.getStoredEnergy());
        }
        return total;
    }

    /** Extracts from the shared cache without creating a second energy balance. */
    public BigNumber extractStoredEnergy(BigNumber maximum, boolean simulate) {
        if (maximum == null || maximum.signum() <= 0) return new BigNumber(0);

        BigNumber remaining = maximum.deepCopy();
        BigNumber extractedTotal = new BigNumber(0);
        for (DysonSphereStructure sphere : spheres.values()) {
            if (remaining.isEmpty()) break;
            BigNumber extracted = sphere.extractEnergy(remaining, simulate);
            if (extracted == null || extracted.signum() <= 0) continue;
            extractedTotal.addEnergy(extracted.deepCopy());
            remaining.subtract(extracted.deepCopy());
        }
        if (!simulate && !extractedTotal.isEmpty()) setDirty();
        return extractedTotal;
    }

    // 魔改新增

    public long getTotalWrapped() { return FluxMath8.toLongSaturated(totalWrapped); }
    public AbsoluteInteger getTotalWrappedExact() { return totalWrapped.copy(); }
    public void setTotalWrapped(long totalWrapped) {
        setTotalWrappedExact(absolute(totalWrapped));
    }
    public void setTotalWrappedExact(AbsoluteInteger totalWrapped) {
        this.totalWrapped = totalWrapped == null ? new AbsoluteInteger() : totalWrapped.copy();
        setDirty();
    }

    public StarData getCurrentStarData() { return currentStarData; }
    public void setCurrentStarData(StarData data) {
        this.currentStarData = data != null ? data : StarData.SUN;
        setDirty();
    }

    public boolean isUniverseAwakened() { return universeAwakened; }
    public void setUniverseAwakened(boolean awakened) {
        this.universeAwakened = awakened;
        setDirty();
    }

    public boolean isAntimatterAwakened() { return antimatterAwakened; }
    public void setAntimatterAwakened(boolean awakened) {
        this.antimatterAwakened = awakened;
        setDirty();
    }

    public HashMap<UUID, BlackHoleData> getBlackHoleData() { return blackHoleData; }

    public CosmicHeart getCosmicHeart() { return cosmicHeart; }
    public void setCosmicHeart(CosmicHeart heart) { this.cosmicHeart = heart; setDirty(); }

    /** Applies reward side effects that live outside the Cosmic Heart counter itself. */
    public void applyCosmicHeartReward(CosmicHeart.RuleModification reward) {
        if (reward == null) return;
        if (reward == CosmicHeart.RuleModification.TOTAL_WRAPPED_PLUS_1M) {
            AbsoluteInteger increase = absolute(1_000_000L);
            FluxMath8.addInPlace(this.totalWrapped, increase);
            for (DysonSphereStructure sphere : this.spheres.values()) {
                AbsoluteInteger sphereWrapped = sphere.getTotalWrappedExact();
                FluxMath8.addInPlace(sphereWrapped, increase);
                sphere.setTotalWrappedExact(sphereWrapped);
            }
            this.universeAwakened = this.totalWrapped.compareTo(
                    absolute(Config.BLACKHOLE_UNLOCK_THRESHOLD)) >= 0;
            this.antimatterAwakened = this.totalWrapped.compareTo(
                    absolute(Config.ANTIMATTER_UNLOCK_THRESHOLD)) >= 0;
        }
        synchronizeCosmicHeartEffects();
        setDirty();
    }

    /** Applies all side effects from one aggregate Cosmic Heart reward settlement. */
    public void applyCosmicHeartRewardBatch(AbsoluteInteger[] claimed) {
        if (claimed == null || claimed.length != CosmicHeart.RuleModification.values().length) return;
        AbsoluteInteger wrappedReward = claimed[
                CosmicHeart.RuleModification.TOTAL_WRAPPED_PLUS_1M.ordinal()];
        if (wrappedReward != null && !wrappedReward.isZero()) {
            AbsoluteInteger increase = FluxMath8.multiply(wrappedReward, 1_000_000L);
            FluxMath8.addInPlace(this.totalWrapped, increase);
            for (DysonSphereStructure sphere : this.spheres.values()) {
                AbsoluteInteger sphereWrapped = sphere.getTotalWrappedExact();
                FluxMath8.addInPlace(sphereWrapped, increase);
                sphere.setTotalWrappedExact(sphereWrapped);
            }
            this.universeAwakened = this.totalWrapped.compareTo(
                    absolute(Config.BLACKHOLE_UNLOCK_THRESHOLD)) >= 0;
            this.antimatterAwakened = this.totalWrapped.compareTo(
                    absolute(Config.ANTIMATTER_UNLOCK_THRESHOLD)) >= 0;
        }
        synchronizeCosmicHeartEffects();
        setDirty();
    }

    public AbsoluteInteger getQueuedCosmicHeartsExact() {
        AbsoluteInteger total = new AbsoluteInteger();
        for (DysonSphereStructure sphere : this.spheres.values()) {
            FluxMath8.addInPlace(total, sphere.getPendingCosmicHeartsExact());
        }
        return total;
    }

    public void synchronizeCosmicHeartEffects() {
        AbsoluteInteger outputLayers = this.cosmicHeart.getModificationCountExact(
                CosmicHeart.RuleModification.ALL_STAR_OUTPUT_X2);
        for (DysonSphereStructure sphere : this.spheres.values()) {
            sphere.setCosmicHeartOutputLayers(outputLayers);
        }
    }

    /** Routes structure input to the sphere that currently owns the active Cosmic Heart. */
    public String resolveStructureTargetSphereId(String configuredSphereId) {
        String configured = configuredSphereId == null ? "" : configuredSphereId;
        if (!this.cosmicHeart.isActive()) return configured;
        String heartTarget = this.cosmicHeart.getTargetSphereId();
        return heartTarget == null || heartTarget.isEmpty() ? configured : heartTarget;
    }

    /** Returns the exact maximum resonance without projecting any sphere through int or long. */
    public AbsoluteInteger getMaximumDarkMatterResonanceExact() {
        AbsoluteInteger maximum = new AbsoluteInteger();
        for (DysonSphereStructure sphere : this.spheres.values()) {
            AbsoluteInteger candidate = sphere.getDarkMatterResonanceExact();
            if (candidate.compareTo(maximum) > 0) maximum = candidate;
        }
        return maximum;
    }

    public MiningTargetData getMiningTarget(String key) {
        return miningTargets.get(key);
    }

    public MiningTargetData persistMiningTarget(String key, MiningTargetData target) {
        MiningTargetData existing = miningTargets.get(key);
        if (existing != null) return existing;
        if (target == null || !target.hasMiningStarted()) return target;
        miningTargets.put(key, target);
        setDirty();
        return target;
    }

    int miningTargetCount() {
        return miningTargets.size();
    }

    public void removeMiningTarget(String key) {
        if (miningTargets.remove(key) != null) setDirty();
    }

    public void removeMiningTargetsExcept(String keyPrefix, String retainedKey) {
        boolean removed = miningTargets.keySet().removeIf(key -> key.startsWith(keyPrefix)
                && !key.equals(retainedKey));
        if (removed) setDirty();
    }

    /** 寰宇之心是否已激活 */
    public boolean isCosmicHeartActive() { return cosmicHeart.isActive(); }

    /** 寰宇之心是否已完成覆盖 */
    public boolean isCosmicHeartComplete() { return cosmicHeart.isComplete(); }

    /** 寰宇之心覆盖进度（0~100） */
    public double getCosmicHeartProgress() { return cosmicHeart.getProgress().doubleValue(); }

    /** 寰宇之心覆盖面积显示 */
    public String getCosmicHeartAreaDisplay() { return cosmicHeart.getCoveredAreaDisplay(); }

    /** 寰宇之心产出倍率 */
    public double getCosmicHeartOutputMultiplier() { return cosmicHeart.getOutputMultiplier(); }

    public BlackHoleData getOrCreateBlackHole(UUID uuid, double massMSun, double spin) {
        return blackHoleData.computeIfAbsent(uuid, k -> {
            BlackHoleData data = new BlackHoleData(massMSun, spin);
            setDirty();
            return data;
        });
    }

/**
     * 递增 totalWrapped 并返回新值。
     * 当达到 BLACKHOLE_UNLOCK_THRESHOLD 时自动设置 universeAwakened 标志。
     * 当达到 ANTIMATTER_UNLOCK_THRESHOLD 时自动设置 antimatterAwakened 标志。
     */
    public long incrementWrapped() {
        this.totalWrapped.increment();
        if (this.totalWrapped.compareTo(absolute(com.gugugaga233.dysoncubeprojectaddon.Config.BLACKHOLE_UNLOCK_THRESHOLD)) >= 0 && !this.universeAwakened) {
            this.universeAwakened = true;
        }
        if (this.totalWrapped.compareTo(absolute(com.gugugaga233.dysoncubeprojectaddon.overhaul.UniverseRandomizer.ANTIMATTER_UNLOCK_THRESHOLD)) >= 0 && !this.antimatterAwakened) {
            this.antimatterAwakened = true;
        }
        setDirty();
        return getTotalWrapped();
    }

    private static AbsoluteInteger absolute(long value) {
        return AbsoluteInteger.parse(Long.toString(Math.max(0L, value)));
    }
}

