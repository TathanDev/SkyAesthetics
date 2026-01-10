package fr.tathan.sky_aesthetics.client.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Settings related to lighting in the sky.
 *
 * @param forceBrightLightmap   If true, forces a bright lightmap regardless of time of day.
 * @param constantAmbientLight  If true, maintains constant ambient light levels.
 */
public record LightSettings(
        boolean forceBrightLightmap,
        boolean constantAmbientLight) {

    public static final Codec<LightSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("forceBrightLightmap").forGetter(LightSettings::forceBrightLightmap),
            Codec.BOOL.fieldOf("constantAmbientLight").forGetter(LightSettings::constantAmbientLight)
    ).apply(instance, LightSettings::new));

    public static LightSettings createDefaultSettings() {
        return new LightSettings(false, false);
    }

}
