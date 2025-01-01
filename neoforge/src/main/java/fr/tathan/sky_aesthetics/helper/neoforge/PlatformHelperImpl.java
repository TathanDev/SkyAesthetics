package fr.tathan.sky_aesthetics.helper.neoforge;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.povstalec.stellarview.client.render.level.StellarViewOverworldEffects;
import net.povstalec.stellarview.common.config.OverworldConfig;

public class PlatformHelperImpl {

    public static boolean isModLoaded(String modid) {
        return ModList.get().isLoaded(modid);
    }

    public static boolean modCompat(ClientLevel level) {
        if (isStellarViewLoaded(level.dimension()) ) {
            level.effects = new StellarViewOverworldEffects();
            return false;
        }
        return true;
    }

    public static boolean isStellarViewLoaded(ResourceKey<Level> dimension) {
        return (isModLoaded("stellarview") && dimension.equals(Level.OVERWORLD)) && OverworldConfig.replace_vanilla.get();
    }
}
