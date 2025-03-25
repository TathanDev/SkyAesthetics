package fr.tathan.sky_aesthetics.client.skies.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record CustomVanillaObject(
        boolean sun, Optional<ResourceLocation> sunTexture, Optional<Float> sunHeight, Optional<Float> sunSize,
        boolean moon, boolean moonPhase, Optional<ResourceLocation> moonTexture, Optional<Float> moonHeight, Optional<Float> moonSize
) {

    public static final Codec<CustomVanillaObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("sun").forGetter(CustomVanillaObject::sun),
            ResourceLocation.CODEC.optionalFieldOf("sun_texture").forGetter(CustomVanillaObject::sunTexture),
            Codec.FLOAT.optionalFieldOf("sun_height").forGetter(CustomVanillaObject::sunHeight),
            Codec.FLOAT.optionalFieldOf("sun_size").forGetter(CustomVanillaObject::sunSize),
            Codec.BOOL.fieldOf("moon").forGetter(CustomVanillaObject::moon),
            Codec.BOOL.fieldOf("moon_phase").forGetter(CustomVanillaObject::moonPhase),
            ResourceLocation.CODEC.optionalFieldOf("moon_texture").forGetter(CustomVanillaObject::moonTexture),
            Codec.FLOAT.optionalFieldOf("moon_height").forGetter(CustomVanillaObject::moonHeight),
            Codec.FLOAT.optionalFieldOf("moon_size").forGetter(CustomVanillaObject::moonSize)
    ).apply(instance, CustomVanillaObject::new));
}
