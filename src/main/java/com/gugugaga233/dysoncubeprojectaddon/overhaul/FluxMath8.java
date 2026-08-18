package com.gugugaga233.dysoncubeprojectaddon.overhaul;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import net.minecraft.nbt.CompoundTag;
import sonar.fluxnetworks.api.energy.AbsoluteInteger;
import sonar.fluxnetworks.api.energy.BigNumber;

/** Compatibility arithmetic for the public Flux Networks 8.0.0 API. */
public final class FluxMath8 {
    private static final int LAYERS = 4;
    private static final int DIGITS_PER_LAYER = 65_536;
    private static final int BITS_PER_DIGIT = 63;
    private static final int ABSOLUTE_DIGIT_CAPACITY = LAYERS * DIGITS_PER_LAYER;
    private static final int MAX_EXACT_BIG_NUMBER_BITS = 4_096;
    private static final double LOG10_TWO = Math.log10(2.0D);
    private static final double ABSOLUTE_DIGIT_BASE = Math.scalb(1.0D, BITS_PER_DIGIT);
    private static final MathContext BIG_NUMBER_CONTEXT =
            new MathContext(BigNumber.PRECISION_THRESHOLD, RoundingMode.HALF_EVEN);
    private static final int APPROXIMATION_DIGITS = 6;
    private static final String COMPACT_ABSOLUTE_VERSION = "compactAbsoluteIntegerVersion";
    private static final int COMPACT_ABSOLUTE_FORMAT = 1;
    private static final BigInteger DIGIT_MASK = BigInteger.ONE.shiftLeft(BITS_PER_DIGIT)
            .subtract(BigInteger.ONE);
    private static final AbsoluteInteger LONG_MAX = AbsoluteInteger.parse(Long.toString(Long.MAX_VALUE));
    private static final AbsoluteInteger INT_MAX = AbsoluteInteger.parse(Integer.toString(Integer.MAX_VALUE));
    private static final MethodHandle ABSOLUTE_GET = privateMethod("get", int.class,
            MethodType.methodType(long.class, int.class));
    private static final MethodHandle ABSOLUTE_SET = privateMethod("set", int.class,
            MethodType.methodType(void.class, int.class, long.class));
    private static final MethodHandle ABSOLUTE_HIGHEST_POSITION = privateFieldGetter(
            "highestPosition", int.class);
    private static final MethodHandle ABSOLUTE_LAYERS = privateFieldGetter("layers", long[][].class);

    private FluxMath8() {
    }

    public static BigInteger toBigInteger(AbsoluteInteger value) {
        if (value == null || value.isZero()) return BigInteger.ZERO;
        CompoundTag root = value.toTag();
        BigInteger result = BigInteger.ZERO;
        for (int layer = 0; layer < LAYERS; layer++) {
            CompoundTag digits = root.getCompound("layer" + layer);
            for (String key : digits.getAllKeys()) {
                int index = Integer.parseInt(key);
                long digit = digits.getLong(key);
                if (digit == 0L) continue;
                int position = Math.addExact(Math.multiplyExact(layer, DIGITS_PER_LAYER), index);
                result = result.add(BigInteger.valueOf(digit)
                        .shiftLeft(Math.multiplyExact(position, BITS_PER_DIGIT)));
            }
        }
        return result;
    }

    /** Stores only allocated binary digits instead of Flux's large named-key tag. */
    public static CompoundTag toCompactTag(AbsoluteInteger value) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(COMPACT_ABSOLUTE_VERSION, COMPACT_ABSOLUTE_FORMAT);
        if (value == null || value.isZero()) {
            tag.putInt("highestPosition", -1);
            tag.putByteArray("digits", new byte[0]);
            return tag;
        }

        int highest = highestPosition(value);
        int byteCount = Math.multiplyExact(highest + 1, Long.BYTES);
        ByteBuffer buffer = ByteBuffer.allocate(byteCount).order(ByteOrder.LITTLE_ENDIAN);
        for (int position = 0; position <= highest; position++) {
            buffer.putLong(getDigit(value, position));
        }
        tag.putInt("highestPosition", highest);
        tag.putByteArray("digits", buffer.array());
        return tag;
    }

    /** Reads the compact format and remains compatible with Flux's native tag. */
    public static AbsoluteInteger fromCompactTag(CompoundTag tag) {
        if (tag == null) return new AbsoluteInteger();
        if (!tag.contains(COMPACT_ABSOLUTE_VERSION)
                || tag.getInt(COMPACT_ABSOLUTE_VERSION) != COMPACT_ABSOLUTE_FORMAT) {
            return AbsoluteInteger.fromTag(tag);
        }

        int highest = tag.getInt("highestPosition");
        byte[] bytes = tag.getByteArray("digits");
        if (highest < -1 || highest >= ABSOLUTE_DIGIT_CAPACITY) {
            throw new IllegalArgumentException("invalid compact absolute-integer position");
        }
        int expectedLength = Math.multiplyExact(highest + 1, Long.BYTES);
        if (bytes.length != expectedLength) {
            throw new IllegalArgumentException("invalid compact absolute-integer payload");
        }

        AbsoluteInteger result = new AbsoluteInteger();
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        for (int position = 0; position <= highest; position++) {
            long digit = buffer.getLong();
            if (digit < 0L) throw new IllegalArgumentException("invalid compact absolute-integer digit");
            if (digit != 0L) setDigit(result, position, digit);
        }
        return result;
    }

    public static AbsoluteInteger fromBigInteger(BigInteger value) {
        if (value == null || value.signum() == 0) return new AbsoluteInteger();
        if (value.signum() < 0) throw new IllegalArgumentException("absolute integer cannot be negative");
        if (value.bitLength() > ABSOLUTE_DIGIT_CAPACITY * BITS_PER_DIGIT) {
            throw new IllegalArgumentException("energy level exceeds array capacity");
        }

        AbsoluteInteger result = new AbsoluteInteger();
        byte[] magnitude = value.toByteArray();
        int remainingBits = value.bitLength();
        int position = 0;
        int digitBits = 0;
        long digit = 0L;

        for (int byteIndex = magnitude.length - 1; remainingBits > 0; byteIndex--) {
            int source = magnitude[byteIndex] & 0xFF;
            int sourceBits = Math.min(Byte.SIZE, remainingBits);
            remainingBits -= sourceBits;

            while (sourceBits > 0) {
                int chunkBits = Math.min(sourceBits, BITS_PER_DIGIT - digitBits);
                int mask = (1 << chunkBits) - 1;
                digit |= (long) (source & mask) << digitBits;
                source >>>= chunkBits;
                sourceBits -= chunkBits;
                digitBits += chunkBits;

                if (digitBits == BITS_PER_DIGIT) {
                    setDigit(result, position++, digit);
                    digit = 0L;
                    digitBits = 0;
                }
            }
        }
        if (digitBits > 0) {
            setDigit(result, position, digit);
        }
        return result;
    }

    public static AbsoluteInteger subtract(AbsoluteInteger value, AbsoluteInteger amount) {
        if (value == null || value.isZero()) {
            if (amount == null || amount.isZero()) return new AbsoluteInteger();
            throw new ArithmeticException("absolute integer underflow");
        }
        if (amount == null || amount.isZero()) return value.copy();
        if (value.compareTo(amount) < 0) throw new ArithmeticException("absolute integer underflow");
        AbsoluteInteger result = value.copy();
        int sourceHighest = highestPosition(amount);
        long borrow = 0L;
        for (int position = 0; position <= sourceHighest || borrow != 0L; position++) {
            long current = getDigit(result, position);
            long incoming = position <= sourceHighest ? getDigit(amount, position) : 0L;
            long next;
            if (borrow == 0L) {
                if (current >= incoming) {
                    next = current - incoming;
                } else {
                    next = Long.MAX_VALUE - (incoming - current) + 1L;
                    borrow = 1L;
                }
            } else if (current > incoming) {
                next = current - incoming - 1L;
                borrow = 0L;
            } else {
                next = Long.MAX_VALUE - (incoming - current);
            }
            setDigit(result, position, next);
        }
        return result;
    }

    public static AbsoluteInteger multiply(AbsoluteInteger value, long factor) {
        if (factor < 0L) throw new IllegalArgumentException("factor must not be negative");
        if (value == null || value.isZero() || factor == 0L) return new AbsoluteInteger();
        if (factor == 1L) return value.copy();

        AbsoluteInteger result = new AbsoluteInteger();
        long carry = 0L;
        int highest = highestPosition(value);
        for (int position = 0; position <= highest; position++) {
            long digit = getDigit(value, position);
            long low = digit * factor;
            long high = Math.multiplyHigh(digit, factor);
            long sum = low + carry;
            if (Long.compareUnsigned(sum, low) < 0) high++;

            setDigit(result, position, sum & Long.MAX_VALUE);
            carry = (high << 1) + (sum >>> BITS_PER_DIGIT);
        }
        if (carry != 0L) {
            if (highest + 1 >= ABSOLUTE_DIGIT_CAPACITY) {
                throw new IllegalArgumentException("absolute integer exceeds array capacity");
            }
            setDigit(result, highest + 1, carry);
        }
        return result;
    }

    /**
     * Adds only through the incoming value's highest digit and a possible carry.
     * Flux 8's public add scans through the larger operand, which is catastrophic
     * when a modest batch is credited to an already enormous counter.
     */
    public static void addInPlace(AbsoluteInteger target, AbsoluteInteger amount) {
        if (target == null) throw new IllegalArgumentException("target must not be null");
        if (amount == null || amount.isZero()) return;
        int sourceHighest = highestPosition(amount);
        long carry = 0L;
        for (int position = 0; position <= sourceHighest || carry != 0L; position++) {
            if (position >= ABSOLUTE_DIGIT_CAPACITY) {
                throw new IllegalArgumentException("absolute integer exceeds array capacity");
            }
            long current = getDigit(target, position);
            long incoming = position <= sourceHighest ? getDigit(amount, position) : 0L;
            long available = Long.MAX_VALUE - current;
            long next;
            if (carry == 0L) {
                if (incoming <= available) {
                    next = current + incoming;
                } else {
                    next = incoming - available - 1L;
                    carry = 1L;
                }
            } else if (incoming < available) {
                next = current + incoming + 1L;
                carry = 0L;
            } else {
                next = incoming - available;
                carry = 1L;
            }
            setDigit(target, position, next);
        }
    }

    public static Division divideAndRemainder(AbsoluteInteger value, long divisor) {
        if (divisor <= 0L) throw new IllegalArgumentException("divisor must be positive");
        if (value == null || value.isZero()) return new Division(new AbsoluteInteger(), 0L);
        if (divisor == 1L) return new Division(value.copy(), 0L);

        AbsoluteInteger quotient = new AbsoluteInteger();
        long remainder = 0L;
        long halfDivisor = divisor >>> 1;
        int oddDivisor = (int) (divisor & 1L);
        for (int position = highestPosition(value); position >= 0; position--) {
            long digit = getDigit(value, position);
            long quotientDigit = 0L;
            for (int bit = BITS_PER_DIGIT - 1; bit >= 0; bit--) {
                int incoming = (int) ((digit >>> bit) & 1L);
                boolean subtract = remainder > halfDivisor
                        || (remainder == halfDivisor && incoming >= oddDivisor);
                if (subtract) {
                    remainder = (remainder - halfDivisor) * 2L + incoming - oddDivisor;
                    quotientDigit |= 1L << bit;
                } else {
                    remainder = remainder * 2L + incoming;
                }
            }
            if (quotientDigit != 0L) setDigit(quotient, position, quotientDigit);
        }
        return new Division(quotient, remainder);
    }

    /**
     * Scales an exponent by a proper fraction. Huge exponents retain their
     * leading digits only: lower digits cannot affect the retained prefix and
     * are below BigNumber's useful display precision.
     */
    public static ScaledDivision multiplyDivideExponent(AbsoluteInteger value,
                                                         long multiplier,
                                                         long divisor) {
        if (multiplier < 0L || divisor <= 0L || multiplier >= divisor) {
            throw new IllegalArgumentException("expected 0 <= multiplier < divisor");
        }
        if (value == null || value.isZero() || multiplier == 0L) {
            return new ScaledDivision(new AbsoluteInteger(), 0L, true);
        }

        int highest = highestPosition(value);
        int bitLength = Math.addExact(Math.multiplyExact(highest, BITS_PER_DIGIT),
                Long.SIZE - Long.numberOfLeadingZeros(getDigit(value, highest)));
        if (bitLength <= MAX_EXACT_BIG_NUMBER_BITS) {
            Division exact = divideAndRemainder(multiply(value, multiplier), divisor);
            return new ScaledDivision(exact.quotient(), exact.remainder(), true);
        }

        int lowestIncluded = Math.max(0, highest - APPROXIMATION_DIGITS + 1);
        BigInteger leading = BigInteger.ZERO;
        for (int position = highest; position >= lowestIncluded; position--) {
            leading = leading.shiftLeft(BITS_PER_DIGIT)
                    .add(BigInteger.valueOf(getDigit(value, position)));
        }
        BigInteger scaledLeading = leading.multiply(BigInteger.valueOf(multiplier))
                .divide(BigInteger.valueOf(divisor));
        AbsoluteInteger quotient = new AbsoluteInteger();
        int outputPosition = lowestIncluded;
        while (scaledLeading.signum() != 0) {
            setDigit(quotient, outputPosition++, scaledLeading.and(DIGIT_MASK).longValueExact());
            scaledLeading = scaledLeading.shiftRight(BITS_PER_DIGIT);
        }
        return new ScaledDivision(quotient, 0L, false);
    }

    public static long toLongSaturated(AbsoluteInteger value) {
        if (value == null || value.isZero()) return 0L;
        return value.compareTo(LONG_MAX) > 0 ? Long.MAX_VALUE : value.getLowDigit();
    }

    public static int toIntSaturated(AbsoluteInteger value) {
        if (value == null || value.isZero()) return 0;
        return value.compareTo(INT_MAX) > 0 ? Integer.MAX_VALUE : (int) value.getLowDigit();
    }

    /** Returns floor(log10(value)) from Flux's sparse base-2^63 representation. */
    public static int decimalOrder(AbsoluteInteger value) {
        if (value == null || value.isZero()) return 0;
        int highestPosition = highestPosition(value);
        long highestDigit = getDigit(value, highestPosition);
        long nextDigit = highestPosition == 0 ? 0L : getDigit(value, highestPosition - 1);
        double coefficient = highestDigit + nextDigit / ABSOLUTE_DIGIT_BASE;
        double logarithm = Math.log10(coefficient)
                + (double) highestPosition * BITS_PER_DIGIT * LOG10_TWO;
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, (long) Math.floor(logarithm)));
    }

    public static long quoteChunk(AbsoluteInteger value, long maximum) {
        if (value == null || maximum <= 0L) return 0L;
        return Math.min(maximum, toLongSaturated(value));
    }

    public static AbsoluteInteger extractChunk(AbsoluteInteger value, long maximum) {
        long extracted = quoteChunk(value, maximum);
        if (extracted == 0L) return value.copy();
        return subtractSmall(value, extracted);
    }

    /**
     * Flux 8.0.0 exposes addition but not subtraction on AbsoluteInteger. For
     * the small primitive-sized transfers used by item/fluid handlers, mutate
     * a copy directly by borrowing across its base-2^63 digits. This avoids
     * materializing a huge BigInteger on every capability probe or transfer.
     */
    private static AbsoluteInteger subtractSmall(AbsoluteInteger value, long amount) {
        AbsoluteInteger result = value.copy();
        long digit = getDigit(result, 0);
        if (digit >= amount) {
            setDigit(result, 0, digit - amount);
            return result;
        }

        setDigit(result, 0, Long.MAX_VALUE - (amount - digit - 1L));
        for (int position = 1; position < LAYERS * DIGITS_PER_LAYER; position++) {
            digit = getDigit(result, position);
            if (digit == 0L) {
                setDigit(result, position, Long.MAX_VALUE);
            } else {
                setDigit(result, position, digit - 1L);
                return result;
            }
        }
        throw new ArithmeticException("absolute integer subtraction underflow");
    }

    private static MethodHandle privateMethod(String name, Class<?> returnType, MethodType type) {
        try {
            return MethodHandles.privateLookupIn(AbsoluteInteger.class, MethodHandles.lookup())
                    .findVirtual(AbsoluteInteger.class, name, type);
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static MethodHandle privateFieldGetter(String name, Class<?> type) {
        try {
            return MethodHandles.privateLookupIn(AbsoluteInteger.class, MethodHandles.lookup())
                    .findGetter(AbsoluteInteger.class, name, type);
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static long getDigit(AbsoluteInteger value, int position) {
        try {
            return (long) ABSOLUTE_GET.invokeExact(value, position);
        } catch (Throwable exception) {
            throw new IllegalStateException("Unable to read Flux AbsoluteInteger digit", exception);
        }
    }

    private static void setDigit(AbsoluteInteger value, int position, long digit) {
        try {
            ABSOLUTE_SET.invokeExact(value, position, digit);
        } catch (Throwable exception) {
            throw new IllegalStateException("Unable to write Flux AbsoluteInteger digit", exception);
        }
    }

    private static int highestPosition(AbsoluteInteger value) {
        try {
            return (int) ABSOLUTE_HIGHEST_POSITION.invokeExact(value);
        } catch (Throwable exception) {
            throw new IllegalStateException("Unable to read Flux AbsoluteInteger highest position", exception);
        }
    }

    private static long[][] layers(AbsoluteInteger value) {
        try {
            return (long[][]) ABSOLUTE_LAYERS.invokeExact(value);
        } catch (Throwable exception) {
            throw new IllegalStateException("Unable to read Flux AbsoluteInteger layers", exception);
        }
    }

    public static BigNumber toBigNumber(AbsoluteInteger value) {
        if (value == null || value.isZero()) return new BigNumber(0L);
        int highest = highestPosition(value);
        long highestDigit = getDigit(value, highest);
        int bitLength = Math.addExact(Math.multiplyExact(highest, BITS_PER_DIGIT),
                Long.SIZE - Long.numberOfLeadingZeros(highestDigit));
        if (bitLength <= MAX_EXACT_BIG_NUMBER_BITS) {
            return BigNumber.valueOf(toBigInteger(value));
        }

        // Keep far more leading bits than BigNumber can retain, then round to its precision.
        // This makes conversion depend on BigNumber precision instead of the stored magnitude.
        int lowestIncluded = Math.max(0, highest - APPROXIMATION_DIGITS + 1);
        BigInteger leading = BigInteger.ZERO;
        for (int position = highest; position >= lowestIncluded; position--) {
            leading = leading.shiftLeft(BITS_PER_DIGIT)
                    .add(BigInteger.valueOf(getDigit(value, position)));
        }
        int binaryShift = Math.multiplyExact(lowestIncluded, BITS_PER_DIGIT);
        double decimalLogarithm = Math.log10(leading.doubleValue())
                + binaryShift * LOG10_TWO;
        long decimalExponent = (long) Math.floor(decimalLogarithm);
        double normalized = Math.pow(10.0D, decimalLogarithm - decimalExponent);
        if (normalized >= 9.999_999_999D) {
            normalized = 1.0D;
            decimalExponent++;
        }
        BigDecimal coefficient = BigDecimal.valueOf(normalized)
                .round(BIG_NUMBER_CONTEXT)
                .stripTrailingZeros();
        if (coefficient.compareTo(BigDecimal.TEN) >= 0) {
            coefficient = coefficient.movePointLeft(1).stripTrailingZeros();
            decimalExponent++;
        }
        return BigNumber.scientific(coefficient.toPlainString(), Long.toString(decimalExponent));
    }

    public static void addExponent(BigNumber value, AbsoluteInteger exponent) {
        if (value == null || value.isZero() || exponent == null || exponent.isZero()) return;
        long[][] sourceLayers = layers(exponent);
        int highest = highestPosition(exponent);
        int highestLayer = highest / DIGITS_PER_LAYER;
        int highestIndex = highest % DIGITS_PER_LAYER;
        for (int layer = 0; layer <= highestLayer; layer++) {
            long[] source = sourceLayers[layer];
            if (source == null) continue;
            int endExclusive = layer == highestLayer
                    ? Math.min(source.length, highestIndex + 1) : source.length;
            for (int index = 0; index < endExclusive; index++) {
                long digit = source[index];
                if (digit != 0L) addExponentDigit(value, layer, index, digit);
            }
        }
    }

    private static void addExponentDigit(BigNumber value, int layer, int index, long incoming) {
        int targetLayer = layer;
        int targetIndex = index;
        long amount = incoming;
        while (amount != 0L) {
            if (targetLayer >= BigNumber.EXPONENT_LAYER_COUNT) {
                throw new IllegalArgumentException("BigNumber exponent exceeds array capacity");
            }
            long current = getExponentDigit(value, targetLayer, targetIndex);
            long available = Long.MAX_VALUE - current;
            if (amount <= available) {
                setExponentDigit(value, targetLayer, targetIndex, current + amount);
                return;
            }
            setExponentDigit(value, targetLayer, targetIndex, amount - available - 1L);
            amount = 1L;
            if (++targetIndex == DIGITS_PER_LAYER) {
                targetIndex = 0;
                targetLayer++;
            }
        }
    }

    private static long getExponentDigit(BigNumber value, int layer, int index) {
        return switch (layer) {
            case 0 -> value.getM(index);
            case 1 -> value.getK(index);
            case 2 -> value.getDigits(index);
            case 3 -> value.getMaxCounter(index);
            case 4 -> value.getMaxPlus1(index);
            case 5 -> value.getMaxPlus2(index);
            default -> throw new IllegalArgumentException("invalid BigNumber exponent layer");
        };
    }

    private static void setExponentDigit(BigNumber value, int layer, int index, long digit) {
        switch (layer) {
            case 0 -> value.setM(index, digit);
            case 1 -> value.setK(index, digit);
            case 2 -> value.setDigits(index, digit);
            case 3 -> value.setMaxCounter(index, digit);
            case 4 -> value.setMaxPlus1(index, digit);
            case 5 -> value.setMaxPlus2(index, digit);
            default -> throw new IllegalArgumentException("invalid BigNumber exponent layer");
        }
    }

    public static BigNumber scientific(String coefficient, AbsoluteInteger exponent) {
        BigNumber result = new BigNumber(coefficient);
        addExponent(result, exponent);
        return result;
    }

    public static AbsoluteInteger floorDivide(BigNumber value, long divisor) {
        if (divisor <= 0L) throw new IllegalArgumentException("divisor must be positive");
        if (value.signum() < 0) throw new IllegalStateException("negative value cannot become absolute");
        if (value.isZero()) return new AbsoluteInteger();
        BigInteger exponent = value.toBigIntegerExponent();
        if (exponent.bitLength() > 31) {
            throw new ArithmeticException("integer needs more addressable JVM digits than can be allocated");
        }
        BigInteger integer = value.getCoefficient().scaleByPowerOfTen(exponent.intValueExact()).toBigInteger();
        return fromBigInteger(integer.divide(BigInteger.valueOf(divisor)));
    }

    public static double toDoubleSaturated(BigNumber value) {
        if (value == null || value.isZero()) return 0.0D;
        var exact = value.getExactEnergy();
        if (exact.isPresent()) {
            double converted = exact.get().doubleValue();
            return Double.isInfinite(converted)
                    ? Math.copySign(Double.MAX_VALUE, converted) : converted;
        }
        BigInteger exponent = value.toBigIntegerExponent();
        if (exponent.compareTo(BigInteger.valueOf(308L)) > 0) {
            return value.signum() < 0 ? -Double.MAX_VALUE : Double.MAX_VALUE;
        }
        double converted = value.getCoefficient()
                .scaleByPowerOfTen(exponent.intValue())
                .doubleValue();
        return Double.isInfinite(converted)
                ? Math.copySign(Double.MAX_VALUE, converted) : converted;
    }

    public record Division(AbsoluteInteger quotient, long remainder) {
    }

    public record ScaledDivision(AbsoluteInteger quotient, long remainder, boolean exact) {
    }
}
