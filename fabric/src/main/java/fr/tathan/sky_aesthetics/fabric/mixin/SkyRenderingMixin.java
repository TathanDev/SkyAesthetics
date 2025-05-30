package fr.tathan.sky_aesthetics.fabric.mixin;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 900)
public class SkyRenderingMixin {
    @Mutable
    @Shadow
    private ClientLevel level;

    @Final
    @Shadow
    private LevelTargetBundle targets;

    @Inject(
            method = "addSkyPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/FogParameters;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void renderCustomSkybox(FrameGraphBuilder frameGraphBuilder, Camera camera, float partialTick, FogParameters fog, CallbackInfo ci) {
        FogType cameraSubmersionType = camera.getFluidInCamera();
        LevelRenderer levelRenderer = (LevelRenderer) (Object) this;

        if (cameraSubmersionType != FogType.POWDER_SNOW && cameraSubmersionType != FogType.LAVA && cameraSubmersionType != FogType.WATER && !levelRenderer.doesMobEffectBlockSky(camera)) {
            SkyHelper.canRenderSky(level, (planetSky -> {
                FramePass framePass = frameGraphBuilder.addPass("sky");
                this.targets.main = framePass.readsAndWrites(this.targets.main);

                framePass.executes(() -> {
                    RenderSystem.setShaderFog(fog);
                    RenderStateShard.MAIN_TARGET.setupRenderState();

                    PoseStack poseStack = new PoseStack();
                    level.effects = planetSky;
                    planetSky.getRenderer().render(level, poseStack, partialTick, this.level.getTimeOfDay(partialTick), fog, Tesselator.getInstance());
                });
                ci.cancel();
            }));
        }
    }
}
