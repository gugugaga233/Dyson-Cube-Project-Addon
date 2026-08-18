package com.gugugaga233.dysoncubeprojectaddon.overhaul.api;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.MultiLevelEnergy;
import sonar.fluxnetworks.api.energy.BigNumber;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8;
import net.minecraft.nbt.CompoundTag;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

/**
 * DCPFluxAPI - 寰宇大道能量 API（适配 Flux-Networks 新 BigNumber 架构）
 */
public final class DCPFluxAPI {
    private DCPFluxAPI() {}

    // ==================== 工厂方法 ====================
    public static BigNumber of(long value)              { return BigNumber.valueOf(value); }
    public static BigNumber of(double value)            {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("BigNumber cannot represent NaN or infinity");
        }
        return new BigNumber(value);
    }
    public static BigNumber of(BigDecimal value)        { return BigNumber.fromBigDecimal(value); }
    public static BigNumber of(BigInteger value)        { return BigNumber.valueOf(value); }
    public static BigNumber zero()                      { return BigNumber.valueOf(0L); }
    public static BigNumber one()                       { return BigNumber.valueOf(1L); }
    /** 宇宙倍率 10^n */
    public static BigNumber universeMultiplier(int n)   {
        return n <= 0 ? one() : BigNumber.scientific("1", Integer.toString(n));
    }

    // ==================== 状态查询 ====================
    public static boolean isZero(BigNumber n)           { return n.isZero(); }
    public static boolean isExponentZero(BigNumber n)   { return n.isExponentZero(); }
    public static boolean isImmutable(BigNumber n)      { return n.isImmutable(); }
    public static int getStateLevel(BigNumber n)        { return n.getStateLevel(); }
    /** True only when the value overflows the legacy double projection. */
    public static boolean isInfinite(BigNumber n)       { return Double.isInfinite(toDouble(n)); }
    public static double toDouble(BigNumber n)          { return FluxMath8.toDoubleSaturated(n); }

    public static long getM(BigNumber n, int index)     { return n.getM(index); }
    public static long getK(BigNumber n, int index)     { return n.getK(index); }
    public static long getDigits(BigNumber n, int index){ return n.getDigits(index); }
    public static long getMaxCounter(BigNumber n, int index) { return n.getMaxCounter(index); }
    public static BigDecimal getCoefficient(BigNumber n){ return n.getCoefficient(); }
    public static BigInteger getEnergy(BigNumber n)     { return n.getEnergy(); }
    public static BigInteger getSaturated(BigNumber n){ return n.toBigIntegerSaturated(); }
    public static Optional<BigInteger> getExactEnergy(BigNumber n) { return n.getExactEnergy(); }
    public static BigInteger getExponentAsBigInt(BigNumber n){ return n.toBigIntegerExponent(); }

    // ==================== 运算 ====================
    public static BigNumber add(BigNumber a, BigNumber b)       { return a.add(b.deepCopy()); }
    public static BigNumber subtract(BigNumber a, BigNumber b)  { return a.subtract(b.deepCopy()); }
    public static BigNumber multiply(BigNumber a, BigNumber b)  { return a.multiply(b.deepCopy()); }
    public static BigNumber pow(BigNumber base, int exp)        { return base.pow(exp); }
    public static BigNumber deepCopy(BigNumber n)               { return n.deepCopy(); }
    public static int compare(BigNumber a, BigNumber b)         { return a.compareTo(b.deepCopy()); }
    public static void normalize(BigNumber n)                   { n.normalize(); }
    public static void incrementExponent(BigNumber n)           { n.incrementExponent(); }
    public static void decrementExponent(BigNumber n)           { n.decrementExponent(); }
    public static void setM(BigNumber n, int i, long v)         { n.setM(i, v); }
    public static void setK(BigNumber n, int i, long v)         { n.setK(i, v); }
    public static void setDigits(BigNumber n, int i, long v)    { n.setDigits(i, v); }
    public static void setMaxCounter(BigNumber n, int i, long v){ n.setMaxCounter(i, v); }
    public static boolean isWithinPrecision(BigNumber a, BigNumber b){ return BigNumber.isWithinPrecision(a, b); }

    // ==================== 常量 ====================
    public static int precisionThreshold()        { return BigNumber.PRECISION_THRESHOLD; }
    public static int digitsSize()                { return BigNumber.DIGITS_SIZE; }
    public static long digitMax()                 { return BigNumber.DIGIT_MAX; }
    public static BigInteger base()               { return BigNumber.BASE; }
    public static BigDecimal threshold1e300()     { return BigNumber.THRESHOLD_1E300; }
    public static BigInteger threshold1e300BI()   { return BigNumber.THRESHOLD_1E300_BI; }
    public static int maxK()                      { return BigNumber.MAX_K; }

    // ==================== 显示 ====================
    public static String toDisplayString(BigNumber n)    { return n.toDisplayString(); }
    public static String toScientificString(BigNumber n) { return n.toScientificString(); }

    // ==================== 序列化 ====================
    public static CompoundTag save(BigNumber n)         { return n.toTag(); }
    public static BigNumber load(CompoundTag tag)       { return BigNumber.fromTag(tag); }

    // ==================== 能量操作 ====================
    public static void addEnergy(BigNumber n, long amount)              { n.addEnergy(amount); }
    public static void addEnergy(BigNumber n, BigInteger amount)        { n.addEnergy(amount); }
    public static void addEnergy(BigNumber n, BigNumber amount)         { n.addEnergy(amount.deepCopy()); }
    public static BigNumber extract(BigNumber n, BigNumber max)         { return n.extract(max.deepCopy()); }
    public static BigNumber quote(BigNumber n, BigNumber max)           { return n.quote(max.deepCopy()); }
    public static boolean isEmpty(BigNumber n)                          { return n.isEmpty(); }
    public static long quoteChunk(BigNumber n, long max)                { return n.quoteChunk(max); }
    public static long extractChunk(BigNumber n, long max)              { return n.extractChunk(max); }

    // ==================== 多层能量 API ====================
    public static MultiLevelEnergy multiZero()                                      { return MultiLevelEnergy.zero(); }
    public static MultiLevelEnergy multiFromBigNumber(BigNumber n)                  { return MultiLevelEnergy.fromBigNumber(n); }
    public static MultiLevelEnergy multiFromBigInteger(BigInteger v)                { return MultiLevelEnergy.fromBigInteger(v); }
    public static BigInteger multiCapacity()                                        { return MultiLevelEnergy.CAPACITY; }
    public static void multiAdd(MultiLevelEnergy t, MultiLevelEnergy s)             { t.add(s); }
    public static void multiSubtract(MultiLevelEnergy t, MultiLevelEnergy s)        { t.subtract(s); }
    public static BigInteger multiExtract(MultiLevelEnergy e, BigInteger amount)    { return e.extract(amount); }
    public static int multiCompare(MultiLevelEnergy a, MultiLevelEnergy b)          { return a.compareTo(b); }
    public static boolean multiIsNegligibleComparedTo(MultiLevelEnergy a, MultiLevelEnergy b){
        return a.isNegligibleComparedTo(b); }
    public static int multiGetCurrentLevel(MultiLevelEnergy e)  { return e.getCurrentLevel(); }
    public static BigNumber multiGetValue(MultiLevelEnergy e)   { return e.getValue(); }
    public static BigInteger multiGetOverflowAt(MultiLevelEnergy e, int l) { return e.getOverflowAt(l); }
    public static boolean multiHasAtLeastLevel(MultiLevelEnergy e, int l)  { return e.hasAtLeastLevel(l); }
    public static boolean multiIsEmpty(MultiLevelEnergy e)      { return e.isEmpty(); }
    public static MultiLevelEnergy multiCopy(MultiLevelEnergy e){ return e.copy(); }
    public static void multiCopyFrom(MultiLevelEnergy t, MultiLevelEnergy s){ t.copyFrom(s); }
    public static CompoundTag multiSaveNBT(MultiLevelEnergy e)  { return e.saveNBT(); }
    public static void multiLoadNBT(MultiLevelEnergy e, CompoundTag tag){ e.loadNBT(tag); }
    public static String multiToDisplayString(MultiLevelEnergy e){ return e.toDisplayString(); }
}

