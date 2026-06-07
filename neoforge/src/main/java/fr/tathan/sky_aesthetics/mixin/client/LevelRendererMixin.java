package fr.tathan.sky_aesthetics.mixin.client;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.data.SkiesRegistry;
import fr.tathan.sky_aesthetics.client.utils.SkyHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.joml.Matrix4fStack;
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
    private SkyRenderer skyRenderer;

    @Mutable
    @Shadow
    LevelRenderState levelRenderState;


    @Inject(method = "addSkyPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Matrix4fc;)V", at = @At("HEAD"), cancellable = true)
    private void renderCustomSkyboxes(FrameGraphBuilder frame, CameraRenderState cameraState, GpuBufferSlice skyFog, Matrix4fc modelViewMatrix, CallbackInfo ci) {
        if (cameraState.fogType != FogType.POWDER_SNOW && cameraState.fogType != FogType.LAVA && !cameraState.entityRenderState.doesMobEffectBlockSky) {
            SkyRenderState skyRenderState = this.levelRenderState.skyRenderState;
            if (skyRenderState.skybox != DimensionType.Skybox.NONE) {
                SkyRenderer skyRenderer = this.skyRenderer;
                if (skyRenderer != null) {
                    SkyHelper.canRenderSky(level, (planetSky -> {
                        FramePass framePass = frame.addPass("sky");
                        this.targets.main = framePass.readsAndWrites(this.targets.main);

                        framePass.executes(() -> {
                            Matrix4fStack mvStack = RenderSystem.getModelViewStack();
                            mvStack.pushMatrix();
                            mvStack.set(modelViewMatrix);
                            RenderSystem.setShaderFog(skyFog);
                            SkiesRegistry.getOrBuildRenderer(planetSky).render(skyRenderState, skyRenderer);
                            mvStack.popMatrix();
                        });
                        ci.cancel();
                    }));

                }
            }
        }
    }

    @Inject(method = "addCloudsPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/CloudStatus;Lnet/minecraft/world/phys/Vec3;JFIFILorg/joml/Matrix4fc;)V", at = @At(value = "HEAD"), cancellable = true)
    private void cancelCloudRenderer(FrameGraphBuilder frame, CloudStatus cloudStatus, Vec3 cameraPosition, long gameTime, float partialTicks, int cloudColor, float cloudHeight, int cloudRange, Matrix4fc modelViewMatrix, CallbackInfo ci) {
        SkyHelper.canRenderSky(level, (planetSky -> {
            if(!planetSky.renderClouds()) {
                //Only cancel if the sky set remvove clouds but don't cancel if the config said we don't touch clouds
                if(!(SkyAesthetics.CONFIG.disableCustomCloud || SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingCloudRender))) {
                    ci.cancel();
                }

            }
        }));
    }
}