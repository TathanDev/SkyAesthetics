package fr.tathan.sky_aesthetics.mixin.client;

import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.utils.SkyHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Inject(
            method = "tickWeatherEffects",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelRainParticles(CallbackInfo ci) {
        SkyHelper.canRenderSky(Minecraft.getInstance().level, sky -> {
            if (!sky.weather()
                    && !SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingWeather)
                    && !SkyAesthetics.CONFIG.disableCustomWeather) {
                ci.cancel();
            }
        });
    }
}
