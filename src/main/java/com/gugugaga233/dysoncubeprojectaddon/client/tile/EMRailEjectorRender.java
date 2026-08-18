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
 */
package com.gugugaga233.dysoncubeprojectaddon.client.tile;

import com.gugugaga233.dysoncubeprojectaddon.block.EMRailEjectorControllerBlock;
import com.gugugaga233.dysoncubeprojectaddon.block.tile.EMRailEjectorBlockEntity;
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

public class EMRailEjectorRender
implements BlockEntityRenderer<EMRailEjectorBlockEntity> {
    public void render(EMRailEjectorBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLightIn, int combinedOverlayIn) {
        poseStack.pushPose();
        BlockState blockState = entity.getBlockState();
        renderModel(DCPExtraModels.EM_RAILEJECTOR_BASE, blockState, poseStack, multiBufferSource, combinedLightIn, combinedOverlayIn);
        poseStack.translate(0.0, 2.5, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
        poseStack.translate(1.0f, 0.0f, 0.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
        poseStack.rotateAround(Axis.XP.rotationDegrees(360.0f - entity.getCurrentYaw()), 0.0f, 0.5f, 0.5f);
        poseStack.rotateAround(Axis.ZP.rotationDegrees(360.0f - entity.getCurrentPitch()), 0.0f, 0.5f, 0.5f);
        renderModel(DCPExtraModels.EM_RAILEJECTOR_GUN, blockState, poseStack, multiBufferSource, combinedLightIn, combinedOverlayIn);
        if (entity.getLevel() != null) {
            float b;
            float g;
            long gameTime = entity.getLevel().getGameTime();
            float period = entity.getProgressBarComponent().getMaxProgress();
            float shootWindow = 28.0f;
            float chargeWindow = 85.0f;
            float t = entity.getProgressBarComponent().getProgress();
            if (t >= period - chargeWindow) {
                float chargeT = (t - (period - chargeWindow)) / chargeWindow;
                float intensity = 0.0f + (float)Math.pow(chargeT, 3.0);
                poseStack.pushPose();
                poseStack.translate(0.12, 0.45, 0.5);
                if (DCPShaders.RAIL_ELECTRIC != null) {
                    try {
                        Uniform uInt;
                        ShaderInstance shader = DCPShaders.RAIL_ELECTRIC;
                        Uniform uTime = shader.getUniform("uTime");
                        if (uTime != null) {
                            uTime.set(((float)entity.getLevel().getGameTime() + partialTicks) / 20.0f);
                        }
                        if ((uInt = shader.getUniform("uIntensity")) != null) {
                            uInt.set(intensity);
                        }
                    }
                    catch (Throwable shader) {
                        // empty catch block
                    }
                    RenderType rt = DCPRenderTypes.railElectricLines();
                    VertexConsumer lines = multiBufferSource.getBuffer(rt);
                    int segments = 7;
                    float baseRadius = 0.62f + 0.05f * (float)Math.sin(((float)gameTime + partialTicks) * 0.2f);
                    float jitter = 0.05f;
                    int r = 100;
                    int g2 = 200;
                    int b2 = 255;
                    int a = Math.min(255, 60 + (int)(195.0f * intensity));
                    int ring = 0;
                    while ((float)ring < 38.0f * chargeT) {
                        float ringOffsetX = 0.05f * (float)ring;
                        for (int i = 0; i < segments; ++i) {
                            float seed = (float)i * 17.0f + (float)ring * 31.0f + entity.getLevel().getRandom().nextFloat() * 6.0f;
                            float ang = (float)Math.toRadians((float)i * (360.0f / (float)segments) + (float)Math.sin(((float)gameTime + partialTicks + seed) * 0.6f) * 20.0f);
                            float ang2 = ang + (float)Math.toRadians(10.0f + (float)Math.sin(((float)gameTime + partialTicks + seed * 1.37f) * 0.9f) * 12.0f);
                            float rad1 = baseRadius + (float)Math.sin(((float)gameTime + partialTicks + seed) * 0.8f) * jitter;
                            float rad2 = baseRadius + 0.07f + (float)Math.sin(((float)gameTime + partialTicks + seed * 0.77f) * 0.8f) * jitter;
                            float y1 = (float)(Math.cos(ang) * (double)rad1);
                            float z1 = (float)(Math.sin(ang) * (double)rad1);
                            float y2 = (float)(Math.cos(ang2) * (double)rad2);
                            float z2 = (float)(Math.sin(ang2) * (double)rad2);
                            float expand = 0.04f * intensity;
                            y1 *= 1.0f + expand;
                            z1 *= 1.0f + expand;
                            y2 *= 1.0f + expand;
                            z2 *= 1.0f + expand;
                            float rf = (float)r / 255.0f;
                            float gf = (float)g2 / 255.0f;
                            float bf = (float)b2 / 255.0f;
                            float af = (float)a / 255.0f;
                            lines.addVertex(poseStack.last().pose(), 0.0f + ringOffsetX, y1, z1).setColor(rf, gf, bf, af);
                            lines.addVertex(poseStack.last().pose(), 0.12f + ringOffsetX, y2, z2).setColor(rf, gf, bf, af);
                        }
                        ++ring;
                    }
                    Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
                }
                poseStack.popPose();
            }
            t = entity.getLevel().getGameTime() - entity.getLastExecution();
            float progress = t / shootWindow;
            if (t > 0.0f && t < shootWindow && DCPShaders.RAIL_BEAM != null) {
                try {
                    ShaderInstance shader = DCPShaders.RAIL_BEAM;
                    Uniform uTime = shader.getUniform("uTime");
                    if (uTime != null) {
                        uTime.set(((float)entity.getLevel().getGameTime() + partialTicks) / 20.0f);
                    }
                    Uniform uInt = shader.getUniform("uIntensity");
                    float beamIntensity = 1.2f * (1.0f - progress);
                    if (uInt != null) {
                        uInt.set(beamIntensity);
                    }
                }
                catch (Throwable shader) {
                    // empty catch block
                }
                VertexConsumer beam = multiBufferSource.getBuffer(DCPRenderTypes.railBeam());
                poseStack.pushPose();
                poseStack.translate(0.12, 0.45, 0.5);
                float beamLen = 160.0f * (2.0f - progress * 2.0f);
                float halfW = 0.1f + 0.06f * (1.0f - progress);
                float r = 0.9f;
                g = 1.0f;
                b = 1.0f;
                float a = 1.0f;
                beam.addVertex(poseStack.last().pose(), 0.0f, -halfW, 0.0f).setColor(r, g, b, a);
                beam.addVertex(poseStack.last().pose(), beamLen, -halfW, 0.0f).setColor(r, g, b, a);
                beam.addVertex(poseStack.last().pose(), beamLen, halfW, 0.0f).setColor(r, g, b, a);
                beam.addVertex(poseStack.last().pose(), 0.0f, halfW, 0.0f).setColor(r, g, b, a);
                beam.addVertex(poseStack.last().pose(), 0.0f, 0.0f, -halfW).setColor(r, g, b, a);
                beam.addVertex(poseStack.last().pose(), beamLen, 0.0f, -halfW).setColor(r, g, b, a);
                beam.addVertex(poseStack.last().pose(), beamLen, 0.0f, halfW).setColor(r, g, b, a);
                beam.addVertex(poseStack.last().pose(), 0.0f, 0.0f, halfW).setColor(r, g, b, a);
                poseStack.popPose();
            }
            if (DCPShaders.RAIL_ELECTRIC != null) {
                float shockDur = 6.0f;
                if (t > 0.0f && t < shockDur) {
                    try {
                        Uniform uInt;
                        ShaderInstance shader = DCPShaders.RAIL_ELECTRIC;
                        Uniform uTime = shader.getUniform("uTime");
                        if (uTime != null) {
                            uTime.set(((float)entity.getLevel().getGameTime() + partialTicks) / 20.0f);
                        }
                        if ((uInt = shader.getUniform("uIntensity")) != null) {
                            uInt.set(1.0f);
                        }
                    }
                    catch (Throwable shader) {
                        // empty catch block
                    }
                    VertexConsumer lines = multiBufferSource.getBuffer(DCPRenderTypes.railElectricLines());
                    poseStack.pushPose();
                    poseStack.translate(0.12, 0.45, 0.5);
                    float radius = 0.2f + 0.9f * (t / shockDur);
                    int segs = 32;
                    float rf = 1.0f;
                    float gf = 1.0f;
                    float bf = 1.0f;
                    float af = 1.0f;
                    for (int depth = 0; depth < 7; ++depth) {
                        for (int i = 0; i < segs; ++i) {
                            double a0 = Math.PI * 2 * (double)i / (double)segs;
                            double a1 = Math.PI * 2 * (double)(i + 1) / (double)segs;
                            float y0 = (float)(Math.cos(a0) * (double)radius);
                            float z0 = (float)(Math.sin(a0) * (double)radius);
                            float y1 = (float)(Math.cos(a1) * (double)radius);
                            float z1 = (float)(Math.sin(a1) * (double)radius);
                            lines.addVertex(poseStack.last().pose(), (float)depth * 0.5f, y0, z0).setColor(rf, gf, bf, af);
                            lines.addVertex(poseStack.last().pose(), (float)depth * 0.5f, y1, z1).setColor(rf, gf, bf, af);
                        }
                    }
                    poseStack.popPose();
                }
            }
            if (DCPExtraModels.EM_RAILEJECTOR_PROJECTILE != null && t > 0.0f && t < shootWindow) {
                float distance = 0.5f + progress * 1000.0f;
                poseStack.pushPose();
                poseStack.translate(0.75, -0.1, 0.0);
                poseStack.translate(distance, 0.0f, 0.0f);
                renderModel(DCPExtraModels.EM_RAILEJECTOR_PROJECTILE, blockState, poseStack, multiBufferSource, combinedLightIn, combinedOverlayIn);
                if (DCPShaders.RAIL_BEAM != null) {
                    try {
                        Uniform uInt;
                        ShaderInstance shader = DCPShaders.RAIL_BEAM;
                        Uniform uTime = shader.getUniform("uTime");
                        if (uTime != null) {
                            uTime.set(((float)entity.getLevel().getGameTime() + partialTicks) / 20.0f);
                        }
                        if ((uInt = shader.getUniform("uIntensity")) != null) {
                            uInt.set(1.2f);
                        }
                    }
                    catch (Throwable shader) {
                        // empty catch block
                    }
                    VertexConsumer glow = multiBufferSource.getBuffer(DCPRenderTypes.railBeam());
                    float s = 0.18f;
                    float r = 0.9f;
                    g = 1.0f;
                    b = 1.0f;
                    float a = 1.0f;
                    glow.addVertex(poseStack.last().pose(), -s, -s, 0.0f).setColor(r, g, b, a);
                    glow.addVertex(poseStack.last().pose(), s, -s, 0.0f).setColor(r, g, b, a);
                    glow.addVertex(poseStack.last().pose(), s, s, 0.0f).setColor(r, g, b, a);
                    glow.addVertex(poseStack.last().pose(), -s, s, 0.0f).setColor(r, g, b, a);
                    glow.addVertex(poseStack.last().pose(), -s, 0.0f, -s).setColor(r, g, b, a);
                    glow.addVertex(poseStack.last().pose(), s, 0.0f, -s).setColor(r, g, b, a);
                    glow.addVertex(poseStack.last().pose(), s, 0.0f, s).setColor(r, g, b, a);
                    glow.addVertex(poseStack.last().pose(), -s, 0.0f, s).setColor(r, g, b, a);
                }
                poseStack.popPose();
            }
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

    public AABB getRenderBoundingBox(EMRailEjectorBlockEntity blockEntity) {
        return EMRailEjectorControllerBlock.MULTIBLOCK_STRUCTURE.getAABB(blockEntity.getBlockPos());
    }
}

