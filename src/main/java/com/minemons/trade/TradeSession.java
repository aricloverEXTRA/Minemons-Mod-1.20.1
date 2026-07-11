package com.minemons.trade;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

/**
 * Tracks an active trade session between two players.
 * Both must confirm before cards are exchanged.
 */
public class TradeSession {

    public enum TradeStatus {
        PENDING, OPEN, CONFIRMED_A, CONFIRMED_B, BOTH_CONFIRMED, COMPLETE, CANCELLED
    }

    public final UUID tradeId;
    public final ServerPlayerEntity playerA;
    public final ServerPlayerEntity playerB;

    /** Cards that each player is offering (card IDs) */
    public final List<String> offerA = new ArrayList<>();
    public final List<String> offerB = new ArrayList<>();

    private TradeStatus status = TradeStatus.PENDING;
    private long lastActivity = System.currentTimeMillis();
    private static final long TIMEOUT_MS = 120_000; // 2 minutes

    public TradeSession(ServerPlayerEntity a, ServerPlayerEntity b) {
        this.tradeId = UUID.randomUUID();
        this.playerA = a;
        this.playerB = b;
    }

    public TradeStatus getStatus()  { return status; }
    public void setStatus(TradeStatus s) { this.status = s; touch(); }

    public boolean isExpired() {
        return System.currentTimeMillis() - lastActivity > TIMEOUT_MS;
    }

    public void touch() { lastActivity = System.currentTimeMillis(); }

    public boolean addOfferCard(ServerPlayerEntity player, String cardId) {
        touch();
        if (player.getUuid().equals(playerA.getUuid())) {
            if (offerA.size() >= 10) return false;
            offerA.add(cardId); return true;
        } else if (player.getUuid().equals(playerB.getUuid())) {
            if (offerB.size() >= 10) return false;
            offerB.add(cardId); return true;
        }
        return false;
    }

    public boolean removeOfferCard(ServerPlayerEntity player, String cardId) {
        touch();
        if (player.getUuid().equals(playerA.getUuid())) return offerA.remove(cardId);
        if (player.getUuid().equals(playerB.getUuid())) return offerB.remove(cardId);
        return false;
    }

    public void confirm(ServerPlayerEntity player) {
        touch();
        if (player.getUuid().equals(playerA.getUuid())) {
            if (status == TradeStatus.CONFIRMED_B) setStatus(TradeStatus.BOTH_CONFIRMED);
            else setStatus(TradeStatus.CONFIRMED_A);
        } else if (player.getUuid().equals(playerB.getUuid())) {
            if (status == TradeStatus.CONFIRMED_A) setStatus(TradeStatus.BOTH_CONFIRMED);
            else setStatus(TradeStatus.CONFIRMED_B);
        }
    }

    public boolean isReadyToFinalize() {
        return status == TradeStatus.BOTH_CONFIRMED;
    }

    /** Alias so command code can call getSessionId() without knowing internal field name. */
    public UUID getSessionId() { return tradeId; }

    /** Returns the other participant in this trade, or null if player is not in it. */
    public ServerPlayerEntity getOtherPlayer(ServerPlayerEntity player) {
        if (player.getUuid().equals(playerA.getUuid())) return playerB;
        if (player.getUuid().equals(playerB.getUuid())) return playerA;
        return null;
    }
}
