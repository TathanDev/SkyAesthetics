package fr.tathan.sky_aesthetics.client.skies.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import fr.tathan.sky_aesthetics.client.skies.record.*;
import fr.tathan.sky_aesthetics.client.skies.utils.ShootingStar;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import fr.tathan.sky_aesthetics.client.skies.utils.StarHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.*;

public class SkyRenderer {

    private final SkyProperties properties;
    private final VertexBuffer starBuffer;
    private final Map<UUID, ShootingStar> shootingStars;

    public SkyRenderer(SkyProperties properties) {
        this.properties = properties;

        if(!properties.stars().vanilla()) {
            starBuffer = StarHelper.createStars(properties.stars().scale(), properties.stars().count(), properties.stars().color().r(), properties.stars().color().g(), properties.stars().color().b(), properties.constellations());
        } else {
            starBuffer = StarHelper.createVanillaStars();
        }
        this.shootingStars = new HashMap<>();
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
        renderStars(level, partialTick, poseStack, projectionMatrix, fogCallback, nightAngle);

        properties.stars().shootingStars().ifPresent((shootingStar -> {
            handleShootingStars(level, poseStack, projectionMatrix, shootingStar);

        }));


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

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.depthMask(true);
    }

    private void handleShootingStars(Level level, PoseStack poseStack, Matrix4f projectionMatrix, Star.ShootingStars shootingStarConfig) {
        if(!level.isClientSide) return;

        Random random = new Random();
        if (random.nextInt(101) >= shootingStarConfig.percentage()) {
            UUID starId = UUID.randomUUID();
            var shootingStar = new ShootingStar((float) random.nextInt( (int) shootingStarConfig.randomLifetime().x, (int) shootingStarConfig.randomLifetime().y), shootingStarConfig,  starId);
            this.shootingStars.putIfAbsent(starId, shootingStar);
        }

        if(this.shootingStars == null || this.shootingStars.isEmpty() ) return;
        ArrayList<UUID> starsToRemove = new ArrayList<>();
        for (Iterator<ShootingStar> iterator = this.shootingStars.values().iterator(); iterator.hasNext();) {
            ShootingStar shootingStar = (ShootingStar) iterator.next();
            if (shootingStar.render(poseStack, projectionMatrix)) {
                starsToRemove.add(shootingStar.starId);
            }
        }
        starsToRemove.forEach(this.shootingStars::remove);


    }

    private void renderStars(ClientLevel level, float partialTick, PoseStack poseStack, Matrix4f projectionMatrix, Runnable fogCallback, float nightAngle) {
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

        float starsAngle = !this.properties.stars().movingStars() ? -90f : nightAngle;

        if (properties.stars().allDaysVisible()){
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.setShaderColor(starLight + 1f, starLight + 1f, starLight + 1f, starLight + 1f);
            StarHelper.drawStars(starBuffer, poseStack, projectionMatrix, starsAngle);
        } else if (starLight > 0.2F) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.setShaderColor(starLight + 0.5f, starLight + 0.5f, starLight + 0.5f, starLight + 0.5f);
            StarHelper.drawStars(starBuffer, poseStack, projectionMatrix, starsAngle);
        }


        if (properties.fog()) fogCallback.run();

    }


    public Boolean shouldRemoveCloud() {
        return properties.cloud();
    }

    public Boolean shouldRemoveSnowAndRain() {
        return properties.weather().isEmpty();
    }
}
