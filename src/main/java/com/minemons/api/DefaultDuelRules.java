package com.minemons.api;

import com.minemons.battle.DuelState;
import com.minemons.data.PlayerData;
import com.minemons.data.PlayerDataManager;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Default Minemons duel rules.
 * 
 * These match the original hardcoded logic so the mod behaves
 * exactly the same unless a server overrides the rules.
 */
public class DefaultDuelRules implements DuelRulesProvider {

    @Override
    public boolean canChallenge(ServerPlayerEntity challenger, ServerPlayerEntity target) {

        // Must have a valid deck with at least 10 cards
        PlayerData data = PlayerDataManager.get(challenger);
        if (data.getActiveDeck() == null) return false;
        if (data.getActiveDeck().getCards().size() < 10) return false;

        return true;
    }

    @Override
    public boolean canStartDuel(ServerPlayerEntity p1, ServerPlayerEntity p2) {
        // Default: always allow duel start
        return true;
    }

    @Override
    public int rewardXpOnWin(ServerPlayerEntity winner, DuelState duel) {
        // Default XP reward
        return 5;
    }

    @Override
    public void onDuelStart(DuelState duel) {
        // No-op by default
    }

    @Override
    public void onDuelEnd(DuelState duel, ServerPlayerEntity winner) {
        // No-op by default
    }
}
