package dev.voidpulsar.lc_claim_economy.service;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class MapChunkClaimService {
    private MapChunkClaimService() {
    }

    public static void claim(ServerPlayer player, String chunkKey) {
        if (chunkKey == null || chunkKey.isBlank()) {
            return;
        }
        NativeClaimService.ClaimResult result = NativeClaimService.claimForPlayer(player, chunkKey);
        if (result == NativeClaimService.ClaimResult.SUCCESS) {
            ClaimMapSyncService.syncToPlayer(player);
            return;
        }

        switch (result) {
            case NO_TOWN -> player.displayClientMessage(Component.translatable("message.lc_claim_economy.map_claim_no_town"), false);
            case ALREADY_CLAIMED -> player.displayClientMessage(Component.translatable("message.lc_claim_economy.map_claim_failed"), false);
            case DENIED -> player.displayClientMessage(Component.translatable("message.lc_claim_economy.map_claim_denied"), false);
            case INVALID_CHUNK -> player.displayClientMessage(Component.translatable("message.lc_claim_economy.map_claim_failed"), false);
            default -> player.displayClientMessage(Component.translatable("message.lc_claim_economy.map_claim_failed"), false);
        }
    }
}