package com.gugugaga233.dysoncubeprojectaddon.overhaul.block.entity;

import com.gugugaga233.dysoncubeprojectaddon.DysonCubeProject;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientboundOpenCosmicMiningHubPacket;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.FluxMath8;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.OverhaulContent;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.AbsoluteMiningInventory;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.LaserPowerSystem;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.MiningTargetData;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.MiningStarTier;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.PlanetaryResource;
import com.gugugaga233.dysoncubeprojectaddon.util.NumberUtils;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereProgressSavedData;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import sonar.fluxnetworks.api.energy.BigNumber;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;
import sonar.fluxnetworks.api.energy.IBigNumberEnergyStorage;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

public class CosmicMiningHubBlockEntity extends BlockEntity {
    private static final int OUTPUT_INTERVAL = 20;

    private final LaserPowerSystem laserPower = new LaserPowerSystem();
    private final AbsoluteMiningInventory inventory = new AbsoluteMiningInventory();
    private final IItemHandler outputItems = new OutputItemHandler();
    private final IFluidHandler outputFluids = new OutputFluidHandler();
    private BigNumber energy = new BigNumber(0);
    private final IBigNumberEnergyStorage bigEnergyStorage = new MiningBigEnergyStorage();
    private final IFNEnergyStorage fluxEnergyStorage = new MiningFluxEnergyStorage();
    private final IEnergyStorage forgeEnergyStorage = new MiningForgeEnergyStorage();
    private boolean running = true;
    private long targetIndex;
    private long cycle;
    private String status = "running";
    private MiningTargetData previewTarget;
    private MiningStarTier selectedStarTier = MiningStarTier.SINGLE_PLANET;
    private BigDecimal customStarEndEarthMass;
    private boolean cleanedLegacyTargets;

    public CosmicMiningHubBlockEntity(BlockPos pos, BlockState state) {
        super(OverhaulContent.getCosmicMiningHubBEType(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;
        cycle++;
        if (cycle % 5 == 0) pushOutputs();
        MiningTargetData target = getTarget(false);
        if (target == null) {
            status = "no_target";
            return;
        }
        if (!running) {
            status = "stopped";
            return;
        }
        if (target.depleted()) {
            stopForDepletedTarget();
            return;
        }
        if (!consumeLaserEnergy(target.starTier().energyCost())) {
            status = "no_energy";
            return;
        }
        if (cycle % OUTPUT_INTERVAL == 0) {
            status = "running";
            produce(target);
        } else if (!status.equals("output_full")) {
            status = "running";
        }
        setChanged();
    }

    private boolean consumeLaserEnergy(BigNumber amount) {
        if (amount == null || amount.signum() <= 0) return true;
        DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(level);
        if (!drawLaserEnergy(amount.deepCopy(), data, true).isEmpty()) return false;
        return drawLaserEnergy(amount.deepCopy(), data, false).isEmpty();
    }

    public boolean consumeOreProcessingEnergy(BigNumber amount) {
        return consumeLaserEnergy(amount);
    }

    /** Returns the unpaid remainder while preserving Dyson -> local buffer -> adjacent port priority. */
    private BigNumber drawLaserEnergy(BigNumber remaining, DysonSphereProgressSavedData data, boolean simulate) {
        if (data != null && !remaining.isEmpty()) {
            BigNumber extracted = data.extractStoredEnergy(remaining.deepCopy(), simulate);
            remaining.subtract(extracted);
        }
        if (!remaining.isEmpty()) {
            BigNumber extracted = simulate
                    ? energy.quote(remaining.deepCopy())
                    : energy.extract(remaining.deepCopy());
            remaining.subtract(extracted);
            if (!simulate && !extracted.isEmpty()) setChanged();
        }
        if (!remaining.isEmpty()) {
            for (Direction direction : Direction.values()) {
                if (level.getBlockEntity(worldPosition.relative(direction))
                        instanceof LaserEnergyInputPortBlockEntity port) {
                    remaining.subtract(port.extractForHub(remaining.deepCopy(), simulate));
                    if (remaining.isEmpty()) break;
                }
            }
        }
        return remaining;
    }

    private BigNumber getExternalPortEnergy() {
        BigNumber total = new BigNumber(0);
        if (level == null) return total;
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction))
                    instanceof LaserEnergyInputPortBlockEntity port) {
                total.addEnergy(port.getStoredEnergy());
            }
        }
        return total;
    }

    private int getConnectedPortCount() {
        if (level == null) return 0;
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction))
                    instanceof LaserEnergyInputPortBlockEntity) count++;
        }
        return count;
    }

    private void produce(MiningTargetData target) {
        Random random = target.planetType() == com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.PlanetType.ROGUE
                ? new Random(target.seed())
                : new Random(target.seed() ^ cycle ^ worldPosition.asLong());
        PlanetaryResource.Output output = PlanetaryResource.roll(target.planetType(), random);
        BigDecimal unitMass = output.massKind().kgPerUnit();
        BigDecimal remainingMass = target.remainingMassKg();
        BigNumber effectiveOutput = laserPower.effectiveOutput();
        // Do not divide two enormous BigDecimals unless the output can actually
        // consume the remaining mass. This is the common path for high-tier
        // targets and avoids BigInteger.divideAndRemainder on every cycle.
        boolean massLimitedOutput = reachesMassLimit(effectiveOutput, remainingMass, unitMass);
        BigDecimal massLimitedDecimal = massLimitedOutput
                ? remainingMass.divideToIntegralValue(unitMass)
                : null;
        if (massLimitedOutput && massLimitedDecimal.signum() <= 0) {
            target.discardRemainingMass();
            DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(level);
            if (data != null) {
                previewTarget = data.persistMiningTarget(targetKey(), target);
                data.setDirty();
            } else {
                previewTarget = target;
            }
            stopForDepletedTarget();
            return;
        }
        BigDecimal produced;
        if (massLimitedOutput) {
            produced = massLimitedDecimal;
        } else {
            produced = finiteBigNumberToDecimal(effectiveOutput);
        }
        if (produced.signum() <= 0) return;

        AbsoluteInteger exactOutput = floorToAbsoluteInteger(produced);

        if (output.isFluid()) {
            inventory.addFluid(output.fluid(), exactOutput);
        } else {
            inventory.addItem(output.item(), exactOutput);
        }
        markOutputInventoryChanged();
        DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(level);
        if (data == null) return;
        BigDecimal consumedMass = massLimitedOutput
                ? massLimitedDecimal.multiply(unitMass)
                : unitMass.multiply(produced);
        target.consumeMass(consumedMass);
        target = data.persistMiningTarget(targetKey(), target);
        previewTarget = target;
        data.setDirty();
        if (target.depleted()) stopForDepletedTarget();
    }

    private void stopForDepletedTarget() {
        boolean changed = running || !status.equals("depleted");
        running = false;
        status = "depleted";
        if (changed) setChanged();
    }

    private static BigDecimal finiteBigNumberToDecimal(BigNumber value) {
        return value.getCoefficient().scaleByPowerOfTen(value.toBigIntegerExponent().intValueExact());
    }

    /** Floors positive values and writes Flux's binary limbs without decimal-string expansion. */
    static AbsoluteInteger floorToAbsoluteInteger(BigDecimal value) {
        if (value == null || value.signum() <= 0) return new AbsoluteInteger();
        return FluxMath8.fromBigInteger(value.toBigInteger());
    }

    /** Avoids feeding enormous expanded decimals through BigNumber.stripTrailingZeros every tick. */
    static boolean reachesMassLimit(BigNumber output, BigDecimal integerLimit) {
        if (integerLimit == null || integerLimit.signum() <= 0) return true;
        if (output == null || output.signum() <= 0) return false;

        BigInteger outputExponent = output.toBigIntegerExponent();
        long limitExponent = (long) integerLimit.precision() - integerLimit.scale() - 1L;
        int exponentComparison = outputExponent.compareTo(BigInteger.valueOf(limitExponent));
        if (exponentComparison != 0) return exponentComparison > 0;

        return finiteBigNumberToDecimal(output).compareTo(integerLimit) >= 0;
    }

    /**
     * Fast mass-limit check that only expands the BigNumber when its decimal
     * exponent is close enough to the limit to require an exact comparison.
     */
    static boolean reachesMassLimit(BigNumber output, BigDecimal remainingMass, BigDecimal unitMass) {
        if (output == null || output.signum() <= 0 || remainingMass == null
                || remainingMass.signum() <= 0 || unitMass == null || unitMass.signum() <= 0) {
            return false;
        }
        long limitExponent = decimalExponent(remainingMass) - decimalExponent(unitMass);
        int exponentComparison = output.toBigIntegerExponent()
                .compareTo(BigInteger.valueOf(limitExponent));
        if (exponentComparison != 0) return exponentComparison > 0;
        return unitMass.multiply(finiteBigNumberToDecimal(output)).compareTo(remainingMass) >= 0;
    }

    private static long decimalExponent(BigDecimal value) {
        return (long) value.precision() - value.scale() - 1L;
    }

    private void pushOutputs() {
        if (level == null) return;
        boolean outputChanged = false;
        for (Direction direction : Direction.values()) {
            BlockPos targetPos = worldPosition.relative(direction);
            BlockEntity targetEntity = level.getBlockEntity(targetPos);
            IItemHandler targetItems = level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos,
                    level.getBlockState(targetPos), targetEntity, direction.getOpposite());
            if (targetItems != null) {
                for (int slot = 0; slot < inventory.getItemSlots(); slot++) {
                    ItemStack stack = inventory.extractItem(slot, Integer.MAX_VALUE, true);
                    if (stack.isEmpty()) continue;
                    ItemStack remainder = ItemHandlerHelper.insertItemStacked(targetItems, stack, false);
                    int moved = stack.getCount() - remainder.getCount();
                    if (moved > 0) {
                        inventory.extractItem(slot, moved, false);
                        outputChanged = true;
                    }
                }
            }
            IFluidHandler targetFluids = level.getCapability(Capabilities.FluidHandler.BLOCK, targetPos,
                    level.getBlockState(targetPos), targetEntity, direction.getOpposite());
            if (targetFluids != null) {
                for (int tank = 0; tank < inventory.getStoredFluidTypes(); tank++) {
                    FluidStack offered = inventory.getFluidInTank(tank);
                    int accepted = targetFluids.fill(offered, IFluidHandler.FluidAction.EXECUTE);
                    if (accepted > 0) {
                        inventory.drainFluid(tank, accepted, false);
                        outputChanged = true;
                    }
                }
            }
        }
        if (outputChanged) markOutputInventoryChanged();
    }

    private String targetKey() {
        String dimension = level == null ? "unknown" : level.dimension().location().toString();
        return dimension + ":" + worldPosition.asLong() + ":" + targetIndex;
    }

    private String targetKeyPrefix() {
        String dimension = level == null ? "unknown" : level.dimension().location().toString();
        return dimension + ":" + worldPosition.asLong() + ":";
    }

    private long targetSeed() {
        long worldSeed = level instanceof ServerLevel serverLevel ? serverLevel.getSeed() : 0L;
        return worldSeed ^ worldPosition.asLong() ^ (0x9E3779B97F4A7C15L * (targetIndex + 1L));
    }

    private MiningTargetData getTarget(boolean persist) {
        if (level == null) return null;
        DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(level);
        if (data == null) return null;
        if (!cleanedLegacyTargets) {
            data.removeMiningTargetsExcept(targetKeyPrefix(), targetKey());
            cleanedLegacyTargets = true;
        }
        MiningTargetData saved = data.getMiningTarget(targetKey());
        if (saved != null) {
            selectedStarTier = saved.starTier();
            previewTarget = saved;
            return saved;
        }
        if (previewTarget == null || previewTarget.seed() != targetSeed()
                || previewTarget.starTier() != selectedStarTier) {
            previewTarget = selectedStarTier == MiningStarTier.STAR_END && customStarEndEarthMass != null
                    ? MiningTargetData.createWithEarthMass(targetSeed(), selectedStarTier, customStarEndEarthMass)
                    : MiningTargetData.create(targetSeed(), selectedStarTier);
        }
        return persist ? data.persistMiningTarget(targetKey(), previewTarget) : previewTarget;
    }

    public void setLaserPower(String input) {
        laserPower.setInput(input);
        setChanged();
    }

    public void toggleRunning() {
        MiningTargetData target = getTarget(false);
        if (target != null && target.depleted()) {
            stopForDepletedTarget();
            return;
        }
        running = !running;
        status = running ? "running" : "stopped";
        setChanged();
    }

    public boolean setStarTier(int level) {
        MiningTargetData target = getTarget(false);
        DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(this.level);
        if (target == null || data != null && data.getMiningTarget(targetKey()) != null) return false;
        MiningStarTier tier = MiningStarTier.byLevel(level);
        if (tier == selectedStarTier) return true;
        selectedStarTier = tier;
        previewTarget = null;
        if (tier == MiningStarTier.STAR_END) {
            running = false;
            status = "stopped";
        }
        setChanged();
        return true;
    }

    public boolean setStarEndMass(String input) {
        MiningTargetData target = getTarget(false);
        DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(level);
        if (target == null || selectedStarTier != MiningStarTier.STAR_END
                || data != null && data.getMiningTarget(targetKey()) != null) return false;
        customStarEndEarthMass = MiningStarTier.STAR_END.parseCustomEarthMass(input);
        previewTarget = MiningTargetData.createWithEarthMass(targetSeed(), selectedStarTier,
                customStarEndEarthMass);
        setChanged();
        return true;
    }

    public boolean selectNextTarget() {
        MiningTargetData target = getTarget(false);
        DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(level);
        boolean persisted = data != null && data.getMiningTarget(targetKey()) != null;
        if (target == null || persisted && !target.depleted()) return false;
        if (persisted) data.removeMiningTarget(targetKey());
        targetIndex++;
        previewTarget = null;
        running = false;
        status = "stopped";
        setChanged();
        return true;
    }

    public void extractSlot(Player player, int slot) {
        if (slot < 0 || slot >= inventory.getItemSlots()) return;
        ItemStack extracted = inventory.extractItem(slot, 64, false);
        if (!extracted.isEmpty()) {
            markOutputInventoryChanged();
            if (!player.getInventory().add(extracted)) player.drop(extracted, false);
        }
    }

    public void markExternalOutputChanged() {
        markOutputInventoryChanged();
    }

    private void markOutputInventoryChanged() {
        setChanged();
        invalidateCapabilities();
        if (level == null || level.isClientSide) return;
        for (Direction direction : Direction.values()) {
            BlockEntity adjacent = level.getBlockEntity(worldPosition.relative(direction));
            if (adjacent instanceof MiningItemOutputPortBlockEntity itemPort) {
                itemPort.refreshExternalStorage();
            } else if (adjacent instanceof MiningFluidOutputPortBlockEntity fluidPort) {
                fluidPort.refreshExternalStorage();
            }
        }
    }

    public void openGui(Player player) {
        if (player instanceof ServerPlayer serverPlayer) sendSnapshot(serverPlayer);
    }

    public void sendSnapshot(ServerPlayer player) {
        MiningTargetData target = getTarget(false);
        if (target == null) return;
        StringBuilder items = new StringBuilder();
        for (AbsoluteMiningInventory.ItemSnapshot entry : inventory.itemSnapshots()) {
            if (!items.isEmpty()) items.append(';');
            items.append(entry.id()).append(',').append(entry.amount());
        }
        StringBuilder fluids = new StringBuilder();
        for (AbsoluteMiningInventory.FluidSnapshot entry : inventory.fluidSnapshots()) {
            if (!fluids.isEmpty()) fluids.append(';');
            fluids.append(entry.id()).append(',').append(entry.amount());
        }
        DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(level);
        boolean canSelectNext = data == null || data.getMiningTarget(targetKey()) == null || target.depleted();
        boolean canChangeTier = data == null || data.getMiningTarget(targetKey()) == null;
        BigNumber dysonEnergy = data == null ? new BigNumber(0) : data.getStoredEnergy();
        BigNumber externalEnergy = energy.deepCopy();
        externalEnergy.addEnergy(getExternalPortEnergy());
        com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.PlanetaryResource.ResourcePreview resources =
                com.gugugaga233.dysoncubeprojectaddon.overhaul.mining.PlanetaryResource.preview(target.planetType());
        DysonCubeProject.NETWORK.sendTo(new ClientboundOpenCosmicMiningHubPacket(
                worldPosition.asLong(), target.planetType().id(),
                NumberUtils.getCompactDecimal(target.initialMassKg()),
                NumberUtils.getCompactDecimal(target.remainingMassKg()),
                target.percentRemaining(), target.depleted(),
                laserPower.inputText(), laserPower.zeroCount(), laserPower.efficiencyPercent(),
                stripFe(laserPower.effectivePower().toCalculationString()),
                NumberUtils.getCompactBigNumber(dysonEnergy),
                NumberUtils.getCompactBigNumber(externalEnergy), getConnectedPortCount(),
                target.starTier().level(), target.starTier().id(),
                target.starTier().minimumEarthMass().toEngineeringString(),
                target.starTier().maximumEarthMass().toEngineeringString(),
                stripFe(target.starTier().energyMultiplier().toCalculationString()),
                stripFe(target.starTier().energyCost().toCalculationString()), canChangeTier,
                running, canSelectNext, status, items.toString(),
                fluids.toString(), joinIds(resources.items()), joinIds(resources.fluids())), player);
    }

    private static String joinIds(java.util.List<net.minecraft.resources.ResourceLocation> ids) {
        return ids.stream().map(net.minecraft.resources.ResourceLocation::toString)
                .collect(java.util.stream.Collectors.joining(";"));
    }

    private static String stripFe(String display) {
        return display.endsWith(" FE") ? display.substring(0, display.length() - 3) : display;
    }

    public IItemHandler getItemHandler() { return outputItems; }
    public IItemHandler getOutputItems() { return outputItems; }
    public IFluidHandler getOutputFluids() { return outputFluids; }
    public AbsoluteMiningInventory getAbsoluteMiningInventory() { return inventory; }
    public AbsoluteInteger getStoredItemAmount() { return inventory.getTotalItemAmount(); }
    public AbsoluteInteger getStoredFluidAmount() { return inventory.getTotalFluidAmount(); }
    public IBigNumberEnergyStorage getBigEnergyStorage() { return bigEnergyStorage; }
    public IFNEnergyStorage getFluxEnergyStorage() { return fluxEnergyStorage; }
    public IEnergyStorage getForgeEnergyStorage() { return forgeEnergyStorage; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("laserPowerBigNumber", laserPower.input().toTag());
        tag.putString("laserPowerInput", laserPower.inputText());
        tag.putBoolean("running", running);
        tag.putLong("targetIndex", targetIndex);
        tag.putLong("cycle", cycle);
        tag.putInt("selectedStarTier", selectedStarTier.level());
        if (customStarEndEarthMass != null) {
            tag.putString("customStarEndEarthMass", customStarEndEarthMass.toEngineeringString());
        }
        tag.put("energy", energy.toTag());
        tag.put("miningInventoryAbsolute", inventory.save());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("laserPowerBigNumber")) {
            laserPower.restore(BigNumber.fromTag(tag.getCompound("laserPowerBigNumber")),
                    tag.getString("laserPowerInput"));
        } else {
            try {
                laserPower.setInput(tag.getString("laserPower"));
            } catch (IllegalArgumentException ignored) {
                laserPower.setInput("1");
            }
        }
        running = !tag.contains("running") || tag.getBoolean("running");
        targetIndex = tag.getLong("targetIndex");
        cycle = tag.getLong("cycle");
        selectedStarTier = MiningStarTier.byLevel(tag.contains("selectedStarTier")
                ? tag.getInt("selectedStarTier") : 1);
        customStarEndEarthMass = null;
        if (tag.contains("customStarEndEarthMass")) {
            try {
                customStarEndEarthMass = MiningStarTier.STAR_END.parseCustomEarthMass(
                        tag.getString("customStarEndEarthMass"));
            } catch (IllegalArgumentException ignored) {
                customStarEndEarthMass = null;
            }
        }
        if (tag.contains("energy")) energy = BigNumber.fromTag(tag.getCompound("energy"));
        if (energy.isImmutable()) energy = energy.deepCopy();
        if (tag.contains("miningInventoryAbsolute")) {
            inventory.load(tag.getCompound("miningInventoryAbsolute"));
        } else if (tag.contains("miningInventoryBigNumber")) {
            inventory.loadLegacyBigNumber(tag.getCompound("miningInventoryBigNumber"));
        } else {
            ItemStackHandler legacyItems = new ItemStackHandler(9);
            if (tag.contains("inventory")) legacyItems.deserializeNBT(provider, tag.getCompound("inventory"));
            inventory.migrateLegacyItems(legacyItems);
            if (tag.contains("fluid")) {
                net.neoforged.neoforge.fluids.capability.templates.FluidTank legacyFluid =
                        new net.neoforged.neoforge.fluids.capability.templates.FluidTank(10_000);
                legacyFluid.readFromNBT(provider, tag.getCompound("fluid"));
                inventory.migrateLegacyFluid(legacyFluid.getFluid());
            }
        }
    }

    private final class MiningBigEnergyStorage implements IBigNumberEnergyStorage {
        @Override public BigNumber receiveEnergy(BigNumber maximum, boolean simulate) {
            if (maximum == null || maximum.signum() <= 0) return new BigNumber(0);
            BigNumber accepted = maximum.deepCopy();
            if (!simulate) { energy.addEnergy(accepted.deepCopy()); setChanged(); }
            return accepted;
        }
        @Override public BigNumber extractEnergy(BigNumber maximum, boolean simulate) { return new BigNumber(0); }
        @Override public BigNumber getEnergyStored() { return energy.deepCopy(); }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    }

    private final class MiningFluxEnergyStorage implements IFNEnergyStorage {
        @Override public long receiveEnergyL(long maximum, boolean simulate) {
            if (maximum <= 0) return 0;
            if (!simulate) { energy.addEnergy(maximum); setChanged(); }
            return maximum;
        }
        @Override public long extractEnergyL(long maximum, boolean simulate) { return 0; }
        @Override public long getEnergyStoredL() { return energy.getEnergyStoredLong(); }
        @Override public long getMaxEnergyStoredL() { return Long.MAX_VALUE; }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    }

    private final class MiningForgeEnergyStorage implements IEnergyStorage {
        @Override public int receiveEnergy(int maximum, boolean simulate) {
            if (maximum <= 0) return 0;
            if (!simulate) { energy.addEnergy(maximum); setChanged(); }
            return maximum;
        }
        @Override public int extractEnergy(int maximum, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return (int) Math.min(Integer.MAX_VALUE, energy.getEnergyStoredLong()); }
        @Override public int getMaxEnergyStored() { return Integer.MAX_VALUE; }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    }

    private final class OutputItemHandler implements IItemHandler {
        @Override public int getSlots() { return inventory.getItemSlots(); }
        @Override public ItemStack getStackInSlot(int slot) { return inventory.getItemInSlot(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack extracted = inventory.extractItem(slot, amount, simulate);
            if (!simulate && !extracted.isEmpty()) markOutputInventoryChanged();
            return extracted;
        }
        @Override public int getSlotLimit(int slot) { return Integer.MAX_VALUE; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
    }

    private final class OutputFluidHandler implements IFluidHandler {
        @Override public int getTanks() { return inventory.getFluidTanks(); }
        @Override public FluidStack getFluidInTank(int tank) { return inventory.getFluidInTank(tank); }
        @Override public int getTankCapacity(int tank) { return Integer.MAX_VALUE; }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return false; }
        @Override public int fill(FluidStack resource, FluidAction action) { return 0; }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack drained = inventory.drainFluid(resource, action.simulate());
            if (action.execute() && !drained.isEmpty()) markOutputInventoryChanged();
            return drained;
        }
        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack drained = FluidStack.EMPTY;
            for (int tank = 0; tank < inventory.getStoredFluidTypes(); tank++) {
                drained = inventory.drainFluid(tank, maxDrain, action.simulate());
                if (!drained.isEmpty()) break;
            }
            if (action.execute() && !drained.isEmpty()) markOutputInventoryChanged();
            return drained;
        }
    }
}

