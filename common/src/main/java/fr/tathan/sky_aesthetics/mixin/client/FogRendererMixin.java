package fr.tathan.sky_aesthetics.mixin.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FogRenderer.class, priority = 900)
public class FogRendererMixin {
    @Unique
    private static ClientLevel sky_aesthetics$level;


    @Inject(method = "setupFog", at = @At(value = "TAIL"))
    private static void modifyFogThickness(Camera camera, FogRenderer.FogMode fogMode, Vector4f fogColor, float renderDistance, boolean isFoggy, float partialTick, CallbackInfoReturnable<FogParameters> cir) {
        FogType fogType = camera.getFluidInCamera();

        if (sky_aesthetics$level != null && fogType == FogType.NONE) {
            SkyHelper.canRenderSky(sky_aesthetics$level, (planetSky -> planetSky.getRenderer().fogSettings.fogDensity().ifPresent(density -> {
                FogParameters parameters = new FogParameters(density.x(), density.y(), FogShape.SPHERE, fogColor.x(), fogColor.y(), fogColor.z(), fogColor.w());
                RenderSystem.setShaderFog(parameters);
            })));
        }
    }
}
