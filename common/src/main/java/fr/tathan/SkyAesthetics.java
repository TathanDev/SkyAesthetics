package fr.tathan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import com.google.gson.ToNumberPolicy;
import fr.tathan.exoconfig.common.loader.ConfigsRegistry;
import fr.tathan.sky_aesthetics.client.data.ConstellationsData;
import fr.tathan.sky_aesthetics.client.data.SkiesRegistry;
import fr.tathan.sky_aesthetics.client.registry.RenderPipelineRegistry;
import fr.tathan.sky_aesthetics.config.SkyConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

public final class SkyAesthetics {
    public static final String MODID = "sky_aesthetics";
    public static final Logger LOG = LoggerFactory.getLogger("Sky Aesthetics");
    public static SkyConfig CONFIG = new SkyConfig();

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .setStrictness(Strictness.LENIENT)
            .create();

    public static void init() {
        CONFIG = ConfigsRegistry.getInstance().registerConfig(new SkyConfig(), CONFIG);
        RenderPipelineRegistry.init();
    }


    public static void onAddReloadListenerEvent(BiConsumer<Identifier, PreparableReloadListener> registry) {
        registry.accept(Identifier.fromNamespaceAndPath(MODID, "constellation"), new ConstellationsData());
        registry.accept(Identifier.fromNamespaceAndPath(MODID, "sky_aesthetics"), new SkiesRegistry());
    }
}
