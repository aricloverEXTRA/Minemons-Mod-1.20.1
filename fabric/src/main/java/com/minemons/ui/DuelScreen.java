package com.minemons.ui;

import com.minemons.card.*;
import com.minemons.client.CardRenderer;
import com.minemons.network.ClientPacketHandlers;
import com.minemons.network.PacketRegistry;
import com.minemons.registry.CardRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Duel screen — Pokemon TCG-style layout.
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │ [Opponent name] HP ████  Turn N  OPPONENT'S/YOUR TURN  [Prizes: N] │ ← header
 * ├────────────────────────────────────────────────────────────┬────────┤
 * │          [Bench 0..4]  ← opponent field                   │        │
 * │                  [ACTIVE CARD — opponent]      [Opp deck] │  GAME  │
 * │     ──────────────────── VS ────────────────────          │  LOG   │
 * │                  [ACTIVE CARD — player]        [My  deck] │        │
 * │          [Bench 0..4]  ← player field                     │        │
 * ├────────────────────────────────────────────────────────────┤        │
 * │  [hand card × N]          END TURN  DRAW  CONCEDE         │        │
 * └────────────────────────────────────────────────────────────┴────────┘
 */
@Environment(EnvType.CLIENT)
public class DuelScreen extends Screen {

    // ── State ─────────────────────────────────────────────────────────────────
    private ClientPacketHandlers.DuelClientState state;
    private String lastMsg = "";
    private final List<String> gameLog = new ArrayList<>();
    private int selectedHand = -1;
    private int animTick = 0;

    // Damage flash
    private boolean flashing = false;
    private int flashTick = 0;
    private int flashDamage = 0;
    private int flashX, flashY;

    // ── Layout constants ──────────────────────────────────────────────────────
    private static final int HEADER_H    = 24;
    private static final int LOG_W       = 120;
    private static final int HAND_H      = 64;
    private static final int ACTION_W    = 108;

    // Card sizes
    private static final int ACTIVE_W = 96,  ACTIVE_H = 128;
    private static final int BENCH_W  = 56,  BENCH_H  = 76;
    private static final int HAND_W   = 52,  HAND_H2  = 68;
    private static final int DECK_W   = 40,  DECK_H   = 54;

    public DuelScreen(ClientPacketHandlers.DuelClientState state) {
        super(Text.literal("Minemons Duel"));
        this.state = state;
        gameLog.add("§7Duel started!");
    }

    @Override
    protected void init() {
        int w = this.width, h = this.height;
        int fieldW = w - LOG_W;
        int actionY = h - HAND_H - 24;
        int btnX = fieldW - ACTION_W + 4;

        addDrawableChild(ButtonWidget.builder(Text.literal("▶ END TURN"),
                b -> act("END_TURN", 0, 0))
            .dimensions(btnX, actionY, ACTION_W - 8, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("⚔ ATTACK"),
                b -> act("ATTACK", 0, 0))
            .dimensions(btnX, actionY + 22, ACTION_W - 8, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("▲ SUMMON"),
                b -> { if (selectedHand >= 0) act("SUMMON", selectedHand, 1); })
            .dimensions(btnX, actionY + 44, ACTION_W - 8, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("✕ CONCEDE"),
                b -> act("END_TURN", -1, 0))
            .dimensions(btnX, actionY + 66, ACTION_W - 8, 18).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        animTick++;

        int w = this.width, h = this.height;
        int fieldW = w - LOG_W;

        // ── Full dark background ──────────────────────────────────────────────
        ctx.fill(0, 0, w, h, 0xFF111111);

        // ── Header bar ───────────────────────────────────────────────────────
        renderHeader(ctx, w, mouseX, mouseY);

        // ── Field area ───────────────────────────────────────────────────────
        if (state != null) {
            int my = state.myIndex, op = 1 - my;
            int fieldTop = HEADER_H + 2;
            int handTop = h - HAND_H2 - 6;
            int fieldH = handTop - fieldTop - 2;
            int midY = fieldTop + fieldH / 2;

            renderField(ctx, state.sides[op], state.sides[my], fieldTop, midY, handTop, fieldW, mouseX, mouseY);
            renderHandBar(ctx, state.sides[my].hand, fieldW, h, mouseX, mouseY);
        }

        // ── Log panel (right) ─────────────────────────────────────────────────
        renderLog(ctx, fieldW, HEADER_H, LOG_W, h - HEADER_H);

        // ── Damage flash ──────────────────────────────────────────────────────
        if (flashing) {
            flashTick++;
            if (flashTick < 20) {
                int alpha = (int)(180 * (1.0f - flashTick / 20.0f));
                ctx.fill(0, 0, w - LOG_W, h, (alpha << 24) | 0xFF2222);
                ctx.drawCenteredTextWithShadow(textRenderer,
                    "§c-" + flashDamage, flashX, flashY, 0xFF4444);
            } else { flashing = false; flashTick = 0; }
        }

        // ── No active card warning ─────────────────────────────────────────────
        if (state != null) {
            ClientPacketHandlers.SideSnapshot mine = state.sides[state.myIndex];
            if (mine.activeCardId == null || mine.activeCardId.isEmpty()) {
                ctx.drawCenteredTextWithShadow(textRenderer,
                    "§e⚠ No active card! Summon one from your hand.",
                    (w - LOG_W) / 2, h - HAND_H2 - 20, 0xFFFFAA00);
            }
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private void renderHeader(DrawContext ctx, int w, int mx, int my) {
        ctx.fill(0, 0, w - LOG_W, HEADER_H, 0xFF1A1A1A);
        ctx.fill(0, HEADER_H - 1, w - LOG_W, HEADER_H, 0xFF333333);

        if (state == null) {
            ctx.drawCenteredTextWithShadow(textRenderer, "§7Loading...", (w - LOG_W) / 2, 7, 0xFFFFFFFF);
            return;
        }

        boolean myTurn = (state.currentTurn % 2) == state.myIndex;
        String turnStr = myTurn ? "§a▶ YOUR TURN" : "§c⏸ OPPONENT'S TURN";
        ctx.drawCenteredTextWithShadow(textRenderer, turnStr, (w - LOG_W) / 2, 7, 0xFFFFFFFF);

        // Turn counter
        ctx.drawTextWithShadow(textRenderer, "§7Turn " + state.currentTurn, 6, 7, 0x888888);

        // Phase
        ctx.drawTextWithShadow(textRenderer, "§8" + state.phase,
            w - LOG_W - textRenderer.getWidth(state.phase) * 2 - 6, 7, 0x666666);
    }

    // ── Field ─────────────────────────────────────────────────────────────────

    private void renderField(DrawContext ctx,
                              ClientPacketHandlers.SideSnapshot op,
                              ClientPacketHandlers.SideSnapshot me,
                              int fieldTop, int midY, int handTop,
                              int fieldW, int mx, int my) {

        // ── Field background zones ────────────────────────────────────────────
        // Opponent zone — slightly different shade
        ctx.fill(0, fieldTop, fieldW, midY, 0xFF141418);
        // My zone
        ctx.fill(0, midY, fieldW, handTop, 0xFF18181C);
        // Centre divider
        ctx.fill(20, midY - 1, fieldW - 20, midY + 1, 0xFF3A3A5C);

        int halfH = midY - fieldTop;

        // ── Opponent side ─────────────────────────────────────────────────────
        // Bench (top of opponent zone)
        renderBench(ctx, op.benchIds, op.benchHps, fieldW, fieldTop + 4, false, mx, my);

        // Active card (bottom of opponent zone)
        int opActiveX = fieldW / 2 - ACTIVE_W / 2;
        int opActiveY = midY - ACTIVE_H - 4;
        if (op.activeCardId != null && !op.activeCardId.isEmpty()) {
            CardRenderer.drawCard(ctx, textRenderer, op.activeCardId, op.activeCardHp,
                opActiveX, opActiveY, ACTIVE_W, ACTIVE_H, false, false, mx, my);
        } else {
            // Show player card if no minemon active
            drawPlayerActiveSlot(ctx, op.playerName, op.playerHp, opActiveX, opActiveY, ACTIVE_W, ACTIVE_H, false);
        }

        // Opponent deck (right)
        int opDeckX = fieldW - DECK_W - 8;
        int opDeckY = midY - DECK_H - 4;
        CardRenderer.drawCardBack(ctx, opDeckX, opDeckY, DECK_W, DECK_H);
        ctx.drawCenteredTextWithShadow(textRenderer, "§7" + op.deckSize,
            opDeckX + DECK_W / 2, opDeckY + DECK_H, 0xAAAAAA);

        // Opponent prizes
        renderPrizes(ctx, op.prizeCount, 8, midY - DECK_H - 4, false);

        // Opponent label
        ctx.drawTextWithShadow(textRenderer, "§c" + op.playerName + " §7HP:" + op.playerHp,
            opActiveX, fieldTop + 2, 0xFF888888);

        // ── My side ───────────────────────────────────────────────────────────
        // Active card (top of my zone)
        int myActiveX = fieldW / 2 - ACTIVE_W / 2;
        int myActiveY = midY + 4;
        if (me.activeCardId != null && !me.activeCardId.isEmpty()) {
            CardRenderer.drawCard(ctx, textRenderer, me.activeCardId, me.activeCardHp,
                myActiveX, myActiveY, ACTIVE_W, ACTIVE_H, false, false, mx, my);
        } else {
            drawPlayerActiveSlot(ctx, me.playerName, me.playerHp, myActiveX, myActiveY, ACTIVE_W, ACTIVE_H, true);
        }

        // My deck (right)
        int myDeckX = fieldW - DECK_W - 8;
        int myDeckY = midY + 4;
        CardRenderer.drawCardBack(ctx, myDeckX, myDeckY, DECK_W, DECK_H);
        ctx.drawCenteredTextWithShadow(textRenderer, "§7" + me.deckSize,
            myDeckX + DECK_W / 2, myDeckY + DECK_H, 0xAAAAAA);

        // My prizes
        renderPrizes(ctx, me.prizeCount, 8, midY + 4, true);

        // My bench (bottom of my zone)
        renderBench(ctx, me.benchIds, me.benchHps, fieldW, handTop - BENCH_H - 4, true, mx, my);

        // VS label at center
        ctx.drawCenteredTextWithShadow(textRenderer, "§6VS", fieldW / 2, midY - 6, 0xFFDD9900);
    }

    private void drawPlayerActiveSlot(DrawContext ctx, String name, int hp,
                                       int x, int y, int w, int h, boolean isMe) {
        CardRenderer.fillRounded(ctx, x, y, w, h, 0xFF1A1A2E);
        CardRenderer.drawRoundedBorder(ctx, x, y, w, h, isMe ? 0xFF4488FF : 0xFFAA4444, 1);
        ctx.drawCenteredTextWithShadow(textRenderer, isMe ? "§9👤" : "§c👤", x + w/2, y + h/2 - 10, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "§f" + name, x + w/2, y + h/2 + 2, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "§cHP §f" + hp, x + w/2, y + h/2 + 14, 0xFFFF5555);
        if (isMe)
            ctx.drawCenteredTextWithShadow(textRenderer, "§eSummon a card!", x + w/2, y + h - 14, 0xFFFFDD44);
    }

    private void renderBench(DrawContext ctx, List<String> ids, List<Integer> hps,
                               int fieldW, int y, boolean isMe, int mx, int my) {
        int total = ids.size();
        int totalW = total * (BENCH_W + 4) - 4;
        int startX = fieldW / 2 - totalW / 2;

        // Empty bench slots
        for (int i = total; i < 5; i++) {
            int bx = startX + (total == 0 ? i : total) * (BENCH_W + 4);
            ctx.fill(bx, y, bx + BENCH_W, y + BENCH_H, 0xFF1A1A1A);
            CardRenderer.drawRoundedBorder(ctx, bx, y, BENCH_W, BENCH_H, 0xFF2A2A2A, 1);
            ctx.drawCenteredTextWithShadow(textRenderer, "§8—", bx + BENCH_W/2, y + BENCH_H/2 - 4, 0x444444);
        }

        for (int i = 0; i < total; i++) {
            int bx = startX + i * (BENCH_W + 4);
            int hp = i < hps.size() ? hps.get(i) : 0;
            CardRenderer.drawCard(ctx, textRenderer, ids.get(i), hp,
                bx, y, BENCH_W, BENCH_H, false, false, mx, my);
        }
    }

    private void renderPrizes(DrawContext ctx, int count, int x, int y, boolean isMe) {
        ctx.drawTextWithShadow(textRenderer, "§6⬡", x, y, 0xFFFFAA00);
        ctx.drawTextWithShadow(textRenderer, "§f" + count, x + 10, y, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "§8prizes", x, y + 9, 0x666666);
    }

    // ── Hand bar ──────────────────────────────────────────────────────────────

    private void renderHandBar(DrawContext ctx, List<String> hand, int fieldW, int h, int mx, int my) {
        int barH = HAND_H2 + 6;
        int barY = h - barH;

        // Background
        ctx.fill(0, barY, fieldW, h, 0xFF1A1A1A);
        ctx.fill(0, barY, fieldW, barY + 1, 0xFF333333);

        if (hand.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer, "§8No cards in hand", fieldW / 2, barY + barH/2 - 4, 0x444444);
            return;
        }

        int actionZoneW = ACTION_W + 4;
        int availW = fieldW - actionZoneW - 8;
        int spacing = Math.min(HAND_W + 4, availW / hand.size());
        int startX = 4;

        for (int i = 0; i < hand.size(); i++) {
            int cx = startX + i * spacing;
            boolean sel = (i == selectedHand);
            CardRenderer.drawHandCard(ctx, textRenderer, hand.get(i),
                cx, barY + 3, HAND_W, HAND_H2, sel, mx, my);
        }

        // Selected card indicator label
        if (selectedHand >= 0 && selectedHand < hand.size()) {
            Card c = CardRegistry.getCard(hand.get(selectedHand));
            if (c != null)
                ctx.drawCenteredTextWithShadow(textRenderer,
                    "§e" + c.getDisplayName() + " §7selected",
                    fieldW / 2, barY - 10, 0xFFFFDD44);
        }
    }

    // ── Game log panel ────────────────────────────────────────────────────────

    private void renderLog(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x, y, x + w, y + h, 0xFF161616);
        ctx.fill(x, y, x + 1, y + h, 0xFF333333);

        // Title
        ctx.fill(x, y, x + w, y + 18, 0xFF1E1E1E);
        ctx.drawCenteredTextWithShadow(textRenderer, "§7GAME LOG", x + w/2, y + 5, 0xAAAAAA);

        int logY = y + 20;
        int maxLines = (h - 20) / 11;
        int start = Math.max(0, gameLog.size() - maxLines);
        for (int i = start; i < gameLog.size(); i++) {
            ctx.drawTextWithShadow(textRenderer, gameLog.get(i), x + 4, logY, 0xCCCCCC);
            logY += 11;
        }

        // Current message at bottom
        if (!lastMsg.isEmpty()) {
            ctx.fill(x, y + h - 22, x + w, y + h, 0xFF1E1E1E);
            // Word-wrap the message
            String[] words = lastMsg.split(" ");
            StringBuilder line = new StringBuilder();
            int lY = y + h - 20;
            for (String word : words) {
                if (textRenderer.getWidth(line + word) > w - 8) {
                    ctx.drawTextWithShadow(textRenderer, line.toString(), x + 4, lY, 0xDDDDDD);
                    lY += 9;
                    line = new StringBuilder(word + " ");
                } else {
                    line.append(word).append(" ");
                }
            }
            if (line.length() > 0)
                ctx.drawTextWithShadow(textRenderer, line.toString(), x + 4, lY, 0xDDDDDD);
        }
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (state == null) return super.mouseClicked(mx, my, button);

        int w = this.width, h = this.height, fieldW = w - LOG_W;
        int barY = h - HAND_H2 - 6;

        // Hand card click
        if (my >= barY && mx < fieldW) {
            List<String> hand = state.sides[state.myIndex].hand;
            int actionZoneW = ACTION_W + 4;
            int spacing = Math.min(HAND_W + 4, (fieldW - actionZoneW - 8) / Math.max(1, hand.size()));
            for (int i = 0; i < hand.size(); i++) {
                int cx = 4 + i * spacing;
                int cy = barY + 3 - (i == selectedHand ? 8 : 0);
                if (mx >= cx && mx < cx + HAND_W && my >= cy && my < cy + HAND_H2 + 8) {
                    selectedHand = (selectedHand == i) ? -1 : i;
                    return true;
                }
            }
        }

        // Bench card click → swap with active
        if (state != null && my > HEADER_H) {
            ClientPacketHandlers.SideSnapshot me = state.sides[state.myIndex];
            int midY = HEADER_H + 2 + (h - HEADER_H - HAND_H2 - 8) / 2;
            int handTop = h - HAND_H2 - 6;
            int benchY = handTop - BENCH_H - 4;
            int totalW = me.benchIds.size() * (BENCH_W + 4) - 4;
            int startX = fieldW / 2 - totalW / 2;
            for (int i = 0; i < me.benchIds.size(); i++) {
                int bx = startX + i * (BENCH_W + 4);
                if (mx >= bx && mx < bx + BENCH_W && my >= benchY && my < benchY + BENCH_H) {
                    act("SWAP", i, 0);
                    return true;
                }
            }
        }

        return super.mouseClicked(mx, my, button);
    }

    // ── Public updates ────────────────────────────────────────────────────────

    public void updateState(ClientPacketHandlers.DuelClientState s, String msg) {
        this.state = s;
        if (!msg.isEmpty()) {
            this.lastMsg = msg;
            if (gameLog.size() >= 50) gameLog.remove(0);
            gameLog.add("§7" + msg);
        }
    }

    public void animateCardDraw(String id) {
        Card c = CardRegistry.getCard(id);
        if (c != null && gameLog.size() < 50) gameLog.add("§8Drew: §7" + c.getDisplayName());
    }

    public void animateSummon(String id, boolean opp) {
        Card c = CardRegistry.getCard(id);
        String name = c != null ? c.getDisplayName() : id;
        if (gameLog.size() < 50) gameLog.add((opp ? "§cOpponent" : "§aYou") + " §7played " + name);
    }

    public void animateFaint(String id, boolean opp) {
        Card c = CardRegistry.getCard(id);
        String name = c != null ? c.getDisplayName() : id;
        if (gameLog.size() < 50) gameLog.add("§e" + name + " §7fainted!");
    }

    public void animateAttack(String attackerId, int dmg, String element) {
        int w = this.width, h = this.height;
        flashDamage = dmg;
        flashX = (w - LOG_W) / 2;
        flashY = h / 2;
        flashing = true; flashTick = 0;
        Card c = CardRegistry.getCard(attackerId);
        String name = c != null ? c.getDisplayName() : attackerId;
        if (gameLog.size() < 50) gameLog.add("§c" + name + " §7hit for §e" + dmg + "§7!");
    }

    private void act(String action, int p1, int p2) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(action, 64); buf.writeInt(p1); buf.writeInt(p2);
        ClientPlayNetworking.send(PacketRegistry.DUEL_ACTION, buf);
    }

    @Override public boolean shouldPause() { return false; }
    @Override public boolean shouldCloseOnEsc() { return false; }
}
