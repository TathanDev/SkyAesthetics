package fr.tathan.sky_aesthetics.neoforge;

import fr.tathan.exoconfig.platform.PlatformClientHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import fr.tathan.SkyAesthetics;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

@Mod(SkyAesthetics.MODID)
public final class SkyAestheticsNeoForge {

    public SkyAestheticsNeoForge() {
        SkyAesthetics.init();

    }

    @EventBusSubscriber(modid = SkyAesthetics.MODID, value= Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onReloadListener(AddClientReloadListenersEvent event) {
            SkyAesthetics.onAddReloadListenerEvent(event::addListener);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            PlatformClientHelper.registerConfigScreen(SkyAesthetics.MODID, SkyAesthetics.CONFIG);

        }
    }

}
