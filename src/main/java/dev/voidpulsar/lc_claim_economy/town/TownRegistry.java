package dev.voidpulsar.lc_claim_economy.town;

import dev.voidpulsar.lc_claim_economy.service.ChunkPermissionFlags;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class TownRegistry {
    final Map<UUID, TownEntry> towns = new HashMap<>();
    final Map<String, UUID> plotOwners = new HashMap<>();
    final Map<String, java.util.Set<UUID>> plotTrustedPlayers = new HashMap<>();
    final Map<String, java.util.Set<UUID>> plotDeniedPlayers = new HashMap<>();
    final Map<UUID, Map<UUID, TownRank>> memberships = new HashMap<>();
    final Map<UUID, java.util.Set<UUID>> invitations = new HashMap<>();

    public UUID createTown(String name, UUID mayorId) {
        UUID townId = UUID.randomUUID();
        towns.put(townId, new TownEntry(townId, name, mayorId, null, TownPermissionState.defaultTown(), System.currentTimeMillis(), 0L, 0L, List.of()));
        memberships.computeIfAbsent(townId, ignored -> new HashMap<>()).put(mayorId, TownRank.MAYOR);
        return townId;
    }

    public boolean deleteTown(UUID townId) {
        if (!towns.containsKey(townId)) {
            return false;
        }
        towns.remove(townId);
        plotOwners.entrySet().removeIf(entry -> {
            if (townId.equals(entry.getValue())) {
                plotTrustedPlayers.remove(entry.getKey());
                plotDeniedPlayers.remove(entry.getKey());
                return true;
            }
            return false;
        });
        memberships.remove(townId);
        invitations.remove(townId);
        return true;
    }

    public boolean claimPlot(UUID townId, String chunkKey) {
        if (!towns.containsKey(townId) || chunkKey.isEmpty()) {
            return false;
        }
        plotOwners.put(chunkKey, townId);
        return true;
    }

    public boolean unclaimPlot(String chunkKey) {
        plotTrustedPlayers.remove(chunkKey);
        plotDeniedPlayers.remove(chunkKey);
        return plotOwners.remove(chunkKey) != null;
    }

    @Nullable
    public UUID ownerOf(String chunkKey) {
        return plotOwners.get(chunkKey);
    }

    public boolean isClaimed(String chunkKey) {
        return plotOwners.containsKey(chunkKey);
    }

    @Nullable
    public TownEntry town(UUID townId) {
        return towns.get(townId);
    }

    public Set<UUID> townIds() {
        return Set.copyOf(towns.keySet());
    }

    void clear() {
        towns.clear();
        plotOwners.clear();
        plotTrustedPlayers.clear();
        plotDeniedPlayers.clear();
        memberships.clear();
        invitations.clear();
    }

    void putTown(TownEntry entry) {
        towns.put(entry.townId(), entry);
    }

    void setTownEntry(UUID townId, TownEntry entry) {
        if (towns.containsKey(townId)) {
            towns.put(townId, entry);
        }
    }

    void putPlotOwner(String chunkKey, UUID townId) {
        plotOwners.put(chunkKey, townId);
    }

    void putPlotTrust(String chunkKey, UUID playerId) {
        plotTrustedPlayers.computeIfAbsent(chunkKey, ignored -> new java.util.HashSet<>()).add(playerId);
    }

    void putPlotDeny(String chunkKey, UUID playerId) {
        plotDeniedPlayers.computeIfAbsent(chunkKey, ignored -> new java.util.HashSet<>()).add(playerId);
    }

    void putMembership(UUID townId, UUID playerId, TownRank rank) {
        memberships.computeIfAbsent(townId, ignored -> new HashMap<>()).put(playerId, rank);
    }

    void putInvitation(UUID townId, UUID playerId) {
        invitations.computeIfAbsent(townId, ignored -> new java.util.HashSet<>()).add(playerId);
    }

    public TownRank rankOf(UUID townId, UUID playerId) {
        return memberships.getOrDefault(townId, Map.of()).getOrDefault(playerId, TownRank.OUTSIDER);
    }

    public void setRank(UUID townId, UUID playerId, TownRank rank) {
        if (!towns.containsKey(townId)) {
            return;
        }
        memberships.computeIfAbsent(townId, ignored -> new HashMap<>()).put(playerId, rank);
    }

    public boolean removeMember(UUID townId, UUID playerId) {
        Map<UUID, TownRank> members = memberships.get(townId);
        if (members == null || !members.containsKey(playerId)) {
            return false;
        }
        if (members.get(playerId) == TownRank.MAYOR) {
            return false;
        }
        members.remove(playerId);
        if (members.isEmpty()) {
            memberships.remove(townId);
        }
        return true;
    }

    public boolean trustPlot(UUID townId, String chunkKey, UUID playerId) {
        if (!townId.equals(plotOwners.get(chunkKey))) {
            return false;
        }
        return plotTrustedPlayers.computeIfAbsent(chunkKey, ignored -> new java.util.HashSet<>()).add(playerId);
    }

    public boolean untrustPlot(UUID townId, String chunkKey, UUID playerId) {
        if (!townId.equals(plotOwners.get(chunkKey))) {
            return false;
        }
        java.util.Set<UUID> trusted = plotTrustedPlayers.get(chunkKey);
        if (trusted == null || !trusted.remove(playerId)) {
            return false;
        }
        if (trusted.isEmpty()) {
            plotTrustedPlayers.remove(chunkKey);
        }
        return true;
    }

    public boolean denyPlot(UUID townId, String chunkKey, UUID playerId) {
        if (!townId.equals(plotOwners.get(chunkKey))) {
            return false;
        }
        return plotDeniedPlayers.computeIfAbsent(chunkKey, ignored -> new java.util.HashSet<>()).add(playerId);
    }

    public boolean undenyPlot(UUID townId, String chunkKey, UUID playerId) {
        if (!townId.equals(plotOwners.get(chunkKey))) {
            return false;
        }
        java.util.Set<UUID> denied = plotDeniedPlayers.get(chunkKey);
        if (denied == null || !denied.remove(playerId)) {
            return false;
        }
        if (denied.isEmpty()) {
            plotDeniedPlayers.remove(chunkKey);
        }
        return true;
    }

    public boolean hasPlotTrust(String chunkKey, UUID playerId) {
        return plotTrustedPlayers.getOrDefault(chunkKey, java.util.Set.of()).contains(playerId);
    }

    public boolean hasPlotDeny(String chunkKey, UUID playerId) {
        return plotDeniedPlayers.getOrDefault(chunkKey, java.util.Set.of()).contains(playerId);
    }

    public boolean invitePlayer(UUID townId, UUID playerId) {
        if (!towns.containsKey(townId) || rankOf(townId, playerId) != TownRank.OUTSIDER) {
            return false;
        }
        return invitations.computeIfAbsent(townId, ignored -> new java.util.HashSet<>()).add(playerId);
    }

    public boolean hasInvitation(UUID townId, UUID playerId) {
        return invitations.getOrDefault(townId, java.util.Set.of()).contains(playerId);
    }

    public boolean acceptInvitation(UUID townId, UUID playerId) {
        java.util.Set<UUID> invited = invitations.get(townId);
        if (invited == null || !invited.remove(playerId)) {
            return false;
        }
        if (invited.isEmpty()) {
            invitations.remove(townId);
        }
        setRank(townId, playerId, TownRank.RESIDENT);
        return true;
    }

    public boolean declineInvitation(UUID townId, UUID playerId) {
        java.util.Set<UUID> invited = invitations.get(townId);
        if (invited == null || !invited.remove(playerId)) {
            return false;
        }
        if (invited.isEmpty()) {
            invitations.remove(townId);
        }
        return true;
    }

    public boolean canInteract(UUID townId, String chunkKey, UUID playerId, int flags) {
        TownEntry entry = towns.get(townId);
        if (entry == null) {
            return false;
        }

        TownRank rank = rankOf(townId, playerId);
        if (rank.canManageTown()) {
            return true;
        }

        if (plotDeniedPlayers.getOrDefault(chunkKey, java.util.Set.of()).contains(playerId)) {
            return false;
        }

        if (plotTrustedPlayers.getOrDefault(chunkKey, java.util.Set.of()).contains(playerId)) {
            return flags != ChunkPermissionFlags.PVP || entry.permissionState().allowPvp();
        }

        TownPermissionState permissions = entry.permissionState();
        if (rank.canUseTrustedLand()) {
            return flags != ChunkPermissionFlags.PVP || permissions.allowPvp();
        }

        if (!permissions.publicAccess()) {
            return false;
        }

        return flags != ChunkPermissionFlags.PVP || permissions.allowPvp();
    }

    public boolean canInteract(UUID townId, UUID playerId, int flags) {
        return canInteract(townId, "", playerId, flags);
    }

    public boolean depositTreasury(UUID townId, long amountCopper, String actorName) {
        if (amountCopper <= 0) {
            return false;
        }
        TownEntry entry = towns.get(townId);
        if (entry == null) {
            return false;
        }
        TownEntry updated = entry.withBankCopper(entry.bankCopper() + amountCopper)
                .withBankLedger(appendLedger(entry, new TownBankTransaction("Deposit", actorName == null || actorName.isBlank() ? "Treasury" : actorName, amountCopper, System.currentTimeMillis())));
        towns.put(townId, updated);
        return true;
    }

    public boolean withdrawTreasury(UUID townId, long amountCopper, String actorName) {
        if (amountCopper <= 0) {
            return false;
        }
        TownEntry entry = towns.get(townId);
        if (entry == null || entry.bankCopper() < amountCopper) {
            return false;
        }
        TownEntry updated = entry.withBankCopper(entry.bankCopper() - amountCopper)
                .withBankLedger(appendLedger(entry, new TownBankTransaction("Withdraw", actorName == null || actorName.isBlank() ? "Treasury" : actorName, -amountCopper, System.currentTimeMillis())));
        towns.put(townId, updated);
        return true;
    }

    public List<TownBankTransaction> bankLedger(UUID townId) {
        TownEntry entry = towns.get(townId);
        return entry == null ? List.of() : entry.bankLedger();
    }

    public Map<UUID, TownRank> membersOf(UUID townId) {
        return Map.copyOf(memberships.getOrDefault(townId, Map.of()));
    }

    public boolean setPermissionState(UUID townId, TownPermissionState permissionState) {
        TownEntry entry = towns.get(townId);
        if (entry == null) {
            return false;
        }
        towns.put(townId, entry.withPermissionState(permissionState));
        return true;
    }

    public Map<UUID, TownEntry> townsView() {
        return Map.copyOf(towns);
    }

    private static List<TownBankTransaction> appendLedger(TownEntry entry, TownBankTransaction transaction) {
        List<TownBankTransaction> ledger = new ArrayList<>(entry.bankLedger());
        ledger.add(transaction);
        if (ledger.size() > 12) {
            ledger = ledger.subList(ledger.size() - 12, ledger.size());
        }
        return ledger;
    }

    public Map<String, UUID> plotOwnersView() {
        return Map.copyOf(plotOwners);
    }

    public Map<String, java.util.Set<UUID>> plotTrustedPlayersView() {
        return plotTrustedPlayers.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> java.util.Set.copyOf(entry.getValue())));
    }

    public Map<String, java.util.Set<UUID>> plotDeniedPlayersView() {
        return plotDeniedPlayers.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> java.util.Set.copyOf(entry.getValue())));
    }

    public Map<UUID, Map<UUID, TownRank>> membershipsView() {
        return memberships.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> Map.copyOf(entry.getValue())));
    }

    public Map<UUID, java.util.Set<UUID>> invitationsView() {
        return invitations.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> java.util.Set.copyOf(entry.getValue())));
    }

    public boolean hasInvitationTo(UUID townId, UUID playerId) {
        return hasInvitation(townId, playerId);
    }

    public java.util.Set<UUID> invitationsOf(UUID townId) {
        return java.util.Set.copyOf(invitations.getOrDefault(townId, java.util.Set.of()));
    }

    public void loadFrom(TownRegistry other) {
        towns.clear();
        plotOwners.clear();
        plotTrustedPlayers.clear();
        plotDeniedPlayers.clear();
        memberships.clear();
        invitations.clear();
        towns.putAll(other.towns);
        plotOwners.putAll(other.plotOwners);
        for (Map.Entry<String, java.util.Set<UUID>> entry : other.plotTrustedPlayers.entrySet()) {
            plotTrustedPlayers.put(entry.getKey(), new java.util.HashSet<>(entry.getValue()));
        }
        for (Map.Entry<String, java.util.Set<UUID>> entry : other.plotDeniedPlayers.entrySet()) {
            plotDeniedPlayers.put(entry.getKey(), new java.util.HashSet<>(entry.getValue()));
        }
        for (Map.Entry<UUID, Map<UUID, TownRank>> entry : other.memberships.entrySet()) {
            memberships.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
        for (Map.Entry<UUID, java.util.Set<UUID>> entry : other.invitations.entrySet()) {
            invitations.put(entry.getKey(), new java.util.HashSet<>(entry.getValue()));
        }
    }

    public void copyFrom(TownRegistry other) {
        loadFrom(other);
    }
}