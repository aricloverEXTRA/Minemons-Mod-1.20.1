package com.minemons.card;

public enum Rarity {
    COMMON(1, 0xAAAAAA, "Common"),
    UNCOMMON(2, 0x55AA55, "Uncommon"),
    RARE(3, 0x5555FF, "Rare"),
    EPIC(4, 0xAA00AA, "Epic"),
    MYTHIC(5, 0xFF8800, "Mythic");

    public final int tier;
    public final int color;
    public final String displayName;

    Rarity(int tier, int color, String displayName) {
        this.tier = tier;
        this.color = color;
        this.displayName = displayName;
    }
}
