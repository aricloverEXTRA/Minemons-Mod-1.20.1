package com.minemons.command;

import com.minemons.battle.DuelManager;
import com.minemons.battle.DuelState;
import com.minemons.card.Card;
import com.minemons.card.Rarity;
import com.minemons.data.PlayerData;
import com.minemons.data.PlayerDataManager;
import com.minemons.data.PlayerDeck;
import com.minemons.network.PacketRegistry;
import com.minemons.network.TradePacketHandlers;
import com.minemons.registry.CardRegistry;
import com.minemons.trade.TradeManager;
import com.minemons.trade.TradeSession;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Single unified command: /minemons <subcommand>
 *
 * Subcommands:
 *   /mn duel <player>      — challenge a player
 *   /mn duel accept/deny/forfeit
 *   /mn deck               — open deck builder UI
 *   /mn deck list/new/select/delete/info
 *   /mn store              — open card pack store UI
 *   /mn trade <player>     — open trade with player
 *   /mn trade accept/deny/cancel
 *   /mn gamba              — spend 15 XP for 15 cards
 *   /mn profile            — show your stats
 *
 * Short alias: /mn
 */
public class MinemonsCommand {

    private static final SuggestionProvider<ServerCommandSource> ONLINE =
            (ctx, b) -> CommandSource.suggestMatching(
                ctx.getSource().getServer().getPlayerManager().getPlayerList()
                   .stream().map(p -> p.getName().getString())
                   .filter(n -> !n.equals(ctx.getSource().getName())), b);

    // Gamba constants
    private static final int GAMBA_COST = 15, GAMBA_CARDS = 15;
    private static final double[] THRESHOLDS = {0.55, 0.85, 0.96, 0.99, 1.00};
    private static final Rarity[] RARITIES = {Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC, Rarity.MYTHIC};

    public static void register(CommandDispatcher<ServerCommandSource> d) {
        var root = CommandManager.literal("mn").executes(ctx -> help(ctx.getSource()));
        var mn   = CommandManager.literal("minemons").executes(ctx -> help(ctx.getSource()));

        // ── Duel ────────────────────────────────────────────────────────────
        root.then(CommandManager.literal("duel")
            .then(CommandManager.argument("target", StringArgumentType.word()).suggests(ONLINE)
                .executes(ctx -> duelChallenge(ctx.getSource(), StringArgumentType.getString(ctx, "target"))))
            .then(CommandManager.literal("accept").executes(ctx -> duelRespond(ctx.getSource(), true)))
            .then(CommandManager.literal("deny")  .executes(ctx -> duelRespond(ctx.getSource(), false)))
            .then(CommandManager.literal("forfeit").executes(ctx -> duelForfeit(ctx.getSource()))));

        // ── Deck ────────────────────────────────────────────────────────────
        root.then(CommandManager.literal("deck")
            .executes(ctx -> deckOpen(ctx.getSource()))
            .then(CommandManager.literal("list")  .executes(ctx -> deckList(ctx.getSource())))
            .then(CommandManager.literal("info")  .executes(ctx -> deckInfo(ctx.getSource())))
            .then(CommandManager.literal("new")
                .then(CommandManager.argument("name", StringArgumentType.greedyString())
                    .executes(ctx -> deckNew(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
            .then(CommandManager.literal("select")
                .then(CommandManager.argument("index", IntegerArgumentType.integer(1, 10))
                    .executes(ctx -> deckSelect(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "index") - 1))))
            .then(CommandManager.literal("delete")
                .then(CommandManager.argument("index", IntegerArgumentType.integer(1, 10))
                    .executes(ctx -> deckDelete(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "index") - 1)))));

        // ── Store ────────────────────────────────────────────────────────────
        root.then(CommandManager.literal("store").executes(ctx -> storeOpen(ctx.getSource())));

        // ── Trade ────────────────────────────────────────────────────────────
        root.then(CommandManager.literal("trade")
            .then(CommandManager.argument("target", StringArgumentType.word()).suggests(ONLINE)
                .executes(ctx -> tradeRequest(ctx.getSource(), StringArgumentType.getString(ctx, "target"))))
            .then(CommandManager.literal("accept").executes(ctx -> tradeRespond(ctx.getSource(), true)))
            .then(CommandManager.literal("deny")  .executes(ctx -> tradeRespond(ctx.getSource(), false)))
            .then(CommandManager.literal("cancel").executes(ctx -> tradeCancel(ctx.getSource()))));

        // ── Gamba ────────────────────────────────────────────────────────────
        root.then(CommandManager.literal("gamba").executes(ctx -> gamba(ctx.getSource())));

        // ── Profile ──────────────────────────────────────────────────────────
        root.then(CommandManager.literal("profile").executes(ctx -> profile(ctx.getSource())));

        // Register both /mn and /minemons
        mn.then(CommandManager.literal("duel")
            .then(CommandManager.argument("target", StringArgumentType.word()).suggests(ONLINE)
                .executes(ctx -> duelChallenge(ctx.getSource(), StringArgumentType.getString(ctx, "target"))))
            .then(CommandManager.literal("accept").executes(ctx -> duelRespond(ctx.getSource(), true)))
            .then(CommandManager.literal("deny")  .executes(ctx -> duelRespond(ctx.getSource(), false)))
            .then(CommandManager.literal("forfeit").executes(ctx -> duelForfeit(ctx.getSource()))));
        mn.then(CommandManager.literal("deck")
            .executes(ctx -> deckOpen(ctx.getSource()))
            .then(CommandManager.literal("list").executes(ctx -> deckList(ctx.getSource())))
            .then(CommandManager.literal("info").executes(ctx -> deckInfo(ctx.getSource())))
            .then(CommandManager.literal("new")
                .then(CommandManager.argument("name", StringArgumentType.greedyString())
                    .executes(ctx -> deckNew(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
            .then(CommandManager.literal("select")
                .then(CommandManager.argument("index", IntegerArgumentType.integer(1, 10))
                    .executes(ctx -> deckSelect(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "index") - 1))))
            .then(CommandManager.literal("delete")
                .then(CommandManager.argument("index", IntegerArgumentType.integer(1, 10))
                    .executes(ctx -> deckDelete(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "index") - 1)))));
        mn.then(CommandManager.literal("store").executes(ctx -> storeOpen(ctx.getSource())));
        mn.then(CommandManager.literal("trade")
            .then(CommandManager.argument("target", StringArgumentType.word()).suggests(ONLINE)
                .executes(ctx -> tradeRequest(ctx.getSource(), StringArgumentType.getString(ctx, "target"))))
            .then(CommandManager.literal("accept").executes(ctx -> tradeRespond(ctx.getSource(), true)))
            .then(CommandManager.literal("deny")  .executes(ctx -> tradeRespond(ctx.getSource(), false)))
            .then(CommandManager.literal("cancel").executes(ctx -> tradeCancel(ctx.getSource()))));
        mn.then(CommandManager.literal("gamba").executes(ctx -> gamba(ctx.getSource())));
        mn.then(CommandManager.literal("profile").executes(ctx -> profile(ctx.getSource())));

        d.register(root);
        d.register(mn);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Help
    // ─────────────────────────────────────────────────────────────────────────
    private static int help(ServerCommandSource src) {
        ServerPlayerEntity p = src.getPlayer(); if (p == null) return 0;
        p.sendMessage(Text.literal("§b── Minemons Commands ──────────────────────────────────────────"), false);
        p.sendMessage(Text.literal("§e/mn duel <player>  §7Challenge someone to a duel"), false);
        p.sendMessage(Text.literal("§e/mn duel accept/deny/forfeit  §7Respond to a duel invite"), false);
        p.sendMessage(Text.literal("§e/mn deck  §7Open deck builder"), false);
        p.sendMessage(Text.literal("§e/mn deck list/new/select/delete/info  §7Manage decks"), false);
        p.sendMessage(Text.literal("§e/mn store  §7Open card pack store"), false);
        p.sendMessage(Text.literal("§e/mn trade <player>  §7Trade cards with a player"), false);
        p.sendMessage(Text.literal("§e/mn gamba  §7Spend 15 XP for 15 random cards"), false);
        p.sendMessage(Text.literal("§e/mn profile  §7View your stats and XP"), false);
        p.sendMessage(Text.literal("§7Also available as §e/minemons§7. Short alias: §e/mn"), false);
        return 1;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Duel
    // ─────────────────────────────────────────────────────────────────────────
    private static int duelChallenge(ServerCommandSource src, String targetName) {
        ServerPlayerEntity player = src.getPlayer(); if (player == null) return 0;
        if (DuelManager.isInDuel(player)) { err(player, "You're already in a duel."); return 0; }
        ServerPlayerEntity target = src.getServer().getPlayerManager().getPlayer(targetName);
        if (target == null || target == player) { err(player, "Player not found."); return 0; }
        if (DuelManager.isInDuel(target)) { err(player, targetName + " is already in a duel."); return 0; }
        PlayerData pd = PlayerDataManager.get(player);
        if (pd.getActiveDeck() == null) { err(player, "You need a deck first. Use /mn deck."); return 0; }
        DuelManager.storePendingInvite(player, target);
        player.sendMessage(Text.literal("§6[Minemons] §eDuel request sent to §f" + targetName), false);
        Text msg = Text.literal("§6[Minemons] §f" + player.getName().getString() + " §echallenges you! ")
            .append(Text.literal("§a[Accept]").setStyle(Style.EMPTY.withColor(Formatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mn duel accept"))))
            .append(Text.literal(" §c[Deny]").setStyle(Style.EMPTY.withColor(Formatting.RED)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mn duel deny"))));
        target.sendMessage(msg, false);
        PacketByteBuf buf = PacketByteBufs.create(); buf.writeString(player.getName().getString(), 32);
        ServerPlayNetworking.send(target, PacketRegistry.S2C_DUEL_INVITE, buf);
        return 1;
    }

    private static int duelRespond(ServerCommandSource src, boolean accepted) {
        ServerPlayerEntity player = src.getPlayer(); if (player == null) return 0;
        ServerPlayerEntity challenger = DuelManager.getPendingChallenger(player);
        if (challenger == null) { err(player, "No pending duel request."); return 0; }
        DuelManager.clearPendingInvite(player);
        if (!accepted) {
            player.sendMessage(Text.literal("§6[Minemons] §cDuel declined."), false);
            challenger.sendMessage(Text.literal("§6[Minemons] §c" + player.getName().getString() + " declined."), false);
            return 1;
        }
        if (DuelManager.isInDuel(player) || DuelManager.isInDuel(challenger)) { err(player, "Someone is already in a duel."); return 0; }
        DuelState duel = DuelManager.startDuel(challenger, player);
        for (int i = 0; i < 2; i++) {
            PacketByteBuf b = PacketByteBufs.create(); b.writeInt(i);
            com.minemons.network.DuelPacketHandlers.writeDuelState(b, duel);
            ServerPlayNetworking.send(duel.sides[i].player, PacketRegistry.S2C_DUEL_START, b);
        }
        player.sendMessage(Text.literal("§6[Minemons] §aDuel started! Good luck!"), false);
        challenger.sendMessage(Text.literal("§6[Minemons] §aDuel started! Good luck!"), false);
        return 1;
    }

    private static int duelForfeit(ServerCommandSource src) {
        ServerPlayerEntity player = src.getPlayer(); if (player == null) return 0;
        DuelState duel = DuelManager.getDuelFor(player);
        if (duel == null) { err(player, "Not in a duel."); return 0; }
        DuelState.PlayerSide opp = duel.sides[0].playerId.equals(player.getUuid()) ? duel.sides[1] : duel.sides[0];
        for (DuelState.PlayerSide side : duel.sides) {
            PacketByteBuf b = PacketByteBufs.create(); b.writeString(opp.player.getName().getString(), 32);
            ServerPlayNetworking.send(side.player, PacketRegistry.S2C_DUEL_END, b);
        }
        PlayerDataManager.get(opp.player).addXp(5);
        PlayerDataManager.markDirty(player.getServer());
        DuelManager.endDuel(duel.duelId);
        player.sendMessage(Text.literal("§6[Minemons] §cYou forfeited."), false);
        return 1;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Deck
    // ─────────────────────────────────────────────────────────────────────────
    private static int deckOpen(ServerCommandSource src) {
        ServerPlayerEntity player = src.getPlayer(); if (player == null) return 0;
        PlayerData data = PlayerDataManager.get(player);
        PacketByteBuf buf = PacketByteBufs.create();
        Map<String, Integer> owned = data.getOwnedCards();
        buf.writeInt(owned.size());
        owned.forEach((k, v) -> { buf.writeString(k, 64); buf.writeInt(v); });
        buf.writeInt(data.getDecks().size());
        for (PlayerDeck deck : data.getDecks()) {
            buf.writeString(deck.getName(), 64);
            Map<String, Integer> cards = deck.getCards();
            buf.writeInt(cards.size());
            cards.forEach((k, v) -> { buf.writeString(k, 64); buf.writeInt(v); });
        }
        buf.writeInt(data.getActiveDeckIndex());
        ServerPlayNetworking.send(player, PacketRegistry.S2C_OPEN_DECK_UI, buf);
        return 1;
    }

    private static int deckList(ServerCommandSource src) {
        ServerPlayerEntity p = src.getPlayer(); if (p == null) return 0;
        PlayerData data = PlayerDataManager.get(p);
        if (data.getDecks().isEmpty()) { p.sendMessage(Text.literal("§6[Minemons] §7No decks. Use §e/mn deck new <name>"), false); return 1; }
        p.sendMessage(Text.literal("§b── Your Decks ─────────────────────────────────────────────"), false);
        for (int i = 0; i < data.getDecks().size(); i++) {
            PlayerDeck d = data.getDecks().get(i);
            int tot = d.getCards().values().stream().mapToInt(v -> v).sum();
            boolean active = i == data.getActiveDeckIndex();
            p.sendMessage(Text.literal("  §e" + (i+1) + ". §f" + d.getName() + " §7(" + tot + "/60)" + (active ? " §a★ ACTIVE" : "")).formatted(Formatting.RESET), false);
        }
        return 1;
    }

    private static int deckInfo(ServerCommandSource src) {
        ServerPlayerEntity p = src.getPlayer(); if (p == null) return 0;
        PlayerData data = PlayerDataManager.get(p); PlayerDeck active = data.getActiveDeck();
        if (active == null) { p.sendMessage(Text.literal("§6[Minemons] §7No active deck."), false); return 1; }
        int tot = active.getCards().values().stream().mapToInt(v -> v).sum();
        p.sendMessage(Text.literal("§b── Deck: §f" + active.getName() + " ─────────────────────────────────────"), false);
        p.sendMessage(Text.literal("  §7Cards: " + (tot == 60 ? "§a" : "§e") + tot + "/60  §7Unique: §f" + active.getCards().size()), false);
        p.sendMessage(Text.literal("  §7XP: §e" + data.getXp() + "  §7Owned unique: §f" + data.getOwnedCards().size()), false);
        return 1;
    }

    private static int deckNew(ServerCommandSource src, String name) {
        ServerPlayerEntity p = src.getPlayer(); if (p == null) return 0;
        PlayerData data = PlayerDataManager.get(p);
        if (data.getDecks().size() >= 10) { err(p, "Max 10 decks."); return 0; }
        if (name.length() > 32) { err(p, "Name too long (max 32)."); return 0; }
        data.addDeck(new PlayerDeck(name)); data.setActiveDeckIndex(data.getDecks().size()-1);
        p.sendMessage(Text.literal("§6[Minemons] §aCreated: §f" + name + ". §7Use §e/mn deck §7to build it."), false);
        return 1;
    }

    private static int deckSelect(ServerCommandSource src, int idx) {
        ServerPlayerEntity p = src.getPlayer(); if (p == null) return 0;
        PlayerData data = PlayerDataManager.get(p);
        if (idx < 0 || idx >= data.getDecks().size()) { err(p, "Invalid index."); return 0; }
        data.setActiveDeckIndex(idx);
        p.sendMessage(Text.literal("§6[Minemons] §aActive deck: §f" + data.getDecks().get(idx).getName()), false);
        return 1;
    }

    private static int deckDelete(ServerCommandSource src, int idx) {
        ServerPlayerEntity p = src.getPlayer(); if (p == null) return 0;
        PlayerData data = PlayerDataManager.get(p);
        if (idx < 0 || idx >= data.getDecks().size()) { err(p, "Invalid index."); return 0; }
        if (data.getDecks().size() == 1) { err(p, "Can't delete your only deck."); return 0; }
        String n = data.getDecks().remove(idx).getName();
        if (data.getActiveDeckIndex() >= data.getDecks().size()) data.setActiveDeckIndex(data.getDecks().size()-1);
        p.sendMessage(Text.literal("§6[Minemons] §cDeleted: §f" + n), false);
        return 1;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Store
    // ─────────────────────────────────────────────────────────────────────────
    private static int storeOpen(ServerCommandSource src) {
        ServerPlayerEntity player = src.getPlayer(); if (player == null) return 0;
        PlayerData data = PlayerDataManager.get(player);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(data.getXp());
        Object[][] packs = {
            {"BASIC",5,5,"Common & Uncommon. Great starter pack."},
            {"STANDARD",10,8,"Includes Rare cards."},
            {"PREMIUM",20,12,"Epics & Mythics possible!"}
        };
        buf.writeInt(packs.length);
        for (Object[] pk : packs) {
            buf.writeString((String)pk[0],32); buf.writeInt((int)pk[1]);
            buf.writeInt((int)pk[2]); buf.writeString((String)pk[3],128);
        }
        ServerPlayNetworking.send(player, PacketRegistry.S2C_OPEN_STORE_UI, buf);
        player.sendMessage(Text.literal("§6[Minemons] §7Store opened. XP: §e" + data.getXp()), false);
        return 1;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Trade
    // ─────────────────────────────────────────────────────────────────────────
    private static int tradeRequest(ServerCommandSource src, String targetName) {
        ServerPlayerEntity player = src.getPlayer(); if (player == null) return 0;
        if (DuelManager.isInDuel(player)) { err(player, "Can't trade while in a duel."); return 0; }
        ServerPlayerEntity target = src.getServer().getPlayerManager().getPlayer(targetName);
        if (target == null || target == player) { err(player, "Player not found."); return 0; }
        if (TradeManager.getTradeFor(player) != null) { err(player, "Already in a trade."); return 0; }
        if (TradeManager.hasPendingRequest(target)) { err(player, targetName + " already has a pending trade."); return 0; }
        TradeManager.sendRequest(player, target);
        player.sendMessage(Text.literal("§6[Minemons] §eTrade request sent to §f" + targetName), false);
        Text msg = Text.literal("§6[Minemons] §f" + player.getName().getString() + " §ewants to trade! ")
            .append(Text.literal("§a[Accept]").setStyle(Style.EMPTY.withColor(Formatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mn trade accept"))))
            .append(Text.literal(" §c[Deny]").setStyle(Style.EMPTY.withColor(Formatting.RED)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mn trade deny"))));
        target.sendMessage(msg, false);
        PacketByteBuf buf = PacketByteBufs.create(); buf.writeString(player.getName().getString(), 32);
        ServerPlayNetworking.send(target, PacketRegistry.S2C_TRADE_OPEN, buf);
        return 1;
    }

    private static int tradeRespond(ServerCommandSource src, boolean accepted) {
        ServerPlayerEntity player = src.getPlayer(); if (player == null) return 0;
        ServerPlayerEntity requester = TradeManager.getPendingRequester(player);
        if (requester == null) { err(player, "No pending trade request."); return 0; }
        TradeManager.cancelPendingRequest(player);
        if (!accepted) {
            player.sendMessage(Text.literal("§6[Minemons] §cTrade declined."), false);
            requester.sendMessage(Text.literal("§6[Minemons] §c" + player.getName().getString() + " declined your trade."), false);
            return 1;
        }
        TradeSession session = TradeManager.openTrade(requester, player);
        for (ServerPlayerEntity p2 : new ServerPlayerEntity[]{requester, player}) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(session.getSessionId().toString(), 64);
            buf.writeString(requester.getName().getString(), 32);
            buf.writeString(player.getName().getString(), 32);
            ServerPlayNetworking.send(p2, PacketRegistry.S2C_TRADE_OPEN, buf);
        }
        return 1;
    }

    private static int tradeCancel(ServerCommandSource src) {
        ServerPlayerEntity player = src.getPlayer(); if (player == null) return 0;
        TradeSession session = TradeManager.getTradeFor(player);
        if (session == null) { err(player, "No active trade."); return 0; }
        ServerPlayerEntity other = session.getOtherPlayer(player);
        TradeManager.cancelTrade(session.getSessionId());
        TradePacketHandlers.sendTradeResult(player, "CANCELLED");
        if (other != null) TradePacketHandlers.sendTradeResult(other, "CANCELLED");
        return 1;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Gamba
    // ─────────────────────────────────────────────────────────────────────────
    private static int gamba(ServerCommandSource src) {
        ServerPlayerEntity player = src.getPlayer(); if (player == null) return 0;
        PlayerData data = PlayerDataManager.get(player);
        if (data.getXp() < GAMBA_COST) { err(player, "Need " + GAMBA_COST + " XP — you have " + data.getXp()); return 0; }
        data.spendXp(GAMBA_COST);
        List<Card> pulled = gambaPull(new Random());
        for (Card c : pulled) data.addCard(c.getId());
        PlayerDataManager.markDirty(player.getServer());
        player.sendMessage(Text.literal("§d✦ GAMBA ✦ §7(" + GAMBA_COST + " XP spent, " + data.getXp() + " left)"), false);
        for (Card c : pulled)
            player.sendMessage(Text.literal("  " + rCol(c.getRarity()) + "[" + c.getRarity().displayName + "] §f" + c.getDisplayName()), false);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(data.getXp());
        buf.writeInt(pulled.size());
        for (Card c : pulled) buf.writeString(c.getId(), 64);
        ServerPlayNetworking.send(player, PacketRegistry.S2C_PLAYER_DATA, buf);
        return 1;
    }

    private static List<Card> gambaPull(Random rng) {
        List<List<Card>> pools = new ArrayList<>();
        for (Rarity r : RARITIES) pools.add(CardRegistry.getCardsByRarity(r));
        List<Card> result = new ArrayList<>();
        for (int i = 0; i < GAMBA_CARDS; i++) {
            double roll = rng.nextDouble(); int ri = RARITIES.length - 1;
            for (int j = 0; j < THRESHOLDS.length; j++) if (roll < THRESHOLDS[j]) { ri = j; break; }
            List<Card> pool = pools.get(ri).isEmpty() ? pools.get(0) : pools.get(ri);
            result.add(pool.get(rng.nextInt(pool.size())));
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Profile
    // ─────────────────────────────────────────────────────────────────────────
    private static int profile(ServerCommandSource src) {
        ServerPlayerEntity player = src.getPlayer(); if (player == null) return 0;
        PlayerData data = PlayerDataManager.get(player);
        player.sendMessage(Text.literal("§b── Minemons Profile: §f" + player.getName().getString() + " ─────────────"), false);
        player.sendMessage(Text.literal("  §7XP: §e" + data.getXp()), false);
        player.sendMessage(Text.literal("  §7Cards owned: §f" + data.getOwnedCards().size() + " unique, " +
            data.getOwnedCards().values().stream().mapToInt(v -> v).sum() + " total"), false);
        player.sendMessage(Text.literal("  §7Decks: §f" + data.getDecks().size()), false);
        PlayerDeck active = data.getActiveDeck();
        if (active != null) {
            int tot = active.getCards().values().stream().mapToInt(v -> v).sum();
            player.sendMessage(Text.literal("  §7Active deck: §e" + active.getName() + " §7(" + tot + "/60)"), false);
        }
        player.sendMessage(Text.literal("  §7Use §e/mn store §7to buy packs, §e/mn gamba §7for random cards"), false);
        return 1;
    }

    // ─────────────────────────────────────────────────────────────────────────
    private static void err(ServerPlayerEntity p, String msg) {
        p.sendMessage(Text.literal("§6[Minemons] §c" + msg), false);
    }
    private static String rCol(Rarity r) {
        return switch(r) { case COMMON->"§7"; case UNCOMMON->"§a"; case RARE->"§9"; case EPIC->"§5"; case MYTHIC->"§6"; };
    }
}
