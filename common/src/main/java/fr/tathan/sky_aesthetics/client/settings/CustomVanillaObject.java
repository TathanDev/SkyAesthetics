package fr.tathan.sky_aesthetics.client.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/**
 * Custom vanilla objects like sun and moon with custom textures, sizes and heights.
 */
public record CustomVanillaObject(boolean sun, boolean moon) {

    public static final Codec<CustomVanillaObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("sun").forGetter(CustomVanillaObject::sun),
            Codec.BOOL.fieldOf("moon").forGetter(CustomVanillaObject::moon)
    ).apply(instance, CustomVanillaObject::new));

    public static CustomVanillaObject createDefaultSettings() {
        return new CustomVanillaObject(true, true);
    }

    /**
     * A custom sun that replaces the vanilla sun.
     *
     * @param sunTexture Celestials-atlas identifier for the sun texture.
     * @param size       Size of the sun quad (vanilla ≈ 30).
     */
    public record Sun(Identifier sunTexture, float size) {

        public static final Codec<Sun> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("texture").forGetter(Sun::sunTexture),
                Codec.FLOAT.optionalFieldOf("size", 30.0f).forGetter(Sun::size)
        ).apply(instance, Sun::new));
    }

    /**
     * A custom moon that replaces the vanilla moon.
     *
     * @param moonTexture Celestials-atlas identifier for the moon texture.
     *                    For phase rendering, the texture must be a 4×2 sprite sheet
     *                    (same layout as vanilla's moon_phases.png).
     * @param size        Size of the moon quad (vanilla ≈ 20).
     * @param showPhases  If true, render only the current lunar-phase cell of the sprite sheet;
     *                    if false, render the full texture regardless of phase.
     */
    public record Moon(Identifier moonTexture, float size, boolean showPhases) {

        public static final Codec<Moon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("texture").forGetter(Moon::moonTexture),
                Codec.FLOAT.optionalFieldOf("size", 20.0f).forGetter(Moon::size),
                Codec.BOOL.optionalFieldOf("show_phases", true).forGetter(Moon::showPhases)
        ).apply(instance, Moon::new));
    }
}
