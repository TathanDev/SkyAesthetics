package fr.tathan.sky_aesthetics.client.skies.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record Constellation(
         String id, float scale, Vec3 color, Vec3 firstPoint, List<Vec3> points
) {

    public static final Codec<Constellation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(Constellation::id),
            Codec.FLOAT.fieldOf("scale").forGetter(Constellation::scale),
            Vec3.CODEC.fieldOf("color").forGetter(Constellation::color),
            Vec3.CODEC.fieldOf("firstPoint").forGetter(Constellation::firstPoint),
            Vec3.CODEC.listOf().fieldOf("points").forGetter(Constellation::points)
    ).apply(instance, Constellation::new));

}
