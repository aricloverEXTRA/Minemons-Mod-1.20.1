package com.minemons.client;

import com.minemons.card.*;
import com.minemons.registry.CardRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared card-rendering utility used by all screens.
 *
 * Card anatomy (dark-grey theme, element accent border):
 *   ┌──────────────────────┐
 *   │ [Elem] Name   HP ███ │  ← name row + hp bar
 *   │ ┌──────────────────┐ │
 *   │ │   TEXTURE ART    │ │  ← 60% of card height
 *   │ └──────────────────┘ │
 *   │  Attack name   dmg   │  ← attack row
 *   │  ● ● ● energy dots   │  ← energy cost
 *   └──────────────────────┘
 *
 * Neutral (NEXA) cards use grey border. All other elements use
 * a dark-grey card body with a coloured element accent.
 */
@Environment(EnvType.CLIENT)
public class CardRenderer {

    // Dark UI palette
    public static final int BG_CARD    = 0xFF1E1E1E;
    public static final int BG_ART     = 0xFF141414;
    public static final int BG_PANEL   = 0xFF252525;
    public static final int NEXA_COLOR = 0xFF888888; // grey for neutral
    public static final int TEXT_WHITE = 0xFFFFFFFF;
    public static final int TEXT_GRAY  = 0xFFAAAAAA;
    public static final int TEXT_DIM   = 0xFF666666;
    public static final int BORDER_DIM = 0xFF3A3A3A;

    /**
     * Draw a full card at (x,y) with given dimensions.
     * @param selected  draw with golden highlight border
     * @param showBack  draw the card back instead of front
     */
    public static void drawCard(DrawContext ctx, TextRenderer tr,
                                String cardId, int hp,
                                int x, int y, int w, int h,
                                boolean selected, boolean showBack,
                                int mx, int my) {
        if (showBack) { drawCardBack(ctx, x, y, w, h); return; }

        Card card = cardId != null ? CardRegistry.getCard(cardId) : null;
        int elemColor = getElemColor(card);
        int borderColor = selected ? 0xFFFFD700 : elemColor;

        // Card body
        fillRounded(ctx, x, y, w, h, BG_CARD);
        drawRoundedBorder(ctx, x, y, w, h, borderColor, selected ? 2 : 1);

        if (card == null) {
            ctx.drawCenteredTextWithShadow(tr, "§7Empty", x + w / 2, y + h / 2 - 4, TEXT_GRAY);
            return;
        }

        int padding = 3;
        int nameH = 12;
        int artY = y + padding + nameH + 2;
        int artH = (int)(h * 0.52f);
        int infoY = artY + artH + 2;

        // ── Name row ──────────────────────────────────────────────
        // Element dot
        ctx.fill(x + padding, y + padding + 2, x + padding + 6, y + padding + 8, elemColor);
        // Card name
        String name = card.getDisplayName();
        int maxNameW = w - padding * 2 - 10 - (card instanceof MinemonCard ? 28 : 0);
        while (tr.getWidth(name) > maxNameW && name.length() > 3)
            name = name.substring(0, name.length() - 1);
        if (!name.equals(card.getDisplayName())) name = name.substring(0, name.length()-2) + "..";
        ctx.drawTextWithShadow(tr, "§f" + name, x + padding + 9, y + padding + 1, TEXT_WHITE);

        // HP for minemons
        if (card instanceof MinemonCard mc) {
            String hpStr = "HP " + hp;
            ctx.drawTextWithShadow(tr, "§c" + hpStr, x + w - padding - tr.getWidth(hpStr) - 1, y + padding + 1, 0xFFFF5555);
        }

        // ── Art area ──────────────────────────────────────────────
        ctx.fill(x + padding, artY, x + w - padding, artY + artH, BG_ART);
        // Texture
        Identifier tex = TextureManager.get(cardId);
        try {
            ctx.drawTexture(tex, x + padding + 1, artY + 1, 0, 0, w - padding * 2 - 2, artH - 2, w - padding * 2 - 2, artH - 2);
        } catch (Exception ignored) {
            // Texture missing — draw element symbol fallback
            ctx.drawCenteredTextWithShadow(tr, getElemSymbol(card), x + w / 2, artY + artH / 2 - 4, elemColor);
        }

        // HP bar under art (for minemons)
        if (card instanceof MinemonCard mc) {
            int barY = artY + artH;
            int barW = w - padding * 2;
            float pct = mc.getMaxHp() > 0 ? (float)hp / mc.getMaxHp() : 0;
            ctx.fill(x + padding, barY, x + padding + barW, barY + 3, 0xFF333333);
            int barCol = pct > 0.5f ? 0xFF44DD44 : pct > 0.25f ? 0xFFDDAA00 : 0xFFDD3333;
            ctx.fill(x + padding, barY, x + padding + (int)(barW * pct), barY + 3, barCol);
        }

        // ── Info area ─────────────────────────────────────────────
        if (card instanceof MinemonCard mc) {
            // Attack name + damage
            String atkName = mc.getPassiveAbility();
            if (tr.getWidth(atkName) > w - padding * 2 - 20)
                atkName = atkName.substring(0, Math.max(1, atkName.length()-3)) + "..";
            ctx.drawTextWithShadow(tr, "§7" + atkName, x + padding, infoY, TEXT_GRAY);
            ctx.drawTextWithShadow(tr, "§e" + mc.getBaseAttack(), x + w - padding - tr.getWidth(String.valueOf(mc.getBaseAttack())), infoY, 0xFFFFDD44);

            // Energy dots
            int dotY = infoY + 10;
            int energyCost = Math.max(1, mc.getBaseAttack() / 20);
            for (int d = 0; d < Math.min(energyCost, 4); d++) {
                int dotX = x + padding + d * 8;
                ctx.fill(dotX, dotY, dotX + 6, dotY + 6, elemColor);
                ctx.drawBorder(dotX, dotY, 6, 6, 0xFF000000);
            }
        } else if (card instanceof ConsumableCard cc) {
            ctx.drawCenteredTextWithShadow(tr, "§aUse", x + w / 2, infoY, 0xFF44FF44);
        } else if (card instanceof TrainerCard tc) {
            ctx.drawCenteredTextWithShadow(tr, "§9Trainer", x + w / 2, infoY, 0xFF4488FF);
        } else if (card instanceof PlaceCard pc) {
            ctx.drawCenteredTextWithShadow(tr, "§6Place", x + w / 2, infoY, 0xFFFFAA00);
        }

        // Rarity gem bottom-right
        int[] rarityColors = { 0xFF888888, 0xFF44BB44, 0xFF4488FF, 0xFFAA44FF, 0xFFFFAA00 };
        if (card.getRarity() != null) {
            int rc = rarityColors[card.getRarity().ordinal()];
            ctx.fill(x + w - padding - 5, y + h - padding - 5, x + w - padding, y + h - padding, rc);
        }

        // Tooltip on hover
        if (mx >= x && mx < x + w && my >= y && my < y + h)
            drawCardTooltip(ctx, tr, card, hp, mx, my);
    }

    public static void drawCardBack(DrawContext ctx, int x, int y, int w, int h) {
        fillRounded(ctx, x, y, w, h, 0xFF1A3A1A);
        drawRoundedBorder(ctx, x, y, w, h, 0xFF2D6B2D, 1);
        ctx.drawCenteredTextWithShadow(
            net.minecraft.client.MinecraftClient.getInstance().textRenderer,
            "§2⚔", x + w/2, y + h/2 - 4, 0xFF2ECC40);
    }

    /** Draw a compact mini-card for the hand bar. */
    public static void drawHandCard(DrawContext ctx, TextRenderer tr, String cardId,
                                     int x, int y, int w, int h,
                                     boolean selected, int mx, int my) {
        Card card = cardId != null ? CardRegistry.getCard(cardId) : null;
        int elemColor = getElemColor(card);
        int borderColor = selected ? 0xFFFFD700 : elemColor;
        int cy = selected ? y - 8 : y;

        fillRounded(ctx, x, cy, w, h, BG_CARD);
        drawRoundedBorder(ctx, x, cy, w, h, borderColor, selected ? 2 : 1);

        if (card != null) {
            // Top: element dot
            ctx.fill(x + 3, cy + 3, x + 7, cy + 7, elemColor);

            // Texture thumbnail
            Identifier tex = TextureManager.get(cardId);
            int artH = (int)(h * 0.55f);
            try {
                ctx.drawTexture(tex, x + 2, cy + 10, 0, 0, w - 4, artH, w - 4, artH);
            } catch (Exception ignored) {
                ctx.drawCenteredTextWithShadow(tr, getElemSymbol(card), x + w/2, cy + 10 + artH/2 - 4, elemColor);
            }

            // Name
            String name = card.getDisplayName();
            while (tr.getWidth(name) > w - 4 && name.length() > 2)
                name = name.substring(0, name.length()-1);
            if (!name.equals(card.getDisplayName())) name = name.substring(0, name.length()-2) + "..";
            ctx.drawCenteredTextWithShadow(tr, "§f" + name, x + w / 2, cy + h - 14, TEXT_WHITE);

            // Type badge bottom-left
            String badge = switch (card.getType()) {
                case MINEMON -> "§cM"; case CONSUMABLE -> "§aU";
                case TRAINER -> "§9T"; case PLACE -> "§6P";
            };
            ctx.drawTextWithShadow(tr, badge, x + 2, cy + h - 9, TEXT_WHITE);
        }

        if (mx >= x && mx < x + w && my >= cy && my < cy + h && card != null)
            drawCardTooltip(ctx, tr, card, -1, mx, my);
    }

    // ── Tooltip ────────────────────────────────────────────────────────────────

    public static void drawCardTooltip(DrawContext ctx, TextRenderer tr, Card card, int hp, int mx, int my) {
        if (card == null) return;
        List<Text> lines = new ArrayList<>();
        lines.add(Text.literal("§e" + card.getDisplayName() + "  §7[" + card.getRarity().displayName + "]"));
        lines.add(Text.literal("§7" + card.getElement().displayName + "  " + card.getType()));

        if (card instanceof MinemonCard mc) {
            String hpStr = hp >= 0 ? hp + "/" + mc.getMaxHp() : String.valueOf(mc.getMaxHp());
            lines.add(Text.literal("§cHP: §f" + hpStr + "   §eATK: §f" + mc.getBaseAttack()));
            lines.add(Text.literal("§bPassive: §f" + mc.getPassiveAbility()));
            lines.add(Text.literal("§8  " + mc.getPassiveDescription()));
            if (mc.isNeutralEnergy()) lines.add(Text.literal("§7Uses neutral energy"));
        } else if (card instanceof ConsumableCard cc) {
            lines.add(Text.literal("§aEffect: §f" + cc.getEffect().name().replace('_', ' ')));
            if (cc.getValue() > 0) lines.add(Text.literal("§7Value: §f" + cc.getValue()));
        } else if (card instanceof TrainerCard tc) {
            lines.add(Text.literal("§9Effect: §f" + tc.getEffect().name().replace('_', ' ')));
        } else if (card instanceof PlaceCard pc) {
            lines.add(Text.literal("§6Terrain: §f" + pc.getEffect().name().replace('_', ' ')));
        }
        lines.add(Text.literal("§8" + card.getDescription()));
        ctx.drawTooltip(tr, lines, mx, my);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    public static int getElemColor(Card card) {
        if (card == null) return NEXA_COLOR;
        if (card.getElement() == Element.NEXA) return NEXA_COLOR;
        return card.getElement().color | 0xFF000000;
    }

    public static String getElemSymbol(Card card) {
        if (card == null) return "?";
        return card.getElement().symbol;
    }

    /** Fills a rectangle with slight rounded corners (2px corners blacked out). */
    public static void fillRounded(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 2, y, x + w - 2, y + h, color);
        ctx.fill(x, y + 2, x + 2, y + h - 2, color);
        ctx.fill(x + w - 2, y + 2, x + w, y + h - 2, color);
    }

    /** Draws a 1 or 2px border with rounded corners. */
    public static void drawRoundedBorder(DrawContext ctx, int x, int y, int w, int h, int color, int thickness) {
        for (int t = 0; t < thickness; t++) {
            int ox = x + t, oy = y + t, ow = w - t * 2, oh = h - t * 2;
            // Top + bottom
            ctx.fill(ox + 2, oy, ox + ow - 2, oy + 1, color);
            ctx.fill(ox + 2, oy + oh - 1, ox + ow - 2, oy + oh, color);
            // Left + right
            ctx.fill(ox, oy + 2, ox + 1, oy + oh - 2, color);
            ctx.fill(ox + ow - 1, oy + 2, ox + ow, oy + oh - 2, color);
            // Corners (diagonal 1px)
            ctx.fill(ox + 1, oy + 1, ox + 2, oy + 2, color);
            ctx.fill(ox + ow - 2, oy + 1, ox + ow - 1, oy + 2, color);
            ctx.fill(ox + 1, oy + oh - 2, ox + 2, oy + oh - 1, color);
            ctx.fill(ox + ow - 2, oy + oh - 2, ox + ow - 1, oy + oh - 1, color);
        }
    }

    /** Dark panel with rounded border. */
    public static void drawPanel(DrawContext ctx, int x, int y, int w, int h, int borderColor) {
        fillRounded(ctx, x, y, w, h, BG_PANEL);
        drawRoundedBorder(ctx, x, y, w, h, borderColor, 1);
    }
}
