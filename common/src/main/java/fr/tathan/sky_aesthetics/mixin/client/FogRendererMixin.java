package fr.tathan.sky_aesthetics.mixin.client;

import fr.tathan.sky_aesthetics.client.data.SkyPropertiesData;
import fr.tathan.sky_aesthetics.client.skies.PlanetSky;
import fr.tathan.sky_aesthetics.client.skies.renderer.SkyRenderer;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
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

    @Inject(
            method = "setupColor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;z()D",
                    shift = At.Shift.AFTER
            )
    )
    private static void setupCustomColor(Camera activeRenderInfo, float partialTicks, ClientLevel level, int renderDistanceChunks, float bossColorModifier, CallbackInfo ci) {

        SkyHelper.canRenderSky(level, (planetSky -> {
            planetSky.getProperties().customVanillaObject().customFogColor().ifPresent(color -> {
                fogRed = color.x();
                fogGreen = color.y();
                fogBlue = color.z();
            });
        }));

    }
}
