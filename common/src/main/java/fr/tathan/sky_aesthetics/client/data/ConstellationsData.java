package fr.tathan.sky_aesthetics.client.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.record.Constellation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class ConstellationsData extends SimpleJsonResourceReloadListener {

    public static final Map<String, Constellation> CONSTELLATIONS = new HashMap<>();

    public ConstellationsData() {
        super(SkyAesthetics.GSON, "constellation");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        CONSTELLATIONS.clear();
        object.forEach((key, value) -> {
            JsonObject json = GsonHelper.convertToJsonObject(value, "constellation");
            Constellation constellation = Constellation.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
            SkyAesthetics.LOG.info(constellation.id() + " | registered");

            CONSTELLATIONS.putIfAbsent(constellation.id(), constellation);
        });

    }
}
