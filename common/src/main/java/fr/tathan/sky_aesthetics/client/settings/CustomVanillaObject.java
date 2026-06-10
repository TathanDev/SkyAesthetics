package fr.tathan.sky_aesthetics.client.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public final class CustomVanillaObject {

    private CustomVanillaObject() {}

    /**
     * Custom sun. All fields are optional.
     *
     * @param show       Whether to render the sun at all (default true).
     * @param sunTexture Celestials-atlas identifier for the sun texture. When absent the vanilla sun is rendered.
     * @param size       Size of the sun quad (vanilla ≈ 30).
     */
    public record Sun(boolean show, Optional<Identifier> sunTexture, float size) {

        public static final Codec<Sun> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("show", true).forGetter(Sun::show),
                Identifier.CODEC.optionalFieldOf("texture").forGetter(Sun::sunTexture),
                Codec.FLOAT.optionalFieldOf("size", 30.0f).forGetter(Sun::size)
        ).apply(instance, Sun::new));
    }

    /**
     * Custom moon. All fields are optional.
     *
     * @param show        Whether to render the moon at all (default true).
     * @param moonTexture Celestials-atlas identifier for the moon texture. When absent the vanilla moon is rendered.
     *                    For phase rendering the texture must be a 4×2 sprite sheet (same layout as vanilla).
     * @param size        Size of the moon quad (vanilla ≈ 20).
     * @param showPhases  If true, render only the current lunar-phase cell; if false, render the full texture.
     */
    public record Moon(boolean show, Optional<Identifier> moonTexture, float size, boolean showPhases) {

        public static final Codec<Moon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("show", true).forGetter(Moon::show),
                Identifier.CODEC.optionalFieldOf("texture").forGetter(Moon::moonTexture),
                Codec.FLOAT.optionalFieldOf("size", 20.0f).forGetter(Moon::size),
                Codec.BOOL.optionalFieldOf("show_phases", true).forGetter(Moon::showPhases)
        ).apply(instance, Moon::new));
    }
}
