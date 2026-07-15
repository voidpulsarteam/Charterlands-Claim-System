package dev.voidpulsar.lc_claim_economy.network;

public record ClaimMapEntry(
        int chunkX,
        int chunkZ,
        boolean claimed,
        boolean land,
        String ownerName
) {
}