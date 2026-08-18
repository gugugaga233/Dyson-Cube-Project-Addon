package com.gugugaga233.dysoncubeprojectaddon.overhaul.mining;

import com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;
import sonar.fluxnetworks.api.energy.BigNumber;

public final class OreProcessingMath {
    public static final long OUTPUT_MULTIPLIER = 4L;

    private OreProcessingMath() {
    }

    public static BigNumber energyCost(AbsoluteInteger inputAmount, long costPerOre) {
        if (inputAmount == null || inputAmount.isZero() || costPerOre <= 0) {
            return new BigNumber(0);
        }
        return FluxMath8.toBigNumber(inputAmount).multiplySmallInteger(costPerOre);
    }

    public static AbsoluteInteger outputAmount(AbsoluteInteger inputAmount, int recipeOutputCount) {
        if (inputAmount == null || inputAmount.isZero() || recipeOutputCount <= 0) {
            return new AbsoluteInteger();
        }
        return FluxMath8.multiply(inputAmount,
                Math.multiplyExact(OUTPUT_MULTIPLIER, recipeOutputCount));
    }
}

