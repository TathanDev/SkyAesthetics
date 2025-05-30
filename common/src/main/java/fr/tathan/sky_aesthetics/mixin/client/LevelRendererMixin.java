package fr.tathan.sky_aesthetics.mixin.client;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 900)
public class LevelRendererMixin {
    @Inject(method = "addCloudsPass", at = @At(value = "HEAD"), cancellable = true)
    private void cancelCloudRenderer(FrameGraphBuilder frameGraphBuilder, CloudStatus cloudStatus, Vec3 cameraPosition, float ticks, int cloudColor, float cloudHeight, CallbackInfo ci) {
        SkyHelper.canRenderSky(Minecraft.getInstance().level, (planetSky -> {
            if(planetSky.getRenderer().shouldRemoveCloud()) {
                ci.cancel();
            }
        }));
    }

    /*
    @Inject(method = "renderSnowAndRain", at = @At(value = "HEAD"), cancellable = true)
    private void cancelSnowAndRainRenderer(LightTexture lightTexture, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        for (PlanetSky sky : SkyPropertiesData.SKY_PROPERTIES.values()) {
            if (sky.getProperties().world().equals(level.dimension())) {
                SkyRenderer renderer = sky.getRenderer();
                if(renderer.shouldRemoveSnowAndRain()) {
                   ci.cancel();
                }
            }
        }
    }

    @Inject(method = "tickRain", at = @At(value = "HEAD"), cancellable = true)
    private void canRain(Camera camera, CallbackInfo ci) {
        for (PlanetSky sky : SkyPropertiesData.SKY_PROPERTIES.values()) {
            if (sky.getProperties().world().equals(level.dimension())) {
                SkyRenderer renderer = sky.getRenderer();
                if(renderer.shouldRemoveSnowAndRain()) {
                    ci.cancel();
                }
            }
        }
    }
    */
}