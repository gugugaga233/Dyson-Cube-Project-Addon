package com.gugugaga233.dysoncubeprojectaddon.integration.ae2;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import com.gugugaga233.dysoncubeprojectaddon.DCPAttachments;
import com.gugugaga233.dysoncubeprojectaddon.item.CompressedItem;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class CompressionPatternDetails implements IMolecularAssemblerSupportedPattern {
    private static final int GRID_SIZE = 9;

    private final AEItemKey definition;
    private final AEItemKey inputKey;
    private final AEItemKey outputKey;
    private final IInput[] inputs;
    private final List<GenericStack> outputs;

    private CompressionPatternDetails(AEItemKey definition, AEItemKey inputKey, AEItemKey outputKey) {
        this.definition = definition;
        this.inputKey = inputKey;
        this.outputKey = outputKey;
        this.inputs = new IInput[]{new CompressionInput(inputKey)};
        this.outputs = List.of(new GenericStack(outputKey, 1));
    }

    @Nullable
    public static CompressionPatternDetails decode(AEItemKey definition, Level level) {
        if (!definition.is(AE2Integration.COMPRESSION_PATTERN.get())) {
            return null;
        }

        ItemStack target = definition.getReadOnlyStack().get(DCPAttachments.AE2_COMPRESSION_TARGET.get());
        if (target == null || target.isEmpty() || !(target.getItem() instanceof CompressedItem)) {
            return null;
        }

        ItemStack input = target.copyWithCount(1);
        BigInteger inputLevel = CompressedItem.getLevel(input);
        if (inputLevel.signum() < 1) {
            return null;
        }

        ItemStack output = input.copy();
        output.set(DCPAttachments.COMPRESSION_LEVEL.get(), inputLevel.add(BigInteger.ONE));
        AEItemKey inputKey = AEItemKey.of(input);
        AEItemKey outputKey = AEItemKey.of(output);
        if (inputKey == null || outputKey == null) {
            return null;
        }
        return new CompressionPatternDetails(definition, inputKey, outputKey);
    }

    @Override
    public AEItemKey getDefinition() {
        return definition;
    }

    @Override
    public IInput[] getInputs() {
        return inputs;
    }

    @Override
    public List<GenericStack> getOutputs() {
        return outputs;
    }

    @Override
    public ItemStack assemble(CraftingInput input, Level level) {
        if (input.ingredientCount() != GRID_SIZE) {
            return ItemStack.EMPTY;
        }
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty() || !inputKey.matches(stack)) {
                return ItemStack.EMPTY;
            }
        }
        return outputKey.toStack();
    }

    @Override
    public boolean isItemValid(int slot, AEItemKey key, Level level) {
        return isSlotEnabled(slot) && inputKey.equals(key);
    }

    @Override
    public boolean isSlotEnabled(int slot) {
        return slot >= 0 && slot < GRID_SIZE;
    }

    @Override
    public void fillCraftingGrid(KeyCounter[] inputs, CraftingGridAccessor grid) {
        if (inputs.length == 0) {
            return;
        }
        KeyCounter available = inputs[0];
        for (int slot = 0; slot < GRID_SIZE && available.get(inputKey) > 0; slot++) {
            grid.set(slot, inputKey.toStack());
            available.remove(inputKey, 1);
        }
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof CompressionPatternDetails details
                && definition.equals(details.definition);
    }

    @Override
    public int hashCode() {
        return definition.hashCode();
    }

    private static final class CompressionInput implements IPatternDetails.IInput {
        private final AEItemKey key;
        private final GenericStack[] possibleInputs;

        private CompressionInput(AEItemKey key) {
            this.key = key;
            this.possibleInputs = new GenericStack[]{new GenericStack(key, 1)};
        }

        @Override
        public GenericStack[] getPossibleInputs() {
            return possibleInputs;
        }

        @Override
        public long getMultiplier() {
            return GRID_SIZE;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            return key.equals(input);
        }

        @Override
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || other instanceof CompressionInput input && key.equals(input.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, GRID_SIZE);
        }
    }
}

