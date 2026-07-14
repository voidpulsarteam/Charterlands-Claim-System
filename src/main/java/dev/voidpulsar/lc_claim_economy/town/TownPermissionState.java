package dev.voidpulsar.lc_claim_economy.town;

public record TownPermissionState(
        boolean allowBuild,
        boolean allowDestroy,
        boolean allowSwitch,
        boolean allowItemUse,
        boolean allowMobGrief,
        boolean allowExplosions,
        boolean allowFire,
        boolean allowPvp,
        boolean publicAccess
) {
    public static TownPermissionState defaultTown() {
        return new TownPermissionState(false, false, false, false, false, false, false, false, false);
    }

    public static TownPermissionState wilderness() {
        return new TownPermissionState(true, true, true, true, true, true, true, true, true);
    }

    public TownPermissionState withPublicAccess(boolean value) {
        return new TownPermissionState(
                allowBuild,
                allowDestroy,
                allowSwitch,
                allowItemUse,
                allowMobGrief,
                allowExplosions,
                allowFire,
                allowPvp,
                value
        );
    }
}