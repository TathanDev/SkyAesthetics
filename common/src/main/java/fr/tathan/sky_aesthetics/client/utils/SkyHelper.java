package fr.tathan.sky_aesthetics.client.utils;

import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.data.SkiesRegistry;
import fr.tathan.sky_aesthetics.client.settings.SkyProperties;
import fr.tathan.sky_aesthetics.helper.PlatformHelper;
import net.minecraft.world.level.Level;

import java.util.OptionalInt;
import java.util.function.Consumer;

public class SkyHelper {

    public static boolean canRenderSky(Level level, Consumer<SkyProperties> action) {
        SkyProperties sky = selectSky(level);
        if (sky == null) return false;
        action.accept(sky);
        return true;
    }

    public static OptionalInt getActiveCloudHeight(Level level) {
        if (cloudsHandledExternally()) return OptionalInt.empty();
        SkyProperties sky = selectSky(level);
        return sky != null ? sky.cloudHeight() : OptionalInt.empty();
    }

    public static OptionalInt getActiveCloudColor(Level level) {
        if (cloudsHandledExternally()) return OptionalInt.empty();
        SkyProperties sky = selectSky(level);
        return sky != null ? toPackedColor(sky) : OptionalInt.empty();
    }

    /**
     * Resolves the sky that should be active for {@code level}, shared by every dispatch method so
     * they always agree. Honors (1) the editor live-preview sky (which intentionally bypasses config
     * disables and render conditions so you see exactly what you are editing), then (2) config
     * disables, then (3) each sky's render condition. When several skies target the same dimension,
     * the one with the lowest id wins so selection is deterministic across reloads/JVM runs.
     *
     * @return the chosen sky, or {@code null} when none applies.
     */
    private static SkyProperties selectSky(Level level) {
        SkyProperties preview = SkiesRegistry.getPreviewSky();
        if (preview != null && preview.world().equals(level.dimension())) {
            return preview;
        }
        SkyProperties best = null;
        for (SkyProperties sky : SkiesRegistry.SKY_PROPERTIES.values()) {
            if (!sky.world().equals(level.dimension())) continue;
            if (containsString(SkyAesthetics.CONFIG.disabledSkies, sky.id().toString())) continue;
            if (containsString(SkyAesthetics.CONFIG.disabledDimensions, sky.world().identifier().toString())) continue;
            if (sky.renderCondition().isPresent() && !sky.renderCondition().get().isSkyRendered()) continue;
            if (best == null || sky.id().toString().compareTo(best.id().toString()) < 0) {
                best = sky;
            }
        }
        return best;
    }

    /**
     * True when the user has opted out of Sky Aesthetics touching clouds (config {@code
     * disableCustomCloud}) or another cloud-handling mod is present. Cloud recolor/reheight must honor
     * this, matching the cloud-cancel path in the LevelRenderer mixins.
     */
    private static boolean cloudsHandledExternally() {
        return SkyAesthetics.CONFIG.disableCustomCloud
                || isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingCloudRender);
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