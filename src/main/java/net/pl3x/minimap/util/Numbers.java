package net.pl3x.minimap.util;

public final class Numbers {
    private Numbers() {
    }

    public static int regionToBlock(int n) {
        return n << 9;
    }

    public static int blockToRegion(int n) {
        return n >> 9;
    }

    public static int regionToChunk(int n) {
        return n << 5;
    }

    public static int chunkToRegion(int n) {
        return n >> 5;
    }

    public static int chunkToBlock(int n) {
        return n << 4;
    }

    public static int blockToChunk(int n) {
        return n >> 4;
    }

    public static float normalizeDegrees(float degrees) {
        return (degrees - 180F) % 360F;
    }
}
