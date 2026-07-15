package dev.voidpulsar.lc_claim_economy.client;

import dev.voidpulsar.lc_claim_economy.client.gui.ClaimMapScreen;
import dev.voidpulsar.lc_claim_economy.network.SyncClaimMapPayload;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;

public final class ClaimMapClientHandlers {
    private ClaimMapClientHandlers() {
    }

    public static void openClaimMapScreen() {
        new ClaimMapScreen().openGui();
    }

    public static void handleClaimMapSync(SyncClaimMapPayload payload) {
        ClaimMapState.update(payload);
        if (ClientUtils.getCurrentGuiAs(ClaimMapScreen.class) != null) {
            ClaimMapScreen.refreshIfOpen();
        }
    }
}