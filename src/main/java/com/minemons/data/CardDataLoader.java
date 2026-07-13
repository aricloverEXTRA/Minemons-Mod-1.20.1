package com.minemons.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minemons.card.Card;
import com.minemons.registry.CardRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Loads card definitions from JSON files, enabling modder-friendly card registration.
 * Cards can be loaded from:
 * - classpath resources (built-in cards)
 * - config/minemons/ directory (user/modder cards)
 */
public class CardDataLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger("Minemons");
    private static final Gson GSON = new Gson();
    private static final String CARD_CONFIG_DIR = "config/minemons";
    private static final String CUSTOM_CARDS_FILE = "config/minemons/custom_cards.json";

    /**
     * Load all available cards from built-in and custom sources
     */
    public static void loadAllCards() {
        LOGGER.info("Loading Minemons card definitions...");
        
        // Load custom cards from config directory
        loadCustomCards();
        
        LOGGER.info("Card loading complete!");
    }

    /**
     * Load custom cards from the config directory
     */
    private static void loadCustomCards() {
        try {
            Path customCardsPath = Paths.get(CUSTOM_CARDS_FILE);
            if (!Files.exists(customCardsPath)) {
                LOGGER.debug("No custom cards file found at {}", CUSTOM_CARDS_FILE);
                createDefaultCustomCardsFile();
                return;
            }

            String content = new String(Files.readAllBytes(customCardsPath), StandardCharsets.UTF_8);
            JsonArray cardsArray = JsonParser.parseString(content).getAsJsonArray();
            
            int loaded = 0;
            for (int i = 0; i < cardsArray.size(); i++) {
                try {
                    JsonObject cardJson = cardsArray.get(i).getAsJsonObject();
                    CardDefinition def = new CardDefinition(cardJson);
                    Card card = def.toCard();
                    CardRegistry.registerCard(card);
                    loaded++;
                } catch (Exception e) {
                    LOGGER.warn("Failed to load custom card at index {}", i, e);
                }
            }
            LOGGER.info("Loaded {} custom card definitions from {}", loaded, CUSTOM_CARDS_FILE);
        } catch (IOException e) {
            LOGGER.error("Error reading custom cards file", e);
        }
    }

    /**
     * Create a template custom_cards.json file if it doesn't exist
     */
    private static void createDefaultCustomCardsFile() {
        try {
            Path dir = Paths.get(CARD_CONFIG_DIR);
            Files.createDirectories(dir);
            
            String template = """
            [
              {
                "id": "custom_example",
                "displayName": "Example Minemon",
                "type": "MINEMON",
                "element": "NEXA",
                "rarity": "COMMON",
                "description": "This is an example custom card.",
                "art": {
                  "path": "sheep/sheep.png",
                  "type": "entity"
                },
                "maxHp": 50,
                "baseAttack": 12,
                "passiveAbility": "Example Ability",
                "passiveDescription": "Does something cool!",
                "neutralEnergy": true
              }
            ]
            """;
            
            Path customCardsPath = Paths.get(CUSTOM_CARDS_FILE);
            Files.write(customCardsPath, template.getBytes(StandardCharsets.UTF_8));
            LOGGER.info("Created template custom_cards.json at {}", CUSTOM_CARDS_FILE);
        } catch (IOException e) {
            LOGGER.error("Failed to create custom cards template", e);
        }
    }

    /**
     * Load a specific card definition from JSON
     */
    public static Card loadCardFromJson(JsonObject json) throws InvalidCardDefinitionException {
        try {
            validateCardDefinition(json);
            CardDefinition def = new CardDefinition(json);
            return def.toCard();
        } catch (IllegalArgumentException e) {
            throw new InvalidCardDefinitionException("Invalid card definition: " + e.getMessage(), e);
        }
    }

    /**
     * Validate a card definition JSON object
     */
    private static void validateCardDefinition(JsonObject json) throws IllegalArgumentException {
        String[] required = {"id", "displayName", "type", "element", "rarity", "description", "art"};
        for (String field : required) {
            if (!json.has(field)) {
                throw new IllegalArgumentException("Missing required field: " + field);
            }
        }
        
        try {
            CardType.valueOf(json.get("type").getAsString().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid card type: " + json.get("type"));
        }
        
        try {
            Element.valueOf(json.get("element").getAsString().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid element: " + json.get("element"));
        }
        
        try {
            Rarity.valueOf(json.get("rarity").getAsString().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid rarity: " + json.get("rarity"));
        }
    }

    public static class InvalidCardDefinitionException extends Exception {
        public InvalidCardDefinitionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
