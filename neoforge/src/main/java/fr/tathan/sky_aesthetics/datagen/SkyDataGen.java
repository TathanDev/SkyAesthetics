package fr.tathan.sky_aesthetics.datagen;

import fr.tathan.SkyAesthetics;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = SkyAesthetics.MODID, bus = EventBusSubscriber.Bus.MOD)
public class SkyDataGen {


    @SubscribeEvent
    static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();


        generator.addProvider(
                event.includeClient(),
                new SkyPropertiesProvider(output)
        );



    }

}
