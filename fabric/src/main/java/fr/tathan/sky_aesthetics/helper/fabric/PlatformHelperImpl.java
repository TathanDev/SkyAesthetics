package fr.tathan.sky_aesthetics.helper.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ClientLevel;

public class PlatformHelperImpl {

    public static boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }

    public static boolean modCompat(ClientLevel level) {
        return true;
    }

}
