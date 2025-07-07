package fr.tathan.sky_aesthetics.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FogRenderer.class, priority = 900)
public class FogRendererMixin {

    @Mutable
    @Shadow
    private static float fogRed;

    @Mutable
    @Shadow
    private static float fogGreen;

    @Mutable
    @Shadow
    private static float fogBlue;

    @Unique
    private static ClientLevel level;

    @Inject(method = "setupColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;z()D", shift = At.Shift.AFTER))
    private static void setupCustomColor(Camera activeRenderInfo, float partialTicks, ClientLevel level, int renderDistanceChunks, float bossColorModifier, CallbackInfo ci) {

        FogRendererMixin.level = level;
        SkyHelper.canRenderSky(level, (planetSky -> planetSky.getProperties().fogSettings().ifPresent(settings -> settings.customFogColor().ifPresent(color -> {
            fogRed = color.x() / 255.0F;
            fogGreen = color.y() / 255.0F;
            fogBlue = color.z() / 255.0F;
        }))));
    }

    @Inject(method = "setupFog", at = @At(value = "TAIL"))
    private static void modifyFogThickness(Camera camera, FogRenderer.FogMode fogMode, float farPlaneDistance, boolean shouldCreateFog, float partialTick, CallbackInfo ci) {
        FogType fogType = camera.getFluidInCamera();

        if (level != null && fogType == FogType.NONE) {
            SkyHelper.canRenderSky(level, (planetSky -> planetSky.getProperties().fogSettings().ifPresent(settings -> settings.fogDensity().ifPresent(density -> {
                RenderSystem.setShaderFogStart(density.x);
                RenderSystem.setShaderFogEnd(density.y);

            }))));
        }
    }

}
