package com.minemons.card;

import net.minecraft.nbt.NbtCompound;

/**
 * Base class for all Minemons cards.
 * All card data is immutable from the registry; instances track runtime state.
 */
public abstract class Card {

    protected final String id;
    protected final String displayName;
    protected final CardType type;
    protected final Element element;
    protected final Rarity rarity;
    protected final String description;
    /** Relative path under assets/minemons/textures/card/ for the card art */
    protected final String texturePath;

    protected Card(String id, String displayName, CardType type, Element element,
                   Rarity rarity, String description, String texturePath) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.element = element;
        this.rarity = rarity;
        this.description = description;
        this.texturePath = texturePath;
    }

    public String getId()          { return id; }
    public String getDisplayName() { return displayName; }
    public CardType getType()      { return type; }
    public Element getElement()    { return element; }
    public Rarity getRarity()      { return rarity; }
    public String getDescription() { return description; }
    public String getTexturePath() { return texturePath; }

    /** Write card identity to NBT (for deck/hand storage). */
    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putString("id", id);
        tag.putString("type", type.name());
        return tag;
    }

    public static String readIdFromNbt(NbtCompound tag) {
        return tag.getString("id");
    }

    @Override
    public String toString() {
        return "[" + rarity.displayName + "] " + displayName + " (" + element.displayName + " " + type + ")";
    }
}
