package fr.tathan.sky_aesthetics.client.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.util.Util;
import org.joml.Vector2f;
import org.joml.Vector3i;

import java.util.List;
import java.util.Optional;

/**
 * The class containing information about fog settings
 * @param fog Whether fog is enabled
 * @param customFogColor The custom RGB color of the fog (0-255 per channel)
 * @param fogDensity The density settings of the fog (near distance, far distance)
 */
public record FogSettings(Boolean fog, Optional<Vector3i> customFogColor, Optional<Vector2f> fogDensity) {

    public static Codec<Vector2f> VEC2F = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 2).map((listx) -> new Vector2f(listx.getFirst(), listx.getLast())), (vector2f) -> List.of(vector2f.x, vector2f.y));

    public static final Codec<FogSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("fog").forGetter(FogSettings::fog),
            SkyColorSettings.VEC3I.optionalFieldOf("fog_color").forGetter(FogSettings::customFogColor),
            VEC2F.optionalFieldOf("fog_density").forGetter(FogSettings::fogDensity)
    ).apply(instance, FogSettings::new));


    public static FogSettings createDefaultSettings() {
        return new FogSettings(true, Optional.empty(), Optional.empty());
    }

    public void apply(FogData data) {
        if (!this.fog) {
            data.environmentalStart = Float.MAX_VALUE;
            data.environmentalEnd = Float.MAX_VALUE;
            data.skyEnd = Float.MAX_VALUE;
            data.cloudEnd = Float.MAX_VALUE;
            return;
        }

        this.customFogColor.ifPresent(color ->
                data.color.set(color.x / 255f, color.y / 255f, color.z / 255f, data.color.w));

        this.fogDensity.ifPresent(density -> {
            data.environmentalStart = density.x;
            data.environmentalEnd = density.y;
            data.skyEnd = density.y;
            data.cloudEnd = density.y;
        });
    }
}
