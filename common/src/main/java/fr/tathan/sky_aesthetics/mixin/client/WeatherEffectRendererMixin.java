package fr.tathan.sky_aesthetics.mixin.client;

import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.state.WeatherRenderState;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WeatherEffectRenderer.class, priority = 900)
public abstract class WeatherEffectRendererMixin {
    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
    public void cancelSnowAndRainRenderer(MultiBufferSource multiBufferSource, Vec3 vec3, WeatherRenderState weatherRenderState, CallbackInfo ci) {
        SkyHelper.canRenderSky(Minecraft.getInstance().level, (planetSky -> {
            if (!planetSky.getRenderer().weather && !(SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingWeather) || SkyAesthetics.CONFIG.disableCustomWeather)) {
                ci.cancel();
            }
        }));
    }

    @Inject(method = "tickRainParticles", at = @At(value = "HEAD"), cancellable = true)
    public void tickRainParticles(ClientLevel level, Camera camera, int i, ParticleStatus particleStatus, int j, CallbackInfo ci) {
        SkyHelper.canRenderSky(level, (planetSky -> {
            if (!planetSky.getRenderer().weather && !(SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingWeather) || SkyAesthetics.CONFIG.disableCustomWeather)) {
                ci.cancel();
            }
        }));
    }
}