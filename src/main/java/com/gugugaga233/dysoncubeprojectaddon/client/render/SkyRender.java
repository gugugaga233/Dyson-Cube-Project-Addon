/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.shaders.Uniform
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.ShaderInstance
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent$Stage
 */
package com.gugugaga233.dysoncubeprojectaddon.client.render;

import com.gugugaga233.dysoncubeprojectaddon.Config;
import com.gugugaga233.dysoncubeprojectaddon.client.DCPRenderTypes;
import com.gugugaga233.dysoncubeprojectaddon.client.DCPShaders;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.StarData;
import com.gugugaga233.dysoncubeprojectaddon.world.ClientDysonSphere;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereStructure;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class SkyRender {
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.isRainingAt(mc.player.getOnPos())) {
            return;
        }
        String subscribedTo = ClientDysonSphere.DYSON_SPHERE_PROGRESS.getSubscribedPlayers().getOrDefault(mc.player.getStringUUID(), mc.player.getStringUUID());
        DysonSphereStructure sphere = ClientDysonSphere.DYSON_SPHERE_PROGRESS.getSpheres().getOrDefault(subscribedTo, null);
        if (sphere == null) {
            return;
        }
        float progress = (float)sphere.getProgress();
        if (Config.SHOW_AT_MAX_PROGRESS) {
            progress = 1.0f;
        }
        // ====== 魔改：根据当前星体类型调整戴森球天空渲染颜色 ======
        StarData star = sphere.getCurrentStarData();
        float[] rgb = getStarSkyColor(star);
        float r = rgb[0];
        float g = rgb[1];
        float b = rgb[2];
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        float skyAngle = mc.level.getTimeOfDay(event.getPartialTick().getGameTimeDeltaTicks()) * 360.0f;
        if (DCPShaders.DYSON_SUN != null) {
            block14: {
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
                        uSize.set(25.0f);
                    }
                    if ((uCam = shader.getUniform("uCamPos")) == null) break block14;
                    Vec3 c = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                    try {
                        uCam.set(0.0f, 0.0f, 0.0f);
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
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(-90.0f));
            pose.mulPose(Axis.XP.rotationDegrees(90.0f));
            pose.mulPose(Axis.XP.rotationDegrees(skyAngle));
            pose.translate(-30.0f, 0.0f, -310.0f);
            float s = 30.0f;
            float a = 0.7f;
            float rainLevel = 1.0f - mc.level.getRainLevel(event.getPartialTick().getGameTimeDeltaTicks());
            VertexConsumer vc = buffer.getBuffer(DCPRenderTypes.holoHex());
            SkyRender.emit(vc, pose, 0.0f, s, 0.0f, r, g, b, a *= rainLevel);
            SkyRender.emit(vc, pose, s * 2.0f * progress, s, 0.0f, r, g, b, a);
            SkyRender.emit(vc, pose, s * 2.0f * progress, -s, 0.0f, r, g, b, a);
            SkyRender.emit(vc, pose, 0.0f, -s, 0.0f, r, g, b, a);
            pose.popPose();
            buffer.endBatch(DCPRenderTypes.dysonSun());
        }
    }

    /**
     * 根据星体类型返回天空渲染颜色（RGB 0~1）。
     * 主序星按温度色序，特殊星体有专属颜色。
     */
    private static float[] getStarSkyColor(StarData star) {
        if (star == null) star = StarData.SUN;
        String type = star.type();
        // 反物质星体：紫色/粉色系
        if (type != null && type.startsWith("ANTI_")) {
            return new float[]{0.9f, 0.4f, 0.9f};
        }
        return switch (type) {
            // 红矮星 / 红超巨星：橙红
            case "M_TYPE", "RED_SUPERGIANT" -> new float[]{0.9f, 0.4f, 0.2f};
            // 橙矮星：橙黄
            case "K_TYPE" -> new float[]{1.0f, 0.7f, 0.3f};
            // 黄矮星（太阳）：黄
            case "G_TYPE" -> new float[]{1.0f, 0.9f, 0.5f};
            // F型星：亮白
            case "F_TYPE" -> new float[]{1.0f, 0.95f, 0.8f};
            // A型星：白
            case "A_TYPE" -> new float[]{1.0f, 1.0f, 1.0f};
            // B型星 / O型星：蓝白
            case "B_TYPE", "O_TYPE", "WOLF_RAYET", "BLUE_STRAGGLER" -> new float[]{0.6f, 0.8f, 1.0f};
            // 类星体/耀变体/塞佛特：极亮青白
            case "QUASAR", "BLAZAR", "SEYFERT" -> new float[]{0.9f, 1.0f, 1.0f};
            // 黑洞：暗紫红
            case "BLACK_HOLE", "STELLAR_BH", "SUPERMASSIVE_BH", "Q_STAR" -> new float[]{0.5f, 0.2f, 0.5f};
            // 中子星：亮蓝白
            case "NEUTRON_STAR", "QUARK_STAR" -> new float[]{0.7f, 0.9f, 1.0f};
            // 超新星/GRB：极亮黄白
            case "SUPERNOVA", "HYPERNOVA", "GRB" -> new float[]{1.0f, 1.0f, 0.8f};
            // 棕矮星：深红褐
            case "L_DWARF", "T_DWARF", "Y_DWARF" -> new float[]{0.6f, 0.3f, 0.3f};
            // 默认：太阳黄
            default -> new float[]{1.0f, 0.9f, 0.5f};
        };
    }

    private static void emit(VertexConsumer vc, PoseStack pose, float x, float y, float z, float r, float g, float b, float a) {
        vc.addVertex(pose.last().pose(), x, y, z).setColor(r, g, b, a);
    }
}


