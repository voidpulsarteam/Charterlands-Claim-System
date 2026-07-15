package dev.voidpulsar.lc_claim_economy.network;

import dev.voidpulsar.lc_claim_economy.service.MapChunkClaimService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClaimChunkFromMapPayload(String chunkKey) implements CustomPacketPayload {
    public static final Type<ClaimChunkFromMapPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(dev.voidpulsar.lc_claim_economy.LcClaimEconomy.MOD_ID, "claim_chunk_from_map"));
    public static final StreamCodec<FriendlyByteBuf, ClaimChunkFromMapPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeUtf(payload.chunkKey == null ? "" : payload.chunkKey),
            buffer -> new ClaimChunkFromMapPayload(buffer.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(ClaimChunkFromMapPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                MapChunkClaimService.claim(player, payload.chunkKey());
            }
        });
    }
}