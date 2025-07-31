package fr.tathan.sky_aesthetics.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
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
    private void renderCustomSkyboxes(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
        FogType cameraSubmersionType = camera.getFluidInCamera();

        if (!thickFog && cameraSubmersionType != FogType.POWDER_SNOW && cameraSubmersionType != FogType.LAVA && cameraSubmersionType != FogType.WATER && !this.doesMobEffectBlockSky(camera)) {
            SkyHelper.canRenderSky(level, (planetSky -> {
                if(SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingMainSkyRender)) return;

                PoseStack poseStack = new PoseStack();
                poseStack.mulPose(frustumMatrix);

                level.effects = planetSky;
                planetSky.getRenderer().render(level, poseStack, projectionMatrix, partialTick, camera, fogCallback);
                ci.cancel();
            }));
        }
    }

    @Inject(method = "renderClouds", at = @At(value = "HEAD"), cancellable = true)
    private void cancelCloudRenderer(PoseStack poseStack, Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        SkyHelper.canRenderSky(level, (planetSky -> {
            if(planetSky.getRenderer().shouldRemoveCloud()) {
                ci.cancel();
            }
        }));
    }

    @Inject(method = "renderSnowAndRain", at = @At(value = "HEAD"), cancellable = true)
    private void cancelSnowAndRainRenderer(LightTexture lightTexture, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        SkyHelper.canRenderSky(level, (planetSky -> {
            if(planetSky.getRenderer().shouldRemoveSnowAndRain()) {
                ci.cancel();
            }
        }));
    }

    @Inject(method = "tickRain", at = @At(value = "HEAD"), cancellable = true)
    private void canRain(Camera camera, CallbackInfo ci) {
        SkyHelper.canRenderSky(level, (planetSky -> {
            if(planetSky.getRenderer().shouldRemoveSnowAndRain()) {
                ci.cancel();
            }
        }));
    }
}