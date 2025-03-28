package fr.tathan.sky_aesthetics.mixin.client;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 900)
public class LevelRendererMixin {
    @Mutable
    @Shadow
    private ClientLevel level;

    @Final
    @Mutable
    @Shadow
    private LevelTargetBundle targets;

    @Final
    @Mutable
    @Shadow
    private RenderBuffers renderBuffers;


    @Inject(method = "addSkyPass", at = @At(value = "HEAD"), cancellable = true)
    private void renderCustomSkyboxes(FrameGraphBuilder frameGraphBuilder, Camera camera, float f, FogParameters fogParameters, CallbackInfo ci) {
        FogType cameraSubmersionType = camera.getFluidInCamera();
        LevelRenderer levelRenderer = (LevelRenderer) (Object) this;

        SkyAesthetics.LOG.error("Try to render sky part 1...");

        if (cameraSubmersionType != FogType.POWDER_SNOW && cameraSubmersionType != FogType.LAVA && cameraSubmersionType != FogType.WATER && !levelRenderer.doesMobEffectBlockSky(camera)) {
            SkyHelper.canRenderSky(level, (planetSky -> {
                SkyAesthetics.LOG.error("Try to render sky part 2...");
                FramePass framePass = frameGraphBuilder.addPass("sky");
                this.targets.main = framePass.readsAndWrites(this.targets.main);

                framePass.executes(() -> {
                    RenderSystem.setShaderFog(fogParameters);
                    RenderStateShard.MAIN_TARGET.setupRenderState();

                    MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();

                    PoseStack poseStack = new PoseStack();
                    level.effects = planetSky;
                    planetSky.getRenderer().render(level, poseStack, f, this.level.getTimeOfDay(f), fogParameters, Tesselator.getInstance(), bufferSource);
                });
                ci.cancel();
            }));
        }
    }

    /*@Inject(method = "addCloudsPass", at = @At(value = "HEAD"), cancellable = true)
    private void cancelCloudRenderer(FrameGraphBuilder frameGraphBuilder, CloudStatus cloudStatus, Vec3 vec3, float f, int i, float g, CallbackInfo ci) {
        SkyHelper.canRenderSky(level, (planetSky -> {
            if(planetSky.getRenderer().shouldRemoveCloud()) {
                ci.cancel();
            }
        }));
    }*/

    /**
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
     */
}