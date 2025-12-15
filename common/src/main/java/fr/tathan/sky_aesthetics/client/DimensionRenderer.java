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
import net.minecraft.client.renderer.*;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * The class handling the rendering of a custom sky
 */
public class DimensionRenderer {

    public final List<SkyObject> skyObjects;
    public final CloudSettings cloudSettings;

    public final CustomVanillaObject.Sun sun;
    public final CustomVanillaObject.Moon moon;
    public final SkyColorSettings skyColor;
    public final FogSettings fogSettings;
    public final StarSettings starSettings;
    public final SkyBoxSetting skyBoxSetting;

    public final boolean weather;
    public final SkyProperties.RenderCondition renderCondition;

    private final VertexBuffer starBuffer;
    private final HashMap<UUID, ShootingStar> shootingStars = new HashMap<>();


    private DimensionRenderer(List<SkyObject> skyObjects,
                              CloudSettings cloudSettings,
                              CustomVanillaObject.Sun sun,
                              CustomVanillaObject.Moon moon,
                              SkyColorSettings skyColor, FogSettings fogSettings, StarSettings starSettings, SkyBoxSetting skyBoxSetting, boolean weather, SkyProperties.RenderCondition renderCondition) {

        this.skyObjects = skyObjects;
        this.cloudSettings = cloudSettings;
        this.sun = sun;
        this.moon = moon;
        this.skyColor = skyColor;
        this.fogSettings = fogSettings;
        this.starSettings = starSettings;
        this.starBuffer = starSettings.getStarsBuffer();
        this.skyBoxSetting = skyBoxSetting;
        this.weather = weather;
        this.renderCondition = renderCondition;
    }

    public boolean canRenderSky() {
        if(this.renderCondition == null) {
            return true; // No condition set, render by default
        }
        return this.renderCondition.isSkyRendered(this.getServerLevel());
    }

    public void render(ClientLevel level, PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, Camera camera, MultiBufferSource.BufferSource bufferSource, SkyRenderer skyRenderer,  FogParameters fog) {

        //TODO use buffersource for EVERYTHING

        DimensionSpecialEffects dimensionSpecialEffects = level.effects();



        float timeOfTheDay = level.getTimeOfDay(partialTick);
        float dayAngle = level.getSunAngle(partialTick);
        float nightAngle = dayAngle + 180;
        int sunsetColor = dimensionSpecialEffects.getSunriseOrSunsetColor(timeOfTheDay);

        //this.skyColor.setSkyColor(level, camera, partialTick);

        int m = level.getSkyColor(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(), partialTick);
        float n = ARGB.redFloat(m);
        float o = ARGB.greenFloat(m);
        float p = ARGB.blueFloat(m);
        skyRenderer.renderSkyDisc(n, o, p);

        if(this.skyBoxSetting != null) {
            this.skyBoxSetting.renderSkyBox(poseStack, projectionMatrix, camera, dayAngle);
        }



        this.starSettings.renderStars(level, partialTick, poseStack, projectionMatrix, fog, nightAngle, starBuffer, skyRenderer);

        //this.starSettings.shootingStars().ifPresent((shootingStars) -> {
        //    this.starSettings.handleShootingStars(level, poseStack, projectionMatrix, this.starSettings, partialTick, this.shootingStars);
        //});

        if (sun != null) {
            sun.render(bufferSource, poseStack, dayAngle);
        }

        if (moon != null) {
            moon.render(null, bufferSource, poseStack, nightAngle);
        }

        for (SkyObject skyObject : skyObjects) {
            skyObject.drawSkyObject(bufferSource, poseStack, dayAngle);
        }


        if (dimensionSpecialEffects.isSunriseOrSunset(dayAngle)) {
            skyRenderer.renderSunriseAndSunset(poseStack, bufferSource, dayAngle, sunsetColor);
        }
        bufferSource.endBatch();
        if (Minecraft.getInstance().player.getEyePosition(partialTick).y - level.getLevelData().getHorizonHeight(level) < (double)0.0F) {
            skyRenderer.renderDarkDisc(poseStack);
        }
        //RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        //RenderSystem.depthMask(true);

    }

    public void testRender(ClientLevel level, PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, Camera camera, Runnable fogCallback) {
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

        public List<SkyObject> skyObjects = new ArrayList<>();
        // Default cloud settings: show clouds and set height to 192
        public CloudSettings cloudSettings = CloudSettings.createDefaultSettings();
        //No sun and moon by default
        public CustomVanillaObject.Sun sun = null;
        public CustomVanillaObject.Moon moon = null;
        public FogSettings fogSettings = FogSettings.createDefaultSettings();
        public StarSettings star = StarSettings.createDefaultStars();
        //Always render sky by default
        public SkyProperties.RenderCondition renderCondition = null;
        public SkyColorSettings skyColor = SkyColorSettings.createDefaultSettings();
        public boolean weather = true; // Default to true

        public SkyBoxSetting skyBoxSetting = null;

        public Builder() {
            // Initialize any necessary fields or configurations here
        }

        public Builder setSkyBoxSetting(SkyBoxSetting skyBoxSetting) {
            this.skyBoxSetting = skyBoxSetting;
            return this;
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
            return new DimensionRenderer(skyObjects, cloudSettings, sun, moon, skyColor, fogSettings, star, skyBoxSetting, weather, renderCondition);
        }

    }

}
