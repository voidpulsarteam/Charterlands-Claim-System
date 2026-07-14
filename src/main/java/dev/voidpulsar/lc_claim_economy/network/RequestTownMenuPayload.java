package dev.voidpulsar.lc_claim_economy.network;

import dev.voidpulsar.lc_claim_economy.service.TownMenuSyncService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestTownMenuPayload() implements CustomPacketPayload {
    public static final Type<RequestTownMenuPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(dev.voidpulsar.lc_claim_economy.LcClaimEconomy.MOD_ID, "request_town_menu"));
    public static final StreamCodec<FriendlyByteBuf, RequestTownMenuPayload> STREAM_CODEC = StreamCodec.unit(new RequestTownMenuPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(RequestTownMenuPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                TownMenuSyncService.syncToPlayer(player);
            }
        });
    }
}