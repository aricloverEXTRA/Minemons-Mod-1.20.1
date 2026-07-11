package com.minemons.ui;

import com.minemons.card.Card;
import com.minemons.card.MinemonCard;
import com.minemons.card.Rarity;
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
 * Card Pack Store UI.
 * Shows current XP, available packs, and pull results.
 */
@Environment(EnvType.CLIENT)
public class StoreScreen extends Screen {

    private int xp;
    private final List<ClientPacketHandlers.PackInfo> packs;
    private List<String> lastPulled = new ArrayList<>();
    private boolean showingPull = false;
    private int tick = 0;

    private static final int[] ACCENT = { 0x3A9EFF, 0xFFAA00, 0xCC44FF };

    public StoreScreen(int xp, List<ClientPacketHandlers.PackInfo> packs) {
        super(Text.literal("Minemons Store"));
        this.xp = xp;
        this.packs = packs;
    }

    @Override
    protected void init() {
        int w = this.width, h = this.height;
        int spacing = Math.min(130, (w - 40) / Math.max(1, packs.size()));
        int startX = w / 2 - (packs.size() * spacing) / 2;
        int btnY = h / 2 + 40;

        for (int i = 0; i < packs.size(); i++) {
            final int fi = i;
            ClientPacketHandlers.PackInfo pack = packs.get(i);
            int bx = startX + i * spacing + 5;
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Buy (" + pack.xpCost + " XP)"),
                    b -> buy(fi, 1)).dimensions(bx, btnY, spacing - 10, 20).build());
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("×10 (" + pack.xpCost * 10 + " XP)"),
                    b -> buy(fi, 10)).dimensions(bx, btnY + 24, spacing - 10, 20).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("✕ Close"), b -> this.close())
                .dimensions(w / 2 - 35, h - 26, 70, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        tick++;
        int w = this.width, h = this.height;

        // Title bar
        ctx.fill(0, 0, w, 24, 0xFF1A1A2E);
        ctx.drawCenteredTextWithShadow(textRenderer, "§6⭐ Minemons Card Store ⭐", w / 2, 7, 0xFFDD44);

        // XP display — prominent
        ctx.fill(w / 2 - 70, 27, w / 2 + 70, 43, 0xFF111133);
        ctx.drawBorder(w / 2 - 70, 27, 140, 16, 0xFF4488FF);
        ctx.drawCenteredTextWithShadow(textRenderer, "§7Your XP: §e§l" + xp, w / 2, 31, 0xFFFFFF);

        ctx.drawCenteredTextWithShadow(textRenderer, "§8Earn XP by winning duels  ·  /gamba costs 15 XP", w / 2, 47, 0x666666);

        // Pack displays
        int spacing = Math.min(130, (w - 40) / Math.max(1, packs.size()));
        int startX = w / 2 - (packs.size() * spacing) / 2;
        for (int i = 0; i < packs.size(); i++) {
            renderPack(ctx, packs.get(i), startX + i * spacing, 56, spacing - 4, 130,
                    ACCENT[i % ACCENT.length], mouseX, mouseY);
        }

        if (showingPull) renderPullResult(ctx, w, h);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderPack(DrawContext ctx, ClientPacketHandlers.PackInfo pack,
                             int x, int y, int w, int h, int accent, int mx, int my) {
        float pulse = (float)(Math.sin(tick * 0.05) * 0.5 + 0.5);
        int border = blend(accent, 0xFFFFFF, pulse * 0.25f) | 0xFF000000;
        boolean hov = mx >= x && mx < x + w && my >= y && my < y + h;

        ctx.fill(x, y, x + w, y + h, 0xCC131326);
        ctx.drawBorder(x, y, w, h, border);
        if (hov) ctx.fill(x, y, x + w, y + h, 0x22FFFFFF);

        // Animated card icon
        int iy = y + 10 + (int)(Math.sin(tick * 0.08) * 2);
        ctx.drawCenteredTextWithShadow(textRenderer, "🎴", x + w / 2, iy, accent | 0xFF000000);
        ctx.drawCenteredTextWithShadow(textRenderer, "§f" + pack.id, x + w / 2, y + 30, accent | 0xFF000000);
        ctx.drawCenteredTextWithShadow(textRenderer, "§7" + pack.cardCount + " cards", x + w / 2, y + 44, 0xAAAAAA);
        ctx.drawCenteredTextWithShadow(textRenderer, "§e" + pack.xpCost + " XP each", x + w / 2, y + 56, 0xFFDD44);

        // Affordability indicator
        boolean canAfford = xp >= pack.xpCost;
        ctx.drawCenteredTextWithShadow(textRenderer,
                canAfford ? "§a✔ Can afford" : "§c✘ Need " + pack.xpCost + " XP",
                x + w / 2, y + 70, 0xFFFFFF);

        // Odds summary
        String odds = pack.xpCost <= 5  ? "§7Rare:6% Epic:1%"
                    : pack.xpCost <= 10 ? "§7Rare:12% Epic:3%"
                    :                     "§7Rare:20% Epic:8% Mythic:2%";
        ctx.drawCenteredTextWithShadow(textRenderer, odds, x + w / 2, y + 84, 0x888888);

        if (hov) {
            List<Text> tooltip = new ArrayList<>();
            tooltip.add(Text.literal("§6" + pack.id + " — " + pack.description));
            tooltip.add(Text.literal(odds.replace("§7", "§f")));
            ctx.drawTooltip(textRenderer, tooltip, mx, my);
        }
    }

    private void renderPullResult(DrawContext ctx, int w, int h) {
        ctx.fill(0, 0, w, h, 0xCC000000);
        ctx.drawCenteredTextWithShadow(textRenderer, "§6✨ You Got! ✨", w / 2, h / 2 - 75, 0xFFDD44);
        ctx.drawCenteredTextWithShadow(textRenderer, "§7Your XP: §e§l" + xp, w / 2, h / 2 - 60, 0xFFFFFF);

        int cw = 66, ch = 46, cols = Math.min(lastPulled.size(), 8);
        int sx = w / 2 - (cols * (cw + 5)) / 2;
        for (int i = 0; i < lastPulled.size(); i++) {
            String id = lastPulled.get(i);
            int cx = sx + (i % cols) * (cw + 5);
            int cy = h / 2 - 48 + (i / cols) * (ch + 6);
            Card card = CardRegistry.getCard(id);
            int color = card instanceof MinemonCard mc ? mc.getElement().color | 0xFF000000
                      : card != null ? card.getRarity().color | 0xFF000000 : 0xFF444455;
            ctx.fill(cx, cy, cx + cw, cy + ch, (color & 0x00FFFFFF) | 0xBB000000);
            ctx.drawBorder(cx, cy, cw, ch, color);
            if (card != null) {
                ctx.drawTextWithShadow(textRenderer, rarityStr(card.getRarity()), cx + 2, cy + 2, 0xFFFFFF);
                String name = card.getDisplayName(); if (name.length() > 9) name = name.substring(0, 8) + "..";
                ctx.drawCenteredTextWithShadow(textRenderer, "§f" + name, cx + cw / 2, cy + ch / 2 - 4, 0xFFFFFF);
            }
        }
        ctx.drawCenteredTextWithShadow(textRenderer, "§7(Click to close)", w / 2, h / 2 + 58, 0x888888);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (showingPull) { showingPull = false; return true; }
        return super.mouseClicked(x, y, button);
    }

    private void buy(int packIndex, int count) {
        if (packIndex >= packs.size()) return;
        String id = packs.get(packIndex).id;
        for (int i = 0; i < count; i++) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(id, 32);
            ClientPlayNetworking.send(PacketRegistry.STORE_BUY, buf);
        }
    }

    /** Called by ClientPacketHandlers.onPlayerData after a purchase. */
    public void showPulledCards(List<String> cards) {
        this.lastPulled = new ArrayList<>(cards);
        this.showingPull = true;
    }

    /** Called by ClientPacketHandlers.onPlayerData to update XP display live. */
    public void updateXp(int newXp) { this.xp = newXp; }

    private String rarityStr(Rarity r) {
        return switch (r) { case COMMON -> "§7C"; case UNCOMMON -> "§aU"; case RARE -> "§9R"; case EPIC -> "§5E"; case MYTHIC -> "§6M"; };
    }

    private int blend(int c1, int c2, float t) {
        int r = (int)(((c1>>16)&0xFF)*(1-t)+((c2>>16)&0xFF)*t);
        int g = (int)(((c1>>8)&0xFF)*(1-t)+((c2>>8)&0xFF)*t);
        int b = (int)((c1&0xFF)*(1-t)+(c2&0xFF)*t);
        return (r<<16)|(g<<8)|b;
    }
}
