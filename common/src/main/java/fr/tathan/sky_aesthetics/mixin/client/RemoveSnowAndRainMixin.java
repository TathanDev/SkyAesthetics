package fr.tathan.sky_aesthetics.mixin.client;

import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherEffectRenderer.class)
public class RemoveSnowAndRainMixin {

    @Inject(method = "render(Lnet/minecraft/world/level/Level;Lnet/minecraft/client/renderer/MultiBufferSource;IFLnet/minecraft/world/phys/Vec3;)V", at = @At(value = "HEAD"), cancellable = true)
    private void renderCustomSkyboxes(Level level, MultiBufferSource bufferSource, int ticks, float partialTick, Vec3 cameraPosition, CallbackInfo ci) {
        if(level instanceof ClientLevel clientLevel) {
            SkyHelper.canRenderSky(clientLevel, (planetSky -> {
                if (!planetSky.getProperties().rain()) ci.cancel();
            }));
        }
    }
    @Inject(method = "tickRainParticles", at = @At(value = "HEAD"), cancellable = true)
    public void tickRainParticles(ClientLevel clientLevel, Camera camera, int i, ParticleStatus particleStatus, CallbackInfo ci) {
        SkyHelper.canRenderSky(clientLevel, (planetSky -> {
            if (!planetSky.getProperties().rain()) ci.cancel();
        }));
    }
}
