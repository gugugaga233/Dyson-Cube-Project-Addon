package com.gugugaga233.dysoncubeprojectaddon.integration.ae2.client;

import appeng.init.client.InitScreens;
import com.gugugaga233.dysoncubeprojectaddon.integration.ae2.AE2Integration;
import com.gugugaga233.dysoncubeprojectaddon.integration.ae2.DysonAETerminalMenu;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientPacketDispatcher;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientboundAE2ExactAmountsPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class AE2ClientIntegration {
    private AE2ClientIntegration() {
    }

    public static void register(IEventBus modEventBus) {
        ClientPacketDispatcher.setAE2ExactAmountsHandler(AE2ClientIntegration::handleExactAmounts);
        modEventBus.addListener(AE2ClientIntegration::registerScreens);
    }

    private static void handleExactAmounts(ClientboundAE2ExactAmountsPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null
                && minecraft.player.containerMenu instanceof DysonAETerminalMenu menu
                && menu.containerId == packet.containerId) {
            AE2ExactAmountClientCache.accept(packet.containerId, packet.payload);
        }
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        InitScreens.register(event, AE2Integration.DYSON_AE_TERMINAL_MENU.get(),
                DysonAETerminalScreen::new, "/screens/terminals/terminal.json");
    }
}

