package com.gugugaga233.dysoncubeprojectaddon.client;

import com.gugugaga233.dysoncubeprojectaddon.network.ClientPacketDispatcher;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientboundOpenCosmicMiningHubPacket;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientboundOpenDysonHubPacket;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientboundOpenOreProcessingHubPacket;
import com.gugugaga233.dysoncubeprojectaddon.network.DysonSphereSyncMessage;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.StarData;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.client.gui.CosmicHeartRewardScreen;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.client.gui.CosmicMiningHubScreen;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.client.gui.DysonHubScreen;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.client.gui.OreProcessingHubScreen;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.client.gui.PrimordialQiChoiceScreen;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.client.render.StarTextureManager;
import com.gugugaga233.dysoncubeprojectaddon.world.ClientDysonSphere;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereProgressSavedData;
import net.minecraft.client.Minecraft;

/** Client-only implementations for common clientbound packet payloads. */
public final class ClientPacketHandlers {
    private ClientPacketHandlers() {
    }

    public static void register() {
        ClientPacketDispatcher.setDysonHubHandler(ClientPacketHandlers::openDysonHub);
        ClientPacketDispatcher.setMiningHubHandler(ClientPacketHandlers::openMiningHub);
        ClientPacketDispatcher.setProcessingHubHandler(ClientPacketHandlers::openProcessingHub);
        ClientPacketDispatcher.setCosmicHeartRewardHandler(
                packet -> Minecraft.getInstance().setScreen(
                        new CosmicHeartRewardScreen(packet.appliedCounts, packet.requiredChoices)));
        ClientPacketDispatcher.setPrimordialQiChoiceHandler(packet ->
                Minecraft.getInstance().setScreen(new PrimordialQiChoiceScreen(packet.qiCount)));
        ClientPacketDispatcher.setDysonSphereSyncHandler(ClientPacketHandlers::syncDysonSphere);
    }

    private static void openDysonHub(ClientboundOpenDysonHubPacket packet) {
        StarData star = StarData.deserializeFromString(packet.serializedStar);
        DysonHubScreen screen = new DysonHubScreen();
        screen.updateData(
                star,
                packet.totalPower,
                packet.totalWrappedExact,
                packet.hierarchyLevel,
                packet.hierarchyBatchDisplay,
                packet.hierarchyNextThresholdDisplay,
                packet.hierarchyBatchCapped,
                packet.universeAwakened,
                packet.darkMatterResonanceExact,
                packet.cosmicHeartActive,
                packet.cosmicHeartProgress,
                packet.cosmicHeartComplete,
                packet.cosmicHeartBatchDisplay,
                packet.queuedCosmicHeartDisplay,
                packet.cosmicHeartBeamDisplay,
                packet.cosmicHeartSailDisplay,
                packet.powerDisplay,
                packet.storedEnergyDisplay,
                packet.powerCalculation,
                packet.storedEnergyCalculation,
                StarTextureManager.getTexture(star)
        );
        Minecraft.getInstance().setScreen(screen);
    }

    private static void openMiningHub(ClientboundOpenCosmicMiningHubPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof CosmicMiningHubScreen screen
                && screen.getBlockPos() == packet.blockPos) {
            screen.updateData(packet);
            return;
        }
        CosmicMiningHubScreen screen = new CosmicMiningHubScreen(packet.blockPos);
        screen.updateData(packet);
        minecraft.setScreen(screen);
    }

    private static void openProcessingHub(ClientboundOpenOreProcessingHubPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof OreProcessingHubScreen screen
                && screen.getBlockPos() == packet.blockPos) {
            screen.updateData(packet);
            return;
        }
        OreProcessingHubScreen screen = new OreProcessingHubScreen(packet.blockPos);
        screen.updateData(packet);
        minecraft.setScreen(screen);
    }

    private static void syncDysonSphere(DysonSphereSyncMessage packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        ClientDysonSphere.DYSON_SPHERE_PROGRESS = DysonSphereProgressSavedData.load(
                minecraft.level.registryAccess(), packet.tag);
    }
}

