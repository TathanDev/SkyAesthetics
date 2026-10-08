package fr.tathan.sky_aesthetics.client.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Util;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.List;
import java.util.Optional;

/**
 * The class containing information about sky colors
 * @param color The RGB color of the sky
 * @param sunsetColor The RGB color of the sunset
 * @param sunriseAlphaModifier The alpha modifier for the sunrise/sunset
 */
public record SkyColorSettings(Optional<Vector3f> color,
                               Optional<Vector3f> nightColor,
                               Optional<Vector3i> sunsetColor,
                               Optional<Integer> sunriseAlphaModifier
) {

    public SkyColorSettings(Optional<Vector3f> color,
                            Optional<Vector3i> sunsetColor,
                            Optional<Integer> sunriseAlphaModifier) {
        this(color, Optional.empty(), sunsetColor, sunriseAlphaModifier);
    }

    public static Codec<Vector3f> SKY_COLOR = Codec.FLOAT.listOf().comapFlatMap(
            (list) -> list.size() == 4
                    ? DataResult.success(new Vector3f(list.getFirst(), list.get(1), list.get(2)))
                    : Util.fixedSize(list, 3).map((listx) -> new Vector3f(listx.getFirst(), listx.get(1), listx.getLast())),
            (vector3f) -> List.of(vector3f.x, vector3f.y, vector3f.z));
    public static Codec<Vector3i> VEC3I = Codec.INT.listOf()
            .comapFlatMap((list) -> Util.fixedSize(list, 3)
                            .map((listx) -> new Vector3i(listx.getFirst(), listx.get(1), listx.getLast())),
                    (vec3) -> List.of(vec3.x, vec3.y, vec3.z));

    public static final Codec<SkyColorSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SKY_COLOR.optionalFieldOf("sky_color").forGetter(SkyColorSettings::color),
            SKY_COLOR.optionalFieldOf("night_sky_color").forGetter(SkyColorSettings::nightColor),
            VEC3I.optionalFieldOf("sunset_color").forGetter(SkyColorSettings::sunsetColor),
            Codec.INT.optionalFieldOf("sunset_alpha_modifier").forGetter(SkyColorSettings::sunriseAlphaModifier)
    ).apply(instance, SkyColorSettings::new));

    public static SkyColorSettings createDefaultSettings() {
        return new SkyColorSettings(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(1));
    }
}
