package fr.tathan.sky_aesthetics.client.utils;

import fr.tathan.sky_aesthetics.client.DimensionRenderer;
import fr.tathan.sky_aesthetics.client.data.SkiesRegistry;
import fr.tathan.sky_aesthetics.client.settings.SkyProperties;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.function.Consumer;

public class SkyHelper {
//    private static final Identifier END_SKY_TEXTURE = Identifier.withDefaultNamespace("textures/environment/end_sky.png");
//
    public static boolean canRenderSky(ClientLevel level, Consumer<SkyProperties> action) {

        if((SkiesRegistry.SKY_DEV != null && SkiesRegistry.USE_SKY_DEV) && level.dimension().equals(SkiesRegistry.SKY_DEV.world())) {
            // If the dev sky is set, we render it
            action.accept(SkiesRegistry.SKY_DEV);
            return true;
        }

        for (SkyProperties sky : SkiesRegistry.SKY_PROPERTIES.values()) {
            if (sky.world().equals(level.dimension())) {

                // Check if the sky is disabled in the config
                //if(Arrays.stream(SkyAesthetics.CONFIG.disabledSkies).anyMatch((s)-> s.equals(sky.getSkyId().toString()))) return false;

                // Check if the sky's dimension is disabled in the properties
                //if(Arrays.stream(SkyAesthetics.CONFIG.disabledDimensions).anyMatch((s)-> s.equals(sky.getDimension().identifier().toString()))) return false;

                DimensionRenderer renderer = sky.toDimensionRenderer();
                if (renderer.renderCondition != null && renderer.renderCondition.isSkyRendered(DimensionRenderer.getServerLevel())) {
                    return false;
                }
                action.accept(sky);
                return true;
            }
        }
        return false;
    }
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