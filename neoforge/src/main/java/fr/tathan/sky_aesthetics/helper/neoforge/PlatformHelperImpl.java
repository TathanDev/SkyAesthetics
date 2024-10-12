package fr.tathan.sky_aesthetics.helper.neoforge;


import net.neoforged.fml.ModList;

public class PlatformHelperImpl {

    public static boolean isModLoaded(String modid) {
        return ModList.get().isLoaded(modid);
    }
}
