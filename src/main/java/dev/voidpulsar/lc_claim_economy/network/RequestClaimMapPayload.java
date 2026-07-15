package dev.voidpulsar.lc_claim_economy.network;

import dev.voidpulsar.lc_claim_economy.service.ClaimMapSyncService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestClaimMapPayload() implements CustomPacketPayload {
    public static final Type<RequestClaimMapPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(dev.voidpulsar.lc_claim_economy.LcClaimEconomy.MOD_ID, "request_claim_map"));
    public static final StreamCodec<FriendlyByteBuf, RequestClaimMapPayload> STREAM_CODEC = StreamCodec.unit(new RequestClaimMapPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(RequestClaimMapPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                ClaimMapSyncService.syncToPlayer(player);
            }
        });
    }
}