package fr.tathan.sky_aesthetics.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.tathan.sky_aesthetics.client.data.SkyPropertiesData;
import fr.tathan.sky_aesthetics.client.skies.PlanetSky;
import fr.tathan.sky_aesthetics.client.skies.renderer.SkyRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class LevelRendererMixin {
    @Mutable
    @Shadow
    private ClientLevel level;

    @Shadow
    protected abstract boolean doesMobEffectBlockSky(Camera camera);

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void renderCustomSkyboxes(Matrix4f matrix4f, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
        FogType cameraSubmersionType = camera.getFluidInCamera();

        if (!thickFog && cameraSubmersionType != FogType.POWDER_SNOW && cameraSubmersionType != FogType.LAVA && cameraSubmersionType != FogType.WATER && !this.doesMobEffectBlockSky(camera)) {

            for (PlanetSky sky : SkyPropertiesData.SKY_PROPERTIES.values()) {
                if (sky.getProperties().world().equals(level.dimension())) {
                    PoseStack poseStack = new PoseStack();
                    poseStack.mulPose(matrix4f);

                    SkyRenderer renderer = sky.getRenderer();

                    if(renderer.isSkyRendered()) {
                        renderer.render(level, poseStack, projectionMatrix, partialTick, camera, fogCallback);
                        ci.cancel();
                    }
                }

            }

        }
    }

    @Inject(method = "renderClouds", at = @At(value = "HEAD"), cancellable = true)
    private void cancelCloudRenderer(PoseStack poseStack, Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        for (PlanetSky sky : SkyPropertiesData.SKY_PROPERTIES.values()) {
            if (sky.getProperties().world().equals(level.dimension())) {
                SkyRenderer renderer = sky.getRenderer();
                if(renderer.shouldRemoveCloud()) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "renderSnowAndRain", at = @At(value = "HEAD"), cancellable = true)
    private void cancelSnowAndRainRenderer(LightTexture lightTexture, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        for (PlanetSky sky : SkyPropertiesData.SKY_PROPERTIES.values()) {
            if (sky.getProperties().world().equals(level.dimension())) {
                SkyRenderer renderer = sky.getRenderer();
                if(renderer.shouldRemoveSnowAndRain()) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "tickRain", at = @At(value = "HEAD"), cancellable = true)
    private void canRain(Camera camera, CallbackInfo ci) {
        for (PlanetSky sky : SkyPropertiesData.SKY_PROPERTIES.values()) {
            if (sky.getProperties().world().equals(level.dimension())) {
                SkyRenderer renderer = sky.getRenderer();
                if(renderer.shouldRemoveSnowAndRain()) {
                    ci.cancel();
                }
            }
        }
    }
}