package com.minemons.network;

import com.minemons.battle.DuelManager;
import com.minemons.trade.TradeManager;
import com.minemons.trade.TradeSession;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

public class TradePacketHandlers {

    public static void handleTradeRequest(MinecraftServer server, ServerPlayerEntity player,
                                           ServerPlayNetworkHandler handler, PacketByteBuf buf,
                                           PacketSender sender) {
        String targetName = buf.readString(32);
        server.execute(() -> {
            ServerPlayerEntity target = server.getPlayerManager().getPlayer(targetName);
            if (target == null) {
                player.sendMessage(Text.literal("[Minemons] Player not found."), false);
                return;
            }
            if (DuelManager.isInDuel(player) || DuelManager.isInDuel(target)) {
                player.sendMessage(Text.literal("[Minemons] Cannot trade during a duel."), false);
                return;
            }
            TradeManager.sendRequest(player, target);

            // Notify target with single-string invite packet (no screen, just chat prompt)
            PacketByteBuf notif = PacketByteBufs.create();
            notif.writeString(player.getName().getString(), 32);
            // No second or third read — ClientPacketHandlers.onTradeOpen checks isReadable()
            ServerPlayNetworking.send(target, PacketRegistry.S2C_TRADE_OPEN, notif);

            player.sendMessage(Text.literal("[Minemons] Trade request sent to " + targetName), false);
        });
    }

    public static void handleTradeResponse(MinecraftServer server, ServerPlayerEntity player,
                                            ServerPlayNetworkHandler handler, PacketByteBuf buf,
                                            PacketSender sender) {
        // Currently unused — /decktrade accept sends DUEL_ACCEPT-style flow through command layer
        // This handler catches any raw C2S trade_response packet from the UI Cancel button
        server.execute(() -> {
            TradeSession session = TradeManager.getTradeFor(player);
            if (session == null) return;
            ServerPlayerEntity other = session.getOtherPlayer(player);
            TradeManager.cancelTrade(session.getSessionId());

            sendTradeResult(player, "CANCELLED");
            if (other != null) sendTradeResult(other, "CANCELLED");
        });
    }

    public static void handleTradeOfferUpdate(MinecraftServer server, ServerPlayerEntity player,
                                               ServerPlayNetworkHandler handler, PacketByteBuf buf,
                                               PacketSender sender) {
        boolean adding = buf.readBoolean();
        String cardId = buf.readString(64);

        server.execute(() -> {
            TradeSession session = TradeManager.getTradeFor(player);
            if (session == null) return;
            if (adding) session.addOfferCard(player, cardId);
            else session.removeOfferCard(player, cardId);
            broadcastTradeUpdate(session);
        });
    }

    public static void handleTradeConfirm(MinecraftServer server, ServerPlayerEntity player,
                                           ServerPlayNetworkHandler handler, PacketByteBuf buf,
                                           PacketSender sender) {
        server.execute(() -> {
            TradeSession session = TradeManager.getTradeFor(player);
            if (session == null) return;
            session.confirm(player);
            broadcastTradeUpdate(session);

            if (session.isReadyToFinalize()) {
                boolean ok = TradeManager.finalizeTrade(session);
                String result = ok ? "COMPLETE" : "CANCELLED";
                sendTradeResult(session.playerA, result);
                sendTradeResult(session.playerB, result);
            }
        });
    }

    public static void handleTradeFinalize(MinecraftServer server, ServerPlayerEntity player,
                                            ServerPlayNetworkHandler handler, PacketByteBuf buf,
                                            PacketSender sender) {
        // Redundant finalize packet — handled by confirm above, no-op here
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static void broadcastTradeUpdate(TradeSession session) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(session.offerA.size());
        for (String id : session.offerA) buf.writeString(id, 64);
        buf.writeInt(session.offerB.size());
        for (String id : session.offerB) buf.writeString(id, 64);
        buf.writeString(session.getStatus().name(), 32);
        ServerPlayNetworking.send(session.playerA, PacketRegistry.S2C_TRADE_UPDATE, buf);
        ServerPlayNetworking.send(session.playerB, PacketRegistry.S2C_TRADE_UPDATE, buf);
    }

    /**
     * Sends S2C_TRADE_RESULT as a single string status.
     * Matches ClientPacketHandlers.onTradeResult which reads one string.
     */
    public static void sendTradeResult(ServerPlayerEntity player, String status) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(status, 32);
        ServerPlayNetworking.send(player, PacketRegistry.S2C_TRADE_RESULT, buf);
    }
}
