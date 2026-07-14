package dev.voidpulsar.lc_claim_economy.network;

import dev.voidpulsar.lc_claim_economy.LcClaimEconomy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

public record SyncTownMenuPayload(
        UUID playerTownId,
        String currentChunkKey,
        UUID currentPlotTownId,
        String currentPlotTownName,
        boolean canCreateTown,
        List<TownResidentEntry> residents,
        List<TownMenuEntry> towns
) implements CustomPacketPayload {
    public static final Type<SyncTownMenuPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LcClaimEconomy.MOD_ID, "sync_town_menu"));
    public static final StreamCodec<FriendlyByteBuf, SyncTownMenuPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeBoolean(payload.playerTownId != null);
                if (payload.playerTownId != null) {
                    buffer.writeUUID(payload.playerTownId);
                }
                buffer.writeUtf(payload.currentChunkKey);
                buffer.writeBoolean(payload.currentPlotTownId != null);
                if (payload.currentPlotTownId != null) {
                    buffer.writeUUID(payload.currentPlotTownId);
                    buffer.writeUtf(payload.currentPlotTownName == null ? "" : payload.currentPlotTownName);
                }
                buffer.writeBoolean(payload.canCreateTown);
                buffer.writeCollection(payload.residents, (buf, entry) -> {
                    buf.writeUUID(entry.playerId());
                    buf.writeUtf(entry.playerName());
                    buf.writeUtf(entry.rank());
                    buf.writeBoolean(entry.trustedOnCurrentPlot());
                    buf.writeBoolean(entry.deniedOnCurrentPlot());
                });
                buffer.writeCollection(payload.towns, (buf, entry) -> {
                    buf.writeUUID(entry.townId());
                    buf.writeUtf(entry.name());
                    buf.writeUtf(entry.mayorName());
                    buf.writeVarInt(entry.residentCount());
                    buf.writeVarInt(entry.plotCount());
                    buf.writeLong(entry.treasuryCopper());
                    buf.writeCollection(entry.bankLedger(), (ledgerBuf, ledgerEntry) -> {
                        ledgerBuf.writeUtf(ledgerEntry.action());
                        ledgerBuf.writeUtf(ledgerEntry.actorName());
                        ledgerBuf.writeLong(ledgerEntry.amountCopper());
                        ledgerBuf.writeLong(ledgerEntry.createdAt());
                    });
                    buf.writeBoolean(entry.publicAccess());
                    buf.writeUtf(entry.playerRank());
                    buf.writeBoolean(entry.playerTown());
                    buf.writeBoolean(entry.invited());
                    buf.writeBoolean(entry.canDepositTreasury());
                    buf.writeBoolean(entry.canWithdrawTreasury());
                    buf.writeBoolean(entry.canManage());
                    buf.writeBoolean(entry.canEditPlots());
                });
            },
            buffer -> {
                UUID playerTownId = buffer.readBoolean() ? buffer.readUUID() : null;
                String currentChunkKey = buffer.readUtf();
                UUID currentPlotTownId = null;
                String currentPlotTownName = "";
                if (buffer.readBoolean()) {
                    currentPlotTownId = buffer.readUUID();
                    currentPlotTownName = buffer.readUtf();
                }
                boolean canCreateTown = buffer.readBoolean();
                List<TownResidentEntry> residents = buffer.readCollection(java.util.ArrayList::new, buf -> new TownResidentEntry(
                    buf.readUUID(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readBoolean(),
                    buf.readBoolean()
                ));
                List<TownMenuEntry> towns = buffer.readCollection(java.util.ArrayList::new, buf -> new TownMenuEntry(
                        buf.readUUID(),
                        buf.readUtf(),
                        buf.readUtf(),
                        buf.readVarInt(),
                        buf.readVarInt(),
                        buf.readLong(),
                        buf.readCollection(java.util.ArrayList::new, ledgerBuf -> new dev.voidpulsar.lc_claim_economy.town.TownBankTransaction(
                            ledgerBuf.readUtf(),
                            ledgerBuf.readUtf(),
                            ledgerBuf.readLong(),
                            ledgerBuf.readLong()
                        )),
                        buf.readBoolean(),
                        buf.readUtf(),
                        buf.readBoolean(),
                        buf.readBoolean(),
                        buf.readBoolean(),
                        buf.readBoolean(),
                        buf.readBoolean(),
                        buf.readBoolean()
                ));
                    return new SyncTownMenuPayload(playerTownId, currentChunkKey, currentPlotTownId, currentPlotTownName, canCreateTown, residents, towns);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(SyncTownMenuPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> dev.voidpulsar.lc_claim_economy.client.TownMenuClientHandlers.handleTownMenuSync(payload));
    }
}