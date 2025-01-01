package fr.tathan.sky_aesthetics.helper.neoforge;

import fr.tathan.sky_aesthetics.neoforge.compat.StellarViewCompat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.fml.ModList;

public class PlatformHelperImpl {

    public static boolean isModLoaded(String modid) {
        return ModList.get().isLoaded(modid);
    }

    public static boolean modCompat(ClientLevel level) {

        return checkForStellarView(level);
    }

    public static boolean checkForStellarView(ClientLevel level) {
        if(!isModLoaded("stellarview")) {
            return true;
        }
        return StellarViewCompat.cancelSkyRendering(level);
    }

}
