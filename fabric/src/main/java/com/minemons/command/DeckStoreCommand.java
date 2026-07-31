package com.minemons.command;

import com.minemons.data.PlayerData;
import com.minemons.data.PlayerDataManager;
import com.minemons.network.PacketRegistry;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * /deckstore — opens the card pack store UI.
 *
 * Packet format for S2C_OPEN_STORE_UI (matches ClientPacketHandlers.onOpenStoreUi):
 *   int xp
 *   int packCount
 *   for each pack:
 *     string id
 *     int    xpCost
 *     int    cardCount
 *     string description
 */
public class DeckStoreCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("deckstore")
                .executes(ctx -> openStore(ctx.getSource()))
        );
    }

    private static int openStore(ServerCommandSource src) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) return 0;

        PlayerData data = PlayerDataManager.get(player);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(data.getXp());

        // Pack definitions
        Object[][] packs = {
            { "BASIC",    5,  5,  "Common & Uncommon cards. Great for beginners." },
            { "STANDARD", 10, 8,  "Includes Rare cards. Expands themed decks." },
            { "PREMIUM",  20, 12, "Epic & Mythic possible. Best odds in the store!" },
        };

        buf.writeInt(packs.length);
        for (Object[] pack : packs) {
            buf.writeString((String)pack[0], 32);
            buf.writeInt((int)pack[1]);
            buf.writeInt((int)pack[2]);
            buf.writeString((String)pack[3], 128);
        }

        ServerPlayNetworking.send(player, PacketRegistry.S2C_OPEN_STORE_UI, buf);
        player.sendMessage(
            Text.literal("[Minemons] Store opened. You have ").formatted(Formatting.YELLOW)
                .append(Text.literal(data.getXp() + " XP.").formatted(Formatting.AQUA)),
            false);
        return 1;
    }
}
