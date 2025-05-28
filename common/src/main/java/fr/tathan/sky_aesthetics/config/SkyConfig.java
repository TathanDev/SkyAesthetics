package fr.tathan.sky_aesthetics.config;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record SkyConfig(List<String> disabledSkies) {

    public static final Codec<SkyConfig> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.STRING.listOf().fieldOf("disabledSkies").forGetter(SkyConfig::disabledSkies)
    ).apply(instance, SkyConfig::new));

    public JsonElement toJson() {
        return CODEC
                .encodeStart(JsonOps.INSTANCE, this)
                .result()
                .orElseThrow(() -> new IllegalStateException("Failed to encode to JSON"));
    }

    public String[] modDisablingWeather = new String[]{
            "bad_weather_mod",
    };


}
