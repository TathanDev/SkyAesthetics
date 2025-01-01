package fr.tathan.sky_aesthetics.neoforge.compat;

import fr.tathan.sky_aesthetics.helper.PlatformHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.povstalec.stellarview.client.render.level.StellarViewOverworldEffects;
import net.povstalec.stellarview.common.config.OverworldConfig;

public class StellarViewCompat {

    public static boolean isStellarViewLoaded(ResourceKey<Level> dimension) {
        return (PlatformHelper.isModLoaded("stellarview") && dimension.equals(Level.OVERWORLD)) && OverworldConfig.replace_vanilla.get();
    }

    public static boolean cancelSkyRendering(ClientLevel level) {
        if (isStellarViewLoaded(level.dimension()) ) {
            level.effects = new StellarViewOverworldEffects();
            return false;
        }
        return true;
    }
}
