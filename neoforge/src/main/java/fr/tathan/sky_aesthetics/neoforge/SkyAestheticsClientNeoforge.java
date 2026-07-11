package fr.tathan.sky_aesthetics.neoforge;

import fr.tathan.SkyAesthetics;
import fr.tathan.exoconfig.platform.PlatformClientHelper;
import fr.tathan.sky_aesthetics.client.SkyAestheticClients;
import fr.tathan.sky_aesthetics.client.screens.editor.SkyEditorEntry;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = SkyAesthetics.MODID, value= Dist.CLIENT)
public class SkyAestheticsClientNeoforge {


    @SubscribeEvent
    public static void onReloadListener(AddClientReloadListenersEvent event) {
        SkyAestheticClients.onAddReloadListenerEvent(event::addListener);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(SkyAestheticClients::init);

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
