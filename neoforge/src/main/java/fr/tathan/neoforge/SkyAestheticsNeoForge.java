package fr.tathan.neoforge;

import net.neoforged.fml.common.Mod;

import fr.tathan.SkyAesthetics;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@Mod(SkyAesthetics.MODID)
public final class SkyAestheticsNeoForge {
    public SkyAestheticsNeoForge() {
        SkyAesthetics.init();
        NeoForge.EVENT_BUS.addListener(SkyAestheticsNeoForge::onAddReloadListenerEvent);

    }

    public static void onAddReloadListenerEvent(AddReloadListenerEvent event) {
        SkyAesthetics.onAddReloadListenerEvent((id, listener) -> event.addListener(listener));
    }
}
