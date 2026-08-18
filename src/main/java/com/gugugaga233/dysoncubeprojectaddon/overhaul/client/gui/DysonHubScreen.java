package com.gugugaga233.dysoncubeprojectaddon.overhaul.client.gui;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.StarData;
import com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class DysonHubScreen extends Screen {

    private static final int GUI_WIDTH = 320;
    private static final int GUI_HEIGHT = 244;
    private static final int LINE_HEIGHT = 12;
    private static final int TEXT_WIDTH = 170;

    private StarData starData = StarData.SUN;
    private double totalPower = 0;
    private String totalPowerDisplay = "0";
    private String storedEnergyDisplay = "0";
    private String totalPowerCalculation = "0";
    private String storedEnergyCalculation = "0";
    private String totalWrapped = "0";
    private String totalWrappedDisplay = "0";
    private String hierarchyLevel = "SINGLE_STAR";
    private String hierarchyBatchDisplay = "100";
    private String hierarchyNextThresholdDisplay = "5000";
    private boolean hierarchyBatchCapped;
    private boolean universeAwakened = false;
    private String darkMatterResonance = "0";
    private String darkMatterResonanceCalculation = "0";
    private boolean cosmicHeartActive = false;
    private double cosmicHeartProgress = 0;
    private boolean cosmicHeartComplete = false;
    private String cosmicHeartBatchDisplay = "0";
    private String queuedCosmicHeartDisplay = "0";
    private String cosmicHeartBeamDisplay = "0 / 0";
    private String cosmicHeartSailDisplay = "0 / 0";
    @Nullable
    private Button feedTabButton;
    @Nullable
    private ResourceLocation starTexture;

    public DysonHubScreen() {
        super(Component.translatable("gui.dysoncubeproject.dyson_hub.title"));
    }

    public void updateData(StarData star, double power, long wrapped, boolean awakened,
                           String powerDisplay, ResourceLocation texture) {
        updateData(star, power, Long.toString(wrapped), "SINGLE_STAR", "100", "5000", false,
                awakened, "0", false, 0, false, "0", "0",
                "0 / 0", "0 / 0", powerDisplay, "0", "0", "0", texture);
    }

    public void updateData(StarData star, double power, String wrapped, String hierarchyLevel,
                           String hierarchyBatchDisplay, String hierarchyNextThresholdDisplay,
                           boolean hierarchyBatchCapped,
                           boolean awakened,
                           String resonance, boolean heartActive, double heartProgress,
                           boolean heartComplete, String heartBatchDisplay,
                           String queuedHeartDisplay, String heartBeamDisplay,
                           String heartSailDisplay, String powerDisplay,
                           String storedEnergyDisplay,
                           String powerCalculation,
                           String storedEnergyCalculation,
                           ResourceLocation texture) {
        this.starData = star != null ? star : StarData.SUN;
        this.totalPower = power;
        this.totalPowerDisplay = powerDisplay != null ? powerDisplay : "0";
        this.storedEnergyDisplay = storedEnergyDisplay != null ? storedEnergyDisplay : "0";
        this.totalPowerCalculation = powerCalculation != null ? powerCalculation : "0";
        this.storedEnergyCalculation = storedEnergyCalculation != null ? storedEnergyCalculation : "0";
        this.totalWrapped = wrapped != null ? wrapped : "0";
        this.totalWrappedDisplay = NumberUtils.getCompactAbsoluteCalculation(this.totalWrapped);
        this.hierarchyLevel = hierarchyLevel != null ? hierarchyLevel : "SINGLE_STAR";
        this.hierarchyBatchDisplay = hierarchyBatchDisplay != null ? hierarchyBatchDisplay : "1";
        this.hierarchyNextThresholdDisplay = hierarchyNextThresholdDisplay != null
                ? hierarchyNextThresholdDisplay : "0";
        this.hierarchyBatchCapped = hierarchyBatchCapped;
        this.universeAwakened = awakened;
        this.darkMatterResonanceCalculation = resonance != null ? resonance : "0";
        this.darkMatterResonance = NumberUtils.getCompactAbsoluteCalculation(
                this.darkMatterResonanceCalculation);
        this.cosmicHeartActive = heartActive;
        this.cosmicHeartProgress = heartProgress;
        this.cosmicHeartComplete = heartComplete;
        this.cosmicHeartBatchDisplay = heartBatchDisplay != null ? heartBatchDisplay : "0";
        this.queuedCosmicHeartDisplay = queuedHeartDisplay != null ? queuedHeartDisplay : "0";
        this.cosmicHeartBeamDisplay = heartBeamDisplay != null ? heartBeamDisplay : "0 / 0";
        this.cosmicHeartSailDisplay = heartSailDisplay != null ? heartSailDisplay : "0 / 0";
        this.starTexture = texture;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int left = (this.width - GUI_WIDTH) / 2;
        int top = (this.height - GUI_HEIGHT) / 2;
        int textX = left + 12;

        graphics.fill(left, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xF0121518);
        graphics.renderOutline(left, top, GUI_WIDTH, GUI_HEIGHT, 0xFF3D667A);

        Component title = Component.translatable("gui.dysoncubeproject.dyson_hub.title");
        graphics.drawString(this.font, title,
                left + (GUI_WIDTH - this.font.width(title)) / 2, top + 8,
                0xFF87AEBF, false);

        drawLine(graphics, textX, top + 30,
                Component.translatable("gui.dysoncubeproject.current_star",
                        starData.localizedDisplayName()),
                0xFFF0F3F5);
        drawLine(graphics, textX, top + 42,
                Component.translatable("gui.dysoncubeproject.star_type", starData.type()),
                0xFFC5CDD3);
        drawLine(graphics, textX, top + 54,
                Component.translatable("gui.dysoncubeproject.star_mass",
                        String.format("%.2f", starData.massInMSun())),
                0xFFC5CDD3);
        drawLine(graphics, textX, top + 66,
                Component.translatable("gui.dysoncubeproject.star_radius",
                        String.format("%.2f", starData.radiusInRSun())),
                0xFFC5CDD3);
        drawLine(graphics, textX, top + 78,
                Component.translatable("gui.dysoncubeproject.star_temp",
                        String.format("%.0f", starData.temperatureK())),
                0xFFC5CDD3);

        drawLine(graphics, textX, top + 102,
                Component.translatable("gui.dysoncubeproject.total_power", totalPowerDisplay),
                0xFFC49A63);
        drawLine(graphics, textX, top + 114,
                Component.translatable("gui.dysoncubeproject.energy_buffer", storedEnergyDisplay),
                0xFF79A9B2);
        drawLine(graphics, textX, top + 126,
                Component.translatable("gui.dysoncubeproject.universe_power",
                        powerOfTen(totalWrappedDisplay)),
                0xFF8FDC9A);
        drawLine(graphics, textX, top + 138,
                Component.translatable("gui.dysoncubeproject.dark_matter_resonance",
                        "2^(" + darkMatterResonance + ")"),
                0xFF9EA7FF);
        drawLine(graphics, textX, top + 150,
                Component.translatable("gui.dysoncubeproject.wrapped_count", totalWrappedDisplay),
                0xFFAEB8FF);
        drawLine(graphics, textX, top + 162,
                Component.translatable("gui.dysoncubeproject.hierarchy_level",
                        hierarchyLevelNumber(),
                        Component.translatable("gui.dysoncubeproject.hierarchy." + hierarchyLevel.toLowerCase())),
                0xFF62BCEB);
        drawLine(graphics, textX, top + 174,
                Component.translatable(hierarchyBatchCapped
                                ? "gui.dysoncubeproject.hierarchy_batch_capped"
                                : "gui.dysoncubeproject.hierarchy_batch_next",
                        hierarchyBatchDisplay, hierarchyNextThresholdDisplay),
                0xFF8FDC9A);
        drawLine(graphics, textX, top + 186,
                Component.translatable("gui.dysoncubeproject.universe_awakened_status",
                        status(universeAwakened)),
                universeAwakened ? 0xFFE984FF : 0xFF889096);
        drawLine(graphics, textX, top + 198,
                Component.translatable("gui.dysoncubeproject.cosmic_heart_status_count",
                        heartStatus(), cosmicHeartBatchDisplay, queuedCosmicHeartDisplay),
                cosmicHeartComplete ? 0xFF7F9C88
                        : cosmicHeartActive ? 0xFFC2A55F : 0xFF7E868B);
        drawLine(graphics, textX, top + 210,
                Component.translatable("gui.dysoncubeproject.cosmic_heart_progress",
                        String.format("%.6f", cosmicHeartProgress)),
                0xFFC49A63);

        if (starTexture != null) {
            graphics.setColor(0.72F, 0.76F, 0.78F, 1.0F);
            graphics.blit(starTexture, left + 180, top + 32,
                    0, 0, 128, 128, 128, 128);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        drawLine(graphics, left + 180, top + 174,
                Component.translatable("gui.dysoncubeproject.hierarchy_aggregate_short"),
                0xFF9AA7AD, 128);
        drawLine(graphics, left + 180, top + 186,
                Component.translatable("gui.dysoncubeproject.hierarchy_landing_short"),
                0xFF7F8B91, 128);

        renderTooltips(graphics, mouseX, mouseY, left, top);
    }

    private void drawLine(GuiGraphics graphics, int x, int y, Component text, int color) {
        drawLine(graphics, x, y, text, color, TEXT_WIDTH);
    }

    private void drawLine(GuiGraphics graphics, int x, int y, Component text, int color,
                          int maxWidth) {
        int textWidth = this.font.width(text);
        if (textWidth <= maxWidth) {
            graphics.drawString(this.font, text, x, y, color, false);
            return;
        }

        float scale = maxWidth / (float) textWidth;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y + (LINE_HEIGHT - this.font.lineHeight * scale) / 2.0F, 0);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(this.font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private void renderTooltips(GuiGraphics graphics, int mouseX, int mouseY,
                                int left, int top) {
        int x = left + 10;
        int width = TEXT_WIDTH + 4;
        renderTooltip(graphics, mouseX, mouseY, x, top + 28, width,
                "gui.dysoncubeproject.tooltip.current_star");
        renderTooltip(graphics, mouseX, mouseY, x, top + 40, width,
                "gui.dysoncubeproject.tooltip.star_type");
        renderTooltip(graphics, mouseX, mouseY, x, top + 52, width,
                "gui.dysoncubeproject.tooltip.star_mass");
        renderTooltip(graphics, mouseX, mouseY, x, top + 64, width,
                "gui.dysoncubeproject.tooltip.star_radius");
        renderTooltip(graphics, mouseX, mouseY, x, top + 76, width,
                "gui.dysoncubeproject.tooltip.star_temp");
        renderFormattedNumberTooltip(graphics, mouseX, mouseY, x, top + 100, width,
                "gui.dysoncubeproject.tooltip.total_power", totalPowerDisplay, totalPowerCalculation);
        renderFormattedNumberTooltip(graphics, mouseX, mouseY, x, top + 112, width,
                "gui.dysoncubeproject.tooltip.energy_buffer", storedEnergyDisplay, storedEnergyCalculation);
        renderLargeNumberTooltip(graphics, mouseX, mouseY, x, top + 124, width,
                "gui.dysoncubeproject.tooltip.universe_power",
                powerOfTen(totalWrappedDisplay), totalWrapped);
        renderLargeNumberTooltip(graphics, mouseX, mouseY, x, top + 136, width,
                "gui.dysoncubeproject.tooltip.dark_matter_resonance",
                "2^(" + darkMatterResonance + ")", darkMatterResonanceCalculation);
        renderLargeNumberTooltip(graphics, mouseX, mouseY, x, top + 148, width,
                "gui.dysoncubeproject.tooltip.wrapped_count", totalWrappedDisplay, totalWrapped);
        renderHierarchyTooltip(graphics, mouseX, mouseY, x, top + 160, width);
        renderHierarchyTooltip(graphics, mouseX, mouseY, x, top + 172, width);
        renderTooltip(graphics, mouseX, mouseY, x, top + 184, width,
                "gui.dysoncubeproject.tooltip.universe_awakened");
        renderTooltip(graphics, mouseX, mouseY, x, top + 196, width,
                "gui.dysoncubeproject.tooltip.cosmic_heart");
        renderTooltip(graphics, mouseX, mouseY, x, top + 208, width,
                "gui.dysoncubeproject.tooltip.cosmic_heart_progress");
    }

    private void renderHierarchyTooltip(GuiGraphics graphics, int mouseX, int mouseY,
                                        int x, int y, int width) {
        if (mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + LINE_HEIGHT) return;
        graphics.renderComponentTooltip(this.font, List.of(
                Component.translatable("gui.dysoncubeproject.tooltip.hierarchy_level"),
                Component.translatable("gui.dysoncubeproject.tooltip.hierarchy_batch",
                        hierarchyBatchDisplay).withStyle(ChatFormatting.AQUA),
                (hierarchyBatchCapped
                        ? Component.translatable("gui.dysoncubeproject.tooltip.hierarchy_capped")
                        : Component.translatable("gui.dysoncubeproject.tooltip.hierarchy_next",
                        hierarchyNextThresholdDisplay)).withStyle(ChatFormatting.GREEN),
                Component.translatable("gui.dysoncubeproject.tooltip.hierarchy_aggregate")
                        .withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
    }

    private void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY,
                               int x, int y, int width, String translationKey) {
        renderTooltip(graphics, mouseX, mouseY, x, y, width, translationKey, null);
    }

    private void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY,
                               int x, int y, int width, String translationKey,
                               @Nullable String value) {
        if (mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + LINE_HEIGHT) {
            List<Component> lines = value == null
                    ? List.of(Component.translatable(translationKey))
                    : List.of(Component.translatable(translationKey),
                    Component.literal(value).withStyle(ChatFormatting.AQUA));
            graphics.renderComponentTooltip(this.font,
                    lines, mouseX, mouseY);
        }
    }

    private void renderLargeNumberTooltip(GuiGraphics graphics, int mouseX, int mouseY,
                                          int x, int y, int width, String translationKey,
                                          String display, String calculation) {
        if (mouseX < x || mouseX >= x + width
                || mouseY < y || mouseY >= y + LINE_HEIGHT) {
            return;
        }

        renderFormattedNumberTooltip(graphics, mouseX, mouseY, x, y, width, translationKey,
                display, NumberUtils.getCompactAbsoluteStorageCalculation(calculation));
    }

    private void renderFormattedNumberTooltip(GuiGraphics graphics, int mouseX, int mouseY,
                                              int x, int y, int width, String translationKey,
                                              String display, String storageCalculation) {
        if (mouseX < x || mouseX >= x + width
                || mouseY < y || mouseY >= y + LINE_HEIGHT) {
            return;
        }

        java.util.ArrayList<Component> lines = new java.util.ArrayList<>();
        lines.add(Component.translatable(translationKey));
        lines.add(Component.translatable("gui.dysoncubeproject.tooltip.compact_value", display)
                .withStyle(ChatFormatting.AQUA));
        String explanation = NumberUtils.getScientificNotationExplanation(display);
        if (explanation != null) {
            lines.add(Component.translatable("gui.dysoncubeproject.tooltip.notation_meaning", explanation)
                    .withStyle(ChatFormatting.GRAY));
        }
        lines.add(Component.translatable("gui.dysoncubeproject.tooltip.flux_storage_value",
                        storageCalculation)
                .withStyle(ChatFormatting.DARK_GRAY));
        graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
    }

    private static String powerOfTen(String exponent) {
        return exponent.matches("[0-9]{1,9}") ? "1E" + exponent : "1E(" + exponent + ")";
    }

    private Component status(boolean active) {
        return Component.translatable(active
                ? "gui.dysoncubeproject.status.active"
                : "gui.dysoncubeproject.status.inactive");
    }

    private Component heartStatus() {
        if (cosmicHeartComplete) {
            return Component.translatable("gui.dysoncubeproject.status.complete");
        }
        return status(cosmicHeartActive);
    }

    private int hierarchyLevelNumber() {
        return switch (hierarchyLevel) {
            case "STAR_CLUSTER" -> 2;
            case "GALAXY" -> 3;
            case "STAR_RIVER" -> 4;
            case "STAR_HUB" -> 5;
            case "STAR_DOME" -> 6;
            case "STAR_DOMAIN" -> 7;
            case "STAR_UNIVERSE" -> 8;
            default -> 1;
        };
    }

    @Override
    protected void init() {
        if (cosmicHeartActive && !cosmicHeartComplete) {
            int left = (this.width - GUI_WIDTH) / 2;
            int top = (this.height - GUI_HEIGHT) / 2;
            this.feedTabButton = Button.builder(
                    Component.translatable("gui.dysoncubeproject.cosmic_heart.feed_btn"),
                    button -> {
                        CosmicHeartFeedTab screen = new CosmicHeartFeedTab();
                        screen.updateData(this.starData, this.cosmicHeartProgress,
                                this.cosmicHeartActive, this.cosmicHeartComplete,
                        this.cosmicHeartBeamDisplay, this.cosmicHeartSailDisplay,
                                this.starTexture);
                        this.minecraft.setScreen(screen);
                    }
            ).bounds(left + 180, top + 210, 128, 20).build();
            this.addRenderableWidget(this.feedTabButton);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || keyCode == 257) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}

