package dev.voidpulsar.lc_claim_economy.compat;

import net.neoforged.fml.loading.LoadingModList;

/**
 * Central detection of the FTB claim backend, so bootstrap code can avoid
 * touching FTB classes unless the required mods are present.
 * <p>
 * All FTB-specific registrations (mixins, event handlers, commands) must be
 * gated behind {@link #isFtbAvailable()} before touching any
 * {@code dev.ftb.mods.*} class. This is required because merely classloading
 * a class that references a missing mod's types in method signatures or
 * constructors can throw {@link NoClassDefFoundError} even if that specific
 * code path is never reached.
 * <p>
 * This deliberately uses {@link LoadingModList} rather than
 * {@code net.neoforged.fml.ModList}. {@code ModList} is only populated
 * later, during actual mod construction - it is NOT safe to query from a
 * Mixin config plugin (see {@code LcFtbMixinPlugin}), which runs during the
 * much earlier game bootstrap/mod-discovery phase, well before {@code
 * ModList} exists. {@code LoadingModList} is populated during mod
 * discovery itself and is safe to use at any point, including from Mixin
 * plugins, which is why it's used here universally rather than only in the
 * Mixin-plugin-specific code path.
 */
public final class ModCompat {
    private static final String FTB_LIBRARY_MOD_ID = "ftblibrary";
    private static final String FTB_TEAMS_MOD_ID = "ftbteams";
    private static final String FTB_CHUNKS_MOD_ID = "ftbchunks";

    private ModCompat() {
    }

    public static boolean isFtbAvailable() {
        return isLoaded(FTB_LIBRARY_MOD_ID) && isLoaded(FTB_TEAMS_MOD_ID) && isLoaded(FTB_CHUNKS_MOD_ID);
    }

    private static boolean isLoaded(String modId) {
        return LoadingModList.get().getModFileById(modId) != null;
    }
}

