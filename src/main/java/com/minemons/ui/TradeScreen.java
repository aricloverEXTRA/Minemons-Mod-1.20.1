package com.minemons.ui;

import com.minemons.card.Card;
import com.minemons.card.MinemonCard;
import com.minemons.network.PacketRegistry;
import com.minemons.registry.CardRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Card trade screen — two-column offer layout.
 */
@Environment(EnvType.CLIENT)
public class TradeScreen extends Screen {

    private final String tradeId, nameA, nameB;
    private List<String> myOffer = new ArrayList<>();
    private List<String> theirOffer = new ArrayList<>();
    private String status = "OPEN";

    public TradeScreen(String tradeId, String nameA, String nameB) {
        super(Text.literal("Trade"));
        this.tradeId = tradeId; this.nameA = nameA; this.nameB = nameB;
    }

    @Override
    protected void init() {
        int w = this.width, h = this.height;
        addDrawableChild(ButtonWidget.builder(Text.literal("✅ Confirm"), b -> confirm()).dimensions(w / 2 - 57, h - 28, 52, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("✕ Cancel"),  b -> cancel()).dimensions(w / 2 + 5,  h - 28, 52, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        int w = this.width, h = this.height;

        ctx.fill(0, 0, w, 22, 0xFF1A1A2E);
        ctx.drawCenteredTextWithShadow(textRenderer, "§b🤝 §e" + nameA + " §7↔ §e" + nameB, w / 2, 6, 0xFFFFFF);

        String statusStr = switch (status) {
            case "CONFIRMED_A"    -> "§e" + nameA + " confirmed...";
            case "CONFIRMED_B"    -> "§e" + nameB + " confirmed...";
            case "BOTH_CONFIRMED" -> "§aBoth confirmed! Finalizing...";
            default               -> "§7Drag cards in game to offer — confirm when ready";
        };
        ctx.drawCenteredTextWithShadow(textRenderer, statusStr, w / 2, 26, 0xCCCCCC);

        renderPanel(ctx, "§aYour Offer",      myOffer,    10,      38, w / 2 - 15, h - 70, mouseX, mouseY);
        renderPanel(ctx, "§c" + nameB + "'s", theirOffer, w/2 + 5, 38, w / 2 - 15, h - 70, mouseX, mouseY);

        ctx.drawCenteredTextWithShadow(textRenderer, "§7⇌", w / 2, h / 2, 0xAAAAFF);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderPanel(DrawContext ctx, String title, List<String> offer,
                              int x, int y, int w, int h, int mx, int my) {
        ctx.fill(x, y, x + w, y + h, 0xCC111122);
        ctx.drawBorder(x, y, w, h, 0xFF334466);
        ctx.drawTextWithShadow(textRenderer, title, x + 5, y + 4, 0xFFFFFF);

        int cw = 72, ch = 44, cols = Math.max(1, (w - 8) / (cw + 4)), ry = y + 18;
        for (int i = 0; i < offer.size(); i++) {
            int col = i % cols, row = i / cols;
            int cx = x + 4 + col * (cw + 4), cy = ry + row * (ch + 4);
            if (cy + ch > y + h) break;
            Card card = CardRegistry.getCard(offer.get(i));
            int color = card instanceof MinemonCard mc ? mc.getElement().color | 0xFF000000 : 0xFF334455;
            boolean hov = mx >= cx && mx < cx + cw && my >= cy && my < cy + ch;
            ctx.fill(cx, cy, cx + cw, cy + ch, (color & 0x00FFFFFF) | 0x99000000);
            ctx.drawBorder(cx, cy, cw, ch, hov ? 0xFFFFDD00 : color);
            String name = card != null ? card.getDisplayName() : offer.get(i);
            if (name.length() > 10) name = name.substring(0, 9) + "..";
            ctx.drawCenteredTextWithShadow(textRenderer, "§f" + name, cx + cw / 2, cy + ch / 2 - 4, 0xFFFFFF);
        }
        if (offer.isEmpty())
            ctx.drawCenteredTextWithShadow(textRenderer, "§8Empty", x + w / 2, y + h / 2, 0x666666);
    }

    public void updateOffers(List<String> offerA, List<String> offerB, String status) {
        this.myOffer = offerA; this.theirOffer = offerB; this.status = status;
    }

    private void confirm() { ClientPlayNetworking.send(PacketRegistry.TRADE_CONFIRM, PacketByteBufs.create()); }
    private void cancel()  { ClientPlayNetworking.send(PacketRegistry.TRADE_RESPONSE, PacketByteBufs.create()); this.close(); }
}
