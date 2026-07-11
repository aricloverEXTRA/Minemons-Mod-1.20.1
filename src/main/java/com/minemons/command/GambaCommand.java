package com.minemons.command;

import com.minemons.card.Card;
import com.minemons.card.Rarity;
import com.minemons.data.PlayerData;
import com.minemons.data.PlayerDataManager;
import com.minemons.network.PacketRegistry;
import com.minemons.registry.CardRegistry;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** /gamba — spend 15 XP for 15 random cards. */
public class GambaCommand {

    private static final int COST  = 15;
    private static final int CARDS = 15;

    private static final double[] THRESHOLDS = { 0.55, 0.85, 0.96, 0.99, 1.00 };
    private static final Rarity[] RARITIES   = { Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC, Rarity.MYTHIC };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("gamba").executes(ctx -> run(ctx.getSource())));
    }

    private static int run(ServerCommandSource src) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) return 0;

        PlayerData data = PlayerDataManager.get(player);
        if (data.getXp() < COST) {
            player.sendMessage(Text.literal("[Minemons] Need " + COST + " XP — you have " + data.getXp() + ".")
                    .formatted(Formatting.RED), false);
            return 0;
        }

        data.spendXp(COST);
        List<Card> pulled = pull(new Random());
        for (Card c : pulled) data.addCard(c.getId());
        PlayerDataManager.markDirty(player.getServer());

        player.sendMessage(Text.literal("§d✦ GAMBA ✦ §7(" + COST + " XP spent, " + data.getXp() + " remaining)"), false);
        for (Card c : pulled)
            player.sendMessage(Text.literal("  " + rarityColor(c.getRarity())
                + "[" + c.getRarity().displayName + "] §f" + c.getDisplayName()), false);

        // S2C_PLAYER_DATA: int xp, int count, [string cardId × count]
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(data.getXp());
        buf.writeInt(pulled.size());
        for (Card c : pulled) buf.writeString(c.getId(), 64);
        ServerPlayNetworking.send(player, PacketRegistry.S2C_PLAYER_DATA, buf);
        return 1;
    }

    private static List<Card> pull(Random rng) {
        List<List<Card>> pools = new ArrayList<>();
        for (Rarity r : RARITIES) pools.add(CardRegistry.getCardsByRarity(r));

        List<Card> result = new ArrayList<>();
        for (int i = 0; i < CARDS; i++) {
            double roll = rng.nextDouble();
            int ri = RARITIES.length - 1;
            for (int j = 0; j < THRESHOLDS.length; j++) {
                if (roll < THRESHOLDS[j]) { ri = j; break; }
            }
            List<Card> pool = pools.get(ri).isEmpty() ? pools.get(0) : pools.get(ri);
            result.add(pool.get(rng.nextInt(pool.size())));
        }
        return result;
    }

    private static String rarityColor(Rarity r) {
        return switch (r) {
            case COMMON   -> "§7";
            case UNCOMMON -> "§a";
            case RARE     -> "§9";
            case EPIC     -> "§5";
            case MYTHIC   -> "§6";
        };
    }
}
