package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import java.math.BigInteger;
import net.minecraft.nbt.CompoundTag;
import sonar.fluxnetworks.api.energy.BigNumber;

/**
 * Compatibility wrapper retained for integrations that used the old DCP API.
 * Flux Networks' BigNumber is now the only energy representation underneath.
 */
public final class MultiLevelEnergy {

    public static final BigInteger CAPACITY = BigNumber.THRESHOLD_1E300_BI;

    private BigNumber value = new BigNumber(0);

    public static MultiLevelEnergy zero() {
        return new MultiLevelEnergy();
    }

    public static MultiLevelEnergy fromBigNumber(BigNumber value) {
        MultiLevelEnergy energy = new MultiLevelEnergy();
        if (value != null && !value.isEmpty()) {
            energy.value = value.deepCopy();
        }
        return energy;
    }

    public static MultiLevelEnergy fromBigInteger(BigInteger value) {
        if (value == null || value.signum() <= 0) {
            return zero();
        }
        return fromBigNumber(BigNumber.valueOf(value));
    }

    public int getCurrentLevel() {
        return value.getStateLevel();
    }

    public BigNumber getValue() {
        return value.deepCopy();
    }

    public BigInteger getOverflowAt(int level) {
        return BigInteger.ZERO;
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    public boolean hasAtLeastLevel(int level) {
        return !isEmpty() && level <= getCurrentLevel();
    }

    public void add(MultiLevelEnergy other) {
        if (other != null && !other.isEmpty()) {
            value.addEnergy(other.value.deepCopy());
        }
    }

    public BigInteger extract(BigInteger amount) {
        if (amount == null || amount.signum() <= 0 || isEmpty()) {
            return BigInteger.ZERO;
        }
        return value.extractBigInteger(amount);
    }

    public void subtract(MultiLevelEnergy other) {
        if (other != null && !other.isEmpty() && !isEmpty()) {
            value.subtract(other.value.deepCopy());
        }
    }

    public int compareTo(MultiLevelEnergy other) {
        if (other == null) {
            return isEmpty() ? 0 : 1;
        }
        return value.compareTo(other.value);
    }

    public boolean isNegligibleComparedTo(MultiLevelEnergy other) {
        if (other == null || isEmpty() || other.isEmpty()) {
            return false;
        }
        return Math.abs(value.getStateLevel() - other.value.getStateLevel()) >= 2;
    }

    public void copyFrom(MultiLevelEnergy other) {
        value = other == null ? new BigNumber(0) : other.value.deepCopy();
    }

    public MultiLevelEnergy copy() {
        return fromBigNumber(value);
    }

    public CompoundTag saveNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("value", value.toTag());
        return tag;
    }

    public void loadNBT(CompoundTag tag) {
        if (tag != null && tag.contains("value")) {
            BigNumber loaded = BigNumber.fromTag(tag.getCompound("value"));
            value = loaded.isImmutable() ? loaded.deepCopy() : loaded;
        } else {
            value = new BigNumber(0);
        }
    }

    public String toDisplayString() {
        return value.toDisplayString();
    }

    public String toCompactString() {
        return value.toScientificString();
    }
}

