package fr.tathan.sky_aesthetics.client;

import fr.tathan.SkyAesthetics;import fr.tathan.sky_aesthetics.client.data.ConstellationsData;
import fr.tathan.sky_aesthetics.client.data.SkiesRegistry;
import fr.tathan.sky_aesthetics.client.registry.RenderPipelineRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.function.BiConsumer;

public class SkyAestheticClients {


    public static void init() {
        RenderPipelineRegistry.init();
    }


    public static void onAddReloadListenerEvent(BiConsumer<Identifier, PreparableReloadListener> registry) {
        registry.accept(Identifier.fromNamespaceAndPath(SkyAesthetics.MODID, "constellation"), new ConstellationsData());
        registry.accept(Identifier.fromNamespaceAndPath(SkyAesthetics.MODID, "sky_aesthetics"), new SkiesRegistry());
    }
}
