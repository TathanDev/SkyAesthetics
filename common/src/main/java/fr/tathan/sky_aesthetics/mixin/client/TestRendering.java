package fr.tathan.sky_aesthetics.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.tathan.sky_aesthetics.client.skies.settings.SkyObject;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public class TestRendering {

    @Mutable
    @Shadow
    private TextureAtlas celestialsAtlas;


    @Inject(method = "renderSunMoonAndStars", at = @At("TAIL"), cancellable = true)
    public void renderSunMoonAndStars(PoseStack poseStack, float f, float g, float h, MoonPhase moonPhase, float i, float j, CallbackInfo ci) {
        poseStack.pushPose();
        SkyObject object = new SkyObject(Identifier.withDefaultNamespace("sun"),
                false, 1, null,
                null, 1, null);
        object.renderObject(1, poseStack, celestialsAtlas);
        poseStack.popPose();
    }
}
