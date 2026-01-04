package fr.tathan.sky_aesthetics.mixin.client;

import net.minecraft.client.renderer.*;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class LevelRendererMixin {

    /*
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
    private void renderCustomSkyboxes(FrameGraphBuilder frameGraphBuilder, Camera camera, GpuBufferSlice shaderFog, CallbackInfo ci) {
        FogType cameraSubmersionType = camera.getFluidInCamera();

        if (cameraSubmersionType != FogType.POWDER_SNOW && cameraSubmersionType != FogType.LAVA && cameraSubmersionType != FogType.WATER && !this.doesMobEffectBlockSky(camera)) {
            SkyHelper.canRenderSky(level, (planetSky -> {
                if(SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingMainSkyRender) || SkyAesthetics.CONFIG.disableCustomSkies) return;

                FramePass framePass = frameGraphBuilder.addPass("sky");
                this.targets.main = framePass.readsAndWrites(this.targets.main);

                framePass.executes(() -> {
                    PoseStack poseStack = new PoseStack();
                    //skyRenderer.renderSkyDisc(skyRenderState.skyColor);
                    //skyRenderer.renderSunriseAndSunset(poseStack, skyRenderState.sunAngle, skyRenderState.sunriseAndSunsetColor);
                    //skyRenderer.renderSunMoonAndStars(poseStack, skyRenderState.sunAngle, skyRenderState.moonAngle, skyRenderState.starAngle, skyRenderState.moonPhase, skyRenderState.rainBrightness, skyRenderState.starBrightness);
                    //if (skyRenderState.shouldRenderDarkDisc) {
                        skyRenderer.renderDarkDisc();
                    }

                    planetSky.getRenderer().render(level, poseStack, RenderSystem.getProjectionMatrix(), partialTick, camera,  bufferSource, skyRenderer, fog);
                });
                ci.cancel();
            }));
        }
    }

    @Inject(method = "addCloudsPass", at = @At(value = "HEAD"), cancellable = true)
    private void cancelCloudRenderer(FrameGraphBuilder frameGraphBuilder, CloudStatus cloudStatus, Vec3 vec3, long l, float f, int i, float g, CallbackInfo ci) {
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


    @Inject(method = "renderSnowAndRain", at = @At(value = "HEAD"), cancellable = true)
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