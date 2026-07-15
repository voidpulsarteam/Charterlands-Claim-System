package dev.voidpulsar.lc_claim_economy.mixin.client;

import net.minecraft.client.Minecraft;
import dev.voidpulsar.lc_claim_economy.client.gui.ClaimMapScreen;
import dev.voidpulsar.lc_claim_economy.client.ClaimMapClientHandlers;
import dev.voidpulsar.lc_claim_economy.client.gui.TownMenuScreen;
import dev.voidpulsar.lc_claim_economy.network.RequestTownMenuPayload;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenTownButtonMixin {
    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    protected int imageWidth;

    @Shadow
    protected abstract <T extends GuiEventListener> T addRenderableWidget(T widget);

    @Inject(method = "init", at = @At("TAIL"))
    private void lcClaimEconomy$addTownButton(CallbackInfo ci) {
        InventoryScreen screen = (InventoryScreen) (Object) this;
        addRenderableWidget(Button.builder(Component.literal("Map"), button -> ClaimMapClientHandlers.openClaimMapScreen())
                .bounds(leftPos + imageWidth - 100, topPos + 4, 48, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Town"), button -> {
            Minecraft.getInstance().setScreen(new TownMenuScreen());
            PacketDistributor.sendToServer(new RequestTownMenuPayload());
        }).bounds(leftPos + imageWidth - 52, topPos + 4, 48, 20).build());
    }
}