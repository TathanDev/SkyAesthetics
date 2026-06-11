package fr.tathan.sky_aesthetics.client.utils;

import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.DimensionRenderer;
import fr.tathan.sky_aesthetics.client.data.SkiesRegistry;
import fr.tathan.sky_aesthetics.client.settings.SkyProperties;
import fr.tathan.sky_aesthetics.helper.PlatformHelper;
import net.minecraft.world.level.Level;

import java.util.OptionalInt;
import java.util.function.Consumer;

public class SkyHelper {

    public static boolean canRenderSky(Level level, Consumer<SkyProperties> action) {
        // The editor's live-preview sky always wins for its dimension (config disables and render
        // conditions are intentionally bypassed so you see exactly what you are editing).
        SkyProperties preview = SkiesRegistry.getPreviewSky();
        if (preview != null && preview.world().equals(level.dimension())) {
            action.accept(preview);
            return true;
        }
        for (SkyProperties sky : SkiesRegistry.SKY_PROPERTIES.values()) {
            if (sky.world().equals(level.dimension())) {

                if (containsString(SkyAesthetics.CONFIG.disabledSkies, sky.id().toString())) return false;
                if (containsString(SkyAesthetics.CONFIG.disabledDimensions, sky.world().identifier().toString())) return false;


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
        SkyProperties preview = SkiesRegistry.getPreviewSky();
        if (preview != null && preview.world().equals(level.dimension())) {
            return preview.cloudHeight();
        }
        for (SkyProperties sky : SkiesRegistry.SKY_PROPERTIES.values()) {
            if (sky.world().equals(level.dimension())) {
                if (containsString(SkyAesthetics.CONFIG.disabledSkies, sky.id().toString())) continue;
                if (containsString(SkyAesthetics.CONFIG.disabledDimensions, sky.world().identifier().toString())) continue;
                if (sky.renderCondition().isPresent() && !sky.renderCondition().get().isSkyRendered(DimensionRenderer.getServerLevel())) continue;
                return sky.cloudHeight();
            }
        }
        return OptionalInt.empty();
    }

    public static OptionalInt getActiveCloudColor(Level level) {
        SkyProperties preview = SkiesRegistry.getPreviewSky();
        if (preview != null && preview.world().equals(level.dimension())) {
            return toPackedColor(preview);
        }
        for (SkyProperties sky : SkiesRegistry.SKY_PROPERTIES.values()) {
            if (sky.world().equals(level.dimension())) {
                if (containsString(SkyAesthetics.CONFIG.disabledSkies, sky.id().toString())) continue;
                if (containsString(SkyAesthetics.CONFIG.disabledDimensions, sky.world().identifier().toString())) continue;
                if (sky.renderCondition().isPresent() && !sky.renderCondition().get().isSkyRendered(DimensionRenderer.getServerLevel())) continue;
                return toPackedColor(sky);
            }
        }
        return OptionalInt.empty();
    }

    private static OptionalInt toPackedColor(SkyProperties sky) {
        return sky.cloudSettings()
                .flatMap(cs -> cs.cloudColor().map(rgb ->
                        OptionalInt.of((0xFF << 24) | ((rgb.x & 0xFF) << 16) | ((rgb.y & 0xFF) << 8) | (rgb.z & 0xFF))))
                .orElse(OptionalInt.empty());
    }

    public static boolean isAModCancelRendering(String[] modIds) {
        for (String modId : modIds) {
            if (PlatformHelper.isModLoaded(modId)) return true;
        }
        return false;
    }

    private static boolean containsString(String[] arr, String value) {
        for (String s : arr) if (s.equals(value)) return true;
        return false;
    }
}