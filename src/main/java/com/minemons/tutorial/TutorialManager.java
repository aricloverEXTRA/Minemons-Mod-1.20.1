package com.minemons.tutorial;

import net.minecraft.nbt.NbtCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks tutorial progress and state for first-time players.
 * Persisted in player data to resume where they left off.
 */
public class TutorialManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("Minemons-Tutorial");

    private boolean tutorialCompleted = false;
    private TutorialStep.TutorialTopic currentTopic = TutorialStep.TutorialTopic.COLLECTION;
    private int currentStep = 0;

    public TutorialManager() {
    }

    /**
     * Load tutorial state from NBT (player data)
     */
    public void readFromNbt(NbtCompound tag) {
        if (tag.contains("tutorial_completed")) {
            tutorialCompleted = tag.getBoolean("tutorial_completed");
        }
        if (tag.contains("tutorial_topic")) {
            try {
                currentTopic = TutorialStep.TutorialTopic.valueOf(tag.getString("tutorial_topic"));
            } catch (IllegalArgumentException ignored) {
                currentTopic = TutorialStep.TutorialTopic.COLLECTION;
            }
        }
        if (tag.contains("tutorial_step")) {
            currentStep = tag.getInt("tutorial_step");
        }
    }

    /**
     * Save tutorial state to NBT (player data)
     */
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("tutorial_completed", tutorialCompleted);
        tag.putString("tutorial_topic", currentTopic.name());
        tag.putInt("tutorial_step", currentStep);
    }

    /**
     * Advance to the next tutorial step
     */
    public void nextStep() {
        currentStep++;
        LOGGER.info("Tutorial progress: {} (step {})", currentTopic, currentStep);
    }

    /**
     * Skip the entire tutorial
     */
    public void skipTutorial() {
        tutorialCompleted = true;
        LOGGER.info("Tutorial skipped by player");
    }

    /**
     * Complete the current topic and move to next
     */
    public void completeTopic() {
        TutorialStep.TutorialTopic[] topics = TutorialStep.TutorialTopic.values();
        int nextIndex = currentTopic.ordinal() + 1;
        
        if (nextIndex < topics.length) {
            currentTopic = topics[nextIndex];
            currentStep = 0;
            LOGGER.info("Completed topic, moving to: {}", currentTopic);
        } else {
            tutorialCompleted = true;
            LOGGER.info("Tutorial fully completed!");
        }
    }

    public boolean isTutorialCompleted() { return tutorialCompleted; }
    public TutorialStep.TutorialTopic getCurrentTopic() { return currentTopic; }
    public int getCurrentStep() { return currentStep; }

    public void setCurrentTopic(TutorialStep.TutorialTopic topic) {
        this.currentTopic = topic;
        this.currentStep = 0;
    }
}
