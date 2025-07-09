package fr.tathan.sky_aesthetics.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.settings.*;
import fr.tathan.sky_aesthetics.client.skies.utils.ShootingStar;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerLevel;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DimensionRenderer {

    public final List<SkyObject> skyObjects;
    public final CloudSettings cloudSettings;

    public final CustomVanillaObject.Sun sun;
    public final CustomVanillaObject.Moon moon;
    public final SkyColorSettings skyColor;
    public final FogSettings fogSettings;
    public final StarSettings starSettings;

    public final boolean weather;
    public final SkyProperties.RenderCondition renderCondition;
    private VertexBuffer starBuffer = null;

    private final HashMap<UUID, ShootingStar> shootingStars = new HashMap<>();


    private DimensionRenderer(List<SkyObject> skyObjects,
                              CloudSettings cloudSettings,
                              CustomVanillaObject.Sun sun,
                              CustomVanillaObject.Moon moon,
                              SkyColorSettings skyColor, FogSettings fogSettings, StarSettings starSettings, boolean weather, SkyProperties.RenderCondition renderCondition) {

        this.skyObjects = skyObjects;
        this.cloudSettings = cloudSettings;
        this.sun = sun;
        this.moon = moon;
        this.skyColor = skyColor;
        this.fogSettings = fogSettings;
        this.starSettings = starSettings;
        this.starBuffer = starSettings.getStarsBuffer();
        this.weather = weather;
        this.renderCondition = renderCondition;
    }

    public void render(ClientLevel level, PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, Camera camera, Runnable fogCallback) {

        if(this.renderCondition != null && !this.renderCondition.isSkyRendered(this.getServerLevel())) {
            return; // Skip rendering if the condition is not met
        }

        Tesselator tesselator = Tesselator.getInstance();
        float dayAngle = level.getTimeOfDay(partialTick) * 360f % 360f;
        float nightAngle = dayAngle + 180;

        this.fogSettings.runFogCallback(fogCallback);


        FogRenderer.levelFogColor();
        RenderSystem.depthMask(false);

        this.skyColor.setSkyColor(level, camera, partialTick);

        SkyHelper.drawSky(poseStack.last().pose(), projectionMatrix);


        this.starSettings.renderStars(level, partialTick, poseStack, projectionMatrix, nightAngle, starBuffer);

        this.starSettings.shootingStars().ifPresent((shootingStars) -> {
            this.starSettings.handleShootingStars(level, poseStack, projectionMatrix, this.starSettings, partialTick, this.shootingStars);
        });

        this.fogSettings.runFogCallback(fogCallback);

        if (sun != null) {
            sun.render(tesselator, poseStack, dayAngle);
        }

        if (moon != null) {
            moon.render(null, tesselator, poseStack, nightAngle);
        }

        for (SkyObject skyObject : skyObjects) {
            skyObject.drawSkyObject(tesselator, poseStack, dayAngle);
        }

        this.fogSettings.runFogCallback(fogCallback);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.depthMask(true);

    }

    public boolean renderClouds() {
        return cloudSettings.showCloud();
    }

    public static ServerLevel getServerLevel() {
        Minecraft minecraft = Minecraft.getInstance();
        IntegratedServer integratedServer = minecraft.getSingleplayerServer();
        return integratedServer != null ? integratedServer.getLevel(minecraft.level.dimension()) : null;
    }

    public static class Builder {

        public List<SkyObject> skyObjects = List.of();
        // Default cloud settings: show clouds and set height to 172
        public CloudSettings cloudSettings = CloudSettings.createDefaultSettings();

        public CustomVanillaObject.Sun sun = null;
        public CustomVanillaObject.Moon moon = null;
        public FogSettings fogSettings = FogSettings.createDefaultSettings();
        public StarSettings star = StarSettings.createDefaultStars();

        public SkyProperties.RenderCondition renderCondition = null;
        public SkyColorSettings skyColor = SkyColorSettings.createDefaultSettings();

        public boolean weather = true; // Default to true

        public Builder() {
            // Initialize any necessary fields or configurations here
        }

        public Builder setStar(StarSettings star) {
            this.star = star;
            return this;
        }

        public Builder setFogSettings(FogSettings fogSettings) {
            this.fogSettings = fogSettings;
            return this;
        }

        public Builder setWeather(boolean weather) {
            this.weather = weather;
            return this;
        }

        public Builder setSkyColor(SkyColorSettings skyColor) {
            this.skyColor = skyColor;
            return this;
        }

        public Builder addMoon(CustomVanillaObject.Moon moon) {
            this.moon = moon;
            return this;
        }

        public Builder setRenderCondition(SkyProperties.RenderCondition renderCondition) {
            this.renderCondition = renderCondition;
            return this;
        }

        public Builder addSun(CustomVanillaObject.Sun sun) {
            this.sun = sun;
            return this;
        }

        public Builder addCloudSettings(CloudSettings cloudSettings) {
            this.cloudSettings = cloudSettings;
            return this;
        }

        public Builder addSkyObject(SkyObject skyObject) {
            this.skyObjects.add(skyObject);
            return this;
        }

        public DimensionRenderer build() {
            return new DimensionRenderer(skyObjects, cloudSettings, sun, moon, skyColor, fogSettings, star, weather, renderCondition);
        }

    }

}
