package com.gugugaga233.dysoncubeprojectaddon.integration.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.cells.StorageCell;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.parts.storagebus.StorageBusPart;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.CosmicMiningHubBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.MiningFluidOutputPortBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.MiningItemOutputPortBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.OreProcessingHubBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.AbsoluteMiningInventory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;

final class DysonExactNetworkTransfer {
    private static final int MAX_NORMAL_KEYS_PER_TICK = DysonStorageCellInventory.MAX_TYPES;
    private static final Runnable NOOP = () -> {
    };

    private DysonExactNetworkTransfer() {
    }

    static boolean fillCellFromNetwork(IGrid grid, DysonStorageCellInventory destination,
                                       IEnergyService energy, IActionSource actionSource) {
        boolean changed = false;
        destination.beginBatch();
        try {
            for (ExactSource source : findSources(grid, destination.kind(), destination)) {
                boolean sourceChanged = false;
                source.beginBatch().run();
                try {
                    for (ExactStack entry : source.entries()) {
                        AbsoluteInteger accepted = destination.insertExact(entry.id(), entry.amount(), true);
                        if (accepted.isZero()) continue;
                        AbsoluteInteger extracted = source.extract(entry.id(), accepted);
                        if (extracted.isZero()) continue;
                        destination.insertExact(entry.id(), extracted, false);
                        sourceChanged = true;
                        changed = true;
                    }
                    if (sourceChanged) source.changed().run();
                } finally {
                    source.endBatch().run();
                }
            }
            changed |= fillFromNormalNetwork(grid, destination, energy, actionSource);
        } finally {
            destination.endBatch();
        }
        if (changed) grid.getStorageService().invalidateCache();
        return changed;
    }

    static boolean emptyCellToNetwork(IGrid grid, DysonStorageCellInventory source,
                                      IEnergyService energy, IActionSource actionSource) {
        List<DysonStorageCellInventory> destinations = findDysonDriveCells(
                grid, source.kind(), source);

        boolean changed = false;
        source.beginBatch();
        destinations.forEach(DysonStorageCellInventory::beginBatch);
        try {
            for (DysonStorageCellInventory.ExactEntry entry : source.exactEntries()) {
                AbsoluteInteger remaining = entry.amount();
                for (DysonStorageCellInventory destination : destinations) {
                    AbsoluteInteger accepted = destination.insertExact(entry.id(), remaining, true);
                    if (accepted.isZero()) continue;
                    AbsoluteInteger extracted = source.extractExact(entry.id(), accepted, false);
                    if (extracted.isZero()) break;
                    destination.insertExact(entry.id(), extracted, false);
                    remaining = com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8.subtract(
                            remaining, extracted);
                    changed = true;
                    if (remaining.isZero()) break;
                }
            }
            changed |= emptyToNormalNetwork(grid, source, energy, actionSource);
        } finally {
            destinations.forEach(DysonStorageCellInventory::endBatch);
            source.endBatch();
        }
        if (changed) grid.getStorageService().invalidateCache();
        return changed;
    }

    private static boolean fillFromNormalNetwork(IGrid grid,
                                                 DysonStorageCellInventory destination,
                                                 IEnergyService energy,
                                                 IActionSource actionSource) {
        MEStorage network = grid.getStorageService().getInventory();
        KeyCounter cached = grid.getStorageService().getCachedInventory();
        int keysRemaining = MAX_NORMAL_KEYS_PER_TICK;
        boolean changed = false;

        for (Object2LongMap.Entry<AEKey> entry : cached) {
            if (keysRemaining-- <= 0) break;
            AEKey key = entry.getKey();
            ResourceLocation id = matchingId(key, destination.kind());
            long available = entry.getLongValue();
            if (id == null || available <= 0) continue;
            if (destination.insertExact(id, absolute(available), true).isZero()) continue;

            long extracted = StorageHelper.poweredExtraction(
                    energy, network, key, available, actionSource);
            if (extracted <= 0) continue;
            destination.insertExact(id, absolute(extracted), false);
            changed = true;
        }
        return changed;
    }

    private static boolean emptyToNormalNetwork(IGrid grid,
                                                DysonStorageCellInventory source,
                                                IEnergyService energy,
                                                IActionSource actionSource) {
        MEStorage network = grid.getStorageService().getInventory();
        int keysRemaining = MAX_NORMAL_KEYS_PER_TICK;
        boolean changed = false;

        for (DysonStorageCellInventory.ExactEntry entry : source.exactEntries()) {
            if (keysRemaining-- <= 0) break;
            AEKey key = keyFor(entry.id(), source.kind());
            if (key == null) continue;

            AbsoluteInteger available = source.extractExact(entry.id(), entry.amount(), true);
            long requested = FluxMath8.quoteChunk(available, MiningPortMEStorage.OPERATIONAL_WINDOW);
            if (requested <= 0) continue;
            long accepted = network.insert(key, requested, Actionable.SIMULATE, actionSource);
            if (accepted <= 0) continue;

            AbsoluteInteger extractedExact = source.extractExact(entry.id(), absolute(accepted), false);
            long extracted = FluxMath8.quoteChunk(extractedExact, accepted);
            if (extracted <= 0) continue;
            long inserted = StorageHelper.poweredInsert(
                    energy, network, key, extracted, actionSource);
            if (inserted < extracted) {
                source.insertExact(entry.id(), absolute(extracted - inserted), false);
            }
            changed |= inserted > 0;
        }
        return changed;
    }

    private static ResourceLocation matchingId(AEKey key, DysonStorageCellInventory.Kind kind) {
        if (key == null || key.hasComponents()) return null;
        if (kind == DysonStorageCellInventory.Kind.ITEM && key instanceof AEItemKey itemKey) {
            return itemKey.getId();
        }
        if (kind == DysonStorageCellInventory.Kind.FLUID && key instanceof AEFluidKey fluidKey) {
            return fluidKey.getId();
        }
        return null;
    }

    private static AEKey keyFor(ResourceLocation id, DysonStorageCellInventory.Kind kind) {
        if (kind == DysonStorageCellInventory.Kind.ITEM) {
            Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
            return item == null ? null : AEItemKey.of(item);
        }
        Fluid fluid = BuiltInRegistries.FLUID.getOptional(id).orElse(null);
        return fluid == null || fluid == Fluids.EMPTY ? null : AEFluidKey.of(fluid);
    }

    private static AbsoluteInteger absolute(long value) {
        return AbsoluteInteger.parse(Long.toString(Math.max(0L, value)));
    }

    private static List<ExactSource> findSources(IGrid grid,
                                                  DysonStorageCellInventory.Kind kind,
                                                  DysonStorageCellInventory excluded) {
        List<ExactSource> result = new ArrayList<>();
        Set<AbsoluteMiningInventory> seenLedgers = Collections.newSetFromMap(new IdentityHashMap<>());

        for (StorageBusPart bus : grid.getActiveMachines(StorageBusPart.class)) {
            BlockEntity busBlock = bus.getBlockEntity();
            if (busBlock == null || bus.getSide() == null || busBlock.getLevel() == null) continue;
            BlockEntity target = busBlock.getLevel().getBlockEntity(
                    busBlock.getBlockPos().relative(bus.getSide()));
            if (kind == DysonStorageCellInventory.Kind.ITEM
                    && target instanceof MiningItemOutputPortBlockEntity port) {
                CosmicMiningHubBlockEntity hub = port.findHub();
                if (hub != null) addLedgerSource(result, seenLedgers,
                        hub.getAbsoluteMiningInventory(), kind, hub::markExternalOutputChanged);
            } else if (kind == DysonStorageCellInventory.Kind.FLUID
                    && target instanceof MiningFluidOutputPortBlockEntity port) {
                CosmicMiningHubBlockEntity hub = port.findHub();
                if (hub != null) addLedgerSource(result, seenLedgers,
                        hub.getAbsoluteMiningInventory(), kind, hub::markExternalOutputChanged);
            } else if (kind == DysonStorageCellInventory.Kind.ITEM
                    && target instanceof OreProcessingHubBlockEntity hub) {
                addLedgerSource(result, seenLedgers, hub.getProductInventory(), kind,
                        hub::markExternalOutputChanged);
            }
        }

        for (DysonStorageCellInventory cell : findDysonDriveCells(grid, kind, excluded)) {
            result.add(new ExactSource(
                    () -> cell.exactEntries().stream()
                            .map(entry -> new ExactStack(entry.id(), entry.amount())).toList(),
                    cell::extractExact,
                    cell::beginBatch, NOOP, cell::endBatch));
        }
        return result;
    }

    private static void addLedgerSource(List<ExactSource> result,
                                        Set<AbsoluteMiningInventory> seen,
                                        AbsoluteMiningInventory ledger,
                                        DysonStorageCellInventory.Kind kind,
                                        Runnable changed) {
        if (!seen.add(ledger)) return;
        if (kind == DysonStorageCellInventory.Kind.ITEM) {
            result.add(new ExactSource(
                    () -> ledger.itemEntries().stream()
                            .map(entry -> new ExactStack(entry.id(), entry.amount())).toList(),
                    ledger::extractItemExact,
                    NOOP, changed, NOOP));
        } else {
            result.add(new ExactSource(
                    () -> ledger.fluidEntries().stream()
                            .map(entry -> new ExactStack(entry.id(), entry.amount())).toList(),
                    ledger::extractFluidExact,
                    NOOP, changed, NOOP));
        }
    }

    private static List<DysonStorageCellInventory> findDysonDriveCells(
            IGrid grid,
            DysonStorageCellInventory.Kind kind,
            DysonStorageCellInventory excluded) {
        List<DysonStorageCellInventory> result = new ArrayList<>();
        Set<StorageCell> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (DriveBlockEntity drive : grid.getActiveMachines(DriveBlockEntity.class)) {
            for (int slot = 0; slot < drive.getCellCount(); slot++) {
                StorageCell cell = drive.getOriginalCellInventory(slot);
                if (cell instanceof DysonStorageCellInventory dysonCell
                        && dysonCell != excluded
                        && dysonCell.kind() == kind
                        && seen.add(cell)) {
                    result.add(dysonCell);
                }
            }
        }
        return result;
    }

    private record ExactStack(ResourceLocation id, AbsoluteInteger amount) {
    }

    @FunctionalInterface
    private interface Entries {
        List<ExactStack> get();
    }

    @FunctionalInterface
    private interface Extractor {
        AbsoluteInteger extract(ResourceLocation id, AbsoluteInteger maximum, boolean simulate);
    }

    private record ExactSource(Entries entriesSupplier, Extractor extractor,
                               Runnable beginBatch, Runnable changed, Runnable endBatch) {
        List<ExactStack> entries() {
            return entriesSupplier.get();
        }

        AbsoluteInteger extract(ResourceLocation id, AbsoluteInteger maximum) {
            return extractor.extract(id, maximum, false);
        }
    }
}
