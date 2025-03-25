package fr.tathan.sky_aesthetics.helper.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class PlatformHelperImpl {

    public static boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }

    public static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

}
