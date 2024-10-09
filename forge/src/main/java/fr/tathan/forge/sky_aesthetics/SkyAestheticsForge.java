package fr.tathan.forge.sky_aesthetics;

import fr.tathan.SkyAesthetics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(SkyAesthetics.MODID)
public final class SkyAestheticsForge {
    public SkyAestheticsForge() {
        SkyAesthetics.init();
    }

    @Mod.EventBusSubscriber(modid = SkyAesthetics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value= Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(RegisterClientReloadListenersEvent event) {
            SkyAesthetics.onAddReloadListenerEvent((id, listener) -> {
                event.registerReloadListener(listener);
            });
        }
    }

}
