package com.minemons.card;

/**
 * Place cards modify the battle field — biome/terrain effects that last until replaced.
 */
public class PlaceCard extends Card {

    public enum PlaceEffect {
        BOOST_ELEMENT,       // Buff a specific element
        WEAKEN_ELEMENT,      // Debuff a specific element
        HEAL_ACTIVE_TURN,    // Heal active Minemon each turn
        EXTRA_DRAW,          // Draw bonus card on first turn
        CHIP_DAMAGE_BOTH,    // Both actives take chip damage each turn
        DISABLE_PASSIVES,    // Disables all passive abilities
        DOUBLE_TRAINER,      // Trainer card effects doubled
        SLOWER_DRAW,         // Draw 1 fewer card per turn
        EXTRA_PRIZE_ON_KO,   // Grant extra prize card on KO
        REVEAL_TOP_CARD,     // Reveal top deck card each turn
        SWAP_COST,           // Swapping cards costs an extra action
        STATUS_EXTEND,       // Status effects last 1 extra turn
        BOOST_HEAL           // Healing consumables +50% effective
    }

    private final PlaceEffect effect;
    private final Element affectedElement; // nullable — some effects are global
    private final int value;

    public PlaceCard(String id, String displayName, Element biomeElement, Rarity rarity,
                     PlaceEffect effect, Element affectedElement, int value,
                     String description, String texturePath) {
        super(id, displayName, CardType.PLACE, biomeElement, rarity, description, texturePath);
        this.effect = effect;
        this.affectedElement = affectedElement;
        this.value = value;
    }

    public PlaceEffect getEffect()          { return effect; }
    public Element getAffectedElement()     { return affectedElement; }
    public int getValue()                   { return value; }
}
