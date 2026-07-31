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

    public static TutorialStep[] createDefaultSteps() {
        return new TutorialStep[] {
            new TutorialStep(TutorialTopic.COLLECTION, 0, "Welcome to Minemons", "Minemons is a 60-card deck battler where Minecraft mobs become fighters.", "Open your collection/deck commands, then build toward a consistent 60-card deck.", false),
            new TutorialStep(TutorialTopic.CARD_TYPES, 0, "Four card roles", "Minemon cards fight, Consumables heal or buff, Trainers change tempo, and Places reshape the battlefield.", "Hover cards to read their role, rarity, element, HP, attack, passive, and rules text.", false),
            new TutorialStep(TutorialTopic.ELEMENTS, 0, "Element identity", "Terra, Flora, Electra, Embera, Aqua, Crystra, Luxa, Cosma, Atmosa, and Nexa define strengths and weaknesses.", "Draft around an element core, then add Nexa cards when you need flexible energy.", false),
            new TutorialStep(TutorialTopic.DECK_BUILDING, 0, "Deck plan", "A legal deck is 60 cards. Your active Minemon fights up front while up to five sit on the sideline.", "Mix attackers, support mobs, healing food, Trainers, and Places instead of filling a deck with only damage.", false),
            new TutorialStep(TutorialTopic.DUELING, 0, "Winning a duel", "At duel start, six prize cards are set aside. Knockouts claim prizes; taking all six wins.", "Protect your active Minemon, rotate damaged mobs to the sideline, and watch your deck count because decking out loses.", false),
            new TutorialStep(TutorialTopic.DUELING, 1, "Passives and abilities", "Mobs have passive abilities and triggered effects: Sheep can protect against fire, food can heal, and some mobs draw or disrupt.", "Read each passive before attacking; many Minemons are strongest when their element, Place, or item support is active.", false),
            new TutorialStep(TutorialTopic.TRADING, 0, "Trading and collection growth", "Trade duplicate cards with other players and tune your deck as your collection grows.", "Use trades to chase missing rarity pieces and support cards for your favorite element.", false),
            new TutorialStep(TutorialTopic.FIRST_WIN, 0, "Ready to battle", "You now know the loop: collect, build, duel, take prizes, and improve your deck.", "Press H anytime to replay this guide. Press B to hide it when you are ready.", false)
        };
    }
}
