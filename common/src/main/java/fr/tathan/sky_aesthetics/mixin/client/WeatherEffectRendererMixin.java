package fr.tathan.sky_aesthetics.mixin.client;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = WeatherEffectRenderer.class, priority = 900)
public abstract class WeatherEffectRendererMixin {
    @Inject(method = "render(Lnet/minecraft/world/level/Level;Lnet/minecraft/client/renderer/MultiBufferSource;IFLnet/minecraft/world/phys/Vec3;)V", at = @At(value = "HEAD"), cancellable = true)
    public void cancelSnowAndRainRenderer(Level level, MultiBufferSource bufferSource, int ticks, float partialTick, Vec3 cameraPosition, CallbackInfo ci) {

        if (!(level instanceof ClientLevel)) return;
        SkyHelper.canRenderSky((ClientLevel) level, (planetSky -> {
            if (!planetSky.getRenderer().weather && !(SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingWeather) || SkyAesthetics.CONFIG.disableCustomWeather)) {
                ci.cancel();
            }
        }));
    }

    @Inject(method = "tickRainParticles", at = @At(value = "HEAD"), cancellable = true)
    public void tickRainParticles(ClientLevel level, Camera camera, int ticks, ParticleStatus particleStatus, CallbackInfo ci) {
        if (!(level instanceof ClientLevel)) return;
        SkyHelper.canRenderSky(level, (planetSky -> {
            if (!planetSky.getRenderer().weather && !(SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingWeather) || SkyAesthetics.CONFIG.disableCustomWeather)) {
                ci.cancel();
            }
        }));

    }
}