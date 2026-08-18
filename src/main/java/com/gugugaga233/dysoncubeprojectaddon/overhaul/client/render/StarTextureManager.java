package com.gugugaga233.dysoncubeprojectaddon.overhaul.client.render;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.StarData;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * 星体像素纹理管理器
 * <p>
 * 使用 NativeImage + DynamicTexture 生成星体纹理，缓存到 TextureManager。
 * 按星体类型缓存，避免重复生成。
 */
public final class StarTextureManager {

    private static final Map<String, ResourceLocation> CACHE = new HashMap<>();
    private static final Map<String, DynamicTexture> TEXTURES = new HashMap<>();

    private StarTextureManager() {}

    /**
     * 获取星体纹理的 ResourceLocation。
     * 若未缓存则生成并注册。
     *
     * @param star 星体数据
     * @return 纹理 ResourceLocation（客户端）
     */
    public static ResourceLocation getTexture(StarData star) {
        final StarData effectiveStar = star != null ? star : StarData.SUN;
        // 同一视觉类型共用纹理，避免高速结算时为每颗随机恒星生成动态资源。
        String key = effectiveStar.type();

        return CACHE.computeIfAbsent(key, k -> registerTexture(k, effectiveStar));
    }

    /** 注册纹理到 TextureManager */
    private static ResourceLocation registerTexture(String key, StarData star) {
        // 生成像素数组
        int[] pixels = StarTextureGenerator.generate(star);

        // 创建 NativeImage
        NativeImage image = new NativeImage(128, 128, false);
        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                image.setPixelRGBA(x, y, pixels[y * 128 + x]);
            }
        }

        // 创建 DynamicTexture 并注册
        DynamicTexture texture = new DynamicTexture(image);
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath("dysoncubeproject", "star/" + key.hashCode());
        Minecraft.getInstance().getTextureManager().register(location, texture);
        TEXTURES.put(key, texture);

        return location;
    }

    /** Releases all generated textures so a resource reload can recreate them cleanly. */
    public static void clear() {
        CACHE.values().forEach(Minecraft.getInstance().getTextureManager()::release);
        TEXTURES.clear();
        CACHE.clear();
    }
}

