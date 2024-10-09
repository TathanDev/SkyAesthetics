package fr.tathan.neoforge.sky_aesthetics;

import fr.tathan.SkyAesthetics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@Mod(SkyAesthetics.MODID)
public final class SkyAestheticsNeoForge {

    public SkyAestheticsNeoForge() {
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
