package fr.tathan.sky_aesthetics.client.skies.utils;

import com.mojang.blaze3d.vertex.*;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.data.SkiesRegistry;
import fr.tathan.sky_aesthetics.client.skies.DimensionSky;
import fr.tathan.sky_aesthetics.helper.PlatformHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.function.Consumer;

public class SkyHelper {
//    private static final Identifier END_SKY_TEXTURE = Identifier.withDefaultNamespace("textures/environment/end_sky.png");
//
//    public static boolean canRenderSky(ClientLevel level, Consumer<DimensionSky> action) {
//
//        if((SkiesRegistry.SKY_DEV != null && SkiesRegistry.USE_SKY_DEV) && level.dimension().equals(SkiesRegistry.SKY_DEV.getDimension())) {
//            // If the dev sky is set, we render it
//            action.accept(SkiesRegistry.SKY_DEV);
//            return true;
//        }
//
//        for (DimensionSky sky : SkiesRegistry.SKY_PROPERTIES.values()) {
//            if (sky.getDimension().equals(level.dimension())) {
//
//                // Check if the sky is disabled in the config
//                if(Arrays.stream(SkyAesthetics.CONFIG.disabledSkies).anyMatch((s)-> s.equals(sky.getSkyId().toString()))) return false;
//
//                // Check if the sky's dimension is disabled in the properties
//                if(Arrays.stream(SkyAesthetics.CONFIG.disabledDimensions).anyMatch((s)-> s.equals(sky.getDimension().identifier().toString()))) return false;
//
//                DimensionRenderer renderer = sky.getRenderer();
//                if (renderer.renderCondition != null && renderer.renderCondition.isSkyRendered(DimensionRenderer.getServerLevel())) {
//                    return false;
//                }
//                action.accept(sky);
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean isAModCancelRendering(String[] modIds) {
//        for(String modId : modIds) {
//            if (PlatformHelper.isModLoaded(modId)) {
//                return true;
//            }
//        }
//        return false;
//    }
}