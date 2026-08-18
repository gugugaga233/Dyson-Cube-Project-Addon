package com.gugugaga233.dysoncubeprojectaddon;

import java.math.BigInteger;

final class CompressionLevelEncoding {
    static final int MAX_NETWORK_BYTES = 4096;

    private CompressionLevelEncoding() {
    }

    static byte[] encode(BigInteger level) {
        if (level == null || level.signum() < 1) {
            throw new IllegalArgumentException("Compression level must be at least 1");
        }
        byte[] bytes = level.toByteArray();
        if (bytes.length > MAX_NETWORK_BYTES) {
            throw new IllegalArgumentException("Compression level is too large to synchronize");
        }
        return bytes;
    }
}

