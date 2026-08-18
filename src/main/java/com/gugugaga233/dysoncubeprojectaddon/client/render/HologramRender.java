/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.shaders.Uniform
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.ShaderInstance
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.event.RenderHighlightEvent$Block
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector4f
 */
package com.gugugaga233.dysoncubeprojectaddon.client.render;

import com.gugugaga233.dysoncubeprojectaddon.block.DefaultMultiblockControllerBlock;
import com.gugugaga233.dysoncubeprojectaddon.client.DCPRenderTypes;
import com.gugugaga233.dysoncubeprojectaddon.client.DCPShaders;
import com.gugugaga233.dysoncubeprojectaddon.multiblock.MultiblockStructure;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

public class HologramRender {
    public static void blockOverlayEvent(RenderHighlightEvent.Block event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        DefaultMultiblockControllerBlock<?> controller = HologramRender.getHeldController(player.getMainHandItem());
        if (controller == null) {
            controller = HologramRender.getHeldController(player.getOffhandItem());
        }
        if (controller == null) {
            return;
        }
        BlockHitResult blockHitResult = event.getTarget();
        if (!(blockHitResult instanceof BlockHitResult)) {
            return;
        }
        BlockHitResult hit = blockHitResult;
        BlockPos anchor = hit.getBlockPos().relative(hit.getDirection());
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        MultiblockStructure structure = controller.getMultiblockStructure();
        int sizeX = structure.getSizeX();
        int sizeY = structure.getSizeY();
        int sizeZ = structure.getSizeZ();
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
            return;
        }
        int halfX = sizeX / 2;
        int halfZ = sizeZ / 2;
        BlockPos min = anchor.offset(-halfX, 0, -halfZ);
        BlockPos max = min.offset(sizeX, sizeY, sizeZ);
        boolean valid = structure.validateSpace((LevelAccessor)level, anchor);
        double eps = 0.0025;
        double minX = (double)min.getX() - eps;
        double minY = (double)min.getY() - eps;
        double minZ = (double)min.getZ() - eps;
        double maxX = (double)max.getX() + eps;
        double maxY = (double)max.getY() + eps;
        double maxZ = (double)max.getZ() + eps;
        PoseStack pose = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        Vec3 cam = event.getCamera().getPosition();
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        float time = level.getGameTime() % 2000L;
        float s = (float)(0.5 + 0.5 * Math.sin((double)time * 0.02));
        float r = valid ? 0.2f + 0.3f * (1.0f - s) : 0.8f + 0.2f * s;
        float g = valid ? 0.9f * s + 0.4f * (1.0f - s) : 0.2f + 0.2f * (1.0f - s);
        float b = valid ? 0.9f : 0.1f + 0.2f * (1.0f - s);
        float a = 0.9f;
        if (DCPShaders.HOLOGRAM != null) {
            double inset;
            float faceAlpha;
            VertexConsumer quad;
            block17: {
                try {
                    Uniform uValid;
                    ShaderInstance shader = DCPShaders.HOLOGRAM;
                    Uniform uTime = shader.getUniform("uTime");
                    if (uTime != null) {
                        uTime.set((float)(level.getGameTime() % 100000L) / 20.0f);
                    }
                    if ((uValid = shader.getUniform("uValid")) != null) {
                        uValid.set(valid ? 1.0f : 0.0f);
                    }
                }
                catch (Throwable shader) {
                    // empty catch block
                }
                quad = buffer.getBuffer(DCPRenderTypes.hologram());
                faceAlpha = 0.85f;
                inset = 0.0025;
                try {
                    ShaderInstance shader = DCPShaders.HOLOGRAM;
                    Uniform uCam = shader.getUniform("uCamPos");
                    if (uCam == null) break block17;
                    Vec3 c = event.getCamera().getPosition();
                    try {
                        uCam.set((float)c.x, (float)c.y, (float)c.z);
                    }
                    catch (Throwable t) {
                        try {
                            uCam.set((float)c.x, (float)c.y, (float)c.z, 1.0f);
                        }
                        catch (Throwable throwable) {}
                    }
                }
                catch (Throwable shader) {
                    // empty catch block
                }
            }
            HologramRender.drawBoxFaces(event.getPoseStack(), quad, minX + inset, minY + inset + 0.001, minZ + inset, maxX - inset, maxY - inset, maxZ - inset, r, g, b, faceAlpha);
            int centerX = min.getX() + halfX;
            int centerY = min.getY();
            int centerZ = min.getZ() + halfZ;
            double cMinX = (double)centerX + 0.002;
            double cMinY = (double)centerY + 0.002;
            double cMinZ = (double)centerZ + 0.002;
            double cMaxX = (double)centerX + 1.0 - 0.002;
            double cMaxY = (double)centerY + 1.0 - 0.002;
            double cMaxZ = (double)centerZ + 1.0 - 0.002;
            float hr = valid ? 0.35f : 1.0f;
            float hg = valid ? 1.0f : 0.35f;
            float hb = valid ? 1.0f : 0.25f;
            float ha = 0.95f;
            HologramRender.drawBoxFaces(event.getPoseStack(), quad, cMinX, cMinY, cMinZ, cMaxX, cMaxY, cMaxZ, hr, hg, hb, ha);
            return;
        }
        pose.popPose();
    }

    private static DefaultMultiblockControllerBlock<?> getHeldController(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        Item item = stack.getItem();
        if (item instanceof BlockItem blockItem) {
            if (blockItem.getBlock() instanceof DefaultMultiblockControllerBlock<?> controller) {
                return controller;
            }
        }
        return null;
    }

    private static void drawBoxFaces(PoseStack pose, VertexConsumer vc, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a) {
        HologramRender.emit(vc, pose, (float)minX, (float)minY, (float)minZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)maxX, (float)minY, (float)minZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)maxX, (float)minY, (float)maxZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)minX, (float)minY, (float)maxZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)minX, (float)maxY, (float)minZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)minX, (float)maxY, (float)maxZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)maxX, (float)maxY, (float)maxZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)maxX, (float)maxY, (float)minZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)minX, (float)minY, (float)minZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)minX, (float)maxY, (float)minZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)maxX, (float)maxY, (float)minZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)maxX, (float)minY, (float)minZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)minX, (float)minY, (float)maxZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)maxX, (float)minY, (float)maxZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)maxX, (float)maxY, (float)maxZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)minX, (float)maxY, (float)maxZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)minX, (float)minY, (float)minZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)minX, (float)minY, (float)maxZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)minX, (float)maxY, (float)maxZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)minX, (float)maxY, (float)minZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)maxX, (float)minY, (float)minZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)maxX, (float)maxY, (float)minZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)maxX, (float)maxY, (float)maxZ, r, g, b, a);
        HologramRender.emit(vc, pose, (float)maxX, (float)minY, (float)maxZ, r, g, b, a);
    }

    private static void emit(VertexConsumer vc, PoseStack pose, float x, float y, float z, float r, float g, float b, float a) {
        Matrix4f m = pose.last().pose();
        Vector4f v = new Vector4f(x, y, z, 1.0f);
        v.mul((Matrix4fc)m);
        vc.addVertex(v.x, v.y, v.z).setColor(r, g, b, a);
    }
}


