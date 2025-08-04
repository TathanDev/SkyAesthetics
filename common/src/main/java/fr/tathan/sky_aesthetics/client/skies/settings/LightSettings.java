package fr.tathan.sky_aesthetics.client.skies.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
