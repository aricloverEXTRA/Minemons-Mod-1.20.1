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

import java.util.*;

@Environment(EnvType.CLIENT)
public class DeckScreen extends Screen {

    private final Map<String, Integer> owned;
    private final List<ClientPacketHandlers.DeckSnapshot> savedDecks;
    private int activeIdx;

    private final List<String> deckList = new ArrayList<>();
    private String deckName = "My Deck";
    private CardType filter = null;
    private int collScroll = 0;
    private String statusMsg = "";
    private int statusTick = 0;
    private boolean statusOk = false;

    private static final int CARD_W = 72, CARD_H = 96;
    private static final int DECK_ROW_H = 14;

    public DeckScreen(Map<String, Integer> owned,
                      List<ClientPacketHandlers.DeckSnapshot> savedDecks, int activeIdx) {
        super(Text.literal("Deck Builder"));
        this.owned = owned; this.savedDecks = savedDecks; this.activeIdx = activeIdx;
        if (!savedDecks.isEmpty() && activeIdx < savedDecks.size()) {
            ClientPacketHandlers.DeckSnapshot snap = savedDecks.get(activeIdx);
            this.deckName = snap.name;
            snap.cards.forEach((id, cnt) -> { for (int i = 0; i < cnt; i++) deckList.add(id); });
        }
    }

    @Override
    protected void init() {
        int w = this.width, h = this.height;
        // Filter tabs
        int tx = 5;
        tab("All", null, tx); tx += 34;
        tab("Mon", CardType.MINEMON, tx); tx += 34;
        tab("Use", CardType.CONSUMABLE, tx); tx += 34;
        tab("Trn", CardType.TRAINER, tx); tx += 34;
        tab("Plc", CardType.PLACE, tx);

        // Buttons bottom
        addDrawableChild(ButtonWidget.builder(Text.literal("💾 Save"), b -> save())
            .dimensions(w/2 - 62, h - 26, 56, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("✕ Clear"), b -> deckList.clear())
            .dimensions(w/2 - 2, h - 26, 56, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("✕"), b -> close())
            .dimensions(w - 24, 2, 20, 18).build());
    }

    private void tab(String label, CardType type, int x) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> { filter = type; collScroll = 0; })
            .dimensions(x, 28, 30, 14).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx);
        int w = this.width, h = this.height;
        int splitX = w / 2;

        // Header
        ctx.fill(0, 0, w, 24, 0xFF1A1A1A);
        ctx.drawCenteredTextWithShadow(textRenderer, "§b⚔ Deck Builder §7| §e" + deckName, w/2, 6, 0xFFFFFF);
        ctx.fill(0, 24, w, 44, 0xFF151515);

        int panelTop = 44, panelBot = h - 32;

        // Collection
        renderCollection(ctx, mx, my, 4, panelTop, splitX - 6, panelBot - panelTop);
        // Deck list
        renderDeckList(ctx, mx, my, splitX + 2, panelTop, w - splitX - 6, panelBot - panelTop);

        // Bottom bar
        ctx.fill(0, panelBot, w, h, 0xFF1A1A1A);
        int total = deckList.size();
        String col = total == 60 ? "§a" : total > 60 ? "§c" : "§e";
        ctx.drawCenteredTextWithShadow(textRenderer, col + total + "§7/§f60 cards", 80, h - 18, 0xFFFFFF);

        if (statusTick > 0) {
            statusTick--;
            ctx.drawCenteredTextWithShadow(textRenderer,
                (statusOk ? "§a" : "§c") + statusMsg, w/2, h - 18, 0xFFFFFF);
        }

        super.render(ctx, mx, my, delta);
    }

    private void renderCollection(DrawContext ctx, int mx, int my, int x, int y, int w, int h) {
        CardRenderer.drawPanel(ctx, x, y, w, h, 0xFF333333);
        ctx.drawTextWithShadow(textRenderer, "§7Collection §8(" + owned.size() + ")", x + 5, y + 4, 0xAAAAAA);

        List<String> filtered = getFiltered();
        int cols = Math.max(1, (w - 10) / (CARD_W + 4));
        int rowH = CARD_H + 5;
        int startI = collScroll * cols;
        int visY = y + 16;

        for (int i = startI; i < filtered.size(); i++) {
            int col = (i - startI) % cols, row = (i - startI) / cols;
            int cx = x + 5 + col * (CARD_W + 4);
            int cy = visY + row * rowH;
            if (cy + CARD_H > y + h - 4) break;

            String id = filtered.get(i);
            // Determine HP for display — use max HP
            Card card = CardRegistry.getCard(id);
            int dispHp = card instanceof MinemonCard mc ? mc.getMaxHp() : -1;
            CardRenderer.drawCard(ctx, textRenderer, id, dispHp, cx, cy, CARD_W, CARD_H, false, false, mx, my);

            // Owned count badge
            int ownedCount = owned.getOrDefault(id, 0);
            long inDeck = deckList.stream().filter(id::equals).count();
            ctx.fill(cx + CARD_W - 18, cy + CARD_H - 12, cx + CARD_W - 1, cy + CARD_H - 1, 0xCC000000);
            ctx.drawTextWithShadow(textRenderer, "§7" + ownedCount, cx + CARD_W - 16, cy + CARD_H - 11, 0xAAAAAA);
            if (inDeck > 0) {
                ctx.drawTextWithShadow(textRenderer, "§e×" + inDeck, cx + 2, cy + CARD_H - 11, 0xFFDD44);
            }
        }
    }

    private void renderDeckList(DrawContext ctx, int mx, int my, int x, int y, int w, int h) {
        CardRenderer.drawPanel(ctx, x, y, w, h, 0xFF333333);
        ctx.drawTextWithShadow(textRenderer, "§7Deck: §e" + deckName, x + 5, y + 4, 0xAAAAAA);

        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String id : deckList) counts.merge(id, 1, Integer::sum);

        int ry = y + 16;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (ry + DECK_ROW_H > y + h - 2) break;
            Card card = CardRegistry.getCard(e.getKey());
            String name = card != null ? card.getDisplayName() : e.getKey();
            int elemCol = CardRenderer.getElemColor(card);
            boolean hov = mx >= x + 4 && mx < x + w - 4 && my >= ry && my < ry + DECK_ROW_H;

            ctx.fill(x + 4, ry, x + w - 4, ry + DECK_ROW_H - 1, hov ? 0x44FFFFFF : (elemCol & 0x00FFFFFF) | 0x22000000);
            // Element dot
            ctx.fill(x + 6, ry + 4, x + 11, ry + 9, elemCol);
            ctx.drawTextWithShadow(textRenderer, "§f" + name, x + 14, ry + 2, 0xFFFFFF);
            ctx.drawTextWithShadow(textRenderer, "§7×" + e.getValue(), x + w - 22, ry + 2, 0xCCCCCC);
            if (hov) ctx.drawTextWithShadow(textRenderer, "§c[-]", x + w - 36, ry + 2, 0xFF4444);
            ry += DECK_ROW_H;
        }
        if (counts.isEmpty())
            ctx.drawCenteredTextWithShadow(textRenderer, "§8Click a card to add", x + w/2, y + h/2, 0x555555);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int w = this.width, h = this.height, splitX = w / 2;
        int panelTop = 44, panelBot = h - 32;

        // Collection click
        if (mx >= 4 && mx < splitX - 6 && my >= panelTop && my < panelBot) {
            List<String> filtered = getFiltered();
            int cols = Math.max(1, (splitX - 14) / (CARD_W + 4));
            for (int i = collScroll * cols; i < filtered.size(); i++) {
                int col = (i - collScroll * cols) % cols, row = (i - collScroll * cols) / cols;
                int cx = 9 + col * (CARD_W + 4), cy = panelTop + 16 + row * (CARD_H + 5);
                if ((int)my >= cy && (int)my < cy + CARD_H && (int)mx >= cx && (int)mx < cx + CARD_W) {
                    addCard(filtered.get(i)); return true;
                }
            }
        }

        // Deck list click
        if (mx >= splitX + 2 && mx < w - 2 && my >= panelTop && my < panelBot) {
            Map<String, Integer> counts = new LinkedHashMap<>();
            for (String id : deckList) counts.merge(id, 1, Integer::sum);
            int ry = panelTop + 16;
            for (String id : counts.keySet()) {
                if ((int)my >= ry && (int)my < ry + DECK_ROW_H) { deckList.remove(id); return true; }
                ry += DECK_ROW_H;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double amount) {
        if (mx < this.width / 2.0) { collScroll = Math.max(0, collScroll - (int)Math.signum(amount)); return true; }
        return super.mouseScrolled(mx, my, amount);
    }

    private void addCard(String id) {
        long inDeck = deckList.stream().filter(id::equals).count();
        if (inDeck >= 4) { status("Max 4 copies per card", false); return; }
        if (deckList.size() >= 60) { status("Deck full (60/60)", false); return; }
        deckList.add(id);
    }

    private void save() {
        if (deckList.size() != 60) { status("Need exactly 60 cards (have " + deckList.size() + ")", false); return; }
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(deckName, 64);
        buf.writeInt(deckList.size());
        for (String id : deckList) buf.writeString(id, 64);
        ClientPlayNetworking.send(PacketRegistry.DECK_SAVE, buf);
        status("Deck saved!", true);
    }

    private void status(String msg, boolean ok) { statusMsg = msg; statusTick = 80; statusOk = ok; }

    private List<String> getFiltered() {
        List<String> result = new ArrayList<>();
        for (String id : owned.keySet()) {
            Card c = CardRegistry.getCard(id); if (c == null) continue;
            if (filter != null && c.getType() != filter) continue;
            result.add(id);
        }
        result.sort(Comparator.comparingInt((String id) -> { Card c = CardRegistry.getCard(id); return c == null ? 99 : c.getType().ordinal(); })
                .thenComparing(id -> { Card c = CardRegistry.getCard(id); return c == null ? id : c.getDisplayName(); }));
        return result;
    }

    public void refresh() {}
}
