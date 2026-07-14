package dev.voidpulsar.lc_claim_economy.town;

import java.util.List;
import java.util.UUID;

public record TownEntry(
        UUID townId,
        String name,
        UUID mayorId,
        UUID nationId,
        TownPermissionState permissionState,
        long createdAt,
        long spawnChunkKey,
        long bankCopper,
        List<TownBankTransaction> bankLedger
) {
        public TownEntry withPermissionState(TownPermissionState value) {
                return new TownEntry(townId, name, mayorId, nationId, value, createdAt, spawnChunkKey, bankCopper, bankLedger);
        }

        public TownEntry withBankCopper(long value) {
                return new TownEntry(townId, name, mayorId, nationId, permissionState, createdAt, spawnChunkKey, value, bankLedger);
        }

        public TownEntry withBankLedger(List<TownBankTransaction> value) {
                return new TownEntry(townId, name, mayorId, nationId, permissionState, createdAt, spawnChunkKey, bankCopper, List.copyOf(value));
        }
}