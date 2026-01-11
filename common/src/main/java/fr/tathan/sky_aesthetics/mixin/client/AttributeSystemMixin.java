package fr.tathan.sky_aesthetics.mixin.client;

import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.utils.SkyHelper;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnvironmentAttributeSystem.class)
public class AttributeSystemMixin {

    @Inject(method = "addDefaultLayers", at = @At("TAIL"), cancellable = false)
    private static void addDefaultLayers(EnvironmentAttributeSystem.Builder builder, Level level, CallbackInfo ci) {
        SkyHelper.canRenderSky(level, (planetSky -> {
            SkyAesthetics.LOG.error("Adding environment attributes for sky: " + planetSky.id());
            planetSky.environmentAttributes().ifPresent(envAttributes -> {
                SkyAesthetics.LOG.error("Attributes set : " + envAttributes.keySet().size());
                        builder.addConstantLayer(envAttributes);
                    }
            );
        }));
    }


}
