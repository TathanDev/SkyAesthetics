package fr.tathan.sky_aesthetics.client.skies.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import org.joml.Vector4f;

import java.util.List;

public record SkyColor(boolean customColor, Vector4f color) {

    public static Codec<Vector4f> VEC4F = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 4).map((listx) -> new Vector4f(listx.getFirst(), listx.get(1), listx.get(2), listx.getLast())), (vector4f) -> List.of(vector4f.x, vector4f.y, vector4f.z, vector4f.w));


    public static final Codec<SkyColor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("custom_color").forGetter(SkyColor::customColor),
            VEC4F.fieldOf("color").forGetter(SkyColor::color)
    ).apply(instance, SkyColor::new));

}
