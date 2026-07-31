package com.minemons;

import com.minemons.network.PacketRegistry;
import com.minemons.tutorial.TutorialClientController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MinemonsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MinemonsMain.LOGGER.info("[Minemons] Registering client packet handlers...");
        PacketRegistry.registerClientPackets();
        TutorialClientController.register();
        MinemonsMain.LOGGER.info("[Minemons] Client ready.");
    }
}
