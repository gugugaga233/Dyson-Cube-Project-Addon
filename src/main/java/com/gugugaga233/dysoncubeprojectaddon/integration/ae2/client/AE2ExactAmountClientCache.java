package com.gugugaga233.dysoncubeprojectaddon.integration.ae2.client;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public final class AE2ExactAmountClientCache {
    private static final Map<Integer, Map<String, ExactAmount>> MENUS = new HashMap<>();

    private AE2ExactAmountClientCache() {
    }

    public static void accept(int containerId, CompoundTag payload) {
        Map<String, ExactAmount> amounts = new HashMap<>();
        ListTag entries = payload.getList("entries", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            String compact = entry.getString("compact");
            String exact = entry.getString("exact");
            amounts.put(entry.getString("kind") + ':' + entry.getString("id"),
                    new ExactAmount(exact.isBlank() ? compact : exact, compact, entry.getString("storage")));
        }
        MENUS.put(containerId, amounts);
    }

    public static ExactAmount get(int containerId, AEKey key) {
        Map<String, ExactAmount> amounts = MENUS.get(containerId);
        return amounts == null ? null : amounts.get(key(key));
    }

    public static void clear(int containerId) {
        MENUS.remove(containerId);
    }

    private static String key(AEKey key) {
        if (key instanceof AEItemKey itemKey) return "item:" + itemKey.getId();
        if (key instanceof AEFluidKey fluidKey) return "fluid:" + fluidKey.getId();
        return "";
    }

    public record ExactAmount(String exact, String compact, String storage) {
    }
}

