package fr.tathan.sky_aesthetics.client.skies.settings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import fr.tathan.sky_aesthetics.helper.PlatformHelper;
import fr.tathan.sky_aesthetics.helper.SkyCompat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

public class CustomVanillaObject{

    public record Sun(ResourceLocation sunTexture, Float sunHeight, Float sunSize) {

        public static final Codec<Sun> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("texture").forGetter(Sun::sunTexture),
                Codec.FLOAT.fieldOf("height").forGetter(Sun::sunHeight),
                Codec.FLOAT.fieldOf("size").forGetter(Sun::sunSize)
        ).apply(instance, Sun::new));

        public static Sun createDefaultSun() {
            return createSun(ResourceLocation.parse("textures/environment/sun.png"), 450, 200);
        }

        public static Sun createSun(ResourceLocation texture, float height, float size) {
            return new Sun(texture, height, size);
        }

        public void render(Tesselator tesselator, PoseStack poseStack, float dayAngle) {
            SkyHelper.drawCelestialBody(this.sunTexture(), tesselator, poseStack, sunHeight, sunSize, dayAngle, true);
        }

    }

    public record Moon(boolean moonPhase, ResourceLocation moonTexture, Float moonHeight, Float moonSize) {

        public static final Codec<Moon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("moon_phase").forGetter(Moon::moonPhase),
                ResourceLocation.CODEC.fieldOf("texture").forGetter(Moon::moonTexture),
                Codec.FLOAT.fieldOf("height").forGetter(Moon::moonHeight),
                Codec.FLOAT.fieldOf("size").forGetter(Moon::moonSize)
        ).apply(instance, Moon::new));

        public static Moon createDefaultMoon() {
            return createMoon(true, ResourceLocation.parse("textures/environment/moon_phases.png"), 75, 75);
        }

        public static Moon createMoon(boolean moonPhase, ResourceLocation texture, float height, float size) {
            return new Moon(moonPhase, texture, height, size);
        }

        public void render(ClientLevel level, Tesselator tesselator, PoseStack poseStack, float nightAngle) {
            if(PlatformHelper.isModLoaded("lunar")) {
                SkyCompat.drawLunarSky(level, tesselator, poseStack, moonSize(), nightAngle);
            } else if (this.moonPhase()) {
                SkyHelper.drawMoonWithPhase(tesselator, poseStack, moonSize(), moonTexture(), nightAngle);
            } else {
                SkyHelper.drawCelestialBody(moonTexture(), tesselator, poseStack, moonHeight(), moonSize(), nightAngle, 0, 1, 0, 1, false);
            }
        }
    }

}
