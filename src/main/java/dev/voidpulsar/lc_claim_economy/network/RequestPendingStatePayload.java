package dev.voidpulsar.lc_claim_economy.network;

import dev.voidpulsar.lc_claim_economy.LcClaimEconomy;
import dev.voidpulsar.lc_claim_economy.compat.ModCompat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestPendingStatePayload() implements CustomPacketPayload {
    public static final Type<RequestPendingStatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LcClaimEconomy.MOD_ID, "request_pending_state"));
    public static final StreamCodec<FriendlyByteBuf, RequestPendingStatePayload> STREAM_CODEC = StreamCodec.unit(new RequestPendingStatePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(RequestPendingStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            if (!ModCompat.isFtbAvailable()) {
                PacketDistributor.sendToPlayer(player, SyncPendingStatePayload.EMPTY);
                return;
            }

            try {
                Class<?> syncClass = Class.forName("dev.voidpulsar.lc_claim_economy.network.PendingStateSync");
                syncClass.getMethod("syncToPlayer", ServerPlayer.class).invoke(null, player);
            } catch (ReflectiveOperationException error) {
                LcClaimEconomy.LOGGER.warn("Failed to sync pending state for {}", player.getGameProfile().getName(), error);
                PacketDistributor.sendToPlayer(player, SyncPendingStatePayload.EMPTY);
            }
        });
    }
}
