package com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.OverhaulContent;
import com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;

public class MiningItemOutputPortBlockEntity extends BlockEntity {
    private final IItemHandler outputHandler = new PortItemHandler();

    public MiningItemOutputPortBlockEntity(BlockPos pos, BlockState state) {
        super(OverhaulContent.getMiningItemOutputPortBEType(), pos, state);
    }

    public IItemHandler getOutputHandler() {
        return outputHandler;
    }

    public void refreshExternalStorage() {
        invalidateCapabilities();
        if (level != null && !level.isClientSide) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    public boolean isConnected() {
        return findHub() != null;
    }

    public String getStoredItemCount() {
        CosmicMiningHubBlockEntity hub = findHub();
        return hub == null ? "0" : display(hub.getStoredItemAmount());
    }

    private static String display(AbsoluteInteger amount) {
        return NumberUtils.getScientificInteger(amount);
    }

    @Nullable
    public CosmicMiningHubBlockEntity findHub() {
        if (level == null) return null;
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof CosmicMiningHubBlockEntity hub) {
                return hub;
            }
        }
        return null;
    }

    @Nullable
    private IItemHandler findHandler() {
        CosmicMiningHubBlockEntity hub = findHub();
        return hub == null ? null : hub.getOutputItems();
    }

    private final class PortItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            IItemHandler handler = findHandler();
            return handler == null ? 0 : handler.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            IItemHandler handler = findHandler();
            return handler == null || slot < 0 || slot >= handler.getSlots()
                    ? ItemStack.EMPTY : handler.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            IItemHandler handler = findHandler();
            return handler == null || slot < 0 || slot >= handler.getSlots() || amount <= 0
                    ? ItemStack.EMPTY : handler.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            IItemHandler handler = findHandler();
            return handler == null || slot < 0 || slot >= handler.getSlots() ? 0 : handler.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    }
}

