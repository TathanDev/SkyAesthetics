package fr.tathan.sky_aesthetics.client.data;

import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.DimensionRenderer;
import fr.tathan.sky_aesthetics.client.settings.SkyProperties;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The registry handling the loading of custom skies from data packs
 */
public class SkiesRegistry extends SimpleJsonResourceReloadListener<@NotNull SkyProperties>  {

    public static final Map<Identifier, SkyProperties> SKY_PROPERTIES = new ConcurrentHashMap<>();
    private static final Map<Identifier, DimensionRenderer> RENDERER_CACHE = new ConcurrentHashMap<>();

    /**
     * A live-preview sky pushed from the in-game editor. When set it takes priority over the
     * loaded skies for its dimension (see {@link fr.tathan.sky_aesthetics.client.utils.SkyHelper}).
     */
    private static volatile SkyProperties previewSky;


    public SkiesRegistry() {
        super(SkyProperties.CODEC, FileToIdConverter.json("sky_aesthetics"));
    }

    @Override
    protected void apply(Map<Identifier, SkyProperties> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        RENDERER_CACHE.forEach((id, renderer) -> renderer.close());
        RENDERER_CACHE.clear();
        SKY_PROPERTIES.clear();
        previewSky = null;
        SkyAesthetics.LOG.info("Registering skies...");
        object.forEach((key, skyProperties) -> {

            registerSky(skyProperties.id(), skyProperties);
            SkyAesthetics.LOG.info("{} | registered", skyProperties.id());

        });

    }

    /**
     * Returns a cached DimensionRenderer for the given sky, building it on first access.
     * Must be called on the render thread.
     */
    public static DimensionRenderer getOrBuildRenderer(SkyProperties sky) {
        return RENDERER_CACHE.computeIfAbsent(sky.id(), id -> sky.toDimensionRenderer());
    }

    /** The current live-preview sky, or {@code null} when preview is inactive. */
    public static SkyProperties getPreviewSky() {
        return previewSky;
    }

    /**
     * Sets (or replaces) the live-preview sky. Drops any cached renderer for its id so the edited
     * version is rebuilt on the next frame.
     */
    public static void setPreviewSky(SkyProperties sky) {
        if (previewSky != null) invalidate(previewSky.id()); // close renderer for the old preview id
        invalidate(sky.id()); // force rebuild for the new sky
        previewSky = sky;
    }

    /** Clears the live-preview sky and drops its cached renderer. */
    public static void clearPreviewSky() {
        if (previewSky != null) {
            invalidate(previewSky.id());
            previewSky = null;
        }
    }

    private static void invalidate(Identifier id) {
        DimensionRenderer old = RENDERER_CACHE.remove(id);
        if (old != null) old.close();
    }

    /**
     * Allow to register sky.
     * Can be used to register skies from code/at runtime.
     * @param id the id of the sky to register
     * @param sky the sky to register
     */
    public static void registerSky(Identifier id, SkyProperties sky) {
        if(SKY_PROPERTIES.containsKey(id)) {
            SkyAesthetics.LOG.warn("Sky with id {} already exists, overwriting it", id);
        }
        SKY_PROPERTIES.put(id, sky);
    }
}
