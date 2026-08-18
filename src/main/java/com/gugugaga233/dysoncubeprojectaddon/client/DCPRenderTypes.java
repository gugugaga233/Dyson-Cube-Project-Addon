/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.renderer.RenderStateShard
 *  net.minecraft.client.renderer.RenderStateShard$ShaderStateShard
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 */
package com.gugugaga233.dysoncubeprojectaddon.client;

import com.gugugaga233.dysoncubeprojectaddon.client.DCPShaders;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class DCPRenderTypes {
    private static RenderType DYSON_SUN;
    private static RenderType HOLOGRAM;
    private static RenderType HOLO_HEX;
    private static RenderType RAIL_ELECTRIC_LINES;
    private static RenderType RAIL_BEAM;

    public static RenderType dysonSun() {
        if (DYSON_SUN == null) {
            RenderType.CompositeState state = RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(() -> DCPShaders.DYSON_SUN)).setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST).setCullState(RenderStateShard.NO_CULL).setWriteMaskState(RenderStateShard.COLOR_WRITE).createCompositeState(true);
            DYSON_SUN = RenderType.create((String)"dysoncubeproject_sun", (VertexFormat)DefaultVertexFormat.POSITION_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)state);
        }
        return DYSON_SUN;
    }

    public static RenderType hologram() {
        if (HOLOGRAM == null) {
            RenderType.CompositeState state = RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(() -> DCPShaders.HOLOGRAM)).setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST).setCullState(RenderStateShard.NO_CULL).setWriteMaskState(RenderStateShard.COLOR_WRITE).createCompositeState(true);
            HOLOGRAM = RenderType.create((String)"dysoncubeproject_hologram", (VertexFormat)DefaultVertexFormat.POSITION_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)state);
        }
        return HOLOGRAM;
    }

    public static RenderType railElectricLines() {
        if (RAIL_ELECTRIC_LINES == null) {
            RenderType.CompositeState state = RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(() -> DCPShaders.RAIL_ELECTRIC)).setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST).setCullState(RenderStateShard.NO_CULL).setWriteMaskState(RenderStateShard.COLOR_WRITE).createCompositeState(true);
            RAIL_ELECTRIC_LINES = RenderType.create((String)"dysoncubeproject_rail_electric_lines", (VertexFormat)DefaultVertexFormat.POSITION_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)state);
        }
        return RAIL_ELECTRIC_LINES;
    }

    public static RenderType holoHex() {
        if (HOLO_HEX == null) {
            RenderType.CompositeState state = RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(() -> DCPShaders.HOLO_HEX)).setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST).setCullState(RenderStateShard.NO_CULL).setWriteMaskState(RenderStateShard.COLOR_WRITE).createCompositeState(true);
            HOLO_HEX = RenderType.create((String)"dysoncubeproject_holo_hex", (VertexFormat)DefaultVertexFormat.POSITION_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)state);
        }
        return HOLO_HEX;
    }

    public static RenderType railBeam() {
        if (RAIL_BEAM == null) {
            RenderType.CompositeState state = RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(() -> DCPShaders.RAIL_BEAM)).setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST).setCullState(RenderStateShard.NO_CULL).setWriteMaskState(RenderStateShard.COLOR_WRITE).createCompositeState(true);
            RAIL_BEAM = RenderType.create((String)"dysoncubeproject_rail_beam", (VertexFormat)DefaultVertexFormat.POSITION_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)state);
        }
        return RAIL_BEAM;
    }
}


