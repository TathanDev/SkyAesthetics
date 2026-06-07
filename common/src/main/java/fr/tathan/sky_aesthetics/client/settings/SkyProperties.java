package fr.tathan.sky_aesthetics.client.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.tathan.sky_aesthetics.client.DimensionRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec2;

import java.util.List;
import java.util.Optional;

public record SkyProperties(
        ResourceKey<Level> world,
        Identifier id,
        Optional<Boolean> cloudSettings,
        Boolean weather,
        Optional<CustomVanillaObject> customVanillaObject,
        Optional<CustomVanillaObject.Sun> sun,
        Optional<CustomVanillaObject.Moon> moon,
        Optional<StarSettings> stars,
        Optional<SkyColorSettings> skyColorSettings,
        List<SkyObject> skyObjects,
        Optional<RenderCondition> renderCondition,
        Optional<EnvironmentAttributeMap> environmentAttributes,
        Optional<SkyBoxSetting> skyBoxSetting,
        Optional<LightSettings> lightSettings
) {

    public static final Codec<SkyProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("world").forGetter(SkyProperties::world),
            Identifier.CODEC.fieldOf("id").forGetter(SkyProperties::id),
            Codec.BOOL.optionalFieldOf("cloud").forGetter(SkyProperties::cloudSettings),
            Codec.BOOL.optionalFieldOf("weather", true).forGetter(SkyProperties::weather),
            CustomVanillaObject.CODEC.optionalFieldOf("custom_vanilla_object").forGetter(SkyProperties::customVanillaObject),
            CustomVanillaObject.Sun.CODEC.optionalFieldOf("sun").forGetter(SkyProperties::sun),
            CustomVanillaObject.Moon.CODEC.optionalFieldOf("moon").forGetter(SkyProperties::moon),
            StarSettings.CODEC.optionalFieldOf("stars").forGetter(SkyProperties::stars),
            SkyColorSettings.CODEC.optionalFieldOf("sky_color").forGetter(SkyProperties::skyColorSettings),
            SkyObject.CODEC.listOf().fieldOf("sky_objects").forGetter(SkyProperties::skyObjects),
            RenderCondition.CODEC.optionalFieldOf("condition").forGetter(SkyProperties::renderCondition),
            EnvironmentAttributeMap.CODEC_ONLY_POSITIONAL.optionalFieldOf("environment_attributes").forGetter(SkyProperties::environmentAttributes),
            SkyBoxSetting.CODEC.optionalFieldOf("sky_box").forGetter(SkyProperties::skyBoxSetting),
            LightSettings.CODEC.optionalFieldOf("light_settings").forGetter(SkyProperties::lightSettings)
    ).apply(instance, SkyProperties::new));


    public DimensionRenderer toDimensionRenderer() {
        DimensionRenderer.Builder builder = new DimensionRenderer.Builder()
                .setWeather(this.weather);

        this.stars.ifPresent(builder::setStar);
        this.sun.ifPresent(builder::setCustomSun);
        this.moon.ifPresent(builder::setCustomMoon);
        this.skyObjects.forEach(builder::addSkyObject);
        this.customVanillaObject.ifPresent(builder::setCustomVanillaObject);
        this.skyColorSettings.ifPresent(builder::setSkyColorSettings);
        this.lightSettings.ifPresent(builder::setLightSettings);
        this.renderCondition.ifPresent(builder::setRenderCondition);
        this.skyBoxSetting.ifPresent(builder::setSkyBoxSetting);

        return builder.build();
    }


    public static SkyProperties createDefault() {
        return new SkyProperties(
                ResourceKey.create(Registries.DIMENSION, Identifier.parse("overworld")),
                Identifier.parse("default"),
                Optional.of(true),
                true,
                Optional.of(CustomVanillaObject.createDefaultSettings()),
                Optional.empty(),
                Optional.empty(),
                Optional.of(StarSettings.createDefaultStars()),
                Optional.empty(),
                List.of(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }


    public boolean renderClouds() {
        return this.cloudSettings.orElse(true);
    }


    public record RenderCondition(Optional<TagKey<Biome>> biomes, Optional<ResourceKey<Biome>> biome, Optional<Vec2> heightRange) {
        public static Codec<Vec2> VEC2 = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 2).map((listx) -> new Vec2(listx.getFirst(), listx.getLast())), (vector4f) -> List.of(vector4f.x, vector4f.y));

        public static final Codec<RenderCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TagKey.codec(Registries.BIOME).optionalFieldOf("biomes").forGetter(RenderCondition::biomes),
                ResourceKey.codec(Registries.BIOME).optionalFieldOf("biome").forGetter(RenderCondition::biome),
                VEC2.optionalFieldOf("height_range").forGetter(RenderCondition::heightRange)
        ).apply(instance, RenderCondition::new));



        public boolean isSkyRendered(ServerLevel level) {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player == null || level == null) return false;

            if(this.biomes().isPresent()) {
                return level.getBiome(player.getOnPos()).is(this.biomes().get()) && isPlayerAtHeight(player);
            } else if (this.biome().isPresent()) {
                return level.getBiome(player.getOnPos()).is(this.biome().get()) && isPlayerAtHeight(player);
            }

            return true;
        }

        public boolean isPlayerAtHeight(LocalPlayer player) {
            double playerHeight = player.position().y;
            if(this.heightRange.isPresent()) {
                Vec2 heightRange = this.heightRange.get();
                return playerHeight >= heightRange.x && playerHeight <= heightRange.y;
            }

            return true;
        }
    }

}
