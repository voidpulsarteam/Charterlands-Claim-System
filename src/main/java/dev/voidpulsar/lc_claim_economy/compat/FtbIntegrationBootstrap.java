package dev.voidpulsar.lc_claim_economy.compat;

import dev.voidpulsar.lc_claim_economy.LcClaimEconomy;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Reflective bootstrap for FTB-only integrations so startup never hard-links
 * to FTB-backed classes when those mods are absent.
 */
public final class FtbIntegrationBootstrap {
    private FtbIntegrationBootstrap() {
    }

    public static void init() {
        if (!ModCompat.isFtbAvailable()) {
            LcClaimEconomy.LOGGER.info("FTB Chunks/Teams not detected - FTB integration bootstrap skipped.");
            return;
        }

        try {
            invokeNoArgsStatic("dev.voidpulsar.lc_claim_economy.teams.LandProperties", "register");

            NeoForge.EVENT_BUS.register(newInstance("dev.voidpulsar.lc_claim_economy.service.UpkeepService"));
            NeoForge.EVENT_BUS.register(newInstance("dev.voidpulsar.lc_claim_economy.handler.TeamLifecycleHandler"));
            NeoForge.EVENT_BUS.register(newInstance("dev.voidpulsar.lc_claim_economy.handler.TaxCollectorPlacementHandler"));

            // Constructor side effects register FTB claim/property event hooks.
            newInstance("dev.voidpulsar.lc_claim_economy.handler.ChunkClaimHandler");
            newInstance("dev.voidpulsar.lc_claim_economy.handler.TeamPropertyHandler");
            newInstance("dev.voidpulsar.lc_claim_economy.handler.ForceLoadHandler");

            NeoForge.EVENT_BUS.addListener(dev.voidpulsar.lc_claim_economy.command.UpkeepDetailsCommand::register);
            NeoForge.EVENT_BUS.addListener(dev.voidpulsar.lc_claim_economy.command.UpkeepPriorityCommand::register);
            NeoForge.EVENT_BUS.addListener(dev.voidpulsar.lc_claim_economy.command.SeedTestTeamsCommand::register);

            if (FMLEnvironment.dist == Dist.CLIENT) {
                newInstance("dev.voidpulsar.lc_claim_economy.client.ClientPendingRefreshHandler");
            }
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Failed to initialize FTB integration bootstrap", error);
        }
    }

    private static Object newInstance(String className) throws ReflectiveOperationException {
        Class<?> type = Class.forName(className);
        return type.getDeclaredConstructor().newInstance();
    }

    private static void invokeNoArgsStatic(String className, String methodName) throws ReflectiveOperationException {
        Class<?> type = Class.forName(className);
        type.getMethod(methodName).invoke(null);
    }
}
