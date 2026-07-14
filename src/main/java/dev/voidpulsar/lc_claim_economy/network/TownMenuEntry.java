package dev.voidpulsar.lc_claim_economy.network;

import dev.voidpulsar.lc_claim_economy.town.TownBankTransaction;

import java.util.List;
import java.util.UUID;

public record TownMenuEntry(
        UUID townId,
        String name,
        String mayorName,
        int residentCount,
        int plotCount,
        long treasuryCopper,
        List<TownBankTransaction> bankLedger,
        boolean publicAccess,
        String playerRank,
        boolean playerTown,
        boolean invited,
        boolean canDepositTreasury,
        boolean canWithdrawTreasury,
        boolean canManage,
        boolean canEditPlots
) {
}