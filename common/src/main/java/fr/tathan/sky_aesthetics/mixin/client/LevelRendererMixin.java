package fr.tathan.sky_aesthetics.mixin.client;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.utils.SkyHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.state.SkyRenderState;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
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
    public SkyRenderer skyRenderer;

    @Shadow
    protected abstract boolean doesMobEffectBlockSky(Camera camera);

    @Mutable
    @Shadow
    private LevelRenderState levelRenderState;


    @Inject(method = "addSkyPass", at = @At("HEAD"), cancellable = true)
    private void renderCustomSkyboxes2(FrameGraphBuilder frameGraphBuilder, Camera camera, GpuBufferSlice shaderFog, CallbackInfo ci) {
        FogType fogType = camera.getFluidInCamera();
        if (fogType != FogType.POWDER_SNOW && fogType != FogType.LAVA && !this.doesMobEffectBlockSky(camera)) {
            SkyRenderState skyRenderState = this.levelRenderState.skyRenderState;
            if (skyRenderState.skybox != DimensionType.Skybox.NONE) {
                SkyRenderer skyRenderer = this.skyRenderer;
                if (skyRenderer != null) {
                    SkyHelper.canRenderSky(level, (planetSky -> {
                        FramePass framePass = frameGraphBuilder.addPass("sky");
                        this.targets.main = framePass.readsAndWrites(this.targets.main);

                        framePass.executes(() -> {
                            RenderSystem.setShaderFog(shaderFog);
                            planetSky.toDimensionRenderer().render(
                                    level,
                                    skyRenderState,
                                    skyRenderer,
                                    camera
                            );

                        });
                        ci.cancel();
                    }));

                }
            }
        }
    }

    @Inject(method = "addCloudsPass", at = @At(value = "HEAD"), cancellable = true)
    private void cancelCloudRenderer(FrameGraphBuilder frameGraphBuilder, CloudStatus cloudStatus, Vec3 vec3, long l, float f, int i, float g, CallbackInfo ci) {
        SkyHelper.canRenderSky(level, (planetSky -> {
            if(!planetSky.renderClouds()) {
                //Only cancel if the sky set remvove clouds but don't cancel if the config said we don't touch clouds
                if(!(SkyAesthetics.CONFIG.disableCustomCloud || SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingCloudRender))) {
                    ci.cancel();
                }

            }
        }));
    }

    /*
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