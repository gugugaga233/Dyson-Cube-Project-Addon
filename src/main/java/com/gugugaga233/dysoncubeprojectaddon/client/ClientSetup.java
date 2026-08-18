/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hrznstudio.titanium.event.handler.EventManager
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.math.Transformation
 *  net.minecraft.client.renderer.ShaderInstance
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.Material
 *  net.minecraft.client.resources.model.ModelBaker
 *  net.minecraft.client.resources.model.ModelBakery
 *  net.minecraft.client.resources.model.ModelBakery$ModelBakerImpl
 *  net.minecraft.client.resources.model.ModelResourceLocation
 *  net.minecraft.client.resources.model.ModelState
 *  net.minecraft.client.resources.model.UnbakedModel
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.neoforged.neoforge.client.event.EntityRenderersEvent$RegisterRenderers
 *  net.neoforged.neoforge.client.event.ModelEvent$BakingCompleted
 *  net.neoforged.neoforge.client.event.RegisterShadersEvent
 *  net.neoforged.neoforge.client.event.RenderHighlightEvent$Block
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent
 *  net.neoforged.neoforge.client.model.SimpleModelState
 *  net.neoforged.neoforge.event.entity.player.ItemTooltipEvent
 */
package com.gugugaga233.dysoncubeprojectaddon.client;

import com.gugugaga233.dysoncubeprojectaddon.DCPAttachments;
import com.gugugaga233.dysoncubeprojectaddon.DCPContent;
import com.gugugaga233.dysoncubeprojectaddon.client.DCPExtraModels;
import com.gugugaga233.dysoncubeprojectaddon.client.DCPShaders;
import com.gugugaga233.dysoncubeprojectaddon.client.render.HologramRender;
import com.gugugaga233.dysoncubeprojectaddon.client.render.SkyRender;
import com.gugugaga233.dysoncubeprojectaddon.client.gui.SubscribeDysonGuiAddon;
import com.gugugaga233.dysoncubeprojectaddon.client.tile.EMRailEjectorRender;
import com.gugugaga233.dysoncubeprojectaddon.client.tile.RayReceiverRender;
import com.gugugaga233.dysoncubeprojectaddon.item.CompressedItem;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.client.render.StarTextureManager;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.model.SimpleModelState;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public class ClientSetup {
    private static final ModelResourceLocation EM_RAILEJECTOR_BASE = standaloneModel("block/em_railejector_base");
    private static final ModelResourceLocation EM_RAILEJECTOR_GUN = standaloneModel("block/em_railejector_gun");
    private static final ModelResourceLocation EM_RAILEJECTOR_PROJECTILE = standaloneModel("block/em_railejector_projectile");
    private static final ModelResourceLocation RAY_RECEIVER_BASE = standaloneModel("block/ray_receiver_base");
    private static final ModelResourceLocation RAY_RECEIVER_PLATE = standaloneModel("block/ray_receiver_plate");
    private static final ModelResourceLocation RAY_RECEIVER_LENS = standaloneModel("block/ray_receiver_lens");
    private static final ModelResourceLocation RAY_RECEIVER_LENS_STANDS = standaloneModel("block/ray_receiver_lens_stands");

    public static void init() {
        ClientPacketHandlers.register();
        EventManager.forge(ScreenEvent.Closing.class)
                .process(event -> SubscribeDysonGuiAddon.unsubscribeActive()).subscribe();
        EventManager.forge(RenderHighlightEvent.Block.class).process(HologramRender::blockOverlayEvent).subscribe();
        EventManager.forge(RenderLevelStageEvent.class).process(SkyRender::onRenderStage).subscribe();
        EventManager.mod(RegisterShadersEvent.class).process(ClientSetup::registerShaders).subscribe();
        EventManager.mod(RegisterClientReloadListenersEvent.class).process(event ->
                event.registerReloadListener((ResourceManagerReloadListener)
                        resourceManager -> StarTextureManager.clear())).subscribe();
        EventManager.mod(ModelEvent.RegisterAdditional.class).process(event -> {
            event.register(EM_RAILEJECTOR_BASE);
            event.register(EM_RAILEJECTOR_GUN);
            event.register(EM_RAILEJECTOR_PROJECTILE);
            event.register(RAY_RECEIVER_BASE);
            event.register(RAY_RECEIVER_PLATE);
            event.register(RAY_RECEIVER_LENS);
            event.register(RAY_RECEIVER_LENS_STANDS);
        }).subscribe();
        EventManager.mod(ModelEvent.BakingCompleted.class).process(event -> {
            DCPExtraModels.EM_RAILEJECTOR_BASE = event.getModels().get(EM_RAILEJECTOR_BASE);
            DCPExtraModels.EM_RAILEJECTOR_GUN = event.getModels().get(EM_RAILEJECTOR_GUN);
            DCPExtraModels.EM_RAILEJECTOR_PROJECTILE = event.getModels().get(EM_RAILEJECTOR_PROJECTILE);
            DCPExtraModels.RAY_RECEIVER_BASE = event.getModels().get(RAY_RECEIVER_BASE);
            DCPExtraModels.RAY_RECEIVER_PLATE = event.getModels().get(RAY_RECEIVER_PLATE);
            DCPExtraModels.RAY_RECEIVER_LENS = event.getModels().get(RAY_RECEIVER_LENS);
            DCPExtraModels.RAY_RECEIVER_LENS_STANDS = event.getModels().get(RAY_RECEIVER_LENS_STANDS);
        }).subscribe();
        EventManager.forge(ItemTooltipEvent.class).process(itemTooltipEvent -> {
            ItemStack stack = itemTooltipEvent.getItemStack();
            if (!(stack.getItem() instanceof CompressedItem)
                    && (Integer)stack.getOrDefault(DCPAttachments.SOLAR_SAIL, (Object)0) > 0) {
                itemTooltipEvent.getToolTip().add(Component.translatable((String)"tooltip.dysoncubeproject.contains_solar_sails", (Object[])new Object[]{stack.getOrDefault(DCPAttachments.SOLAR_SAIL, (Object)0)}).withColor(DCPContent.CYAN_COLOR));
            }
            if (!(stack.getItem() instanceof CompressedItem)
                    && (Integer)stack.getOrDefault(DCPAttachments.BEAM, (Object)0) > 0) {
                itemTooltipEvent.getToolTip().add(Component.translatable((String)"tooltip.dysoncubeproject.contains_beams", (Object[])new Object[]{stack.getOrDefault(DCPAttachments.BEAM, (Object)0)}).withColor(DCPContent.CYAN_COLOR));
            }
            if (stack.is(DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER.asItem())) {
                itemTooltipEvent.getToolTip().add(Component.translatable((String)"tooltip.dysoncubeproject.power_optional").withColor(DCPContent.CYAN_COLOR));
            }
        }).subscribe();
    }

    public static void registerShaders(RegisterShadersEvent event) {
        ShaderInstance shader;
        try {
            shader = new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath((String)"dysoncubeproject", (String)"hologram"), DefaultVertexFormat.POSITION_COLOR);
            event.registerShader(shader, s -> {
                DCPShaders.HOLOGRAM = s;
        });
        }
        catch (Exception e) {
            DCPShaders.HOLOGRAM = null;
        }
        try {
            shader = new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath((String)"dysoncubeproject", (String)"dyson_sun"), DefaultVertexFormat.POSITION_COLOR);
            event.registerShader(shader, s -> {
                DCPShaders.DYSON_SUN = s;
            });
        }
        catch (Exception e) {
            DCPShaders.DYSON_SUN = null;
        }
        try {
            shader = new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath((String)"dysoncubeproject", (String)"holo_hex"), DefaultVertexFormat.POSITION_COLOR);
            event.registerShader(shader, s -> {
                DCPShaders.HOLO_HEX = s;
            });
        }
        catch (Exception e) {
            DCPShaders.HOLO_HEX = null;
        }
        try {
            shader = new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath((String)"dysoncubeproject", (String)"rail_electric"), DefaultVertexFormat.POSITION_COLOR);
            event.registerShader(shader, s -> {
                DCPShaders.RAIL_ELECTRIC = s;
            });
        }
        catch (Exception e) {
            DCPShaders.RAIL_ELECTRIC = null;
        }
        try {
            shader = new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath((String)"dysoncubeproject", (String)"rail_beam"), DefaultVertexFormat.POSITION_COLOR);
            event.registerShader(shader, s -> {
                DCPShaders.RAIL_BEAM = s;
            });
        }
        catch (Exception e) {
            DCPShaders.RAIL_BEAM = null;
        }
    }

    private static ModelResourceLocation standaloneModel(String path) {
        return new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath("dysoncubeproject", path), "standalone");
        /*
            // 使用 access transformer 开放的 ModelBakerImpl 构造函数
            // 由于 1.21.1 API 差异，此方法可能返回 null（fallback 到默认渲染）
            // 原代码：new ModelBakery.ModelBakerImpl(modelBakery, ...)
            return null;
        } catch (Exception e) {
            return null;
        }*/
    }
}

