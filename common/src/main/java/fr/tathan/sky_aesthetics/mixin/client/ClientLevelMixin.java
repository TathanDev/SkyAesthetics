package fr.tathan.sky_aesthetics.mixin.client;

import fr.tathan.sky_aesthetics.client.skies.renderer.SkyRenderer;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {


    @Inject(method = "getCloudColor", at = @At("HEAD"), cancellable = true)
    public void modifyCloudColor(float partialTick, CallbackInfoReturnable<Vec3> cir) {

        ClientLevel level = (ClientLevel) (Object) this;

        SkyHelper.canRenderSky(level, (planetSky -> {
            SkyRenderer renderer = planetSky.getRenderer();
            Vec3 cloudColor = renderer.getCloudColor(level.getRainLevel(partialTick), level.getThunderLevel(partialTick));
            if (cloudColor != null) {
                cir.setReturnValue(cloudColor);
            }
        }));
    }
}
