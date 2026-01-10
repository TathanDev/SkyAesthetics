package fr.tathan.sky_aesthetics.client.settings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
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

}
