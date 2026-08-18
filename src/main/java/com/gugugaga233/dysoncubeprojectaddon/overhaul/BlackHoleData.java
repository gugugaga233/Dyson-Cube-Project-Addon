package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * 黑洞数据类（BlackHoleData）
 * <p>
 * 存储单个黑洞的完整状态，用于持久化到 {@link DysonSphereProgressSavedData}。
 * 每个黑洞由 (维度, 质量唯一标识) 作为唯一标识。
 */
public class BlackHoleData {

    /** 黑洞质量（太阳质量 M☉） */
    private double massMSun;
    /** 黑洞自旋参数（0~1） */
    private double spin;
    /** 视界放大器损坏度（0~100，float） */
    private float amplifierDamage;
    /** 视界放大器所在维度 */
    private ResourceKey<Level> amplifierDimension;
    /** 视界放大器位置 */
    private BlockPos amplifierPos;
    /** 吸积率（M☉/年，玩家可喂养提升） */
    private double accretionRate;

    public BlackHoleData(double massMSun, double spin) {
        this.massMSun = massMSun;
        this.spin = Math.max(0, Math.min(1, spin));
        this.amplifierDamage = 0;
        this.amplifierDimension = null;
        this.amplifierPos = null;
        this.accretionRate = 1e-8;
    }

    // ====== Getter / Setter ======

    public double getMassMSun() {
        return massMSun;
    }

    public void setMassMSun(double massMSun) {
        this.massMSun = massMSun;
    }

    public double getSpin() {
        return spin;
    }

    public void setSpin(double spin) {
        this.spin = Math.max(0, Math.min(1, spin));
    }

    public float getAmplifierDamage() {
        return amplifierDamage;
    }

    public void setAmplifierDamage(float damage) {
        this.amplifierDamage = Math.max(0, Math.min(100, damage));
    }

    public ResourceKey<Level> getAmplifierDimension() {
        return amplifierDimension;
    }

    public BlockPos getAmplifierPos() {
        return amplifierPos;
    }

    public void setAmplifierPos(ResourceKey<Level> dimension, BlockPos pos) {
        this.amplifierDimension = dimension;
        this.amplifierPos = pos;
    }

    public boolean hasAmplifier() {
        return amplifierPos != null && amplifierDimension != null;
    }

    public double getAccretionRate() {
        return accretionRate;
    }

    public void setAccretionRate(double accretionRate) {
        this.accretionRate = Math.max(1e-12, accretionRate);
    }

    /** 放大器是否已停机（损坏度 >= 100） */
    public boolean isAmplifierStopped() {
        return amplifierDamage >= 100;
    }

    // ====== NBT 序列化 ======

    private static final String TAG_MASS = "massMSun";
    private static final String TAG_SPIN = "spin";
    private static final String TAG_DAMAGE = "amplifierDamage";
    private static final String TAG_DIM = "amplifierDim";
    private static final String TAG_POS_X = "amplifierX";
    private static final String TAG_POS_Y = "amplifierY";
    private static final String TAG_POS_Z = "amplifierZ";
    private static final String TAG_ACCRETION = "accretionRate";

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble(TAG_MASS, massMSun);
        tag.putDouble(TAG_SPIN, spin);
        tag.putFloat(TAG_DAMAGE, amplifierDamage);
        tag.putDouble(TAG_ACCRETION, accretionRate);
        if (amplifierDimension != null && amplifierPos != null) {
            tag.putString(TAG_DIM, amplifierDimension.location().toString());
            tag.putInt(TAG_POS_X, amplifierPos.getX());
            tag.putInt(TAG_POS_Y, amplifierPos.getY());
            tag.putInt(TAG_POS_Z, amplifierPos.getZ());
        }
        return tag;
    }

    public static BlackHoleData deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        double mass = tag.getDouble(TAG_MASS);
        double spin = tag.getDouble(TAG_SPIN);
        BlackHoleData data = new BlackHoleData(mass, spin);
        data.amplifierDamage = tag.getFloat(TAG_DAMAGE);
        data.accretionRate = tag.getDouble(TAG_ACCRETION);
        if (tag.contains(TAG_DIM)) {
            data.amplifierDimension = ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    net.minecraft.resources.ResourceLocation.parse(tag.getString(TAG_DIM))
            );
            data.amplifierPos = new BlockPos(
                    tag.getInt(TAG_POS_X),
                    tag.getInt(TAG_POS_Y),
                    tag.getInt(TAG_POS_Z)
            );
        }
        return data;
    }
}
