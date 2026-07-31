package com.minemons.tutorial;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;

/**
 * Helper for rendering tutorial overlays, highlights, and tooltips on screen.
 * Provides visual guidance for tutorial steps.
 */
@Environment(EnvType.CLIENT)
public class TutorialRenderer {

    private static final int OVERLAY_COLOR = 0x99000000;
    private static final int HIGHLIGHT_COLOR = 0xFF00FF00;
    private static final int TOOLTIP_BG = 0xFF222222;
    private static final int TOOLTIP_BORDER = 0xFF4488FF;

    /**
     * Draw a darkened overlay with a highlighted region
     */
    public static void drawHighlightOverlay(DrawContext ctx, int x, int y, int w, int h, int screenW, int screenH) {
        // Darken everything
        ctx.fill(0, 0, screenW, screenH, OVERLAY_COLOR);
        
        // Clear the highlight box
        ctx.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0);
        
        // Draw bright border around highlight
        drawBorder(ctx, x - 2, y - 2, w + 4, h + 4, HIGHLIGHT_COLOR, 2);
    }

    /**
     * Draw a tutorial tooltip with text
     */
    public static void drawTutorialTooltip(DrawContext ctx, TextRenderer tr, String title, String description, int x, int y, int maxWidth) {
        int padding = 8;
        int lineHeight = 12;
        
        // Calculate dimensions
        int titleWidth = tr.getWidth(title);
        int descWidth = Math.max(tr.getWidth(description), 100);
        int boxWidth = Math.min(Math.max(titleWidth, descWidth) + padding * 2, maxWidth);
        int boxHeight = lineHeight * 3 + padding * 2;
        
        // Position box near cursor, keep on screen
        int boxX = Math.max(8, Math.min(x, ctx.getScaledWindowWidth() - boxWidth - 8));
        int boxY = Math.max(8, Math.min(y, ctx.getScaledWindowHeight() - boxHeight - 8));
        
        // Draw background
        ctx.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, TOOLTIP_BG);
        drawBorder(ctx, boxX, boxY, boxWidth, boxHeight, TOOLTIP_BORDER, 1);
        
        // Draw text
        ctx.drawTextWithShadow(tr, "§b" + title, boxX + padding, boxY + padding, 0xFF4488FF);
        ctx.drawTextWithShadow(tr, "§7" + description, boxX + padding, boxY + padding + lineHeight * 2, 0xFFAAAAAA);
    }

    /**
     * Draw a progress indicator showing tutorial completion
     */
    public static void drawTutorialProgress(DrawContext ctx, TextRenderer tr, int completed, int total, int x, int y) {
        String progress = "Tutorial: " + completed + "/" + total;
        ctx.drawTextWithShadow(tr, "§e" + progress, x, y, 0xFFFFDD44);
    }

    /**
     * Draw an animated pulsing highlight
     */
    public static void drawPulsingHighlight(DrawContext ctx, int x, int y, int w, int h, float pulse) {
        int alpha = (int)(128 + pulse * 127);
        int color = (HIGHLIGHT_COLOR & 0x00FFFFFF) | ((alpha << 24) & 0xFF000000);
        drawBorder(ctx, x, y, w, h, color, 2);
    }

    private static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color, int thickness) {
        for (int t = 0; t < thickness; t++) {
            int ox = x + t, oy = y + t, ow = w - t * 2, oh = h - t * 2;
            ctx.fill(ox, oy, ox + ow, oy + 1, color);
            ctx.fill(ox, oy + oh - 1, ox + ow, oy + oh, color);
            ctx.fill(ox, oy, ox + 1, oy + oh, color);
            ctx.fill(ox + ow - 1, oy, ox + ow, oy + oh, color);
        }
    }
}
