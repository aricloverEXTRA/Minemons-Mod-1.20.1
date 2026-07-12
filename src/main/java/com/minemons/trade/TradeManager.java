package com.minemons.trade;

import com.minemons.data.PlayerData;
import com.minemons.data.PlayerDataManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

/**
 * Server-side manager for all in-progress trades.
 */
public class TradeManager {

    private static final Map<UUID, TradeSession> ACTIVE_TRADES = new HashMap<>();
    // player UUID → trade UUID
    private static final Map<UUID, UUID> PLAYER_TRADE_MAP = new HashMap<>();
    // pending requests: target UUID → requester UUID
    private static final Map<UUID, UUID> PENDING_REQUESTS = new HashMap<>();

    public static void sendRequest(ServerPlayerEntity from, ServerPlayerEntity to) {
        PENDING_REQUESTS.put(to.getUuid(), from.getUuid());
    }

    public static boolean hasPendingRequest(ServerPlayerEntity target) {
        return PENDING_REQUESTS.containsKey(target.getUuid());
    }

    public static boolean hasPendingRequest(ServerPlayerEntity target, ServerPlayerEntity from) {
        return from.getUuid().equals(PENDING_REQUESTS.get(target.getUuid()));
    }

    /** Returns the player who sent a pending trade request to {@code target}, or null. */
    public static ServerPlayerEntity getPendingRequester(ServerPlayerEntity target) {
        UUID requesterId = PENDING_REQUESTS.get(target.getUuid());
        if (requesterId == null) return null;
        return target.getServer().getPlayerManager().getPlayer(requesterId);
    }

    /** Clears any pending request aimed at {@code target}. */
    public static void cancelPendingRequest(ServerPlayerEntity target) {
        PENDING_REQUESTS.remove(target.getUuid());
    }

    public static TradeSession openTrade(ServerPlayerEntity a, ServerPlayerEntity b) {
        TradeSession session = new TradeSession(a, b);
        session.setStatus(TradeSession.TradeStatus.OPEN);
        ACTIVE_TRADES.put(session.tradeId, session);
        PLAYER_TRADE_MAP.put(a.getUuid(), session.tradeId);
        PLAYER_TRADE_MAP.put(b.getUuid(), session.tradeId);
        PENDING_REQUESTS.remove(b.getUuid());
        return session;
    }

    public static TradeSession getTradeFor(ServerPlayerEntity player) {
        UUID tid = PLAYER_TRADE_MAP.get(player.getUuid());
        if (tid == null) return null;
        return ACTIVE_TRADES.get(tid);
    }

    public static boolean isInTrade(ServerPlayerEntity player) {
        return PLAYER_TRADE_MAP.containsKey(player.getUuid());
    }

    /**
     * Execute the trade — swap offered cards between players' collections.
     * Returns true on success.
     */
    public static boolean finalizeTrade(TradeSession session) {
        if (!session.isReadyToFinalize()) return false;

        PlayerData dataA = PlayerDataManager.get(session.playerA);
        PlayerData dataB = PlayerDataManager.get(session.playerB);

        // Validate both players own the cards they're offering
        for (String id : session.offerA) {
            if (dataA.getCardCount(id) <= 0) return false;
        }
        for (String id : session.offerB) {
            if (dataB.getCardCount(id) <= 0) return false;
        }

        // Execute swap
        for (String id : session.offerA) { dataA.removeCard(id); dataB.addCard(id); }
        for (String id : session.offerB) { dataB.removeCard(id); dataA.addCard(id); }

        session.setStatus(TradeSession.TradeStatus.COMPLETE);
        if (session.playerA.getServer() != null) PlayerDataManager.markDirty(session.playerA.getServer());
        closeTrade(session.tradeId);
        return true;
    }

    public static void cancelTrade(UUID tradeId) {
        closeTrade(tradeId);
    }

    private static void closeTrade(UUID tradeId) {
        TradeSession s = ACTIVE_TRADES.remove(tradeId);
        if (s != null) {
            PLAYER_TRADE_MAP.remove(s.playerA.getUuid());
            PLAYER_TRADE_MAP.remove(s.playerB.getUuid());
        }
    }

    /** Clean up expired trades (call periodically) */
    public static void tickExpired() {
        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, TradeSession> e : ACTIVE_TRADES.entrySet()) {
            if (e.getValue().isExpired()) toRemove.add(e.getKey());
        }
        toRemove.forEach(TradeManager::closeTrade);
    }
}
