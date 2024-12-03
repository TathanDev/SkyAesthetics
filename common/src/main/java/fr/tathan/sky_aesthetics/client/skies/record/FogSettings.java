package fr.tathan.sky_aesthetics.client.skies.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public record FogSettings(Boolean fog, Optional<Vector3f> customFogColor, Optional<Vector2f> fogDensity) {

    public static Codec<Vector2f> VEC2F = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 2).map((listx) -> new Vector2f(listx.getFirst(), listx.getLast())), (vector2f) -> List.of(vector2f.x, vector2f.y));

    public static final Codec<FogSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("fog").forGetter(FogSettings::fog),
            SkyObject.VEC3F.optionalFieldOf("fog_color").forGetter(FogSettings::customFogColor),
            VEC2F.optionalFieldOf("fog_density").forGetter(FogSettings::fogDensity)
    ).apply(instance, FogSettings::new));

}
