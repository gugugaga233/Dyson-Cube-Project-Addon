package com.gugugaga233.dysoncubeprojectaddon.integration.ae2.client;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AmountFormat;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.common.RepoSlot;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.client.gui.style.ScreenStyle;
import appeng.core.AELog;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.Tooltips;
import appeng.menu.me.common.GridInventoryEntry;
import com.gugugaga233.dysoncubeprojectaddon.integration.ae2.DysonAETerminalMenu;
import com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class DysonAETerminalScreen extends MEStorageScreen<DysonAETerminalMenu> {
    public DysonAETerminalScreen(DysonAETerminalMenu menu,
                                 Inventory playerInventory,
                                 Component title,
                                 ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Override
    public void renderSlot(GuiGraphics graphics, Slot slot) {
        if (!(slot instanceof RepoSlot repoSlot)) {
            super.renderSlot(graphics, slot);
            return;
        }

        GridInventoryEntry entry = repoSlot.getEntry();
        if (!getMenu().getLinkStatus().connected() || entry == null) {
            return;
        }

        try {
            AEKeyRendering.drawInGui(minecraft, graphics, slot.x, slot.y, entry.getWhat());
        } catch (Exception exception) {
            AELog.warn("[Dyson AE Terminal] Prevented crash while drawing slot: " + exception);
        }

        long storedAmount = entry.getStoredAmount();
        boolean craftable = entry.isCraftable();
        boolean largeFont = config.isUseLargeFonts();
        if (craftable && (isViewOnlyCraftable() || storedAmount <= 0)) {
            StackSizeRenderer.renderSizeLabel(graphics, font, slot.x, slot.y, "+");
            return;
        }

        AmountFormat format = largeFont ? AmountFormat.SLOT_LARGE_FONT : AmountFormat.SLOT;
        AE2ExactAmountClientCache.ExactAmount exact =
                AE2ExactAmountClientCache.get(getMenu().containerId, entry.getWhat());
        if (exact != null) {
            renderExactSizeLabel(graphics, slot.x, slot.y, exact.compact(), largeFont);
        } else {
            String amount = entry.getWhat().formatAmount(storedAmount, format);
            StackSizeRenderer.renderSizeLabel(graphics, font, slot.x, slot.y, amount, largeFont);
        }
        if (craftable) {
            StackSizeRenderer.renderSizeLabel(graphics, font, slot.x - 11, slot.y - 11, "+", false);
        }
    }

    @Override
    protected void renderGridInventoryEntryTooltip(GuiGraphics graphics,
                                                    GridInventoryEntry entry,
                                                    int mouseX,
                                                    int mouseY) {
        List<Component> tooltip = new ArrayList<>(AEKeyRendering.getTooltip(entry.getWhat()));
        AE2ExactAmountClientCache.ExactAmount exact = AE2ExactAmountClientCache.get(
                getMenu().containerId, entry.getWhat());
        if (exact == null && Tooltips.shouldShowAmountTooltip(entry.getWhat(), entry.getStoredAmount())) {
            tooltip.add(Tooltips.getAmountTooltip(
                    ButtonToolTips.StoredAmount, entry.getWhat(), entry.getStoredAmount()));
        }

        long requestableAmount = entry.getRequestableAmount();
        if (requestableAmount > 0) {
            tooltip.add(ButtonToolTips.RequestableAmount.text(
                    entry.getWhat().formatAmount(requestableAmount, AmountFormat.FULL)));
        }
        if (entry.isCraftable() && !isViewOnlyCraftable() && entry.getStoredAmount() > 0) {
            tooltip.add(ButtonToolTips.Craftable.text().copy().withStyle(ChatFormatting.DARK_GRAY));
        }
        if (Minecraft.getInstance().options.advancedItemTooltips) {
            tooltip.add(ButtonToolTips.Serial.text(entry.getSerial()).withStyle(ChatFormatting.DARK_GRAY));
        }

        appendExactAmount(tooltip, exact);
        if (entry.getWhat() instanceof AEItemKey itemKey) {
            ItemStack stack = itemKey.getReadOnlyStack();
            graphics.renderTooltip(font, tooltip, stack.getTooltipImage(), stack, mouseX, mouseY);
        } else {
            graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public void removed() {
        AE2ExactAmountClientCache.clear(getMenu().containerId);
        super.removed();
    }

    private static void appendExactAmount(List<Component> tooltip,
                                          AE2ExactAmountClientCache.ExactAmount amount) {
        if (amount == null) {
            return;
        }
        String exact = amount.exact() == null || amount.exact().isBlank()
                ? amount.compact() : amount.exact();
        tooltip.add(Component.translatable("gui.dysoncubeproject.ae2.exact_amount", exact)
                .withStyle(ChatFormatting.AQUA));
        String explanation = NumberUtils.getScientificNotationExplanation(exact);
        if (explanation != null) {
            tooltip.add(Component.translatable("gui.dysoncubeproject.ae2.scientific_explanation", explanation)
                    .withStyle(ChatFormatting.GRAY));
        }
        if (amount.storage() != null && !amount.storage().isBlank()) {
            tooltip.add(Component.translatable("gui.dysoncubeproject.tooltip.flux_storage_value",
                    amount.storage()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private void renderExactSizeLabel(GuiGraphics graphics, int slotX, int slotY,
                                      String text, boolean largeFont) {
        float scale = largeFont ? 0.85F : 0.666F;
        int offset = largeFont ? 0 : -1;
        int x = (int) ((slotX + offset + 18.0F - font.width(text) * scale) / scale);
        int y = (int) ((slotY + offset + 16.0F - 5.0F * scale) / scale);

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 200.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, text, x, y, 0xFFFFFF, true);
        graphics.pose().popPose();
    }
}

