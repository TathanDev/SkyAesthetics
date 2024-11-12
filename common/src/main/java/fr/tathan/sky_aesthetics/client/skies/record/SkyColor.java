package fr.tathan.sky_aesthetics.client.skies.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SkyColor(boolean customColor, int color) {
    public static final Codec<SkyColor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("custom_color").forGetter(SkyColor::customColor),
            Codec.INT.fieldOf("color").forGetter(SkyColor::color)
    ).apply(instance, SkyColor::new));
}
