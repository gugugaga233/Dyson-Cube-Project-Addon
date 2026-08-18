package com.gugugaga233.dysoncubeprojectaddon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

class DCPAttachmentsTest {

    @Test
    void compressionLevelAtNetworkLimitIsAccepted() {
        BigInteger level = BigInteger.ONE.shiftLeft(4096 * 8 - 2);

        assertEquals(4096, CompressionLevelEncoding.encode(level).length);
    }

    @Test
    void oversizedCompressionLevelIsRejectedBeforeEncoding() {
        BigInteger level = BigInteger.ONE.shiftLeft(4096 * 8);

        assertThrows(IllegalArgumentException.class, () -> CompressionLevelEncoding.encode(level));
    }

    @Test
    void nonPositiveCompressionLevelIsRejectedBeforeEncoding() {
        assertThrows(IllegalArgumentException.class, () -> CompressionLevelEncoding.encode(BigInteger.ZERO));
    }

    @Test
    void dysonCellNetworkViewDropsOversizedExactContents() {
        CompoundTag contents = new CompoundTag();
        ListTag entries = new ListTag();
        CompoundTag entry = new CompoundTag();
        entry.putByteArray("oversizedTestData", new byte[2_100_000]);
        entries.add(entry);
        contents.put("entries", entries);
        contents.putInt("typeCount", 256);
        contents.putString("totalDisplay", "1E100000");

        CompoundTag network = DCPAttachments.networkCellContents(contents);

        assertFalse(network.contains("entries"));
        assertEquals(256, network.getInt("typeCount"));
        assertEquals("1E100000", network.getString("totalDisplay"));
        assertTrue(network.getBoolean("networkSummary"));
    }
}

