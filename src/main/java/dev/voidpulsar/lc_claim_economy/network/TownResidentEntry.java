package dev.voidpulsar.lc_claim_economy.network;

import java.util.UUID;

public record TownResidentEntry(UUID playerId, String playerName, String rank, boolean trustedOnCurrentPlot, boolean deniedOnCurrentPlot) {
}