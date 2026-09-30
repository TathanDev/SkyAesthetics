package fr.tathan.sky_aesthetics.mixin.client;

import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.utils.SkyHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelExtractor.class, priority = 900)
public abstract class LevelExtractorMixin {

    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @Inject(
        method = "extract(Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/Camera;F)V",
        at = @At("TAIL")
    )
    private void applyCloudSettings(DeltaTracker deltaTracker, Camera camera, float partialTick, CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        LevelRenderState state = this.levelRenderState;
        SkyHelper.canRenderSky(level, planetSky -> {
            if (!planetSky.renderClouds()) {
                if (!(SkyAesthetics.CONFIG.disableCustomCloud || SkyHelper.isAModCancelRendering(SkyAesthetics.CONFIG.modDisablingCloudRender))) {
                    state.cloudColor &= 0x00FFFFFF;
                    return;
                }
            }
            SkyHelper.getActiveCloudColor(level)
                    .ifPresent(color -> state.cloudColor = (state.cloudColor & 0xFF000000) | (color & 0x00FFFFFF));
            SkyHelper.getActiveCloudHeight(level)
                    .ifPresent(height -> state.cloudHeight = height);
        });
    }
}
