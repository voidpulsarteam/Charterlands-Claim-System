package dev.voidpulsar.lc_claim_economy.service;

import dev.voidpulsar.lc_claim_economy.network.ClaimMapEntry;
import dev.voidpulsar.lc_claim_economy.network.SyncClaimMapPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public final class ClaimMapSyncService {
    private static final int MAP_RADIUS = 6;

    private ClaimMapSyncService() {
    }

    public static void syncToPlayer(ServerPlayer player) {
        if (player == null) {
            return;
        }
        PacketDistributor.sendToPlayer(player, buildPayload(player));
    }

    public static SyncClaimMapPayload buildPayload(ServerPlayer player) {
        int centerX = player.chunkPosition().x;
        int centerZ = player.chunkPosition().z;
        List<ClaimMapEntry> entries = new ArrayList<>();
        String dimensionId = player.level().dimension().location().toString();

        for (int dz = -MAP_RADIUS; dz <= MAP_RADIUS; dz++) {
            for (int dx = -MAP_RADIUS; dx <= MAP_RADIUS; dx++) {
                int chunkX = centerX + dx;
                int chunkZ = centerZ + dz;
                NativeClaimService.ChunkClaimInfo info = NativeClaimService.query(
                        player.server,
                        player.level().dimension().location(),
                        chunkX,
                        chunkZ
                );
                entries.add(new ClaimMapEntry(chunkX, chunkZ, info.claimed(), info.land(), info.ownerName()));
            }
        }

        return new SyncClaimMapPayload(dimensionId, centerX, centerZ, MAP_RADIUS, entries);
    }
}