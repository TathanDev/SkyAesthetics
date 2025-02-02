package fr.tathan.sky_aesthetics.mixin.client.iris;

import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.uniform.DynamicUniformHolder;
import net.irisshaders.iris.uniforms.FogUniforms;
import net.minecraft.client.Minecraft;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

@Mixin(FogUniforms.class)
public class FogUniformMixin {

    @Inject(method = "addFogUniforms", at = @At("TAIL"), remap = false)
    private static void addCustomFogColor(DynamicUniformHolder uniforms, FogMode fogMode, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if(client.level != null) {
            SkyHelper.canRenderSky(client.level, (planetSky -> {
                planetSky.getProperties().fogSettings().ifPresent(settings -> {
                    settings.customFogColor().ifPresent(color -> {
                        uniforms.uniform3f(PER_FRAME, "fogColor", () -> new Vector3f(color.x() / 255.0F, color.y() / 255.0F, color.z() / 255.0F));
                    });
                });
            }));
        }
    }
}
