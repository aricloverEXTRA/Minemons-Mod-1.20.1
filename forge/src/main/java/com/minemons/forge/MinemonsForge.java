package com.minemons.forge;

import net.minecraftforge.fml.common.Mod;

@Mod(MinemonsForge.MOD_ID)
public class MinemonsForge {
    public static final String MOD_ID = "minemons";

    public MinemonsForge() {
        // Forge loader entrypoint. Shared gameplay registration will be wired here
        // as common code is separated from Fabric-specific implementation details.
    }
}
