package dev.voidpulsar.lc_claim_economy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenTownMenuPayload() implements CustomPacketPayload {
    public static final Type<OpenTownMenuPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(dev.voidpulsar.lc_claim_economy.LcClaimEconomy.MOD_ID, "open_town_menu"));
    public static final StreamCodec<FriendlyByteBuf, OpenTownMenuPayload> STREAM_CODEC = StreamCodec.unit(new OpenTownMenuPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(OpenTownMenuPayload payload, IPayloadContext context) {
        context.enqueueWork(dev.voidpulsar.lc_claim_economy.client.TownMenuClientHandlers::openTownMenuScreen);
    }
}