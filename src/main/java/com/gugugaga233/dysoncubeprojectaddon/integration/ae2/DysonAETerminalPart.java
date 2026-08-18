package com.gugugaga233.dysoncubeprojectaddon.integration.ae2;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.ItemTerminalPart;
import com.gugugaga233.dysoncubeprojectaddon.DysonCubeProject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public final class DysonAETerminalPart extends ItemTerminalPart {
    private static final ResourceLocation MODEL_OFF = id("part/dyson_ae_terminal_off");
    private static final ResourceLocation MODEL_ON = id("part/dyson_ae_terminal_on");
    private static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    private static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    private static final IPartModel MODELS_HAS_CHANNEL =
            new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);
    private static boolean modelsRegistered;

    public DysonAETerminalPart(IPartItem<?> partItem) {
        super(partItem);
    }

    public static synchronized void registerModels() {
        if (!modelsRegistered) {
            PartModels.registerModels(MODEL_OFF, MODEL_ON);
            modelsRegistered = true;
        }
    }

    @Override
    public MenuType<?> getMenuType(Player player) {
        return AE2Integration.DYSON_AE_TERMINAL_MENU.get();
    }

    @Override
    public IPartModel getStaticModels() {
        return selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(DysonCubeProject.MODID, path);
    }
}

