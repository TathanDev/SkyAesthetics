package fr.tathan.neoforge.sky_aesthetics;

import fr.tathan.sky_aesthetics.client.data.SkyPropertiesData;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import fr.tathan.SkyAesthetics;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@Mod(SkyAesthetics.MODID)
public final class SkyAestheticsNeoForge {

    public SkyAestheticsNeoForge(IEventBus modEventBus) {
        SkyAesthetics.init();
        ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(new SkyPropertiesData());
        SkyAesthetics.LOG.error("eeeeeeeeee");

    }

    @SubscribeEvent
    public static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new SkyPropertiesData());
    }


    @Mod.EventBusSubscriber(modid = SkyAesthetics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(new SkyPropertiesData());
        }



    }

}
