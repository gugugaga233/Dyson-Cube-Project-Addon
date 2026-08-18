package com.gugugaga233.dysoncubeprojectaddon.integration.ae2;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity.CosmicMiningHubBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.AbsoluteMiningInventory;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;

final class MiningPortMEStorage implements MEStorage {
    // Leave headroom for AE2 to aggregate this window with ordinary cells without overflowing long.
    static final long OPERATIONAL_WINDOW = Long.MAX_VALUE / 4;

    enum Kind {
        ITEM,
        FLUID
    }

    private final Supplier<CosmicMiningHubBlockEntity> hubSupplier;
    private final Kind kind;

    MiningPortMEStorage(Supplier<CosmicMiningHubBlockEntity> hubSupplier, Kind kind) {
        this.hubSupplier = hubSupplier;
        this.kind = kind;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        MEStorage.checkPreconditions(what, amount, mode, source);
        return 0;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        MEStorage.checkPreconditions(what, amount, mode, source);
        CosmicMiningHubBlockEntity hub = hubSupplier.get();
        if (hub == null || what.hasComponents()) return 0;

        AbsoluteMiningInventory inventory = hub.getAbsoluteMiningInventory();
        ResourceLocation id;
        long extracted;
        if (kind == Kind.ITEM && what instanceof AEItemKey itemKey) {
            id = itemKey.getId();
            extracted = inventory.extractItem(id, amount, mode == Actionable.SIMULATE);
        } else if (kind == Kind.FLUID && what instanceof AEFluidKey fluidKey) {
            id = fluidKey.getId();
            extracted = inventory.extractFluid(id, amount, mode == Actionable.SIMULATE);
        } else {
            return 0;
        }

        if (mode == Actionable.MODULATE && extracted > 0) hub.markExternalOutputChanged();
        return extracted;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        CosmicMiningHubBlockEntity hub = hubSupplier.get();
        if (hub == null) return;
        AbsoluteMiningInventory inventory = hub.getAbsoluteMiningInventory();
        for (AbsoluteMiningInventory.ExactEntry entry
                : kind == Kind.ITEM ? inventory.itemEntries() : inventory.fluidEntries()) {
            long visible = quote(entry.amount());
            if (visible <= 0) continue;
            if (kind == Kind.ITEM) {
                Item item = BuiltInRegistries.ITEM.get(entry.id());
                if (item != null) out.add(AEItemKey.of(item), visible);
            } else {
                Fluid fluid = BuiltInRegistries.FLUID.get(entry.id());
                if (fluid != null && fluid != Fluids.EMPTY) out.add(AEFluidKey.of(fluid), visible);
            }
        }
    }

    static long quote(AbsoluteInteger amount) {
        return amount == null ? 0 : FluxMath8.quoteChunk(amount, OPERATIONAL_WINDOW);
    }

    @Override
    public Component getDescription() {
        return Component.translatable(kind == Kind.ITEM
                ? "block.dysoncubeproject.mining_item_output_port"
                : "block.dysoncubeproject.mining_fluid_output_port");
    }
}

