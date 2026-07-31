package com.minemons.command;

import com.minemons.battle.DuelManager;
import com.minemons.network.PacketRegistry;
import com.minemons.network.TradePacketHandlers;
import com.minemons.trade.TradeManager;
import com.minemons.trade.TradeSession;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandSource;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * /decktrade <player> — send trade request
 * /decktrade accept   — accept pending request
 * /decktrade deny     — deny pending request
 * /decktrade cancel   — cancel active trade
 */
public class DeckTradeCommand {

    private static final SuggestionProvider<ServerCommandSource> ONLINE_PLAYERS =
            (ctx, builder) -> CommandSource.suggestMatching(
                    ctx.getSource().getServer().getPlayerManager()
                            .getPlayerList().stream().map(p -> p.getName().getString())
                            .filter(n -> !n.equals(ctx.getSource().getName())),
                    builder);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("decktrade")
                .then(CommandManager.argument("target", StringArgumentType.word())
                        .suggests(ONLINE_PLAYERS)
                        .executes(ctx -> sendRequest(ctx.getSource(), StringArgumentType.getString(ctx, "target"))))
                .then(CommandManager.literal("accept")
                        .executes(ctx -> respond(ctx.getSource(), true)))
                .then(CommandManager.literal("deny")
                        .executes(ctx -> respond(ctx.getSource(), false)))
                .then(CommandManager.literal("cancel")
                        .executes(ctx -> cancel(ctx.getSource())))
        );
    }

    // ─────────────────────────────────────────────────────────────────────────

    private static int sendRequest(ServerCommandSource src, String targetName) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) return 0;

        if (DuelManager.isInDuel(player)) {
            player.sendMessage(Text.literal("[Minemons] Can't trade while in a duel.").formatted(Formatting.RED), false);
            return 0;
        }
        ServerPlayerEntity target = src.getServer().getPlayerManager().getPlayer(targetName);
        if (target == null || target == player) {
            player.sendMessage(Text.literal("[Minemons] Player '" + targetName + "' not found.").formatted(Formatting.RED), false);
            return 0;
        }
        if (DuelManager.isInDuel(target)) {
            player.sendMessage(Text.literal("[Minemons] " + targetName + " is in a duel.").formatted(Formatting.RED), false);
            return 0;
        }
        if (TradeManager.getTradeFor(player) != null) {
            player.sendMessage(Text.literal("[Minemons] You already have an active trade.").formatted(Formatting.RED), false);
            return 0;
        }
        if (TradeManager.hasPendingRequest(target)) {
            player.sendMessage(Text.literal("[Minemons] " + targetName + " already has a pending trade request.").formatted(Formatting.RED), false);
            return 0;
        }

        TradeManager.sendRequest(player, target);
        player.sendMessage(Text.literal("[Minemons] Trade request sent to " + targetName + ".").formatted(Formatting.YELLOW), false);

        // Target: clickable invite message
        MutableText msg = Text.literal("[Minemons] ").formatted(Formatting.YELLOW)
                .append(Text.literal(player.getName().getString()).formatted(Formatting.GOLD))
                .append(Text.literal(" wants to trade! ").formatted(Formatting.YELLOW))
                .append(Text.literal("[Accept]").setStyle(Style.EMPTY.withColor(Formatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/decktrade accept"))))
                .append(Text.literal(" ").formatted(Formatting.WHITE))
                .append(Text.literal("[Deny]").setStyle(Style.EMPTY.withColor(Formatting.RED)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/decktrade deny"))));
        target.sendMessage(msg, false);

        // Send single-string S2C invite (client will show chat msg, not open screen)
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(player.getName().getString(), 32);
        // NOTE: only 1 string — ClientPacketHandlers.onTradeOpen checks isReadable() to detect this
        ServerPlayNetworking.send(target, PacketRegistry.S2C_TRADE_OPEN, buf);
        return 1;
    }

    private static int respond(ServerCommandSource src, boolean accepted) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) return 0;

        ServerPlayerEntity requester = TradeManager.getPendingRequester(player);
        if (requester == null) {
            player.sendMessage(Text.literal("[Minemons] No pending trade request.").formatted(Formatting.RED), false);
            return 0;
        }

        TradeManager.cancelPendingRequest(player);

        if (!accepted) {
            player.sendMessage(Text.literal("[Minemons] Trade declined.").formatted(Formatting.YELLOW), false);
            requester.sendMessage(Text.literal("[Minemons] " + player.getName().getString() + " declined your trade.").formatted(Formatting.YELLOW), false);
            return 1;
        }

        if (DuelManager.isInDuel(player) || DuelManager.isInDuel(requester)) {
            player.sendMessage(Text.literal("[Minemons] Can't trade — someone is in a duel.").formatted(Formatting.RED), false);
            return 0;
        }
        if (TradeManager.getTradeFor(player) != null || TradeManager.getTradeFor(requester) != null) {
            player.sendMessage(Text.literal("[Minemons] Someone already has an active trade.").formatted(Formatting.RED), false);
            return 0;
        }

        TradeSession session = TradeManager.openTrade(requester, player);

        // Send full session open packet to BOTH (3 strings → triggers screen open)
        for (ServerPlayerEntity p : new ServerPlayerEntity[]{requester, player}) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(session.getSessionId().toString(), 64);
            buf.writeString(requester.getName().getString(), 32);
            buf.writeString(player.getName().getString(), 32);
            ServerPlayNetworking.send(p, PacketRegistry.S2C_TRADE_OPEN, buf);
        }

        player.sendMessage(Text.literal("[Minemons] Trade opened with " + requester.getName().getString() + "!").formatted(Formatting.GREEN), false);
        requester.sendMessage(Text.literal("[Minemons] Trade opened with " + player.getName().getString() + "!").formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int cancel(ServerCommandSource src) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) return 0;

        TradeSession session = TradeManager.getTradeFor(player);
        if (session == null) {
            player.sendMessage(Text.literal("[Minemons] No active trade.").formatted(Formatting.RED), false);
            return 0;
        }

        ServerPlayerEntity other = session.getOtherPlayer(player);
        TradeManager.cancelTrade(session.getSessionId());

        TradePacketHandlers.sendTradeResult(player, "CANCELLED");
        if (other != null) TradePacketHandlers.sendTradeResult(other, "CANCELLED");

        player.sendMessage(Text.literal("[Minemons] Trade cancelled.").formatted(Formatting.YELLOW), false);
        return 1;
    }
}
