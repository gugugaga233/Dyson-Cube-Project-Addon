package com.gugugaga233.dysoncubeprojectaddon.item;

import com.gugugaga233.dysoncubeprojectaddon.DCPAttachments;
import com.gugugaga233.dysoncubeprojectaddon.DCPContent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import sonar.fluxnetworks.api.energy.BigNumber;

public class CompressedItem extends Item {
    public static final int COMPRESSED_SAIL_BASE_COUNT = 72;
    public static final int COMPRESSED_BEAM_BASE_COUNT = 36;

    private static final BigDecimal TEN = BigDecimal.TEN;
    private static final int DIRECT_MASS_DISPLAY_LEVEL = 24;

    private final BigDecimal baseMassKg;

    public CompressedItem(Item.Properties properties, String baseMassKg) {
        super(properties);
        this.baseMassKg = new BigDecimal(baseMassKg);
    }

    public CompressedItem(Item.Properties properties, double baseMassKg) {
        this(properties, Double.toString(baseMassKg));
    }

    public CompressedItem(Item.Properties properties) {
        this(properties, BigDecimal.ZERO.toPlainString());
    }

    public BigDecimal getBaseMassKg() {
        return baseMassKg;
    }

    public static BigInteger getLevel(ItemStack stack) {
        return stack.getOrDefault(DCPAttachments.COMPRESSION_LEVEL.get(), BigInteger.ONE);
    }

    /** Returns the uncompressed sail count before applying 10^(level - 1). */
    public static int getSolarSailCount(ItemStack stack) {
        int count = stack.getOrDefault(DCPAttachments.SOLAR_SAIL.get(), 0);
        if (count > 0) {
            return count;
        }
        return stack.is(DCPContent.Items.COMPRESSED_SAIL.get())
                ? COMPRESSED_SAIL_BASE_COUNT
                : 0;
    }

    /** Returns the uncompressed beam count before applying 10^(level - 1). */
    public static int getBeamCount(ItemStack stack) {
        int count = stack.getOrDefault(DCPAttachments.BEAM.get(), 0);
        if (count > 0) {
            return count;
        }
        return stack.is(DCPContent.Items.COMPRESSED_BEAM.get())
                ? COMPRESSED_BEAM_BASE_COUNT
                : 0;
    }

    public static ItemStack withLevel(Item item, BigInteger level) {
        if (level.signum() < 1) {
            throw new IllegalArgumentException("Compression level must be at least 1");
        }
        ItemStack stack = new ItemStack(item);
        stack.set(DCPAttachments.COMPRESSION_LEVEL.get(), level);
        return stack;
    }

    /**
     * Returns 10^(level-1) in Flux Networks' exponent-aware number format. Both
     * the compression level and decimal exponent stay as strings/BigInteger, so
     * levels such as LV10000 never pass through a primitive or floating point.
     */
    public static BigNumber getFluxProgressMultiplier(ItemStack stack) {
        return getFluxProgressMultiplier(getLevel(stack));
    }

    static BigNumber getFluxProgressMultiplier(BigInteger level) {
        BigInteger exponent = level.max(BigInteger.ONE).subtract(BigInteger.ONE);
        return exponent.signum() == 0
                ? BigNumber.valueOf(1L)
                : BigNumber.scientific("1", exponent.toString());
    }

    /** Returns baseMass * 10^(level-1), saturated at cap. */
    public BigDecimal getMassKgCapped(ItemStack stack, BigDecimal cap) {
        return scaleMassCapped(baseMassKg, getLevel(stack).subtract(BigInteger.ONE), cap);
    }

    static BigDecimal scaleMassCapped(BigDecimal baseMass, BigInteger exponent, BigDecimal cap) {
        return CompressionMath.scaleMassCapped(baseMass, exponent, cap);
    }

    public String getMassDisplay(ItemStack stack) {
        BigInteger level = getLevel(stack);
        if (level.compareTo(BigInteger.valueOf(DIRECT_MASS_DISPLAY_LEVEL)) <= 0) {
            int exponent = level.subtract(BigInteger.ONE).intValueExact();
            return baseMassKg.multiply(TEN.pow(exponent)).stripTrailingZeros().toEngineeringString() + " kg";
        }
        return baseMassKg.stripTrailingZeros().toPlainString() + " x 10^(" +
                abbreviate(level.subtract(BigInteger.ONE)) + ") kg";
    }

    private static String getMaterialCountDisplay(ItemStack stack, int baseCount) {
        BigInteger exponent = getLevel(stack).subtract(BigInteger.ONE);
        if (exponent.compareTo(BigInteger.valueOf(DIRECT_MASS_DISPLAY_LEVEL - 1L)) <= 0) {
            return BigInteger.valueOf(baseCount)
                    .multiply(BigInteger.TEN.pow(exponent.intValueExact()))
                    .toString();
        }
        return baseCount + " x 10^(" + abbreviate(exponent) + ")";
    }

    public static String abbreviate(BigInteger value) {
        String text = value.toString();
        if (text.length() <= 32) {
            return text;
        }
        return text.substring(0, 16) + "... (" + text.length() + " digits)";
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable TooltipContext context,
                                List<Component> tooltips, TooltipFlag flag) {
        BigInteger level = getLevel(stack);
        tooltips.add(Component.translatable("tooltip.dysoncubeproject.compression_level", abbreviate(level)));
        tooltips.add(Component.translatable("tooltip.dysoncubeproject.injection_multiplier",
                "10^(" + abbreviate(level.subtract(BigInteger.ONE)) + ")"));
        if (stack.is(DCPContent.Items.COMPRESSED_SAIL.get())) {
            tooltips.add(Component.translatable("tooltip.dysoncubeproject.contains_solar_sails",
                    getMaterialCountDisplay(stack, COMPRESSED_SAIL_BASE_COUNT)));
        } else if (stack.is(DCPContent.Items.COMPRESSED_BEAM.get())) {
            tooltips.add(Component.translatable("tooltip.dysoncubeproject.contains_beams",
                    getMaterialCountDisplay(stack, COMPRESSED_BEAM_BASE_COUNT)));
        }
        if (baseMassKg.signum() > 0) {
            tooltips.add(Component.translatable("tooltip.dysoncubeproject.mass", getMassDisplay(stack)));
        }
        tooltips.add(Component.translatable("tooltip.dysoncubeproject.next_compression",
                abbreviate(level.add(BigInteger.ONE))));
    }
}

