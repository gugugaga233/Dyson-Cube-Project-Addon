package com.gugugaga233.dysoncubeprojectaddon.integration.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.OreProcessingHubBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.AbsoluteMiningInventory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

final class OreProcessingMEStorage implements MEStorage {
    private final OreProcessingHubBlockEntity hub;

    OreProcessingMEStorage(OreProcessingHubBlockEntity hub) {
        this.hub = hub;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        MEStorage.checkPreconditions(what, amount, mode, source);
        return 0;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        MEStorage.checkPreconditions(what, amount, mode, source);
        if (!(what instanceof AEItemKey itemKey) || what.hasComponents()) return 0;
        long extracted = hub.getProductInventory().extractItem(
                itemKey.getId(), amount, mode == Actionable.SIMULATE);
        if (mode == Actionable.MODULATE && extracted > 0) hub.markExternalOutputChanged();
        return extracted;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (AbsoluteMiningInventory.ExactEntry entry : hub.getProductInventory().itemEntries()) {
            long visible = MiningPortMEStorage.quote(entry.amount());
            if (visible <= 0) continue;
            Item item = BuiltInRegistries.ITEM.get(entry.id());
            if (item != null) out.add(AEItemKey.of(item), visible);
        }
    }

    @Override
    public Component getDescription() {
        return Component.translatable("block.dysoncubeproject.ore_processing_hub");
    }
}

