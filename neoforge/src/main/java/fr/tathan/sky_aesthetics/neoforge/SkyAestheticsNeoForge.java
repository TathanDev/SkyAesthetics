package fr.tathan.sky_aesthetics.neoforge;

import fr.tathan.exoconfig.platform.PlatformHelper;
import fr.tathan.exoconfig.platform.PlatformHelperClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import fr.tathan.SkyAesthetics;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@Mod(SkyAesthetics.MODID)
public final class SkyAestheticsNeoForge {

    public SkyAestheticsNeoForge() {
        SkyAesthetics.init();

    }

    @EventBusSubscriber(modid = SkyAesthetics.MODID, bus = EventBusSubscriber.Bus.MOD, value= Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onReloadListener(RegisterClientReloadListenersEvent event) {

            SkyAesthetics.onAddReloadListenerEvent((id, listener) -> event.registerReloadListener(listener));
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            PlatformHelperClient.registerConfigScreen(SkyAesthetics.MODID, SkyAesthetics.CONFIG);

        }
    }

}
