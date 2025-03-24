package fr.tathan.sky_aesthetics.mixin.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.tathan.sky_aesthetics.client.skies.record.FogSettings;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FogRenderer.class, priority = 900)
public class FogRendererMixin {


    @Unique
    private static ClientLevel sky_aesthetics$level;

    @Mutable
    @Shadow
    private static float fogRed;

    @Mutable
    @Shadow
    private static float fogGreen;

    @Mutable
    @Shadow
    private static float fogBlue;

    @Inject(
            method = "computeFogColor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;z()D",
                    shift = At.Shift.AFTER
            ),
            cancellable = true)
    private static void setupCustomColor(Camera camera, float f, ClientLevel clientLevel, int i, float g, CallbackInfoReturnable<Vector4f> cir) {

        FogRendererMixin.sky_aesthetics$level = clientLevel;

        SkyHelper.canRenderSky(level, (planetSky -> planetSky.getProperties().fogSettings().ifPresent(settings -> settings.customFogColor().ifPresent(color -> {
            fogRed = color.x() / 255.0F;
            fogGreen = color.y() / 255.0F;
            fogBlue = color.z() / 255.0F;
        }))));
    }

    @Inject(
            method = "setupFog",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true)
    private static void modifyFogThickness(Camera camera, FogRenderer.FogMode fogMode, Vector4f vector4f, float f, boolean bl, float g, CallbackInfoReturnable<FogParameters> cir) {
        FogType fogType = camera.getFluidInCamera();


        if (sky_aesthetics$level != null && fogType == FogType.NONE) {
            SkyHelper.canRenderSky(sky_aesthetics$level, (planetSky -> planetSky.getProperties().fogSettings().ifPresent(settings -> settings.fogDensity().ifPresent(density -> {
                RenderSystem.setShaderFogStart(density.x);
                RenderSystem.setShaderFogEnd(density.y);

            }))));
        }
    }

}
