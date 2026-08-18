package com.gugugaga233.dysoncubeprojectaddon.integration.ae2.recipe;

import appeng.core.definitions.AEItems;
import com.gugugaga233.dysoncubeprojectaddon.DCPAttachments;
import com.gugugaga233.dysoncubeprojectaddon.DCPContent;
import com.gugugaga233.dysoncubeprojectaddon.integration.ae2.AE2Integration;
import com.gugugaga233.dysoncubeprojectaddon.item.CompressedItem;
import com.mojang.serialization.MapCodec;
import java.math.BigInteger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class AE2CompressionPatternRecipe extends CustomRecipe {
    public AE2CompressionPatternRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != 2) {
            return false;
        }

        boolean blankPattern = false;
        boolean compressedItem = false;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (AEItems.BLANK_PATTERN.is(stack) && !blankPattern) {
                blankPattern = true;
            } else if (stack.getItem() instanceof CompressedItem && !compressedItem) {
                compressedItem = true;
            } else {
                return false;
            }
        }
        return blankPattern && compressedItem;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        for (ItemStack stack : input.items()) {
            if (stack.getItem() instanceof CompressedItem) {
                return createPattern(stack);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        ItemStack example = CompressedItem.withLevel(DCPContent.Items.COMPRESSED_SAIL.get(), BigInteger.ONE);
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.of(AEItems.BLANK_PATTERN.stack()));
        ingredients.add(Ingredient.of(example));
        return ingredients;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return createPattern(CompressedItem.withLevel(DCPContent.Items.COMPRESSED_SAIL.get(), BigInteger.ONE));
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.getItem() instanceof CompressedItem) {
                remaining.set(slot, stack.copyWithCount(1));
            }
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AE2Integration.PATTERN_RECIPE_SERIALIZER.get();
    }

    private static ItemStack createPattern(ItemStack target) {
        ItemStack pattern = new ItemStack(AE2Integration.COMPRESSION_PATTERN.get());
        pattern.set(DCPAttachments.AE2_COMPRESSION_TARGET.get(), target.copyWithCount(1));
        return pattern;
    }

    public static final class Serializer implements RecipeSerializer<AE2CompressionPatternRecipe> {
        private static final MapCodec<AE2CompressionPatternRecipe> CODEC =
                MapCodec.unit(() -> new AE2CompressionPatternRecipe(CraftingBookCategory.MISC));
        private static final StreamCodec<RegistryFriendlyByteBuf, AE2CompressionPatternRecipe> STREAM_CODEC =
                StreamCodec.of(
                        (buffer, recipe) -> { },
                        buffer -> new AE2CompressionPatternRecipe(CraftingBookCategory.MISC));

        @Override
        public MapCodec<AE2CompressionPatternRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AE2CompressionPatternRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

