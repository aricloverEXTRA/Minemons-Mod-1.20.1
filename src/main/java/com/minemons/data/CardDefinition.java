package com.minemons.data;

import com.google.gson.JsonObject;
import com.minemons.card.*;

/**
 * Data-driven card definition loaded from JSON configuration.
 * Allows modders to add custom cards without code changes.
 */
public class CardDefinition {

    private final String id;
    private final String displayName;
    private final CardType type;
    private final String element;
    private final String rarity;
    private final String description;
    private final String texturePath;
    
    // Minemon-specific
    private final int maxHp;
    private final int baseAttack;
    private final String passiveAbility;
    private final String passiveDescription;
    private final boolean neutralEnergy;
    
    // Consumable-specific
    private final String consumableEffect;
    private final int consumableValue;
    
    // Trainer-specific
    private final String trainerEffect;
    
    // Place-specific
    private final String placeEffect;

    public CardDefinition(JsonObject json) {
        this.id = json.get("id").getAsString();
        this.displayName = json.get("displayName").getAsString();
        this.type = CardType.valueOf(json.get("type").getAsString().toUpperCase());
        this.element = json.get("element").getAsString();
        this.rarity = json.get("rarity").getAsString();
        this.description = json.get("description").getAsString();
        this.texturePath = json.getAsJsonObject("art").get("path").getAsString();
        
        // Minemon properties
        this.maxHp = json.has("maxHp") ? json.get("maxHp").getAsInt() : 0;
        this.baseAttack = json.has("baseAttack") ? json.get("baseAttack").getAsInt() : 0;
        this.passiveAbility = json.has("passiveAbility") ? json.get("passiveAbility").getAsString() : "";
        this.passiveDescription = json.has("passiveDescription") ? json.get("passiveDescription").getAsString() : "";
        this.neutralEnergy = json.has("neutralEnergy") && json.get("neutralEnergy").getAsBoolean();
        
        // Consumable properties
        this.consumableEffect = json.has("effect") ? json.get("effect").getAsString() : "";
        this.consumableValue = json.has("value") ? json.get("value").getAsInt() : 0;
        
        // Trainer properties
        this.trainerEffect = json.has("trainerEffect") ? json.get("trainerEffect").getAsString() : "";
        
        // Place properties
        this.placeEffect = json.has("placeEffect") ? json.get("placeEffect").getAsString() : "";
    }

    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public CardType getType() { return type; }
    public Element getElement() { return Element.valueOf(element.toUpperCase()); }
    public Rarity getRarity() { return Rarity.valueOf(rarity.toUpperCase()); }
    public String getDescription() { return description; }
    public String getTexturePath() { return texturePath; }
    
    public int getMaxHp() { return maxHp; }
    public int getBaseAttack() { return baseAttack; }
    public String getPassiveAbility() { return passiveAbility; }
    public String getPassiveDescription() { return passiveDescription; }
    public boolean isNeutralEnergy() { return neutralEnergy; }
    
    public String getConsumableEffect() { return consumableEffect; }
    public int getConsumableValue() { return consumableValue; }
    
    public String getTrainerEffect() { return trainerEffect; }
    public String getPlaceEffect() { return placeEffect; }

    /**
     * Convert this definition to an actual Card instance
     */
    public Card toCard() {
        return switch(type) {
            case MINEMON -> new MinemonCard(
                id, displayName, getElement(), getRarity(),
                maxHp, baseAttack, passiveAbility, passiveDescription,
                neutralEnergy, description, texturePath
            );
            case CONSUMABLE -> new ConsumableCard(
                id, displayName, getElement(), getRarity(),
                ConsumableCard.Effect.valueOf(consumableEffect.toUpperCase()),
                consumableValue, description, texturePath
            );
            case TRAINER -> new TrainerCard(
                id, displayName, getElement(), getRarity(),
                TrainerCard.Effect.valueOf(trainerEffect.toUpperCase()),
                description, texturePath
            );
            case PLACE -> new PlaceCard(
                id, displayName, getElement(), getRarity(),
                PlaceCard.Effect.valueOf(placeEffect.toUpperCase()),
                description, texturePath
            );
        };
    }
}
