package dev.voidpulsar.lc_claim_economy.town;

import dev.voidpulsar.lc_claim_economy.service.ChunkPermissionFlags;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TownRegistryDataTest {
    @Test
    void claimAndUnclaimPlot_tracksOwnership() {
        TownRegistry registry = new TownRegistry();
        UUID mayorId = UUID.randomUUID();
        UUID townId = registry.createTown("Spawn", mayorId);

        assertTrue(registry.claimPlot(townId, "minecraft:overworld#0#0"));
        assertEquals(townId, registry.ownerOf("minecraft:overworld#0#0"));
        assertTrue(registry.isClaimed("minecraft:overworld#0#0"));

        assertTrue(registry.unclaimPlot("minecraft:overworld#0#0"));
        assertFalse(registry.isClaimed("minecraft:overworld#0#0"));
    }

    @Test
    void rankDefaultsToOutsider_thenCanBeUpdated() {
        TownRegistry registry = new TownRegistry();
        UUID mayorId = UUID.randomUUID();
        UUID residentId = UUID.randomUUID();
        UUID townId = registry.createTown("Frontier", mayorId);

        assertEquals(TownRank.OUTSIDER, registry.rankOf(townId, residentId));

        registry.setRank(townId, residentId, TownRank.RESIDENT);
        assertEquals(TownRank.RESIDENT, registry.rankOf(townId, residentId));
    }

    @Test
    void nativeTownAccess_allowsResidents_andKeepsOutsidersOutByDefault() {
        TownRegistry registry = new TownRegistry();
        UUID mayorId = UUID.randomUUID();
        UUID residentId = UUID.randomUUID();
        UUID outsiderId = UUID.randomUUID();
        UUID townId = registry.createTown("Harbor", mayorId);
        registry.setRank(townId, residentId, TownRank.RESIDENT);

        assertTrue(registry.canInteract(townId, mayorId, ChunkPermissionFlags.BLOCK_EDIT));
        assertTrue(registry.canInteract(townId, residentId, ChunkPermissionFlags.BLOCK_INTERACT));
        assertFalse(registry.canInteract(townId, outsiderId, ChunkPermissionFlags.BLOCK_EDIT));

        registry.setPermissionState(townId, new TownPermissionState(false, false, false, false, false, false, false, false, true));

        assertTrue(registry.canInteract(townId, outsiderId, ChunkPermissionFlags.BLOCK_INTERACT));
        assertFalse(registry.canInteract(townId, outsiderId, ChunkPermissionFlags.PVP));
    }

    @Test
    void invitations_canBeSent_andAccepted() {
        TownRegistry registry = new TownRegistry();
        UUID mayorId = UUID.randomUUID();
        UUID invitedId = UUID.randomUUID();
        UUID townId = registry.createTown("Summit", mayorId);

        assertTrue(registry.invitePlayer(townId, invitedId));
        assertTrue(registry.hasInvitation(townId, invitedId));

        assertTrue(registry.acceptInvitation(townId, invitedId));
        assertEquals(TownRank.RESIDENT, registry.rankOf(townId, invitedId));
        assertFalse(registry.hasInvitation(townId, invitedId));
    }

    @Test
    void plotTrust_allowsOutsiderOnSingleClaimWithoutOpeningTown() {
        TownRegistry registry = new TownRegistry();
        UUID mayorId = UUID.randomUUID();
        UUID trustedId = UUID.randomUUID();
        UUID outsiderId = UUID.randomUUID();
        UUID townId = registry.createTown("Ridge", mayorId);
        String chunkKey = "minecraft:overworld#4#7";
        registry.claimPlot(townId, chunkKey);

        assertFalse(registry.canInteract(townId, chunkKey, trustedId, ChunkPermissionFlags.BLOCK_EDIT));

        assertTrue(registry.trustPlot(townId, chunkKey, trustedId));
        assertTrue(registry.canInteract(townId, chunkKey, trustedId, ChunkPermissionFlags.BLOCK_EDIT));
        assertFalse(registry.canInteract(townId, chunkKey, outsiderId, ChunkPermissionFlags.BLOCK_EDIT));
    }

    @Test
    void plotDeny_overridesTrust() {
        TownRegistry registry = new TownRegistry();
        UUID mayorId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID townId = registry.createTown("Vale", mayorId);
        String chunkKey = "minecraft:overworld#9#2";
        registry.claimPlot(townId, chunkKey);

        assertTrue(registry.trustPlot(townId, chunkKey, targetId));
        assertTrue(registry.denyPlot(townId, chunkKey, targetId));
        assertFalse(registry.canInteract(townId, chunkKey, targetId, ChunkPermissionFlags.BLOCK_INTERACT));
    }

    @Test
    void treasury_balance_canIncrease_andDecrease() {
        TownRegistry registry = new TownRegistry();
        UUID mayorId = UUID.randomUUID();
        UUID townId = registry.createTown("Keep", mayorId);

        assertTrue(registry.depositTreasury(townId, 250L, "Mayor"));
        assertEquals(250L, registry.town(townId).bankCopper());
        assertEquals(1, registry.town(townId).bankLedger().size());

        assertTrue(registry.withdrawTreasury(townId, 40L, "Mayor"));
        assertEquals(210L, registry.town(townId).bankCopper());
        assertEquals(2, registry.town(townId).bankLedger().size());
    }

    @Test
    void treasuryPermissions_followRankRules() {
        assertTrue(TownRank.MAYOR.canDepositTreasury());
        assertTrue(TownRank.ASSISTANT.canDepositTreasury());
        assertTrue(TownRank.RESIDENT.canDepositTreasury());
        assertFalse(TownRank.OUTSIDER.canDepositTreasury());

        assertTrue(TownRank.MAYOR.canWithdrawTreasury());
        assertFalse(TownRank.ASSISTANT.canWithdrawTreasury());
        assertFalse(TownRank.RESIDENT.canWithdrawTreasury());
    }
}