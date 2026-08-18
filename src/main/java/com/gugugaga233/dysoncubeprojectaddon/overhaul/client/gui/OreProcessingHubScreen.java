package com.gugugaga233.dysoncubeprojectaddon.overhaul.client.gui;

import com.gugugaga233.dysoncubeprojectaddon.DysonCubeProject;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientboundOpenOreProcessingHubPacket;
import com.gugugaga233.dysoncubeprojectaddon.network.ServerboundOreProcessingHubPacket;
import com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class OreProcessingHubScreen extends Screen {
    private static final int GUI_WIDTH = 470;
    private static final int GUI_HEIGHT = 270;
    private static final int GRID_COLUMNS = 8;
    private static final int GRID_ROWS = 4;
    private static final int CELL_SIZE = 24;
    private static final int GRID_X = 18;
    private static final int GRID_Y = 150;

    private final long blockPos;
    private final List<ProductEntry> products = new ArrayList<>();
    private int refreshTicks;
    private int scrollRow;
    private String status = "no_input";
    private boolean enabled = true;
    private long costPerOre = 12_000L;
    private int connectedInputs;
    private String pendingOre = "0";
    private String lastProcessed = "0";
    private String totalProcessed = "0";
    private String storedProducts = "0";
    private String lastEnergyCost = "0";

    public OreProcessingHubScreen(long blockPos) {
        super(Component.translatable("gui.dysoncubeproject.ore_processing.title"));
        this.blockPos = blockPos;
    }

    public long getBlockPos() {
        return blockPos;
    }

    public void updateData(ClientboundOpenOreProcessingHubPacket packet) {
        status = packet.status;
        enabled = packet.enabled;
        costPerOre = packet.costPerOre;
        connectedInputs = packet.connectedInputs;
        pendingOre = packet.pendingOre;
        lastProcessed = packet.lastProcessed;
        totalProcessed = packet.totalProcessed;
        storedProducts = packet.storedProducts;
        lastEnergyCost = packet.lastEnergyCost;
        products.clear();
        if (!packet.inventory.isEmpty()) {
            for (String encoded : packet.inventory.split(";")) {
                String[] parts = encoded.split(",", 3);
                if (parts.length != 3) continue;
                ResourceLocation id = ResourceLocation.tryParse(parts[0]);
                if (id == null) continue;
                Item item = BuiltInRegistries.ITEM.get(id);
                if (item != null) products.add(new ProductEntry(new ItemStack(item), parts[1], parts[2]));
            }
        }
        clampScroll();
    }

    @Override
    public void tick() {
        super.tick();
        if (++refreshTicks >= 20) {
            refreshTicks = 0;
            send("refresh", -1);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(left, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xF012161A);
        graphics.renderOutline(left, top, GUI_WIDTH, GUI_HEIGHT, 0xFF356A6A);

        Component title = Component.translatable("gui.dysoncubeproject.ore_processing.title");
        graphics.drawString(font, title, left + (GUI_WIDTH - font.width(title)) / 2,
                top + 9, 0xFF8BC4C0, false);
        drawPipeline(graphics, left, top);
        drawStatus(graphics, left, top);
        drawProducts(graphics, left, top, mouseX, mouseY);
    }

    private void drawPipeline(GuiGraphics graphics, int left, int top) {
        long firstCost = costPerOre / 3;
        long secondCost = costPerOre / 3;
        long thirdCost = costPerOre - firstCost - secondCost;
        int x = left + 18;
        drawStage(graphics, x, top + 34, "grinding", firstCost, 0xFF5D8FA8);
        drawArrow(graphics, x + 130, top + 52);
        drawStage(graphics, x + 146, top + 34, "washing", secondCost, 0xFF5A9A96);
        drawArrow(graphics, x + 276, top + 52);
        drawStage(graphics, x + 292, top + 34, "smelting", thirdCost, 0xFFB88A56);
        graphics.drawString(font,
                Component.translatable("gui.dysoncubeproject.ore_processing.single_settlement"),
                left + 18, top + 88, 0xFFAAB5BC, false);
    }

    private void drawStage(GuiGraphics graphics, int x, int y, String stage, long cost, int color) {
        graphics.fill(x, y, x + 122, y + 45, 0xFF1B2328);
        graphics.renderOutline(x, y, 122, 45, color);
        graphics.drawString(font,
                Component.translatable("gui.dysoncubeproject.ore_processing.stage." + stage),
                x + 7, y + 7, color, false);
        graphics.drawString(font,
                Component.translatable("gui.dysoncubeproject.ore_processing.stage_cost", compactCost(cost)),
                x + 7, y + 24, 0xFFD5DCE0, false);
    }

    private void drawArrow(GuiGraphics graphics, int x, int y) {
        graphics.drawString(font, Component.literal(">"), x, y, 0xFF8A979F, false);
    }

    private void drawStatus(GuiGraphics graphics, int left, int top) {
        Component state = Component.translatable("gui.dysoncubeproject.ore_processing.status." + status);
        graphics.drawString(font,
                Component.translatable("gui.dysoncubeproject.ore_processing.status", state),
                left + 18, top + 108, statusColor(), false);
        drawFitted(graphics,
                Component.translatable("gui.dysoncubeproject.ore_processing.inputs", connectedInputs, pendingOre),
                left + 18, top + 122, 205, 0xFFB7C2C7);
        drawFitted(graphics,
                Component.translatable("gui.dysoncubeproject.ore_processing.config",
                        Component.translatable(enabled ? "options.on" : "options.off"),
                        compactCost(costPerOre)),
                left + 238, top + 108, 214, enabled ? 0xFF86B895 : 0xFFB87979);
        drawFitted(graphics,
                Component.translatable("gui.dysoncubeproject.ore_processing.last_batch",
                        lastProcessed, lastEnergyCost),
                left + 238, top + 122, 214, 0xFFC09B62);
    }

    private void drawProducts(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        drawFitted(graphics,
                Component.translatable("gui.dysoncubeproject.ore_processing.products",
                        storedProducts, totalProcessed),
                left + GRID_X, top + 138, GUI_WIDTH - GRID_X * 2, 0xFF8BC4C0);
        int start = scrollRow * GRID_COLUMNS;
        ProductEntry hovered = null;
        for (int visible = 0; visible < GRID_COLUMNS * GRID_ROWS; visible++) {
            int x = left + GRID_X + visible % GRID_COLUMNS * CELL_SIZE;
            int y = top + GRID_Y + visible / GRID_COLUMNS * CELL_SIZE;
            graphics.fill(x, y, x + 22, y + 22, 0xFF20292F);
            graphics.renderOutline(x, y, 22, 22, 0xFF53656E);
            int index = start + visible;
            if (index >= products.size()) continue;
            ProductEntry entry = products.get(index);
            graphics.renderItem(entry.stack(), x + 3, y + 3);
            drawAmount(graphics, entry.compact(), x, y);
            if (inside(mouseX, mouseY, x, y, 22, 22)) hovered = entry;
        }
        int maxRows = Math.max(1, (products.size() + GRID_COLUMNS - 1) / GRID_COLUMNS);
        graphics.drawString(font,
                Component.translatable("gui.dysoncubeproject.ore_processing.page",
                        Math.min(scrollRow + 1, maxRows), maxRows),
                left + 218, top + 151, 0xFF89969D, false);
        graphics.drawWordWrap(font,
                Component.translatable("gui.dysoncubeproject.ore_processing.output_help"),
                left + 218, top + 171, 230, 0xFFAAB5BC);
        if (hovered != null) renderProductTooltip(graphics, hovered, mouseX, mouseY);
    }

    private void drawAmount(GuiGraphics graphics, String amount, int x, int y) {
        String display = NumberUtils.getAeCompactAmount(amount == null ? "0" : amount);
        graphics.pose().pushPose();
        graphics.pose().translate(x + 21, y + 16, 200);
        float scale = Math.min(0.5F, 19.0F / Math.max(1, font.width(display)));
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, display, -font.width(display), 0, 0xFFD9E1E3, true);
        graphics.pose().popPose();
    }

    private void drawFitted(GuiGraphics graphics, Component text, int x, int y,
                            int maxWidth, int color) {
        int textWidth = font.width(text);
        if (textWidth <= maxWidth) {
            graphics.drawString(font, text, x, y, color, false);
            return;
        }
        float scale = maxWidth / (float) textWidth;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y + (font.lineHeight - font.lineHeight * scale) / 2.0F, 0);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private void renderProductTooltip(GuiGraphics graphics, ProductEntry entry, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>(Screen.getTooltipFromItem(minecraft, entry.stack()));
        String compact = entry.compact();
        tooltip.add(Component.translatable("gui.dysoncubeproject.ore_processing.exact", compact)
                .withStyle(ChatFormatting.AQUA));
        String explanation = NumberUtils.getScientificNotationExplanation(compact);
        if (explanation != null) {
            tooltip.add(Component.translatable("gui.dysoncubeproject.ore_processing.scientific_explanation",
                            explanation)
                    .withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("gui.dysoncubeproject.ore_processing.exact_full")
                .withStyle(ChatFormatting.GRAY));
        int displayedLength = Math.min(entry.exact().length(), 48 * 6);
        for (int offset = 0; offset < displayedLength; offset += 48) {
            tooltip.add(Component.literal(entry.exact().substring(
                            offset, Math.min(offset + 48, displayedLength)))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (displayedLength < entry.exact().length()) {
            tooltip.add(Component.literal("...").withStyle(ChatFormatting.DARK_GRAY));
        }
        graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;
        int start = scrollRow * GRID_COLUMNS;
        for (int visible = 0; visible < GRID_COLUMNS * GRID_ROWS; visible++) {
            int x = left + GRID_X + visible % GRID_COLUMNS * CELL_SIZE;
            int y = top + GRID_Y + visible / GRID_COLUMNS * CELL_SIZE;
            int index = start + visible;
            if (index < products.size() && inside((int) mouseX, (int) mouseY, x, y, 22, 22)) {
                send("extract", index);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (deltaY != 0) {
            scrollRow += deltaY > 0 ? -1 : 1;
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    private void clampScroll() {
        int rows = (products.size() + GRID_COLUMNS - 1) / GRID_COLUMNS;
        scrollRow = Math.max(0, Math.min(scrollRow, Math.max(0, rows - GRID_ROWS)));
    }

    private int statusColor() {
        return switch (status) {
            case "processed" -> 0xFF7DE09A;
            case "no_energy", "disabled" -> 0xFFE06F6F;
            default -> 0xFFFFC766;
        };
    }

    private static String compactCost(long cost) {
        return NumberUtils.getAeCompactAmount(Long.toString(cost));
    }

    private void send(String action, int slot) {
        DysonCubeProject.NETWORK.sendToServer(
                new ServerboundOreProcessingHubPacket(blockPos, action, slot));
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record ProductEntry(ItemStack stack, String compact, String exact) {
    }
}

