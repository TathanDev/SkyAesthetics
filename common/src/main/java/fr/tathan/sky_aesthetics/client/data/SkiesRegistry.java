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

import java.util.HashMap;
import java.util.Map;

/**
 * The registry handling the loading of custom skies from data packs
 */
public class SkiesRegistry extends SimpleJsonResourceReloadListener<@NotNull SkyProperties>  {

    public static final Map<Identifier, SkyProperties> SKY_PROPERTIES = new HashMap<>();
    private static final Map<Identifier, DimensionRenderer> RENDERER_CACHE = new HashMap<>();

    /**
     * The default sky used in development, it is not registered in the registry.
     * It is used to test the sky aesthetics without having to load a custom sky.
     */
    public static SkyProperties SKY_DEV = null;
    public static Boolean USE_SKY_DEV = false;


    public SkiesRegistry() {
        super(SkyProperties.CODEC, FileToIdConverter.json("sky_aesthetics"));
    }

    @Override
    protected void apply(Map<Identifier, SkyProperties> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        RENDERER_CACHE.forEach((id, renderer) -> renderer.close());
        RENDERER_CACHE.clear();
        SKY_PROPERTIES.clear();
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

    public static void setSkyDev(SkyProperties sky) {
        if(SKY_DEV != null) {
            SkyAesthetics.LOG.warn("Sky dev already set, overwriting it");
        }
        SKY_DEV = sky;
    }
}
