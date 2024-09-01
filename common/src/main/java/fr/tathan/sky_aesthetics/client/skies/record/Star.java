package fr.tathan.sky_aesthetics.client.skies.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Star(boolean vanilla, boolean movingStars, int count, boolean allDaysVisible, float scale, Color color) {
    public static final Codec<Star> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("vanilla").forGetter(Star::vanilla),
            Codec.BOOL.fieldOf("moving_stars").forGetter(Star::movingStars),
            Codec.INT.fieldOf("count").forGetter(Star::count),
            Codec.BOOL.fieldOf("all_days_visible").forGetter(Star::allDaysVisible),
            Codec.FLOAT.fieldOf("scale").forGetter(Star::scale),
            Color.CODEC.fieldOf("color").forGetter(Star::color)
    ).apply(instance, Star::new));

    public record Color(int r, int g, int b) {
        public static final Codec<Color> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("r").forGetter(Color::r),
                Codec.INT.fieldOf("g").forGetter(Color::g),
                Codec.INT.fieldOf("b").forGetter(Color::b)
        ).apply(instance, Color::new));

    }

}
