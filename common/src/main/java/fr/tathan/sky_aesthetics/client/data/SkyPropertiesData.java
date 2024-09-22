package fr.tathan.sky_aesthetics.client.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.PlanetSky;
import fr.tathan.sky_aesthetics.client.skies.record.SkyProperties;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class SkyPropertiesData extends SimpleJsonResourceReloadListener  {

    public static final Map<ResourceLocation, PlanetSky> SKY_PROPERTIES = new HashMap<>();

    public SkyPropertiesData() {
        super(SkyAesthetics.GSON, "sky");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        object.forEach((key, value) -> {
            JsonObject json = GsonHelper.convertToJsonObject(value, "sky_renderer");
            SkyProperties skyProperties = SkyProperties.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
            PlanetSky planetSky = new PlanetSky(skyProperties);

            if(skyProperties.id().isPresent()) {
                SKY_PROPERTIES.putIfAbsent(skyProperties.id().get(), planetSky);
                SkyAesthetics.LOG.info(skyProperties.id().get() + " | registered");

            } else {
                SKY_PROPERTIES.putIfAbsent(skyProperties.world().location(), planetSky);
                SkyAesthetics.LOG.info(skyProperties.world().location() + " | registered");

            }
        });

    }
}
