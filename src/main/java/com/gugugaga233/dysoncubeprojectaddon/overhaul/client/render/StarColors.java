package com.gugugaga233.dysoncubeprojectaddon.overhaul.client.render;

/**
 * 星体像素图标颜色常量速查表
 * <p>
 * 所有颜色为 ARGB 格式（0xAARRGGBB），默认 Alpha = 0xFF（不透明）。
 * 按用户规格固化，供 {@link StarTextureGenerator} 使用。
 */
public final class StarColors {

    private StarColors() {}

    // ====== 主序星 ======
    public static final int COLOR_RED_DWARF_CENTER = 0xFFC85000;
    public static final int COLOR_RED_DWARF_EDGE   = 0xFF961E00;
    public static final int COLOR_ORANGE_DWARF_CENTER = 0xFFFFB432;
    public static final int COLOR_ORANGE_DWARF_EDGE   = 0xFFC86414;
    public static final int COLOR_YELLOW_DWARF_CENTER = 0xFFFFE664;
    public static final int COLOR_YELLOW_DWARF_EDGE   = 0xFFFFB400;
    public static final int COLOR_F_STAR_CENTER = 0xFFFFF0C8;
    public static final int COLOR_F_STAR_EDGE   = 0xFFE6D2AA;
    public static final int COLOR_A_STAR_CENTER = 0xFFFFFFFF;
    public static final int COLOR_A_STAR_EDGE   = 0xFFDCE6FF;
    public static final int COLOR_B_STAR_CENTER = 0xFFC8DCFF;
    public static final int COLOR_B_STAR_EDGE   = 0xFF6496FF;
    public static final int COLOR_O_STAR_CENTER = 0xFFFFFFFF;
    public static final int COLOR_O_STAR_EDGE   = 0xFF6496FF;
    public static final int COLOR_O_STAR_SPIKE  = 0xFFB4DCFF;

    // ====== 棕矮星 ======
    public static final int COLOR_L_DWARF   = 0xFFB4503C;
    public static final int COLOR_T_DWARF   = 0xFF824678;
    public static final int COLOR_Y_DWARF   = 0xFF281432;

    // ====== 致密星 ======
    public static final int COLOR_WHITE_DWARF_CORE  = 0xFFFFFFFF;
    public static final int COLOR_WHITE_DWARF_HALO  = 0xFFFFFFFF;
    public static final int COLOR_NEUTRON_CORE      = 0xFFFFFFFF;
    public static final int COLOR_NEUTRON_PULSE     = 0xFFB4DCFF;
    public static final int COLOR_QUARK_CORE        = 0xFF783296;
    public static final int COLOR_QUARK_HALO        = 0xFF64FF64;
    public static final int COLOR_QSTAR_CORE        = 0xFFA0A0A0;
    public static final int COLOR_QSTAR_RING        = 0xFF3C3C3C;
    public static final int COLOR_ELECTROWEAK_CORE  = 0xFFFFFFFF;
    public static final int COLOR_ELECTROWEAK_ARC   = 0xFF64C8FF;
    public static final int COLOR_BOSON_CORE        = 0x50FFFFFF;
    public static final int COLOR_BOSON_RING        = 0x80FFFFFF;

    // ====== 星系核 ======
    public static final int COLOR_SEYFERT_CORE      = 0xFFFFF0C8;
    public static final int COLOR_SEYFERT_ARM       = 0xFFFFF0C8;
    public static final int COLOR_BLAZAR_CORE       = 0xFFFFFFFF;
    public static final int COLOR_BLAZAR_JET        = 0xFFB4DCFF;
    public static final int COLOR_QUASAR_CORE       = 0xFFFFFFFF;
    public static final int COLOR_QUASAR_DISK_INNER = 0xFFC8E6FF;
    public static final int COLOR_QUASAR_DISK_MID   = 0xFFFFC864;
    public static final int COLOR_QUASAR_DISK_OUTER = 0xFFC864C8;
    public static final int COLOR_QUASAR_JET        = 0xFFDCECFF;

    // ====== 巨星/超新星前身 ======
    public static final int COLOR_RED_SUPERGIANT_CENTER = 0xFFC85028;
    public static final int COLOR_RED_SUPERGIANT_EDGE   = 0xFF961E0A;
    public static final int COLOR_RED_SUPERGIANT_SPOT   = 0xFF64140A;
    public static final int COLOR_WR_CORE  = 0xFF64B4FF;
    public static final int COLOR_WR_HALO  = 0xFF50C8FF;

    // ====== 行星 ======
    public static final int COLOR_HOT_JUPITER_BG      = 0xFFC89650;
    public static final int COLOR_HOT_JUPITER_BAND1   = 0xFFB46432;
    public static final int COLOR_HOT_JUPITER_BAND2   = 0xFFDCB478;
    public static final int COLOR_SUPER_EARTH_BLUE    = 0xFF3296FF;
    public static final int COLOR_SUPER_EARTH_GREEN   = 0xFF32C832;

    // ====== 特殊事件 ======
    public static final int COLOR_BLUE_STRAGGLER_CENTER = 0xFF96DCFF;
    public static final int COLOR_BLUE_STRAGGLER_EDGE   = 0xFF50B4FF;
    public static final int COLOR_SUPERNOVA             = 0xFFFFFFC8;
    public static final int COLOR_HYPERNOVA_INNER       = 0xFFFFFFFF;
    public static final int COLOR_HYPERNOVA_OUTER       = 0xFFB4DCFF;
    public static final int COLOR_GRB                   = 0xFFFFFF64;

    // ====== 黑洞 ======
    public static final int COLOR_BH_DISK_INNER = 0xFFFF9632;
    public static final int COLOR_BH_DISK_OUTER = 0xFFC83200;
    public static final int COLOR_SMBH_DISK_INNER = 0xFF3264FF;
    public static final int COLOR_SMBH_DISK_MID   = 0xFFFFFFFF;
    public static final int COLOR_SMBH_DISK_WARM  = 0xFFFFC832;
    public static final int COLOR_SMBH_DISK_OUTER = 0xFFC83296;

    // ====== 工具方法 ======

    /** 颜色插值：t 在 0~1 之间 */
    public static int lerpColor(int c1, int c2, float t) {
        int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** 设置像素颜色透明度 */
    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}
