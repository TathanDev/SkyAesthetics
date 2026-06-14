package fr.tathan.sky_aesthetics.neoforge;

import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.SkyAestheticClients;

import fr.tathan.sky_aesthetics.client.screens.editor.SkyEditorEntry;
import fr.tathan.exoconfig.platform.PlatformClientHelper;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod(SkyAesthetics.MODID)
public final class SkyAestheticsNeoForge {

    public SkyAestheticsNeoForge() {
        SkyAesthetics.init();

    }

    @EventBusSubscriber(modid = SkyAesthetics.MODID, value= Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onReloadListener(AddClientReloadListenersEvent event) {
            SkyAesthetics.onAddReloadListenerEvent(event::addListener);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            SkyAestheticClients.init();

            PlatformClientHelper.registerConfigScreen(SkyAesthetics.MODID, SkyAesthetics.CONFIG);
        }

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(SkyEditorEntry.OPEN_EDITOR);
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            SkyEditorEntry.handleKeyInput();
        }

        @SubscribeEvent
        public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
            event.getDispatcher().register(Commands.literal("skyaesthetics")
                    .then(Commands.literal("editor").executes(ctx -> {
                        SkyEditorEntry.openEditor();
                        return 1;
                    })));
        }
    }

}
