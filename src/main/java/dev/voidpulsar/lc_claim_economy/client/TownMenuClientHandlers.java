package dev.voidpulsar.lc_claim_economy.client;

import dev.voidpulsar.lc_claim_economy.client.gui.TownMenuScreen;
import dev.voidpulsar.lc_claim_economy.client.gui.TownBankScreen;
import dev.voidpulsar.lc_claim_economy.network.RequestTownMenuPayload;
import dev.voidpulsar.lc_claim_economy.network.SyncTownMenuPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;

public final class TownMenuClientHandlers {
    private TownMenuClientHandlers() {
    }

    public static void openTownMenuScreen() {
        Minecraft.getInstance().setScreen(new TownMenuScreen());
        PacketDistributor.sendToServer(new RequestTownMenuPayload());
    }

    public static void openTownBankScreen() {
        Minecraft.getInstance().setScreen(new TownBankScreen());
    }

    public static void handleTownMenuSync(SyncTownMenuPayload payload) {
        TownMenuState.update(payload);
        if (Minecraft.getInstance().screen instanceof TownMenuScreen townMenuScreen) {
            Minecraft.getInstance().setScreen(new TownMenuScreen(townMenuScreen.section()));
        }
    }
}