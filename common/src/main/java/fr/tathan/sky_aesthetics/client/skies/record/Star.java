package fr.tathan.sky_aesthetics.client.skies.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public record Star(boolean vanilla, boolean movingStars, int count, boolean allDaysVisible, float scale, Vec3 color, Optional<ShootingStars> shootingStars, Optional<ResourceLocation> stars_texture) {

    public static final Codec<Star> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("vanilla").forGetter(Star::vanilla),
            Codec.BOOL.fieldOf("moving_stars").forGetter(Star::movingStars),
            Codec.INT.fieldOf("count").forGetter(Star::count),
            Codec.BOOL.fieldOf("all_days_visible").forGetter(Star::allDaysVisible),
            Codec.FLOAT.fieldOf("scale").forGetter(Star::scale),
            Vec3.CODEC.fieldOf("color").forGetter(Star::color),
            ShootingStars.CODEC.optionalFieldOf("shooting_stars").forGetter(Star::shootingStars),
            ResourceLocation.CODEC.optionalFieldOf("stars_texture").forGetter(Star::stars_texture)
    ).apply(instance, Star::new));

    public record ShootingStars(int percentage, Vec2 randomLifetime, float scale, float speed, Vec3 color, Optional<Integer> rotation) {

        public static Codec<Vec2> VEC2 = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 2).map((listx) -> new Vec2(listx.getFirst(), listx.get(1))), (vec2) -> List.of(vec2.x, vec2.y));

        public static final Codec<ShootingStars> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("percentage").forGetter(ShootingStars::percentage),
                VEC2.fieldOf("random_lifetime").forGetter(ShootingStars::randomLifetime),
                Codec.FLOAT.fieldOf("scale").forGetter(ShootingStars::scale),
                Codec.FLOAT.fieldOf("speed").forGetter(ShootingStars::speed),
                Vec3.CODEC.fieldOf("color").forGetter(ShootingStars::color),
                Codec.INT.optionalFieldOf("rotation").forGetter(ShootingStars::rotation)
        ).apply(instance, ShootingStars::new));

    }

}
