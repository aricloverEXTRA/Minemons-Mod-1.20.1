package com.minemons.network;

import com.minemons.ui.DuelScreen;
import com.minemons.ui.DeckScreen;
import com.minemons.ui.StoreScreen;
import com.minemons.ui.TradeScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

import java.util.*;

/**
 * Client-side handlers for all S2C packets.
 * All UI operations run on the render thread via client.execute().
 */
@Environment(EnvType.CLIENT)
public class ClientPacketHandlers {

    public static DuelClientState clientDuelState = null;

    // ── Duel ─────────────────────────────────────────────────────────────────

    public static void onDuelStart(MinecraftClient client, ClientPlayNetworkHandler handler,
                                    PacketByteBuf buf, PacketSender sender) {
        int myIndex = buf.readInt();
        DuelClientState state = readDuelState(buf, myIndex);
        client.execute(() -> { clientDuelState = state; client.setScreen(new DuelScreen(state)); });
    }

    public static void onDuelUpdate(MinecraftClient client, ClientPlayNetworkHandler handler,
                                     PacketByteBuf buf, PacketSender sender) {
        String message = buf.readString(256);
        int myIndex = clientDuelState != null ? clientDuelState.myIndex : 0;
        DuelClientState state = readDuelState(buf, myIndex);
        client.execute(() -> {
            clientDuelState = state;
            if (client.currentScreen instanceof DuelScreen ds) ds.updateState(state, message);
        });
    }

    public static void onDuelEnd(MinecraftClient client, ClientPlayNetworkHandler handler,
                                  PacketByteBuf buf, PacketSender sender) {
        String winner = buf.readString(32);
        client.execute(() -> {
            clientDuelState = null;
            if (client.currentScreen instanceof DuelScreen) client.setScreen(null);
            if (client.player != null)
                client.player.sendMessage(Text.literal("§6[Minemons] §eDuel over! Winner: §b" + winner), false);
        });
    }

    public static void onDuelInvite(MinecraftClient client, ClientPlayNetworkHandler handler,
                                     PacketByteBuf buf, PacketSender sender) {
        String challenger = buf.readString(32);
        client.execute(() -> {
            if (client.player != null)
                client.player.sendMessage(Text.literal(
                    "§6[Minemons] §e" + challenger + " challenged you! Type §a/duel accept §eor §c/duel deny"), false);
        });
    }

    // ── Deck UI ───────────────────────────────────────────────────────────────
    // Packet: int ownedCount, [string id, int count]×n,
    //         int deckCount, [string name, int entries, [string id, int count]×e]×d,
    //         int activeDeckIndex

    public static void onOpenDeckUi(MinecraftClient client, ClientPlayNetworkHandler handler,
                                     PacketByteBuf buf, PacketSender sender) {
        int ownedCount = buf.readInt();
        Map<String, Integer> owned = new LinkedHashMap<>();
        for (int i = 0; i < ownedCount; i++) owned.put(buf.readString(64), buf.readInt());

        int deckCount = buf.readInt();
        List<DeckSnapshot> decks = new ArrayList<>();
        for (int d = 0; d < deckCount; d++) {
            String name = buf.readString(64);
            int entries = buf.readInt();
            Map<String, Integer> cards = new LinkedHashMap<>();
            for (int e = 0; e < entries; e++) cards.put(buf.readString(64), buf.readInt());
            decks.add(new DeckSnapshot(name, cards));
        }
        int activeIdx = buf.readInt();

        client.execute(() -> client.setScreen(new DeckScreen(owned, decks, activeIdx)));
    }

    // ── Store UI ──────────────────────────────────────────────────────────────
    // Packet: int xp, int packCount, [string id, int cost, int count, string desc]×n

    public static void onOpenStoreUi(MinecraftClient client, ClientPlayNetworkHandler handler,
                                      PacketByteBuf buf, PacketSender sender) {
        int xp = buf.readInt();
        int packCount = buf.readInt();
        List<PackInfo> packs = new ArrayList<>();
        for (int i = 0; i < packCount; i++) {
            packs.add(new PackInfo(buf.readString(32), buf.readInt(), buf.readInt(), buf.readString(128)));
        }
        client.execute(() -> client.setScreen(new StoreScreen(xp, packs)));
    }

    // ── Trade UI ──────────────────────────────────────────────────────────────

    public static void onTradeOpen(MinecraftClient client, ClientPlayNetworkHandler handler,
                                    PacketByteBuf buf, PacketSender sender) {
        String first = buf.readString(64);
        if (buf.isReadable()) {
            // Full session: sessionId, nameA, nameB
            String nameA = buf.readString(32);
            String nameB = buf.readString(32);
            client.execute(() -> client.setScreen(new TradeScreen(first, nameA, nameB)));
        } else {
            // Invite-only: show chat notification
            client.execute(() -> {
                if (client.player != null)
                    client.player.sendMessage(Text.literal(
                        "§6[Minemons] §e" + first + " wants to trade! Type §a/decktrade accept"), false);
            });
        }
    }

    public static void onTradeUpdate(MinecraftClient client, ClientPlayNetworkHandler handler,
                                      PacketByteBuf buf, PacketSender sender) {
        int sa = buf.readInt(); List<String> offerA = new ArrayList<>();
        for (int i = 0; i < sa; i++) offerA.add(buf.readString(64));
        int sb = buf.readInt(); List<String> offerB = new ArrayList<>();
        for (int i = 0; i < sb; i++) offerB.add(buf.readString(64));
        String status = buf.readString(32);
        client.execute(() -> { if (client.currentScreen instanceof TradeScreen ts) ts.updateOffers(offerA, offerB, status); });
    }

    public static void onTradeResult(MinecraftClient client, ClientPlayNetworkHandler handler,
                                      PacketByteBuf buf, PacketSender sender) {
        String result = buf.readString(32);
        client.execute(() -> {
            if (client.currentScreen instanceof TradeScreen) client.setScreen(null);
            if (client.player != null)
                client.player.sendMessage(Text.literal(
                    "COMPLETE".equals(result) ? "§6[Minemons] §aTrade complete!" : "§6[Minemons] §cTrade cancelled."), false);
        });
    }

    // ── Duel animations ───────────────────────────────────────────────────────

    public static void onCardDrawn(MinecraftClient client, ClientPlayNetworkHandler handler,
                                    PacketByteBuf buf, PacketSender sender) {
        String id = buf.readString(64);
        client.execute(() -> { if (client.currentScreen instanceof DuelScreen ds) ds.animateCardDraw(id); });
    }

    public static void onCardSummoned(MinecraftClient client, ClientPlayNetworkHandler handler,
                                       PacketByteBuf buf, PacketSender sender) {
        String id = buf.readString(64); boolean opp = buf.readBoolean();
        client.execute(() -> { if (client.currentScreen instanceof DuelScreen ds) ds.animateSummon(id, opp); });
    }

    public static void onCardFainted(MinecraftClient client, ClientPlayNetworkHandler handler,
                                      PacketByteBuf buf, PacketSender sender) {
        String id = buf.readString(64); boolean opp = buf.readBoolean();
        client.execute(() -> { if (client.currentScreen instanceof DuelScreen ds) ds.animateFaint(id, opp); });
    }

    public static void onAttackAnim(MinecraftClient client, ClientPlayNetworkHandler handler,
                                     PacketByteBuf buf, PacketSender sender) {
        String id = buf.readString(64); int dmg = buf.readInt(); String el = buf.readString(32);
        client.execute(() -> { if (client.currentScreen instanceof DuelScreen ds) ds.animateAttack(id, dmg, el); });
    }

    // ── Player data update (after gamba / store purchase) ────────────────────
    // Packet: int updatedXp, int count, [string cardId × count]

    public static void onPlayerData(MinecraftClient client, ClientPlayNetworkHandler handler,
                                     PacketByteBuf buf, PacketSender sender) {
        int updatedXp = buf.readInt();
        int count = buf.readInt();
        List<String> newCards = new ArrayList<>();
        for (int i = 0; i < count; i++) newCards.add(buf.readString(64));

        client.execute(() -> {
            if (client.currentScreen instanceof StoreScreen ss) {
                ss.updateXp(updatedXp);
                ss.showPulledCards(newCards);
            } else if (client.currentScreen instanceof DeckScreen ds) {
                ds.refresh();
            }
        });
    }

    // ── DTOs ──────────────────────────────────────────────────────────────────

    public static class DuelClientState {
        public int myIndex;
        public String phase;
        public int currentTurn;
        public SideSnapshot[] sides = new SideSnapshot[2];
    }

    public static class SideSnapshot {
        public String playerName;
        public int handSize, deckSize, prizeCount, playerHp;
        public boolean playerIsActive;
        public String activeCardId;
        public int activeCardHp, activeCardAtk, activeCardShield;
        public List<String> benchIds = new ArrayList<>();
        public List<Integer> benchHps = new ArrayList<>();
        public List<String> hand = new ArrayList<>();
    }

    public static class DeckSnapshot {
        public final String name;
        public final Map<String, Integer> cards;
        public DeckSnapshot(String name, Map<String, Integer> cards) { this.name = name; this.cards = cards; }
    }

    public static class PackInfo {
        public final String id;
        public final int xpCost, cardCount;
        public final String description;
        public PackInfo(String id, int xpCost, int cardCount, String description) {
            this.id = id; this.xpCost = xpCost; this.cardCount = cardCount; this.description = description;
        }
    }

    private static DuelClientState readDuelState(PacketByteBuf buf, int myIndex) {
        DuelClientState s = new DuelClientState();
        s.myIndex = myIndex;
        s.phase = buf.readString(32);
        s.currentTurn = buf.readInt();
        for (int i = 0; i < 2; i++) {
            SideSnapshot snap = new SideSnapshot();
            snap.playerName     = buf.readString(32);
            snap.handSize       = buf.readInt();
            snap.deckSize       = buf.readInt();
            snap.prizeCount     = buf.readInt();
            snap.playerHp       = buf.readInt();
            snap.playerIsActive = buf.readBoolean();
            if (buf.readBoolean()) {
                snap.activeCardId     = buf.readString(64);
                snap.activeCardHp     = buf.readInt();
                snap.activeCardAtk    = buf.readInt();
                snap.activeCardShield = buf.readInt();
            }
            int bench = buf.readInt();
            for (int j = 0; j < bench; j++) { snap.benchIds.add(buf.readString(64)); snap.benchHps.add(buf.readInt()); }
            int hand = buf.readInt();
            for (int j = 0; j < hand; j++) snap.hand.add(buf.readString(64));
            s.sides[i] = snap;
        }
        return s;
    }
}
