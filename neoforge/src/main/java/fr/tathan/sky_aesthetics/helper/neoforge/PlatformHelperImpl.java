package fr.tathan.sky_aesthetics.helper.neoforge;


import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class PlatformHelperImpl {

    public static boolean isModLoaded(String modid) {
        return ModList.get().isLoaded(modid);
    }

    public static Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }
}