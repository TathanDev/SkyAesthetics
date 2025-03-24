package fr.tathan.sky_aesthetics.client.skies.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Axis;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.PlanetSky;
import fr.tathan.sky_aesthetics.client.skies.record.*;
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
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.*;
import java.util.*;

public class SkyRenderer {

    private final SkyProperties properties;
    private final VertexBuffer starBuffer;
    private final Map<UUID, ShootingStar> shootingStars;
    private final net.minecraft.client.renderer.SkyRenderer skyRenderer = new net.minecraft.client.renderer.SkyRenderer();
    private final PlanetSky planetSky;

    public SkyRenderer(SkyProperties properties, PlanetSky planetSky) {
        this.properties = properties;
        this.planetSky = planetSky;
        if(!properties.stars().vanilla()) {
            starBuffer = StarHelper.createStars(properties.stars().scale(), properties.stars().count(), properties.stars().color().x, properties.stars().color().y, properties.stars().color().z, properties.constellations());
        } else {
            starBuffer = StarHelper.createVanillaStars();
        }
        this.shootingStars = new HashMap<>();
    }


    public void render(ClientLevel level, PoseStack poseStack, Camera camera, float partialTick, float gameTime, FogParameters fog, Tesselator tesselator,MultiBufferSource.BufferSource bufferSource) {
        if(!isSkyRendered()) return;

        if (Objects.equals(properties.skyType(), "END")) {
            this.skyRenderer.renderEndSky();
            return;
        }

        CustomVanillaObject customVanillaObject = properties.customVanillaObject();

        float dayAngle = gameTime * 360f;
        float nightAngle = dayAngle + 180;
        float sunAngle = level.getSunAngle(partialTick);
        boolean shouldRenderDarkDisc = Minecraft.getInstance().player.getEyePosition(partialTick).y - level.getLevelData().getHorizonHeight(level) < (double)0.0F;
        float rainLevel = 1.0F - level.getRainLevel(partialTick);


        RenderSystem.depthMask(false);

        int m = level.getSkyColor(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(), partialTick);
        Vec3 skyColorVector = new Vec3(ARGB.red(m), ARGB.green(m), ARGB.blue(m));

        Vector4f vec4 = new Vector4f((float) skyColorVector.x,(float) skyColorVector.y,(float) skyColorVector.z, 1.0f);
        if (properties.skyColor().customColor() && properties.skyColor().color().isPresent()) {
            vec4 = properties.skyColor().color().get();
        }


        this.skyRenderer.renderSkyDisc((float) skyColorVector.x / 255f, (float) skyColorVector.y / 255f, (float) skyColorVector.z / 255f);

        int sunsetColor = planetSky.getSunriseOrSunsetColor(gameTime);

        if (planetSky.isSunriseOrSunset(gameTime)) {
            this.skyRenderer.renderSunriseAndSunset(poseStack, bufferSource, sunAngle, sunsetColor);
        }

        SkyHelper.renderSunMoonAndStars(customVanillaObject,  poseStack,  (gameTime), level.getMoonPhase(), bufferSource, rainLevel);

        renderStars(level, partialTick, poseStack, nightAngle, fog);

        properties.stars().shootingStars().ifPresent((shootingStar -> handleShootingStars(level, poseStack, properties.stars(), partialTick)));




        // Other sky object
        for (SkyObject skyObject : properties.skyObjects()) {
            SkyHelper.renderCelestialBody(skyObject, tesselator, poseStack,  dayAngle);
        }

        if (shouldRenderDarkDisc) {
            this.skyRenderer.renderDarkDisc(poseStack);
        }

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
        if (random.nextInt(1001) == 0) {
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

        float rainLevel = 1.0F - level.getRainLevel(partialTick);
        float starLight = level.getStarBrightness(partialTick) * rainLevel;

        if(properties.stars().vanilla()) {
            if(starLight > 0.0f) {
                this.skyRenderer.renderStars(fog, starLight, poseStack);
            }
            return;
        }

        if (properties.stars().allDaysVisible()){
            drawStar(starBuffer, poseStack, starLight, nightAngle, fog);
        } else if (starLight > 0.2F) {
            drawStar(starBuffer, poseStack, starLight, nightAngle, fog);
        }
    }

    public void drawStar(VertexBuffer vertexBuffer, PoseStack poseStack, float starLight, float nightTime, FogParameters fogParameters) {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();

        if(properties.stars().movingStars())
            poseStack.mulPose(Axis.ZP.rotationDegrees(nightTime));

        matrix4fStack.mul(poseStack.last().pose());


        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(CoreShaders.POSITION);
        RenderSystem.setShaderColor(starLight, starLight, starLight, starLight);
        RenderSystem.enableBlend();
        RenderSystem.setShaderFog(FogParameters.NO_FOG);
        vertexBuffer.bind();
        vertexBuffer.drawWithShader(matrix4fStack, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
        VertexBuffer.unbind();
        RenderSystem.setShaderFog(fogParameters);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        matrix4fStack.popMatrix();

    }


    public Boolean shouldRemoveCloud() {
        return !properties.cloudSettings().showCloud();
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
