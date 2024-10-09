package fr.tathan.fabric.sky_aesthetics;

import net.fabricmc.api.ModInitializer;

import fr.tathan.SkyAesthetics;

public final class SkyAestheticsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SkyAesthetics.init();
    }


}
