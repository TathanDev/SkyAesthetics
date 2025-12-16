package fr.tathan.sky_aesthetics.mixin.client;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
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

    @Mutable
    @Shadow
    private LevelTargetBundle targets;

    @Mutable
    @Shadow
    private RenderBuffers renderBuffers;

    @Mutable
    @Shadow
    public SkyRenderer skyRenderer;

    @Shadow
    protected abstract boolean doesMobEffectBlockSky(Camera camera);

    @Inject(method = "addSkyPass", at = @At("HEAD"), cancellable = true)
    private void renderCustomSkyboxes(FrameGraphBuilder frameGraphBuilder, Camera camera, float partialTick, FogParameters fog, CallbackInfo ci) {
        FogType cameraSubmersionType = camera.getFluidInCamera();

        if (cameraSubmersionType != FogType.POWDER_SNOW && cameraSubmersionType != FogType.LAVA && cameraSubmersionType != FogType.WATER && !this.doesMobEffectBlockSky(camera)) {
            SkyHelper.canRenderSky(level, (planetSky -> {
                if(SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingMainSkyRender) || SkyAesthetics.CONFIG.disableCustomSkies) return;

                FramePass framePass = frameGraphBuilder.addPass("sky");
                this.targets.main = framePass.readsAndWrites(this.targets.main);

                framePass.executes(() -> {
                    RenderStateShard.MAIN_TARGET.setupRenderState();

                    MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
                    PoseStack poseStack = new PoseStack();

                    level.effects = planetSky;

                    planetSky.getRenderer().render(level, poseStack, RenderSystem.getProjectionMatrix(), partialTick, camera,  bufferSource, skyRenderer, fog);
                });
                ci.cancel();
            }));
        }
    }

    @Inject(method = "addCloudsPass", at = @At(value = "HEAD"), cancellable = true)
    private void cancelCloudRenderer(FrameGraphBuilder frameGraphBuilder, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CloudStatus cloudStatus, Vec3 cameraPosition, float ageInTicks, int height, float ticks, CallbackInfo ci) {
        SkyHelper.canRenderSky(level, (planetSky -> {
            if(!planetSky.getRenderer().renderClouds()) {
                //Only cancel if the sky set remvove clouds but don't cancel if the config said we don't touch clouds
                if(!(SkyAesthetics.CONFIG.disableCustomCloud || SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingCloudRender))) {
                    ci.cancel();
                }

            }
        }));
    }

//    @Inject(method = "tickParticles", at = @At(value = "HEAD"), cancellable = true)
//    public void cancelSnowAndRainRenderer(Camera camera, CallbackInfo ci) {
//        SkyHelper.canRenderSky(level, (planetSky -> {
//            if(!planetSky.getRenderer().weather && !(SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingWeather) || SkyAesthetics.CONFIG.disableCustomWeather)) {
//                ci.cancel();
//            }
//        }));
//    }


    /*@Inject(method = "renderSnowAndRain", at = @At(value = "HEAD"), cancellable = true)
    private void cancelSnowAndRainRenderer(LightTexture lightTexture, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        SkyHelper.canRenderSky(level, (planetSky -> {
            if(!planetSky.getRenderer().weather && !(SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingWeather) || SkyAesthetics.CONFIG.disableCustomWeather)) {
                ci.cancel();
            }
        }));
    }

    @Inject(method = "tickRain", at = @At(value = "HEAD"), cancellable = true)
    private void canRain(Camera camera, CallbackInfo ci) {
        SkyHelper.canRenderSky(level, (planetSky -> {
            if(!planetSky.getRenderer().weather && !(SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingWeather) || SkyAesthetics.CONFIG.disableCustomWeather)) {
                ci.cancel();
            }
        }));
    }*/
}