package fr.tathan.neoforge.sky_aesthetics;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
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
        public static void onClientSetup(RegisterClientReloadListenersEvent event) {
            SkyAesthetics.onAddReloadListenerEvent((id, listener) -> {
                event.registerReloadListener(listener);
            });
        }
    }

}
