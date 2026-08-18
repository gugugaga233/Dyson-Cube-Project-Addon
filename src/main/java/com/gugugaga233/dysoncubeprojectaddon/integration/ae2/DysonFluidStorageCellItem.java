package com.gugugaga233.dysoncubeprojectaddon.integration.ae2;

import appeng.items.AEBaseItem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class DysonFluidStorageCellItem extends AEBaseItem {
    public DysonFluidStorageCellItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        DysonStorageCellInventory inventory = new DysonStorageCellInventory(
                stack, null, DysonStorageCellInventory.Kind.FLUID);
        tooltip.add(Component.translatable("item.dysoncubeproject.dyson_fluid_storage_cell.types",
                inventory.getStoredTypeCount(), DysonStorageCellInventory.MAX_TYPES)
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.dysoncubeproject.dyson_fluid_storage_cell.total",
                inventory.getTotalDisplay()).withStyle(ChatFormatting.GOLD));
    }
}
