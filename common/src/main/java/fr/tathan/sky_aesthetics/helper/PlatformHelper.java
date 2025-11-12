package fr.tathan.sky_aesthetics.helper;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class PlatformHelper {

    @ExpectPlatform
    public static boolean isModLoaded(String modid) {
        throw new AssertionError();
    }
}
