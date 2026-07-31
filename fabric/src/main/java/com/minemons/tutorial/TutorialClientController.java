package com.minemons.tutorial;

import com.minemons.MinemonsMain;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class TutorialClientController {
    private static final TutorialManager MANAGER = new TutorialManager();
    private static KeyBinding tutorialKey;
    private static KeyBinding nextKey;
    private static KeyBinding skipKey;
    private static boolean visible = true;

    private TutorialClientController() {}

    public static void register() {
        tutorialKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.minemons.tutorial",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category.minemons"
        ));
        nextKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.minemons.tutorial_next",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            "category.minemons"
        ));
        skipKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.minemons.tutorial_skip",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "category.minemons"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(TutorialClientController::tick);
        HudRenderCallback.EVENT.register(TutorialClientController::render);
        MinemonsMain.LOGGER.info("[Minemons] Tutorial keybinds registered: H revisit, N next, B skip/hide.");
    }

    private static void tick(MinecraftClient client) {
        while (tutorialKey.wasPressed()) {
            visible = true;
            MANAGER.restartTutorial();
            send(client, "§aMinemons tutorial reopened. Press N to move through it or B to hide it.");
        }
        while (nextKey.wasPressed()) {
            visible = true;
            MANAGER.advance();
        }
        while (skipKey.wasPressed()) {
            visible = false;
            MANAGER.skipTutorial();
            send(client, "§7Minemons tutorial hidden. Press H whenever you want to revisit it.");
        }
    }

    private static void render(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || !visible) return;

        TutorialStep step = MANAGER.getCurrentTutorialStep();
        if (step == null) return;

        int width = client.getWindow().getScaledWidth();
        int x = Math.max(8, width - 238);
        int y = 18;
        TutorialRenderer.drawTutorialTooltip(
            context,
            client.textRenderer,
            step.getTitle(),
            step.getDescription() + "\n§e" + step.getInstruction() + "\n§8H: revisit  N: next  B: hide",
            x,
            y,
            226
        );
        TutorialRenderer.drawTutorialProgress(
            context,
            client.textRenderer,
            MANAGER.getCompletedStepCount() + 1,
            MANAGER.getTotalStepCount(),
            x,
            y + 88
        );
    }

    private static void send(MinecraftClient client, String message) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal(message), true);
        }
    }
}
