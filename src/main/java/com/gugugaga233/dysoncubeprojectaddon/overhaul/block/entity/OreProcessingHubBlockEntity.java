package com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity;

import com.gugugaga233.dysoncubeprojectaddon.DCPConfig;
import com.gugugaga233.dysoncubeprojectaddon.DysonCubeProject;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientboundOpenOreProcessingHubPacket;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.OverhaulContent;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.AbsoluteMiningInventory;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.OreProcessingMath;
import com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;
import sonar.fluxnetworks.api.energy.BigNumber;

public final class OreProcessingHubBlockEntity extends BlockEntity {
    private static final int OUTPUT_INTERVAL = 5;

    private final AbsoluteMiningInventory products = new AbsoluteMiningInventory();
    private final IItemHandler outputHandler = new ProductItemHandler();
    private final Map<Item, ItemStack> recipeCache = new IdentityHashMap<>();
    private RecipeManager cachedRecipeManager;
    private long cycle;
    private String status = "no_input";
    private AbsoluteInteger lastProcessed = new AbsoluteInteger();
    private AbsoluteInteger totalProcessed = new AbsoluteInteger();
    private AbsoluteInteger pendingOre = new AbsoluteInteger();
    private BigNumber lastEnergyCost = new BigNumber(0);
    private AbsoluteInteger cachedEnergyInput = new AbsoluteInteger();
    private long cachedCostPerOre = Long.MIN_VALUE;
    private BigNumber cachedEnergyCost = new BigNumber(0);

    public OreProcessingHubBlockEntity(BlockPos pos, BlockState state) {
        super(OverhaulContent.getOreProcessingHubBEType(), pos, state);
    }

    public void tick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        cycle++;
        if (cycle % OUTPUT_INTERVAL == 0) pushProducts();
        if (!DCPConfig.ORE_PROCESSING_ENABLED.get()) {
            status = "disabled";
            pendingOre = new AbsoluteInteger();
            return;
        }

        CosmicMiningHubBlockEntity source = findSourceHub();
        if (source == null) {
            status = "no_input";
            pendingOre = new AbsoluteInteger();
            return;
        }

        BatchPlan plan = createPlan(serverLevel, source);
        pendingOre = plan.inputAmount();
        if (plan.entries().isEmpty()) {
            status = "no_ore";
            return;
        }

        long costPerOre = DCPConfig.ORE_PROCESSING_COST.get();
        BigNumber energyCost = energyCost(plan.inputAmount(), costPerOre);
        if (!source.consumeOreProcessingEnergy(energyCost.deepCopy())) {
            status = "no_energy";
            lastEnergyCost = energyCost;
            return;
        }

        AbsoluteInteger processed = new AbsoluteInteger();
        for (PlanEntry entry : plan.entries()) {
            AbsoluteInteger extracted = source.getAbsoluteMiningInventory()
                    .extractItemExact(entry.inputId(), entry.inputAmount(), false);
            if (extracted.isZero()) continue;
            products.addItem(entry.output(), OreProcessingMath.outputAmount(
                    extracted, entry.output().getCount()));
            processed.add(extracted);
        }
        if (processed.isZero()) return;

        source.markExternalOutputChanged();
        lastProcessed = processed;
        FluxMath8.addInPlace(totalProcessed, processed);
        lastEnergyCost = energyCost;
        status = "processed";
        markProductInventoryChanged();
    }

    private BigNumber energyCost(AbsoluteInteger inputAmount, long costPerOre) {
        if (costPerOre != cachedCostPerOre || inputAmount.compareTo(cachedEnergyInput) != 0) {
            cachedEnergyInput = inputAmount.copy();
            cachedCostPerOre = costPerOre;
            cachedEnergyCost = OreProcessingMath.energyCost(inputAmount, costPerOre);
        }
        return cachedEnergyCost.deepCopy();
    }

    private BatchPlan createPlan(ServerLevel serverLevel, CosmicMiningHubBlockEntity source) {
        if (cachedRecipeManager != serverLevel.getRecipeManager()) {
            cachedRecipeManager = serverLevel.getRecipeManager();
            recipeCache.clear();
        }
        java.util.List<PlanEntry> entries = new java.util.ArrayList<>();
        AbsoluteInteger total = new AbsoluteInteger();
        for (AbsoluteMiningInventory.ExactEntry entry : source.getAbsoluteMiningInventory().itemEntries()) {
            Item input = BuiltInRegistries.ITEM.get(entry.id());
            if (input == null) continue;
            ItemStack output = recipeCache.computeIfAbsent(input,
                    ignored -> resolveOutput(serverLevel, new ItemStack(input)));
            if (output.isEmpty()) continue;
            entries.add(new PlanEntry(entry.id(), entry.amount(), output.copy()));
            total.add(entry.amount());
        }
        return new BatchPlan(entries, total);
    }

    private static ItemStack resolveOutput(ServerLevel level, ItemStack input) {
        if (!isOre(input)) return ItemStack.EMPTY;
        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, recipeInput, level)
                .map(RecipeHolder<SmeltingRecipe>::value)
                .map(recipe -> recipe.assemble(recipeInput, level.registryAccess()))
                .filter(stack -> !stack.isEmpty())
                .orElse(ItemStack.EMPTY);
    }

    private static boolean isOre(ItemStack stack) {
        return stack.getTags().anyMatch(tag -> {
            String path = tag.location().getPath();
            return path.startsWith("ores/") || path.endsWith("_ores")
                    || path.equals("ores") || path.startsWith("raw_materials/");
        });
    }

    private void pushProducts() {
        if (level == null) return;
        boolean changed = false;
        for (Direction direction : Direction.values()) {
            BlockPos targetPos = worldPosition.relative(direction);
            BlockEntity targetEntity = level.getBlockEntity(targetPos);
            IItemHandler target = level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos,
                    level.getBlockState(targetPos), targetEntity, direction.getOpposite());
            if (target == null || target == outputHandler) continue;
            for (int slot = 0; slot < products.getItemSlots(); slot++) {
                ItemStack offered = products.extractItem(slot, Integer.MAX_VALUE, true);
                if (offered.isEmpty()) continue;
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(target, offered, false);
                int moved = offered.getCount() - remainder.getCount();
                if (moved > 0) {
                    products.extractItem(slot, moved, false);
                    changed = true;
                }
            }
        }
        if (changed) markProductInventoryChanged();
    }

    private CosmicMiningHubBlockEntity findSourceHub() {
        if (level == null) return null;
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction))
                    instanceof MiningItemOutputPortBlockEntity port) {
                CosmicMiningHubBlockEntity hub = port.findHub();
                if (hub != null) return hub;
            }
        }
        return null;
    }

    private int connectedInputCount() {
        if (level == null) return 0;
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction))
                    instanceof MiningItemOutputPortBlockEntity port && port.findHub() != null) count++;
        }
        return count;
    }

    public void openGui(Player player) {
        if (player instanceof ServerPlayer serverPlayer) sendSnapshot(serverPlayer);
    }

    public void sendSnapshot(ServerPlayer player) {
        StringBuilder inventory = new StringBuilder();
        for (AbsoluteMiningInventory.ExactEntry entry : products.itemEntries()) {
            if (!inventory.isEmpty()) inventory.append(';');
            inventory.append(entry.id()).append(',')
                    .append(NumberUtils.getScientificInteger(entry.amount())).append(',')
                    .append(entry.amount().toCalculationString());
        }
        DysonCubeProject.NETWORK.sendTo(new ClientboundOpenOreProcessingHubPacket(
                worldPosition.asLong(), status, DCPConfig.ORE_PROCESSING_ENABLED.get(),
                DCPConfig.ORE_PROCESSING_COST.get(), connectedInputCount(),
                NumberUtils.getScientificInteger(pendingOre),
                NumberUtils.getScientificInteger(lastProcessed),
                NumberUtils.getScientificInteger(totalProcessed),
                NumberUtils.getScientificInteger(products.getTotalItemAmount()),
                stripFe(lastEnergyCost.toDisplayString()), inventory.toString()), player);
    }

    public void extractSlot(Player player, int slot) {
        ItemStack extracted = products.extractItem(slot, 64, false);
        if (extracted.isEmpty()) return;
        markProductInventoryChanged();
        if (!player.getInventory().add(extracted)) player.drop(extracted, false);
    }

    public AbsoluteMiningInventory getProductInventory() {
        return products;
    }

    public IItemHandler getOutputHandler() {
        return outputHandler;
    }

    public void markExternalOutputChanged() {
        markProductInventoryChanged();
    }

    private void markProductInventoryChanged() {
        setChanged();
        invalidateCapabilities();
        if (level != null && !level.isClientSide) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    private static String stripFe(String display) {
        return display.endsWith(" FE") ? display.substring(0, display.length() - 3) : display;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putLong("cycle", cycle);
        tag.put("products", products.save());
        tag.put("totalProcessed", FluxMath8.toCompactTag(totalProcessed));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        cycle = tag.getLong("cycle");
        if (tag.contains("products")) products.load(tag.getCompound("products"));
        if (tag.contains("totalProcessed")) {
            totalProcessed = FluxMath8.fromCompactTag(tag.getCompound("totalProcessed"));
        }
    }

    private final class ProductItemHandler implements IItemHandler {
        @Override public int getSlots() { return products.getItemSlots(); }
        @Override public ItemStack getStackInSlot(int slot) { return products.getItemInSlot(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack extracted = products.extractItem(slot, amount, simulate);
            if (!simulate && !extracted.isEmpty()) markProductInventoryChanged();
            return extracted;
        }
        @Override public int getSlotLimit(int slot) { return Integer.MAX_VALUE; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
    }

    private record PlanEntry(ResourceLocation inputId, AbsoluteInteger inputAmount, ItemStack output) {
    }

    private record BatchPlan(java.util.List<PlanEntry> entries, AbsoluteInteger inputAmount) {
    }
}

