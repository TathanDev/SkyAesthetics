package fr.tathan.sky_aesthetics.mixin.client.iris;

import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.uniforms.IrisExclusiveUniforms;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IrisExclusiveUniforms.WorldInfoUniforms.class)
public class CustomCloudHeightMixin {

    @Unique
    private static final Minecraft sky_aesthetics$client = Minecraft.getInstance();


    @Inject(method = "addWorldInfoUniforms", at = @At("TAIL"), remap = false)
    private static void setCustomCloudHeight(UniformHolder uniforms, CallbackInfo ci) {
        if(sky_aesthetics$client.level != null) {
            SkyHelper.canRenderSky(sky_aesthetics$client.level, (planetSky -> {
                if(!planetSky.getProperties().cloudSettings().showCloud()) {
                    uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "cloudHeight", () -> 0);
                } else {
                    uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "cloudHeight", () -> planetSky.getProperties().cloudSettings().cloudHeight());
                }

            }));
        }
    }

}
