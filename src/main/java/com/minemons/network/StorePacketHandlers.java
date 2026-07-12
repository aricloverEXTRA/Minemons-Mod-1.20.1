package com.minemons.network;

import com.minemons.data.PlayerData;
import com.minemons.data.PlayerDataManager;
import com.minemons.card.Card;
import com.minemons.card.Rarity;
import com.minemons.registry.CardRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.*;

public class StorePacketHandlers {

    public enum PackType {
        BASIC(5, 5),
        STANDARD(10, 8),
        PREMIUM(20, 12);

        public final int xpCost;
        public final int cardCount;
        PackType(int xp, int count) { this.xpCost = xp; this.cardCount = count; }
    }

    public static void handleStoreBuy(MinecraftServer server, ServerPlayerEntity player,
                                       ServerPlayNetworkHandler handler, PacketByteBuf buf,
                                       PacketSender sender) {
        String packName = buf.readString(32);

        server.execute(() -> {
            PackType pack;
            try { pack = PackType.valueOf(packName.toUpperCase()); }
            catch (IllegalArgumentException e) {
                player.sendMessage(Text.literal("[Minemons] Unknown pack: " + packName), false);
                return;
            }

            PlayerData data = PlayerDataManager.get(player);
            if (!data.spendXp(pack.xpCost)) {
                player.sendMessage(Text.literal("[Minemons] Not enough XP. Need "
                    + pack.xpCost + ", have " + data.getXp()), false);
                return;
            }

            List<String> pulled = pullCards(pack);
            data.addCards(pulled);
            PlayerDataManager.markDirty(server);

            // S2C_PLAYER_DATA format: int xp, int count, [string cardId × count]
            // Client uses xp to update the store display live
            PacketByteBuf resBuf = PacketByteBufs.create();
            resBuf.writeInt(data.getXp());       // updated XP after purchase
            resBuf.writeInt(pulled.size());
            for (String id : pulled) resBuf.writeString(id, 64);
            ServerPlayNetworking.send(player, PacketRegistry.S2C_PLAYER_DATA, resBuf);

            player.sendMessage(Text.literal("[Minemons] Pulled " + pulled.size()
                + " cards! XP left: " + data.getXp()), false);
        });
    }

    private static List<String> pullCards(PackType pack) {
        List<String> result = new ArrayList<>();
        Random rng = new Random();

        for (int i = 0; i < pack.cardCount; i++) {
            double roll = rng.nextDouble();
            Rarity rarity;
            if (pack == PackType.PREMIUM) {
                rarity = roll < 0.02 ? Rarity.MYTHIC
                       : roll < 0.08 ? Rarity.EPIC
                       : roll < 0.20 ? Rarity.RARE
                       : roll < 0.45 ? Rarity.UNCOMMON
                       : Rarity.COMMON;
            } else if (pack == PackType.STANDARD) {
                rarity = roll < 0.005 ? Rarity.MYTHIC
                       : roll < 0.03  ? Rarity.EPIC
                       : roll < 0.12  ? Rarity.RARE
                       : roll < 0.40  ? Rarity.UNCOMMON
                       : Rarity.COMMON;
            } else {
                rarity = roll < 0.001 ? Rarity.MYTHIC
                       : roll < 0.01  ? Rarity.EPIC
                       : roll < 0.06  ? Rarity.RARE
                       : roll < 0.30  ? Rarity.UNCOMMON
                       : Rarity.COMMON;
            }
            List<Card> pool = CardRegistry.getCardsByRarity(rarity);
            if (pool.isEmpty()) pool = new ArrayList<>(CardRegistry.getAllCards());
            result.add(pool.get(rng.nextInt(pool.size())).getId());
        }
        return result;
    }
}
