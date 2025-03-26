package fr.tathan.sky_aesthetics.client.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.DimensionSky;
import fr.tathan.sky_aesthetics.client.skies.record.SkyProperties;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class SkyPropertiesData extends SimpleJsonResourceReloadListener<JsonElement>  {

    public static final Map<ResourceLocation, DimensionSky> SKY_PROPERTIES = new HashMap<>();

    public SkyPropertiesData() {
        super(ExtraCodecs.JSON, FileToIdConverter.json("sky_aesthetics"));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        SKY_PROPERTIES.clear();
        object.forEach((key, value) -> {
            JsonObject json = GsonHelper.convertToJsonObject(value, "sky_renderer");
            DataResult<SkyProperties> decoder = SkyProperties.CODEC.parse(JsonOps.INSTANCE, json);

            if(decoder.error().isPresent()) {
                SkyAesthetics.LOG.error("Error parsing sky : {}", decoder.error().get().message());
                return;
            }
            SkyProperties skyProperties = decoder.getOrThrow();
            DimensionSky planetSky = new DimensionSky(skyProperties);

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
