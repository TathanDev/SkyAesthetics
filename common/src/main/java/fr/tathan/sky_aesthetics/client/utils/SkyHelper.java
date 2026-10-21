package fr.tathan.sky_aesthetics.client.utils;

import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.DimensionRenderer;
import fr.tathan.sky_aesthetics.client.data.SkiesRegistry;
import fr.tathan.sky_aesthetics.client.settings.SkyProperties;
import fr.tathan.sky_aesthetics.helper.PlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.Level;

import java.lang.ref.WeakReference;
import java.util.OptionalInt;
import java.util.function.Consumer;

public class SkyHelper {

    // Per-tick cache for selectSky(level, true); the level is weakly held so it can be collected after leaving.
    private static WeakReference<Level> cachedLevel = new WeakReference<>(null);
    private static long cachedGameTime;
    private static int cachedVersion;
    private static String[] cachedDisabledSkies;
    private static String[] cachedDisabledDimensions;
    private static SkyProperties cachedSky;

    private static SkyProperties previousSky;
    private static long switchGameTime;

    /** When the sky just changed and has transition_ticks, the skybox layers of the previous sky fade out */
    public static void renderSky(Level level, SkyProperties sky, SkyRenderState state, SkyRenderer skyRenderer) {
        DimensionRenderer renderer = SkiesRegistry.getOrBuildRenderer(sky);
        DimensionRenderer outgoing = null;
        float outgoingAlpha = 0.0f;

        SkyProperties previous = previousSky;
        if (previous != null && sky.transitionTicks() > 0 && SkiesRegistry.getPreviewSky() == null) {
            float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
            double elapsed = level.getGameTime() - switchGameTime + partialTick;
            if (elapsed < sky.transitionTicks()) {
                outgoing = SkiesRegistry.getOrBuildRenderer(previous);
                outgoingAlpha = (float) (1.0 - elapsed / sky.transitionTicks());
            } else {
                previousSky = null;
            }
        }
        renderer.render(state, skyRenderer, outgoing, outgoingAlpha);
    }

    private static boolean sameSky(SkyProperties a, SkyProperties b) {
        if (a == null || b == null) return a == b;
        return a.id().equals(b.id());
    }

    public static boolean canRenderSky(Level level, Consumer<SkyProperties> action) {
        SkyProperties sky = selectSky(level, true);
        if (sky == null) return false;
        action.accept(sky);
        return true;
    }

    /**
     * Like {@link #canRenderSky} but ignores {@link SkyProperties.RenderCondition}. The vanilla
     * environment-attribute system is baked once, inside the level constructor, so the player does not
     * exist yet and {@code RenderCondition.isSkyRendered()} would always answer {@code false} — which
     * silently dropped {@code environment_attributes} for every sky that declares a {@code condition}.
     * A per-tick biome/height condition could not be expressed in a layer baked once per level anyway.
     */
    public static boolean canApplyAttributes(Level level, Consumer<SkyProperties> action) {
        SkyProperties sky = selectSky(level, false);
        if (sky == null) return false;
        action.accept(sky);
        return true;
    }

    public static OptionalInt getActiveCloudHeight(Level level) {
        if (cloudsHandledExternally()) return OptionalInt.empty();
        SkyProperties sky = selectSky(level, true);
        return sky != null ? sky.cloudHeight() : OptionalInt.empty();
    }

    public static OptionalInt getActiveCloudColor(Level level) {
        if (cloudsHandledExternally()) return OptionalInt.empty();
        SkyProperties sky = selectSky(level, true);
        return sky != null ? toPackedColor(sky) : OptionalInt.empty();
    }

    /**
     * Resolves the sky that should be active for {@code level}, shared by every dispatch method so
     * they always agree. Honors (1) the editor live-preview sky (which intentionally bypasses config
     * disables and render conditions so you see exactly what you are editing), then (2) config
     * disables, then (3) each sky's render condition. When several skies target the same dimension,
     * the one with the lowest id wins so selection is deterministic across reloads/JVM runs.
     *
     * @param honorRenderCondition {@code false} for callers that run before the player exists; see
     *                             {@link #canApplyAttributes}.
     * @return the chosen sky, or {@code null} when none applies.
     */
    private static SkyProperties selectSky(Level level, boolean honorRenderCondition) {
        if (!honorRenderCondition) return computeSky(level, false);

        // Called several times per frame (sky, lightmap, weather, clouds, fog). The answer only changes
        // when the player moves (once per tick), the skies/preview change, or the disable lists change.
        long gameTime = level.getGameTime();
        int version = SkiesRegistry.getVersion();
        String[] disabledSkies = SkyAesthetics.CONFIG.disabledSkies;
        String[] disabledDimensions = SkyAesthetics.CONFIG.disabledDimensions;
        if (level != cachedLevel.get() || gameTime != cachedGameTime || version != cachedVersion
                || disabledSkies != cachedDisabledSkies || disabledDimensions != cachedDisabledDimensions) {
            SkyProperties next = computeSky(level, true);
            if (level != cachedLevel.get() || version != cachedVersion) {
                previousSky = null;
            } else if (!sameSky(next, cachedSky)) {
                previousSky = cachedSky;
                switchGameTime = gameTime;
            }
            cachedSky = next;
            cachedLevel = new WeakReference<>(level);
            cachedGameTime = gameTime;
            cachedVersion = version;
            cachedDisabledSkies = disabledSkies;
            cachedDisabledDimensions = disabledDimensions;
        }
        return cachedSky;
    }

    private static SkyProperties computeSky(Level level, boolean honorRenderCondition) {
        SkyProperties preview = SkiesRegistry.getPreviewSky();
        if (preview != null && preview.world().equals(level.dimension())) {
            return preview;
        }
        SkyProperties best = null;
        for (SkyProperties sky : SkiesRegistry.SKY_PROPERTIES.values()) {
            if (!sky.world().equals(level.dimension())) continue;
            if (containsString(SkyAesthetics.CONFIG.disabledSkies, sky.id().toString())) continue;
            if (containsString(SkyAesthetics.CONFIG.disabledDimensions, sky.world().identifier().toString())) continue;
            if (honorRenderCondition && sky.renderCondition().isPresent() && !sky.renderCondition().get().isSkyRendered()) continue;
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