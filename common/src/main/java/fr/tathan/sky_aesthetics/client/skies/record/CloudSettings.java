package fr.tathan.sky_aesthetics.client.skies.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record CloudSettings(boolean showCloud, Optional<Integer> cloudHeight, Optional<CustomCloudColor> cloudColor) {

    public static final Codec<CloudSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("cloud").forGetter(CloudSettings::showCloud),
            Codec.INT.optionalFieldOf("cloud_height").forGetter(CloudSettings::cloudHeight),
            CustomCloudColor.CODEC.optionalFieldOf("cloud_color").forGetter(CloudSettings::cloudColor)
    ).apply(instance, CloudSettings::new));


    public record CustomCloudColor(Vec3 baseColor, Vec3 stormColor, Vec3 rainColor, boolean alwaysBaseColor) {
        public static final Codec<CustomCloudColor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Vec3.CODEC.fieldOf("base_color").forGetter(CustomCloudColor::baseColor),
                Vec3.CODEC.fieldOf("storm_color").forGetter(CustomCloudColor::stormColor),
                Vec3.CODEC.fieldOf("rain_color").forGetter(CustomCloudColor::rainColor),
                Codec.BOOL.fieldOf("always_base_color").forGetter(CustomCloudColor::alwaysBaseColor)
        ).apply(instance, CustomCloudColor::new));
    }
}
