package fr.tathan.sky_aesthetics.neoforge.mixin.client;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import fr.tathan.sky_aesthetics.client.DimensionRenderer;
import fr.tathan.sky_aesthetics.client.FogDataCapture;
import fr.tathan.sky_aesthetics.client.data.SkiesRegistry;
import fr.tathan.sky_aesthetics.client.settings.FogSettings;
import fr.tathan.sky_aesthetics.client.utils.SkyHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FogType;
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
    private LevelTargetBundle targets;


    @Mutable
    @Shadow
    private SkyRenderer skyRenderer;


    @Mutable
    @Shadow
    private LevelRenderState levelRenderState;


    @Inject(method = "addSkyPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;)V", at = @At("HEAD"), cancellable = true)
    private void renderCustomSkyboxes(FrameGraphBuilder frame, CameraRenderState cameraState, GpuBufferSlice skyFog, CallbackInfo ci) {
        if (cameraState.fogType != FogType.POWDER_SNOW && cameraState.fogType != FogType.LAVA && !cameraState.entityRenderState.doesMobEffectBlockSky) {
            SkyRenderState skyRenderState = this.levelRenderState.skyRenderState;
            if (skyRenderState.skybox != DimensionType.Skybox.NONE) {
                SkyRenderer skyRenderer = this.skyRenderer;
                if (skyRenderer != null) {
                    SkyHelper.canRenderSky(Minecraft.getInstance().level, (planetSky -> {
                        FramePass framePass = frame.addPass("sky");
                        this.targets.main = framePass.readsAndWrites(this.targets.main);

                        framePass.executes(() -> {
                            DimensionRenderer renderer = SkiesRegistry.getOrBuildRenderer(planetSky);
                            FogSettings fogSettings = renderer.fogSettings;
                            if (fogSettings != null && fogSettings.needsDynamicBuffer()) {
                                FogData vanillaFog = FogDataCapture.getLast();
                                GpuBuffer dynBuf = vanillaFog != null
                                        ? fogSettings.buildDynamicFogBuffer(vanillaFog)
                                        : null;
                                GpuBufferSlice fogSlice = dynBuf != null ? dynBuf.slice() : skyFog;
                                RenderSystem.setShaderFog(fogSlice);
                                renderer.render(skyRenderState, skyRenderer);
                                if (dynBuf != null) dynBuf.close();
                            } else {
                                RenderSystem.setShaderFog(renderer.getCustomFogSlice(skyFog));
                                renderer.render(skyRenderState, skyRenderer);
                            }
                        });
                        ci.cancel();
                    }));

                }
            }
        }
    }
}
