package dev.voidpulsar.lc_claim_economy.town;

public enum TownRank {
    MAYOR,
    ASSISTANT,
    RESIDENT,
    OUTSIDER;

    public boolean canManageTown() {
        return this == MAYOR || this == ASSISTANT;
    }

    public boolean canDisbandTown() {
        return this == MAYOR;
    }

    public boolean canDepositTreasury() {
        return this == MAYOR || this == ASSISTANT || this == RESIDENT;
    }

    public boolean canWithdrawTreasury() {
        return this == MAYOR;
    }

    public boolean canEditPlots() {
        return this == MAYOR || this == ASSISTANT || this == RESIDENT;
    }

    public boolean canUseTrustedLand() {
        return this != OUTSIDER;
    }
}