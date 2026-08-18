package com.gugugaga233.dysoncubeprojectaddon.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;
import sonar.fluxnetworks.api.energy.BigNumber;

class NumberUtilsTest {

    @Test
    void formatsAeSlotAmountsWithPostfixes() {
        assertEquals("1.2K", NumberUtils.getAeCompactAmount("1234"));
        assertEquals("12K", NumberUtils.getAeCompactAmount("12345"));
        assertEquals("123K", NumberUtils.getAeCompactAmount("123456"));
        assertEquals("1.2M", NumberUtils.getAeCompactAmount("1234567"));
        assertEquals("1.2G", NumberUtils.getAeCompactAmount("1234567890"));
        assertEquals("10P", NumberUtils.getAeCompactAmount("10000000000000000"));
    }

    @Test
    void keepsExactLargeAmountSeparateFromAeSlotAmount() {
        AbsoluteInteger value = AbsoluteInteger.parse("1000000000000000000000000");

        assertEquals("1E24", NumberUtils.getScientificInteger(value));
        assertEquals("1E24", NumberUtils.getAeCompactAmount(value));
    }
    @Test
    void compactsHugeEnergyWithoutDroppingExponent() {
        BigNumber energy = BigNumber.scientific("1.234567890123456789", "450000");

        assertEquals("1.23456789E450000", NumberUtils.getCompactBigNumber(energy));
        assertEquals("1.23456789E450000", NumberUtils.getFormatedBigNumber(energy));
    }

    @Test
    void carriesMantissaRoundingIntoExponent() {
        BigNumber energy = BigNumber.scientific("9.9999999999", "450000");

        assertEquals("1E450001", NumberUtils.getCompactBigNumber(energy));
    }

    @Test
    void carriesIntoAnExponentTooLargeForBigInteger() {
        assertEquals("1E(1E27)",
                NumberUtils.compactBigNumberDisplay(
                        "9.9999999999E999999999999999999999999 FE"));
        assertEquals("1 x 10^(1E27)",
                NumberUtils.getScientificNotationExplanation("1E(1E27)"));
    }

    @Test
    void keepsNestedExponentNotationBounded() {
        assertEquals("1E(1E27)",
                NumberUtils.compactBigNumberDisplay(
                        "1E1000000000000000000000000 FE"));
    }

    @Test
    void explainsOrdinaryScientificNotation() {
        assertEquals("7.5 x 10^27",
                NumberUtils.getScientificNotationExplanation("7.5E27"));
    }

    @Test
    void boundsExactFluxStorageNotation() {
        assertEquals("248805152106699 x 2^24879271184",
                NumberUtils.getCompactAbsoluteStorageCalculation(
                        "248805152106699 x 2^24879271184"));
    }

    @Test
    void hubScaleAbsoluteIntegerUsesNestedCompactExponent() {
        assertEquals("1E(7.49E9)",
                NumberUtils.getCompactAbsoluteCalculation(
                        "248805152106699 x 2^24879271184"));
    }

    @Test
    void compactsFluxEightLayeredBigNumberExponent() {
        BigNumber energy = BigNumber.fromExponentDigit(
                "5.8012448129723429130797169411635944748933049897405429",
                0, 526, 2_488_126_415_135_961L);

        assertEquals("1E(8.469714182E9990)", NumberUtils.getCompactBigNumber(energy));
        assertEquals("5.801244813 x 10^(2488126415135961 x 2^33138)",
                NumberUtils.getCompactBigNumberStorageCalculation(energy));
    }

    @Test
    void compactsLongPlainDecimalReturnedByFluxEight() {
        assertEquals("1.23456789E29", NumberUtils.compactBigNumberDisplay(
                "123456789012345678901234567890 FE"));
    }

    @Test
    void keepsShortPlainDecimalReadable() {
        assertEquals("123456789012345678", NumberUtils.compactBigNumberDisplay(
                "123456789012345678 FE"));
    }

    @Test
    void carriesPlainDecimalRoundingIntoExponent() {
        assertEquals("1E30", NumberUtils.compactBigNumberDisplay(
                "999999999999999999999999999999 FE"));
    }

    @Test
    void decimalGuiFormattingUsesTenSignificantDigitsOnly() {
        assertEquals("1.23456789E+450000",
                NumberUtils.getCompactDecimal("1.234567890123456789E450000"));
    }

    @Test
    void compactAbsoluteIntegerFormattingDoesNotExpandHugeValues() {
        AbsoluteInteger value = com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8
                .fromBigInteger(java.math.BigInteger.TEN.pow(2_000));

        String display = NumberUtils.getScientificInteger(value);

        org.junit.jupiter.api.Assertions.assertTrue(display.length() < 100);
        org.junit.jupiter.api.Assertions.assertTrue(display.matches("1\\.\\d+E\\d+"));
    }

    @Test
    void compactsLayeredAbsoluteIntegerIntoDecimalScientificNotation() {
        AbsoluteInteger value = AbsoluteInteger.parse("1" + "0".repeat(10_000));

        String display = NumberUtils.getScientificInteger(value);
        org.junit.jupiter.api.Assertions.assertTrue(display.matches("[1-9]\\.?\\d*E\\d+"));
    }

    @Test
    void compactsHugeSubtractedDecimalWithoutBuildingPlainNetworkText() {
        BigDecimal remaining = new BigDecimal("3.4E100000").subtract(BigDecimal.ONE);

        String display = NumberUtils.getCompactDecimal(remaining);

        assertTrue(display.length() < 64);
        assertEquals("3.4E+100000", display);
    }

    @Test
    void unknownAbsoluteCalculationFormatIsStillBounded() {
        String display = NumberUtils.getCompactAbsoluteCalculation("9".repeat(100_000) + ".0");

        assertTrue(display.length() < 64);
        assertTrue(display.startsWith("10E+"));
    }
}

