package fr.tathan.sky_aesthetics.client.skies.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.Optional;

public record SkyProperties(
        ResourceKey<Level> world,
        Optional<ResourceLocation> id,
        Boolean cloud,
        Float cloudHeight,
        Boolean fog,
        Boolean rain,
        CustomVanillaObject customVanillaObject,
        Star stars,
        Optional<Vector4f> sunriseColor,
        String skyType,
        SkyColor skyColor,
        List<SkyObject> skyObjects,
        Optional<List<String>> constellations,
        Optional<RenderCondition> renderCondition

) {

    public static Codec<Vector4f> VEC4F = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 4).map((listx) -> new Vector4f(listx.getFirst(), listx.get(1), listx.get(2), listx.getLast())), (vec4) -> List.of(vec4.x, vec4.y, vec4.z, vec4.w));


    public static final Codec<SkyProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("world").forGetter(SkyProperties::world),
            ResourceLocation.CODEC.optionalFieldOf("id").forGetter(SkyProperties::id),
            Codec.BOOL.fieldOf("cloud").forGetter(SkyProperties::cloud),
            Codec.FLOAT.fieldOf("cloud_height").forGetter(SkyProperties::cloudHeight),
            Codec.BOOL.fieldOf("fog").forGetter(SkyProperties::fog),
            Codec.BOOL.fieldOf("rain").forGetter(SkyProperties::rain),
            CustomVanillaObject.CODEC.fieldOf("custom_vanilla_objects").forGetter(SkyProperties::customVanillaObject),
            Star.CODEC.fieldOf("stars").forGetter(SkyProperties::stars),
            VEC4F.optionalFieldOf("sunrise_color").forGetter(SkyProperties::sunriseColor),
            Codec.STRING.fieldOf("sky_type").forGetter(SkyProperties::skyType),
            SkyColor.CODEC.fieldOf("sky_color").forGetter(SkyProperties::skyColor),
            SkyObject.CODEC.listOf().fieldOf("sky_objects").forGetter(SkyProperties::skyObjects),
            Codec.STRING.listOf().optionalFieldOf("constellations").forGetter(SkyProperties::constellations),
            RenderCondition.CODEC.optionalFieldOf("condition").forGetter(SkyProperties::renderCondition)
    ).apply(instance, SkyProperties::new));



    public record RenderCondition(boolean condition, Optional<TagKey<Biome>> biomes, Optional<ResourceKey<Biome>> biome) {
        public static final Codec<RenderCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("condition").forGetter(RenderCondition::condition),
                TagKey.codec(Registries.BIOME).optionalFieldOf("biomes").forGetter(RenderCondition::biomes),
                ResourceKey.codec(Registries.BIOME).optionalFieldOf("biome").forGetter(RenderCondition::biome)
        ).apply(instance, RenderCondition::new));

    }
}