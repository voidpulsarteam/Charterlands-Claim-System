package dev.voidpulsar.lc_claim_economy;

import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Validates that companion mods match the versions this hook was built against.
 */
public final class ModCompatibility {
    /** Must match {@code lightmanscurrency_version} in gradle.properties. */
    public static final String REQUIRED_LIGHTMANS_CURRENCY_VERSION = "1.21-2.3.0.5";

    private ModCompatibility() {
    }

    public static void validateOrThrow() {
        List<String> errors = new ArrayList<>();
        requireExactVersion("lightmanscurrency", "Lightman's Currency", REQUIRED_LIGHTMANS_CURRENCY_VERSION, errors);

        if (errors.isEmpty()) {
            return;
        }

        for (String error : errors) {
            LcClaimEconomy.LOGGER.error(error);
        }

        throw new IllegalStateException(
                "Lightman's Currency: Claim Economy cannot load with incompatible mod versions. "
                        + "Details: "
                        + String.join(" | ", errors)
        );
    }

    private static void requireExactVersion(
            String modId,
            String displayName,
            String requiredVersion,
            List<String> errorsOut
    ) {
        Optional<String> installed = ModList.get()
                .getModContainerById(modId)
                .map(container -> container.getModInfo().getVersion().toString());
        if (installed.isEmpty()) {
            errorsOut.add(displayName + " (" + modId + ") is missing.");
            return;
        }

        if (!requiredVersion.equals(installed.get())) {
            errorsOut.add(displayName + " version mismatch: required " + requiredVersion + ", found " + installed.get() + ".");
        }
    }
}
