package fr.tathan.sky_aesthetics.client.skies.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.record.*;
import fr.tathan.sky_aesthetics.client.skies.utils.ShootingStar;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import fr.tathan.sky_aesthetics.client.skies.utils.StarHelper;
import fr.tathan.sky_aesthetics.helper.PlatformHelper;
import fr.tathan.sky_aesthetics.helper.SkyCompat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.*;

public class SkyRenderer {

    private final SkyProperties properties;
    private VertexBuffer starBuffer = null;
    private final Map<UUID, ShootingStar> shootingStars;

    public SkyRenderer(SkyProperties properties) {
        this.properties = properties;

        if(properties.stars().count() > 100) {
            starBuffer = StarHelper.createStars(properties.stars().scale(), properties.stars().count(), (int) properties.stars().color().x(), (int) properties.stars().color().y(), (int) properties.stars().color().z(), properties.constellations(), properties.stars().starsTexture());
        } else if (properties.stars().vanilla() ){
            starBuffer = StarHelper.createVanillaStars();
        }
        this.shootingStars = new HashMap<>();
    }


    public void render(ClientLevel level, PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, Camera camera, Runnable fogCallback) {
        if(!isSkyRendered()) return;

        runFogCallback(fogCallback);

        Tesselator tesselator = Tesselator.getInstance();
        CustomVanillaObject customVanillaObject = null;
        if (properties.customVanillaObject().isPresent()) {
            customVanillaObject = properties.customVanillaObject().get();
        }

        float dayAngle = level.getTimeOfDay(partialTick) * 360f % 360f;
        float nightAngle = dayAngle + 180;

        Vec3 vec3 = level.getSkyColor(camera.getPosition(), partialTick);
        Vector4f vec4 = new Vector4f((float) vec3.x,(float) vec3.y,(float) vec3.z, 1.0f);

        if (properties.skyColor().customColor() && properties.skyColor().color().isPresent()) {
            vec4 = properties.skyColor().color().get();
        }

        FogRenderer.levelFogColor();
        RenderSystem.depthMask(false);

        RenderSystem.setShaderColor(vec4.x, vec4.y, vec4.z, vec4.w);

        ShaderInstance shaderInstance = RenderSystem.getShader();

        if(Objects.equals(properties.skyType(), "NORMAL")) {
            SkyHelper.drawSky(poseStack.last().pose(), projectionMatrix, shaderInstance);
        } else if(Objects.equals(properties.skyType(), "END")) {
            SkyHelper.renderEndSky(poseStack);
        }

        // Star
        renderStars(level, partialTick, poseStack, projectionMatrix, fogCallback, nightAngle);

        properties.stars().shootingStars().ifPresent((shootingStar -> handleShootingStars(level, poseStack, projectionMatrix, properties.stars(), partialTick)));

        if (customVanillaObject != null) {
            // Sun
            if (customVanillaObject.sun() && customVanillaObject.sunTexture().isPresent() && customVanillaObject.sunHeight().isPresent() && customVanillaObject.sunSize().isPresent()) {
                SkyHelper.drawCelestialBody(customVanillaObject.sunTexture().get(), tesselator, poseStack, customVanillaObject.sunHeight().get(), customVanillaObject.sunSize().get(), dayAngle, true);
            }

            // Moon
            if (customVanillaObject.moon()) {
                if(PlatformHelper.isModLoaded("lunar")) {
                    SkyCompat.drawLunarSky(level, tesselator, poseStack, customVanillaObject.moonSize().get(), nightAngle);
                } else if (customVanillaObject.moonPhase()) {
                    SkyHelper.drawMoonWithPhase(tesselator, poseStack, customVanillaObject.moonSize().get(), customVanillaObject, nightAngle);
                } else {
                    SkyHelper.drawCelestialBody(customVanillaObject.moonTexture().get(), tesselator, poseStack, customVanillaObject.moonHeight().get(), customVanillaObject.moonSize().get(), nightAngle, 0, 1, 0, 1, false);
                }
            }
        }


        // Other sky object
        for (SkyObject skyObject : properties.skyObjects()) {
            SkyHelper.drawCelestialBody(skyObject, tesselator, poseStack,  dayAngle);
        }
        runFogCallback(fogCallback);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.depthMask(true);
    }

    private void handleShootingStars(ClientLevel level, PoseStack poseStack, Matrix4f projectionMatrix, Star star, float partialTick) {
        if(!level.isClientSide) return;

        float starLight = level.getStarBrightness(partialTick) * (1.0f - level.getRainLevel(partialTick));

        if(!star.allDaysVisible() && !(starLight > 0.2F)) {
            if(!shootingStars.isEmpty()) shootingStars.clear();
            return;
        }

        Star.ShootingStars shootingStarConfig = star.shootingStars().get();
        Random random = new Random();
        if (random.nextInt(1001) >= shootingStarConfig.percentage()) {
            UUID starId = UUID.randomUUID();
            var shootingStar = new ShootingStar((float) random.nextInt( (int) shootingStarConfig.randomLifetime().x, (int) shootingStarConfig.randomLifetime().y), shootingStarConfig,  starId);
            this.shootingStars.putIfAbsent(starId, shootingStar);
        }

        if(this.shootingStars == null || this.shootingStars.isEmpty() ) return;
        ArrayList<UUID> starsToRemove = new ArrayList<>();
        for (ShootingStar shootingStar : this.shootingStars.values()) {
            if (shootingStar.render(poseStack, projectionMatrix)) {
                starsToRemove.add(shootingStar.starId);
            }
        }
        starsToRemove.forEach(this.shootingStars::remove);
    }

    private void renderStars(ClientLevel level, float partialTick, PoseStack poseStack, Matrix4f projectionMatrix, Runnable fogCallback, float nightAngle) {
        float starLight = level.getStarBrightness(partialTick) * (1.0f - level.getRainLevel(partialTick));

        if(starBuffer == null) return;

        if (properties.stars().vanilla()) {
            if (starLight > 0.0f) {
                RenderSystem.setShaderColor(starLight, starLight, starLight, starLight);
                FogRenderer.setupNoFog();
                this.starBuffer.bind();
                this.starBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionShader());
                VertexBuffer.unbind();
            }
            return;
        }

        // star texture

        float starsAngle = !this.properties.stars().movingStars() ? -90f : nightAngle;

        if (properties.stars().allDaysVisible()) {
            if(properties.stars().starsTexture().isPresent()) {
                RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            } else {
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
            }

            RenderSystem.setShaderColor(starLight + 1f, starLight + 1f, starLight + 1f, starLight + 1f);
            StarHelper.drawStars(starBuffer, poseStack, projectionMatrix, starsAngle, this.properties.stars().starsTexture());
        } else if (starLight > 0.2F) {
            if(properties.stars().starsTexture().isPresent()) {
                RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            } else {
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
            }
            RenderSystem.setShaderColor(starLight + 0.5f, starLight + 0.5f, starLight + 0.5f, starLight + 0.5f);
            StarHelper.drawStars(starBuffer, poseStack, projectionMatrix, starsAngle, this.properties.stars().starsTexture());
        }

        runFogCallback(fogCallback);
    }



    public void runFogCallback(Runnable fogCallback) {

        if(properties.fogSettings().isEmpty()) {
            fogCallback.run();
            return;
        }

        properties.fogSettings().ifPresent((fogSettings -> {
            if(fogSettings.fog()) {
                fogCallback.run();
            }
        }));
    }

    public Boolean shouldRemoveCloud() {
        return SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingCloudRender) || properties.cloudSettings().isPresent() && !properties.cloudSettings().get().showCloud();
    }

    public Boolean shouldRemoveSnowAndRain() {
        return SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingWeather) || !properties.rain();
    }

    public boolean isSkyRendered() {
        if (this.properties.renderCondition().isEmpty() || !this.properties.renderCondition().get().condition()) return true;
        SkyProperties.RenderCondition condition = this.properties.renderCondition().get();
        ServerLevel level = this.getServerLevel();
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null || level == null) return false;

        if(condition.biomes().isPresent()) {
            return level.getBiome(player.getOnPos()).is(condition.biomes().get());
        } else if (condition.biome().isPresent()) {
            return level.getBiome(player.getOnPos()).is(condition.biome().get());
        }

        return true;
    }

    public Vec3 getCloudColor(float rainLevel, float stormLevel) {
        if(this.properties.cloudSettings().isPresent() && this.properties.cloudSettings().get().cloudColor().isPresent()) {
            CloudSettings.CustomCloudColor color = this.properties.cloudSettings().get().cloudColor().get();

            if(stormLevel > 0.0f && !color.alwaysBaseColor()) {
                return new Vec3(color.stormColor().x, color.stormColor().y, color.stormColor().z);
            } else if(rainLevel > 0.0f && !color.alwaysBaseColor()) {
                return new Vec3(color.rainColor().x, color.rainColor().y, color.rainColor().z);
            } else {
                return new Vec3(color.baseColor().x, color.baseColor().y, color.baseColor().z);
            }
        }

        return null;
    }

    private ServerLevel getServerLevel() {
        Minecraft minecraft = Minecraft.getInstance();
        IntegratedServer integratedServer = minecraft.getSingleplayerServer();
        return integratedServer != null ? integratedServer.getLevel(minecraft.level.dimension()) : null;
    }
}
