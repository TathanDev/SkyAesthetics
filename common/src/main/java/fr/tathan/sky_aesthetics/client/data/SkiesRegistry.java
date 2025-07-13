package fr.tathan.sky_aesthetics.client.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.DimensionSky;
import fr.tathan.sky_aesthetics.client.skies.settings.SkyProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SkiesRegistry extends SimpleJsonResourceReloadListener  {

    public static final Map<ResourceLocation, DimensionSky> SKY_PROPERTIES = new HashMap<>();

    /**
     * The default sky used in development, it is not registered in the registry.
     * It is used to test the sky aesthetics without having to load a custom sky.
     */
    public static DimensionSky SKY_DEV = null;

    public SkiesRegistry() {
        super(SkyAesthetics.GSON, "sky_aesthetics");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, @Nullable ResourceManager resourceManager, @Nullable ProfilerFiller profiler) {
        SKY_PROPERTIES.clear();
        object.forEach((key, value) -> {
            JsonObject json = GsonHelper.convertToJsonObject(value, "sky properties");
            DataResult<SkyProperties> decoder = SkyProperties.CODEC.parse(JsonOps.INSTANCE, json);

            if(decoder.error().isPresent()) {
                SkyAesthetics.LOG.error("Error parsing sky : {}", decoder.error().get().message());
                return;
            }

            SkyProperties skyProperties = decoder.getOrThrow();
            DimensionSky dimensionSky = new DimensionSky(skyProperties.world(), skyProperties.id(), skyProperties.toDimensionRenderer());

            registerSky(skyProperties.id(), dimensionSky);
            SkyAesthetics.LOG.info("{} | registered", skyProperties.id());

        });
    }

    public static void registerSky(ResourceLocation id, DimensionSky sky) {
        if(SKY_PROPERTIES.containsKey(id)) {
            SkyAesthetics.LOG.warn("Sky with id {} already exists, overwriting it", id);
        }
        SKY_PROPERTIES.put(id, sky);
    }

    public static void setSkyDev(DimensionSky sky) {
        if(SKY_DEV != null) {
            SkyAesthetics.LOG.warn("Sky dev already set, overwriting it");
        }
        SKY_DEV = sky;
    }
}
