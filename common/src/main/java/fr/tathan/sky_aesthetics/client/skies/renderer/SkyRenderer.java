package fr.tathan.sky_aesthetics.client.skies.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.record.CustomVanillaObject;
import fr.tathan.sky_aesthetics.client.skies.record.SkyObject;
import fr.tathan.sky_aesthetics.client.skies.record.SkyProperties;
import fr.tathan.sky_aesthetics.client.skies.record.Star;
import fr.tathan.sky_aesthetics.client.skies.utils.ShootingStar;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import fr.tathan.sky_aesthetics.client.skies.utils.StarHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
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


    public void render(ClientLevel level, PoseStack poseStack, Camera camera, float partialTick, FogParameters fog) {
        if(!isSkyRendered()) return;

        Tesselator tesselator = Tesselator.getInstance();
        CustomVanillaObject customVanillaObject = properties.customVanillaObject();

        float dayAngle = level.getTimeOfDay(partialTick) * 360f % 360f;
        float nightAngle = dayAngle + 180;

        RenderSystem.depthMask(false);

        int c = level.getSkyColor(camera.getPosition(), partialTick);
        if(properties.skyColor().customColor()) {
            c = properties.skyColor().color();
        }
        Color color = new Color(c);

        RenderSystem.setShaderColor(color.getRed(), color.getGreen(), color.getBlue(), 1.0f);

        if (Objects.equals(properties.skyType(), "OVERWORLD")) {
            SkyHelper.renderSky();
        } else if (Objects.equals(properties.skyType(), "END")) {
            SkyHelper.renderEndSky(poseStack);
        }

        // Star
        renderStars(level, partialTick, poseStack, nightAngle, fog);

        properties.stars().shootingStars().ifPresent((shootingStar -> handleShootingStars(level, poseStack, properties.stars(), partialTick)));

        // Sun
        if (customVanillaObject.sun()) {
            SkyHelper.drawCelestialBody(customVanillaObject.sunTexture(), tesselator, poseStack, customVanillaObject.sunHeight(), customVanillaObject.sunSize(), dayAngle, true);
        }

        // Moon
        if (customVanillaObject.moon()) {
            if (customVanillaObject.moonPhase()) {
                SkyHelper.drawMoonWithPhase(tesselator, poseStack, customVanillaObject.moonSize(), customVanillaObject, nightAngle);
            } else {
                SkyHelper.drawCelestialBody(customVanillaObject.moonTexture(), tesselator, poseStack, customVanillaObject.moonHeight(), customVanillaObject.moonSize(), nightAngle, 0, 1, 0, 1, false);
            }
        }

        // Other sky object
        for (SkyObject skyObject : properties.skyObjects()) {
            SkyHelper.drawCelestialBody(skyObject, tesselator, poseStack,  dayAngle);
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.depthMask(true);
    }

    private void handleShootingStars(ClientLevel level, PoseStack poseStack, Star star, float partialTick) {
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
            if (shootingStar.render(poseStack)) {
                starsToRemove.add(shootingStar.starId);
            }
        }
        starsToRemove.forEach(this.shootingStars::remove);
    }

    private void renderStars(ClientLevel level, float partialTick, PoseStack poseStack, float nightAngle, FogParameters fog) {
        float starLight = level.getStarBrightness(partialTick) * (1.0f - level.getRainLevel(partialTick));

        if(properties.stars().vanilla()) {
            if(starLight > 0.0f) {
                RenderSystem.setShaderColor(starLight, starLight, starLight, starLight);
                RenderSystem.setShaderFog(fog);
                this.starBuffer.bind();
                this.starBuffer.drawWithShader(poseStack.last().pose(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
                VertexBuffer.unbind();
            }
            return;
        }

        float starsAngle = !this.properties.stars().movingStars() ? -90f : nightAngle;

        if (properties.stars().allDaysVisible()){
            RenderSystem.setShader(CoreShaders.POSITION_COLOR);
            RenderSystem.setShaderColor(starLight + 1f, starLight + 1f, starLight + 1f, starLight + 1f);
            StarHelper.drawStars(starBuffer, poseStack, starsAngle);
        } else if (starLight > 0.2F) {
            RenderSystem.setShader(CoreShaders.POSITION_COLOR);
            RenderSystem.setShaderColor(starLight + 0.5f, starLight + 0.5f, starLight + 0.5f, starLight + 0.5f);
            StarHelper.drawStars(starBuffer, poseStack, starsAngle);
        }
    }


    public Boolean shouldRemoveCloud() {
        return !properties.cloud();
    }

    public Boolean shouldRemoveSnowAndRain() {
        return !properties.rain();
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

    private ServerLevel getServerLevel() {
        Minecraft minecraft = Minecraft.getInstance();
        IntegratedServer integratedServer = minecraft.getSingleplayerServer();
        return integratedServer != null ? integratedServer.getLevel(minecraft.level.dimension()) : null;
    }

    public Vec3 getCloudColor(float rainLevel, float stormLevel) {
        if(this.properties.cloudSettings().cloudColor().isPresent()) {
            CloudSettings.CustomCloudColor color = this.properties.cloudSettings().cloudColor().get();

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

}
