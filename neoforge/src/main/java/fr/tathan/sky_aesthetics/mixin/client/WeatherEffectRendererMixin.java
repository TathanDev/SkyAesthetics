package fr.tathan.sky_aesthetics.mixin.client;

import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.utils.SkyHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WeatherEffectRenderer.class, priority = 900)
public abstract class WeatherEffectRendererMixin {

    @Inject(
        method = "render(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/state/level/WeatherRenderState;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void cancelWeatherRender(Vec3 cameraPos, WeatherRenderState weatherState, CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        SkyHelper.canRenderSky(level, sky -> {
            if (!sky.weather()
                    && !SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingWeather)
                    && !SkyAesthetics.CONFIG.disableCustomWeather) {
                ci.cancel();
            }
        });
    }

    @Inject(
        method = "tickRainParticles(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/Camera;ILnet/minecraft/server/level/ParticleStatus;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void cancelRainParticles(ClientLevel level, Camera camera, int ticks,
                                     ParticleStatus particleStatus, int p5, CallbackInfo ci) {
        SkyHelper.canRenderSky(level, sky -> {
            if (!sky.weather()
                    && !SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingWeather)
                    && !SkyAesthetics.CONFIG.disableCustomWeather) {
                ci.cancel();
            }
        });
    }
}
