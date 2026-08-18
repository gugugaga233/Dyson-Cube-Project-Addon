/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon
 *  com.hrznstudio.titanium.client.screen.asset.IAssetProvider
 *  com.hrznstudio.titanium.util.AssetUtil
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 */
package com.gugugaga233.dysoncubeprojectaddon.client.gui;

import com.gugugaga233.dysoncubeprojectaddon.DCPContent;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.UniverseHierarchy;
import com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils;
import com.gugugaga233.dysoncubeprojectaddon.world.ClientDysonSphere;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereStructure;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.util.AssetUtil;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class DysonProgressGuiAddon
extends BasicScreenAddon {
    private static final int WIDTH = 108;
    private static final int HEIGHT = 68;
    private static final int LINE_HEIGHT = 9;
    private final String dysonID;
    private List<Component> tooltipLines = List.of();

    public DysonProgressGuiAddon(String dysonID, int posX, int posY) {
        super(posX, posY);
        this.dysonID = dysonID;
    }

    public int getXSize() {
        return WIDTH;
    }

    public int getYSize() {
        return HEIGHT;
    }

    public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        DysonSphereStructure dyson = ClientDysonSphere.DYSON_SPHERE_PROGRESS.getSpheres().computeIfAbsent(this.dysonID, s -> new DysonSphereStructure());
        Font font = Minecraft.getInstance().font;
        int x = this.getPosX() + guiX;
        int top = this.getPosY() + guiY;
        sonar.fluxnetworks.api.energy.AbsoluteInteger wrapped = dyson.getTotalWrappedExact();
        sonar.fluxnetworks.api.energy.AbsoluteInteger hierarchyEffectLayers =
                ClientDysonSphere.DYSON_SPHERE_PROGRESS.getCosmicHeart()
                        .getHierarchyEffectLayersExact();
        dyson.setCosmicHeartOutputLayers(
                ClientDysonSphere.DYSON_SPHERE_PROGRESS.getCosmicHeart()
                        .getModificationCountExact(
                                com.gugugaga233.dysoncubeprojectaddon.overhaul.CosmicHeart
                                        .RuleModification.ALL_STAR_OUTPUT_X2));
        UniverseHierarchy.Level hierarchy = UniverseHierarchy.getUnlockedLevel(wrapped);
        String batch = NumberUtils.getScientificInteger(
                UniverseHierarchy.effectiveBatchSize(wrapped, hierarchyEffectLayers));
        String next = NumberUtils.getScientificInteger(UniverseHierarchy.nextBatchThreshold(wrapped));

        String power = NumberUtils.getFormatedBigNumber(dyson.getRawEnergy());
        String beams = NumberUtils.getFormatedBigNumber(dyson.getBeams()) + "/"
                + NumberUtils.getFormatedBigNumber(dyson.getRequiredBeams());
        String progress = new DecimalFormat().format(dyson.getProgress() * 100.0);
        String calculationCompleted = NumberUtils.getScientificInteger(
                dyson.getCalculationCompletedExact());
        String calculationCapacity = NumberUtils.getScientificInteger(
                dyson.getCalculationCapacityExact());
        Component calculation = Component.translatable(
                "gui.dysoncubeproject.calculation.state." + dyson.getCalculationState(),
                calculationCompleted, calculationCapacity);

        guiGraphics.fill(x - 3, top - 3, x + WIDTH + 3, top + HEIGHT + 3, 0xD0182028);
        AssetUtil.drawHorizontalLine(guiGraphics, x - 4, x + WIDTH + 4, top - 4, DCPContent.CYAN_COLOR);
        AssetUtil.drawHorizontalLine(guiGraphics, x - 4, x + WIDTH + 4, top + HEIGHT + 4, DCPContent.CYAN_COLOR);
        AssetUtil.drawVerticalLine(guiGraphics, x - 4, top - 4, top + HEIGHT + 4, DCPContent.CYAN_COLOR);
        AssetUtil.drawVerticalLine(guiGraphics, x + WIDTH + 4, top - 4, top + HEIGHT + 4, DCPContent.CYAN_COLOR);

        drawFitted(guiGraphics, font, Component.translatable("gui.dysoncubeproject.dyson_information"),
                x, top, 0x55FFFF);
        drawFitted(guiGraphics, font, Component.translatable("gui.dysoncubeproject.progress", progress),
                x, top + LINE_HEIGHT, 0x7FA9FF);
        drawFitted(guiGraphics, font, Component.translatable("gui.dysoncubeproject.power_gen", power),
                x, top + LINE_HEIGHT * 2, 0x7FA9FF);
        drawFitted(guiGraphics, font, Component.translatable("gui.dysoncubeproject.beams", beams),
                x, top + LINE_HEIGHT * 3, 0xA8C7FF);
        drawFitted(guiGraphics, font, Component.translatable("gui.dysoncubeproject.sails",
                        NumberUtils.getFormatedBigNumber(dyson.getSolarPanels()),
                        NumberUtils.getFormatedBigNumber(dyson.getRequiredSolarPanels())),
                x, top + LINE_HEIGHT * 4, 0xA8C7FF);
        drawFitted(guiGraphics, font, Component.translatable("gui.dysoncubeproject.progress_compact",
                        hierarchy.ordinal() + 1, batch),
                x, top + LINE_HEIGHT * 5, 0x66FF99);
        drawFitted(guiGraphics, font, calculation,
                x, top + LINE_HEIGHT * 6, 0xFFD166);

        List<Component> details = new ArrayList<>();
        details.add(Component.translatable("gui.dysoncubeproject.dyson_information").withStyle(ChatFormatting.AQUA));
        details.add(Component.translatable("gui.dysoncubeproject.progress", progress));
        details.add(Component.translatable("gui.dysoncubeproject.power_gen", power));
        details.add(Component.translatable("gui.dysoncubeproject.power_con",
                NumberUtils.getFormatedBigNumber(dyson.getLastConsumedEnergy())));
        details.add(Component.translatable("gui.dysoncubeproject.beams", beams));
        details.add(Component.translatable("gui.dysoncubeproject.sails",
                NumberUtils.getFormatedBigNumber(dyson.getSolarPanels()),
                NumberUtils.getFormatedBigNumber(dyson.getRequiredSolarPanels())));
        details.add(Component.translatable("gui.dysoncubeproject.progress_hierarchy",
                hierarchy.ordinal() + 1, Component.translatable("gui.dysoncubeproject.hierarchy."
                        + hierarchy.name().toLowerCase(java.util.Locale.ROOT))));
        details.add(Component.translatable("gui.dysoncubeproject.progress_batch", batch)
                .withStyle(ChatFormatting.GREEN));
        details.add(Component.translatable(UniverseHierarchy.isBatchSizeCapped(
                        wrapped, hierarchyEffectLayers)
                ? "gui.dysoncubeproject.progress_batch_capped"
                : "gui.dysoncubeproject.progress_batch_next", next).withStyle(ChatFormatting.GRAY));
        details.add(calculation.copy().withStyle(ChatFormatting.YELLOW));
        details.add(Component.translatable("gui.dysoncubeproject.progress_aggregate")
                .withStyle(ChatFormatting.DARK_GRAY));
        if (dyson.getSolarPanels().compareTo(dyson.getSupportedSolarPanels()) >= 0
                && dyson.getBeams().compareTo(dyson.getRequiredBeams()) < 0) {
            details.add(Component.translatable("gui.dysoncubeproject.needs_more_beams")
                    .withStyle(ChatFormatting.RED));
        }
        this.tooltipLines = List.copyOf(details);
    }

    private static void drawFitted(GuiGraphics graphics, Font font, Component text,
                                   int x, int y, int color) {
        if (font.width(text) <= WIDTH) {
            graphics.drawString(font, text, x, y, color, false);
            return;
        }
        String suffix = "...";
        String clipped = font.plainSubstrByWidth(text.getString(), WIDTH - font.width(suffix));
        graphics.drawString(font, Component.literal(clipped + suffix), x, y, color, false);
    }

    @Override
    public List<Component> getTooltipLines() {
        return this.tooltipLines;
    }

    public void drawForegroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
    }
}

