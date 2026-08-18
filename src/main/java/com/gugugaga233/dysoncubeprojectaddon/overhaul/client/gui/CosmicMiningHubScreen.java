package com.gugugaga233.dysoncubeprojectaddon.overhaul.client.gui;

import com.gugugaga233.dysoncubeprojectaddon.DysonCubeProject;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientboundOpenCosmicMiningHubPacket;
import com.gugugaga233.dysoncubeprojectaddon.network.ServerboundCosmicMiningHubPacket;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.PlanetType;
import com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public class CosmicMiningHubScreen extends Screen {
    private static final int GUI_WIDTH = 550;
    private static final int GUI_HEIGHT = 306;
    private static final int SLOT_SIZE = 20;
    private static final int SLOT_START_X = 238;
    private static final int SLOT_START_Y = 190;
    private static final int PLANET_X = 238;
    private static final int PLANET_Y = 28;
    private static final int PLANET_SIZE = 128;
    private static final int RESOURCE_ICON_CELL = 18;
    private static final int RESOURCE_DEFAULT_LIMIT = 10;
    private static final BigDecimal EARTH_MASS_KG = new BigDecimal("5.9722E24");

    private final long blockPos;
    private final ItemStack[] inventory = new ItemStack[9];
    private final String[] itemAmounts = new String[9];
    private EditBox laserInputBox;
    private EditBox customMassBox;
    private Button toggleButton;
    private Button nextButton;
    private Button tierDownButton;
    private Button tierUpButton;
    private Button customMassButton;
    private int refreshTicks;
    private String planetType = "silicate";
    private String initialMass = "0";
    private String remainingMass = "0";
    private String remainingPercent = "0";
    private String laserInput = "1";
    private String zeroCount = "0";
    private String efficiencyPercent = "100.0";
    private String effectivePower = "1";
    private String storedEnergy = "0";
    private String externalEnergy = "0";
    private int connectedPorts;
    private int starTier = 1;
    private String starTierId = "single_planet";
    private String tierMinimumMass = "0.1";
    private String tierMaximumMass = "1";
    private String tierEnergyMultiplier = "1";
    private String energyCost = "1000";
    private boolean canChangeTier = true;
    private boolean running;
    private boolean canSelectNext = true;
    private String status = "stopped";
    private String fluidId = "";
    private String fluidAmount = "0";
    private List<ItemStack> resourceItems = List.of();
    private List<Fluid> resourceFluids = List.of();

    public CosmicMiningHubScreen(long blockPos) {
        super(Component.translatable("gui.dysoncubeproject.cosmic_mining.title"));
        this.blockPos = blockPos;
        for (int slot = 0; slot < inventory.length; slot++) {
            inventory[slot] = ItemStack.EMPTY;
            itemAmounts[slot] = "0";
        }
    }

    public long getBlockPos() {
        return blockPos;
    }

    public void updateData(ClientboundOpenCosmicMiningHubPacket packet) {
        planetType = packet.planetType;
        initialMass = packet.initialMass;
        remainingMass = packet.remainingMass;
        remainingPercent = packet.remainingPercent;
        laserInput = packet.laserInput;
        zeroCount = packet.zeroCount;
        efficiencyPercent = packet.efficiencyPercent;
        effectivePower = packet.effectivePower;
        storedEnergy = nonBlankNumber(packet.storedEnergy);
        externalEnergy = nonBlankNumber(packet.externalEnergy);
        connectedPorts = packet.connectedPorts;
        starTier = packet.starTier;
        starTierId = packet.starTierId;
        tierMinimumMass = packet.tierMinimumMass;
        tierMaximumMass = packet.tierMaximumMass;
        tierEnergyMultiplier = packet.tierEnergyMultiplier;
        energyCost = packet.energyCost;
        canChangeTier = packet.canChangeTier;
        running = packet.running;
        canSelectNext = packet.canSelectNext;
        status = packet.status;
        parseFluids(packet.fluids);
        parseInventory(packet.inventory);
        resourceItems = parseResourceItems(packet.resourceItems);
        resourceFluids = parseResourceFluids(packet.resourceFluids);
        if (laserInputBox != null && !laserInputBox.isFocused()) laserInputBox.setValue(laserInput);
        if (customMassBox != null && !customMassBox.isFocused()) {
            customMassBox.setValue(earthMassFromKg(initialMass));
        }
        updateToggleLabel();
        updateNextButton();
        updateTierButtons();
        updateCustomMassControls();
    }

    @Override
    protected void init() {
        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;
        laserInputBox = new EditBox(font, left + 16, top + 147, 192, 18,
                Component.translatable("gui.dysoncubeproject.cosmic_mining.laser_input"));
        laserInputBox.setMaxLength(Integer.MAX_VALUE);
        laserInputBox.setFilter(CosmicMiningHubScreen::isPotentialPowerInput);
        laserInputBox.setValue(laserInput);
        laserInputBox.setCanLoseFocus(true);
        laserInputBox.setFocused(false);
        addRenderableWidget(laserInputBox);

        addRenderableWidget(Button.builder(
                Component.translatable("gui.dysoncubeproject.cosmic_mining.apply"),
                button -> send("apply", laserInputBox.getValue(), -1))
                .bounds(left + 16, top + 272, 94, 20).build());
        toggleButton = addRenderableWidget(Button.builder(Component.empty(),
                button -> send("toggle", "", -1))
                .bounds(left + 116, top + 272, 94, 20).build());
        nextButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.dysoncubeproject.cosmic_mining.next"),
                button -> send("next", "", -1))
                .bounds(left + 216, top + 272, 112, 20).build());
        tierDownButton = addRenderableWidget(Button.builder(Component.literal("<"),
                button -> send("tier", Integer.toString(starTier - 1), -1))
                .bounds(left + 390, top + 130, 24, 20).build());
        tierUpButton = addRenderableWidget(Button.builder(Component.literal(">"),
                button -> send("tier", Integer.toString(starTier + 1), -1))
                .bounds(left + 510, top + 130, 24, 20).build());
        customMassBox = new EditBox(font, left + 422, top + 188, 112, 18,
                Component.translatable("gui.dysoncubeproject.cosmic_mining.custom_mass"));
        customMassBox.setMaxLength(128);
        customMassBox.setFilter(CosmicMiningHubScreen::isPotentialPowerInput);
        customMassBox.setValue(earthMassFromKg(initialMass));
        customMassBox.setCanLoseFocus(true);
        customMassBox.setFocused(false);
        addRenderableWidget(customMassBox);
        customMassButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.dysoncubeproject.cosmic_mining.apply_custom_mass"),
                button -> send("mass", customMassBox.getValue(), -1))
                .bounds(left + 422, top + 210, 112, 20).build());
        updateToggleLabel();
        updateNextButton();
        updateTierButtons();
        updateCustomMassControls();
        clearFocus();
    }

    @Override
    protected void setInitialFocus() {
        clearFocus();
    }

    @Override
    public void tick() {
        super.tick();
        if (++refreshTicks >= 20) {
            refreshTicks = 0;
            send("refresh", "", -1);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;

        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(left, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xE612151A);
        graphics.renderOutline(left, top, GUI_WIDTH, GUI_HEIGHT, 0xFF4DA6D8);
        Component title = Component.translatable("gui.dysoncubeproject.cosmic_mining.title");
        graphics.drawString(font, title, left + (GUI_WIDTH - font.width(title)) / 2,
                top + 8, 0xFF62BCEB, false);

        int x = left + 16;
        drawLine(graphics, x, top + 30, Component.translatable("gui.dysoncubeproject.cosmic_mining.target",
                Component.translatable("gui.dysoncubeproject.planet." + planetType)), 0xFFF0F3F5);
        drawLine(graphics, x, top + 44, Component.translatable("gui.dysoncubeproject.cosmic_mining.mass_remaining",
                remainingPercent, compactNumber(remainingMass), compactNumber(initialMass)), 0xFFC5CDD3);
        PlanetType type = PlanetType.byId(planetType);
        drawLine(graphics, x, top + 58, Component.translatable("gui.dysoncubeproject.cosmic_mining.abundance",
                abundance(type)), 0xFF8FDC9A);
        drawLine(graphics, x, top + 72, Component.translatable("gui.dysoncubeproject.cosmic_mining.status",
                Component.translatable("gui.dysoncubeproject.cosmic_mining.status." + status)), statusColor());
        drawLine(graphics, x, top + 94, Component.translatable("gui.dysoncubeproject.cosmic_mining.energy",
                storedEnergy), 0xFF66D9EF);
        drawLine(graphics, x, top + 108, Component.translatable("gui.dysoncubeproject.cosmic_mining.external_energy",
                externalEnergy, connectedPorts), 0xFF8FDC9A);
        drawLine(graphics, x, top + 122, Component.translatable("gui.dysoncubeproject.cosmic_mining.energy_cost"),
                0xFFFFB347);
        drawLine(graphics, x, top + 136,
                Component.translatable("gui.dysoncubeproject.cosmic_mining.laser_input"), 0xFFC5CDD3);
        drawLine(graphics, x, top + 172, Component.translatable("gui.dysoncubeproject.cosmic_mining.zero_count",
                zeroCount), 0xFFC5CDD3);
        drawLine(graphics, x, top + 186, Component.translatable("gui.dysoncubeproject.cosmic_mining.efficiency",
                efficiencyPercent), 0xFF8FDC9A);
        drawLine(graphics, x, top + 200, Component.translatable("gui.dysoncubeproject.cosmic_mining.effective_power",
                compactNumber(effectivePower)), 0xFFFFD45E);
        drawLine(graphics, x, top + 214, Component.translatable("gui.dysoncubeproject.cosmic_mining.multiplier",
                compactNumber(effectivePower)), 0xFFAEB8FF);

        graphics.blit(type.texture(), left + PLANET_X, top + PLANET_Y, 0, 0,
                PLANET_SIZE, PLANET_SIZE, PLANET_SIZE, PLANET_SIZE);
        drawTierPanel(graphics, left, top);

        drawInventory(graphics, left, top);
        drawFluid(graphics, left, top);
        if (starTier == 16) {
            graphics.drawString(font,
                    Component.translatable("gui.dysoncubeproject.cosmic_mining.custom_mass"),
                    left + 422, top + 174, 0xFFC5CDD3, false);
        }
        for (net.minecraft.client.gui.components.Renderable renderable : renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
        if (!renderPlanetResourcePanel(graphics, left, top, mouseX, mouseY)) {
            renderInventoryTooltip(graphics, left, top, mouseX, mouseY);
            renderFluidTooltip(graphics, left, top, mouseX, mouseY);
            renderLineTooltips(graphics, left, top, mouseX, mouseY);
        }
    }

    private boolean renderPlanetResourcePanel(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        boolean expanded = Screen.hasShiftDown();
        int itemCount = expanded ? resourceItems.size() : Math.min(RESOURCE_DEFAULT_LIMIT, resourceItems.size());
        int fluidCount = expanded ? resourceFluids.size() : Math.min(RESOURCE_DEFAULT_LIMIT, resourceFluids.size());
        boolean hasMore = resourceItems.size() > RESOURCE_DEFAULT_LIMIT
                || resourceFluids.size() > RESOURCE_DEFAULT_LIMIT;
        ResourcePanelLayout layout = resourcePanelLayout(left, top, itemCount, fluidCount,
                hasMore && !expanded);
        boolean overPlanet = inside(mouseX, mouseY, left + PLANET_X, top + PLANET_Y,
                PLANET_SIZE, PLANET_SIZE);
        boolean overPanel = inside(mouseX, mouseY, layout.x(), layout.y(), layout.width(), layout.height());
        if (!overPlanet && !overPanel) return false;

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        graphics.fill(layout.x(), layout.y(), layout.x() + layout.width(), layout.y() + layout.height(),
                0xF2161B20);
        graphics.renderOutline(layout.x(), layout.y(), layout.width(), layout.height(), 0xFF62BCEB);
        Component title = Component.translatable("gui.dysoncubeproject.cosmic_mining.resources",
                Component.translatable("gui.dysoncubeproject.planet." + planetType));
        graphics.drawString(font, title, layout.x() + 7, layout.y() + 6, 0xFF62BCEB, false);

        Component itemHeading = Component.translatable("gui.dysoncubeproject.cosmic_mining.resource_items",
                resourceItems.size());
        Component fluidHeading = Component.translatable("gui.dysoncubeproject.cosmic_mining.resource_fluids",
                resourceFluids.size());
        graphics.drawString(font, itemHeading, layout.itemX(), layout.contentY(), 0xFFC5CDD3, false);
        graphics.drawString(font, fluidHeading, layout.fluidX(), layout.contentY(), 0xFFC5CDD3, false);

        HoveredResource hovered = null;
        int gridY = layout.contentY() + 13;
        if (itemCount == 0) {
            graphics.drawString(font, Component.translatable("gui.dysoncubeproject.cosmic_mining.resource_empty"),
                    layout.itemX(), gridY + 4, 0xFF7F8A92, false);
        } else {
            for (int index = 0; index < itemCount; index++) {
                int iconX = layout.itemX() + index % layout.itemColumns() * RESOURCE_ICON_CELL;
                int iconY = gridY + index / layout.itemColumns() * RESOURCE_ICON_CELL;
                ItemStack stack = resourceItems.get(index);
                drawResourceSlot(graphics, iconX, iconY);
                graphics.renderItem(stack, iconX + 1, iconY + 1);
                if (inside(mouseX, mouseY, iconX, iconY, RESOURCE_ICON_CELL, RESOURCE_ICON_CELL)) {
                    hovered = new HoveredResource(stack, null);
                }
            }
        }

        if (fluidCount == 0) {
            graphics.drawString(font, Component.translatable("gui.dysoncubeproject.cosmic_mining.resource_empty"),
                    layout.fluidX(), gridY + 4, 0xFF7F8A92, false);
        } else {
            for (int index = 0; index < fluidCount; index++) {
                int iconX = layout.fluidX() + index % layout.fluidColumns() * RESOURCE_ICON_CELL;
                int iconY = gridY + index / layout.fluidColumns() * RESOURCE_ICON_CELL;
                Fluid fluid = resourceFluids.get(index);
                drawResourceSlot(graphics, iconX, iconY);
                renderFluidIcon(graphics, fluid, iconX + 1, iconY + 1);
                if (inside(mouseX, mouseY, iconX, iconY, RESOURCE_ICON_CELL, RESOURCE_ICON_CELL)) {
                    hovered = new HoveredResource(ItemStack.EMPTY, fluid);
                }
            }
        }

        if (hasMore && !expanded) {
            graphics.drawString(font,
                    Component.translatable("gui.dysoncubeproject.cosmic_mining.resource_more"),
                    layout.x() + 7, layout.y() + layout.height() - 13, 0xFFFFD45E, false);
        }
        graphics.pose().popPose();

        if (hovered != null) {
            if (!hovered.item().isEmpty()) {
                graphics.renderComponentTooltip(font, Screen.getTooltipFromItem(minecraft, hovered.item()),
                        mouseX, mouseY);
            } else {
                graphics.renderComponentTooltip(font, List.of(
                        Component.translatable(hovered.fluid().getFluidType().getDescriptionId()),
                        Component.translatable("gui.dysoncubeproject.cosmic_mining.resource_fluid_type")),
                        mouseX, mouseY);
            }
        }
        return true;
    }

    private ResourcePanelLayout resourcePanelLayout(int left, int top, int itemCount, int fluidCount,
                                                     boolean showMore) {
        int maxRows = Math.max(1, (height - 58 - (showMore ? 13 : 0)) / RESOURCE_ICON_CELL);
        int maxColumns = Math.max(1, (width - 34) / (RESOURCE_ICON_CELL * 2));
        int itemColumns = resourceColumns(itemCount, maxRows, maxColumns);
        int fluidColumns = resourceColumns(fluidCount, maxRows, maxColumns);
        int itemRows = Math.max(1, divideRoundUp(itemCount, itemColumns));
        int fluidRows = Math.max(1, divideRoundUp(fluidCount, fluidColumns));
        Component itemHeading = Component.translatable("gui.dysoncubeproject.cosmic_mining.resource_items",
                resourceItems.size());
        Component fluidHeading = Component.translatable("gui.dysoncubeproject.cosmic_mining.resource_fluids",
                resourceFluids.size());
        int itemWidth = Math.max(itemColumns * RESOURCE_ICON_CELL, font.width(itemHeading));
        int fluidWidth = Math.max(fluidColumns * RESOURCE_ICON_CELL, font.width(fluidHeading));
        int panelWidth = 14 + itemWidth + 10 + fluidWidth;
        int panelHeight = 32 + Math.max(itemRows, fluidRows) * RESOURCE_ICON_CELL + (showMore ? 13 : 0);
        int planetX = left + PLANET_X;
        int planetY = top + PLANET_Y;
        int panelX = Math.max(4, Math.min(width - panelWidth - 4,
                planetX + PLANET_SIZE / 2 - panelWidth / 2));
        int below = planetY + PLANET_SIZE;
        int panelY = below + panelHeight <= height - 4 ? below : Math.max(4, planetY - panelHeight);
        int itemX = panelX + 7;
        return new ResourcePanelLayout(panelX, panelY, panelWidth, panelHeight, itemX,
                itemX + itemWidth + 10, panelY + 19, itemColumns, fluidColumns);
    }

    private static int resourceColumns(int count, int maxRows, int maxColumns) {
        if (count <= 0) return 1;
        int preferred = Math.max(Math.min(RESOURCE_DEFAULT_LIMIT, count), divideRoundUp(count, maxRows));
        return Math.max(1, Math.min(maxColumns, preferred));
    }

    private static int divideRoundUp(int value, int divisor) {
        return value <= 0 ? 0 : (value + divisor - 1) / divisor;
    }

    private static void drawResourceSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + RESOURCE_ICON_CELL, y + RESOURCE_ICON_CELL, 0xFF20262D);
        graphics.renderOutline(x, y, RESOURCE_ICON_CELL, RESOURCE_ICON_CELL, 0xFF65717B);
    }

    private static void renderFluidIcon(GuiGraphics graphics, Fluid fluid, int x, int y) {
        try {
            IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid);
            ResourceLocation texture = extensions.getStillTexture();
            if (texture != null) {
                TextureAtlasSprite sprite = Minecraft.getInstance()
                        .getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
                int tint = extensions.getTintColor();
                float alpha = ((tint >>> 24) & 0xFF) / 255.0F;
                graphics.setColor(((tint >>> 16) & 0xFF) / 255.0F,
                        ((tint >>> 8) & 0xFF) / 255.0F, (tint & 0xFF) / 255.0F,
                        alpha == 0.0F ? 1.0F : alpha);
                graphics.blit(x, y, 0, 16, 16, sprite);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                return;
            }
        } catch (RuntimeException ignored) {
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        ItemStack bucket = new ItemStack(fluid.getBucket());
        if (!bucket.isEmpty()) {
            graphics.renderItem(bucket, x, y);
        } else {
            graphics.fill(x, y, x + 16, y + 16, fluid == Fluids.LAVA ? 0xFFD96228 : 0xFF397DCE);
        }
    }

    private void drawInventory(GuiGraphics graphics, int left, int top) {
        graphics.drawString(font, Component.translatable("gui.dysoncubeproject.cosmic_mining.inventory"),
                left + SLOT_START_X, top + 174, 0xFFC5CDD3, false);
        for (int slot = 0; slot < inventory.length; slot++) {
            int slotX = left + SLOT_START_X + (slot % 3) * SLOT_SIZE;
            int slotY = top + SLOT_START_Y + (slot / 3) * SLOT_SIZE;
            graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF20262D);
            graphics.renderOutline(slotX, slotY, 18, 18, 0xFF65717B);
            ItemStack stack = inventory[slot];
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, slotX + 1, slotY + 1);
            }
        }
    }

    private void drawTierPanel(GuiGraphics graphics, int left, int top) {
        int x = left + 382;
        int y = top + 28;
        int panelWidth = 152;
        int panelHeight = 128;
        graphics.fill(x, y, x + panelWidth, y + panelHeight, 0xFF18232B);
        graphics.renderOutline(x, y, panelWidth, panelHeight, 0xFF397F9E);

        Component heading = Component.translatable("gui.dysoncubeproject.cosmic_mining.star_tier",
                starTier, Component.translatable("gui.dysoncubeproject.cosmic_mining.tier." + starTierId));
        graphics.drawString(font, font.split(heading, panelWidth - 12).getFirst(), x + 6, y + 8,
                0xFF62BCEB, false);
        drawTierLine(graphics, x + 6, y + 30,
                starTier == 16
                        ? Component.translatable("gui.dysoncubeproject.cosmic_mining.tier_mass_unlimited",
                        compactNumber(tierMinimumMass))
                        : Component.translatable("gui.dysoncubeproject.cosmic_mining.tier_mass_range",
                        compactNumber(tierMinimumMass), compactNumber(tierMaximumMass)), 0xFFC5CDD3);
        drawTierLine(graphics, x + 6, y + 48,
                Component.translatable("gui.dysoncubeproject.cosmic_mining.tier_energy_multiplier",
                        compactNumber(tierEnergyMultiplier)), 0xFFAEB8FF);
        drawTierLine(graphics, x + 6, y + 66,
                Component.translatable("gui.dysoncubeproject.cosmic_mining.tier_energy_cost",
                        compactNumber(energyCost)), 0xFFFFB347);
        drawTierLine(graphics, x + 6, y + 88,
                Component.translatable(canChangeTier
                        ? "gui.dysoncubeproject.cosmic_mining.tier_selectable"
                        : "gui.dysoncubeproject.cosmic_mining.tier_fixed"),
                canChangeTier ? 0xFF8FDC9A : 0xFFFFB347);
    }

    private void drawTierLine(GuiGraphics graphics, int x, int y, Component text, int color) {
        graphics.drawString(font, font.split(text, 140).getFirst(), x, y, color, false);
    }

    private void drawFluid(GuiGraphics graphics, int left, int top) {
        int x = left + 316;
        int y = top + 190;
        int height = 58;
        graphics.drawString(font, Component.translatable("gui.dysoncubeproject.cosmic_mining.fluid"),
                x, top + 174, 0xFFC5CDD3, false);
        graphics.fill(x, y, x + 96, y + height, 0xFF20262D);
        int filled = fluidId.isEmpty() ? 0 : height - 2;
        if (filled > 0) graphics.fill(x + 1, y + height - 1 - filled, x + 95, y + height - 1,
                fluidId.contains("lava") ? 0xFFD96228 : 0xFF397DCE);
        graphics.renderOutline(x, y, 96, height, 0xFF65717B);
        Component amount = Component.literal(compactNumber(fluidAmount) + " mB");
        graphics.drawString(font, amount, x + (96 - font.width(amount)) / 2, y + 25, 0xFFFFFFFF, true);
    }

    private void renderInventoryTooltip(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        for (int slot = 0; slot < inventory.length; slot++) {
            int slotX = left + SLOT_START_X + (slot % 3) * SLOT_SIZE;
            int slotY = top + SLOT_START_Y + (slot / 3) * SLOT_SIZE;
            ItemStack stack = inventory[slot];
            if (!stack.isEmpty() && inside(mouseX, mouseY, slotX, slotY, 18, 18)) {
                List<Component> tooltip = new java.util.ArrayList<>(Screen.getTooltipFromItem(minecraft, stack));
                tooltip.add(Component.translatable("gui.dysoncubeproject.cosmic_mining.tooltip.amount",
                        itemAmounts[slot]));
                graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
                return;
            }
        }
    }

    private void renderFluidTooltip(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        int x = left + 316;
        int y = top + 190;
        int height = 58;
        if (inside(mouseX, mouseY, x, y, 96, height)) {
            Component fluidName = fluidId.isEmpty() ? Component.translatable("gui.dysoncubeproject.cosmic_mining.empty")
                    : Component.translatable(BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluidId))
                    .getFluidType().getDescriptionId());
            graphics.renderComponentTooltip(font, List.of(fluidName,
                    Component.translatable("gui.dysoncubeproject.cosmic_mining.tooltip.amount_mb", fluidAmount),
                    Component.translatable("gui.dysoncubeproject.cosmic_mining.tooltip.fluid")), mouseX, mouseY);
        }
    }

    private void renderLineTooltips(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        tooltipLine(graphics, mouseX, mouseY, left + 14, top + 28, 204,
                "gui.dysoncubeproject.cosmic_mining.tooltip.target");
        tooltipLine(graphics, mouseX, mouseY, left + 14, top + 42, 260,
                "gui.dysoncubeproject.cosmic_mining.tooltip.mass");
        tooltipLine(graphics, mouseX, mouseY, left + 14, top + 56, 204,
                "gui.dysoncubeproject.cosmic_mining.tooltip.abundance");
        tooltipLine(graphics, mouseX, mouseY, left + 14, top + 70, 204,
                "gui.dysoncubeproject.cosmic_mining.tooltip.status");
        tooltipLine(graphics, mouseX, mouseY, left + 14, top + 92, 204,
                "gui.dysoncubeproject.cosmic_mining.tooltip.energy");
        tooltipLine(graphics, mouseX, mouseY, left + 14, top + 106, 204,
                "gui.dysoncubeproject.cosmic_mining.tooltip.external_energy");
        tooltipLine(graphics, mouseX, mouseY, left + 14, top + 120, 204,
                "gui.dysoncubeproject.cosmic_mining.tooltip.energy_cost");
        tooltipLine(graphics, mouseX, mouseY, left + 14, top + 170, 204,
                "gui.dysoncubeproject.cosmic_mining.tooltip.zero_count");
        tooltipLine(graphics, mouseX, mouseY, left + 14, top + 184, 204,
                "gui.dysoncubeproject.cosmic_mining.tooltip.efficiency");
        tooltipLine(graphics, mouseX, mouseY, left + 14, top + 198, 204,
                "gui.dysoncubeproject.cosmic_mining.tooltip.effective_power");
        tooltipLine(graphics, mouseX, mouseY, left + 14, top + 212, 204,
                "gui.dysoncubeproject.cosmic_mining.tooltip.multiplier");
        tooltipLine(graphics, mouseX, mouseY, left + 382, top + 28, 152,
                "gui.dysoncubeproject.cosmic_mining.tooltip.star_tier");
        tooltipLine(graphics, mouseX, mouseY, left + 382, top + 56, 152,
                "gui.dysoncubeproject.cosmic_mining.tooltip.tier_mass_range");
        tooltipLine(graphics, mouseX, mouseY, left + 382, top + 74, 152,
                "gui.dysoncubeproject.cosmic_mining.tooltip.tier_energy_multiplier");
        tooltipLine(graphics, mouseX, mouseY, left + 382, top + 92, 152,
                "gui.dysoncubeproject.cosmic_mining.tooltip.tier_energy_cost");
    }

    private void tooltipLine(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int width, String key) {
        if (inside(mouseX, mouseY, x, y, width, 12)) {
            graphics.renderComponentTooltip(font, List.of(Component.translatable(key)), mouseX, mouseY);
        }
    }

    private void parseInventory(String snapshot) {
        for (int slot = 0; slot < inventory.length; slot++) {
            inventory[slot] = ItemStack.EMPTY;
            itemAmounts[slot] = "0";
        }
        String[] entries = snapshot.split(";", -1);
        for (int slot = 0; slot < inventory.length && slot < entries.length; slot++) {
            if (entries[slot].isEmpty()) continue;
            String[] fields = entries[slot].split(",", 2);
            ResourceLocation id = ResourceLocation.tryParse(fields[0]);
            if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) continue;
            itemAmounts[slot] = fields.length > 1 ? fields[1] : "1";
            inventory[slot] = new ItemStack(BuiltInRegistries.ITEM.get(id), 1);
        }
    }

    private void parseFluids(String snapshot) {
        fluidId = "";
        fluidAmount = "0";
        if (snapshot == null || snapshot.isEmpty()) return;
        String first = snapshot.split(";", 2)[0];
        String[] fields = first.split(",", 2);
        ResourceLocation id = ResourceLocation.tryParse(fields[0]);
        if (id != null && BuiltInRegistries.FLUID.containsKey(id)) {
            fluidId = id.toString();
            fluidAmount = fields.length > 1 ? fields[1] : "1";
        }
    }

    private static List<ItemStack> parseResourceItems(String snapshot) {
        if (snapshot == null || snapshot.isEmpty()) return List.of();
        List<ItemStack> parsed = new ArrayList<>();
        for (String value : snapshot.split(";")) {
            ResourceLocation id = ResourceLocation.tryParse(value);
            if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) continue;
            ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(id));
            if (!stack.isEmpty()) parsed.add(stack);
        }
        return List.copyOf(parsed);
    }

    private static List<Fluid> parseResourceFluids(String snapshot) {
        if (snapshot == null || snapshot.isEmpty()) return List.of();
        List<Fluid> parsed = new ArrayList<>();
        for (String value : snapshot.split(";")) {
            ResourceLocation id = ResourceLocation.tryParse(value);
            if (id == null || !BuiltInRegistries.FLUID.containsKey(id)) continue;
            Fluid fluid = BuiltInRegistries.FLUID.get(id);
            if (fluid != Fluids.EMPTY) parsed.add(fluid);
        }
        return List.copyOf(parsed);
    }

    private static String abundance(PlanetType type) {
        return BigDecimal.valueOf(type.abundanceBasisPoints()).movePointLeft(4)
                .setScale(4, RoundingMode.HALF_UP).toPlainString();
    }

    private static String compactNumber(String value) {
        return NumberUtils.getAeCompactAmount(value);
    }

    private static String nonBlankNumber(String value) {
        return value == null || value.isBlank() ? "0" : value;
    }

    private static String earthMassFromKg(String value) {
        try {
            return new BigDecimal(value).divide(EARTH_MASS_KG).stripTrailingZeros().toEngineeringString();
        } catch (ArithmeticException | NumberFormatException ignored) {
            return "1E118";
        }
    }

    private static boolean isPotentialPowerInput(String value) {
        if (value.isEmpty()) return true;
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (!Character.isDigit(character) && character != '.' && character != 'e'
                    && character != 'E' && character != '+' && character != '-') return false;
        }
        return true;
    }

    private void drawLine(GuiGraphics graphics, int x, int y, Component text, int color) {
        graphics.drawString(font, font.split(text, 214).getFirst(), x, y, color, false);
    }

    private int statusColor() {
        return switch (status) {
            case "running" -> 0xFF8FDC9A;
            case "output_full", "no_energy" -> 0xFFFFB347;
            case "depleted" -> 0xFFFF6B6B;
            default -> 0xFF9AA4AC;
        };
    }

    private void updateToggleLabel() {
        if (toggleButton != null) toggleButton.setMessage(Component.translatable(running
                ? "gui.dysoncubeproject.cosmic_mining.stop"
                : "gui.dysoncubeproject.cosmic_mining.start"));
    }

    private void updateNextButton() {
        if (nextButton != null) {
            nextButton.active = canSelectNext;
            nextButton.setTooltip(canSelectNext ? null : net.minecraft.client.gui.components.Tooltip.create(
                    Component.translatable("gui.dysoncubeproject.cosmic_mining.next_locked")));
        }
    }

    private void updateTierButtons() {
        if (tierDownButton != null) {
            tierDownButton.active = canChangeTier && starTier > 1;
            tierDownButton.setTooltip(canChangeTier ? null : net.minecraft.client.gui.components.Tooltip.create(
                    Component.translatable("gui.dysoncubeproject.cosmic_mining.tier_locked")));
        }
        if (tierUpButton != null) {
            tierUpButton.active = canChangeTier && starTier < 16;
            tierUpButton.setTooltip(canChangeTier ? null : net.minecraft.client.gui.components.Tooltip.create(
                    Component.translatable("gui.dysoncubeproject.cosmic_mining.tier_locked")));
        }
    }

    private void updateCustomMassControls() {
        boolean visible = starTier == 16;
        if (customMassBox != null) {
            customMassBox.visible = visible;
            customMassBox.active = visible && canChangeTier;
            customMassBox.setTooltip(visible ? net.minecraft.client.gui.components.Tooltip.create(
                    Component.translatable(canChangeTier
                            ? "gui.dysoncubeproject.cosmic_mining.tooltip.custom_mass"
                            : "gui.dysoncubeproject.cosmic_mining.custom_mass_locked")) : null);
        }
        if (customMassButton != null) {
            customMassButton.visible = visible;
            customMassButton.active = visible && canChangeTier;
            customMassButton.setTooltip(visible && !canChangeTier
                    ? net.minecraft.client.gui.components.Tooltip.create(Component.translatable(
                    "gui.dysoncubeproject.cosmic_mining.custom_mass_locked")) : null);
        }
    }

    private void send(String action, String value, int slot) {
        DysonCubeProject.NETWORK.sendToServer(new ServerboundCosmicMiningHubPacket(blockPos, action, value, slot));
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;
        for (int slot = 0; slot < inventory.length; slot++) {
            int slotX = left + SLOT_START_X + (slot % 3) * SLOT_SIZE;
            int slotY = top + SLOT_START_Y + (slot / 3) * SLOT_SIZE;
            if (inside((int) mouseX, (int) mouseY, slotX, slotY, 18, 18)) {
                send("extract", "", slot);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            onClose();
            return true;
        }
        if (keyCode == 257 && laserInputBox != null && laserInputBox.isFocused()) {
            send("apply", laserInputBox.getValue(), -1);
            laserInputBox.setFocused(false);
            clearFocus();
            return true;
        }
        if (keyCode == 257 && customMassBox != null && customMassBox.isFocused()) {
            send("mass", customMassBox.getValue(), -1);
            customMassBox.setFocused(false);
            clearFocus();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record ResourcePanelLayout(int x, int y, int width, int height, int itemX, int fluidX,
                                       int contentY, int itemColumns, int fluidColumns) {
    }

    private record HoveredResource(ItemStack item, Fluid fluid) {
    }
}

