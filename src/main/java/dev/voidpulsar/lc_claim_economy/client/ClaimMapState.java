package dev.voidpulsar.lc_claim_economy.client;

import dev.voidpulsar.lc_claim_economy.network.ClaimMapEntry;
import dev.voidpulsar.lc_claim_economy.network.SyncClaimMapPayload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ClaimMapState {
    private static String dimensionId = "";
    private static int centerChunkX;
    private static int centerChunkZ;
    private static int radius;
    private static Map<Long, ClaimMapEntry> entries = Map.of();

    private ClaimMapState() {
    }

    public static void update(SyncClaimMapPayload payload) {
        dimensionId = payload.dimensionId();
        centerChunkX = payload.centerChunkX();
        centerChunkZ = payload.centerChunkZ();
        radius = payload.radius();
        Map<Long, ClaimMapEntry> next = new HashMap<>();
        for (ClaimMapEntry entry : payload.entries()) {
            next.put(key(entry.chunkX(), entry.chunkZ()), entry);
        }
        entries = Map.copyOf(next);
    }

    public static String dimensionId() {
        return dimensionId;
    }

    public static int centerChunkX() {
        return centerChunkX;
    }

    public static int centerChunkZ() {
        return centerChunkZ;
    }

    public static int radius() {
        return radius;
    }

    public static List<ClaimMapEntry> entries() {
        return entries.values().stream().toList();
    }

    public static Optional<ClaimMapEntry> entryAt(int chunkX, int chunkZ) {
        return Optional.ofNullable(entries.get(key(chunkX, chunkZ)));
    }

    private static long key(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) ^ (chunkZ & 0xffffffffL);
    }
}