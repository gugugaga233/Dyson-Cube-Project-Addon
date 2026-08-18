/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.shaders.Uniform
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.ShaderInstance
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.gugugaga233.dysoncubeprojectaddon.client.tile;

import com.gugugaga233.dysoncubeprojectaddon.block.RayReceiverControllerBlock;
import com.gugugaga233.dysoncubeprojectaddon.block.tile.RayReceiverBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.client.DCPExtraModels;
import com.gugugaga233.dysoncubeprojectaddon.client.DCPRenderTypes;
import com.gugugaga233.dysoncubeprojectaddon.client.DCPShaders;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RayReceiverRender
implements BlockEntityRenderer<RayReceiverBlockEntity> {
    private static void drawBoxTopFace(PoseStack pose, VertexConsumer vc, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a) {
        RayReceiverRender.emit(vc, pose, (float)minX, (float)maxY, (float)minZ, r, g, b, a);
        RayReceiverRender.emit(vc, pose, (float)minX, (float)maxY, (float)maxZ, r, g, b, a);
        RayReceiverRender.emit(vc, pose, (float)maxX, (float)maxY, (float)maxZ, r, g, b, a);
        RayReceiverRender.emit(vc, pose, (float)maxX, (float)maxY, (float)minZ, r, g, b, a);
    }

    private static void drawBoxSideFace(PoseStack pose, VertexConsumer vc, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a) {
        RayReceiverRender.emit(vc, pose, (float)minX, (float)minY, (float)minZ, r, g, b, a);
        RayReceiverRender.emit(vc, pose, (float)minX, (float)maxY, (float)minZ, r, g, b, a);
        RayReceiverRender.emit(vc, pose, (float)maxX, (float)maxY, (float)minZ, r, g, b, a);
        RayReceiverRender.emit(vc, pose, (float)maxX, (float)minY, (float)minZ, r, g, b, a);
        RayReceiverRender.emit(vc, pose, (float)minX, (float)minY, (float)maxZ, r, g, b, a);
        RayReceiverRender.emit(vc, pose, (float)maxX, (float)minY, (float)maxZ, r, g, b, a);
        RayReceiverRender.emit(vc, pose, (float)maxX, (float)maxY, (float)maxZ, r, g, b, a);
        RayReceiverRender.emit(vc, pose, (float)minX, (float)maxY, (float)maxZ, r, g, b, a);
        RayReceiverRender.emit(vc, pose, (float)minX, (float)minY, (float)minZ, r, g, b, a);
        RayReceiverRender.emit(vc, pose, (float)minX, (float)minY, (float)maxZ, r, g, b, a);
        RayReceiverRender.emit(vc, pose, (float)minX, (float)maxY, (float)maxZ, r, g, b, a);
        RayReceiverRender.emit(vc, pose, (float)minX, (float)maxY, (float)minZ, r, g, b, a);
        RayReceiverRender.emit(vc, pose, (float)maxX, (float)minY, (float)minZ, r, g, b, a);
        RayReceiverRender.emit(vc, pose, (float)maxX, (float)maxY, (float)minZ, r, g, b, a);
        RayReceiverRender.emit(vc, pose, (float)maxX, (float)maxY, (float)maxZ, r, g, b, a);
        RayReceiverRender.emit(vc, pose, (float)maxX, (float)minY, (float)maxZ, r, g, b, a);
    }

    private static void emit(VertexConsumer vc, PoseStack pose, float x, float y, float z, float r, float g, float b, float a) {
        vc.addVertex(pose.last().pose(), x, y, z).setColor(r, g, b, a);
    }

    public void render(RayReceiverBlockEntity rayReceiverBlockEntity, float partial, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLightIn, int combinedOverlayIn) {
        poseStack.pushPose();
        BlockState blockState = rayReceiverBlockEntity.getBlockState();
        renderModel(DCPExtraModels.RAY_RECEIVER_BASE, blockState, poseStack, multiBufferSource, combinedLightIn, combinedOverlayIn);
        poseStack.translate(0.0f, 2.0f, 0.0f);
        renderModel(DCPExtraModels.RAY_RECEIVER_PLATE, blockState, poseStack, multiBufferSource, combinedLightIn, combinedOverlayIn);
        poseStack.pushPose();
        poseStack.translate(0.0f, 2.0f, 1.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
        renderModel(DCPExtraModels.RAY_RECEIVER_LENS_STANDS, blockState, poseStack, multiBufferSource, combinedLightIn, combinedOverlayIn);
        poseStack.rotateAround(Axis.XP.rotationDegrees(-90.0f), 0.0f, 0.55f, 0.5f);
        poseStack.rotateAround(Axis.XP.rotationDegrees(360.0f - rayReceiverBlockEntity.getCurrentPitch() - 180.0f), 0.0f, 0.55f, 0.5f);
        renderModel(DCPExtraModels.RAY_RECEIVER_LENS, blockState, poseStack, multiBufferSource, combinedLightIn, combinedOverlayIn);
        poseStack.popPose();
        if (DCPShaders.HOLO_HEX != null) {
            block10: {
                ShaderInstance shader = DCPShaders.HOLO_HEX;
                try {
                    Uniform uCam;
                    Uniform uSize;
                    Uniform uValid;
                    Uniform uTime = shader.getUniform("uTime");
                    if (uTime != null) {
                        uTime.set((float)(Minecraft.getInstance().level.getGameTime() % 100000L) / 20.0f);
                    }
                    if ((uValid = shader.getUniform("uValid")) != null) {
                        uValid.set(1.0f);
                    }
                    if ((uSize = shader.getUniform("uSize")) != null) {
                        uSize.set(0.75f);
                    }
                    if ((uCam = shader.getUniform("uCamPos")) == null) break block10;
                    Vec3 c = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
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
                catch (Throwable uTime) {
                    // empty catch block
                }
            }
            float r = 0.5f;
            float g = 0.9f;
            float b = 0.9f;
            float a = 0.7f;
            VertexConsumer quad = multiBufferSource.getBuffer(DCPRenderTypes.holoHex());
            RayReceiverRender.drawBoxTopFace(poseStack, quad, -1.0, 0.0, -1.0, 2.0, 0.3, 2.0, r, g, b, 0.85f);
            RayReceiverRender.drawBoxSideFace(poseStack, quad, 0.2499, 0.5, 0.2499, 0.751, 1.75, 0.751, r, g, b, 0.25f);
        }
        poseStack.popPose();
    }

    private static void renderModel(BakedModel model, BlockState blockState, PoseStack poseStack,
                                    MultiBufferSource buffers, int light, int overlay) {
        if (model != null) {
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                    poseStack.last(), buffers.getBuffer(RenderType.solid()), blockState, model,
                    1.0f, 1.0f, 1.0f, light, overlay
            );
        }
    }

    public AABB getRenderBoundingBox(RayReceiverBlockEntity blockEntity) {
        return RayReceiverControllerBlock.MULTIBLOCK_STRUCTURE.getAABB(blockEntity.getBlockPos());
    }
}

