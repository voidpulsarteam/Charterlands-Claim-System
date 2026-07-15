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

import java.util.List;
import java.util.UUID;

public record RequestWarStatePayload() implements CustomPacketPayload {
    public static final Type<RequestWarStatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LcClaimEconomy.MOD_ID, "request_war_state"));
    public static final StreamCodec<FriendlyByteBuf, RequestWarStatePayload> STREAM_CODEC = StreamCodec.unit(new RequestWarStatePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(RequestWarStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            if (!ModCompat.isFtbAvailable()) {
                PacketDistributor.sendToPlayer(player, emptyPayload());
                return;
            }

            try {
                Class<?> syncClass = Class.forName("dev.voidpulsar.lc_claim_economy.service.WarStateSync");
                syncClass.getMethod("syncToPlayer", ServerPlayer.class).invoke(null, player);
            } catch (ReflectiveOperationException error) {
                LcClaimEconomy.LOGGER.warn("Failed to sync war state for {}", player.getGameProfile().getName(), error);
                PacketDistributor.sendToPlayer(player, emptyPayload());
            }
        });
    }

    private static SyncWarStatePayload emptyPayload() {
        return new SyncWarStatePayload(0L, 0L, 0L, 1.0D, List.of(), List.of(), List.of(), false);
    }
}
