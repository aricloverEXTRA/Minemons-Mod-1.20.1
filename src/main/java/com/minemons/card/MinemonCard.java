package com.minemons.card;

/**
 * A Minemon creature card — the main fighters in a duel.
 */
public class MinemonCard extends Card {

    private final int maxHp;
    private final int baseAttack;
    private final String passiveAbility;
    private final String passiveDescription;
    /** Whether this card can use any energy type (e.g. Sheep, Cow, Pig) */
    private final boolean isNeutralEnergy;

    public MinemonCard(String id, String displayName, Element element, Rarity rarity,
                       int maxHp, int baseAttack,
                       String passiveAbility, String passiveDescription,
                       boolean isNeutralEnergy,
                       String description, String texturePath) {
        super(id, displayName, CardType.MINEMON, element, rarity, description, texturePath);
        this.maxHp = maxHp;
        this.baseAttack = baseAttack;
        this.passiveAbility = passiveAbility;
        this.passiveDescription = passiveDescription;
        this.isNeutralEnergy = isNeutralEnergy;
    }

    public int getMaxHp()                 { return maxHp; }
    public int getBaseAttack()            { return baseAttack; }
    public String getPassiveAbility()     { return passiveAbility; }
    public String getPassiveDescription() { return passiveDescription; }
    public boolean isNeutralEnergy()      { return isNeutralEnergy; }

    /** Compute final damage against a defender, factoring in elemental advantage. */
    public int computeDamage(MinemonCard defender) {
        float multiplier = element.getMultiplierVs(defender.getElement());
        return Math.round(baseAttack * multiplier);
    }
}
