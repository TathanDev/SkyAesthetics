package fr.tathan.sky_aesthetics.client.skies.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.DimensionRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

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
        StarSettings stars,
        Optional<SkyColorSettings> skyColor,
        List<SkyObject> skyObjects,
        Optional<RenderCondition> renderCondition) {

    public static final Codec<SkyProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("world").forGetter(SkyProperties::world),
            ResourceLocation.CODEC.fieldOf("id").forGetter(SkyProperties::id),

            CloudSettings.CODEC.optionalFieldOf("cloud_settings").forGetter(SkyProperties::cloudSettings),

            FogSettings.CODEC.optionalFieldOf("fog_settings").forGetter(SkyProperties::fogSettings),

            Codec.BOOL.fieldOf("weather").forGetter(SkyProperties::weather),
            CustomVanillaObject.Sun.CODEC.optionalFieldOf("sun").forGetter(SkyProperties::sun),
            CustomVanillaObject.Moon.CODEC.optionalFieldOf("moon").forGetter(SkyProperties::moon),
            StarSettings.CODEC.fieldOf("stars").forGetter(SkyProperties::stars),
            SkyColorSettings.CODEC.optionalFieldOf("sky_color").forGetter(SkyProperties::skyColor),
            SkyObject.CODEC.listOf().fieldOf("sky_objects").forGetter(SkyProperties::skyObjects),
            RenderCondition.CODEC.optionalFieldOf("condition").forGetter(SkyProperties::renderCondition)
    ).apply(instance, SkyProperties::new));


    public DimensionRenderer toDimensionRenderer() {
        DimensionRenderer.Builder builder = new DimensionRenderer.Builder()
                .setWeather(this.weather).setStar(this.stars);

        this.moon.ifPresent(builder::addMoon);
        this.sun.ifPresent(builder::addSun);
        this.skyObjects.forEach(builder::addSkyObject);
        this.cloudSettings.ifPresent(builder::addCloudSettings);
        this.fogSettings.ifPresent(builder::setFogSettings);
        this.skyColor.ifPresent(builder::setSkyColor);
        this.renderCondition.ifPresent(builder::setRenderCondition);

        return builder.build();
    }

    public record RenderCondition(Optional<TagKey<Biome>> biomes, Optional<ResourceKey<Biome>> biome) {
        public static final Codec<RenderCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TagKey.codec(Registries.BIOME).optionalFieldOf("biomes").forGetter(RenderCondition::biomes),
                ResourceKey.codec(Registries.BIOME).optionalFieldOf("biome").forGetter(RenderCondition::biome)
        ).apply(instance, RenderCondition::new));

        public boolean isSkyRendered(ServerLevel level) {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player == null || level == null) return false;

            if(this.biomes().isPresent()) {
                return level.getBiome(player.getOnPos()).is(this.biomes().get());
            } else if (this.biome().isPresent()) {
                return level.getBiome(player.getOnPos()).is(this.biome().get());
            }

            return true;
        }
    }

}