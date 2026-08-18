package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import com.gugugaga233.dysoncubeprojectaddon.DCPContent;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.BlackHoleAmplifierBlock;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.DysonHubBlock;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.CosmicMiningHubBlock;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.LaserEnergyInputPortBlock;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.BigNumberEnergyOutputPortBlock;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.MiningFluidOutputPortBlock;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.MiningItemOutputPortBlock;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.OreProcessingHubBlock;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.BlackHoleAmplifierBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.DysonHubBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.CosmicMiningHubBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.LaserEnergyInputPortBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.BigNumberEnergyOutputPortBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.MiningFluidOutputPortBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.MiningItemOutputPortBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.OreProcessingHubBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class OverhaulContent {
    private static DeferredHolder<Block, Block> dysonHubBlock;
    private static DeferredHolder<Block, Block> blackHoleAmplifierBlock;
    private static DeferredHolder<Block, Block> cosmicMiningHubBlock;
    private static DeferredHolder<Block, Block> laserEnergyInputPortBlock;
    private static DeferredHolder<Block, Block> bigNumberEnergyOutputPortBlock;
    private static DeferredHolder<Block, Block> miningItemOutputPortBlock;
    private static DeferredHolder<Block, Block> miningFluidOutputPortBlock;
    private static DeferredHolder<Block, Block> oreProcessingHubBlock;
    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> dysonHubBlockEntity;
    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> blackHoleAmplifierBlockEntity;
    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> cosmicMiningHubBlockEntity;
    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> laserEnergyInputPortBlockEntity;
    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> bigNumberEnergyOutputPortBlockEntity;
    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> miningItemOutputPortBlockEntity;
    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> miningFluidOutputPortBlockEntity;
    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> oreProcessingHubBlockEntity;
    private static boolean initialized;

    private OverhaulContent() {
    }

    public static Block getDysonHubBlock() {
        return dysonHubBlock.get();
    }

    public static Block getBlackHoleAmplifierBlock() {
        return blackHoleAmplifierBlock.get();
    }

    public static Block getCosmicMiningHubBlock() {
        return cosmicMiningHubBlock.get();
    }

    public static Block getLaserEnergyInputPortBlock() {
        return laserEnergyInputPortBlock.get();
    }

    public static Block getBigNumberEnergyOutputPortBlock() {
        return bigNumberEnergyOutputPortBlock.get();
    }

    public static Block getMiningItemOutputPortBlock() {
        return miningItemOutputPortBlock.get();
    }

    public static Block getMiningFluidOutputPortBlock() {
        return miningFluidOutputPortBlock.get();
    }

    public static Block getOreProcessingHubBlock() {
        return oreProcessingHubBlock.get();
    }

    @SuppressWarnings("unchecked")
    public static BlockEntityType<DysonHubBlockEntity> getDysonHubBEType() {
        return (BlockEntityType<DysonHubBlockEntity>) (BlockEntityType<?>) dysonHubBlockEntity.get();
    }

    @SuppressWarnings("unchecked")
    public static BlockEntityType<BlackHoleAmplifierBlockEntity> getBlackHoleAmplifierBEType() {
        return (BlockEntityType<BlackHoleAmplifierBlockEntity>) (BlockEntityType<?>) blackHoleAmplifierBlockEntity.get();
    }

    @SuppressWarnings("unchecked")
    public static BlockEntityType<CosmicMiningHubBlockEntity> getCosmicMiningHubBEType() {
        return (BlockEntityType<CosmicMiningHubBlockEntity>) (BlockEntityType<?>) cosmicMiningHubBlockEntity.get();
    }

    @SuppressWarnings("unchecked")
    public static BlockEntityType<LaserEnergyInputPortBlockEntity> getLaserEnergyInputPortBEType() {
        return (BlockEntityType<LaserEnergyInputPortBlockEntity>) (BlockEntityType<?>) laserEnergyInputPortBlockEntity.get();
    }

    @SuppressWarnings("unchecked")
    public static BlockEntityType<BigNumberEnergyOutputPortBlockEntity> getBigNumberEnergyOutputPortBEType() {
        return (BlockEntityType<BigNumberEnergyOutputPortBlockEntity>) (BlockEntityType<?>)
                bigNumberEnergyOutputPortBlockEntity.get();
    }

    @SuppressWarnings("unchecked")
    public static BlockEntityType<MiningItemOutputPortBlockEntity> getMiningItemOutputPortBEType() {
        return (BlockEntityType<MiningItemOutputPortBlockEntity>) (BlockEntityType<?>)
                miningItemOutputPortBlockEntity.get();
    }

    @SuppressWarnings("unchecked")
    public static BlockEntityType<MiningFluidOutputPortBlockEntity> getMiningFluidOutputPortBEType() {
        return (BlockEntityType<MiningFluidOutputPortBlockEntity>) (BlockEntityType<?>)
                miningFluidOutputPortBlockEntity.get();
    }

    @SuppressWarnings("unchecked")
    public static BlockEntityType<OreProcessingHubBlockEntity> getOreProcessingHubBEType() {
        return (BlockEntityType<OreProcessingHubBlockEntity>) (BlockEntityType<?>)
                oreProcessingHubBlockEntity.get();
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        dysonHubBlock = DCPContent.REGISTRY.registerBlockWithItem(
                "dyson_hub",
                () -> new DysonHubBlock(BlockBehaviour.Properties.of()
                        .strength(3.0f)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                holder -> () -> new BlockItem(holder.get(), new Item.Properties()),
                DCPContent.TAB);
        dysonHubBlockEntity = DCPContent.REGISTRY.registerBlockEntityType("dyson_hub", () ->
                BlockEntityType.Builder.of(DysonHubBlockEntity::new, getDysonHubBlock()).build(null));

        blackHoleAmplifierBlock = DCPContent.REGISTRY.registerBlockWithItem(
                "black_hole_amplifier",
                () -> new BlackHoleAmplifierBlock(BlockBehaviour.Properties.of()
                        .strength(2.0f)
                        .requiresCorrectToolForDrops()
                        .noCollission()
                        .noOcclusion()
                        .lightLevel(state -> 4)),
                holder -> () -> new BlockItem(holder.get(), new Item.Properties()),
                DCPContent.TAB);
        blackHoleAmplifierBlockEntity = DCPContent.REGISTRY.registerBlockEntityType("black_hole_amplifier", () ->
                BlockEntityType.Builder.of(BlackHoleAmplifierBlockEntity::new, getBlackHoleAmplifierBlock()).build(null));

        cosmicMiningHubBlock = DCPContent.REGISTRY.registerBlockWithItem(
                "cosmic_mining_hub",
                () -> new CosmicMiningHubBlock(BlockBehaviour.Properties.of()
                        .strength(4.0f)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()
                        .lightLevel(state -> 6)),
                holder -> () -> new BlockItem(holder.get(), new Item.Properties()),
                DCPContent.TAB);
        cosmicMiningHubBlockEntity = DCPContent.REGISTRY.registerBlockEntityType("cosmic_mining_hub", () ->
                BlockEntityType.Builder.of(CosmicMiningHubBlockEntity::new, getCosmicMiningHubBlock()).build(null));

        laserEnergyInputPortBlock = DCPContent.REGISTRY.registerBlockWithItem(
                "laser_energy_input_port",
                () -> new LaserEnergyInputPortBlock(BlockBehaviour.Properties.of()
                        .strength(3.5f)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()
                        .lightLevel(state -> 8)),
                holder -> () -> new BlockItem(holder.get(), new Item.Properties()),
                DCPContent.TAB);
        laserEnergyInputPortBlockEntity = DCPContent.REGISTRY.registerBlockEntityType("laser_energy_input_port", () ->
                BlockEntityType.Builder.of(LaserEnergyInputPortBlockEntity::new,
                        getLaserEnergyInputPortBlock()).build(null));

        bigNumberEnergyOutputPortBlock = DCPContent.REGISTRY.registerBlockWithItem(
                "bignumber_energy_output_port",
                () -> new BigNumberEnergyOutputPortBlock(BlockBehaviour.Properties.of()
                        .strength(3.5f)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()
                        .lightLevel(state -> 8)),
                holder -> () -> new BlockItem(holder.get(), new Item.Properties()),
                DCPContent.TAB);
        bigNumberEnergyOutputPortBlockEntity = DCPContent.REGISTRY.registerBlockEntityType(
                "bignumber_energy_output_port", () -> BlockEntityType.Builder.of(
                        BigNumberEnergyOutputPortBlockEntity::new, getBigNumberEnergyOutputPortBlock()).build(null));

        miningItemOutputPortBlock = DCPContent.REGISTRY.registerBlockWithItem(
                "mining_item_output_port",
                () -> new MiningItemOutputPortBlock(BlockBehaviour.Properties.of()
                        .strength(3.5f)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()
                        .lightLevel(state -> 5)),
                holder -> () -> new BlockItem(holder.get(), new Item.Properties()),
                DCPContent.TAB);
        miningItemOutputPortBlockEntity = DCPContent.REGISTRY.registerBlockEntityType(
                "mining_item_output_port", () -> BlockEntityType.Builder.of(
                        MiningItemOutputPortBlockEntity::new, getMiningItemOutputPortBlock()).build(null));

        miningFluidOutputPortBlock = DCPContent.REGISTRY.registerBlockWithItem(
                "mining_fluid_output_port",
                () -> new MiningFluidOutputPortBlock(BlockBehaviour.Properties.of()
                        .strength(3.5f)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()
                        .lightLevel(state -> 5)),
                holder -> () -> new BlockItem(holder.get(), new Item.Properties()),
                DCPContent.TAB);
        miningFluidOutputPortBlockEntity = DCPContent.REGISTRY.registerBlockEntityType(
                "mining_fluid_output_port", () -> BlockEntityType.Builder.of(
                        MiningFluidOutputPortBlockEntity::new, getMiningFluidOutputPortBlock()).build(null));

        oreProcessingHubBlock = DCPContent.REGISTRY.registerBlockWithItem(
                "ore_processing_hub",
                () -> new OreProcessingHubBlock(BlockBehaviour.Properties.of()
                        .strength(4.0f)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()
                        .lightLevel(state -> 7)),
                holder -> () -> new BlockItem(holder.get(), new Item.Properties()),
                DCPContent.TAB);
        oreProcessingHubBlockEntity = DCPContent.REGISTRY.registerBlockEntityType(
                "ore_processing_hub", () -> BlockEntityType.Builder.of(
                        OreProcessingHubBlockEntity::new, getOreProcessingHubBlock()).build(null));
    }
}

