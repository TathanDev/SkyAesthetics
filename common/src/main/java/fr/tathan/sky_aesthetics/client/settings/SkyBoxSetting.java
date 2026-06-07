package fr.tathan.sky_aesthetics.client.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

import java.util.Optional;

public record SkyBoxSetting(int gradation, Identifier texture, Vector3f rotation, Optional<Rotation> dynamicRotation) {

    public static final Codec<SkyBoxSetting> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("gradation").forGetter(SkyBoxSetting::gradation),
            Identifier.CODEC.fieldOf("texture").forGetter(SkyBoxSetting::texture),
            SkyObject.VEC3F.fieldOf("rotation").forGetter(SkyBoxSetting::rotation),
            Rotation.CODEC.optionalFieldOf("dynamic_rotation").forGetter(SkyBoxSetting::dynamicRotation)
    ).apply(instance, SkyBoxSetting::new));
}
