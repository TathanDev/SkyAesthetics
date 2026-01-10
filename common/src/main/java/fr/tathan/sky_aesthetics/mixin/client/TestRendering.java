package fr.tathan.sky_aesthetics.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.tathan.sky_aesthetics.client.settings.SkyObject;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.MoonPhase;
import org.joml.Vector3f;
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
    public void renderSunMoonAndStars(PoseStack poseStack, float sunAngle, float moonAngle, float starAngle, MoonPhase moonPhase, float rainBrightness, float starBrightness, CallbackInfo ci) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotation(sunAngle));
        SkyObject object = new SkyObject(Identifier.withDefaultNamespace("sun"),
                false, 70, new Vector3f(0),
                new Vector3f(0), 1, "DAY");


        object.setObjectRotation(poseStack);
        object.setObjectPosition(poseStack, sunAngle);

        object.renderObject(1, poseStack, celestialsAtlas, sunAngle, moonAngle);
        poseStack.popPose();
    }

}
