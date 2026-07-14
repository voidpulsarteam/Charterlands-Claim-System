package dev.voidpulsar.lc_claim_economy.service;

import dev.voidpulsar.lc_claim_economy.network.SyncTownMenuPayload;
import dev.voidpulsar.lc_claim_economy.network.TownMenuEntry;
import dev.voidpulsar.lc_claim_economy.network.TownResidentEntry;
import dev.voidpulsar.lc_claim_economy.town.TownEntry;
import dev.voidpulsar.lc_claim_economy.town.TownBankTransaction;
import dev.voidpulsar.lc_claim_economy.town.TownRank;
import dev.voidpulsar.lc_claim_economy.town.TownRegistryData;
import dev.voidpulsar.lc_claim_economy.town.TownService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class TownMenuSyncService {
    private TownMenuSyncService() {
    }

    public static void syncToPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, buildPayload(player));
    }

    public static void broadcast(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncToPlayer(player);
        }
    }

    public static SyncTownMenuPayload buildPayload(ServerPlayer player) {
        TownRegistryData registry = TownRegistryData.get(player.server);
        Optional<UUID> playerTown = registry.townForPlayer(player.getUUID());
        String currentChunkKey = TownService.currentChunkKey(player);
        UUID currentPlotTownId = registry.ownerOf(currentChunkKey);
        String currentPlotTownName = "Wilderness";
        if (currentPlotTownId != null) {
            TownEntry plotTown = registry.town(currentPlotTownId);
            if (plotTown != null) {
                currentPlotTownName = plotTown.name();
            }
        }
        List<TownResidentEntry> residents = new ArrayList<>();
        if (playerTown.isPresent()) {
            UUID townId = playerTown.get();
            boolean currentPlotOwnedByTown = townId.equals(currentPlotTownId);
            for (Map.Entry<UUID, TownRank> member : registry.membersOf(townId).entrySet()) {
                String name = player.server.getProfileCache() != null && player.server.getProfileCache().get(member.getKey()).isPresent()
                        ? player.server.getProfileCache().get(member.getKey()).get().getName()
                        : member.getKey().toString();
                residents.add(new TownResidentEntry(
                        member.getKey(),
                        name,
                        member.getValue().name(),
                    currentPlotOwnedByTown && registry.hasPlotTrust(currentChunkKey, member.getKey()),
                    currentPlotOwnedByTown && registry.hasPlotDeny(currentChunkKey, member.getKey())
                ));
            }
            residents.sort(Comparator.comparing(TownResidentEntry::playerName, String.CASE_INSENSITIVE_ORDER));
        }
        List<TownMenuEntry> towns = new ArrayList<>();
        for (Map.Entry<UUID, TownEntry> entry : registry.townsView().entrySet()) {
            UUID townId = entry.getKey();
            TownEntry town = entry.getValue();
            TownRank rank = registry.rankOf(townId, player.getUUID());
                boolean isPlayerTown = playerTown.map(townId::equals).orElse(false);
            towns.add(new TownMenuEntry(
                    townId,
                    town.name(),
                    player.server.getProfileCache() != null && player.server.getProfileCache().get(town.mayorId()).isPresent()
                            ? player.server.getProfileCache().get(town.mayorId()).get().getName()
                            : town.mayorId().toString(),
                    registry.residentCount(townId),
                    registry.plotCount(townId),
                        town.bankCopper(),
                    isPlayerTown ? registry.bankLedger(townId) : List.<TownBankTransaction>of(),
                    town.permissionState().publicAccess(),
                    rank.name(),
                    isPlayerTown,
                    registry.hasInvitation(townId, player.getUUID()),
                    rank.canDepositTreasury(),
                    rank.canWithdrawTreasury(),
                    rank.canManageTown(),
                    rank.canEditPlots()
            ));
        }
        towns.sort(Comparator.comparing(TownMenuEntry::name, String.CASE_INSENSITIVE_ORDER));
        return new SyncTownMenuPayload(playerTown.orElse(null), currentChunkKey, currentPlotTownId, currentPlotTownName, playerTown.isEmpty(), residents, towns);
    }
}