package fr.tathan.sky_aesthetics.fabric;

import net.fabricmc.api.ClientModInitializer;

public final class SkyAestheticsClientFabric implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        //PlatformClientHelper.registerConfigScreen(SkyAesthetics.MODID, SkyAesthetics.CONFIG);

        //onAddReloadListener();
    }

//    public static void onAddReloadListener() {
//        SkyAesthetics.onAddReloadListenerEvent((id, listener) -> ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
//            @Override
//            public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, Executor backgroundExecutor, Executor gameExecutor) {
//                return listener.reload(barrier, manager, backgroundExecutor, gameExecutor);
//            }
//
//            @Override
//            public Identifier getFabricId() {
//                return id;
//            }
//        }));
//    }
}
