package com.minemons.api;

import com.minemons.battle.DuelState;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * API interface allowing servers to override Minemons duel logic.
 * 
 * Servers can implement this and call:
 *   DuelManager.setRules(new CustomRules());
 */
public interface DuelRulesProvider {

    /**
     * Determines whether a player is allowed to challenge another player.
     * Called BEFORE a duel invite is sent.
     */
    boolean canChallenge(ServerPlayerEntity challenger, ServerPlayerEntity target);

    /**
     * Determines whether a duel is allowed to start.
     * Called right before DuelManager.startDuel creates the DuelState.
     */
    boolean canStartDuel(ServerPlayerEntity p1, ServerPlayerEntity p2);

    /**
     * Determines how much XP the winner receives.
     */
    int rewardXpOnWin(ServerPlayerEntity winner, DuelState duel);

    /**
     * Optional hook: called when a duel officially starts.
     */
    default void onDuelStart(DuelState duel) {}

    /**
     * Optional hook: called when a duel ends.
     */
    default void onDuelEnd(DuelState duel, ServerPlayerEntity winner) {}
}