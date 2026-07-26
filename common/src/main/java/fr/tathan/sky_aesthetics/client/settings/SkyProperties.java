package fr.tathan.sky_aesthetics.client.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.tathan.sky_aesthetics.client.DimensionRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
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
        Optional<CloudSettings> cloudSettings,
        Boolean weather,
        Optional<CustomVanillaObject.Sun> sun,
        Optional<CustomVanillaObject.Moon> moon,
        Optional<StarSettings> stars,
        Optional<SkyColorSettings> skyColorSettings,
        List<SkyObject> skyObjects,
        Optional<RenderCondition> renderCondition,
        Optional<EnvironmentAttributeMap> environmentAttributes,
        Optional<SkyBoxSetting> skyBoxSetting,
        Optional<LightSettings> lightSettings,
        Optional<FogSettings> fogSettings
) {

    public static final Codec<SkyProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("world").forGetter(SkyProperties::world),
            Identifier.CODEC.fieldOf("id").forGetter(SkyProperties::id),
            CloudSettings.CODEC.optionalFieldOf("cloud_settings").forGetter(SkyProperties::cloudSettings),
            Codec.BOOL.optionalFieldOf("weather", true).forGetter(SkyProperties::weather),
            CustomVanillaObject.Sun.CODEC.optionalFieldOf("sun").forGetter(SkyProperties::sun),
            CustomVanillaObject.Moon.CODEC.optionalFieldOf("moon").forGetter(SkyProperties::moon),
            StarSettings.CODEC.optionalFieldOf("stars").forGetter(SkyProperties::stars),
            SkyColorSettings.CODEC.optionalFieldOf("sky_color").forGetter(SkyProperties::skyColorSettings),
            SkyObject.CODEC.listOf().fieldOf("sky_objects").forGetter(SkyProperties::skyObjects),
            RenderCondition.CODEC.optionalFieldOf("condition").forGetter(SkyProperties::renderCondition),
            EnvironmentAttributeMap.CODEC.optionalFieldOf("environment_attributes").forGetter(SkyProperties::environmentAttributes),
            SkyBoxSetting.CODEC.optionalFieldOf("sky_box").forGetter(SkyProperties::skyBoxSetting),
            LightSettings.CODEC.optionalFieldOf("light_settings").forGetter(SkyProperties::lightSettings),
            FogSettings.CODEC.optionalFieldOf("fog_settings").forGetter(SkyProperties::fogSettings)
    ).apply(instance, SkyProperties::new));


    public DimensionRenderer toDimensionRenderer() {
        DimensionRenderer.Builder builder = new DimensionRenderer.Builder()
                .setWeather(this.weather);

        this.stars.ifPresent(builder::setStar);
        this.sun.ifPresent(builder::setCustomSun);
        this.moon.ifPresent(builder::setCustomMoon);
        this.skyObjects.forEach(builder::addSkyObject);
        this.skyColorSettings.ifPresent(builder::setSkyColorSettings);
        this.lightSettings.ifPresent(builder::setLightSettings);
        this.renderCondition.ifPresent(builder::setRenderCondition);
        this.skyBoxSetting.ifPresent(builder::setSkyBoxSetting);
        this.fogSettings.ifPresent(builder::setFogSettings);

        return builder.build();
    }


    public static SkyProperties createDefault() {
        return new SkyProperties(
                ResourceKey.create(Registries.DIMENSION, Identifier.parse("overworld")),
                Identifier.parse("default"),
                Optional.empty(),
                true,
                Optional.empty(),
                Optional.empty(),
                Optional.of(StarSettings.createDefaultStars()),
                Optional.empty(),
                List.of(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }


    public boolean renderClouds() {
        return this.cloudSettings.map(CloudSettings::showCloud).orElse(true);
    }

    public java.util.OptionalInt cloudHeight() {
        return this.cloudSettings
                .map(cs -> java.util.OptionalInt.of(cs.cloudHeight()))
                .orElse(java.util.OptionalInt.empty());
    }


    public record RenderCondition(Optional<TagKey<Biome>> biomes, Optional<ResourceKey<Biome>> biome, Optional<Vec2> heightRange) {
        public static Codec<Vec2> VEC2 = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 2).map((listx) -> new Vec2(listx.getFirst(), listx.getLast())), (vector4f) -> List.of(vector4f.x, vector4f.y));

        public static final Codec<RenderCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TagKey.codec(Registries.BIOME).optionalFieldOf("biomes").forGetter(RenderCondition::biomes),
                ResourceKey.codec(Registries.BIOME).optionalFieldOf("biome").forGetter(RenderCondition::biome),
                VEC2.optionalFieldOf("height_range").forGetter(RenderCondition::heightRange)
        ).apply(instance, RenderCondition::new));



        public boolean isSkyRendered() {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            Level level = minecraft.level;

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
