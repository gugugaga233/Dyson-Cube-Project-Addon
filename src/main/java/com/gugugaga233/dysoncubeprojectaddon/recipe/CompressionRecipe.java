package com.gugugaga233.dysoncubeprojectaddon.recipe;

import com.gugugaga233.dysoncubeprojectaddon.item.CompressedItem;
import java.math.BigInteger;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class CompressionRecipe extends CustomRecipe {
    private static final int INPUT_COUNT = 9;

    private final Item item;
    private final Item baseItem;
    private final boolean baseOnly;

    public CompressionRecipe(CraftingBookCategory category, Item item) {
        this(category, item, null, false);
    }

    public CompressionRecipe(CraftingBookCategory category, Item item, Optional<Item> baseItem,
                             boolean baseOnly) {
        super(category);
        this.item = item;
        this.baseItem = baseItem.orElse(null);
        this.baseOnly = baseOnly;
    }

    public Item item() {
        return item;
    }

    public Item baseItem() {
        return baseItem;
    }

    public boolean baseOnly() {
        return baseOnly;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != INPUT_COUNT || !(item instanceof CompressedItem)) {
            return false;
        }

        if (baseOnly) {
            if (baseItem == null) {
                return false;
            }
            for (ItemStack stack : input.items()) {
                if (stack.isEmpty() || !stack.is(baseItem)) {
                    return false;
                }
            }
            return true;
        }

        BigInteger compressionLevel = null;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (!stack.is(item)) {
                return false;
            }
            BigInteger stackLevel = CompressedItem.getLevel(stack);
            if (compressionLevel == null) {
                compressionLevel = stackLevel;
            } else if (!compressionLevel.equals(stackLevel)) {
                return false;
            }
        }
        return compressionLevel != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        if (baseOnly) {
            return CompressedItem.withLevel(item, BigInteger.ONE);
        }

        for (ItemStack stack : input.items()) {
            if (!stack.isEmpty()) {
                ItemStack result = stack.copyWithCount(1);
                result.set(com.gugugaga233.dysoncubeprojectaddon.DCPAttachments.COMPRESSION_LEVEL.get(),
                        CompressedItem.getLevel(stack).add(BigInteger.ONE));
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= INPUT_COUNT;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return CompressedItem.withLevel(item, baseOnly ? BigInteger.ONE : BigInteger.TWO);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        ItemStack example = baseOnly
                ? new ItemStack(baseItem)
                : CompressedItem.withLevel(item, BigInteger.ONE);
        NonNullList<Ingredient> ingredients = NonNullList.withSize(INPUT_COUNT, Ingredient.EMPTY);
        for (int index = 0; index < INPUT_COUNT; index++) {
            ingredients.set(index, Ingredient.of(example.copy()));
        }
        return ingredients;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.COMPRESSION_RECIPE.get();
    }
}

