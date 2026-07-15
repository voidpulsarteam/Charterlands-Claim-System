package dev.voidpulsar.lc_claim_economy.service;

import dev.voidpulsar.lc_claim_economy.LcClaimEconomy;
import dev.voidpulsar.lc_claim_economy.bank.BankAccountHelper;
import dev.voidpulsar.lc_claim_economy.network.TownMenuActionPayload;
import dev.voidpulsar.lc_claim_economy.town.TownRank;
import dev.voidpulsar.lc_claim_economy.town.TownRegistryData;
import dev.voidpulsar.lc_claim_economy.town.TownService;
import dev.voidpulsar.lc_claim_economy.util.MoneyMessageUtil;
import dev.voidpulsar.lc_claim_economy.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

public final class TownMenuService {
    private TownMenuService() {
    }

    public static void handleAction(ServerPlayer player, TownMenuActionPayload.Action action, UUID townId, String text, String targetRef, String rankText) {
        TownRegistryData registry = TownRegistryData.get(player.server);
        boolean changed = false;
        switch (action) {
            case CREATE_TOWN -> {
                String name = text == null ? "" : text.trim();
                if (!name.isEmpty() && registry.townForPlayer(player.getUUID()).isEmpty()) {
                    TownService.createTown(player.server, name, player.getUUID());
                    changed = true;
                    TownMenuSyncService.syncToPlayer(player);
                    player.displayClientMessage(Component.literal("Town created: " + name), false);
                }
            }
            case INVITE_PLAYER -> {
                if (townId != null && registry.rankOf(townId, player.getUUID()).canManageTown()) {
                    Optional<UUID> target = resolvePlayerRef(player, targetRef);
                    if (target.isPresent() && registry.townForPlayer(target.get()).isEmpty()) {
                        changed = registry.invitePlayer(townId, target.get());
                    }
                }
            }
            case ACCEPT_INVITE -> {
                if (townId != null && registry.hasInvitation(townId, player.getUUID())) {
                    changed = registry.acceptInvitation(townId, player.getUUID());
                }
            }
            case DECLINE_INVITE -> {
                if (townId != null && registry.hasInvitation(townId, player.getUUID())) {
                    changed = registry.declineInvitation(townId, player.getUUID());
                }
            }
            case DISBAND_TOWN -> {
                if (townId != null && registry.town(townId) != null && registry.rankOf(townId, player.getUUID()).canDisbandTown()) {
                    changed = registry.deleteTown(townId);
                    if (changed) {
                        player.displayClientMessage(Component.literal("Town disbanded."), false);
                    }
                }
            }
            case DEPOSIT_TREASURY -> {
                if (townId != null && registry.town(townId) != null && registry.rankOf(townId, player.getUUID()).canDepositTreasury()) {
                    long amount = parseCopperAmount(text);
                    if (amount > 0) {
                        MoneyValue value = MoneyUtil.fromCopper(amount);
                        if (!value.isEmpty()) {
                            IBankAccount account = BankAccountHelper.getAccountForPlayer(player.server, player);
                            if (account.getMoneyStorage().containsValue(value)) {
                                account.withdrawMoney(value);
                                changed = registry.depositTreasury(townId, amount, player.getGameProfile().getName());
                            } else {
                                player.displayClientMessage(Component.translatable("message.lc_claim_economy.insufficient_funds", MoneyMessageUtil.formatValue(value), MoneyMessageUtil.formatBalance(account)), false);
                            }
                        }
                    }
                }
            }
            case WITHDRAW_TREASURY -> {
                if (townId != null && registry.town(townId) != null && registry.rankOf(townId, player.getUUID()).canWithdrawTreasury()) {
                    long amount = parseCopperAmount(text);
                    if (amount > 0 && registry.withdrawTreasury(townId, amount, player.getGameProfile().getName())) {
                        MoneyValue value = MoneyUtil.fromCopper(amount);
                        BankAccountHelper.getAccountForPlayer(player.server, player).depositMoney(value);
                        changed = true;
                    }
                }
            }
            case TRUST_CURRENT_PLOT -> {
                if (townId != null && registry.rankOf(townId, player.getUUID()).canEditPlots()) {
                    Optional<UUID> target = resolvePlayerRef(player, targetRef);
                    if (target.isPresent()) {
                        changed = registry.trustPlot(townId, TownService.currentChunkKey(player), target.get());
                    }
                }
            }
            case UNTRUST_CURRENT_PLOT -> {
                if (townId != null && registry.rankOf(townId, player.getUUID()).canEditPlots()) {
                    Optional<UUID> target = resolvePlayerRef(player, targetRef);
                    if (target.isPresent()) {
                        changed = registry.untrustPlot(townId, TownService.currentChunkKey(player), target.get());
                    }
                }
            }
            case DENY_CURRENT_PLOT -> {
                if (townId != null && registry.rankOf(townId, player.getUUID()).canEditPlots()) {
                    Optional<UUID> target = resolvePlayerRef(player, targetRef);
                    if (target.isPresent()) {
                        changed = registry.denyPlot(townId, TownService.currentChunkKey(player), target.get());
                    }
                }
            }
            case UNDENY_CURRENT_PLOT -> {
                if (townId != null && registry.rankOf(townId, player.getUUID()).canEditPlots()) {
                    Optional<UUID> target = resolvePlayerRef(player, targetRef);
                    if (target.isPresent()) {
                        changed = registry.undenyPlot(townId, TownService.currentChunkKey(player), target.get());
                    }
                }
            }
            case CLAIM_CURRENT_PLOT -> {
                if (townId != null && registry.rankOf(townId, player.getUUID()).canEditPlots()) {
                    UUID owner = registry.ownerOf(TownService.currentChunkKey(player));
                    if (owner == null || owner.equals(townId)) {
                        changed = TownService.claimCurrentPlot(player.server, player, townId);
                    } else {
                        player.displayClientMessage(Component.literal("This plot is already claimed by another town."), false);
                    }
                }
            }
            case UNCLAIM_CURRENT_PLOT -> {
                if (townId != null && registry.rankOf(townId, player.getUUID()).canEditPlots()) {
                    String currentKey = TownService.currentChunkKey(player);
                    UUID owner = registry.ownerOf(currentKey);
                    if (townId.equals(owner)) {
                        changed = TownService.unclaimCurrentPlot(player.server, player);
                    }
                }
            }
            case TOGGLE_PUBLIC_ACCESS -> {
                if (townId != null && registry.rankOf(townId, player.getUUID()).canManageTown()) {
                    changed = TownService.togglePublicAccess(player.server, townId);
                }
            }
            case SET_RESIDENT_RANK -> {
                if (townId != null && registry.rankOf(townId, player.getUUID()).canManageTown()) {
                    Optional<UUID> target = resolvePlayerRef(player, targetRef);
                    if (target.isPresent() && registry.membersOf(townId).containsKey(target.get())) {
                        TownRank nextRank = parseRank(rankText);
                        if (nextRank != null && nextRank != TownRank.MAYOR) {
                            registry.setRank(townId, target.get(), nextRank);
                            changed = true;
                        }
                    }
                }
            }
            case REMOVE_RESIDENT -> {
                if (townId != null && registry.rankOf(townId, player.getUUID()).canManageTown()) {
                    Optional<UUID> target = resolvePlayerRef(player, targetRef);
                    if (target.isPresent()) {
                        changed = registry.removeMember(townId, target.get());
                    }
                }
            }
        }

        if (changed) {
            TownMenuSyncService.broadcast(player.server);
            player.displayClientMessage(Component.literal("Town menu updated."), false);
        }
    }

    private static Optional<UUID> resolvePlayerRef(ServerPlayer player, String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String trimmed = value.trim();
        try {
            return Optional.of(UUID.fromString(trimmed));
        } catch (IllegalArgumentException ignored) {
        }
        ServerPlayer online = player.server.getPlayerList().getPlayerByName(trimmed);
        if (online != null) {
            return Optional.of(online.getUUID());
        }
        return Optional.empty();
    }

    private static TownRank parseRank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return TownRank.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static long parseCopperAmount(String value) {
        if (value == null || value.isBlank()) {
            return -1L;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return -1L;
        }
    }
}