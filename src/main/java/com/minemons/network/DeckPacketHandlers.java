package com.minemons.network;

import com.minemons.data.PlayerData;
import com.minemons.data.PlayerDataManager;
import com.minemons.data.PlayerDeck;
import com.minemons.registry.CardRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class DeckPacketHandlers {

    public static void handleDeckSave(MinecraftServer server, ServerPlayerEntity player,
                                       ServerPlayNetworkHandler handler, PacketByteBuf buf,
                                       PacketSender sender) {
        String deckName = buf.readString(64);
        int cardCount = buf.readInt();
        List<String> cardIds = new ArrayList<>();
        for (int i = 0; i < Math.min(cardCount, 60); i++) {
            cardIds.add(buf.readString(64));
        }

        server.execute(() -> {
            PlayerData data = PlayerDataManager.get(player);
            PlayerDeck deck = new PlayerDeck(deckName);

            for (String id : cardIds) {
                if (CardRegistry.getCard(id) == null) continue;
                if (data.getCardCount(id) <= 0) {
                    player.sendMessage(Text.literal("[Minemons] You don't own: " + id), false);
                    return;
                }
                deck.addCard(id);
            }

            if (!deck.isValid()) {
                player.sendMessage(Text.literal("[Minemons] Deck must be exactly 60 cards. Got: " + deck.getSize()), false);
                return;
            }

            // Replace existing deck with same name, or add new one
            List<PlayerDeck> decks = data.getDecks();
            boolean found = false;
            for (int i = 0; i < decks.size(); i++) {
                if (decks.get(i).getDeckName().equalsIgnoreCase(deckName)) {
                    decks.set(i, deck);
                    found = true;
                    break;
                }
            }
            if (!found) data.addDeck(deck);

            PlayerDataManager.markDirty(server);
            player.sendMessage(Text.literal("[Minemons] Deck '" + deckName + "' saved!"), false);
        });
    }
}
