package fr.tathan.sky_aesthetics.fabric.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fr.tathan.SkyAesthetics;
import fr.tathan.exoconfig.client.screen.ConfigScreen;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (previous) -> new ConfigScreen<>(previous, SkyAesthetics.CONFIG);
    }
}