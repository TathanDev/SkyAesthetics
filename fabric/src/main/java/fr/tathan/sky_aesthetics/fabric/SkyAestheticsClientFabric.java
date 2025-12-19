package fr.tathan.sky_aesthetics.fabric;

import fr.tathan.SkyAesthetics;
import fr.tathan.exoconfig.platform.PlatformClientHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class SkyAestheticsClientFabric implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        PlatformClientHelper.registerConfigScreen(SkyAesthetics.MODID, SkyAesthetics.CONFIG);

        onAddReloadListener();
    }

    public static void onAddReloadListener() {
        SkyAesthetics.onAddReloadListenerEvent((id, listener) -> ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, Executor backgroundExecutor, Executor gameExecutor) {
                return listener.reload(barrier, manager, backgroundExecutor, gameExecutor);
            }

            @Override
            public Identifier getFabricId() {
                return id;
            }
        }));
    }
}
