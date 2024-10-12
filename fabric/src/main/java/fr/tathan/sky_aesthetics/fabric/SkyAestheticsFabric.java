package fr.tathan.sky_aesthetics.fabric;

import net.fabricmc.api.ModInitializer;

import fr.tathan.SkyAesthetics;

public final class SkyAestheticsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SkyAesthetics.init();
    }


}
