package fr.tathan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import fr.tathan.sky_aesthetics.client.data.ConstellationsData;
import fr.tathan.sky_aesthetics.client.data.SkyPropertiesData;
import fr.tathan.sky_aesthetics.helper.IrisCompat;
import fr.tathan.sky_aesthetics.helper.PlatformHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

public final class SkyAesthetics {
    public static final String MODID = "sky_aesthetics";
    public static final Logger LOG = LoggerFactory.getLogger("Sky Aesthetics");

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create();

    public static void init() {
        if(PlatformHelper.isModLoaded("iris")) {
            IrisCompat.initIrisCompat();
        }
    }

    public static void onAddReloadListenerEvent(BiConsumer<ResourceLocation, PreparableReloadListener> registry) {
        registry.accept(ResourceLocation.fromNamespaceAndPath(MODID, "constellation"), new ConstellationsData());
        registry.accept(ResourceLocation.fromNamespaceAndPath(MODID, "sky_renderer"), new SkyPropertiesData());
    }
}
