package com.minemons.battle;

import com.minemons.api.DuelRulesProvider;
import com.minemons.api.DefaultDuelRules;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

public class DuelManager {

    // ─────────────────────────────────────────────────────────────
    //  Duel Rules Provider (server‑customizable)
    // ─────────────────────────────────────────────────────────────

    private static DuelRulesProvider RULES = new DefaultDuelRules();

    public static void setRules(DuelRulesProvider provider) {
        if (provider != null) RULES = provider;
    }

    public static DuelRulesProvider getRules() {
        return RULES;
    }

    // ─────────────────────────────────────────────────────────────
    //  Duel State Tracking
    // ─────────────────────────────────────────────────────────────

    private static final Map<UUID, DuelState> ACTIVE_DUELS = new HashMap<>();
    private static final Map<UUID, UUID> PLAYER_DUEL_MAP = new HashMap<>();
    private static final Map<UUID, UUID> PENDING_INVITES = new HashMap<>();

    // ─────────────────────────────────────────────────────────────
    //  Pending Invites
    // ─────────────────────────────────────────────────────────────

    public static void sendInvite(ServerPlayerEntity challenger, ServerPlayerEntity target) {
        PENDING_INVITES.put(challenger.getUuid(), target.getUuid());
    }

    public static void storePendingInvite(ServerPlayerEntity challenger, ServerPlayerEntity target) {
        PENDING_INVITES.put(target.getUuid(), challenger.getUuid());
    }

    public static ServerPlayerEntity getPendingChallenger(ServerPlayerEntity target) {
        UUID challengerId = PENDING_INVITES.get(target.getUuid());
        if (challengerId == null) return null;
        return target.getServer().getPlayerManager().getPlayer(challengerId);
    }

    public static void clearPendingInvite(ServerPlayerEntity target) {
        PENDING_INVITES.remove(target.getUuid());
    }

    public static boolean hasPendingInvite(ServerPlayerEntity target, ServerPlayerEntity challenger) {
        UUID pending = PENDING_INVITES.get(challenger.getUuid());
        return target.getUuid().equals(pending);
    }

    // ─────────────────────────────────────────────────────────────
    //  Duel Lifecycle
    // ─────────────────────────────────────────────────────────────

    public static DuelState startDuel(ServerPlayerEntity p1, ServerPlayerEntity p2) {

        // Allow server rules to veto duel start
        if (!RULES.canStartDuel(p1, p2)) {
            return null;
        }

        UUID duelId = UUID.randomUUID();
        long seed = System.currentTimeMillis();

        DuelState state = new DuelState(duelId, p1, p2, seed);
        state.startMatch();

        ACTIVE_DUELS.put(duelId, state);
        PLAYER_DUEL_MAP.put(p1.getUuid(), duelId);
        PLAYER_DUEL_MAP.put(p2.getUuid(), duelId);

        PENDING_INVITES.remove(p1.getUuid());
        PENDING_INVITES.remove(p2.getUuid());

        // Server hook
        RULES.onDuelStart(state);

        return state;
    }

    public static DuelState getDuelFor(ServerPlayerEntity player) {
        UUID duelId = PLAYER_DUEL_MAP.get(player.getUuid());
        if (duelId == null) return null;
        return ACTIVE_DUELS.get(duelId);
    }

    public static boolean isInDuel(ServerPlayerEntity player) {
        return PLAYER_DUEL_MAP.containsKey(player.getUuid());
    }

    public static void endDuel(UUID duelId) {
        DuelState state = ACTIVE_DUELS.remove(duelId);
        if (state != null) {
            for (DuelState.PlayerSide side : state.sides) {
                PLAYER_DUEL_MAP.remove(side.playerId);
            }
        }
    }

    public static Map<UUID, DuelState> getActiveDuels() {
        return Collections.unmodifiableMap(ACTIVE_DUELS);
    }

    // ─────────────────────────────────────────────────────────────
    //  XP Reward Helper
    // ─────────────────────────────────────────────────────────────

    public static int getXpReward(ServerPlayerEntity winner, DuelState duel) {
        return RULES.rewardXpOnWin(winner, duel);
    }
}
