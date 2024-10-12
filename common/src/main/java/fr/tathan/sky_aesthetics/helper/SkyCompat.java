package fr.tathan.sky_aesthetics.helper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mrbysco.lunar.client.MoonHandler;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import fr.tathan.sky_aesthetics.mixin.client.LunarMoonInfosAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class SkyCompat {

    public static void drawLunarSky(ClientLevel level, Tesselator tesselator, PoseStack poseStack, float y, float dayAngle) {
        int moonPhase = level.getMoonPhase();
        int xCoord = moonPhase % 4;
        int yCoord = moonPhase / 4 % 2;
        float startX = xCoord / 4.0F;
        float startY = yCoord / 2.0F;
        float endX = (xCoord + 1) / 4.0F;
        float endY = (yCoord + 1) / 2.0F;

        SkyHelper.drawCelestialBody(MoonHandler.getMoonTexture(ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png")), tesselator, poseStack, y, getLunarMoonSize(), dayAngle, startX, endX, startY, endY, true, getLunarMoonColor());
    }

    public static float[] getLunarMoonColor() {
        float[] moonColor = LunarMoonInfosAccessor.sky_aesthetic$moonColor();
        switch (LunarMoonInfosAccessor.sky_aesthetic$eventId()) {
            case "lunar:hero_moon":
                return new float[]{moonColor[0] * 5f, 1.0F, 1.0F, 1.0F};
            default:
                return moonColor;
        }
    }

    public static float getLunarMoonSize() {
        switch (LunarMoonInfosAccessor.sky_aesthetic$eventId()) {
            case "lunar:big_moon":
                return 40f;
            case "lunar:tiny_moon":
                return 10f;
            default:
                return 20f;
        }
    }
}
