package com.gugugaga233.dysoncubeprojectaddon;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8;
import java.math.BigInteger;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DCPAttachments {
    private static final Codec<BigInteger> COMPRESSION_LEVEL_CODEC =
            Codec.either(Codec.STRING, Codec.INT).comapFlatMap(
                    value -> value.map(DCPAttachments::parseLevel,
                            legacy -> validateLevel(BigInteger.valueOf(legacy))),
                    level -> Either.left(level.toString()));

    private static final StreamCodec<RegistryFriendlyByteBuf, BigInteger> COMPRESSION_LEVEL_STREAM_CODEC =
            StreamCodec.of(
                    (buffer, level) -> buffer.writeByteArray(networkBytes(level)),
                    buffer -> validateNetworkLevel(
                            new BigInteger(buffer.readByteArray(
                             CompressionLevelEncoding.MAX_NETWORK_BYTES))));

    private static final StreamCodec<RegistryFriendlyByteBuf, CompoundTag> COMPOUND_TAG_STREAM_CODEC =
            ByteBufCodecs.COMPOUND_TAG.cast();
    private static final StreamCodec<RegistryFriendlyByteBuf, CompoundTag> DYSON_CELL_STREAM_CODEC =
            StreamCodec.of(
                    (buffer, contents) -> COMPOUND_TAG_STREAM_CODEC.encode(
                            buffer, networkCellContents(contents)),
                    COMPOUND_TAG_STREAM_CODEC::decode);

    public static final DeferredRegister<DataComponentType<?>> DR =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, DysonCubeProject.MODID);

    public static final Supplier<DataComponentType<Integer>> SOLAR_SAIL =
            com.buuz135.dysoncubeproject.DCPAttachments.SOLAR_SAIL;
    public static final Supplier<DataComponentType<Integer>> BEAM =
            com.buuz135.dysoncubeproject.DCPAttachments.BEAM;

    /**
     * Arbitrary precision compression level. Legacy integer values remain readable, while
     * new values are persisted and synchronized as decimal strings.
     */
    public static final Supplier<DataComponentType<BigInteger>> COMPRESSION_LEVEL =
            register("compression_level", () -> BigInteger.ONE,
                    builder -> builder.persistent(COMPRESSION_LEVEL_CODEC)
                            .networkSynchronized(COMPRESSION_LEVEL_STREAM_CODEC));

    /** Exact compressed stack used to define an optional AE2 compression pattern. */
    public static final Supplier<DataComponentType<ItemStack>> AE2_COMPRESSION_TARGET =
            register("ae2_compression_target", () -> ItemStack.EMPTY,
                    builder -> builder.persistent(ItemStack.CODEC)
                            .networkSynchronized(ItemStack.STREAM_CODEC));

    /** Opaque exact-count storage used only by the optional AE2 integration. */
    public static final Supplier<DataComponentType<CompoundTag>> DYSON_AE_CELL_CONTENTS =
            register("dyson_ae_cell_contents", CompoundTag::new,
                    builder -> builder.persistent(CompoundTag.CODEC.xmap(
                            DCPAttachments::compactCellContents,
                            value -> value == null ? new CompoundTag() : value))
                            .networkSynchronized(DYSON_CELL_STREAM_CODEC));

    /** Migrates old Flux layer tags before an item stack can be put in a chunk packet. */
    private static CompoundTag compactCellContents(CompoundTag contents) {
        if (contents == null || contents.getBoolean("networkSummary")) return contents;
        ListTag source = contents.getList("entries", Tag.TAG_COMPOUND);
        if (source.isEmpty()) return contents;

        CompoundTag compact = contents.copy();
        ListTag entries = new ListTag();
        for (int index = 0; index < source.size(); index++) {
            CompoundTag entry = source.getCompound(index).copy();
            if (entry.contains("amount", Tag.TAG_COMPOUND)) {
                entry.put("amount", FluxMath8.toCompactTag(
                        FluxMath8.fromCompactTag(entry.getCompound("amount"))));
            }
            entries.add(entry);
        }
        compact.put("entries", entries);
        return compact;
    }

    static CompoundTag networkCellContents(CompoundTag contents) {
        CompoundTag summary = new CompoundTag();
        int typeCount = contents.contains("typeCount")
                ? contents.getInt("typeCount")
                : contents.getList("entries", Tag.TAG_COMPOUND).size();
        String totalDisplay = contents.getString("totalDisplay");
        if (totalDisplay.isEmpty()) totalDisplay = typeCount == 0 ? "0" : "?";
        summary.putInt("typeCount", typeCount);
        summary.putString("totalDisplay", totalDisplay);
        summary.putBoolean("networkSummary", true);
        return summary;
    }

    private static DataResult<BigInteger> parseLevel(String value) {
        try {
            return validateLevel(new BigInteger(value));
        } catch (NumberFormatException exception) {
            return DataResult.error(() -> "Invalid compression level: " + value);
        }
    }

    private static BigInteger validateNetworkLevel(BigInteger level) {
        if (level.signum() < 1) {
            throw new IllegalArgumentException("Compression level must be at least 1");
        }
        return level;
    }

    static byte[] networkBytes(BigInteger level) {
        return CompressionLevelEncoding.encode(level);
    }

    private static DataResult<BigInteger> validateLevel(BigInteger level) {
        return level.signum() < 1
                ? DataResult.error(() -> "Compression level must be at least 1")
                : DataResult.success(level);
    }

    private static <T> ComponentSupplier<T> register(
            String name, Supplier<T> defaultValue, UnaryOperator<DataComponentType.Builder<T>> operation) {
        DeferredHolder<DataComponentType<?>, DataComponentType<T>> registered =
                DR.register(name, () -> operation.apply(DataComponentType.builder()).build());
        return new ComponentSupplier<>(registered, defaultValue);
    }

    public static class ComponentSupplier<T> implements Supplier<DataComponentType<T>> {
        private final Supplier<DataComponentType<T>> type;
        private final Supplier<T> defaultSupplier;

        public ComponentSupplier(Supplier<DataComponentType<T>> type, Supplier<T> defaultSupplier) {
            this.type = type;
            this.defaultSupplier = Suppliers.memoize(defaultSupplier::get);
        }

        public T get(ItemStack stack) {
            return stack.getOrDefault(this.type, this.defaultSupplier.get());
        }

        @Override
        public DataComponentType<T> get() {
            return this.type.get();
        }
    }
}

