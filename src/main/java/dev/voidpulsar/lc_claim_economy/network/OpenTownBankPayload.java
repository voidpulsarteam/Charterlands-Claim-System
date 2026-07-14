package dev.voidpulsar.lc_claim_economy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenTownBankPayload() implements CustomPacketPayload {
    public static final Type<OpenTownBankPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(dev.voidpulsar.lc_claim_economy.LcClaimEconomy.MOD_ID, "open_town_bank"));
    public static final StreamCodec<FriendlyByteBuf, OpenTownBankPayload> STREAM_CODEC = StreamCodec.unit(new OpenTownBankPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(OpenTownBankPayload payload, IPayloadContext context) {
        context.enqueueWork(dev.voidpulsar.lc_claim_economy.client.TownMenuClientHandlers::openTownBankScreen);
    }
}