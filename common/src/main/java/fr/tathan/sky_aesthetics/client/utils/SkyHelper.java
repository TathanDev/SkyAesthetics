package fr.tathan.sky_aesthetics.client.utils;

import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.DimensionRenderer;
import fr.tathan.sky_aesthetics.client.data.SkiesRegistry;
import fr.tathan.sky_aesthetics.client.settings.SkyProperties;
import fr.tathan.sky_aesthetics.helper.PlatformHelper;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.OptionalInt;
import java.util.function.Consumer;

public class SkyHelper {

    public static boolean canRenderSky(Level level, Consumer<SkyProperties> action) {
        for (SkyProperties sky : SkiesRegistry.SKY_PROPERTIES.values()) {
            if (sky.world().equals(level.dimension())) {

                // Check if the sky is disabled in the config
                if(Arrays.stream(SkyAesthetics.CONFIG.disabledSkies).anyMatch((s)-> s.equals(sky.id().toString()))) return false;

                // Check if the sky's dimension is disabled in the properties
                if(Arrays.stream(SkyAesthetics.CONFIG.disabledDimensions).anyMatch((s)-> s.equals(sky.world().identifier().toString()))) return false;


                if (sky.renderCondition().isPresent() && !sky.renderCondition().get().isSkyRendered(DimensionRenderer.getServerLevel())) {
                    return false;
                }
                action.accept(sky);
                return true;
            }
        }
        return false;
    }

    public static OptionalInt getActiveCloudHeight(Level level) {
        for (SkyProperties sky : SkiesRegistry.SKY_PROPERTIES.values()) {
            if (sky.world().equals(level.dimension())) {
                if (Arrays.stream(SkyAesthetics.CONFIG.disabledSkies).anyMatch(s -> s.equals(sky.id().toString()))) continue;
                if (Arrays.stream(SkyAesthetics.CONFIG.disabledDimensions).anyMatch(s -> s.equals(sky.world().identifier().toString()))) continue;
                if (sky.renderCondition().isPresent() && !sky.renderCondition().get().isSkyRendered(DimensionRenderer.getServerLevel())) continue;
                return sky.cloudHeight();
            }
        }
        return OptionalInt.empty();
    }

    public static OptionalInt getActiveCloudColor(Level level) {
        for (SkyProperties sky : SkiesRegistry.SKY_PROPERTIES.values()) {
            if (sky.world().equals(level.dimension())) {
                if (Arrays.stream(SkyAesthetics.CONFIG.disabledSkies).anyMatch(s -> s.equals(sky.id().toString()))) continue;
                if (Arrays.stream(SkyAesthetics.CONFIG.disabledDimensions).anyMatch(s -> s.equals(sky.world().identifier().toString()))) continue;
                if (sky.renderCondition().isPresent() && !sky.renderCondition().get().isSkyRendered(DimensionRenderer.getServerLevel())) continue;
                return toPackedColor(sky);
            }
        }
        return OptionalInt.empty();
    }

    private static OptionalInt toPackedColor(SkyProperties sky) {
        return sky.cloudSettings()
                .flatMap(cs -> cs.cloudColor().map(rgb ->
                        OptionalInt.of((0xFF << 24) | (rgb.x << 16) | (rgb.y << 8) | rgb.z)))
                .orElse(OptionalInt.empty());
    }

    public static boolean isAModCancelRendering(String[] modIds) {
        for(String modId : modIds) {
            if (PlatformHelper.isModLoaded(modId)) {
                return true;
            }
        }
        return false;
    }
}