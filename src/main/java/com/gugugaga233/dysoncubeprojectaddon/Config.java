package com.gugugaga233.dysoncubeprojectaddon;
import com.hrznstudio.titanium.annotation.config.ConfigFile;
import com.hrznstudio.titanium.annotation.config.ConfigVal;
/**
 * 原模组配置 + 寰宇大道魔改配置
 * <p>
 * 原模组使用 Titanium @ConfigFile 注解自动加载配置。
 * 新增魔改相关配置项统一添加在此处。
 */
@ConfigFile
public class Config {
    private static final int DEFAULT_SOLAR_SAIL_REQUIREMENT = 50_000_000;

    // ====== 原模组配置 ======
    @ConfigVal(comment="Solar sail requirement for a star with one solar radius; actual per-star requirements scale with radius squared and have no fixed cap")
    @ConfigVal.InRangeInt(min=1)
    public static int BASE_SOLAR_SAIL_REQUIREMENT = DEFAULT_SOLAR_SAIL_REQUIREMENT;
    @Deprecated
    @ConfigVal(comment="Legacy config key; interpreted as the one-solar-radius base requirement, not a maximum")
    @ConfigVal.InRangeInt(min=1)
    public static int MAX_SOLAR_PANELS = DEFAULT_SOLAR_SAIL_REQUIREMENT;
    @ConfigVal(comment="How many solar panels each beam can support")
    @ConfigVal.InRangeInt(min=1)
    public static int BEAM_TO_SOLAR_PANEL_RATIO = 6;
    @ConfigVal(comment="The amount of power generated per sail")
    @ConfigVal.InRangeInt(min=0)
    public static int POWER_PER_SAIL = 20;
    @ConfigVal(comment="Always show sphere at max progress")
    public static boolean SHOW_AT_MAX_PROGRESS = false;
    @ConfigVal(comment="The power that the ray receiver can extract from the sphere every tick")
    @ConfigVal.InRangeInt(min=1)
    public static int RAY_RECEIVER_EXTRACT_POWER = 50000000;
    @ConfigVal(comment="The power that the ray receiver buffer has")
    @ConfigVal.InRangeInt(min=1)
    public static int RAY_RECEIVER_POWER_BUFFER = 100000000;
    @ConfigVal(comment="The power that the em railejector buffer has")
    @ConfigVal.InRangeInt(min=1)
    public static int RAIL_EJECTOR_POWER_BUFFER = 400000;
    @ConfigVal(comment="The power that the em railejector consumes each tick per sent item")
    @ConfigVal.InRangeInt(min=1)
    public static int RAIL_EJECTOR_CONSUME = 40;
    // ====== 寰宇大道魔改新增配置 ======
    @ConfigVal(comment="寰宇大道乘算基数（默认 10，每包裹一颗星体 ×10）")
    public static double MULTIPLIER_BASE = 10.0;
    @ConfigVal(comment="黑洞解锁阈值（默认 1000，包裹第 1001 颗星体时触发宇宙觉醒）")
    @ConfigVal.InRangeInt(min=1)
    public static int BLACKHOLE_UNLOCK_THRESHOLD = 1000;
    @ConfigVal(comment="视界放大器倍率基数（默认 173，时间膨胀因子 sqrt(3) ≈ 1.732 × 100）")
    public static double AMPLIFIER_BASE = 173.0;
    @ConfigVal(comment="视界放大器损耗速率（损坏度 < 50 时，每 Tick +X，默认 0.1）")
    public static double AMPLIFIER_DAMAGE_RATE_LOW = 0.1;
    @ConfigVal(comment="视界放大器损耗速率（损坏度 >= 50 时，每 Tick +X，默认 0.005）")
    public static double AMPLIFIER_DAMAGE_RATE_HIGH = 0.005;
    @ConfigVal(comment="视界放大器单次修复减少的损坏度百分比（默认 5%）")
    public static double AMPLIFIER_REPAIR_AMOUNT = 5.0;
    @ConfigVal(comment="视界放大器修复所需物品 ID（默认 dysoncubeproject:strange_alloy）")
    public static String AMPLIFIER_REPAIR_ITEM = "dysoncubeproject:strange_alloy";
    @ConfigVal(comment="反物质星体解锁阈值（默认 5000，包裹第 5001 颗星体时触发反物质觉醒）")
    @ConfigVal.InRangeInt(min=1)
    public static int ANTIMATTER_UNLOCK_THRESHOLD = 5000;
    @ConfigVal(comment="暗物质星解锁阈值（默认 3000）")
    @ConfigVal.InRangeInt(min=1)
    public static int DARK_MATTER_UNLOCK_THRESHOLD = 3000;
    @ConfigVal(comment="普朗克星解锁阈值（默认 10000）")
    @ConfigVal.InRangeInt(min=1)
    public static int PLANCK_STAR_UNLOCK_THRESHOLD = 10000;
    @ConfigVal(comment="普朗克星包裹所需量子结构光束数量（默认 64000）")
    @ConfigVal.InRangeInt(min=1)
    public static int QUANTUM_BEAM_REQUIRED = 64000;
    @ConfigVal(comment="普朗克星包裹所需量子结构光束物品ID（默认 dysoncubeproject:quantum_structure_beam）")
    public static String QUANTUM_BEAM_ITEM = "dysoncubeproject:quantum_structure_beam";
    @ConfigVal(comment="鸿蒙之气解锁所需包裹数（默认 500,000）")
    @ConfigVal.InRangeInt(min=1)
    public static int PRIMORDIAL_QI_UNLOCK_THRESHOLD = 500_000;

    public static int getBaseSolarSailRequirement() {
        return BASE_SOLAR_SAIL_REQUIREMENT != DEFAULT_SOLAR_SAIL_REQUIREMENT
                ? BASE_SOLAR_SAIL_REQUIREMENT
                : MAX_SOLAR_PANELS;
    }

}

