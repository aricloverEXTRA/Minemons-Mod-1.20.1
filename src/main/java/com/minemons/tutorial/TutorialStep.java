package com.minemons.tutorial;

/**
 * Tutorial step definitions for first-time player onboarding.
 * Each step teaches a specific mechanic with prompts and highlights.
 */
public class TutorialStep {

    public enum TutorialTopic {
        COLLECTION("Understanding Your Collection", "Learn how to view and organize your cards"),
        DECK_BUILDING("Building a Deck", "Create a 60-card deck from your collection"),
        CARD_TYPES("Card Types", "Learn about Minemons, Consumables, Trainers, and Places"),
        RARITY_SYSTEM("Card Rarity", "Understand rarity tiers and card strengths"),
        ELEMENTS("Elements & Advantages", "Master elemental matchups and type advantages"),
        DUELING("Dueling Basics", "Learn how to battle other trainers"),
        TRADING("Trading Cards", "Exchange cards with other players"),
        FIRST_WIN("Your First Win", "Complete your first duel successfully");

        public final String title;
        public final String description;

        TutorialTopic(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    private final TutorialTopic topic;
    private final int stepNumber;
    private final String title;
    private final String description;
    private final String instruction;
    private final boolean requiresAction;

    public TutorialStep(TutorialTopic topic, int stepNumber, String title, String description, String instruction, boolean requiresAction) {
        this.topic = topic;
        this.stepNumber = stepNumber;
        this.title = title;
        this.description = description;
        this.instruction = instruction;
        this.requiresAction = requiresAction;
    }

    public TutorialTopic getTopic() { return topic; }
    public int getStepNumber() { return stepNumber; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getInstruction() { return instruction; }
    public boolean isRequiresAction() { return requiresAction; }
}
