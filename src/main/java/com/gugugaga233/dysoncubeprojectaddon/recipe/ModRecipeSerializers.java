package com.gugugaga233.dysoncubeprojectaddon.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> DR =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, "dysoncubeproject");

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CompressionRecipe>> COMPRESSION_RECIPE =
            DR.register("compression", CompressionRecipeSerializer::new);

    private ModRecipeSerializers() {
    }

    private static final class CompressionRecipeSerializer implements RecipeSerializer<CompressionRecipe> {
        private static final MapCodec<CompressionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC)
                        .forGetter(CompressionRecipe::category),
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(CompressionRecipe::item),
                BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("base_item")
                        .forGetter(recipe -> Optional.ofNullable(recipe.baseItem())),
                Codec.BOOL.optionalFieldOf("base_only", false).forGetter(CompressionRecipe::baseOnly)
        ).apply(instance, CompressionRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, CompressionRecipe> STREAM_CODEC = StreamCodec.of(
                (buffer, recipe) -> {
                    CraftingBookCategory.STREAM_CODEC.encode(buffer, recipe.category());
                    buffer.writeResourceLocation(BuiltInRegistries.ITEM.getKey(recipe.item()));
                    buffer.writeBoolean(recipe.baseItem() != null);
                    if (recipe.baseItem() != null) {
                        buffer.writeResourceLocation(BuiltInRegistries.ITEM.getKey(recipe.baseItem()));
                    }
                    buffer.writeBoolean(recipe.baseOnly());
                },
                buffer -> {
                    CraftingBookCategory category = CraftingBookCategory.STREAM_CODEC.decode(buffer);
                    Item item = BuiltInRegistries.ITEM.get(buffer.readResourceLocation());
                    Item baseItem = buffer.readBoolean()
                            ? BuiltInRegistries.ITEM.get(buffer.readResourceLocation())
                            : null;
                    return new CompressionRecipe(category, item, Optional.ofNullable(baseItem), buffer.readBoolean());
                });

        @Override
        public MapCodec<CompressionRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CompressionRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

