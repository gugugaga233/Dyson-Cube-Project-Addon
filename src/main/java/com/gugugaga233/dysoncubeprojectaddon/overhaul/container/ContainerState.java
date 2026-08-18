package com.gugugaga233.dysoncubeprojectaddon.overhaul.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Thin compatibility wrapper around a CompoundTag.
 * Provides the same API as the old ContainerState for GUI display purposes.
 */
public class ContainerState {

    public static final int DIGITS_SIZE = 16384;

    private final CompoundTag mTag;

    public ContainerState(CompoundTag tag) {
        this.mTag = tag;
    }

    public CompoundTag toTag() { return mTag; }

    // ---- numeric fields (all default to 0 if missing) ----
    public int M() { return mTag.getInt("M"); }
    public int K() { return mTag.getInt("K"); }
    public int maxCounter() { return mTag.getInt("maxCounter"); }
    public int towerHeight() { return mTag.getInt("towerHeight"); }
    public int version() { return mTag.getInt("version"); }
    public int stateMaxK() { return DIGITS_SIZE; }
    public long digitsFingerprint() { return mTag.getLong("fingerprint"); }

    public long[] digits() {
        long[] arr = new long[DIGITS_SIZE];
        CompoundTag dt = mTag.getCompound("digits");
        for (String k : dt.getAllKeys()) {
            int idx = Integer.parseInt(k);
            if (idx >= 0 && idx < DIGITS_SIZE) arr[idx] = dt.getLong(k);
        }
        return arr;
    }

    public record DigitSummary(int usedSlots, long topDigit, long[] digits) {
        public long progress() { return topDigit; }
    }

    public DigitSummary digitSummary() {
        long[] d = digits();
        int usedSlots = 0;
        long topDigit = 0;
        for (int i = DIGITS_SIZE - 1; i >= 0; i--) {
            if (d[i] != 0) { usedSlots = i + 1; topDigit = d[i]; break; }
        }
        return new DigitSummary(usedSlots, topDigit, d);
    }

    // ---- NBT / network ----

    public static ContainerState fromTag(CompoundTag tag) {
        return new ContainerState(tag.copy());
    }

    public static void encode(FriendlyByteBuf buf, ContainerState state) {
        buf.writeNbt(state.toTag());
    }

    public static ContainerState decode(FriendlyByteBuf buf) {
        return new ContainerState(buf.readNbt());
    }

    public static final StreamCodec STREAM_CODEC = new StreamCodec();

    public static class StreamCodec {
        public void encode(FriendlyByteBuf buf, ContainerState state) { ContainerState.encode(buf, state); }
        public ContainerState decode(FriendlyByteBuf buf) { return ContainerState.decode(buf); }
    }

    public static class Builder {
        private final CompoundTag tag = new CompoundTag();

        public Builder M(int m) { tag.putInt("M", m); return this; }
        public Builder K(int k) { tag.putInt("K", k); return this; }
        public Builder maxCounter(int mc) { tag.putInt("maxCounter", mc); return this; }
        public Builder towerHeight(int th) { tag.putInt("towerHeight", th); return this; }
        public Builder digits(long[] d) {
            CompoundTag dt = new CompoundTag();
            for (int i = 0; i < d.length; i++) if (d[i] != 0) dt.putLong(String.valueOf(i), d[i]);
            tag.put("digits", dt);
            return this;
        }
        public Builder fingerprint(long fp) { tag.putLong("fingerprint", fp); return this; }
        public Builder version(long v) { tag.putInt("version", (int) v); return this; }
        public ContainerState build() { return new ContainerState(tag); }
    }
}

