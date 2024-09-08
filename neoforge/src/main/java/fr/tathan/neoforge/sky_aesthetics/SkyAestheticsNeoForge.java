package fr.tathan.neoforge.sky_aesthetics;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.neoforged.fml.common.Mod;
import fr.tathan.SkyAesthetics;

@Mod(SkyAesthetics.MODID)
public final class SkyAestheticsNeoForge {

    public SkyAestheticsNeoForge() {
        SkyAesthetics.init();
        SkyAesthetics.onAddReloadListenerEvent((id, listener) -> {
            ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(listener);

        });
    }


}
