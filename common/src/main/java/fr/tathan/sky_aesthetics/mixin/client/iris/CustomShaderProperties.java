package fr.tathan.sky_aesthetics.mixin.client.iris;

import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.irisshaders.iris.helpers.OptionalBoolean;
import net.irisshaders.iris.shaderpack.properties.ShaderProperties;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaderProperties.class)
public class CustomShaderProperties {

    @Unique
    private static final Minecraft sky_aesthetics$client = Minecraft.getInstance();

    @Inject(method = "getSun", at = @At("HEAD"), cancellable = true, remap = false)
    public void getSun(CallbackInfoReturnable<OptionalBoolean> cir) {
        if(sky_aesthetics$client.level != null) {
            SkyHelper.canRenderSky(sky_aesthetics$client.level, (planetSky -> {
                cir.setReturnValue(sky_aesthetics$fromBoolean(planetSky.getProperties().customVanillaObject().sun()));
            }));
        }
    }

    @Inject(method = "getMoon", at = @At("HEAD"), cancellable = true, remap = false)
    public void getMoon(CallbackInfoReturnable<OptionalBoolean> cir) {
        if(sky_aesthetics$client.level != null) {
            SkyHelper.canRenderSky(sky_aesthetics$client.level, (planetSky -> {
                cir.setReturnValue(sky_aesthetics$fromBoolean(planetSky.getProperties().customVanillaObject().moon()));
            }));
        }
    }

    @Unique
    private OptionalBoolean sky_aesthetics$fromBoolean(boolean value) {
        return value ? OptionalBoolean.TRUE : OptionalBoolean.FALSE;
    }
}
