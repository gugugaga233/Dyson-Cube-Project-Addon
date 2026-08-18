package com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.OverhaulContent;
import com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;

public class MiningFluidOutputPortBlockEntity extends BlockEntity {
    private final IFluidHandler outputHandler = new PortFluidHandler();

    public MiningFluidOutputPortBlockEntity(BlockPos pos, BlockState state) {
        super(OverhaulContent.getMiningFluidOutputPortBEType(), pos, state);
    }

    public IFluidHandler getOutputHandler() {
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

    public String getStoredFluidAmount() {
        CosmicMiningHubBlockEntity hub = findHub();
        return hub == null ? "0" : display(hub.getStoredFluidAmount());
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
    private IFluidHandler findHandler() {
        CosmicMiningHubBlockEntity hub = findHub();
        return hub == null ? null : hub.getOutputFluids();
    }

    private final class PortFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            IFluidHandler handler = findHandler();
            return handler == null ? 0 : handler.getTanks();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            IFluidHandler handler = findHandler();
            return handler == null || tank < 0 || tank >= handler.getTanks()
                    ? FluidStack.EMPTY : handler.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            IFluidHandler handler = findHandler();
            return handler == null || tank < 0 || tank >= handler.getTanks() ? 0 : handler.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            IFluidHandler handler = findHandler();
            return handler == null || resource.isEmpty() ? FluidStack.EMPTY : handler.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            IFluidHandler handler = findHandler();
            return handler == null || maxDrain <= 0 ? FluidStack.EMPTY : handler.drain(maxDrain, action);
        }
    }
}

