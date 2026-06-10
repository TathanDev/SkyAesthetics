package fr.tathan.sky_aesthetics.client.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector3i;

import java.util.Optional;

public record CloudSettings(boolean showCloud, Integer cloudHeight, Optional<Vector3i> cloudColor) {

    public static final Codec<CloudSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("cloud").forGetter(CloudSettings::showCloud),
            Codec.INT.optionalFieldOf("cloud_height", 192).forGetter(CloudSettings::cloudHeight),
            SkyColorSettings.VEC3I.optionalFieldOf("cloud_color").forGetter(CloudSettings::cloudColor)
    ).apply(instance, CloudSettings::new));

}
