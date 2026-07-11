package com.minemons.card;

/**
 * Trainer cards: support effects that manipulate the deck, hand, or field.
 */
public class TrainerCard extends Card {

    public enum TrainerEffect {
        DRAW_CARDS,         // Draw N cards
        PEEK_DECK,          // Look at top N cards of deck
        REARRANGE_DECK,     // Reorder top N cards
        SWAP_FIELD,         // Move a benched card to active
        BUFF_ELEMENT,       // Boost element for N turns
        TAKE_EXTRA_PRIZE,   // Claim 2 prizes on next KO
        REVIVE_MINEMON,     // Put a fainted Minemon back on field with partial HP
        HEAL_BENCH,         // Heal all benched Minemons by N
        SEARCH_DECK,        // Search deck for a specific card
        DISRUPT_OPPONENT    // Make opponent skip an action or discard
    }

    private final TrainerEffect effect;
    private final int value;

    public TrainerCard(String id, String displayName, Rarity rarity,
                       TrainerEffect effect, int value,
                       String description, String texturePath) {
        super(id, displayName, CardType.TRAINER, Element.NEXA, rarity, description, texturePath);
        this.effect = effect;
        this.value = value;
    }

    public TrainerEffect getEffect() { return effect; }
    public int getValue()            { return value; }
}
