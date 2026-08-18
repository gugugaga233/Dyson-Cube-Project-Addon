/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hrznstudio.titanium.tab.TitaniumTab
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 */
package com.gugugaga233.dysoncubeprojectaddon.item;

import com.gugugaga233.dysoncubeprojectaddon.DCPAttachments;
import com.hrznstudio.titanium.tab.TitaniumTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DysonComponentItem
extends Item {
    public DysonComponentItem(int solarSail, int beam, TitaniumTab tab) {
        super(new Item.Properties()
                .component(DCPAttachments.SOLAR_SAIL.get(), solarSail)
                .component(DCPAttachments.BEAM.get(), beam));
        tab.getTabList().add(this);
    }

    public void verifyComponentsAfterLoad(ItemStack stack) {
        super.verifyComponentsAfterLoad(stack);
    }
}


