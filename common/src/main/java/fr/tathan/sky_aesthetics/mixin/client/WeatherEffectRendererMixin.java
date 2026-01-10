package fr.tathan.sky_aesthetics.mixin.client;

import net.minecraft.client.renderer.*;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = WeatherEffectRenderer.class, priority = 900)
public abstract class WeatherEffectRendererMixin {
//    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
//    public void cancelSnowAndRainRenderer(MultiBufferSource multiBufferSource, Vec3 vec3, WeatherRenderState weatherRenderState, CallbackInfo ci) {
//        SkyHelper.canRenderSky(Minecraft.getInstance().level, (planetSky -> {
//            if (!planetSky.getRenderer().weather && !(SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingWeather) || SkyAesthetics.CONFIG.disableCustomWeather)) {
//                ci.cancel();
//            }
//        }));
//    }
//
//    @Inject(method = "tickRainParticles", at = @At(value = "HEAD"), cancellable = true)
//    public void tickRainParticles(ClientLevel level, Camera camera, int i, ParticleStatus particleStatus, int j, CallbackInfo ci) {
//        SkyHelper.canRenderSky(level, (planetSky -> {
//            if (!planetSky.getRenderer().weather && !(SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingWeather) || SkyAesthetics.CONFIG.disableCustomWeather)) {
//                ci.cancel();
//            }
//        }));
//    }
}