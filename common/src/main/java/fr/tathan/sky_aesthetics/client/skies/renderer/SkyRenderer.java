package fr.tathan.sky_aesthetics.client.skies.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.record.Constellation;
import fr.tathan.sky_aesthetics.client.skies.record.CustomVanillaObject;
import fr.tathan.sky_aesthetics.client.skies.record.SkyObject;
import fr.tathan.sky_aesthetics.client.skies.record.SkyProperties;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import fr.tathan.sky_aesthetics.client.skies.utils.StarHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

public class SkyRenderer {

    private final SkyProperties properties;
    private final VertexBuffer starBuffer;

    public SkyRenderer(SkyProperties properties) {
        this.properties = properties;

        if(!properties.stars().vanilla()) {
            starBuffer = StarHelper.createStars(properties.stars().scale(), properties.stars().count(), properties.stars().color().r(), properties.stars().color().g(), properties.stars().color().b());
        } else {
            starBuffer = StarHelper.createVanillaStars();
        }


    }


    public void render(ClientLevel level, PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, Camera camera, Runnable fogCallback) {
        if (properties.fog()) fogCallback.run();

        Tesselator tesselator = Tesselator.getInstance();
        CustomVanillaObject customVanillaObject = properties.customVanillaObject();

        float dayAngle = level.getTimeOfDay(partialTick) * 360f % 360f;
        float nightAngle = dayAngle + 180;

        Vec3 vec3 = level.getSkyColor(camera.getPosition(), partialTick);
        float r = (float) vec3.x;
        float g = (float) vec3.y;
        float b = (float) vec3.z;

        FogRenderer.levelFogColor();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(r, g, b, 1.0f);

        ShaderInstance shaderInstance = RenderSystem.getShader();
        SkyHelper.drawSky(poseStack.last().pose(), projectionMatrix, shaderInstance, tesselator, poseStack, partialTick);

        // Star
        renderStars(level, partialTick, poseStack, projectionMatrix, fogCallback);

        // Sun
        if (customVanillaObject.sun()) {
            SkyHelper.drawCelestialBody(customVanillaObject.sunTexture(), tesselator, poseStack, customVanillaObject.sunHeight(), customVanillaObject.sunSize(), dayAngle, true);
        }

        // Moon
        if (customVanillaObject.moon()) {
            if (customVanillaObject.moonPhase()) {
                SkyHelper.drawMoonWithPhase(level, tesselator, poseStack, customVanillaObject.moonSize(), customVanillaObject, nightAngle);
            } else {
                SkyHelper.drawCelestialBody(customVanillaObject.moonTexture(), tesselator, poseStack, customVanillaObject.moonHeight(), customVanillaObject.moonSize(), nightAngle, 0, 1, 0, 1, false);
            }
        }


        // Other sky object
        for (SkyObject skyObject : properties.skyObjects()) {
            SkyHelper.drawCelestialBody(skyObject, tesselator, poseStack,  dayAngle);
        }
        if (properties.fog()) fogCallback.run();

        renderConstellation(level, partialTick, poseStack, projectionMatrix, fogCallback);


        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.depthMask(true);
    }



    private void renderConstellation(ClientLevel level, float partialTick, PoseStack poseStack, Matrix4f projectionMatrix, Runnable fogCallback) {
        float starLight = level.getStarBrightness(partialTick) * (1.0f - level.getRainLevel(partialTick));
        if (starLight > 0.2F) {

            for (Constellation constellation : properties.constellations()) {

                VertexBuffer buffer = StarHelper.createConstellation(constellation);
                RenderSystem.setShaderColor(5, 5, 5, 5);
                FogRenderer.setupNoFog();
                buffer.bind();
                buffer.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionShader());
                VertexBuffer.unbind();
            }


        }
        if (properties.fog()) fogCallback.run();

    }


    private void renderStars(ClientLevel level, float partialTick, PoseStack poseStack, Matrix4f projectionMatrix, Runnable fogCallback) {
        float starLight = level.getStarBrightness(partialTick) * (1.0f - level.getRainLevel(partialTick));

        if(properties.stars().vanilla()) {
            if(starLight > 0.0f) {
                RenderSystem.setShaderColor(starLight, starLight, starLight, starLight);
                FogRenderer.setupNoFog();
                this.starBuffer.bind();
                this.starBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionShader());
                VertexBuffer.unbind();
            }
            return;
        }

        if (properties.stars().allDaysVisible()){
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.setShaderColor(starLight + 1f, starLight + 1f, starLight + 1f, starLight + 1f);
            StarHelper.drawStars(starBuffer, poseStack, projectionMatrix);
        } else if (starLight > 0.2F) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.setShaderColor(starLight + 0.5f, starLight + 0.5f, starLight + 0.5f, starLight + 0.5f);
            StarHelper.drawStars(starBuffer, poseStack, projectionMatrix);
        }
        if (properties.fog()) fogCallback.run();

    }

    public Boolean shouldRemoveCloud() {
        return !properties.cloud();
    }

    public Boolean shouldRemoveSnowAndRain() {
        return properties.weather().isEmpty();
    }
}
