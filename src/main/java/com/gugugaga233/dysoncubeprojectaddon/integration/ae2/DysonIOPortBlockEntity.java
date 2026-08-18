package com.gugugaga233.dysoncubeprojectaddon.integration.ae2;

import appeng.api.config.Actionable;
import appeng.api.config.OperationMode;
import appeng.api.config.Settings;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.cells.StorageCell;
import appeng.blockentity.storage.IOPortBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.me.helpers.MachineSource;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class DysonIOPortBlockEntity extends IOPortBlockEntity {
    private static final int INPUT_SLOTS = 6;
    private static final long NORMAL_CELL_BASE_OPERATIONS = 1L << 20;
    private static final int NORMAL_CELL_MAX_KEYS_PER_TICK = 8192;
    private static final ResourceLocation SUPER_SPEED_CARD_ID =
            ResourceLocation.fromNamespaceAndPath("makeae2better", "super_speed_card");
    private final IActionSource actionSource = new MachineSource(this);

    public DysonIOPortBlockEntity(BlockPos pos, BlockState state) {
        this(AE2Integration.DYSON_IO_PORT_BLOCK_ENTITY.get(), pos, state);
    }

    public DysonIOPortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        IGrid grid = getMainNode().getGrid();
        if (grid == null || !getMainNode().isActive()) return TickRateModulation.IDLE;

        boolean exactTransfer = false;
        InternalInventory inventory = getInternalInventory();
        OperationMode operation = getConfigManager().getSetting(Settings.OPERATION_MODE);
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            StorageCell cell = StorageCells.getCellInventory(stack, null);
            if (cell instanceof DysonStorageCellInventory dysonCell) {
                boolean moved = operation == OperationMode.FILL
                        ? DysonExactNetworkTransfer.fillCellFromNetwork(
                        grid, dysonCell, grid.getEnergyService(), actionSource)
                        : DysonExactNetworkTransfer.emptyCellToNetwork(
                        grid, dysonCell, grid.getEnergyService(), actionSource);
                exactTransfer |= moved;
                if (matchesFullnessMode(dysonCell) && moveToOutput(inventory, slot)) {
                    exactTransfer = true;
                }
            }
        }

        boolean normalTransfer = false;
        boolean normalCellPresent = false;
        long normalOperations = getNormalOperationsPerTick();
        int normalKeysRemaining = NORMAL_CELL_MAX_KEYS_PER_TICK;
        MEStorage networkInventory = grid.getStorageService().getInventory();
        IEnergyService energy = grid.getEnergyService();
        KeyCounter networkEntries = operation == OperationMode.FILL
                ? grid.getStorageService().getCachedInventory() : null;
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            StorageCell cell = StorageCells.getCellInventory(stack, null);
            if (cell instanceof DysonStorageCellInventory) continue;

            if (cell == null) {
                if (moveToOutput(inventory, slot)) normalTransfer = true;
                continue;
            }

            normalCellPresent = true;
            if (normalOperations > 0 && normalKeysRemaining > 0) {
                NormalTransferResult result = transferNormalCell(
                        networkInventory, energy, cell, operation == OperationMode.FILL,
                        operation == OperationMode.FILL ? networkEntries : cell.getAvailableStacks(),
                        normalOperations, normalKeysRemaining);
                normalOperations = result.remainingOperations();
                normalKeysRemaining = result.remainingKeys();
                normalTransfer |= result.moved();
            }
            if (matchesFullnessMode(cell) && moveToOutput(inventory, slot)) {
                normalTransfer = true;
            }
        }

        if (normalTransfer && normalOperations <= 0) return TickRateModulation.URGENT;
        if (normalTransfer || normalCellPresent) return TickRateModulation.IDLE;
        return exactTransfer ? TickRateModulation.URGENT : TickRateModulation.SLEEP;
    }

    private long getNormalOperationsPerTick() {
        Item superSpeedCard = BuiltInRegistries.ITEM.get(SUPER_SPEED_CARD_ID);
        if (getUpgrades().getInstalledUpgrades(superSpeedCard) > 0) {
            return Long.MAX_VALUE;
        }
        int speedCards = getUpgrades().getInstalledUpgrades(AEItems.SPEED_CARD);
        int shift = Math.min(10, Math.max(0, speedCards));
        return NORMAL_CELL_BASE_OPERATIONS << shift;
    }

    private NormalTransferResult transferNormalCell(
            MEStorage networkInventory,
            IEnergyService energy,
            StorageCell cell,
            boolean fill,
            Iterable<Object2LongMap.Entry<AEKey>> entries,
            long operations,
            int keysRemaining) {
        MEStorage source = fill ? networkInventory : cell;
        MEStorage destination = fill ? cell : networkInventory;
        boolean moved = false;

        for (Object2LongMap.Entry<AEKey> entry : entries) {
            if (operations <= 0 || keysRemaining <= 0) break;
            keysRemaining--;

            AEKey what = entry.getKey();
            long available = entry.getLongValue();
            if (available <= 0) continue;

            long amountPerOperation = Math.max(1L, what.getAmountPerOperation());
            long maxAmount = operations > Long.MAX_VALUE / amountPerOperation
                    ? Long.MAX_VALUE : operations * amountPerOperation;
            long possible = Math.min(available,
                    destination.insert(what, maxAmount, Actionable.SIMULATE, actionSource));
            if (possible <= 0) continue;

            long extracted = source.extract(what, possible, Actionable.MODULATE, actionSource);
            if (extracted <= 0) continue;

            long inserted = StorageHelper.poweredInsert(
                    energy, destination, what, extracted, actionSource);
            if (inserted < extracted) {
                source.insert(what, extracted - inserted, Actionable.MODULATE, actionSource);
            }
            if (inserted <= 0) continue;

            operations -= Math.min(operations,
                    Math.max(1L, inserted / amountPerOperation));
            moved = true;
        }

        return new NormalTransferResult(operations, keysRemaining, moved);
    }

    private record NormalTransferResult(long remainingOperations, int remainingKeys, boolean moved) {
    }

    private static boolean moveToOutput(InternalInventory inventory, int inputSlot) {
        ItemStack stack = inventory.getStackInSlot(inputSlot);
        if (stack.isEmpty()) return false;
        InternalInventory outputs = inventory.getSubInventory(INPUT_SLOTS, INPUT_SLOTS);
        if (!outputs.addItems(stack).isEmpty()) return false;
        inventory.setItemDirect(inputSlot, ItemStack.EMPTY);
        return true;
    }
}
