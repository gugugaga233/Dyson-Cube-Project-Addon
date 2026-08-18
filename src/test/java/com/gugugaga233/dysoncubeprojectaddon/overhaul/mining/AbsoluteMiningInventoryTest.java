package com.gugugaga233.dysoncubeprojectaddon.overhaul.mining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.junit.jupiter.api.Test;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;

class AbsoluteMiningInventoryTest {

    @Test
    void preservesTenThousandDigitIntegersAcrossNbtRoundTrip() {
        AbsoluteMiningInventory source = new AbsoluteMiningInventory();
        AbsoluteInteger itemAmount = AbsoluteInteger.parse("9".repeat(10_000));
        AbsoluteInteger fluidAmount = AbsoluteInteger.parse("7" + "0".repeat(9_999));
        source.addItem(new ItemStack(Items.IRON_INGOT), itemAmount);
        source.addFluid(new FluidStack(Fluids.WATER, 1), fluidAmount);

        AbsoluteMiningInventory restored = new AbsoluteMiningInventory();
        restored.load(source.save());

        assertEquals(0, itemAmount.compareTo(restored.getItemAmount(0)));
        assertEquals(0, fluidAmount.compareTo(restored.getFluidAmount(0)));
    }

    @Test
    void preservesAbsoluteLayersBeyondLegacyFourLayerWindow() {
        CompoundTag layer = new CompoundTag();
        layer.putLong("0", 17L);
        CompoundTag amountTag = new CompoundTag();
        amountTag.put("layer4", layer);
        AbsoluteInteger amount = AbsoluteInteger.fromTag(amountTag);

        AbsoluteMiningInventory source = new AbsoluteMiningInventory();
        source.addItem(new ItemStack(Items.NETHERITE_INGOT), amount);
        AbsoluteMiningInventory restored = new AbsoluteMiningInventory();
        restored.load(source.save());

        assertEquals(0, amount.compareTo(restored.getItemAmount(0)));
    }

    @Test
    void ordinaryHandlersUseRequestedIntBatchesFromAbsoluteLedger() {
        AbsoluteMiningInventory inventory = new AbsoluteMiningInventory();
        AbsoluteInteger initialItems = AbsoluteInteger.parse("1000000000000000000000000000000");
        AbsoluteInteger initialFluid = AbsoluteInteger.parse("10000000000000000000000000");
        inventory.addItem(new ItemStack(Items.GOLD_INGOT), initialItems);
        inventory.addFluid(new FluidStack(Fluids.LAVA, 1), initialFluid);

        ItemStack extracted = inventory.extractItem(0, 4096, false);
        FluidStack drained = inventory.drainFluid(0, Integer.MAX_VALUE, false);

        assertEquals(4096, extracted.getCount());
        assertEquals(Integer.MAX_VALUE, drained.getAmount());
        assertEquals(0, AbsoluteInteger.parse("999999999999999999999999995904")
                .compareTo(inventory.getItemAmount(0)));
        assertEquals(0, AbsoluteInteger.parse("9999999999999997852516353")
                .compareTo(inventory.getFluidAmount(0)));
    }

    @Test
    void extractionBorrowsExactlyAcrossBaseTwoToTheSixtyThreeBoundary() {
        AbsoluteMiningInventory inventory = new AbsoluteMiningInventory();
        inventory.addItem(new ItemStack(Items.DIAMOND), AbsoluteInteger.parse("9223372036854775808"));

        assertEquals(64, inventory.extractItem(0, 64, false).getCount());
        assertEquals(0, AbsoluteInteger.parse("9223372036854775744")
                .compareTo(inventory.getItemAmount(0)));
    }

    @Test
    void ordinaryItemViewAdvertisesTheFullIntTransferWindow() {
        AbsoluteMiningInventory inventory = new AbsoluteMiningInventory();
        inventory.addItem(new ItemStack(Items.DIAMOND), AbsoluteInteger.parse("9".repeat(240)));

        assertEquals(Integer.MAX_VALUE, inventory.getItemInSlot(0).getCount());
        assertEquals(Integer.MAX_VALUE, inventory.extractItem(0, Integer.MAX_VALUE, true).getCount());
        assertEquals(240, com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8
                .toBigInteger(inventory.getItemAmount(0)).toString().length());
    }

    @Test
    void simulationDoesNotMutateLedger() {
        AbsoluteMiningInventory inventory = new AbsoluteMiningInventory();
        inventory.addItem(new ItemStack(Items.DIAMOND), absolute(1000));
        inventory.addFluid(new FluidStack(Fluids.WATER, 1), absolute(1000));

        inventory.extractItem(0, 64, true);
        inventory.drainFluid(0, 250, true);

        assertEquals(0, absolute(1000).compareTo(inventory.getItemAmount(0)));
        assertEquals(0, absolute(1000).compareTo(inventory.getFluidAmount(0)));
    }

    @Test
    void supportsDynamicResourceEntriesBeyondLegacySlotCount() {
        AbsoluteMiningInventory inventory = new AbsoluteMiningInventory();
        ItemStack[] resources = {
                new ItemStack(Items.STONE), new ItemStack(Items.DIRT), new ItemStack(Items.COBBLESTONE),
                new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.COPPER_INGOT),
                new ItemStack(Items.DIAMOND), new ItemStack(Items.REDSTONE), new ItemStack(Items.COAL),
                new ItemStack(Items.EMERALD), new ItemStack(Items.LAPIS_LAZULI)
        };
        for (ItemStack resource : resources) inventory.addItem(resource, absolute(1));

        assertEquals(resources.length, inventory.getItemSlots());
        assertFalse(inventory.getItemInSlot(resources.length - 1).isEmpty());
    }

    @Test
    void migratesLegacySlotsAndFluid() {
        AbsoluteMiningInventory inventory = new AbsoluteMiningInventory();
        ItemStackHandler legacy = new ItemStackHandler(9);
        legacy.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 37));

        inventory.migrateLegacyItems(legacy);
        inventory.migrateLegacyFluid(new FluidStack(Fluids.WATER, 9000));

        assertEquals(0, absolute(37).compareTo(inventory.getItemAmount(0)));
        assertEquals(0, absolute(9000).compareTo(inventory.getFluidAmount(0)));
    }

    @Test
    void ignoresEmptyOrMalformedAbsoluteEntries() {
        AbsoluteMiningInventory inventory = new AbsoluteMiningInventory();
        inventory.load(new CompoundTag());

        assertEquals(9, inventory.getItemSlots());
        assertEquals(256, inventory.getFluidTanks());
        assertEquals(0, absolute(0).compareTo(inventory.getTotalItemAmount()));
    }

    @Test
    void keepsEnoughEmptyFluidTanksForExternalStorageDiscovery() {
        AbsoluteMiningInventory inventory = new AbsoluteMiningInventory();

        assertEquals(256, inventory.getFluidTanks());
        assertEquals(FluidStack.EMPTY, inventory.getFluidInTank(0));
        assertEquals(FluidStack.EMPTY, inventory.getFluidInTank(255));
        assertEquals(0, inventory.getStoredFluidTypes());
    }

    @Test
    void fluidTankCountExpandsBeyondTheReservedAeSlots() {
        ListTag fluids = new ListTag();
        for (int index = 0; index < 300; index++) {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", "dysoncubeproject:test_fluid_" + index);
            entry.put("amount", absolute(1).toTag());
            fluids.add(entry);
        }
        CompoundTag inventoryTag = new CompoundTag();
        inventoryTag.put("fluids", fluids);

        AbsoluteMiningInventory inventory = new AbsoluteMiningInventory();
        inventory.load(inventoryTag);

        assertEquals(300, inventory.getStoredFluidTypes());
        assertEquals(300, inventory.getFluidTanks());
    }

    @Test
    void snapshotsUseTenDigitScientificNotationWithoutChangingExactAmounts() {
        AbsoluteMiningInventory inventory = new AbsoluteMiningInventory();
        AbsoluteInteger amount = AbsoluteInteger.parse("12345678909654321000");
        inventory.addItem(new ItemStack(Items.IRON_INGOT), amount);
        inventory.addFluid(new FluidStack(Fluids.WATER, 1), amount);

        assertEquals("1.234567891E19", inventory.itemSnapshots().getFirst().amount());
        assertEquals("1.234567891E19", inventory.fluidSnapshots().getFirst().amount());
        assertEquals(0, amount.compareTo(inventory.getItemAmount(0)));
    }

    @Test
    void scientificSnapshotsKeepExactlyTenSignificantDigits() {
        AbsoluteMiningInventory inventory = new AbsoluteMiningInventory();
        inventory.addItem(new ItemStack(Items.DIAMOND), AbsoluteInteger.parse("100000000000000000000"));

        assertEquals("1.000000000E20", inventory.itemSnapshots().getFirst().amount());
    }

    @Test
    void aeIntegrationExtractsLongBatchesFromA240DigitItemLedger() {
        String exact = "9".repeat(240);
        AbsoluteMiningInventory inventory = new AbsoluteMiningInventory();
        inventory.addItem(new ItemStack(Items.DIAMOND), AbsoluteInteger.parse(exact));
        var diamondId = BuiltInRegistries.ITEM.getKey(Items.DIAMOND);

        assertEquals(Long.MAX_VALUE, inventory.quoteItem(diamondId, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, inventory.extractItem(diamondId, Long.MAX_VALUE, true));
        assertEquals(0, AbsoluteInteger.parse(exact).compareTo(inventory.getItemAmount(diamondId)));

        assertEquals(Long.MAX_VALUE, inventory.extractItem(diamondId, Long.MAX_VALUE, false));
        AbsoluteInteger expected = AbsoluteInteger.parse(exact);
        expected = com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8.subtract(
                expected, AbsoluteInteger.parse(Long.toString(Long.MAX_VALUE)));
        assertEquals(0, expected.compareTo(inventory.getItemAmount(diamondId)));
    }

    @Test
    void aeIntegrationExtractsFluidBatchesBeyondTheVanillaIntLimit() {
        AbsoluteMiningInventory inventory = new AbsoluteMiningInventory();
        inventory.addFluid(new FluidStack(Fluids.WATER, 1), AbsoluteInteger.parse("100000000000000000000"));
        var waterId = BuiltInRegistries.FLUID.getKey(Fluids.WATER);
        long batch = (long)Integer.MAX_VALUE * 4;

        assertEquals(batch, inventory.extractFluid(waterId, batch, false));
        assertEquals(0, AbsoluteInteger.parse("99999999991410065412")
                .compareTo(inventory.getFluidAmount(waterId)));
    }

    private static AbsoluteInteger absolute(long amount) {
        return AbsoluteInteger.parse(Long.toString(Math.max(0L, amount)));
    }
}

