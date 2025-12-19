package fr.tathan.sky_aesthetics.client.skies.settings;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Util;
import org.joml.Vector2f;
import org.joml.Vector3i;

import java.util.List;
import java.util.Optional;

/**
 * The class containing information about fog settings
 * @param fog Whether fog is enabled
 * @param customFogColor The custom RGB color of the fog
 * @param fogDensity The density settings of the fog (near, far)
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

    public FogParameters setCustomFog(FogParameters fog) {
        if(this.customFogColor().isPresent()) {
            Vector3i color = this.customFogColor().get();
            fog = new FogParameters(fog.start(), fog.end(), fog.shape(), color.x, color.y, color.z, fog.alpha());
        }
        if (this.fogDensity().isPresent()) {
            Vector2f density = this.fogDensity().get();
            fog = new FogParameters(density.x, density.y, fog.shape(), fog.red(), fog.green(), fog.blue(), fog.alpha());
        }
        return fog;
    }

    public void runFogCallback(FogParameters fog) {
        if(this.fog) {
            RenderSystem.setShaderFog(fog);
        }
    }
}
