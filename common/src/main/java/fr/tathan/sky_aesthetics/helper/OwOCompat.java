package fr.tathan.sky_aesthetics.helper;

import fr.tathan.sky_aesthetics.client.screens.SkyModificationScreen;
import net.minecraft.client.Minecraft;

public class OwOCompat {

    public static void openOwOSettings(Minecraft mc) {
        if(PlatformHelper.isModLoaded("owo")) {
            mc.setScreen(new SkyModificationScreen());
        }
    }
}
