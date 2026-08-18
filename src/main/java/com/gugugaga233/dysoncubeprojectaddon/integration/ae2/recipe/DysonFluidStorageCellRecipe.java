package com.gugugaga233.dysoncubeprojectaddon.integration.ae2.recipe;

import appeng.core.definitions.AEItems;
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

public final class DysonFluidStorageCellRecipe extends CustomRecipe {
    private static final BigInteger REQUIRED_LEVEL = BigInteger.valueOf(20);

    public DysonFluidStorageCellRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() < 3 || input.height() < 3 || input.ingredientCount() != 9) return false;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                ItemStack stack = input.getItem(x, y);
                if (x == 1 && y == 1) {
                    if (!AEItems.FLUID_CELL_256K.is(stack)) return false;
                } else if (!stack.is(DCPContent.Items.COMPRESSED_GOLD_BLOCK.get())
                        || !REQUIRED_LEVEL.equals(CompressedItem.getLevel(stack))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return new ItemStack(AE2Integration.DYSON_FLUID_STORAGE_CELL.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(AE2Integration.DYSON_FLUID_STORAGE_CELL.get());
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> result = NonNullList.withSize(9, Ingredient.EMPTY);
        ItemStack gold = CompressedItem.withLevel(
                DCPContent.Items.COMPRESSED_GOLD_BLOCK.get(), REQUIRED_LEVEL);
        for (int slot = 0; slot < 9; slot++) {
            result.set(slot, slot == 4
                    ? Ingredient.of(AEItems.FLUID_CELL_256K)
                    : Ingredient.of(gold.copy()));
        }
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AE2Integration.DYSON_FLUID_STORAGE_CELL_RECIPE_SERIALIZER.get();
    }

    public static final class Serializer implements RecipeSerializer<DysonFluidStorageCellRecipe> {
        private static final MapCodec<DysonFluidStorageCellRecipe> CODEC =
                MapCodec.unit(() -> new DysonFluidStorageCellRecipe(CraftingBookCategory.MISC));
        private static final StreamCodec<RegistryFriendlyByteBuf, DysonFluidStorageCellRecipe> STREAM_CODEC =
                StreamCodec.of((buffer, recipe) -> {
                }, buffer -> new DysonFluidStorageCellRecipe(CraftingBookCategory.MISC));

        @Override
        public MapCodec<DysonFluidStorageCellRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DysonFluidStorageCellRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
