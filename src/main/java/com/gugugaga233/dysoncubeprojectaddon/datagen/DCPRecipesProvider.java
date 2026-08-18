package com.gugugaga233.dysoncubeprojectaddon.datagen;

import com.gugugaga233.dysoncubeprojectaddon.DCPContent;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.neoforge.common.Tags;

public class DCPRecipesProvider extends RecipeProvider {

    public DCPRecipesProvider(net.minecraft.data.PackOutput output,
                              CompletableFuture<net.minecraft.core.HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput consumer) {
        com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder
                .shapedRecipe(DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER.asItem())
                .pattern("DRB").pattern("RCB").pattern("SSS")
                .define('D', Tags.Items.GEMS_DIAMOND)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('B', DCPContent.Items.BEAM.get())
                .define('C', Tags.Items.STORAGE_BLOCKS_COPPER)
                .define('S', net.minecraft.world.item.Items.SMOOTH_STONE_SLAB)
                .save(consumer);

        com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder
                .shapedRecipe(DCPContent.Items.SOLAR_SAIL.get())
                .pattern("GCG").pattern("GCG").pattern("LCL")
                .define('G', Tags.Items.GLASS_PANES_COLORLESS)
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('L', Tags.Items.GEMS_LAPIS)
                .save(consumer);

        com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder
                .shapedRecipe(DCPContent.Items.SOLAR_SAIL_PACKAGE.get())
                .pattern("GGG").pattern("GIG").pattern("GGG")
                .define('G', DCPContent.Items.SOLAR_SAIL.get())
                .define('I', Tags.Items.STORAGE_BLOCKS_IRON)
                .save(consumer);

        com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder
                .shapedRecipe(DCPContent.Items.BEAM.get(), 2)
                .pattern("NIN").pattern("BIB").pattern("NIN")
                .define('N', Tags.Items.NUGGETS_IRON)
                .define('I', Tags.Items.STORAGE_BLOCKS_IRON)
                .define('B', net.minecraft.world.item.Items.IRON_BARS)
                .save(consumer);

        com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder
                .shapedRecipe(DCPContent.Items.BEAM_PACKAGE.get())
                .pattern(" G ").pattern("GIG").pattern(" G ")
                .define('G', DCPContent.Items.BEAM.get())
                .define('I', Tags.Items.STORAGE_BLOCKS_COPPER)
                .save(consumer);

        com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder
                .shapedRecipe(DCPContent.Blocks.RAY_RECEIVER_CONTROLLER.asItem())
                .pattern("SSS").pattern("NBN").pattern("III")
                .define('S', DCPContent.Items.SOLAR_SAIL.get())
                .define('N', net.minecraft.world.item.Items.SMOOTH_STONE_SLAB)
                .define('I', Tags.Items.STORAGE_BLOCKS_IRON)
                .define('B', DCPContent.Items.BEAM.get())
                .save(consumer);
    }
}

