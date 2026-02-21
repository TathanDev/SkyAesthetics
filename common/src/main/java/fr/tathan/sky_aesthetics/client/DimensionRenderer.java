package fr.tathan.sky_aesthetics.client;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.tathan.sky_aesthetics.client.settings.CustomVanillaObject;
import fr.tathan.sky_aesthetics.client.settings.SkyObject;
import fr.tathan.sky_aesthetics.client.settings.SkyProperties;
import fr.tathan.sky_aesthetics.client.settings.StarSettings;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.SkyRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.biome.Biome;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The class handling the rendering of a custom sky
 */
public class DimensionRenderer {

    public final List<SkyObject> skyObjects;
    public final TextureAtlas celestialsAtlas;
//    public final CloudSettings cloudSettings;

//    public final CustomVanillaObject.Sun sun;
//    public final CustomVanillaObject.Moon moon;
//    public final SkyColorSettings skyColor;
//    public final FogSettings fogSettings;
    public final StarSettings starSettings;
//    public final SkyBoxSetting skyBoxSetting;
    public final CustomVanillaObject customVanillaObject;
//    public final boolean weather;
    public final SkyProperties.RenderCondition renderCondition;
    public final StarSettings.BufferHolder gpuBuffer;

//    private final HashMap<UUID, ShootingStar> shootingStars = new HashMap<>();

    private DimensionRenderer(List<SkyObject> skyObjects,
                              CustomVanillaObject customVanillaObject,
//                              CustomVanillaObject.Moon moon,
//                              SkyColorSettings skyColor, FogSettings fogSettings,
                              StarSettings starSettings,
//                              SkyBoxSetting skyBoxSetting, boolean weather
                              SkyProperties.RenderCondition renderCondition) {
//
        this.skyObjects = skyObjects;
        this.celestialsAtlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.CELESTIALS);
//        this.cloudSettings = cloudSettings;
//        this.sun = sun;
        this.customVanillaObject = customVanillaObject;
//        this.skyColor = skyColor;
//        this.fogSettings = fogSettings;
        this.starSettings = starSettings;
        this.gpuBuffer = starSettings.buildCustomStars();
//        this.skyBoxSetting = skyBoxSetting;
//        this.weather = weather;
        this.renderCondition = renderCondition;
    }

    public boolean canRenderSky() {
        if(this.renderCondition == null) {
            return true; // No condition set, render by default
        }
        return this.renderCondition.isSkyRendered(this.getServerLevel());
    }

    public void render(ClientLevel level, SkyRenderState skyRenderState, SkyRenderer skyRenderer, Camera camera) {
       // Implementation goes here
       PoseStack poseStack = new PoseStack();


       skyRenderer.renderSkyDisc(skyRenderState.skyColor);

       //TODO: change sunriseAndSunsetColor to use skyRenderState.sunriseAndSunsetColor
       skyRenderer.renderSunriseAndSunset(poseStack, skyRenderState.sunAngle, skyRenderState.sunriseAndSunsetColor);

        this.starSettings.renderStars(poseStack, skyRenderState, skyRenderer, skyRenderState.starAngle, this.gpuBuffer);


        this.renderSkyObjects(poseStack, skyRenderState.sunAngle, skyRenderState.moonAngle, skyRenderState.moonPhase, skyRenderState.rainBrightness, skyRenderer);



       if (skyRenderState.shouldRenderDarkDisc) {
           skyRenderer.renderDarkDisc();
       }

   }

    public void renderSkyObjects(PoseStack poseStack, float sunAngle, float moonAngle, MoonPhase moonPhase, float rainBrightness, SkyRenderer skyRenderer) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));

        if(this.customVanillaObject.sun()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotation(sunAngle));
            skyRenderer.renderSun(rainBrightness, poseStack);
            poseStack.popPose();
        }

        if(this.customVanillaObject.moon()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotation(moonAngle));
            skyRenderer.renderMoon(moonPhase, rainBrightness, poseStack);
            poseStack.popPose();
        }


        for( SkyObject skyObject : skyObjects) {
            skyObject.renderObject(1, poseStack, this.celestialsAtlas, sunAngle, sunAngle);
        }


        poseStack.popPose();
    }

//    public void render(ClientLevel level, PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, Camera camera, MultiBufferSource.BufferSource bufferSource, SkyRenderer skyRenderer,  FogParameters fog) {
//
//        //TODO : Add Fix Sky Color, change fog params, sun angle
//
//        DimensionSpecialEffects dimensionSpecialEffects = level.effects();
//
//        float timeOfTheDay = level.getTimeOfDay(partialTick);
//        float dayAngle = level.getSunAngle(partialTick);
//
//        float nightAngle = dayAngle + 180;
//        int sunsetColor = dimensionSpecialEffects.getSunriseOrSunsetColor(timeOfTheDay);
//
//        //Fog Handling
//        fog = this.fogSettings.setCustomFog(fog);
//        this.fogSettings.runFogCallback(fog);
//
//        /**
//         * Sky Color Handling
//         */
//        int baseSkyColor = level.getSkyColor(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(), partialTick);
//
//        Vector3f skyColor = new Vector3f(ARGB.redFloat(baseSkyColor), ARGB.greenFloat(baseSkyColor), ARGB.blueFloat(baseSkyColor));
//
//        if(this.skyColor.color().isPresent()) {
//            Vector4f customSkyColor = this.skyColor.color().get();
//            skyColor.set(customSkyColor.x / 255f, customSkyColor.y / 255f, customSkyColor.z / 255f);
//        }
//        skyRenderer.renderSkyDisc(skyColor.x, skyColor.y, skyColor.z);
//
//        /**
//         * Sunrise and Sunset Rendering
//         */
//        if (dimensionSpecialEffects.isSunriseOrSunset(timeOfTheDay)) {
//            skyRenderer.renderSunriseAndSunset(poseStack, bufferSource, dayAngle, sunsetColor);
//        }
//
//        /**
//         * Skybox Rendering
//         */
//        if(this.skyBoxSetting != null) {
//            this.skyBoxSetting.renderSkyBox(poseStack, projectionMatrix, camera, dayAngle);
//        }
//
//
//
//        this.starSettings.renderStars(level, partialTick, poseStack, projectionMatrix, fog, nightAngle, starBuffer, skyRenderer);
//
//        //this.starSettings.shootingStars().ifPresent((shootingStars) -> {
//        //    this.starSettings.handleShootingStars(level, poseStack, projectionMatrix, this.starSettings, partialTick, this.shootingStars);
//        //});
//
//
//        this.renderSunAndMoon(poseStack, bufferSource, timeOfTheDay, dayAngle, nightAngle);
//
//        for (SkyObject skyObject : skyObjects) {
//            skyObject.drawSkyObject(bufferSource, poseStack, dayAngle);
//        }
//
//
//        bufferSource.endBatch();
//        if (Minecraft.getInstance().player.getEyePosition(partialTick).y - level.getLevelData().getHorizonHeight(level) < (double)0.0F) {
//            skyRenderer.renderDarkDisc(poseStack);
//        }
//
//    }
//
//    public void testRender(ClientLevel level, PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, Camera camera, Runnable fogCallback) {
//    }
//
//    public void renderSunAndMoon(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float timeOfTheDay, float dayAngle, float nightAngle) {
//        poseStack.pushPose();
//        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
//        poseStack.mulPose(Axis.XP.rotationDegrees(timeOfTheDay * 360.0F));
//
//
//        if (sun != null) {
//            sun.render(bufferSource, poseStack, dayAngle);
//        }
//
//        if (moon != null) {
//            moon.render(null, bufferSource, poseStack, nightAngle);
//        }
//
//        poseStack.popPose();
//
//    }
//
//    public boolean renderClouds() {
//        return cloudSettings.showCloud();
//    }

    public static ServerLevel getServerLevel() {
        Minecraft minecraft = Minecraft.getInstance();
        IntegratedServer integratedServer = minecraft.getSingleplayerServer();
        return integratedServer != null ? integratedServer.getLevel(minecraft.level.dimension()) : null;
    }

    public static class Builder {

        public List<SkyObject> skyObjects = new ArrayList<>();
        // Default cloud settings: show clouds and set height to 192
//        public CloudSettings cloudSettings = CloudSettings.createDefaultSettings();
        //No sun and moon by default
//        public CustomVanillaObject.Sun sun = null;
        public CustomVanillaObject customVanillaObject = CustomVanillaObject.createDefaultSettings();
//        public FogSettings fogSettings = FogSettings.createDefaultSettings();
    //TODO: Default star settings
        public StarSettings star = StarSettings.createDefaultStars();
        //Always render sky by default
        public SkyProperties.RenderCondition renderCondition = null;
//        public SkyColorSettings skyColor = SkyColorSettings.createDefaultSettings();
//        public boolean weather = true; // Default to true
//
//        public SkyBoxSetting skyBoxSetting = null;

        public Builder() {
            // Initialize any necessary fields or configurations here
        }

//        public Builder setSkyBoxSetting(SkyBoxSetting skyBoxSetting) {
//            this.skyBoxSetting = skyBoxSetting;
//            return this;
//        }
//
        public Builder setStar(StarSettings star) {
            this.star = star;
            return this;
        }
//
//        public Builder setFogSettings(FogSettings fogSettings) {
//            this.fogSettings = fogSettings;
//            return this;
//        }
//
//        public Builder setWeather(boolean weather) {
//            this.weather = weather;
//            return this;
//        }
//
//        public Builder setSkyColor(SkyColorSettings skyColor) {
//            this.skyColor = skyColor;
//            return this;
//        }
//
        public Builder setCustomVanillaObject(CustomVanillaObject vanillaObject) {
            this.customVanillaObject = vanillaObject;
            return this;
        }

        public Builder setRenderCondition(SkyProperties.RenderCondition renderCondition) {
            this.renderCondition = renderCondition;
            return this;
        }

//        public Builder addSun(CustomVanillaObject.Sun sun) {
//            this.sun = sun;
//            return this;
//        }

//        public Builder addCloudSettings(CloudSettings cloudSettings) {
//            this.cloudSettings = cloudSettings;
//            return this;
//        }

        public Builder addSkyObject(SkyObject skyObject) {
            this.skyObjects.add(skyObject);
            return this;
        }

        public DimensionRenderer build() {
            return new DimensionRenderer(
                    skyObjects,
                    customVanillaObject,
//                  cloudSettings,
//                    sun,
//                    moon,
//                    skyColor,
//                    fogSettings,
                    star,
//                    skyBoxSetting,
//                    weather,
                    renderCondition
            );
        }
    }

}
