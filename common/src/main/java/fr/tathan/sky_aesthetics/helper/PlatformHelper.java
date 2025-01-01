package fr.tathan.sky_aesthetics.helper;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class PlatformHelper {

    @ExpectPlatform
    public static boolean isModLoaded(String modid) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean modCompat(ClientLevel level) {
        throw new AssertionError();
    }

}
