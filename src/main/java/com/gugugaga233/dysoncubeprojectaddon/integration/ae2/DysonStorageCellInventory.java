package com.gugugaga233.dysoncubeprojectaddon.integration.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import com.gugugaga233.dysoncubeprojectaddon.DCPAttachments;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8;
import com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;

public final class DysonStorageCellInventory implements StorageCell {
    public static final int MAX_TYPES = 256;
    private static final double IDLE_DRAIN = 32.0;

    private final ItemStack stack;
    private final ISaveProvider saveProvider;
    private final Kind kind;
    private final Map<ResourceLocation, AbsoluteInteger> entries = new LinkedHashMap<>();
    /**
     * Mutations are committed as deltas so a stale AE2 cell view cannot replace newer contents.
     */
    private final List<PendingDelta> pendingDeltas = new ArrayList<>();
    private int batchDepth;
    private boolean batchChanged;
    private long loadedRevision;
    private boolean networkSummary;
    private int summarizedTypeCount;
    private String summarizedTotal = "0";

    public DysonStorageCellInventory(ItemStack stack, ISaveProvider saveProvider) {
        this(stack, saveProvider, Kind.ITEM);
    }

    public DysonStorageCellInventory(ItemStack stack, ISaveProvider saveProvider, Kind kind) {
        this.stack = stack;
        this.saveProvider = saveProvider;
        this.kind = kind;
        load(stack.getOrDefault(DCPAttachments.DYSON_AE_CELL_CONTENTS.get(), new CompoundTag()));
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        MEStorage.checkPreconditions(what, amount, mode, source);
        ResourceLocation id = keyId(what);
        if (id == null || what.hasComponents()) return 0;
        AbsoluteInteger accepted = insertExact(
                id, AbsoluteInteger.parse(Long.toString(amount)), mode == Actionable.SIMULATE);
        return FluxMath8.quoteChunk(accepted, amount);
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        MEStorage.checkPreconditions(what, amount, mode, source);
        ResourceLocation id = keyId(what);
        if (id == null || what.hasComponents()) return 0;
        AbsoluteInteger extracted = extractExact(
                id, AbsoluteInteger.parse(Long.toString(amount)), mode == Actionable.SIMULATE);
        return FluxMath8.quoteChunk(extracted, amount);
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        refreshIfStale();
        entries.forEach((id, amount) -> {
            long visible = FluxMath8.quoteChunk(amount, MiningPortMEStorage.OPERATIONAL_WINDOW);
            if (visible <= 0) return;
            if (kind == Kind.ITEM) {
                Item item = BuiltInRegistries.ITEM.get(id);
                if (item != null) out.add(AEItemKey.of(item), visible);
            } else {
                Fluid fluid = BuiltInRegistries.FLUID.get(id);
                if (fluid != null && fluid != Fluids.EMPTY) out.add(AEFluidKey.of(fluid), visible);
            }
        });
    }

    public Kind kind() {
        return kind;
    }

    public AbsoluteInteger insertExact(ResourceLocation id, AbsoluteInteger amount, boolean simulate) {
        refreshIfStale();
        if (id == null || amount == null || amount.isZero()) return new AbsoluteInteger();
        if (!entries.containsKey(id) && entries.size() >= MAX_TYPES) return new AbsoluteInteger();
        if (!simulate) {
            FluxMath8.addInPlace(entries.computeIfAbsent(id, ignored -> new AbsoluteInteger()), amount);
            pendingDeltas.add(new PendingDelta(id, amount.copy(), true));
            persist();
        }
        return amount.copy();
    }

    public AbsoluteInteger extractExact(ResourceLocation id, AbsoluteInteger maximum, boolean simulate) {
        refreshIfStale();
        AbsoluteInteger stored = id == null ? null : entries.get(id);
        if (stored == null || maximum == null || maximum.isZero()) return new AbsoluteInteger();
        AbsoluteInteger extracted = stored.compareTo(maximum) <= 0 ? stored.copy() : maximum.copy();
        if (!simulate) {
            AbsoluteInteger remaining = FluxMath8.subtract(stored, extracted);
            if (remaining.isZero()) entries.remove(id);
            else entries.put(id, remaining);
            pendingDeltas.add(new PendingDelta(id, extracted.copy(), false));
            persist();
        }
        return extracted;
    }

    public List<ExactEntry> exactEntries() {
        refreshIfStale();
        List<ExactEntry> result = new ArrayList<>(entries.size());
        entries.forEach((id, amount) -> result.add(new ExactEntry(id, amount.copy())));
        return result;
    }

    public int getStoredTypeCount() {
        refreshIfStale();
        return networkSummary ? summarizedTypeCount : entries.size();
    }

    public AbsoluteInteger getTotalAmount() {
        refreshIfStale();
        AbsoluteInteger total = new AbsoluteInteger();
        entries.values().forEach(amount -> FluxMath8.addInPlace(total, amount));
        return total;
    }

    public String getTotalDisplay() {
        return networkSummary ? summarizedTotal : NumberUtils.getScientificInteger(getTotalAmount());
    }

    @Override
    public CellState getStatus() {
        int typeCount = getStoredTypeCount();
        if (typeCount == 0) return CellState.EMPTY;
        return typeCount >= MAX_TYPES ? CellState.TYPES_FULL : CellState.NOT_EMPTY;
    }

    @Override
    public double getIdleDrain() {
        return IDLE_DRAIN;
    }

    @Override
    public boolean canFitInsideCell() {
        return false;
    }

    @Override
    public void persist() {
        if (batchDepth > 0) {
            batchChanged = true;
            return;
        }

        refreshIfStale();
        if (!pendingDeltas.isEmpty()) {
            Map<ResourceLocation, AbsoluteInteger> current = new LinkedHashMap<>();
            CompoundTag stored = stack.getOrDefault(
                    DCPAttachments.DYSON_AE_CELL_CONTENTS.get(), new CompoundTag());
            if (stored.getBoolean("networkSummary")) {
                // A summary is client-side display data. Entries already include these deltas.
                entries.forEach((id, amount) -> current.put(id, amount.copy()));
            } else {
                loadEntries(stored, current);
                applyDeltas(current, pendingDeltas);
                loadedRevision = stored.getLong("revision") + 1L;
            }
            entries.clear();
            entries.putAll(current);
            networkSummary = false;
            summarizedTypeCount = 0;
            summarizedTotal = "0";
            pendingDeltas.clear();
        }

        stack.set(DCPAttachments.DYSON_AE_CELL_CONTENTS.get(), save());
        if (saveProvider != null) saveProvider.saveChanges();
    }

    void beginBatch() {
        batchDepth++;
    }

    void endBatch() {
        if (batchDepth <= 0) throw new IllegalStateException("Dyson cell batch is not active");
        batchDepth--;
        if (batchDepth == 0 && batchChanged) {
            batchChanged = false;
            persist();
        }
    }

    @Override
    public Component getDescription() {
        return Component.translatable(kind == Kind.ITEM
                ? "item.dysoncubeproject.dyson_storage_cell"
                : "item.dysoncubeproject.dyson_fluid_storage_cell");
    }

    private CompoundTag save() {
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        AbsoluteInteger total = new AbsoluteInteger();
        entries.forEach((id, amount) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", id.toString());
            entry.put("amount", FluxMath8.toCompactTag(amount));
            list.add(entry);
            FluxMath8.addInPlace(total, amount);
        });
        root.put("entries", list);
        root.putString("kind", kind.id);
        root.putInt("typeCount", entries.size());
        root.putString("totalDisplay", NumberUtils.getScientificInteger(total));
        root.putLong("revision", loadedRevision);
        return root;
    }

    private void load(CompoundTag root) {
        loadedRevision = root.getLong("revision");
        networkSummary = root.getBoolean("networkSummary");
        summarizedTypeCount = Math.max(0, root.getInt("typeCount"));
        String totalDisplay = root.getString("totalDisplay");
        summarizedTotal = totalDisplay.isEmpty() ? "0" : totalDisplay;
        loadEntries(root, entries);
    }

    private static void loadEntries(CompoundTag root, Map<ResourceLocation, AbsoluteInteger> target) {
        ListTag list = root.getList("entries", Tag.TAG_COMPOUND);
        for (int index = 0; index < list.size(); index++) {
            CompoundTag entry = list.getCompound(index);
            ResourceLocation id = ResourceLocation.tryParse(entry.getString("id"));
            AbsoluteInteger amount = FluxMath8.fromCompactTag(entry.getCompound("amount"));
            if (id != null && !amount.isZero()) {
                FluxMath8.addInPlace(target.computeIfAbsent(id, ignored -> new AbsoluteInteger()), amount);
            }
        }
    }

    private void refreshIfStale() {
        CompoundTag stored = stack.getOrDefault(
                DCPAttachments.DYSON_AE_CELL_CONTENTS.get(), new CompoundTag());
        if (stored.getBoolean("networkSummary") || stored.getLong("revision") == loadedRevision) return;

        Map<ResourceLocation, AbsoluteInteger> current = new LinkedHashMap<>();
        loadEntries(stored, current);
        applyDeltas(current, pendingDeltas);
        entries.clear();
        entries.putAll(current);
        loadedRevision = stored.getLong("revision");
        networkSummary = false;
        summarizedTypeCount = 0;
        summarizedTotal = "0";
    }

    private static void applyDeltas(Map<ResourceLocation, AbsoluteInteger> target,
                                    List<PendingDelta> deltas) {
        for (PendingDelta delta : deltas) {
            if (delta.add()) {
                // Never discard an accepted transfer. The regular admission check still caps new types.
                FluxMath8.addInPlace(
                        target.computeIfAbsent(delta.id(), ignored -> new AbsoluteInteger()),
                        delta.amount());
            } else {
                AbsoluteInteger storedAmount = target.get(delta.id());
                if (storedAmount == null) continue;
                if (storedAmount.compareTo(delta.amount()) <= 0) target.remove(delta.id());
                else target.put(delta.id(), FluxMath8.subtract(storedAmount, delta.amount()));
            }
        }
    }

    private ResourceLocation keyId(AEKey key) {
        if (kind == Kind.ITEM && key instanceof AEItemKey itemKey) return itemKey.getId();
        if (kind == Kind.FLUID && key instanceof AEFluidKey fluidKey) return fluidKey.getId();
        return null;
    }

    public enum Kind {
        ITEM("item"),
        FLUID("fluid");

        private final String id;

        Kind(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

    private record PendingDelta(ResourceLocation id, AbsoluteInteger amount, boolean add) {
    }

    public record ExactEntry(ResourceLocation id, AbsoluteInteger amount) {
    }
}
