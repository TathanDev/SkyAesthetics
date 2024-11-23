package fr.tathan.sky_aesthetics.mixin.client;

import fr.tathan.sky_aesthetics.client.data.SkyPropertiesData;
import fr.tathan.sky_aesthetics.client.skies.PlanetSky;
import fr.tathan.sky_aesthetics.client.skies.renderer.SkyRenderer;
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

        for (PlanetSky sky : SkyPropertiesData.SKY_PROPERTIES.values()) {
            if (sky.getProperties().world().equals(level.dimension())) {
                SkyRenderer renderer = sky.getRenderer();
                Vec3 cloudColor = renderer.getCloudColor(level.getRainLevel(partialTick), level.getThunderLevel(partialTick));

                if(cloudColor != null) {
                    cir.setReturnValue(cloudColor);
                }
            }
        }
    }

}
