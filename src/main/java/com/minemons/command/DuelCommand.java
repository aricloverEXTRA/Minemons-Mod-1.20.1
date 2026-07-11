package com.minemons.command;

import com.minemons.api.DuelRulesProvider;
import com.minemons.battle.DuelManager;
import com.minemons.battle.DuelState;
import com.minemons.data.PlayerDataManager;
import com.minemons.network.DuelPacketHandlers;
import com.minemons.network.PacketRegistry;
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

public class DuelCommand {

    private static final SuggestionProvider<ServerCommandSource> ONLINE_PLAYERS =
            (context, builder) -> CommandSource.suggestMatching(
                    context.getSource().getServer().getPlayerManager()
                            .getPlayerList().stream()
                            .map(p -> p.getName().getString())
                            .filter(n -> !n.equals(context.getSource().getName())),
                    builder);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("duel")
                .then(CommandManager.argument("target", StringArgumentType.word())
                        .suggests(ONLINE_PLAYERS)
                        .executes(ctx -> challenge(ctx.getSource(), StringArgumentType.getString(ctx, "target"))))
                .then(CommandManager.literal("accept")
                        .executes(ctx -> respond(ctx.getSource(), true)))
                .then(CommandManager.literal("deny")
                        .executes(ctx -> respond(ctx.getSource(), false)))
                .then(CommandManager.literal("forfeit")
                        .executes(ctx -> forfeit(ctx.getSource())))
        );
    }

    // ──────────────────────────────────────────────────────────
    private static int challenge(ServerCommandSource src, String targetName) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) return 0;

        ServerPlayerEntity target = src.getServer().getPlayerManager().getPlayer(targetName);
        if (target == null || target == player) {
            player.sendMessage(Text.literal("[Minemons] Player '" + targetName + "' not found.")
                    .formatted(Formatting.RED), false);
            return 0;
        }

        if (DuelManager.isInDuel(player)) {
            player.sendMessage(Text.literal("[Minemons] You are already in a duel.")
                    .formatted(Formatting.RED), false);
            return 0;
        }
        if (DuelManager.isInDuel(target)) {
            player.sendMessage(Text.literal("[Minemons] " + targetName + " is already in a duel.")
                    .formatted(Formatting.RED), false);
            return 0;
        }

        // NEW: server‑defined rules
        DuelRulesProvider rules = DuelManager.getRules();
        if (!rules.canChallenge(player, target)) {
            player.sendMessage(Text.literal("[Minemons] You cannot challenge this player.")
                    .formatted(Formatting.RED), false);
            return 0;
        }

        DuelManager.storePendingInvite(player, target);

        player.sendMessage(
            Text.literal("[Minemons] Duel request sent to ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal(targetName).formatted(Formatting.GOLD)),
            false
        );

        MutableText msg = Text.literal("[Minemons] ").formatted(Formatting.YELLOW)
                .append(Text.literal(player.getName().getString()).formatted(Formatting.GOLD))
                .append(Text.literal(" challenges you to a duel! ").formatted(Formatting.YELLOW));

        MutableText accept = Text.literal("[Accept]").setStyle(
                Style.EMPTY.withColor(Formatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel accept"))
                        .withBold(true));

        MutableText deny = Text.literal(" [Deny]").setStyle(
                Style.EMPTY.withColor(Formatting.RED)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel deny"))
                        .withBold(true));

        target.sendMessage(msg.append(accept).append(deny), false);

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(player.getName().getString(), 32);
        ServerPlayNetworking.send(target, PacketRegistry.S2C_DUEL_INVITE, buf);

        return 1;
    }

    // ──────────────────────────────────────────────────────────
    private static int respond(ServerCommandSource src, boolean accepted) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) return 0;

        ServerPlayerEntity challenger = DuelManager.getPendingChallenger(player);
        if (challenger == null) {
            player.sendMessage(Text.literal("[Minemons] You have no pending duel request.")
                    .formatted(Formatting.RED), false);
            return 0;
        }

        DuelManager.clearPendingInvite(player);

        if (!accepted) {
            player.sendMessage(Text.literal("[Minemons] You declined the duel.")
                    .formatted(Formatting.YELLOW), false);
            challenger.sendMessage(Text.literal("[Minemons] " + player.getName().getString() +
                    " declined your duel.").formatted(Formatting.YELLOW), false);
            return 1;
        }

        if (DuelManager.isInDuel(player) || DuelManager.isInDuel(challenger)) {
            player.sendMessage(Text.literal("[Minemons] One of you is already in a duel.")
                    .formatted(Formatting.RED), false);
            return 0;
        }

        DuelState duel = DuelManager.startDuel(challenger, player);
        if (duel == null) {
            player.sendMessage(Text.literal("[Minemons] Duel could not start.")
                    .formatted(Formatting.RED), false);
            return 0;
        }

        for (int i = 0; i < 2; i++) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(i);
            DuelPacketHandlers.writeDuelState(buf, duel);
            ServerPlayNetworking.send(duel.sides[i].player, PacketRegistry.S2C_DUEL_START, buf);
        }

        player.sendMessage(Text.literal("[Minemons] Duel started! Good luck!")
                .formatted(Formatting.GREEN), false);
        challenger.sendMessage(Text.literal("[Minemons] Duel started! Good luck!")
                .formatted(Formatting.GREEN), false);

        return 1;
    }

    // ──────────────────────────────────────────────────────────
    private static int forfeit(ServerCommandSource src) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) return 0;

        DuelState duel = DuelManager.getDuelFor(player);
        if (duel == null) {
            player.sendMessage(Text.literal("[Minemons] You are not in a duel.")
                    .formatted(Formatting.RED), false);
            return 0;
        }

        DuelState.PlayerSide mySide =
                duel.sides[0].playerId.equals(player.getUuid()) ? duel.sides[0] : duel.sides[1];
        DuelState.PlayerSide opponentSide =
                (mySide == duel.sides[0]) ? duel.sides[1] : duel.sides[0];

        for (DuelState.PlayerSide side : duel.sides) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(opponentSide.player.getName().getString(), 32);
            ServerPlayNetworking.send(side.player, PacketRegistry.S2C_DUEL_END, buf);
        }

        opponentSide.player.sendMessage(
            Text.literal("[Minemons] " + player.getName().getString() +
                    " forfeited. You win!").formatted(Formatting.GREEN), false);
        player.sendMessage(Text.literal("[Minemons] You forfeited the duel.")
                .formatted(Formatting.RED), false);

        int xp = DuelManager.getXpReward(opponentSide.player, duel);
        PlayerDataManager.get(opponentSide.player).addXp(xp);
        PlayerDataManager.markDirty(opponentSide.player.getServer());

        DuelManager.endDuel(duel.duelId);
        return 1;
    }
}
