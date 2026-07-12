package com.minemons.command;

import com.minemons.data.PlayerData;
import com.minemons.data.PlayerDataManager;
import com.minemons.data.PlayerDeck;
import com.minemons.network.PacketRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

/**
 * /deck          — opens the deck builder UI
 * /deck list     — lists saved decks
 * /deck select <n> — set active deck
 * /deck new <name> — create empty deck
 * /deck delete <n> — delete a deck
 * /deck info     — show active deck info
 */
public class DeckCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("deck")
                .executes(ctx -> openDeck(ctx.getSource()))
                .then(CommandManager.literal("list")
                        .executes(ctx -> listDecks(ctx.getSource())))
                .then(CommandManager.literal("select")
                        .then(CommandManager.argument("index", IntegerArgumentType.integer(1, 10))
                                .executes(ctx -> selectDeck(ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "index") - 1))))
                .then(CommandManager.literal("new")
                        .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> newDeck(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "name")))))
                .then(CommandManager.literal("delete")
                        .then(CommandManager.argument("index", IntegerArgumentType.integer(1, 10))
                                .executes(ctx -> deleteDeck(ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "index") - 1))))
                .then(CommandManager.literal("info")
                        .executes(ctx -> deckInfo(ctx.getSource())))
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Packet format for S2C_OPEN_DECK_UI:
    //   int   ownedCount               — number of unique owned card IDs
    //   for each owned:
    //     string cardId
    //     int    count
    //   int   deckCount                — number of decks
    //   for each deck:
    //     string deckName
    //     int    cardEntries
    //     for each entry:
    //       string cardId
    //       int    count
    //   int   activeDeckIndex
    // ─────────────────────────────────────────────────────────────────────────

    private static int openDeck(ServerCommandSource src) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) return 0;

        PlayerData data = PlayerDataManager.get(player);
        PacketByteBuf buf = PacketByteBufs.create();

        // Owned cards
        Map<String, Integer> owned = data.getOwnedCards();
        buf.writeInt(owned.size());
        for (Map.Entry<String, Integer> e : owned.entrySet()) {
            buf.writeString(e.getKey(), 64);
            buf.writeInt(e.getValue());
        }

        // Decks
        buf.writeInt(data.getDecks().size());
        for (PlayerDeck deck : data.getDecks()) {
            buf.writeString(deck.getName(), 64);
            Map<String, Integer> cards = deck.getCards();
            buf.writeInt(cards.size());
            for (Map.Entry<String, Integer> e : cards.entrySet()) {
                buf.writeString(e.getKey(), 64);
                buf.writeInt(e.getValue());
            }
        }
        buf.writeInt(data.getActiveDeckIndex());

        ServerPlayNetworking.send(player, PacketRegistry.S2C_OPEN_DECK_UI, buf);
        return 1;
    }

    private static int listDecks(ServerCommandSource src) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) return 0;

        PlayerData data = PlayerDataManager.get(player);
        var decks = data.getDecks();

        if (decks.isEmpty()) {
            player.sendMessage(Text.literal("[Minemons] No decks yet. Use /deck new <name> to create one.").formatted(Formatting.YELLOW), false);
            return 1;
        }

        player.sendMessage(Text.literal("── Your Decks ──────────────────").formatted(Formatting.GOLD), false);
        for (int i = 0; i < decks.size(); i++) {
            PlayerDeck d = decks.get(i);
            int total = d.getCards().values().stream().mapToInt(Integer::intValue).sum();
            boolean active = i == data.getActiveDeckIndex();
            Formatting color = active ? Formatting.GREEN : Formatting.GRAY;
            String marker = active ? " ★ [ACTIVE]" : "";
            player.sendMessage(
                Text.literal("  " + (i+1) + ". " + d.getName() + " (" + total + "/60)" + marker).formatted(color), false);
        }
        return 1;
    }

    private static int selectDeck(ServerCommandSource src, int index) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) return 0;
        PlayerData data = PlayerDataManager.get(player);
        if (index < 0 || index >= data.getDecks().size()) {
            player.sendMessage(Text.literal("[Minemons] Invalid deck index.").formatted(Formatting.RED), false);
            return 0;
        }
        data.setActiveDeckIndex(index);
        player.sendMessage(Text.literal("[Minemons] Active deck: " + data.getDecks().get(index).getName()).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int newDeck(ServerCommandSource src, String name) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) return 0;
        PlayerData data = PlayerDataManager.get(player);
        if (data.getDecks().size() >= 10) {
            player.sendMessage(Text.literal("[Minemons] Max 10 decks allowed.").formatted(Formatting.RED), false);
            return 0;
        }
        if (name.length() > 32) {
            player.sendMessage(Text.literal("[Minemons] Deck name too long (max 32 chars).").formatted(Formatting.RED), false);
            return 0;
        }
        data.addDeck(new PlayerDeck(name));
        data.setActiveDeckIndex(data.getDecks().size() - 1);
        player.sendMessage(Text.literal("[Minemons] Created deck: " + name + ". Use /deck to build it.").formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int deleteDeck(ServerCommandSource src, int index) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) return 0;
        PlayerData data = PlayerDataManager.get(player);
        if (index < 0 || index >= data.getDecks().size()) {
            player.sendMessage(Text.literal("[Minemons] Invalid deck index.").formatted(Formatting.RED), false);
            return 0;
        }
        if (data.getDecks().size() == 1) {
            player.sendMessage(Text.literal("[Minemons] Can't delete your only deck.").formatted(Formatting.RED), false);
            return 0;
        }
        String name = data.getDecks().remove(index).getName();
        if (data.getActiveDeckIndex() >= data.getDecks().size())
            data.setActiveDeckIndex(data.getDecks().size() - 1);
        player.sendMessage(Text.literal("[Minemons] Deleted deck: " + name).formatted(Formatting.YELLOW), false);
        return 1;
    }

    private static int deckInfo(ServerCommandSource src) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) return 0;
        PlayerData data = PlayerDataManager.get(player);
        PlayerDeck active = data.getActiveDeck();
        if (active == null) {
            player.sendMessage(Text.literal("[Minemons] No active deck. Use /deck new <name>.").formatted(Formatting.YELLOW), false);
            return 1;
        }
        int total = active.getCards().values().stream().mapToInt(Integer::intValue).sum();
        player.sendMessage(Text.literal("── Deck: " + active.getName()).formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("  Cards: " + total + "/60").formatted(total == 60 ? Formatting.GREEN : Formatting.YELLOW), false);
        player.sendMessage(Text.literal("  Unique: " + active.getCards().size() + " | XP: " + data.getXp()).formatted(Formatting.AQUA), false);
        return 1;
    }
}
