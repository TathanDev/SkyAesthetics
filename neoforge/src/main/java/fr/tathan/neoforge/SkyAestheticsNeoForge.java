package fr.tathan.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.neoforged.fml.common.Mod;

import fr.tathan.SkyAesthetics;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@Mod(SkyAesthetics.MODID)
public final class SkyAestheticsNeoForge {
    public SkyAestheticsNeoForge() {
        SkyAesthetics.init();
        NeoForge.EVENT_BUS.addListener(SkyAestheticsNeoForge::onAddReloadListenerEvent);
        SkyAesthetics.onAddReloadListenerEvent((id, listener) -> {
            ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(listener);
        });
    }

    public static void onAddReloadListenerEvent(AddReloadListenerEvent event) {
        SkyAesthetics.onAddReloadListenerEvent((id, listener) -> event.addListener(listener));
    }
}
