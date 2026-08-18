package com.gugugaga233.dysoncubeprojectaddon.overhaul.client.gui;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.StarData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/** Read-only Cosmic Heart structure progress. Materials arrive through rail ejectors. */
public class CosmicHeartFeedTab extends Screen {

    private static final int GUI_WIDTH = 280;
    private static final int GUI_HEIGHT = 190;
    private StarData starData = StarData.SUN;
    private double progress;
    private boolean active;
    private boolean complete;
    private String beamProgress = "0 / 0";
    private String sailProgress = "0 / 0";
    @Nullable
    private ResourceLocation starTexture;

    protected CosmicHeartFeedTab() {
        super(Component.translatable("gui.dysoncubeproject.cosmic_heart.feed.title"));
    }

    public void updateData(StarData star, double progress, boolean active, boolean complete,
                           String beamProgress, String sailProgress,
                           @Nullable ResourceLocation texture) {
        this.starData = star == null ? StarData.SUN : star;
        this.progress = Math.max(0.0D, Math.min(100.0D, progress));
        this.active = active;
        this.complete = complete;
        this.beamProgress = beamProgress == null ? "0 / 0" : beamProgress;
        this.sailProgress = sailProgress == null ? "0 / 0" : sailProgress;
        this.starTexture = texture;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int left = (this.width - GUI_WIDTH) / 2;
        int top = (this.height - GUI_HEIGHT) / 2;

        graphics.fill(left, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xE612151A);
        graphics.renderOutline(left, top, GUI_WIDTH, GUI_HEIGHT, 0xFFFFD45E);
        drawCentered(graphics, Component.translatable(
                "gui.dysoncubeproject.cosmic_heart.feed.title"), top + 10, 0xFFFFD45E);

        graphics.drawString(this.font, Component.translatable(
                "gui.dysoncubeproject.current_star", this.starData.localizedDisplayName()),
                left + 14, top + 34, 0xFFF0F3F5, false);
        graphics.drawString(this.font, Component.translatable(
                "gui.dysoncubeproject.cosmic_heart_progress", String.format("%.6f", this.progress)),
                left + 14, top + 50, 0xFFFFB347, false);

        int barLeft = left + 14;
        int barTop = top + 68;
        int barWidth = GUI_WIDTH - 28;
        int filled = (int) Math.round(barWidth * this.progress / 100.0D);
        graphics.fill(barLeft, barTop, barLeft + barWidth, barTop + 10, 0xFF30363D);
        graphics.fill(barLeft, barTop, barLeft + filled, barTop + 10, 0xFFFFB347);
        graphics.renderOutline(barLeft, barTop, barWidth, 10, 0xFF7D8790);

        Component state = Component.translatable(complete
                ? "gui.dysoncubeproject.status.complete"
                : active ? "gui.dysoncubeproject.status.active"
                : "gui.dysoncubeproject.status.inactive");
        graphics.drawString(this.font, Component.translatable(
                "gui.dysoncubeproject.cosmic_heart_status", state),
                left + 14, top + 88, active ? 0xFFFFD45E : 0xFF889096, false);
        graphics.drawString(this.font, Component.translatable(
                "gui.dysoncubeproject.cosmic_heart.beams", this.beamProgress),
                left + 14, top + 104, 0xFF8FDC9A, false);
        graphics.drawString(this.font, Component.translatable(
                "gui.dysoncubeproject.cosmic_heart.sails", this.sailProgress),
                left + 14, top + 120, 0xFF66D9EF, false);
        graphics.drawWordWrap(this.font, Component.translatable(
                "gui.dysoncubeproject.cosmic_heart.launcher_hint"),
                left + 14, top + 140, GUI_WIDTH - 28, 0xFFC5CDD3);

        if (this.starTexture != null) {
            graphics.blit(this.starTexture, left + GUI_WIDTH - 70, top + 22,
                    0, 0, 48, 48, 128, 128);
        }
    }

    private void drawCentered(GuiGraphics graphics, Component text, int y, int color) {
        graphics.drawString(this.font, text, (this.width - this.font.width(text)) / 2, y, color, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

