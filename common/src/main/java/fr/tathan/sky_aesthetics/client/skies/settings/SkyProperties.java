package fr.tathan.sky_aesthetics.client.skies.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.DimensionRenderer;
import fr.tathan.sky_aesthetics.client.skies.DimensionSky;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

import java.util.List;
import java.util.Optional;

public record SkyProperties(
        ResourceKey<Level> world,
        ResourceLocation id,
        Optional<CloudSettings> cloudSettings,
        Optional<FogSettings> fogSettings,
        Boolean weather,
        Optional<CustomVanillaObject.Sun> sun,
        Optional<CustomVanillaObject.Moon> moon,
        Optional<StarSettings> stars,
        Optional<SkyColorSettings> skyColor,
        List<SkyObject> skyObjects,
        Optional<RenderCondition> renderCondition,
        Optional<SkyBoxSetting> skyBoxSetting,
        Optional<LightSettings> lightSettings

) {

    public static final Codec<SkyProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("world").forGetter(SkyProperties::world),
            ResourceLocation.CODEC.fieldOf("id").forGetter(SkyProperties::id),

            CloudSettings.CODEC.optionalFieldOf("cloud_settings").forGetter(SkyProperties::cloudSettings),

            FogSettings.CODEC.optionalFieldOf("fog_settings").forGetter(SkyProperties::fogSettings),

            Codec.BOOL.fieldOf("weather").forGetter(SkyProperties::weather),
            CustomVanillaObject.Sun.CODEC.optionalFieldOf("sun").forGetter(SkyProperties::sun),
            CustomVanillaObject.Moon.CODEC.optionalFieldOf("moon").forGetter(SkyProperties::moon),
            StarSettings.CODEC.optionalFieldOf("stars").forGetter(SkyProperties::stars),
            SkyColorSettings.CODEC.optionalFieldOf("sky_color").forGetter(SkyProperties::skyColor),
            SkyObject.CODEC.listOf().fieldOf("sky_objects").forGetter(SkyProperties::skyObjects),
            RenderCondition.CODEC.optionalFieldOf("condition").forGetter(SkyProperties::renderCondition),
            SkyBoxSetting.CODEC.optionalFieldOf("sky_box").forGetter(SkyProperties::skyBoxSetting),
            LightSettings.CODEC.optionalFieldOf("light_settings").forGetter(SkyProperties::lightSettings)
    ).apply(instance, SkyProperties::new));


    public DimensionRenderer toDimensionRenderer() {
        DimensionRenderer.Builder builder = new DimensionRenderer.Builder()
                .setWeather(this.weather);

        this.stars.ifPresent(builder::setStar);
        this.moon.ifPresent(builder::addMoon);
        this.sun.ifPresent(builder::addSun);
        this.skyObjects.forEach(builder::addSkyObject);
        this.cloudSettings.ifPresent(builder::addCloudSettings);
        this.fogSettings.ifPresent(builder::setFogSettings);
        this.skyColor.ifPresent(builder::setSkyColor);
        this.renderCondition.ifPresent(builder::setRenderCondition);
        this.skyBoxSetting.ifPresent(builder::setSkyBoxSetting);

        return builder.build();
    }

    public DimensionSky toDimensionSky() {
        return new DimensionSky(this);
    }

    public static SkyProperties createDefault() {
        return new SkyProperties(
                ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("overworld")),
                ResourceLocation.parse("default"),
                Optional.of(CloudSettings.createDefaultSettings()),
                Optional.of(FogSettings.createDefaultSettings()),
                true,
                Optional.of(CustomVanillaObject.Sun.createDefaultSun()),
                Optional.of(CustomVanillaObject.Moon.createDefaultMoon()),
                Optional.of(StarSettings.createDefaultStars()),
                Optional.of(SkyColorSettings.createDefaultSettings()),
                List.of(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    public static Codec<Vec2> VEC2 = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 2).map((listx) -> new Vec2(listx.getFirst(), listx.getLast())), (vector4f) -> List.of(vector4f.x, vector4f.y));


    public record RenderCondition(Optional<TagKey<Biome>> biomes, Optional<ResourceKey<Biome>> biome, Optional<Vec2> heightRange) {
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
                return playerHeight >= heightRange.y && playerHeight <= heightRange.y;
            }

            return true;
        }
    }

}