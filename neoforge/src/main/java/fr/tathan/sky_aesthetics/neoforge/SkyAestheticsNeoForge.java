package fr.tathan.sky_aesthetics.neoforge;

import fr.tathan.exoconfig.platform.PlatformHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import fr.tathan.SkyAesthetics;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@Mod(SkyAesthetics.MODID)
public final class SkyAestheticsNeoForge {

    public SkyAestheticsNeoForge() {
        SkyAesthetics.init();

    }

    @EventBusSubscriber(modid = SkyAesthetics.MODID, bus = EventBusSubscriber.Bus.MOD, value= Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(RegisterClientReloadListenersEvent event) {
            PlatformHelper.registerConfigScreen(SkyAesthetics.MODID, SkyAesthetics.CONFIG);

            SkyAesthetics.onAddReloadListenerEvent((id, listener) -> event.registerReloadListener(listener));
        }
    }

}
