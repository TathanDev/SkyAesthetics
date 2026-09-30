package fr.tathan.sky_aesthetics.mixin.client;

import fr.tathan.sky_aesthetics.client.utils.SkyHelper;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnvironmentAttributeSystem.Builder.class)
public class AttributeSystemMixin {

    @Inject(method = "addDefaultLayers(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/attribute/EnvironmentAttributeSystem$Builder;", at = @At("TAIL"))
    private void addDefaultLayers(Level level, CallbackInfoReturnable<EnvironmentAttributeSystem.Builder> cir) {
        if (!level.isClientSide()) return;
        EnvironmentAttributeSystem.Builder builder = (EnvironmentAttributeSystem.Builder) (Object) this;
        SkyHelper.canApplyAttributes(level, (planetSky -> planetSky.environmentAttributes().ifPresent(builder::addConstantLayer)));
    }
}
