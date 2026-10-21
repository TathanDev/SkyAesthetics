package fr.tathan.sky_aesthetics.client.settings;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * The class containing the texture of a skybox layer, a panorama or a cubemap
 * An id ending with .png (mypack:textures/skybox/day.png) is loaded as its own texture
 * Any other id (mypack:day) is a sprite of the celestial atlas
 * @param panorama The equirectangular panorama
 * @param cubemap The six faces of a cubemap
 */
public record SkyTexture(Optional<Identifier> panorama, Optional<Cubemap> cubemap) {

    public static final Codec<SkyTexture> CODEC = Codec.either(Identifier.CODEC, Cubemap.CODEC).xmap(
            (either) -> either.map(SkyTexture::panorama, SkyTexture::cubemap),
            (texture) -> texture.cubemap().<Either<Identifier, Cubemap>>map(Either::right)
                    .orElseGet(() -> Either.left(texture.panorama().orElseThrow())));

    public static SkyTexture panorama(Identifier id) {
        return new SkyTexture(Optional.of(id), Optional.empty());
    }

    public static SkyTexture cubemap(Cubemap cubemap) {
        return new SkyTexture(Optional.empty(), Optional.of(cubemap));
    }

    public static boolean isStandalone(Identifier id) {
        return id.getPath().endsWith(".png");
    }

    public List<Identifier> all() {
        return this.cubemap.map(Cubemap::faces).orElseGet(() -> List.of(this.panorama.orElseThrow()));
    }

    /**
     * Each face is drawn as seen from inside the sky
     * The side faces are upright, up and down have their top edge pointing north
     */
    public record Cubemap(Identifier up, Identifier down, Identifier north, Identifier south, Identifier east, Identifier west) {

        public static final Codec<Cubemap> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("up").forGetter(Cubemap::up),
                Identifier.CODEC.fieldOf("down").forGetter(Cubemap::down),
                Identifier.CODEC.fieldOf("north").forGetter(Cubemap::north),
                Identifier.CODEC.fieldOf("south").forGetter(Cubemap::south),
                Identifier.CODEC.fieldOf("east").forGetter(Cubemap::east),
                Identifier.CODEC.fieldOf("west").forGetter(Cubemap::west)
        ).apply(instance, Cubemap::new));

        /** The faces in the same order as {@link Face} */
        public List<Identifier> faces() {
            return List.of(this.up, this.down, this.north, this.south, this.east, this.west);
        }
    }

    public enum Face {
        UP,
        DOWN,
        NORTH,
        SOUTH,
        EAST,
        WEST
    }
}
