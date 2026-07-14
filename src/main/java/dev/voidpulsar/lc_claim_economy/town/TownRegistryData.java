package dev.voidpulsar.lc_claim_economy.town;

import dev.voidpulsar.lc_claim_economy.LcClaimEconomy;
import dev.voidpulsar.lc_claim_economy.service.ChunkPermissionFlags;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class TownRegistryData extends SavedData {
    private static final String DATA_NAME = LcClaimEconomy.MOD_ID + "_towns";

    private final TownRegistry registry = new TownRegistry();

    public static TownRegistryData get(net.minecraft.server.MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(TownRegistryData::new, TownRegistryData::load),
                DATA_NAME
        );
    }

    private static TownRegistryData load(CompoundTag tag, HolderLookup.Provider lookup) {
        TownRegistryData data = new TownRegistryData();
        data.registry.clear();
        if (tag.contains("Towns", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Towns", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entryTag = list.getCompound(i);
                UUID townId = entryTag.getUUID("TownId");
                UUID mayorId = entryTag.getUUID("MayorId");
                UUID nationId = entryTag.contains("NationId", Tag.TAG_INT_ARRAY) ? net.minecraft.nbt.NbtUtils.loadUUID(entryTag.get("NationId")) : null;
                String name = entryTag.getString("Name");
                long createdAt = entryTag.getLong("CreatedAt");
                long spawnChunkKey = entryTag.getLong("SpawnChunkKey");
                long bankCopper = entryTag.getLong("BankCopper");
                TownPermissionState permissionState = loadPermissionState(entryTag.getCompound("Permissions"));
                List<TownBankTransaction> ledger = new java.util.ArrayList<>();
                if (entryTag.contains("BankLedger", Tag.TAG_LIST)) {
                    ListTag ledgerTag = entryTag.getList("BankLedger", Tag.TAG_COMPOUND);
                    for (int j = 0; j < ledgerTag.size(); j++) {
                        CompoundTag txTag = ledgerTag.getCompound(j);
                        ledger.add(new TownBankTransaction(
                                txTag.getString("Action"),
                                txTag.getString("ActorName"),
                                txTag.getLong("AmountCopper"),
                                txTag.getLong("CreatedAt")
                        ));
                    }
                }
                data.registry.putTown(new TownEntry(townId, name, mayorId, nationId, permissionState, createdAt, spawnChunkKey, bankCopper, List.copyOf(ledger)));
            }
        }

        if (tag.contains("Plots", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Plots", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entryTag = list.getCompound(i);
                UUID townId = entryTag.getUUID("TownId");
                String chunkKey = entryTag.getString("ChunkKey");
                if (!chunkKey.isEmpty()) {
                    data.registry.putPlotOwner(chunkKey, townId);
                }
            }
        }

        if (tag.contains("PlotTrusts", Tag.TAG_LIST)) {
            ListTag list = tag.getList("PlotTrusts", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entryTag = list.getCompound(i);
                String chunkKey = entryTag.getString("ChunkKey");
                UUID playerId = entryTag.getUUID("PlayerId");
                if (!chunkKey.isEmpty()) {
                    data.registry.putPlotTrust(chunkKey, playerId);
                }
            }
        }

        if (tag.contains("PlotDenies", Tag.TAG_LIST)) {
            ListTag list = tag.getList("PlotDenies", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entryTag = list.getCompound(i);
                String chunkKey = entryTag.getString("ChunkKey");
                UUID playerId = entryTag.getUUID("PlayerId");
                if (!chunkKey.isEmpty()) {
                    data.registry.putPlotDeny(chunkKey, playerId);
                }
            }
        }

        if (tag.contains("Memberships", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Memberships", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entryTag = list.getCompound(i);
                UUID townId = entryTag.getUUID("TownId");
                UUID playerId = entryTag.getUUID("PlayerId");
                TownRank rank = TownRank.valueOf(entryTag.getString("Rank"));
                data.registry.putMembership(townId, playerId, rank);
            }
        }

        if (tag.contains("Invitations", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Invitations", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entryTag = list.getCompound(i);
                UUID townId = entryTag.getUUID("TownId");
                UUID playerId = entryTag.getUUID("PlayerId");
                data.registry.putInvitation(townId, playerId);
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookup) {
        ListTag townsTag = new ListTag();
        for (TownEntry entry : registry.townsView().values()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("TownId", entry.townId());
            entryTag.putString("Name", entry.name());
            entryTag.putUUID("MayorId", entry.mayorId());
            if (entry.nationId() != null) {
                entryTag.put("NationId", net.minecraft.nbt.NbtUtils.createUUID(entry.nationId()));
            }
            entryTag.putLong("CreatedAt", entry.createdAt());
            entryTag.putLong("SpawnChunkKey", entry.spawnChunkKey());
            entryTag.putLong("BankCopper", entry.bankCopper());
            if (!entry.bankLedger().isEmpty()) {
                ListTag ledgerTag = new ListTag();
                for (TownBankTransaction transaction : entry.bankLedger()) {
                    CompoundTag txTag = new CompoundTag();
                    txTag.putString("Action", transaction.action());
                    txTag.putString("ActorName", transaction.actorName());
                    txTag.putLong("AmountCopper", transaction.amountCopper());
                    txTag.putLong("CreatedAt", transaction.createdAt());
                    ledgerTag.add(txTag);
                }
                entryTag.put("BankLedger", ledgerTag);
            }
            entryTag.put("Permissions", savePermissionState(entry.permissionState()));
            townsTag.add(entryTag);
        }
        tag.put("Towns", townsTag);

        if (!registry.plotOwnersView().isEmpty()) {
            ListTag plotsTag = new ListTag();
            for (Map.Entry<String, UUID> entry : registry.plotOwnersView().entrySet()) {
                CompoundTag plotTag = new CompoundTag();
                plotTag.putString("ChunkKey", entry.getKey());
                plotTag.putUUID("TownId", entry.getValue());
                plotsTag.add(plotTag);
            }
            tag.put("Plots", plotsTag);
        }

        if (!registry.plotTrustedPlayersView().isEmpty()) {
            ListTag trustTag = new ListTag();
            for (Map.Entry<String, java.util.Set<UUID>> entry : registry.plotTrustedPlayersView().entrySet()) {
                for (UUID playerId : entry.getValue()) {
                    CompoundTag trustEntry = new CompoundTag();
                    trustEntry.putString("ChunkKey", entry.getKey());
                    trustEntry.putUUID("PlayerId", playerId);
                    trustTag.add(trustEntry);
                }
            }
            tag.put("PlotTrusts", trustTag);
        }

        if (!registry.membershipsView().isEmpty()) {
            ListTag membershipTag = new ListTag();
            for (Map.Entry<UUID, Map<UUID, TownRank>> townEntry : registry.membershipsView().entrySet()) {
                for (Map.Entry<UUID, TownRank> memberEntry : townEntry.getValue().entrySet()) {
                    CompoundTag memberTag = new CompoundTag();
                    memberTag.putUUID("TownId", townEntry.getKey());
                    memberTag.putUUID("PlayerId", memberEntry.getKey());
                    memberTag.putString("Rank", memberEntry.getValue().name());
                    membershipTag.add(memberTag);
                }
            }
            tag.put("Memberships", membershipTag);
        }

        if (!registry.invitationsView().isEmpty()) {
            ListTag invitationTag = new ListTag();
            for (Map.Entry<UUID, java.util.Set<UUID>> townEntry : registry.invitationsView().entrySet()) {
                for (UUID invitedPlayer : townEntry.getValue()) {
                    CompoundTag inviteTag = new CompoundTag();
                    inviteTag.putUUID("TownId", townEntry.getKey());
                    inviteTag.putUUID("PlayerId", invitedPlayer);
                    invitationTag.add(inviteTag);
                }
            }
            tag.put("Invitations", invitationTag);
        }

        return tag;
    }

    public UUID createTown(String name, UUID mayorId) {
        UUID townId = registry.createTown(name, mayorId);
        setDirty();
        return townId;
    }

    public boolean deleteTown(UUID townId) {
        if (!registry.deleteTown(townId)) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean claimPlot(UUID townId, String chunkKey) {
        boolean changed = registry.claimPlot(townId, chunkKey);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean unclaimPlot(String chunkKey) {
        if (registry.unclaimPlot(chunkKey)) {
            setDirty();
            return true;
        }
        return false;
    }

    @Nullable
    public UUID ownerOf(String chunkKey) {
        return registry.ownerOf(chunkKey);
    }

    public boolean isClaimed(String chunkKey) {
        return registry.isClaimed(chunkKey);
    }

    @Nullable
    public TownEntry town(UUID townId) {
        return registry.town(townId);
    }

    public Set<UUID> townIds() {
        return registry.townIds();
    }

    public Map<UUID, TownEntry> townsView() {
        return registry.townsView();
    }

    public Map<String, UUID> plotOwnersView() {
        return registry.plotOwnersView();
    }

    public Map<UUID, Map<UUID, TownRank>> membershipsView() {
        return registry.membershipsView();
    }

    public Optional<UUID> townForPlayer(UUID playerId) {
        for (Map.Entry<UUID, Map<UUID, TownRank>> entry : registry.membershipsView().entrySet()) {
            if (entry.getValue().containsKey(playerId)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    public int residentCount(UUID townId) {
        return registry.membershipsView().getOrDefault(townId, Map.of()).size();
    }

    public int plotCount(UUID townId) {
        int count = 0;
        for (UUID owner : registry.plotOwnersView().values()) {
            if (townId.equals(owner)) {
                count++;
            }
        }
        return count;
    }

    public Map<UUID, TownRank> membersOf(UUID townId) {
        return registry.membersOf(townId);
    }

    public boolean setPermissionState(UUID townId, TownPermissionState permissionState) {
        boolean changed = registry.setPermissionState(townId, permissionState);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public TownRank rankOf(UUID townId, UUID playerId) {
        return registry.rankOf(townId, playerId);
    }

    public void setRank(UUID townId, UUID playerId, TownRank rank) {
        registry.setRank(townId, playerId, rank);
        setDirty();
    }

    public boolean removeMember(UUID townId, UUID playerId) {
        boolean changed = registry.removeMember(townId, playerId);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean invitePlayer(UUID townId, UUID playerId) {
        boolean changed = registry.invitePlayer(townId, playerId);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean acceptInvitation(UUID townId, UUID playerId) {
        boolean changed = registry.acceptInvitation(townId, playerId);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean declineInvitation(UUID townId, UUID playerId) {
        boolean changed = registry.declineInvitation(townId, playerId);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean denyPlot(UUID townId, String chunkKey, UUID playerId) {
        boolean changed = registry.denyPlot(townId, chunkKey, playerId);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean undenyPlot(UUID townId, String chunkKey, UUID playerId) {
        boolean changed = registry.undenyPlot(townId, chunkKey, playerId);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean trustPlot(UUID townId, String chunkKey, UUID playerId) {
        boolean changed = registry.trustPlot(townId, chunkKey, playerId);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean untrustPlot(UUID townId, String chunkKey, UUID playerId) {
        boolean changed = registry.untrustPlot(townId, chunkKey, playerId);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean canInteract(UUID townId, UUID playerId, int flags) {
        return registry.canInteract(townId, "", playerId, ChunkPermissionFlags.sanitize(flags));
    }

    public boolean canInteract(UUID townId, String chunkKey, UUID playerId, int flags) {
        return registry.canInteract(townId, chunkKey, playerId, ChunkPermissionFlags.sanitize(flags));
    }

    public boolean hasInvitation(UUID townId, UUID playerId) {
        return registry.hasInvitationTo(townId, playerId);
    }

    public boolean hasPlotTrust(String chunkKey, UUID playerId) {
        return registry.hasPlotTrust(chunkKey, playerId);
    }

    public boolean hasPlotDeny(String chunkKey, UUID playerId) {
        return registry.hasPlotDeny(chunkKey, playerId);
    }

    public Map<String, java.util.Set<UUID>> plotDeniedPlayersView() {
        return registry.plotDeniedPlayersView();
    }

    public boolean depositTreasury(UUID townId, long amountCopper, String actorName) {
        boolean changed = registry.depositTreasury(townId, amountCopper, actorName);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean withdrawTreasury(UUID townId, long amountCopper, String actorName) {
        boolean changed = registry.withdrawTreasury(townId, amountCopper, actorName);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public Map<UUID, Set<UUID>> invitationsView() {
        return registry.invitationsView();
    }

    public java.util.List<TownBankTransaction> bankLedger(UUID townId) {
        return registry.bankLedger(townId);
    }

    private static TownPermissionState loadPermissionState(CompoundTag tag) {
        return new TownPermissionState(
                tag.getBoolean("AllowBuild"),
                tag.getBoolean("AllowDestroy"),
                tag.getBoolean("AllowSwitch"),
                tag.getBoolean("AllowItemUse"),
                tag.getBoolean("AllowMobGrief"),
                tag.getBoolean("AllowExplosions"),
                tag.getBoolean("AllowFire"),
                tag.getBoolean("AllowPvp"),
                tag.getBoolean("PublicAccess")
        );
    }

    private static CompoundTag savePermissionState(TownPermissionState state) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("AllowBuild", state.allowBuild());
        tag.putBoolean("AllowDestroy", state.allowDestroy());
        tag.putBoolean("AllowSwitch", state.allowSwitch());
        tag.putBoolean("AllowItemUse", state.allowItemUse());
        tag.putBoolean("AllowMobGrief", state.allowMobGrief());
        tag.putBoolean("AllowExplosions", state.allowExplosions());
        tag.putBoolean("AllowFire", state.allowFire());
        tag.putBoolean("AllowPvp", state.allowPvp());
        tag.putBoolean("PublicAccess", state.publicAccess());
        return tag;
    }
}