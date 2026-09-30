package fr.tathan.sky_aesthetics.mixin.client;

import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.utils.SkyHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WeatherEffectRenderer.class, priority = 900)
public abstract class WeatherEffectRendererMixin {

    @Inject(
        method = "prepare(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/state/level/WeatherRenderState;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void cancelWeatherPrepare(Vec3 cameraPos, WeatherRenderState weatherState, CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        SkyHelper.canRenderSky(level, sky -> {
            if (!sky.weather()
                    && !SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingWeather)
                    && !SkyAesthetics.CONFIG.disableCustomWeather) {
                weatherState.rainColumns.clear();
                weatherState.snowColumns.clear();
                ci.cancel();
            }
        });
    }
}
