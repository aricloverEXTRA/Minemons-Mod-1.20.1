package com.minemons;

import com.minemons.command.MinemonsCommand;
import com.minemons.data.PlayerDataManager;
import com.minemons.network.PacketRegistry;
import com.minemons.registry.CardRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinemonsMain implements ModInitializer {

    public static final String MOD_ID = "minemons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[Minemons] Initializing...");

        CardRegistry.init();
        PacketRegistry.registerServerPackets();

        CommandRegistrationCallback.EVENT.register((d, ra, env) -> MinemonsCommand.register(d));

        // Give starter pack to new players on join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            PlayerDataManager.ensureLoaded(handler.player));

        // Release PersistentState cache on server stop
        ServerLifecycleEvents.SERVER_STOPPED.register(server ->
            PlayerDataManager.clearInstance());

        LOGGER.info("[Minemons] Ready — {} cards, commands: /mn /minemons", CardRegistry.getAllCards().size());
    }
}
