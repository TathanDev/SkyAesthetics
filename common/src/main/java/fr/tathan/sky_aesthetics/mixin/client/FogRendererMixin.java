package fr.tathan.sky_aesthetics.mixin.client;

import fr.tathan.sky_aesthetics.client.utils.SkyHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Shadow
    @Final
    private static List<FogEnvironment> FOG_ENVIRONMENTS;

    @Shadow
    private FogType getFogType(Camera camera) {
        throw new AssertionError();
    }

    @Inject(method = "setupFog", at = @At("RETURN"))
    private void applySkyFogSettings(Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker,
                                     float darkenWorldAmount, ClientLevel level, CallbackInfoReturnable<FogData> cir) {
        if (level == null || !sky_aesthetics$isAtmosphericFog(camera)) return;
        FogData data = cir.getReturnValue();
        SkyHelper.canRenderSky(level, sky -> sky.fogSettings().ifPresent(fog -> fog.apply(data)));
    }

    @Unique
    private boolean sky_aesthetics$isAtmosphericFog(Camera camera) {
        FogType fogType = this.getFogType(camera);
        Entity entity = camera.entity();
        for (FogEnvironment environment : FOG_ENVIRONMENTS) {
            if (environment.isApplicable(fogType, entity)) {
                return environment instanceof AtmosphericFogEnvironment;
            }
        }
        return false;
    }
}
