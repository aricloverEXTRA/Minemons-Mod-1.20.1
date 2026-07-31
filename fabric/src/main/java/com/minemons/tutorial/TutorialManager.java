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
    private static final TutorialStep[] STEPS = TutorialStep.createDefaultSteps();

    private boolean tutorialCompleted = false;
    private TutorialStep.TutorialTopic currentTopic = TutorialStep.TutorialTopic.COLLECTION;
    private int currentStep = 0;

    public void readFromNbt(NbtCompound tag) {
        if (tag.contains("tutorial_completed")) tutorialCompleted = tag.getBoolean("tutorial_completed");
        if (tag.contains("tutorial_topic")) {
            try {
                currentTopic = TutorialStep.TutorialTopic.valueOf(tag.getString("tutorial_topic"));
            } catch (IllegalArgumentException ignored) {
                currentTopic = TutorialStep.TutorialTopic.COLLECTION;
            }
        }
        if (tag.contains("tutorial_step")) currentStep = clampStep(tag.getInt("tutorial_step"));
    }

    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("tutorial_completed", tutorialCompleted);
        tag.putString("tutorial_topic", currentTopic.name());
        tag.putInt("tutorial_step", currentStep);
    }

    public void advance() {
        if (currentStep < STEPS.length - 1) {
            currentStep++;
            currentTopic = STEPS[currentStep].getTopic();
            tutorialCompleted = false;
        } else {
            tutorialCompleted = true;
        }
        LOGGER.info("Tutorial progress: {} (step {}/{})", currentTopic, currentStep + 1, STEPS.length);
    }

    public void nextStep() {
        advance();
    }

    public void skipTutorial() {
        tutorialCompleted = true;
        LOGGER.info("Tutorial skipped by player");
    }

    public void restartTutorial() {
        tutorialCompleted = false;
        currentStep = 0;
        currentTopic = STEPS[0].getTopic();
        LOGGER.info("Tutorial restarted by player");
    }

    public void completeTopic() {
        TutorialStep.TutorialTopic topic = currentTopic;
        do {
            advance();
        } while (!tutorialCompleted && currentTopic == topic);
    }

    public TutorialStep getCurrentTutorialStep() {
        return tutorialCompleted ? null : STEPS[clampStep(currentStep)];
    }

    public int getCompletedStepCount() { return clampStep(currentStep); }
    public int getTotalStepCount() { return STEPS.length; }
    public boolean isTutorialCompleted() { return tutorialCompleted; }
    public TutorialStep.TutorialTopic getCurrentTopic() { return currentTopic; }
    public int getCurrentStep() { return currentStep; }

    public void setCurrentTopic(TutorialStep.TutorialTopic topic) {
        this.currentTopic = topic;
        this.currentStep = findFirstStep(topic);
        this.tutorialCompleted = false;
    }

    private int findFirstStep(TutorialStep.TutorialTopic topic) {
        for (int i = 0; i < STEPS.length; i++) if (STEPS[i].getTopic() == topic) return i;
        return 0;
    }

    private int clampStep(int step) {
        if (step < 0) return 0;
        if (step >= STEPS.length) return STEPS.length - 1;
        return step;
    }
}
