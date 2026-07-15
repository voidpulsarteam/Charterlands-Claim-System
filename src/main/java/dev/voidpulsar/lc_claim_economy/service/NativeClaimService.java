package dev.voidpulsar.lc_claim_economy.service;

import dev.voidpulsar.lc_claim_economy.data.ChunkPosKey;
import dev.voidpulsar.lc_claim_economy.town.TownEntry;
import dev.voidpulsar.lc_claim_economy.town.TownRegistryData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * Native claim backend that stores ownership in the town registry instead of
 * FTB Chunks/Teams APIs.
 */
public final class NativeClaimService {
    private NativeClaimService() {
    }

    public enum ClaimResult {
        SUCCESS,
        NO_TOWN,
        DENIED,
        ALREADY_CLAIMED,
        INVALID_CHUNK
    }

    public record ChunkClaimInfo(boolean claimed, boolean land, String ownerName) {
        public static final ChunkClaimInfo WILDERNESS = new ChunkClaimInfo(false, false, "");
    }

    public static ChunkClaimInfo query(MinecraftServer server, ResourceLocation dimension, int chunkX, int chunkZ) {
        return query(server, ChunkPosKey.encode(dimension, chunkX, chunkZ));
    }

    public static ChunkClaimInfo query(MinecraftServer server, String chunkKey) {
        if (server == null || chunkKey == null || chunkKey.isBlank()) {
            return ChunkClaimInfo.WILDERNESS;
        }

        TownRegistryData registry = TownRegistryData.get(server);
        UUID townId = registry.ownerOf(chunkKey);
        if (townId == null) {
            return ChunkClaimInfo.WILDERNESS;
        }

        TownEntry town = registry.town(townId);
        String owner = town == null ? "Unknown" : town.name();
        // Native system currently treats all atlas claims as land territory.
        return new ChunkClaimInfo(true, true, owner);
    }

    public static ClaimResult claimForPlayer(ServerPlayer player, String chunkKey) {
        if (player == null || chunkKey == null || chunkKey.isBlank()) {
            return ClaimResult.INVALID_CHUNK;
        }

        try {
            ChunkPosKey.dimension(chunkKey);
            ChunkPosKey.x(chunkKey);
            ChunkPosKey.z(chunkKey);
        } catch (RuntimeException ex) {
            return ClaimResult.INVALID_CHUNK;
        }

        TownRegistryData registry = TownRegistryData.get(player.server);
        Optional<UUID> townIdOpt = registry.townForPlayer(player.getUUID());
        if (townIdOpt.isEmpty()) {
            return ClaimResult.NO_TOWN;
        }

        UUID townId = townIdOpt.get();
        if (!registry.rankOf(townId, player.getUUID()).canEditPlots()) {
            return ClaimResult.DENIED;
        }

        UUID owner = registry.ownerOf(chunkKey);
        if (owner != null && !owner.equals(townId)) {
            return ClaimResult.ALREADY_CLAIMED;
        }

        return registry.claimPlot(townId, chunkKey) ? ClaimResult.SUCCESS : ClaimResult.DENIED;
    }

    public static int claimedCountForPlayer(@Nullable ServerPlayer player) {
        if (player == null) {
            return 0;
        }

        TownRegistryData registry = TownRegistryData.get(player.server);
        Optional<UUID> townId = registry.townForPlayer(player.getUUID());
        return townId.map(registry::plotCount).orElse(0);
    }
}