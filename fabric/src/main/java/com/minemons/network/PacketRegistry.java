package com.minemons.network;

import com.minemons.MinemonsMain;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

/**
 * Central packet registration.
 *
 * Packet naming convention: minemons:<action>
 */
public class PacketRegistry {

    // ──────────────────────────────────────────────
    // Packet IDs — Client → Server
    // ──────────────────────────────────────────────
    public static final Identifier DUEL_ACTION         = id("duel_action");
    public static final Identifier DUEL_ACCEPT         = id("duel_accept");
    public static final Identifier DECK_SAVE           = id("deck_save");
    public static final Identifier STORE_BUY           = id("store_buy");
    public static final Identifier TRADE_REQUEST       = id("trade_request");
    public static final Identifier TRADE_OFFER_UPDATE  = id("trade_offer_update");
    public static final Identifier TRADE_CONFIRM       = id("trade_confirm");
    public static final Identifier TRADE_FINALIZE      = id("trade_finalize");
    public static final Identifier TRADE_RESPONSE      = id("trade_response");

    // ──────────────────────────────────────────────
    // Packet IDs — Server → Client
    // ──────────────────────────────────────────────
    public static final Identifier S2C_DUEL_START      = id("s2c_duel_start");
    public static final Identifier S2C_DUEL_UPDATE     = id("s2c_duel_update");
    public static final Identifier S2C_DUEL_END        = id("s2c_duel_end");
    public static final Identifier S2C_DUEL_INVITE     = id("s2c_duel_invite");
    public static final Identifier S2C_OPEN_DECK_UI    = id("s2c_open_deck_ui");
    public static final Identifier S2C_OPEN_STORE_UI   = id("s2c_open_store_ui");
    public static final Identifier S2C_TRADE_OPEN      = id("s2c_trade_open");
    public static final Identifier S2C_TRADE_UPDATE    = id("s2c_trade_update");
    public static final Identifier S2C_TRADE_RESULT    = id("s2c_trade_result");
    public static final Identifier S2C_CARD_DRAWN      = id("s2c_card_drawn");
    public static final Identifier S2C_CARD_SUMMONED   = id("s2c_card_summoned");
    public static final Identifier S2C_CARD_FAINTED    = id("s2c_card_fainted");
    public static final Identifier S2C_ATTACK_ANIM     = id("s2c_attack_anim");
    public static final Identifier S2C_PLAYER_DATA     = id("s2c_player_data");

    private static Identifier id(String path) {
        return new Identifier(MinemonsMain.MOD_ID, path);
    }

    /** Register handlers that process packets arriving at the server. */
    public static void registerServerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(DUEL_ACTION,        DuelPacketHandlers::handleDuelAction);
        ServerPlayNetworking.registerGlobalReceiver(DUEL_ACCEPT,        DuelPacketHandlers::handleDuelAccept);
        ServerPlayNetworking.registerGlobalReceiver(DECK_SAVE,          DeckPacketHandlers::handleDeckSave);
        ServerPlayNetworking.registerGlobalReceiver(STORE_BUY,          StorePacketHandlers::handleStoreBuy);
        ServerPlayNetworking.registerGlobalReceiver(TRADE_REQUEST,      TradePacketHandlers::handleTradeRequest);
        ServerPlayNetworking.registerGlobalReceiver(TRADE_OFFER_UPDATE, TradePacketHandlers::handleTradeOfferUpdate);
        ServerPlayNetworking.registerGlobalReceiver(TRADE_CONFIRM,      TradePacketHandlers::handleTradeConfirm);
        ServerPlayNetworking.registerGlobalReceiver(TRADE_FINALIZE,     TradePacketHandlers::handleTradeFinalize);
        ServerPlayNetworking.registerGlobalReceiver(TRADE_RESPONSE,     TradePacketHandlers::handleTradeResponse);
    }

    /** Register handlers that process packets arriving at the client. */
    @Environment(EnvType.CLIENT)
    public static void registerClientPackets() {
        ClientPlayNetworking.registerGlobalReceiver(S2C_DUEL_START,     ClientPacketHandlers::onDuelStart);
        ClientPlayNetworking.registerGlobalReceiver(S2C_DUEL_UPDATE,    ClientPacketHandlers::onDuelUpdate);
        ClientPlayNetworking.registerGlobalReceiver(S2C_DUEL_END,       ClientPacketHandlers::onDuelEnd);
        ClientPlayNetworking.registerGlobalReceiver(S2C_DUEL_INVITE,    ClientPacketHandlers::onDuelInvite);
        ClientPlayNetworking.registerGlobalReceiver(S2C_OPEN_DECK_UI,   ClientPacketHandlers::onOpenDeckUi);
        ClientPlayNetworking.registerGlobalReceiver(S2C_OPEN_STORE_UI,  ClientPacketHandlers::onOpenStoreUi);
        ClientPlayNetworking.registerGlobalReceiver(S2C_TRADE_OPEN,     ClientPacketHandlers::onTradeOpen);
        ClientPlayNetworking.registerGlobalReceiver(S2C_TRADE_UPDATE,   ClientPacketHandlers::onTradeUpdate);
        ClientPlayNetworking.registerGlobalReceiver(S2C_TRADE_RESULT,   ClientPacketHandlers::onTradeResult);
        ClientPlayNetworking.registerGlobalReceiver(S2C_CARD_DRAWN,     ClientPacketHandlers::onCardDrawn);
        ClientPlayNetworking.registerGlobalReceiver(S2C_CARD_SUMMONED,  ClientPacketHandlers::onCardSummoned);
        ClientPlayNetworking.registerGlobalReceiver(S2C_CARD_FAINTED,   ClientPacketHandlers::onCardFainted);
        ClientPlayNetworking.registerGlobalReceiver(S2C_ATTACK_ANIM,    ClientPacketHandlers::onAttackAnim);
        ClientPlayNetworking.registerGlobalReceiver(S2C_PLAYER_DATA,    ClientPacketHandlers::onPlayerData);
    }
}
