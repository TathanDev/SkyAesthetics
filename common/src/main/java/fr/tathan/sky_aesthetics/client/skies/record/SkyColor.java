package fr.tathan.sky_aesthetics.client.skies.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

public record SkyColor(boolean customColor, Vec3 color) {

    public static final Codec<SkyColor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("custom_color").forGetter(SkyColor::customColor),
            Vec3.CODEC.fieldOf("color").forGetter(SkyColor::color)
    ).apply(instance, SkyColor::new));

}
