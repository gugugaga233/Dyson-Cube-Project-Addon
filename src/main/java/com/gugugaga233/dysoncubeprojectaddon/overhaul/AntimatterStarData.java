package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import com.gugugaga233.dysoncubeprojectaddon.DCPContent;
import com.gugugaga233.dysoncubeprojectaddon.item.CompressedItem;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import sonar.fluxnetworks.api.energy.BigNumber;

public class AntimatterStarData {
    public static final BigDecimal C_SQUARED = new BigDecimal("8.9875E16");
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final MathContext CALCULATION_CONTEXT = MathContext.DECIMAL128;

    private static final Map<Item, BigDecimal> ITEM_MASS_TABLE = new HashMap<>();
    static {
        ITEM_MASS_TABLE.put(Items.IRON_BLOCK, new BigDecimal("0.015625"));
        ITEM_MASS_TABLE.put(Items.GOLD_BLOCK, new BigDecimal("0.0275"));
        ITEM_MASS_TABLE.put(Items.NETHERITE_BLOCK, new BigDecimal("0.0390625"));
        ITEM_MASS_TABLE.put(Items.IRON_INGOT, new BigDecimal("0.001736"));
        ITEM_MASS_TABLE.put(Items.GOLD_INGOT, new BigDecimal("0.003056"));
        ITEM_MASS_TABLE.put(Items.NETHERITE_INGOT, new BigDecimal("0.00434"));
        ITEM_MASS_TABLE.put(DCPContent.Items.SOLAR_SAIL.get(), new BigDecimal("0.0005"));
        ITEM_MASS_TABLE.put(DCPContent.Items.BEAM.get(), new BigDecimal("0.001"));
    }

    private StarData baseStar;
    private boolean antimatter = true;
    private BigInteger totalEnergyReservoir;
    private double currentProgress = 0;
    private BigNumber cumulativeOutput = new BigNumber(0);
    private BigNumber currentOutputRate = new BigNumber(0);

    public AntimatterStarData(StarData baseStar) {
        this.baseStar = baseStar;
        BigDecimal massKg = BigDecimal.valueOf(baseStar.massInMSun())
                .multiply(BigDecimal.valueOf(AstrophysicalCalculator.SOLAR_MASS_KG));
        this.totalEnergyReservoir = massKg.multiply(C_SQUARED).toBigInteger();
    }

    public static BigInteger calculateTotalAnnihilationEnergy(double massKg) {
        return BigDecimal.valueOf(massKg).multiply(C_SQUARED).toBigInteger();
    }

    public BigInteger calculateOutputRate(double progress, long totalWrapped) {
        return legacyProjection(calculateOutputRateBigNumber(progress, totalWrapped));
    }

    public BigNumber calculateOutputRateBigNumber(double progress, long totalWrapped) {
        if (progress >= 100) return new BigNumber(0);
        BigDecimal pct = BigDecimal.valueOf(progress).divide(ONE_HUNDRED);
        BigDecimal baseOutput = new BigDecimal(totalEnergyReservoir).multiply(pct);
        return scaleByWrappedCount(baseOutput.toBigInteger(), totalWrapped);
    }

    public static BigDecimal getItemMassKgCapped(ItemStack stack, BigDecimal cap) {
        if (stack.isEmpty() || cap.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        if (stack.getItem() instanceof CompressedItem compressedItem) {
            return compressedItem.getMassKgCapped(stack, cap);
        }
        return ITEM_MASS_TABLE.getOrDefault(stack.getItem(), BigDecimal.ZERO).min(cap);
    }

    public double getProgressIncrease(ItemStack stack, int count, BigDecimal starMassKg) {
        if (count <= 0 || starMassKg.signum() <= 0 || currentProgress >= 100) {
            return 0;
        }
        BigDecimal remainingPercent = ONE_HUNDRED.subtract(BigDecimal.valueOf(currentProgress));
        BigDecimal remainingMass = starMassKg.multiply(remainingPercent)
                .divide(ONE_HUNDRED, CALCULATION_CONTEXT);
        BigDecimal itemMass = getItemMassKgCapped(stack, remainingMass);
        BigDecimal acceptedMass = itemMass.multiply(BigDecimal.valueOf(count)).min(remainingMass);
        if (acceptedMass.signum() <= 0) {
            return 0;
        }
        return acceptedMass.multiply(ONE_HUNDRED)
                .divide(starMassKg, CALCULATION_CONTEXT)
                .doubleValue();
    }

    public BigInteger feed(ItemStack stack, int count, long totalWrapped, BigDecimal starMassKg) {
        return legacyProjection(feedBigNumber(stack, count, totalWrapped, starMassKg));
    }

    public BigNumber feedBigNumber(ItemStack stack, int count, long totalWrapped, BigDecimal starMassKg) {
        double increase = getProgressIncrease(stack, count, starMassKg);
        if (increase <= 0) {
            return new BigNumber(0);
        }
        double newProgress = Math.min(100, currentProgress + increase);
        BigDecimal deltaPct = BigDecimal.valueOf(newProgress - currentProgress).divide(ONE_HUNDRED);
        BigDecimal deltaOutput = new BigDecimal(totalEnergyReservoir).multiply(deltaPct);
        BigNumber output = scaleByWrappedCount(deltaOutput.toBigInteger(), totalWrapped);

        this.currentProgress = newProgress;
        this.cumulativeOutput.addEnergy(output.deepCopy());
        this.currentOutputRate = calculateOutputRateBigNumber(currentProgress, totalWrapped);
        return output;
    }

    private static BigNumber scaleByWrappedCount(BigInteger baseValue, long totalWrapped) {
        if (baseValue.signum() <= 0) return new BigNumber(0);
        BigNumber result = BigNumber.valueOf(baseValue);
        if (totalWrapped > 0) {
            result = result.multiply(BigNumber.scientific("1", Long.toString(totalWrapped)));
        }
        return result;
    }

    private static BigInteger legacyProjection(BigNumber value) {
        return value.getExactEnergy().orElse(BigInteger.valueOf(Long.MAX_VALUE));
    }

    public boolean isComplete() {
        return currentProgress >= 100;
    }

    public String serializeToString() {
        return baseStar.serializeToString() + "|" + antimatter + "|" + totalEnergyReservoir
                + "|" + currentProgress + "|" + legacyProjection(cumulativeOutput);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("baseStar", baseStar.serializeToString());
        tag.putBoolean("antimatter", antimatter);
        tag.putString("totalEnergyReservoir", totalEnergyReservoir.toString());
        tag.putDouble("currentProgress", currentProgress);
        tag.put("cumulativeOutput", cumulativeOutput.toTag());
        tag.put("currentOutputRate", currentOutputRate.toTag());
        return tag;
    }

    public static AntimatterStarData fromTag(CompoundTag tag) {
        if (tag == null) return null;
        try {
            StarData base = StarData.deserializeFromString(tag.getString("baseStar"));
            AntimatterStarData data = new AntimatterStarData(base);
            data.antimatter = tag.getBoolean("antimatter");
            data.totalEnergyReservoir = new BigInteger(tag.getString("totalEnergyReservoir"));
            data.currentProgress = tag.getDouble("currentProgress");
            data.cumulativeOutput = BigNumber.fromTag(tag.getCompound("cumulativeOutput"));
            data.currentOutputRate = BigNumber.fromTag(tag.getCompound("currentOutputRate"));
            if (data.cumulativeOutput.isImmutable()) data.cumulativeOutput = data.cumulativeOutput.deepCopy();
            if (data.currentOutputRate.isImmutable()) data.currentOutputRate = data.currentOutputRate.deepCopy();
            return data;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public static AntimatterStarData deserializeFromString(String s) {
        if (s == null || s.isEmpty()) return null;
        String[] parts = s.split("\\|");
        try {
            if (parts.length >= 11) {
                StringBuilder starStr = new StringBuilder();
                for (int i = 0; i < 7; i++) {
                    if (i > 0) starStr.append('|');
                    starStr.append(parts[i]);
                }
                StarData base = StarData.deserializeFromString(starStr.toString());
                AntimatterStarData data = new AntimatterStarData(base);
                data.antimatter = Boolean.parseBoolean(parts[7]);
                data.totalEnergyReservoir = new BigInteger(parts[8]);
                data.currentProgress = Double.parseDouble(parts[9]);
                data.cumulativeOutput = BigNumber.valueOf(new BigInteger(parts[10]));
                return data;
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    public StarData getBaseStar() { return baseStar; }
    public void setBaseStar(StarData baseStar) { this.baseStar = baseStar; }
    public boolean isAntimatter() { return antimatter; }
    public BigInteger getTotalEnergyReservoir() { return totalEnergyReservoir; }
    public void setTotalEnergyReservoir(BigInteger totalEnergyReservoir) { this.totalEnergyReservoir = totalEnergyReservoir; }
    public double getCurrentProgress() { return currentProgress; }
    public void setCurrentProgress(double currentProgress) { this.currentProgress = currentProgress; }
    public BigInteger getCumulativeOutput() { return legacyProjection(cumulativeOutput); }
    public BigNumber getCumulativeOutputBigNumber() { return cumulativeOutput.deepCopy(); }
    public void setCumulativeOutput(BigInteger cumulativeOutput) {
        this.cumulativeOutput = cumulativeOutput == null || cumulativeOutput.signum() <= 0
                ? new BigNumber(0) : BigNumber.valueOf(cumulativeOutput);
    }
    public void setCumulativeOutputBigNumber(BigNumber cumulativeOutput) {
        this.cumulativeOutput = cumulativeOutput == null ? new BigNumber(0) : cumulativeOutput.deepCopy();
    }
    public BigInteger getCurrentOutputRate() { return legacyProjection(currentOutputRate); }
    public BigNumber getCurrentOutputRateBigNumber() { return currentOutputRate.deepCopy(); }
    public void setCurrentOutputRate(BigInteger currentOutputRate) {
        this.currentOutputRate = currentOutputRate == null || currentOutputRate.signum() <= 0
                ? new BigNumber(0) : BigNumber.valueOf(currentOutputRate);
    }
    public void setCurrentOutputRateBigNumber(BigNumber currentOutputRate) {
        this.currentOutputRate = currentOutputRate == null ? new BigNumber(0) : currentOutputRate.deepCopy();
    }

    public BigInteger getRemainingEnergy() {
        double remainingPct = 1 - currentProgress / 100.0;
        return new BigDecimal(totalEnergyReservoir).multiply(BigDecimal.valueOf(remainingPct)).toBigInteger();
    }
}

