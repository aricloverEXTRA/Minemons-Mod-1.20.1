package com.minemons;

import com.minemons.network.PacketRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MinemonsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MinemonsMain.LOGGER.info("[Minemons] Registering client packet handlers...");
        PacketRegistry.registerClientPackets();
        MinemonsMain.LOGGER.info("[Minemons] Client ready.");
    }
}
