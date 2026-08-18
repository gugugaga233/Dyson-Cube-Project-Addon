package com.gugugaga233.dysoncubeprojectaddon.integration.ae2;

import appeng.api.networking.IGridNode;
import appeng.api.storage.cells.StorageCell;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.parts.storagebus.StorageBusPart;
import com.gugugaga233.dysoncubeprojectaddon.DysonCubeProject;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientboundAE2ExactAmountsPacket;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.CosmicMiningHubBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.MiningFluidOutputPortBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.MiningItemOutputPortBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.OreProcessingHubBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.AbsoluteMiningInventory;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8;
import com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;

public final class AE2ExactAmountSync {
    private AE2ExactAmountSync() {
    }

    public static String synchronize(DysonAETerminalMenu menu, String previousSignature) {
        if (!(menu.getPlayer() instanceof ServerPlayer player)) return previousSignature;

        Map<ExactKey, AbsoluteInteger> totals = new HashMap<>();
        Set<CosmicMiningHubBlockEntity> itemHubs = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        Set<CosmicMiningHubBlockEntity> fluidHubs = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        Set<OreProcessingHubBlockEntity> processingHubs =
                java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        Set<StorageCell> dysonCells = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        IGridNode terminalNode = menu.getGridNode();
        if (terminalNode != null && terminalNode.getGrid() != null) {
            for (StorageBusPart bus : terminalNode.getGrid().getActiveMachines(StorageBusPart.class)) {
                BlockEntity busBlock = bus.getBlockEntity();
                if (busBlock == null || bus.getSide() == null || busBlock.getLevel() == null) continue;
                BlockEntity target = busBlock.getLevel().getBlockEntity(
                        busBlock.getBlockPos().relative(bus.getSide()));
                if (target instanceof MiningItemOutputPortBlockEntity port) {
                    CosmicMiningHubBlockEntity hub = port.findHub();
                    if (hub != null && itemHubs.add(hub)) addEntries(totals, "item", hub.getAbsoluteMiningInventory().itemEntries());
                } else if (target instanceof MiningFluidOutputPortBlockEntity port) {
                    CosmicMiningHubBlockEntity hub = port.findHub();
                    if (hub != null && fluidHubs.add(hub)) addEntries(totals, "fluid", hub.getAbsoluteMiningInventory().fluidEntries());
                } else if (target instanceof OreProcessingHubBlockEntity hub && processingHubs.add(hub)) {
                    addEntries(totals, "item", hub.getProductInventory().itemEntries());
                }
            }
            for (DriveBlockEntity drive : terminalNode.getGrid().getActiveMachines(DriveBlockEntity.class)) {
                for (int slot = 0; slot < drive.getCellCount(); slot++) {
                    StorageCell cell = drive.getOriginalCellInventory(slot);
                    if (cell instanceof DysonStorageCellInventory dysonCell && dysonCells.add(cell)) {
                        for (DysonStorageCellInventory.ExactEntry entry : dysonCell.exactEntries()) {
                            addEntry(totals, dysonCell.kind().id(),
                                    entry.id().toString(), entry.amount());
                        }
                    }
                }
            }
        }

        String signature = signature(totals);
        if (signature.equals(previousSignature)) return previousSignature;

        ListTag entries = new ListTag();
        totals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    CompoundTag tag = new CompoundTag();
                    tag.putString("kind", entry.getKey().kind());
                    tag.putString("id", entry.getKey().id());
                    String exact = NumberUtils.getScientificInteger(entry.getValue());
                    tag.putString("exact", exact);
                    tag.putString("compact", NumberUtils.getAeCompactAmount(entry.getValue()));
                    tag.putString("storage", NumberUtils.getCompactAbsoluteStorageCalculation(
                            entry.getValue().toCalculationString()));
                    entries.add(tag);
                });
        CompoundTag payload = new CompoundTag();
        payload.put("entries", entries);
        DysonCubeProject.NETWORK.sendTo(
                new ClientboundAE2ExactAmountsPacket(menu.containerId, payload), player);
        return signature;
    }

    private static void addEntries(Map<ExactKey, AbsoluteInteger> totals,
                                   String kind,
                                   Iterable<AbsoluteMiningInventory.ExactEntry> entries) {
        for (AbsoluteMiningInventory.ExactEntry entry : entries) {
            addEntry(totals, kind, entry.id().toString(), entry.amount());
        }
    }

    private static void addEntry(Map<ExactKey, AbsoluteInteger> totals,
                                 String kind,
                                 String id,
                                 AbsoluteInteger amount) {
        ExactKey key = new ExactKey(kind, id);
        FluxMath8.addInPlace(totals.computeIfAbsent(key, ignored -> new AbsoluteInteger()), amount);
    }

    private static String signature(Map<ExactKey, AbsoluteInteger> totals) {
        StringBuilder result = new StringBuilder();
        totals.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> result
                .append(entry.getKey().kind()).append(':').append(entry.getKey().id()).append('=')
                .append(entry.getValue().toCalculationString()).append(';'));
        return result.toString();
    }

    private record ExactKey(String kind, String id) implements Comparable<ExactKey> {
        @Override
        public int compareTo(ExactKey other) {
            int byKind = kind.compareTo(other.kind);
            return byKind != 0 ? byKind : id.compareTo(other.id);
        }
    }
}

