package fr.tathan.quilt;

import net.fabricmc.api.ModInitializer;
import org.quiltmc.loader.api.ModContainer;

import fr.tathan.SkyAesthetics;

public final class ExampleModQuilt implements ModInitializer {

    @Override
    public void onInitialize() {
        SkyAesthetics.init();

    }
}
