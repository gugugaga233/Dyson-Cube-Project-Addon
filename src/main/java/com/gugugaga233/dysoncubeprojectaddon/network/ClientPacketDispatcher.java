package com.gugugaga233.dysoncubeprojectaddon.network;

import java.util.function.Consumer;

/**
 * Common-side bridge for clientbound packets. Client-only handlers are
 * installed during client setup, keeping Screen and Minecraft classes out of
 * packet bytecode that is loaded by the logical server.
 */
public final class ClientPacketDispatcher {
    private static final Consumer<Object> NO_OP = ignored -> { };

    private static Consumer<ClientboundOpenDysonHubPacket> dysonHub = noOp();
    private static Consumer<ClientboundOpenCosmicMiningHubPacket> miningHub = noOp();
    private static Consumer<ClientboundOpenOreProcessingHubPacket> processingHub = noOp();
    private static Consumer<ClientboundCosmicHeartRewardPacket> cosmicHeartReward = noOp();
    private static Consumer<ClientboundPrimordialQiChoicePacket> primordialQiChoice = noOp();
    private static Consumer<DysonSphereSyncMessage> dysonSphereSync = noOp();
    private static Consumer<ClientboundAE2ExactAmountsPacket> ae2ExactAmounts = noOp();

    private ClientPacketDispatcher() {
    }

    public static void setDysonHubHandler(Consumer<ClientboundOpenDysonHubPacket> handler) {
        dysonHub = handler;
    }

    public static void setMiningHubHandler(Consumer<ClientboundOpenCosmicMiningHubPacket> handler) {
        miningHub = handler;
    }

    public static void setProcessingHubHandler(Consumer<ClientboundOpenOreProcessingHubPacket> handler) {
        processingHub = handler;
    }

    public static void setCosmicHeartRewardHandler(Consumer<ClientboundCosmicHeartRewardPacket> handler) {
        cosmicHeartReward = handler;
    }

    public static void setPrimordialQiChoiceHandler(Consumer<ClientboundPrimordialQiChoicePacket> handler) {
        primordialQiChoice = handler;
    }

    public static void setDysonSphereSyncHandler(Consumer<DysonSphereSyncMessage> handler) {
        dysonSphereSync = handler;
    }

    public static void setAE2ExactAmountsHandler(Consumer<ClientboundAE2ExactAmountsPacket> handler) {
        ae2ExactAmounts = handler;
    }

    public static void handle(ClientboundOpenDysonHubPacket packet) {
        dysonHub.accept(packet);
    }

    public static void handle(ClientboundOpenCosmicMiningHubPacket packet) {
        miningHub.accept(packet);
    }

    public static void handle(ClientboundOpenOreProcessingHubPacket packet) {
        processingHub.accept(packet);
    }

    public static void handle(ClientboundCosmicHeartRewardPacket packet) {
        cosmicHeartReward.accept(packet);
    }

    public static void handle(ClientboundPrimordialQiChoicePacket packet) {
        primordialQiChoice.accept(packet);
    }

    public static void handle(DysonSphereSyncMessage packet) {
        dysonSphereSync.accept(packet);
    }

    public static void handle(ClientboundAE2ExactAmountsPacket packet) {
        ae2ExactAmounts.accept(packet);
    }

    @SuppressWarnings("unchecked")
    private static <T> Consumer<T> noOp() {
        return (Consumer<T>) (Consumer<?>) NO_OP;
    }
}

