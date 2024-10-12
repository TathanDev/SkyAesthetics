package fr.tathan.sky_aesthetics.helper.fabric;

import net.fabricmc.loader.api.FabricLoader;

public class PlatformHelperImpl {

    public static boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }
}
