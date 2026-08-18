/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hrznstudio.titanium.event.handler.EventManager
 *  com.hrznstudio.titanium.module.ModuleController
 *  com.hrznstudio.titanium.network.Message
 *  com.hrznstudio.titanium.network.NetworkHandler
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  com.mojang.logging.LogUtils
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.commands.SharedSuggestionProvider
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.DataProvider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.dimension.BuiltinDimensionTypes
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.fml.common.Mod
 *  net.neoforged.neoforge.capabilities.Capabilities$EnergyStorage
 *  net.neoforged.neoforge.capabilities.Capabilities$ItemHandler
 *  net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
 *  net.neoforged.neoforge.data.event.GatherDataEvent
 *  net.neoforged.neoforge.event.RegisterCommandsEvent
 *  net.neoforged.neoforge.event.tick.LevelTickEvent$Pre
 *  org.slf4j.Logger
 */
package com.gugugaga233.dysoncubeprojectaddon;

import com.gugugaga233.dysoncubeprojectaddon.DCPAttachments;
import com.gugugaga233.dysoncubeprojectaddon.DCPContent;
import com.gugugaga233.dysoncubeprojectaddon.item.CompressedItem;
import com.gugugaga233.dysoncubeprojectaddon.block.tile.EMRailEjectorBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.block.tile.RayReceiverBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.bridge.OriginalRayReceiverBridge;
import com.gugugaga233.dysoncubeprojectaddon.client.ClientSetup;
import com.gugugaga233.dysoncubeprojectaddon.datagen.DCPBlockTagsProvider;
import com.gugugaga233.dysoncubeprojectaddon.datagen.DCPBlockstateProvider;
import com.gugugaga233.dysoncubeprojectaddon.datagen.DCPLangItemProvider;
import com.gugugaga233.dysoncubeprojectaddon.datagen.DCPLootTableDataProvider;
import com.gugugaga233.dysoncubeprojectaddon.datagen.DCPRecipesProvider;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientSubscribeSphereMessage;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientUnsubscribeSphereMessage;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientboundCosmicHeartFeedPacket;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientboundCosmicHeartRewardPacket;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientboundOpenDysonHubPacket;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientboundOpenCosmicMiningHubPacket;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientboundAE2ExactAmountsPacket;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientboundPrimordialQiChoicePacket;
import com.gugugaga233.dysoncubeprojectaddon.network.DysonSphereSyncMessage;
import com.gugugaga233.dysoncubeprojectaddon.network.ServerboundCosmicHeartFeedPacket;
import com.gugugaga233.dysoncubeprojectaddon.network.ServerboundCosmicHeartRewardPacket;
import com.gugugaga233.dysoncubeprojectaddon.network.ServerboundPrimordialQiChoicePacket;
import com.gugugaga233.dysoncubeprojectaddon.network.ServerboundCosmicMiningHubPacket;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientboundOpenOreProcessingHubPacket;
import com.gugugaga233.dysoncubeprojectaddon.network.ServerboundOreProcessingHubPacket;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.OverhaulContent;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.DysonHubBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.CosmicMiningHubBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.LaserEnergyInputPortBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.BigNumberEnergyOutputPortBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.MiningFluidOutputPortBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.MiningItemOutputPortBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.OreProcessingHubBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereProgressSavedData;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereStructure;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.module.ModuleController;
import com.hrznstudio.titanium.network.Message;
import com.hrznstudio.titanium.network.NetworkHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.logging.LogUtils;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.slf4j.Logger;
import sonar.fluxnetworks.api.FluxCapabilities;
import sonar.fluxnetworks.api.energy.BigNumber;

@Mod(value=DysonCubeProject.MODID)
public class DysonCubeProject
extends ModuleController {
    public static final String MODID = "dysoncubeproject_addon";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static NetworkHandler NETWORK = new NetworkHandler(MODID);
    private static final Map<UUID, Boolean> COSMIC_HEART_SELECTION_PROTECTION = new HashMap<>();
    // Titanium invokes initModules from its superclass constructor. Load the
    // historical-namespace content before that lifecycle starts so its
    // DeferredRegisters are attached before registry events are dispatched.
    static {
        DCPContent.Items.init();
    }
    /** 最后一个已加载的 RecipeManager（由 AddReloadListenerEvent 填充，供动态配方注入使用） */
    /** 最后一个已加载的 HolderLookup.Provider（由 AddReloadListenerEvent 填充） */

    public DysonCubeProject(Dist dist, IEventBus modEventBus, ModContainer modContainer) {
        super(modContainer);
        modContainer.registerConfig(ModConfig.Type.COMMON, DCPConfig.SPEC,
                "dysoncubeproject-addon-common.toml");
        NETWORK.registerMessage("dyson_sphere_sync", DysonSphereSyncMessage.class);
        NETWORK.registerMessage("client_subscribe_sphere", ClientSubscribeSphereMessage.class);
        NETWORK.registerMessage("client_unsubscribe_sphere", ClientUnsubscribeSphereMessage.class);
        // V5.3 网络包注册
        NETWORK.registerMessage("clientbound_primordial_qi_choice", ClientboundPrimordialQiChoicePacket.class);
        NETWORK.registerMessage("serverbound_primordial_qi_choice", ServerboundPrimordialQiChoicePacket.class);
        NETWORK.registerMessage("clientbound_cosmic_heart_reward", ClientboundCosmicHeartRewardPacket.class);
        NETWORK.registerMessage("serverbound_cosmic_heart_reward", ServerboundCosmicHeartRewardPacket.class);
        NETWORK.registerMessage("serverbound_cosmic_heart_feed", ServerboundCosmicHeartFeedPacket.class);
        NETWORK.registerMessage("clientbound_cosmic_heart_feed", ClientboundCosmicHeartFeedPacket.class);
        NETWORK.registerMessage("clientbound_open_dyson_hub", ClientboundOpenDysonHubPacket.class);
        NETWORK.registerMessage("clientbound_open_cosmic_mining_hub", ClientboundOpenCosmicMiningHubPacket.class);
        NETWORK.registerMessage("serverbound_cosmic_mining_hub", ServerboundCosmicMiningHubPacket.class);
        NETWORK.registerMessage("clientbound_open_ore_processing_hub", ClientboundOpenOreProcessingHubPacket.class);
        NETWORK.registerMessage("serverbound_ore_processing_hub", ServerboundOreProcessingHubPacket.class);
        if (ModList.get().isLoaded("ae2")) {
            NETWORK.registerMessage("clientbound_ae2_exact_amounts", ClientboundAE2ExactAmountsPacket.class);
        }
        if (dist == Dist.CLIENT) {
            ClientSetup.init();
        }
        EventManager.forge(LevelTickEvent.Pre.class).process(post -> {
            ServerLevel serverLevel;
            Level patt0$temp = post.getLevel();
            if (patt0$temp instanceof ServerLevel && (serverLevel = (ServerLevel)patt0$temp).dimensionTypeRegistration().getRegisteredName().equals(BuiltinDimensionTypes.OVERWORLD.location().toString())) {
                DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get((Level)serverLevel);
                if (post.getLevel().getGameTime() % 20L == 0L) {
                    // Persistent sphere selection is separate from runtime viewers. A closed UI
                    // must not keep rebuilding and broadcasting the full Dyson state.
                    java.util.Map<String, String> activeViewers = data.getActiveSyncSubscribers();
                    activeViewers.keySet().removeIf(uuid -> {
                        try {
                            return serverLevel.getServer().getPlayerList()
                                    .getPlayer(java.util.UUID.fromString(uuid)) == null;
                        } catch (IllegalArgumentException ignored) {
                            return true;
                        }
                    });
                    java.util.Map<String, DysonSphereSyncMessage> packetsBySphere = new java.util.HashMap<>();
                    for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
                        String viewedSphere = activeViewers.get(player.getStringUUID());
                        if (viewedSphere != null) {
                            DysonSphereSyncMessage packet = packetsBySphere.computeIfAbsent(viewedSphere,
                                    sphereId -> new DysonSphereSyncMessage(data.getCachedClientSyncTag(
                                            (HolderLookup.Provider)serverLevel.getServer().registryAccess(), sphereId)));
                            NETWORK.sendTo((Message)packet, player);
                        }
                        com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereStructure sphere =
                                data.getSpheres().get(player.getUUID().toString());
                        com.gugugaga233.dysoncubeprojectaddon.overhaul.PrimordialQiManager
                                .remindPendingChoice(player, sphere);
                    }
                }
                if (post.getLevel().getGameTime() % 5L == 0L) {
                    data.synchronizeCosmicHeartEffects();
                    data.getSpheres().values().forEach(sphere -> sphere.generatePower(5L));
                    // Power generation mutates the persisted energy buffer. Mark it once per
                    // five-tick batch so saves remain correct without dirtying every tick.
                    if (!data.getSpheres().isEmpty()) data.setDirty();
                }
                // V5.3：检测寰宇之心完成
                java.util.List<ServerPlayer> onlinePlayers =
                        serverLevel.getServer().getPlayerList().getPlayers();
                COSMIC_HEART_SELECTION_PROTECTION.keySet().removeIf(uuid ->
                        serverLevel.getServer().getPlayerList().getPlayer(uuid) == null);
                if (data.isCosmicHeartComplete()
                        && COSMIC_HEART_SELECTION_PROTECTION.isEmpty()) {
                    data.getCosmicHeart().resetCompletionNotification();
                }
                if (data.isCosmicHeartComplete() && !onlinePlayers.isEmpty()) {
                    com.gugugaga233.dysoncubeprojectaddon.overhaul.CosmicHeart heart =
                            data.getCosmicHeart();
                    sonar.fluxnetworks.api.energy.AbsoluteInteger[] autoRewards =
                            heart.claimPlannedRewardBatch();
                    sonar.fluxnetworks.api.energy.AbsoluteInteger autoRewardCount =
                            new sonar.fluxnetworks.api.energy.AbsoluteInteger();
                    for (sonar.fluxnetworks.api.energy.AbsoluteInteger count : autoRewards) {
                        com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8
                                .addInPlace(autoRewardCount, count);
                    }
                    if (!autoRewardCount.isZero()) {
                        data.applyCosmicHeartRewardBatch(autoRewards);
                        serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                                Component.translatable(
                                        "message.dysoncubeproject.cosmic_heart.reward_auto_batch",
                                        com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils
                                                .getScientificInteger(autoRewardCount),
                                        heart.getPlannedRewardCountDisplay()), false);
                    }
                    if (heart.isComplete()
                            && heart.checkAndNotifyComplete(onlinePlayers.getFirst())) {
                        // The reward is global. One selector avoids locking every online player
                        // into duplicate screens and leaving the other protections behind.
                        ServerPlayer selector = onlinePlayers.getFirst();
                        beginCosmicHeartSelection(selector);
                        com.gugugaga233.dysoncubeprojectaddon.overhaul.UniverseEvents.notifyCosmicHeartComplete(selector);
                        com.gugugaga233.dysoncubeprojectaddon.DysonCubeProject.NETWORK.sendTo(
                                new com.gugugaga233.dysoncubeprojectaddon.network.ClientboundCosmicHeartRewardPacket(
                                        heart.getAppliedRewardCounts(), heart.getBatchCountDisplay()), selector);
                    }
                }
            }
        }).subscribe();
        EventManager.mod(RegisterCapabilitiesEvent.class).process(event -> {
            if (ModList.get().isLoaded("ae2")) {
                com.gugugaga233.dysoncubeprojectaddon.integration.ae2.AE2Integration.registerCapabilities(event);
            }
            event.registerBlock(Capabilities.EnergyStorage.BLOCK, (level, blockPos, blockState, blockEntity, direction) -> {
                if (blockEntity instanceof OriginalRayReceiverBridge receiver && direction == Direction.DOWN) {
                    return receiver.dcpAddon$getForgeEnergyStorage();
                }
                return null;
            }, new Block[]{DCPContent.Blocks.RAY_RECEIVER_CONTROLLER.getBlock()});
            event.registerBlock(FluxCapabilities.BIG_NUMBER_BLOCK, (level, blockPos, blockState, blockEntity, direction) -> {
                if (blockEntity instanceof OriginalRayReceiverBridge receiver && direction == Direction.DOWN) {
                    return receiver.dcpAddon$getBigEnergyStorage();
                }
                return null;
            }, new Block[]{DCPContent.Blocks.RAY_RECEIVER_CONTROLLER.getBlock()});
            event.registerBlock(FluxCapabilities.BLOCK, (level, blockPos, blockState, blockEntity, direction) -> {
                if (blockEntity instanceof OriginalRayReceiverBridge receiver && direction == Direction.DOWN) {
                    return receiver.dcpAddon$getFluxEnergyStorage();
                }
                return null;
            }, new Block[]{DCPContent.Blocks.RAY_RECEIVER_CONTROLLER.getBlock()});
            event.registerBlock(Capabilities.EnergyStorage.BLOCK, (level, blockPos, blockState, blockEntity, direction) -> {
                if (blockEntity instanceof DysonHubBlockEntity hub) {
                    return hub.getForgeEnergyStorage();
                }
                return null;
            }, new Block[]{OverhaulContent.getDysonHubBlock()});
            event.registerBlock(FluxCapabilities.BIG_NUMBER_BLOCK, (level, blockPos, blockState, blockEntity, direction) -> {
                if (blockEntity instanceof DysonHubBlockEntity hub) {
                    return hub.getBigEnergyStorage();
                }
                return null;
            }, new Block[]{OverhaulContent.getDysonHubBlock()});
            event.registerBlock(FluxCapabilities.BLOCK, (level, blockPos, blockState, blockEntity, direction) -> {
                if (blockEntity instanceof DysonHubBlockEntity hub) {
                    return hub.getFluxEnergyStorage();
                }
                return null;
            }, new Block[]{OverhaulContent.getDysonHubBlock()});
            event.registerBlock(Capabilities.EnergyStorage.BLOCK, (level, blockPos, blockState, blockEntity, direction) ->
                    blockEntity instanceof CosmicMiningHubBlockEntity hub ? hub.getForgeEnergyStorage() : null,
                    new Block[]{OverhaulContent.getCosmicMiningHubBlock()});
            event.registerBlock(FluxCapabilities.BIG_NUMBER_BLOCK, (level, blockPos, blockState, blockEntity, direction) ->
                    blockEntity instanceof CosmicMiningHubBlockEntity hub ? hub.getBigEnergyStorage() : null,
                    new Block[]{OverhaulContent.getCosmicMiningHubBlock()});
            event.registerBlock(FluxCapabilities.BLOCK, (level, blockPos, blockState, blockEntity, direction) ->
                    blockEntity instanceof CosmicMiningHubBlockEntity hub ? hub.getFluxEnergyStorage() : null,
                    new Block[]{OverhaulContent.getCosmicMiningHubBlock()});
            event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, blockPos, blockState, blockEntity, direction) ->
                    blockEntity instanceof CosmicMiningHubBlockEntity hub ? hub.getOutputItems() : null,
                    new Block[]{OverhaulContent.getCosmicMiningHubBlock()});
            event.registerBlock(Capabilities.FluidHandler.BLOCK, (level, blockPos, blockState, blockEntity, direction) ->
                    blockEntity instanceof CosmicMiningHubBlockEntity hub ? hub.getOutputFluids() : null,
                    new Block[]{OverhaulContent.getCosmicMiningHubBlock()});
            event.registerBlock(Capabilities.EnergyStorage.BLOCK, (level, blockPos, blockState, blockEntity, direction) ->
                    blockEntity instanceof LaserEnergyInputPortBlockEntity port ? port.getForgeEnergyStorage() : null,
                    new Block[]{OverhaulContent.getLaserEnergyInputPortBlock()});
            event.registerBlock(FluxCapabilities.BIG_NUMBER_BLOCK, (level, blockPos, blockState, blockEntity, direction) ->
                    blockEntity instanceof LaserEnergyInputPortBlockEntity port ? port.getBigEnergyStorage() : null,
                    new Block[]{OverhaulContent.getLaserEnergyInputPortBlock()});
            event.registerBlock(FluxCapabilities.BLOCK, (level, blockPos, blockState, blockEntity, direction) ->
                    blockEntity instanceof LaserEnergyInputPortBlockEntity port ? port.getFluxEnergyStorage() : null,
                    new Block[]{OverhaulContent.getLaserEnergyInputPortBlock()});
            event.registerBlock(FluxCapabilities.BIG_NUMBER_BLOCK, (level, blockPos, blockState, blockEntity, direction) ->
                    blockEntity instanceof BigNumberEnergyOutputPortBlockEntity port
                            ? port.getBigEnergyStorage() : null,
                    new Block[]{OverhaulContent.getBigNumberEnergyOutputPortBlock()});
            event.registerBlock(FluxCapabilities.BLOCK, (level, blockPos, blockState, blockEntity, direction) ->
                    blockEntity instanceof BigNumberEnergyOutputPortBlockEntity port
                            ? port.getFluxEnergyStorage() : null,
                    new Block[]{OverhaulContent.getBigNumberEnergyOutputPortBlock()});
            event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                    (level, blockPos, blockState, blockEntity, direction) ->
                            blockEntity instanceof BigNumberEnergyOutputPortBlockEntity port
                                    ? port.getForgeEnergyStorage() : null,
                    new Block[]{OverhaulContent.getBigNumberEnergyOutputPortBlock()});
            event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, blockPos, blockState, blockEntity, direction) ->
                    blockEntity instanceof MiningItemOutputPortBlockEntity port
                            ? port.getOutputHandler() : null,
                    new Block[]{OverhaulContent.getMiningItemOutputPortBlock()});
            event.registerBlock(Capabilities.FluidHandler.BLOCK, (level, blockPos, blockState, blockEntity, direction) ->
                    blockEntity instanceof MiningFluidOutputPortBlockEntity port
                            ? port.getOutputHandler() : null,
                    new Block[]{OverhaulContent.getMiningFluidOutputPortBlock()});
            event.registerBlock(Capabilities.ItemHandler.BLOCK,
                    (level, blockPos, blockState, blockEntity, direction) ->
                            blockEntity instanceof OreProcessingHubBlockEntity hub
                                    ? hub.getOutputHandler() : null,
                    new Block[]{OverhaulContent.getOreProcessingHubBlock()});
        }).subscribe();
        EventManager.forge(RegisterCommandsEvent.class).process(event -> {
            CommandDispatcher dispatcher = event.getDispatcher();
            SuggestionProvider sphereIdSuggestions = (ctx, builder) -> {
                ServerLevel level = ((CommandSourceStack)ctx.getSource()).getLevel();
                DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get((Level)level);
                if (data != null) {
                    return SharedSuggestionProvider.suggest(data.getSpheres().keySet(), (SuggestionsBuilder)builder);
                }
                return SharedSuggestionProvider.suggest(List.of(), (SuggestionsBuilder)builder);
            };
            dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)MODID).requires(source -> source.hasPermission(4))).then(Commands.literal((String)"set").then(Commands.literal((String)"beams").then(Commands.argument((String)"sphereId", (ArgumentType)StringArgumentType.string()).suggests(sphereIdSuggestions).then(Commands.argument((String)"value", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
                CommandSourceStack source = (CommandSourceStack)ctx.getSource();
                ServerLevel level = source.getLevel();
                String sphereId = StringArgumentType.getString((CommandContext)ctx, (String)"sphereId");
                ParsedBigNumber input = parseStructureNumber(source,
                        StringArgumentType.getString((CommandContext)ctx, (String)"value"), false);
                if (input == null) return 0;
                DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get((Level)level);
                if (data == null) {
                    return 0;
                }
                DysonSphereStructure config = data.getSpheres().computeIfAbsent(sphereId, s -> new DysonSphereStructure());
                config.setBeams(input.value());
                data.setDirty();
                source.sendSuccess(() -> Component.literal((String)("Set beams for sphere '" + sphereId + "' to " + config.getBeams().toCalculationString())), true);
                return 1;
            })))))).then(Commands.literal((String)"set").then(Commands.literal((String)"panels").then(Commands.argument((String)"sphereId", (ArgumentType)StringArgumentType.string()).suggests(sphereIdSuggestions).then(Commands.argument((String)"value", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
                CommandSourceStack source = (CommandSourceStack)ctx.getSource();
                ServerLevel level = source.getLevel();
                String sphereId = StringArgumentType.getString((CommandContext)ctx, (String)"sphereId");
                ParsedBigNumber input = parseStructureNumber(source,
                        StringArgumentType.getString((CommandContext)ctx, (String)"value"), false);
                if (input == null) return 0;
                DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get((Level)level);
                if (data == null) {
                    return 0;
                }
                DysonSphereStructure config = data.getSpheres().computeIfAbsent(sphereId, s -> new DysonSphereStructure());
                config.setSolarPanels(input.value());
                data.setDirty();
                source.sendSuccess(() -> Component.literal((String)("Set solar panels for sphere '" + sphereId + "' to " + config.getSolarPanels().toCalculationString())), true);
                return 1;
            })))))).then(Commands.literal((String)"add").then(Commands.literal((String)"beams").then(Commands.argument((String)"sphereId", (ArgumentType)StringArgumentType.string()).suggests(sphereIdSuggestions).then(Commands.argument((String)"delta", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
                CommandSourceStack source = (CommandSourceStack)ctx.getSource();
                ServerLevel level = source.getLevel();
                String sphereId = StringArgumentType.getString((CommandContext)ctx, (String)"sphereId");
                ParsedBigNumber delta = parseStructureNumber(source,
                        StringArgumentType.getString((CommandContext)ctx, (String)"delta"), true);
                if (delta == null) return 0;
                DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get((Level)level);
                if (data == null) {
                    return 0;
                }
                DysonSphereStructure config = data.getSpheres().computeIfAbsent(sphereId, s -> new DysonSphereStructure());
                BigNumber newVal = config.getBeams();
                if (delta.negative()) newVal.subtract(delta.value());
                else newVal.add(delta.value());
                config.setBeams(newVal);
                data.setDirty();
                source.sendSuccess(() -> Component.literal((String)("Beams for sphere '" + sphereId + "' is now " + config.getBeams().toCalculationString())), true);
                return 1;
            })))))).then(Commands.literal((String)"add").then(Commands.literal((String)"panels").then(Commands.argument((String)"sphereId", (ArgumentType)StringArgumentType.string()).suggests(sphereIdSuggestions).then(Commands.argument((String)"delta", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
                CommandSourceStack source = (CommandSourceStack)ctx.getSource();
                ServerLevel level = source.getLevel();
                String sphereId = StringArgumentType.getString((CommandContext)ctx, (String)"sphereId");
                ParsedBigNumber delta = parseStructureNumber(source,
                        StringArgumentType.getString((CommandContext)ctx, (String)"delta"), true);
                if (delta == null) return 0;
                DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get((Level)level);
                if (data == null) {
                    return 0;
                }
                DysonSphereStructure config = data.getSpheres().computeIfAbsent(sphereId, s -> new DysonSphereStructure());
                BigNumber newVal = config.getSolarPanels();
                if (delta.negative()) newVal.subtract(delta.value());
                else newVal.add(delta.value());
                config.setSolarPanels(newVal);
                data.setDirty();
                source.sendSuccess(() -> Component.literal((String)("Solar panels for sphere '" + sphereId + "' is now " + config.getSolarPanels().toCalculationString())), true);
                return 1;
            }))))));
            dispatcher.register(Commands.literal(MODID)
                    .requires(source -> source.hasPermission(4))
                    .then(Commands.literal("force_next_cosmic_heart")
                            .executes(ctx -> forceNextCosmicHeart(ctx.getSource(), null))
                            .then(Commands.argument("sphereId", StringArgumentType.string())
                                    .suggests(sphereIdSuggestions)
                                    .executes(ctx -> forceNextCosmicHeart(
                                            (CommandSourceStack)ctx.getSource(),
                                            StringArgumentType.getString(ctx, "sphereId"))))));
            SuggestionProvider compressedItemSuggestions = (ctx, builder) ->
                    SharedSuggestionProvider.suggest(List.of(
                            "sail", "beam", "iron_block", "gold_block", "netherite_block"), builder);
            var compressedCountArgument = Commands.argument("count", IntegerArgumentType.integer(1, 64))
                    .executes(ctx -> giveCompressedItem(
                            (CommandSourceStack) ctx.getSource(),
                            EntityArgument.getPlayer(ctx, "player"),
                            StringArgumentType.getString(ctx, "type"),
                            StringArgumentType.getString(ctx, "level"),
                            IntegerArgumentType.getInteger(ctx, "count")));
            var compressedLevelArgument = Commands.argument("level", StringArgumentType.word())
                    .executes(ctx -> giveCompressedItem(
                            (CommandSourceStack) ctx.getSource(),
                            EntityArgument.getPlayer(ctx, "player"),
                            StringArgumentType.getString(ctx, "type"),
                            StringArgumentType.getString(ctx, "level"), 1))
                    .then(compressedCountArgument);
            var compressedTypeArgument = Commands.argument("type", StringArgumentType.word())
                    .suggests(compressedItemSuggestions)
                    .then(compressedLevelArgument);
            var compressedPlayerArgument = Commands.argument("player", EntityArgument.player())
                    .then(compressedTypeArgument);
            dispatcher.register(Commands.literal(MODID)
                    .requires(source -> source.hasPermission(4))
                    .then(Commands.literal("give_compressed")
                            .then(compressedPlayerArgument)));
        }).subscribe();
        DCPAttachments.DR.register(modEventBus);
        com.gugugaga233.dysoncubeprojectaddon.recipe.ModRecipeSerializers.DR.register(modEventBus);
        if (ModList.get().isLoaded("ae2")) {
            com.gugugaga233.dysoncubeprojectaddon.integration.ae2.AE2Integration.register(modEventBus);
            if (dist == Dist.CLIENT) {
                com.gugugaga233.dysoncubeprojectaddon.integration.ae2.client.AE2ClientIntegration.register(modEventBus);
            }
        }
        // Add a few useful milestones without enumerating every compression level.
        modEventBus.addListener(this::addCompressionCreativeVariants);
        // 注意：方块实体类型通过手动创建（非 DeferredRegister），已在 OverhaulContent 中初始化
    }

    /** Keeps a modal reward screen from turning into an unavoidable death in multiplayer. */
    public static void beginCosmicHeartSelection(ServerPlayer player) {
        if (player == null) return;
        COSMIC_HEART_SELECTION_PROTECTION.putIfAbsent(player.getUUID(), player.isInvulnerable());
        player.setInvulnerable(true);
    }

    public static void endCosmicHeartSelection(ServerPlayer player) {
        if (player == null) return;
        Boolean previous = COSMIC_HEART_SELECTION_PROTECTION.remove(player.getUUID());
        if (previous != null) player.setInvulnerable(previous);
    }

    protected void initModules() {
        this.addCreativeTab("main", () -> new ItemStack(DCPContent.Items.COMPRESSED_SAIL.get()),
                "dyson_cube_project_addon", DCPContent.TAB);
        DCPContent.Blocks.init();
        DCPContent.Items.init();
        if (ModList.get().isLoaded("ae2")) {
            initializeAE2Integration();
        }
        DCPContent.Sounds.init();
        // 初始化魔改内容
        OverhaulContent.init();
    }

    private static void initializeAE2Integration() {
        try {
            Class.forName("com.gugugaga233.dysoncubeprojectaddon.integration.ae2.AE2Integration")
                    .getMethod("init")
                    .invoke(null);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to initialize the AE2 integration", exception);
        }
    }

    public void addDataProvider(GatherDataEvent event) {
        super.addDataProvider(event);
        // Compression recipes are the five custom recipe JSON files in main resources.
    }

    private void addCompressionCreativeVariants(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().location().equals(ResourceLocation.fromNamespaceAndPath(MODID, "main"))) {
            return;
        }

        if (ModList.get().isLoaded("ae2")) {
            event.accept(com.gugugaga233.dysoncubeprojectaddon.integration.ae2.AE2Integration
                            .DYSON_AE_TERMINAL.get(),
                    CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
            event.accept(com.gugugaga233.dysoncubeprojectaddon.integration.ae2.AE2Integration
                            .DYSON_STORAGE_CELL.get(),
                    CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
            event.accept(com.gugugaga233.dysoncubeprojectaddon.integration.ae2.AE2Integration
                            .DYSON_FLUID_STORAGE_CELL.get(),
                    CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
            event.accept(com.gugugaga233.dysoncubeprojectaddon.integration.ae2.AE2Integration
                            .DYSON_IO_PORT_ITEM.get(),
                    CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
        }

        java.math.BigInteger[] milestones = {
                java.math.BigInteger.TEN,
                java.math.BigInteger.valueOf(100),
                java.math.BigInteger.valueOf(1000)
        };
        net.minecraft.world.item.Item[] items = {
                DCPContent.Items.COMPRESSED_SAIL.get(),
                DCPContent.Items.COMPRESSED_BEAM.get(),
                DCPContent.Items.COMPRESSED_IRON_BLOCK.get(),
                DCPContent.Items.COMPRESSED_GOLD_BLOCK.get(),
                DCPContent.Items.COMPRESSED_NETHERITE_BLOCK.get()
        };
        for (net.minecraft.world.item.Item item : items) {
            for (java.math.BigInteger level : milestones) {
                event.accept(com.gugugaga233.dysoncubeprojectaddon.item.CompressedItem.withLevel(item, level),
                        CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
            }
        }
    }

    private static ParsedBigNumber parseStructureNumber(CommandSourceStack source,
                                                         String input,
                                                         boolean allowNegative) {
        try {
            return parseStructureNumber(input, allowNegative);
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal("Invalid structure amount: " + exception.getMessage()));
            return null;
        }
    }

    private static int giveCompressedItem(CommandSourceStack source, ServerPlayer player,
                                          String type, String levelText, int count) {
        Item item = compressedItem(type);
        if (item == null) {
            source.sendFailure(Component.literal(
                    "Unknown compressed item: " + type
                            + ". Use sail, beam, iron_block, gold_block, or netherite_block."));
            return 0;
        }

        BigInteger level;
        try {
            level = parseCompressionLevel(levelText);
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal("Invalid compression level: " + exception.getMessage()));
            return 0;
        }

        ItemStack stack = CompressedItem.withLevel(item, level);
        stack.setCount(count);
        player.getInventory().placeItemBackInInventory(stack);
        source.sendSuccess(() -> Component.literal(
                "Gave " + count + " x " + type + " to " + player.getName().getString()
                        + " at compression level " + CompressedItem.abbreviate(level)), true);
        return count;
    }

    static Item compressedItem(String type) {
        return switch (type) {
            case "sail", "compressed_sail" -> DCPContent.Items.COMPRESSED_SAIL.get();
            case "beam", "compressed_beam" -> DCPContent.Items.COMPRESSED_BEAM.get();
            case "iron_block", "compressed_iron_block" -> DCPContent.Items.COMPRESSED_IRON_BLOCK.get();
            case "gold_block", "compressed_gold_block" -> DCPContent.Items.COMPRESSED_GOLD_BLOCK.get();
            case "netherite_block", "compressed_netherite_block" ->
                    DCPContent.Items.COMPRESSED_NETHERITE_BLOCK.get();
            default -> null;
        };
    }

    static BigInteger parseCompressionLevel(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("level is required");
        }
        String value = input.strip();
        if (value.startsWith("+")) {
            value = value.substring(1);
        }
        if (value.isEmpty()) {
            throw new IllegalArgumentException("level must be a positive decimal integer");
        }
        if (value.length() > 10_000) {
            throw new IllegalArgumentException("level may contain at most 10000 digits");
        }
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character < '0' || character > '9') {
                throw new IllegalArgumentException("level must be a positive decimal integer");
            }
        }
        BigInteger level = new BigInteger(value);
        if (level.signum() < 1) {
            throw new IllegalArgumentException("level must be at least 1");
        }
        return level;
    }

    static ParsedBigNumber parseStructureNumber(String input, boolean allowNegative) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("value is required");
        }

        String value = input.strip();
        boolean negative = value.startsWith("-");
        if (negative || value.startsWith("+")) value = value.substring(1);
        if (negative && !allowNegative) {
            throw new IllegalArgumentException("negative values are not allowed here");
        }
        if (value.isEmpty()) throw new IllegalArgumentException("value is required");

        int lowerExponentMarker = value.indexOf('e');
        int upperExponentMarker = value.indexOf('E');
        if (lowerExponentMarker != value.lastIndexOf('e')
                || upperExponentMarker != value.lastIndexOf('E')
                || lowerExponentMarker != -1 && upperExponentMarker != -1) {
            throw new IllegalArgumentException("use one scientific-notation exponent");
        }
        int exponentMarker = Math.max(lowerExponentMarker, upperExponentMarker);

        String coefficientText = exponentMarker < 0 ? value : value.substring(0, exponentMarker);
        String exponentText = exponentMarker < 0 ? "0" : value.substring(exponentMarker + 1);
        if (exponentText.startsWith("+")) exponentText = exponentText.substring(1);
        if (exponentText.isEmpty() || exponentText.startsWith("-")) {
            throw new IllegalArgumentException("the exponent must be a non-negative integer");
        }
        for (int index = 0; index < exponentText.length(); index++) {
            if (!Character.isDigit(exponentText.charAt(index))) {
                throw new IllegalArgumentException("the exponent must be a non-negative integer");
            }
        }

        BigDecimal coefficient;
        try {
            coefficient = new BigDecimal(coefficientText).stripTrailingZeros();
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("expected an integer or scientific notation such as 1e1000");
        }
        if (coefficient.signum() < 0) {
            throw new IllegalArgumentException("put the sign before the complete value");
        }
        if (coefficient.scale() > 0 && decimalIntegerLessThan(exponentText, coefficient.scale())) {
            throw new IllegalArgumentException("structure amounts must resolve to whole items");
        }

        BigNumber parsed = new BigNumber(coefficient);
        parsed.addExponent(exponentText);
        parsed.normalize();
        return new ParsedBigNumber(parsed, negative && !parsed.isZero());
    }

    private static boolean decimalIntegerLessThan(String value, int comparison) {
        int firstNonZero = 0;
        while (firstNonZero < value.length() && value.charAt(firstNonZero) == '0') firstNonZero++;
        String significant = firstNonZero == value.length() ? "0" : value.substring(firstNonZero);
        String target = Integer.toString(comparison);
        return significant.length() < target.length()
                || significant.length() == target.length() && significant.compareTo(target) < 0;
    }

    record ParsedBigNumber(BigNumber value, boolean negative) {
    }

    private static int forceNextCosmicHeart(CommandSourceStack source, String requestedSphereId) {
        DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(source.getLevel());
        String sphereId = requestedSphereId;
        if (sphereId == null) {
            if (!(source.getEntity() instanceof ServerPlayer player)) {
                source.sendFailure(Component.translatable("commands.dysoncubeproject.force_cosmic_heart.console_requires_sphere"));
                return 0;
            }
            sphereId = data.getSubscribedFor(player.getStringUUID());
        }

        DysonSphereStructure sphere = data.getSpheres().get(sphereId);
        if (sphere == null) {
            source.sendFailure(Component.translatable("commands.dysoncubeproject.force_cosmic_heart.not_found", sphereId));
            return 0;
        }
        if (!sphere.armForcedCosmicHeart()) {
            source.sendFailure(Component.translatable("commands.dysoncubeproject.force_cosmic_heart.already_armed", sphereId));
            return 0;
        }

        data.setDirty();
        String armedSphereId = sphereId;
        source.sendSuccess(() -> Component.translatable(
                "commands.dysoncubeproject.force_cosmic_heart.success", armedSphereId), true);
        return 1;
    }
}

