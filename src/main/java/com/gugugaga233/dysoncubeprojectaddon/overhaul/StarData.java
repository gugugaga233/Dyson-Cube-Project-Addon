package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import net.minecraft.network.chat.Component;

/**
 * 星体数据类（Record）
 * <p>
 * 描述一颗被戴森球包裹的星体的完整参数。
 * 所有物理参数使用 double（已确认的数据类型策略：星体参数用 double）。
 *
 * @param type            星体类型标识（如 "RED_DWARF"、"BLACK_HOLE" 等，见 {@link UniverseRandomizer}）
 * @param displayName     显示名称（中文，如 "红矮星"、"O型蓝超巨星"）
 * @param radiusInRSun    半径（单位：太阳半径 R☉）
 * @param massInMSun      质量（单位：太阳质量 M☉）
 * @param temperatureK    表面温度（单位：开尔文 K）
 * @param basePowerFE     基础功率（单位：FE/s，由物理引擎计算）
 * @param isSpecial       是否为特殊星体（类星体、GRB 等稀有天体，UI 用特殊颜色/音效）
 */
public record StarData(
        String type,
        String displayName,
        double radiusInRSun,
        double massInMSun,
        double temperatureK,
        double basePowerFE,
        boolean isSpecial
) {
    /** 太阳基准（黄矮星 G）—— 首个星体默认值 */
    public static final StarData SUN = new StarData(
            "G_TYPE", "太阳", 1.0, 1.0, 5778.0,
            // 黑体辐射：P = σ·4πR²·T⁴，σ=5.67e-8，R=1R☉=6.96e8m
            // = 5.67e-8 × 4π × (6.96e8)² × (5778)⁴ ≈ 3.828e26 W
            3.828e26, false
    );

    /**
     * 序列化为 NBT 友好的字符串（用于 SavedData 持久化）。
     * 格式：type|displayName|radius|mass|temp|power|special
     */
    public String serializeToString() {
        return type + "|" + displayName + "|" + radiusInRSun + "|" + massInMSun + "|"
                + temperatureK + "|" + basePowerFE + "|" + isSpecial;
    }

    /** 从序列化字符串还原 StarData（未匹配时返回太阳基准） */
    public static StarData deserializeFromString(String s) {
        if (s == null || s.isEmpty()) {
            return SUN;
        }
        String[] parts = s.split("\\|");
        try {
            if (parts.length == 7) {
                return new StarData(
                        parts[0], parts[1],
                        Double.parseDouble(parts[2]),
                        Double.parseDouble(parts[3]),
                        Double.parseDouble(parts[4]),
                        Double.parseDouble(parts[5]),
                        Boolean.parseBoolean(parts[6])
                );
            }
        } catch (NumberFormatException ignored) {
            // 解析失败回退到太阳
        }
        return SUN;
    }

    /**
     * Resolves the display name through the active Minecraft language. The legacy
     * displayName field remains in the serialized form for save compatibility.
     */
    public Component localizedDisplayName() {
        if ("G_TYPE".equals(type) && "太阳".equals(displayName)) {
            return Component.translatable("star.dysoncubeproject.g_type_sun");
        }
        return Component.translatable("star.dysoncubeproject."
                + type.toLowerCase(java.util.Locale.ROOT));
    }

    @Override
    public String toString() {
        return displayName + "（" + String.format("%.1f", massInMSun) + " M☉，"
                + String.format("%.0f", temperatureK) + " K）";
    }
}
