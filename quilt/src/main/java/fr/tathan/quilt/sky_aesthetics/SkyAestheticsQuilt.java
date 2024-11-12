package fr.tathan.quilt.sky_aesthetics;

import fr.tathan.sky_aesthetics.client.data.SkyPropertiesData;
import net.fabricmc.api.ModInitializer;

import fr.tathan.SkyAesthetics;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;

public final class SkyAestheticsQuilt  implements ModInitializer {
    @Override
    public void onInitialize() {
        SkyAesthetics.init();
        SkyAesthetics.LOG.error("Welcome Quilt !");
        ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(new SkyPropertiesData());

    }
}
