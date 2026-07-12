package com.minemons.network;

import com.minemons.battle.DuelManager;
import com.minemons.battle.DuelState;
import com.minemons.data.PlayerData;
import com.minemons.data.PlayerDataManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DuelPacketHandlers {

    public static void handleDuelAction(net.minecraft.server.MinecraftServer server,
                                         ServerPlayerEntity player,
                                         net.minecraft.server.network.ServerPlayNetworkHandler handler,
                                         PacketByteBuf buf,
                                         net.fabricmc.fabric.api.networking.v1.PacketSender sender) {
        String action = buf.readString(64);
        int param1 = buf.readInt();
        int param2 = buf.readInt();

        server.execute(() -> {
            DuelState duel = DuelManager.getDuelFor(player);
            if (duel == null) {
                player.sendMessage(Text.literal("[Minemons] You are not in a duel."), false);
                return;
            }

            DuelState.PlayerSide side = duel.sides[0].playerId.equals(player.getUuid())
                    ? duel.sides[0] : duel.sides[1];
            DuelState.PlayerSide opponent = side == duel.sides[0] ? duel.sides[1] : duel.sides[0];

            if (!duel.getCurrentSide().playerId.equals(player.getUuid())) {
                player.sendMessage(Text.literal("[Minemons] It's not your turn."), false);
                return;
            }

            switch (action) {
                case "ATTACK" -> {
                    DuelState.AttackResult result = duel.performAttack();
                    broadcastDuelUpdate(duel, result.message);
                    if (duel.phase == DuelState.Phase.END) {
                        endDuelWithWinner(duel, duel.getCurrentSide().player, opponent.player);
                    }
                }
                case "SUMMON" -> {
                    boolean ok = duel.summonFromHand(side, param1, param2 == 1);
                    if (ok) broadcastDuelUpdate(duel, player.getName().getString() + " summoned a Minemon.");
                }
                case "SWAP" -> {
                    boolean ok = duel.swapToActive(side, param1);
                    if (ok) broadcastDuelUpdate(duel, "Swapped active Minemon.");
                }
                case "PLAY_CARD" -> {
                    CardActionProcessor.processHandCard(duel, side, param1);
                    broadcastDuelUpdate(duel, "Card played.");
                }
                case "END_TURN" -> {
                    duel.endTurn();
                    broadcastDuelUpdate(duel, player.getName().getString() + " ended their turn.");
                }
                default -> player.sendMessage(Text.literal("[Minemons] Unknown action: " + action), false);
            }
        });
    }

    public static void handleDuelAccept(net.minecraft.server.MinecraftServer server,
                                         ServerPlayerEntity player,
                                         net.minecraft.server.network.ServerPlayNetworkHandler handler,
                                         PacketByteBuf buf,
                                         net.fabricmc.fabric.api.networking.v1.PacketSender sender) {
        String challengerName = buf.readString(32);
        boolean accepted = buf.readBoolean();

        server.execute(() -> {
            ServerPlayerEntity challenger = server.getPlayerManager().getPlayer(challengerName);
            if (challenger == null) {
                player.sendMessage(Text.literal("[Minemons] That player is no longer online."), false);
                return;
            }

            if (!accepted) {
                challenger.sendMessage(Text.literal("[Minemons] " + player.getName().getString() + " declined your duel request."), false);
                return;
            }

            if (DuelManager.isInDuel(player) || DuelManager.isInDuel(challenger)) {
                player.sendMessage(Text.literal("[Minemons] One of you is already in a duel."), false);
                return;
            }

            DuelState duel = DuelManager.startDuel(challenger, player);
            sendDuelStartPackets(duel);
        });
    }

    private static void broadcastDuelUpdate(DuelState duel, String message) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(message, 256);
        writeDuelState(buf, duel);

        for (DuelState.PlayerSide side : duel.sides) {
            ServerPlayNetworking.send(side.player, PacketRegistry.S2C_DUEL_UPDATE, buf);
        }
    }

    private static void sendDuelStartPackets(DuelState duel) {
        for (int i = 0; i < 2; i++) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(i);
            writeDuelState(buf, duel);
            ServerPlayNetworking.send(duel.sides[i].player, PacketRegistry.S2C_DUEL_START, buf);
        }
    }

    // ⭐ FIXED: now public + static
    public static void writeDuelState(PacketByteBuf buf, DuelState duel) {
        buf.writeString(duel.phase.name(), 32);
        buf.writeInt(duel.currentTurn);

        for (DuelState.PlayerSide side : duel.sides) {
            buf.writeString(side.player.getName().getString(), 32);
            buf.writeInt(side.hand.size());
            buf.writeInt(side.deck.size());
            buf.writeInt(side.prizeCards.size());
            buf.writeInt(side.playerHp);
            buf.writeBoolean(side.playerIsActive);

            buf.writeBoolean(side.activeCard != null);
            if (side.activeCard != null) {
                buf.writeString(side.activeCard.cardId, 64);
                buf.writeInt(side.activeCard.currentHp);
                buf.writeInt(side.activeCard.attackBonus);
                buf.writeInt(side.activeCard.shieldHp);
            }

            buf.writeInt(side.fieldCards.size());
            for (DuelState.ActiveMinemon m : side.fieldCards) {
                buf.writeString(m.cardId, 64);
                buf.writeInt(m.currentHp);
            }

            buf.writeInt(side.hand.size());
            for (String id : side.hand) buf.writeString(id, 64);
        }
    }

    private static void endDuelWithWinner(DuelState duel, ServerPlayerEntity winner, ServerPlayerEntity loser) {
        PlayerData winnerData = PlayerDataManager.get(winner);
        winnerData.addXp(5);
        PlayerDataManager.markDirty(winner.getServer());

        for (DuelState.PlayerSide side : duel.sides) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(winner.getName().getString(), 32);
            ServerPlayNetworking.send(side.player, PacketRegistry.S2C_DUEL_END, buf);
        }

        DuelManager.endDuel(duel.duelId);
    }
}
