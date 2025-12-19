package fr.tathan.sky_aesthetics.client.skies.settings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;

/**
 * Custom vanilla objects like sun and moon with custom textures, sizes and heights.
 */
public class CustomVanillaObject{

    public record Sun(Identifier sunTexture, Float sunHeight, Float sunSize) {

        public static final Codec<Sun> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("texture").forGetter(Sun::sunTexture),
                Codec.FLOAT.fieldOf("height").forGetter(Sun::sunHeight),
                Codec.FLOAT.fieldOf("size").forGetter(Sun::sunSize)
        ).apply(instance, Sun::new));

        public static Sun createDefaultSun() {
            return createSun(Identifier.parse("textures/environment/sun.png"), 450, 135);
        }

        public static Sun createSun(Identifier texture, float height, float size) {
            return new Sun(texture, height, size);
        }

        public void render(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, float dayAngle) {
            SkyHelper.drawCelestialBody(this.sunTexture(), bufferSource, poseStack, sunHeight, sunSize, dayAngle, true);
        }

    }

    public record Moon(boolean moonPhase, Identifier moonTexture, Float moonHeight, Float moonSize) {

        public static final Codec<Moon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("moon_phase").forGetter(Moon::moonPhase),
                Identifier.CODEC.fieldOf("texture").forGetter(Moon::moonTexture),
                Codec.FLOAT.fieldOf("height").forGetter(Moon::moonHeight),
                Codec.FLOAT.fieldOf("size").forGetter(Moon::moonSize)
        ).apply(instance, Moon::new));

        public static Moon createDefaultMoon() {
            return createMoon(true, Identifier.parse("textures/environment/moon_phases.png"), 75, 75);
        }

        public static Moon createMoon(boolean moonPhase, Identifier texture, float height, float size) {
            return new Moon(moonPhase, texture, height, size);
        }

        public void render(ClientLevel level, MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, float nightAngle) {
            if (this.moonPhase()) {
                SkyHelper.drawMoonWithPhase(bufferSource, poseStack, moonSize(), moonTexture(), nightAngle);
            } else {
                SkyHelper.drawCelestialBody(moonTexture(), bufferSource, poseStack, moonHeight(), moonSize(), nightAngle, 0, 1, 0, 1, false);
            }
        }
    }

}
