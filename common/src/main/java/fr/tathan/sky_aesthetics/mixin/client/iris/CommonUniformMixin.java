package fr.tathan.sky_aesthetics.mixin.client.iris;

import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.minecraft.client.Minecraft;
import org.joml.Vector3d;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommonUniforms.class)
public class  CommonUniformMixin {
    @Unique
    private static final Minecraft client = Minecraft.getInstance();

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true, remap = false)
    private static void getSkyColor(CallbackInfoReturnable<Vector3d> cir) {

        SkyHelper.canRenderSky(client.level, (planetSky -> {
            if(planetSky.getProperties().skyColor().customColor()) {
                Vector4f color = planetSky.getProperties().skyColor().color();
                cir.setReturnValue(new Vector3d(color.x, color.y, color.z));
            }
        }));
    }

    @Inject(method = "getRainStrength", at = @At("HEAD"), cancellable = true, remap = false)
    private static void getRainStrength(CallbackInfoReturnable<Float> cir) {
        if(client.level == null) {
            cir.setReturnValue(0f);
        }
        SkyHelper.canRenderSky(client.level, (planetSky -> {
            if(!planetSky.getProperties().rain()) {
                cir.setReturnValue(0f);
            }
        }));
    }

}
