package fr.tathan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import com.google.gson.ToNumberPolicy;
import fr.tathan.exoconfig.common.loader.ConfigsRegistry;
import fr.tathan.sky_aesthetics.config.SkyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    }


}
