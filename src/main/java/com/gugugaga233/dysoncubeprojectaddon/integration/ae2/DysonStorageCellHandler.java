package com.gugugaga233.dysoncubeprojectaddon.integration.ae2;

import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import net.minecraft.world.item.ItemStack;

public final class DysonStorageCellHandler implements ICellHandler {
    public static final DysonStorageCellHandler INSTANCE = new DysonStorageCellHandler();

    private DysonStorageCellHandler() {
    }

    @Override
    public boolean isCell(ItemStack stack) {
        return stack.is(AE2Integration.DYSON_STORAGE_CELL.get())
                || stack.is(AE2Integration.DYSON_FLUID_STORAGE_CELL.get());
    }

    @Override
    public StorageCell getCellInventory(ItemStack stack, ISaveProvider saveProvider) {
        if (stack.is(AE2Integration.DYSON_STORAGE_CELL.get())) {
            return new DysonStorageCellInventory(stack, saveProvider,
                    DysonStorageCellInventory.Kind.ITEM);
        }
        if (stack.is(AE2Integration.DYSON_FLUID_STORAGE_CELL.get())) {
            return new DysonStorageCellInventory(stack, saveProvider,
                    DysonStorageCellInventory.Kind.FLUID);
        }
        return null;
    }
}
