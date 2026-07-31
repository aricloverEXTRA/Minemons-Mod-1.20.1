package com.minemons.card;

/**
 * Consumable cards: food items and potions that apply effects during battle.
 */
public class ConsumableCard extends Card {

    public enum ConsumableEffect {
        HEAL,          // Restore HP to active Minemon
        HP_BOOST,      // Increase max HP temporarily
        ATTACK_BOOST,  // Increase attack for next turn
        SHIELD,        // Absorb next hit
        FLEE,          // Skip attacker's next turn (Invisibility Potion)
        DEBUFF_ENEMY,  // Reduce enemy attack (Weakness Potion)
        STAMINA        // Extra action this turn
    }

    private final ConsumableEffect effect;
    private final int value;          // HP restored, damage absorbed, etc.
    private final int duration;       // Turns the effect lasts (0 = instant)

    public ConsumableCard(String id, String displayName, Element element, Rarity rarity,
                          ConsumableEffect effect, int value, int duration,
                          String description, String texturePath) {
        super(id, displayName, CardType.CONSUMABLE, element, rarity, description, texturePath);
        this.effect = effect;
        this.value = value;
        this.duration = duration;
    }

    public ConsumableEffect getEffect() { return effect; }
    public int getValue()               { return value; }
    public int getDuration()            { return duration; }
}
