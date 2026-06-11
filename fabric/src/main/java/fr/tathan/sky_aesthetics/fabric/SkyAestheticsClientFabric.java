package fr.tathan.sky_aesthetics.fabric;

import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.screens.editor.SkyEditorEntry;
import fr.tathan.exoconfig.platform.PlatformClientHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class SkyAestheticsClientFabric implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        PlatformClientHelper.registerConfigScreen(SkyAesthetics.MODID, SkyAesthetics.CONFIG);

        onAddReloadListener();
        registerEditorEntryPoints();
    }

    private static void registerEditorEntryPoints() {
        KeyMappingHelper.registerKeyMapping(SkyEditorEntry.OPEN_EDITOR);
        ClientTickEvents.END_CLIENT_TICK.register(client -> SkyEditorEntry.handleKeyInput());
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommands.literal("skyaesthetics")
                        .then(ClientCommands.literal("editor").executes(ctx -> {
                            SkyEditorEntry.openEditor();
                            return 1;
                        }))));
    }

    public static void onAddReloadListener() {
        SkyAesthetics.onAddReloadListenerEvent((id, listener) -> ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public CompletableFuture<Void> reload(SharedState sharedState, Executor exectutor, PreparationBarrier barrier, Executor applyExectutor) {
                return listener.reload(sharedState, exectutor, barrier, applyExectutor);
            }


            @Override
            public Identifier getFabricId() {
                return id;
            }
        }));
    }
}
