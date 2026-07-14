package dev.voidpulsar.lc_claim_economy.client;

import dev.voidpulsar.lc_claim_economy.network.SyncTownMenuPayload;
import dev.voidpulsar.lc_claim_economy.network.TownMenuEntry;
import dev.voidpulsar.lc_claim_economy.network.TownResidentEntry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public final class TownMenuState {
    private static UUID playerTownId;
    private static String currentChunkKey = "";
    private static UUID currentPlotTownId;
    private static String currentPlotTownName = "";
    private static boolean canCreateTown;
    private static List<TownResidentEntry> residents = List.of();
    private static List<TownMenuEntry> towns = List.of();

    private TownMenuState() {
    }

    public static void update(SyncTownMenuPayload payload) {
        playerTownId = payload.playerTownId();
        currentChunkKey = payload.currentChunkKey();
        currentPlotTownId = payload.currentPlotTownId();
        currentPlotTownName = payload.currentPlotTownName();
        canCreateTown = payload.canCreateTown();
        residents = List.copyOf(payload.residents());
        towns = List.copyOf(payload.towns());
    }

    @Nullable
    public static UUID playerTownId() {
        return playerTownId;
    }

    public static String currentChunkKey() {
        return currentChunkKey;
    }

    @Nullable
    public static UUID currentPlotTownId() {
        return currentPlotTownId;
    }

    public static String currentPlotTownName() {
        return currentPlotTownName;
    }

    public static boolean canCreateTown() {
        return canCreateTown;
    }

    public static List<TownResidentEntry> residents() {
        return residents;
    }

    public static List<TownMenuEntry> towns() {
        return towns;
    }

    @Nullable
    public static TownMenuEntry currentPlayerTown() {
        if (playerTownId == null) {
            return null;
        }
        for (TownMenuEntry town : towns) {
            if (playerTownId.equals(town.townId())) {
                return town;
            }
        }
        return null;
    }

    public static List<TownMenuEntry> invitedTowns() {
        return towns.stream().filter(TownMenuEntry::invited).toList();
    }
}