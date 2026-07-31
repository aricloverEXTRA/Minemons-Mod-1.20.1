package com.minemons.card;

/**
 * Rarity configuration with visual styling and gameplay effects.
 * Enhanced cosmetics based on rarity tier.
 */
public enum RarityConfig {
    COMMON(
        1, 0xAAAAAA, "Common",
        0xFF1A1A1A,    // Card background
        0xFF888888,    // Border primary
        0xFF666666,    // Border secondary
        false,         // Has glow
        false          // Has particles
    ),
    UNCOMMON(
        2, 0x55AA55, "Uncommon",
        0xFF1A2A1A,    // Subtle green tint
        0xFF44BB44,    // Green border
        0xFF2D8C2D,    // Dark green
        false,
        false
    ),
    RARE(
        3, 0x5555FF, "Rare",
        0xFF1A1A2A,    // Subtle blue tint
        0xFF4488FF,    // Blue border
        0xFF2D5FBB,    // Dark blue
        true,          // Has subtle glow
        true           // Has particle shimmer
    ),
    EPIC(
        4, 0xAA00AA, "Epic",
        0xFF2A1A2A,    // Purple tint
        0xFFBB44FF,    // Purple border
        0xFF7D1D8C,    // Dark purple
        true,
        true
    ),
    MYTHIC(
        5, 0xFF8800, "Mythic",
        0xFF2A2A1A,    // Gold tint
        0xFFFFAA00,    // Gold border
        0xFFCC8800,    // Dark gold
        true,
        true
    );

    public final int tier;
    public final int color;
    public final String displayName;
    public final int cardBgColor;
    public final int borderPrimary;
    public final int borderSecondary;
    public final boolean hasGlow;
    public final boolean hasParticles;

    RarityConfig(int tier, int color, String displayName,
                 int cardBgColor, int borderPrimary, int borderSecondary,
                 boolean hasGlow, boolean hasParticles) {
        this.tier = tier;
        this.color = color;
        this.displayName = displayName;
        this.cardBgColor = cardBgColor;
        this.borderPrimary = borderPrimary;
        this.borderSecondary = borderSecondary;
        this.hasGlow = hasGlow;
        this.hasParticles = hasParticles;
    }

    public int getGlowRadius() {
        return switch(this) {
            case RARE -> 2;
            case EPIC -> 3;
            case MYTHIC -> 4;
            default -> 0;
        };
    }

    public float getParticleIntensity() {
        return switch(this) {
            case RARE -> 0.3f;
            case EPIC -> 0.6f;
            case MYTHIC -> 1.0f;
            default -> 0.0f;
        };
    }
}
