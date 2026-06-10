package fr.tathan.sky_aesthetics.mixin.client;

import fr.tathan.sky_aesthetics.client.utils.SkyHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LightmapRenderStateExtractor.class, priority = 900)
public abstract class LightmapRenderStateExtractorMixin {

    @Inject(
        method = "extract(Lnet/minecraft/client/renderer/state/LightmapRenderState;F)V",
        at = @At("TAIL")
    )
    private void applyLightSettings(LightmapRenderState state, float partialTick, CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        SkyHelper.canRenderSky(level, sky -> sky.lightSettings().ifPresent(lights -> {
            if (lights.forceBrightLightmap()) {
                state.brightness = 1.0f;
                state.skyFactor = 1.0f;
                state.blockFactor = 1.0f;
            }
            if (lights.constantAmbientLight()) {
                state.skyFactor = 1.0f;
            }
        }));
    }
}
