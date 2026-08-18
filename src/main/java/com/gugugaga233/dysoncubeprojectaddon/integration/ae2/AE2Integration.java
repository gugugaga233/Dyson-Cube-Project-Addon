package com.gugugaga233.dysoncubeprojectaddon.integration.ae2;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.AECapabilities;
import appeng.api.storage.StorageCells;
import appeng.block.AEBaseBlockItem;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.items.parts.PartItem;
import appeng.menu.implementations.MenuTypeBuilder;
import com.gugugaga233.dysoncubeprojectaddon.DCPContent;
import com.gugugaga233.dysoncubeprojectaddon.DysonCubeProject;
import com.gugugaga233.dysoncubeprojectaddon.integration.ae2.recipe.AE2CompressionPatternRecipe;
import com.gugugaga233.dysoncubeprojectaddon.integration.ae2.recipe.DysonStorageCellRecipe;
import com.gugugaga233.dysoncubeprojectaddon.integration.ae2.recipe.DysonFluidStorageCellRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class AE2Integration {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, DysonCubeProject.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, DysonCubeProject.MODID);
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, DysonCubeProject.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, DysonCubeProject.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DysonCubeProject.MODID);

    public static final DeferredHolder<Item, Item> COMPRESSION_PATTERN = DCPContent.REGISTRY.registerGeneric(
            Registries.ITEM,
            "ae2_compression_pattern",
            () -> PatternDetailsHelper
                    .encodedPatternItemBuilder(CompressionPatternDetails::decode)
                    .itemProperties(new Item.Properties().stacksTo(1))
                    .build());

    public static final DeferredHolder<Item, Item> DYSON_AE_TERMINAL = DCPContent.REGISTRY.registerGeneric(
            Registries.ITEM,
            "dyson_ae_terminal",
            () -> new PartItem<>(new Item.Properties().stacksTo(1),
                    DysonAETerminalPart.class, DysonAETerminalPart::new));

    public static final DeferredHolder<Item, DysonStorageCellItem> DYSON_STORAGE_CELL = ITEMS.register(
            "dyson_storage_cell", () -> new DysonStorageCellItem(new Item.Properties()));

    public static final DeferredHolder<Item, DysonFluidStorageCellItem> DYSON_FLUID_STORAGE_CELL = ITEMS.register(
            "dyson_fluid_storage_cell", () -> new DysonFluidStorageCellItem(new Item.Properties()));

    public static final DeferredHolder<Block, DysonIOPortBlock> DYSON_IO_PORT = BLOCKS.register(
            "dyson_io_port", DysonIOPortBlock::new);

    public static final DeferredHolder<Item, AEBaseBlockItem> DYSON_IO_PORT_ITEM = ITEMS.register(
            "dyson_io_port", () -> new AEBaseBlockItem(DYSON_IO_PORT.get(), new Item.Properties()));

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DysonIOPortBlockEntity>>
            DYSON_IO_PORT_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("dyson_io_port", () -> {
                BlockEntityType<DysonIOPortBlockEntity> type = BlockEntityType.Builder.of(
                        DysonIOPortBlockEntity::new, DYSON_IO_PORT.get()).build(null);
                DYSON_IO_PORT.get().setBlockEntity((Class) DysonIOPortBlockEntity.class,
                        (BlockEntityType) type, null, null);
                AEBaseBlockEntity.registerBlockEntityItem(type, DYSON_IO_PORT_ITEM.get());
                return type;
            });

    public static final DeferredHolder<MenuType<?>, MenuType<DysonAETerminalMenu>> DYSON_AE_TERMINAL_MENU =
            MENU_TYPES.register("dyson_ae_terminal", () -> MenuTypeBuilder
                    .create(DysonAETerminalMenu::new, DysonAETerminalPart.class)
                    .buildUnregistered(ResourceLocation.fromNamespaceAndPath(
                            DysonCubeProject.MODID, "dyson_ae_terminal")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AE2CompressionPatternRecipe>>
            PATTERN_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "ae2_compression_pattern", AE2CompressionPatternRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DysonStorageCellRecipe>>
            DYSON_STORAGE_CELL_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "dyson_storage_cell", DysonStorageCellRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DysonFluidStorageCellRecipe>>
            DYSON_FLUID_STORAGE_CELL_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "dyson_fluid_storage_cell", DysonFluidStorageCellRecipe.Serializer::new);

    private AE2Integration() {
    }

    public static void init() {
        DysonAETerminalPart.registerModels();
    }

    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(AE2Integration::commonSetup);
        DysonAETerminalPart.registerModels();
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> StorageCells.addCellHandler(DysonStorageCellHandler.INSTANCE));
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(AECapabilities.ME_STORAGE,
                (level, pos, state, blockEntity, side) ->
                        blockEntity instanceof com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.MiningItemOutputPortBlockEntity port
                                ? new MiningPortMEStorage(port::findHub, MiningPortMEStorage.Kind.ITEM) : null,
                com.gugugaga233.dysoncubeprojectaddon.overhaul.OverhaulContent.getMiningItemOutputPortBlock());
        event.registerBlock(AECapabilities.ME_STORAGE,
                (level, pos, state, blockEntity, side) ->
                        blockEntity instanceof com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.MiningFluidOutputPortBlockEntity port
                                ? new MiningPortMEStorage(port::findHub, MiningPortMEStorage.Kind.FLUID) : null,
                com.gugugaga233.dysoncubeprojectaddon.overhaul.OverhaulContent.getMiningFluidOutputPortBlock());
        event.registerBlock(AECapabilities.ME_STORAGE,
                (level, pos, state, blockEntity, side) ->
                        blockEntity instanceof com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.OreProcessingHubBlockEntity hub
                                ? new OreProcessingMEStorage(hub) : null,
                com.gugugaga233.dysoncubeprojectaddon.overhaul.OverhaulContent.getOreProcessingHubBlock());
    }
}

