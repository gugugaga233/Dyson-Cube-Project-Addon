package com.gugugaga233.dysoncubeprojectaddon.overhaul.mining;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8;
import com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.slf4j.Logger;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;
import sonar.fluxnetworks.api.energy.BigNumber;

/**
 * Exact mining output ledger backed by Flux's absolute exponent integer.
 * Vanilla transfer APIs only see and extract finite primitive-sized batches.
 */
public final class AbsoluteMiningInventory {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MINIMUM_ITEM_SLOTS = 9;
    private static final int MINIMUM_FLUID_TANKS = 256;

    private final Map<ResourceLocation, AbsoluteInteger> items = new LinkedHashMap<>();
    private final Map<ResourceLocation, AbsoluteInteger> fluids = new LinkedHashMap<>();

    public AbsoluteMiningInventory() {
    }

    public void addItem(ItemStack stack, AbsoluteInteger amount) {
        if (stack == null || stack.isEmpty()) return;
        add(items, BuiltInRegistries.ITEM.getKey(stack.getItem()), amount);
    }

    public void addFluid(FluidStack stack, AbsoluteInteger amount) {
        if (stack == null || stack.isEmpty()) return;
        add(fluids, BuiltInRegistries.FLUID.getKey(stack.getFluid()), amount);
    }

    private void add(Map<ResourceLocation, AbsoluteInteger> entries, ResourceLocation id, AbsoluteInteger amount) {
        if (id == null || amount == null || amount.isZero()) return;
        entries.compute(id, (ignored, current) -> {
            AbsoluteInteger result = current == null ? new AbsoluteInteger() : current;
            FluxMath8.addInPlace(result, amount);
            return result;
        });
    }

    public int getItemSlots() {
        return Math.max(MINIMUM_ITEM_SLOTS, items.size());
    }

    public int getFluidTanks() {
        return Math.max(MINIMUM_FLUID_TANKS, fluids.size());
    }

    public int getStoredFluidTypes() {
        return fluids.size();
    }

    public ItemStack getItemInSlot(int slot) {
        Map.Entry<ResourceLocation, AbsoluteInteger> entry = entryAt(items, slot);
        if (entry == null) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.get(entry.getKey());
        if (item == null) return ItemStack.EMPTY;
        int count = (int) FluxMath8.quoteChunk(entry.getValue(), Integer.MAX_VALUE);
        return count <= 0 ? ItemStack.EMPTY : new ItemStack(item, count);
    }

    public ItemStack extractItem(int slot, int requested, boolean simulate) {
        Map.Entry<ResourceLocation, AbsoluteInteger> entry = entryAt(items, slot);
        if (entry == null || requested <= 0) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.get(entry.getKey());
        if (item == null) return ItemStack.EMPTY;
        int extracted = (int) Math.min(requested,
                FluxMath8.quoteChunk(entry.getValue(), Integer.MAX_VALUE));
        if (extracted <= 0) return ItemStack.EMPTY;
        if (!simulate) {
            entry.setValue(FluxMath8.extractChunk(entry.getValue(), extracted));
            removeEmpty(items);
        }
        return new ItemStack(item, extracted);
    }

    public FluidStack getFluidInTank(int tank) {
        Map.Entry<ResourceLocation, AbsoluteInteger> entry = entryAt(fluids, tank);
        if (entry == null) return FluidStack.EMPTY;
        Fluid fluid = BuiltInRegistries.FLUID.get(entry.getKey());
        if (fluid == null || fluid == Fluids.EMPTY) return FluidStack.EMPTY;
        int amount = (int) Math.min(Integer.MAX_VALUE,
                FluxMath8.quoteChunk(entry.getValue(), Integer.MAX_VALUE));
        return amount <= 0 ? FluidStack.EMPTY : new FluidStack(fluid, amount);
    }

    public FluidStack drainFluid(int tank, int requested, boolean simulate) {
        Map.Entry<ResourceLocation, AbsoluteInteger> entry = entryAt(fluids, tank);
        if (entry == null || requested <= 0) return FluidStack.EMPTY;
        Fluid fluid = BuiltInRegistries.FLUID.get(entry.getKey());
        if (fluid == null || fluid == Fluids.EMPTY) return FluidStack.EMPTY;
        int drained = (int) Math.min(requested,
                FluxMath8.quoteChunk(entry.getValue(), Integer.MAX_VALUE));
        if (drained <= 0) return FluidStack.EMPTY;
        if (!simulate) {
            entry.setValue(FluxMath8.extractChunk(entry.getValue(), drained));
            removeEmpty(fluids);
        }
        return new FluidStack(fluid, drained);
    }

    public FluidStack drainFluid(FluidStack requested, boolean simulate) {
        if (requested == null || requested.isEmpty()) return FluidStack.EMPTY;
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(requested.getFluid());
        int tank = indexOf(fluids, id);
        return tank < 0 ? FluidStack.EMPTY : drainFluid(tank, requested.getAmount(), simulate);
    }

    public AbsoluteInteger getItemAmount(int slot) {
        Map.Entry<ResourceLocation, AbsoluteInteger> entry = entryAt(items, slot);
        return entry == null ? new AbsoluteInteger() : entry.getValue().copy();
    }

    public AbsoluteInteger getFluidAmount(int tank) {
        Map.Entry<ResourceLocation, AbsoluteInteger> entry = entryAt(fluids, tank);
        return entry == null ? new AbsoluteInteger() : entry.getValue().copy();
    }

    public AbsoluteInteger getItemAmount(ResourceLocation id) {
        return amountFor(items, id);
    }

    public AbsoluteInteger getFluidAmount(ResourceLocation id) {
        return amountFor(fluids, id);
    }

    public long quoteItem(ResourceLocation id, long maximum) {
        return quote(items, id, maximum);
    }

    public long quoteFluid(ResourceLocation id, long maximum) {
        return quote(fluids, id, maximum);
    }

    public long extractItem(ResourceLocation id, long maximum, boolean simulate) {
        return extract(items, id, maximum, simulate);
    }

    public AbsoluteInteger extractItemExact(ResourceLocation id,
                                            AbsoluteInteger maximum,
                                            boolean simulate) {
        return extractExact(items, id, maximum, simulate);
    }

    public AbsoluteInteger extractFluidExact(ResourceLocation id,
                                              AbsoluteInteger maximum,
                                              boolean simulate) {
        return extractExact(fluids, id, maximum, simulate);
    }

    private static AbsoluteInteger extractExact(Map<ResourceLocation, AbsoluteInteger> entries,
                                                ResourceLocation id,
                                                AbsoluteInteger maximum,
                                                boolean simulate) {
        AbsoluteInteger stored = id == null ? null : entries.get(id);
        if (stored == null || maximum == null || maximum.isZero()) return new AbsoluteInteger();
        AbsoluteInteger extracted = stored.compareTo(maximum) <= 0 ? stored.copy() : maximum.copy();
        if (!simulate && !extracted.isZero()) {
            entries.put(id, FluxMath8.subtract(stored, extracted));
            removeEmpty(entries);
        }
        return extracted;
    }

    public long extractFluid(ResourceLocation id, long maximum, boolean simulate) {
        return extract(fluids, id, maximum, simulate);
    }

    public List<ExactEntry> itemEntries() {
        return exactEntries(items);
    }

    public List<ExactEntry> fluidEntries() {
        return exactEntries(fluids);
    }

    public AbsoluteInteger getTotalItemAmount() {
        return total(items);
    }

    public AbsoluteInteger getTotalFluidAmount() {
        return total(fluids);
    }

    public List<ItemSnapshot> itemSnapshots() {
        List<ItemSnapshot> snapshots = new ArrayList<>(items.size());
        items.forEach((id, amount) -> snapshots.add(new ItemSnapshot(id, display(amount))));
        return snapshots;
    }

    public List<FluidSnapshot> fluidSnapshots() {
        List<FluidSnapshot> snapshots = new ArrayList<>(fluids.size());
        fluids.forEach((id, amount) -> snapshots.add(new FluidSnapshot(id, display(amount))));
        return snapshots;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("items", saveEntries(items));
        tag.put("fluids", saveEntries(fluids));
        return tag;
    }

    public void load(CompoundTag tag) {
        items.clear();
        fluids.clear();
        loadEntries(tag.getList("items", Tag.TAG_COMPOUND), items);
        loadEntries(tag.getList("fluids", Tag.TAG_COMPOUND), fluids);
    }

    public void migrateLegacyItems(net.neoforged.neoforge.items.IItemHandler legacy) {
        for (int slot = 0; slot < legacy.getSlots(); slot++) {
            ItemStack stack = legacy.getStackInSlot(slot);
            if (!stack.isEmpty()) addItem(stack, absolute(stack.getCount()));
        }
    }

    public void migrateLegacyFluid(FluidStack stack) {
        if (stack != null && !stack.isEmpty()) {
            addFluid(stack, absolute(stack.getAmount()));
        }
    }

    public void loadLegacyBigNumber(CompoundTag tag) {
        items.clear();
        fluids.clear();
        loadLegacyBigNumberEntries(tag.getList("items", Tag.TAG_COMPOUND), items);
        loadLegacyBigNumberEntries(tag.getList("fluids", Tag.TAG_COMPOUND), fluids);
    }

    private static ListTag saveEntries(Map<ResourceLocation, AbsoluteInteger> entries) {
        ListTag list = new ListTag();
        entries.forEach((id, amount) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", id.toString());
            entry.put("amount", FluxMath8.toCompactTag(amount));
            list.add(entry);
        });
        return list;
    }

    private static void loadEntries(ListTag list, Map<ResourceLocation, AbsoluteInteger> target) {
        for (int index = 0; index < list.size(); index++) {
            CompoundTag entry = list.getCompound(index);
            ResourceLocation id = ResourceLocation.tryParse(entry.getString("id"));
            AbsoluteInteger amount = FluxMath8.fromCompactTag(entry.getCompound("amount"));
            if (id != null && !amount.isZero()) target.put(id, amount);
        }
    }

    private static void loadLegacyBigNumberEntries(ListTag list, Map<ResourceLocation, AbsoluteInteger> target) {
        for (int index = 0; index < list.size(); index++) {
            CompoundTag entry = list.getCompound(index);
            ResourceLocation id = ResourceLocation.tryParse(entry.getString("id"));
            BigNumber amount = BigNumber.fromTag(entry.getCompound("amount"));
            if (id == null || amount.signum() <= 0) continue;
            String scientific = amount.toScientificString();
            if (scientific.endsWith(" FE")) scientific = scientific.substring(0, scientific.length() - 3);
            try {
                target.put(id, AbsoluteInteger.parse(scientific));
            } catch (IllegalArgumentException exception) {
                LOGGER.error("Cannot migrate legacy BigNumber mining amount for {} into AbsoluteInteger: {}. "
                        + "The resource entry was left empty so the block entity can still load.",
                        id, scientific, exception);
            }
        }
    }

    private static AbsoluteInteger absolute(long amount) {
        return AbsoluteInteger.parse(Long.toString(Math.max(0L, amount)));
    }

    private static AbsoluteInteger total(Map<ResourceLocation, AbsoluteInteger> entries) {
        AbsoluteInteger total = new AbsoluteInteger();
        entries.values().forEach(value -> FluxMath8.addInPlace(total, value));
        return total;
    }

    private static AbsoluteInteger amountFor(Map<ResourceLocation, AbsoluteInteger> entries, ResourceLocation id) {
        AbsoluteInteger amount = id == null ? null : entries.get(id);
        return amount == null ? new AbsoluteInteger() : amount.copy();
    }

    private static long quote(Map<ResourceLocation, AbsoluteInteger> entries,
                              ResourceLocation id,
                              long maximum) {
        AbsoluteInteger amount = id == null ? null : entries.get(id);
        return amount == null || maximum <= 0 ? 0 : FluxMath8.quoteChunk(amount, maximum);
    }

    private static long extract(Map<ResourceLocation, AbsoluteInteger> entries,
                                ResourceLocation id,
                                long maximum,
                                boolean simulate) {
        AbsoluteInteger amount = id == null ? null : entries.get(id);
        if (amount == null || maximum <= 0) return 0;
        long extracted = FluxMath8.quoteChunk(amount, maximum);
        if (!simulate && extracted > 0) {
            entries.put(id, FluxMath8.extractChunk(amount, extracted));
            removeEmpty(entries);
        }
        return extracted;
    }

    private static List<ExactEntry> exactEntries(Map<ResourceLocation, AbsoluteInteger> entries) {
        List<ExactEntry> result = new ArrayList<>(entries.size());
        entries.forEach((id, amount) -> result.add(new ExactEntry(id, amount.copy())));
        return result;
    }

    private static String display(AbsoluteInteger amount) {
        return NumberUtils.getScientificInteger(amount);
    }

    private static <T> Map.Entry<ResourceLocation, T> entryAt(Map<ResourceLocation, T> entries, int index) {
        if (index < 0 || index >= entries.size()) return null;
        Iterator<Map.Entry<ResourceLocation, T>> iterator = entries.entrySet().iterator();
        for (int current = 0; current < index; current++) iterator.next();
        return iterator.next();
    }

    private static <T> int indexOf(Map<ResourceLocation, T> entries, ResourceLocation id) {
        int index = 0;
        for (ResourceLocation candidate : entries.keySet()) {
            if (candidate.equals(id)) return index;
            index++;
        }
        return -1;
    }

    private static void removeEmpty(Map<ResourceLocation, AbsoluteInteger> entries) {
        entries.values().removeIf(AbsoluteInteger::isZero);
    }

    public record ItemSnapshot(ResourceLocation id, String amount) {
    }

    public record FluidSnapshot(ResourceLocation id, String amount) {
    }

    public record ExactEntry(ResourceLocation id, AbsoluteInteger amount) {
    }
}

