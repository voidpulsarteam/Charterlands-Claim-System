package dev.voidpulsar.lc_claim_economy.network;

import dev.voidpulsar.lc_claim_economy.service.TownMenuService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record TownMenuActionPayload(Action action, UUID townId, String text, String targetRef, String rank) implements CustomPacketPayload {
    public enum Action {
        CREATE_TOWN,
        INVITE_PLAYER,
        ACCEPT_INVITE,
        DECLINE_INVITE,
        DEPOSIT_TREASURY,
        WITHDRAW_TREASURY,
        TRUST_CURRENT_PLOT,
        UNTRUST_CURRENT_PLOT,
        DENY_CURRENT_PLOT,
        UNDENY_CURRENT_PLOT,
        CLAIM_CURRENT_PLOT,
        UNCLAIM_CURRENT_PLOT,
        TOGGLE_PUBLIC_ACCESS,
        SET_RESIDENT_RANK,
        REMOVE_RESIDENT
    }

    public static final Type<TownMenuActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(dev.voidpulsar.lc_claim_economy.LcClaimEconomy.MOD_ID, "town_menu_action"));
    public static final StreamCodec<FriendlyByteBuf, TownMenuActionPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeEnum(payload.action);
                buffer.writeBoolean(payload.townId != null);
                if (payload.townId != null) {
                    buffer.writeUUID(payload.townId);
                }
                buffer.writeUtf(payload.text == null ? "" : payload.text);
                buffer.writeUtf(payload.targetRef == null ? "" : payload.targetRef);
                buffer.writeUtf(payload.rank == null ? "" : payload.rank);
            },
            buffer -> new TownMenuActionPayload(
                    buffer.readEnum(Action.class),
                    buffer.readBoolean() ? buffer.readUUID() : null,
                    buffer.readUtf(),
                    buffer.readUtf(),
                    buffer.readUtf()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(TownMenuActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            TownMenuService.handleAction(player, payload.action(), payload.townId(), payload.text(), payload.targetRef(), payload.rank());
        });
    }
}