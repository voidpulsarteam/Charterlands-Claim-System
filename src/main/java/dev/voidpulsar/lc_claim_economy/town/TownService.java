package dev.voidpulsar.lc_claim_economy.town;

import dev.ftb.mods.ftbchunks.api.Protection;
import dev.voidpulsar.lc_claim_economy.data.ChunkPosKey;
import dev.voidpulsar.lc_claim_economy.service.ChunkPermissionFlags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nullable;
import java.util.UUID;

public final class TownService {
    private TownService() {
    }

    public static TownRegistryData registry(MinecraftServer server) {
        return TownRegistryData.get(server);
    }

    public static UUID createTown(MinecraftServer server, String name, UUID mayorId) {
        return registry(server).createTown(name, mayorId);
    }

    public static boolean claimPlot(MinecraftServer server, UUID townId, String chunkKey) {
        return registry(server).claimPlot(townId, chunkKey);
    }

    public static boolean unclaimPlot(MinecraftServer server, String chunkKey) {
        return registry(server).unclaimPlot(chunkKey);
    }

    public static boolean isClaimed(MinecraftServer server, String chunkKey) {
        return registry(server).isClaimed(chunkKey);
    }

    @Nullable
    public static UUID ownerOf(MinecraftServer server, String chunkKey) {
        return registry(server).ownerOf(chunkKey);
    }

    public static TownRank rankOf(MinecraftServer server, UUID townId, UUID playerId) {
        return registry(server).rankOf(townId, playerId);
    }

    public static boolean canInteract(MinecraftServer server, ServerPlayer player, String chunkKey, Protection protection) {
        UUID townId = ownerOf(server, chunkKey);
        if (townId == null) {
            return false;
        }
        return registry(server).canInteract(townId, chunkKey, player.getUUID(), ChunkPermissionFlags.fromProtection(protection));
    }

    public static void setRank(MinecraftServer server, UUID townId, UUID playerId, TownRank rank) {
        registry(server).setRank(townId, playerId, rank);
    }

    public static String currentChunkKey(ServerPlayer player) {
        ChunkPos chunkPos = player.chunkPosition();
        ResourceLocation dimension = player.level().dimension().location();
        return ChunkPosKey.encode(dimension, chunkPos.x, chunkPos.z);
    }

    public static boolean claimCurrentPlot(MinecraftServer server, ServerPlayer player, UUID townId) {
        return claimPlot(server, townId, currentChunkKey(player));
    }

    public static boolean unclaimCurrentPlot(MinecraftServer server, ServerPlayer player) {
        return unclaimPlot(server, currentChunkKey(player));
    }

    public static boolean togglePublicAccess(MinecraftServer server, UUID townId) {
        TownRegistryData registry = registry(server);
        TownEntry entry = registry.town(townId);
        if (entry == null) {
            return false;
        }
        return registry.setPermissionState(townId, entry.permissionState().withPublicAccess(!entry.permissionState().publicAccess()));
    }
}