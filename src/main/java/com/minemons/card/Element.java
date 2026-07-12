package com.minemons.card;

/**
 * Elemental types for Minemons cards.
 * Each element has strengths, weaknesses, and special interactions.
 */
public enum Element {
    AQUA("Aqua", 0x3A9EFF, "💧"),
    EMBERA("Embera", 0xFF4500, "🔥"),
    ATMOSA("Atmosa", 0xB0E0FF, "💨"),
    FLORA("Flora", 0x2ECC40, "🌿"),
    TERRA("Terra", 0x8B6914, "🪨"),
    COSMA("Cosma", 0x6A0DAD, "🌌"),
    LUXA("Luxa", 0xFFE066, "✨"),
    ELECTRA("Electra", 0xFFFF00, "⚡"),
    CRYSTRA("Crystra", 0xA8E6CF, "💎"),
    NEXA("Nexa", 0xCCCCCC, "⚖️");

    public final String displayName;
    public final int color;
    public final String symbol;

    Element(String displayName, int color, String symbol) {
        this.displayName = displayName;
        this.color = color;
        this.symbol = symbol;
    }

    /**
     * Returns the damage multiplier when THIS element attacks the given defender element.
     */
    public float getMultiplierVs(Element defender) {
        // Cosma ↔ Luxa are strong counters
        if (this == COSMA && defender == LUXA) return 2.0f;
        if (this == LUXA && defender == COSMA) return 2.0f;

        // Standard elemental advantages
        if (this == EMBERA && defender == FLORA)  return 1.5f;
        if (this == EMBERA && defender == AQUA)   return 0.5f;
        if (this == AQUA  && defender == EMBERA)  return 1.5f;
        if (this == AQUA  && defender == ELECTRA) return 1.5f;
        if (this == AQUA  && defender == TERRA)   return 0.75f;
        if (this == FLORA && defender == TERRA)   return 1.5f;
        if (this == FLORA && defender == AQUA)    return 0.75f;
        if (this == TERRA && defender == ELECTRA) return 1.5f;
        if (this == TERRA && defender == FLORA)   return 0.75f;
        if (this == ELECTRA && defender == ATMOSA)return 1.5f;
        if (this == ELECTRA && defender == TERRA) return 0.5f;
        if (this == ELECTRA && defender == AQUA)  return 0.5f;
        if (this == ATMOSA && defender == ELECTRA)return 0.75f;
        if (this == CRYSTRA && defender == TERRA) return 1.25f;
        if (this == CRYSTRA && defender == COSMA) return 1.25f;
        if (this == COSMA && defender == CRYSTRA) return 0.75f;

        // Nexa is always neutral
        if (this == NEXA || defender == NEXA) return 1.0f;

        return 1.0f;
    }

    public boolean isNeutral() { return this == NEXA; }
}
