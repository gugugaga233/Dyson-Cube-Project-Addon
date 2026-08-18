package com.gugugaga233.dysoncubeprojectaddon;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class DCPConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ORE_PROCESSING_ENABLED;
    public static final ModConfigSpec.LongValue ORE_PROCESSING_COST;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("oreProcessing");
        ORE_PROCESSING_ENABLED = builder
                .comment("Enable the integrated grinding, washing and thermal processing chain.")
                .define("enabled", true);
        ORE_PROCESSING_COST = builder
                .comment("Total FE charged per input ore. The three UI stages are visual only.")
                .defineInRange("Cost", 12_000L, 0L, Long.MAX_VALUE);
        builder.pop();
        SPEC = builder.build();
    }

    private DCPConfig() {
    }
}

