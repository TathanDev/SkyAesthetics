package fr.tathan.sky_aesthetics.client.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Util;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.List;
import java.util.Optional;

/**
 * The class containing information about sky colors
 * @param color The RGBA color of the sky
 * @param sunsetColor The RGB color of the sunset
 * @param sunriseAlphaModifier The alpha modifier for the sunrise/sunset
 */
public record SkyColorSettings(Optional<Vector4f> color,
                               Optional<Vector3i> sunsetColor,
                               Optional<Integer> sunriseAlphaModifier
) {

    public static Codec<Vector4f> VEC4F = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 4).map((listx) -> new Vector4f(listx.getFirst(), listx.get(1), listx.get(2), listx.getLast())), (vector4f) -> List.of(vector4f.x, vector4f.y, vector4f.z, vector4f.w));
    public static Codec<Vector3i> VEC3I = Codec.INT.listOf()
            .comapFlatMap((list) -> Util.fixedSize(list, 3)
                            .map((listx) -> new Vector3i(listx.getFirst(), listx.get(1), listx.getLast())),
                    (vec3) -> List.of(vec3.x, vec3.y, vec3.z));

    public static final Codec<SkyColorSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VEC4F.optionalFieldOf("sky_color").forGetter(SkyColorSettings::color),
            VEC3I.optionalFieldOf("sunset_color").forGetter(SkyColorSettings::sunsetColor),
            Codec.INT.optionalFieldOf("sunset_alpha_modifier").forGetter(SkyColorSettings::sunriseAlphaModifier)
    ).apply(instance, SkyColorSettings::new));

    public static SkyColorSettings createDefaultSettings() {
        return new SkyColorSettings(Optional.empty(), Optional.empty(), Optional.of(1));
    }
}
