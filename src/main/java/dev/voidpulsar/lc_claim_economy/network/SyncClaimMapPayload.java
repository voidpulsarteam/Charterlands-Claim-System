package dev.voidpulsar.lc_claim_economy.network;

import dev.voidpulsar.lc_claim_economy.LcClaimEconomy;
import dev.voidpulsar.lc_claim_economy.client.ClaimMapClientHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record SyncClaimMapPayload(
        String dimensionId,
        int centerChunkX,
        int centerChunkZ,
        int radius,
        List<ClaimMapEntry> entries
) implements CustomPacketPayload {
    public static final Type<SyncClaimMapPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LcClaimEconomy.MOD_ID, "sync_claim_map"));
    public static final StreamCodec<FriendlyByteBuf, SyncClaimMapPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeUtf(payload.dimensionId);
                buffer.writeVarInt(payload.centerChunkX);
                buffer.writeVarInt(payload.centerChunkZ);
                buffer.writeVarInt(payload.radius);
                buffer.writeCollection(payload.entries, (buf, entry) -> {
                    buf.writeVarInt(entry.chunkX());
                    buf.writeVarInt(entry.chunkZ());
                    buf.writeBoolean(entry.claimed());
                    buf.writeBoolean(entry.land());
                    buf.writeUtf(entry.ownerName() == null ? "" : entry.ownerName());
                });
            },
            buffer -> new SyncClaimMapPayload(
                    buffer.readUtf(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readCollection(java.util.ArrayList::new, buf -> new ClaimMapEntry(
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readBoolean(),
                            buf.readBoolean(),
                            buf.readUtf()
                    ))
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(SyncClaimMapPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClaimMapClientHandlers.handleClaimMapSync(payload));
    }
}