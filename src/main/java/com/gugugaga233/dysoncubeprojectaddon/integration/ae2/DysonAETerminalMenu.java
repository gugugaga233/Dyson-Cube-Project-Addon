package com.gugugaga233.dysoncubeprojectaddon.integration.ae2;

import appeng.menu.me.common.MEStorageMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public final class DysonAETerminalMenu extends MEStorageMenu {
    private String exactAmountSignature = "";

    public DysonAETerminalMenu(MenuType<DysonAETerminalMenu> menuType,
                               int containerId,
                               Inventory playerInventory,
                               DysonAETerminalPart host) {
        super(menuType, containerId, playerInventory, host);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (getPlayer().tickCount % 10 == 0) {
            exactAmountSignature = AE2ExactAmountSync.synchronize(this, exactAmountSignature);
        }
    }
}

