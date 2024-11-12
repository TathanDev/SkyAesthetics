package fr.tathan.sky_aesthetics.client.skies.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.world.phys.Vec2;

import java.util.List;
import java.util.Optional;

public record Star(boolean vanilla, boolean movingStars, int count, boolean allDaysVisible, float scale, Color color, Optional<ShootingStars> shootingStars) {
    public static final Codec<Star> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("vanilla").forGetter(Star::vanilla),
            Codec.BOOL.fieldOf("moving_stars").forGetter(Star::movingStars),
            Codec.INT.fieldOf("count").forGetter(Star::count),
            Codec.BOOL.fieldOf("all_days_visible").forGetter(Star::allDaysVisible),
            Codec.FLOAT.fieldOf("scale").forGetter(Star::scale),
            Color.CODEC.fieldOf("color").forGetter(Star::color),
            ShootingStars.CODEC.optionalFieldOf("shooting_stars").forGetter(Star::shootingStars)

    ).apply(instance, Star::new));

    public record Color(int r, int g, int b) {
        public static final Codec<Color> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("r").forGetter(Color::r),
                Codec.INT.fieldOf("g").forGetter(Color::g),
                Codec.INT.fieldOf("b").forGetter(Color::b)
        ).apply(instance, Color::new));

    }

    public record ShootingStars(int percentage, Vec2 randomLifetime, float scale, float speed, Color color, Optional<Integer> rotation) {

        public static Codec<Vec2> VEC2 = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 2).map((listx) -> new Vec2(listx.getFirst(), listx.get(1))), (vec2) -> List.of(vec2.x, vec2.y));


        public static final Codec<ShootingStars> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("percentage").forGetter(ShootingStars::percentage),
                VEC2.fieldOf("random_lifetime").forGetter(ShootingStars::randomLifetime),
                Codec.FLOAT.fieldOf("scale").forGetter(ShootingStars::scale),
                Codec.FLOAT.fieldOf("speed").forGetter(ShootingStars::speed),
                Color.CODEC.fieldOf("color").forGetter(ShootingStars::color),
                Codec.INT.optionalFieldOf("rotation").forGetter(ShootingStars::rotation)
        ).apply(instance, ShootingStars::new));

    }

}
