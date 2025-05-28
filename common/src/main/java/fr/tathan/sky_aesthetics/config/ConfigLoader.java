package fr.tathan.sky_aesthetics.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.helper.PlatformHelper;
import net.minecraft.util.GsonHelper;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ConfigLoader {

    public static SkyConfig loadOrGenerateConfig() {
        Path configPath = PlatformHelper.getConfigPath().resolve("sky-aesthetics.json");
        SkyConfig defaultConfig = new SkyConfig(List.of("bad_sky:sky_to_disable"));

        try {
            BufferedReader reader = Files.newBufferedReader(configPath);
            JsonElement jsonElement = GsonHelper.parse(reader);
            JsonObject json = GsonHelper.convertToJsonObject(jsonElement, "skies");
            Optional<SkyConfig> config = SkyConfig.CODEC.parse(JsonOps.INSTANCE, json).result();


            if(config.isPresent()) {
                SkyAesthetics.LOG.error("Loaded config from file {}", config.get().disabledSkies());
                return config.get();
            }

        } catch (Exception e) {

            if (!(e instanceof NoSuchFileException))
                e.printStackTrace();

            try {
                File folder = configPath.toFile().getParentFile();
                if (!folder.exists())
                    folder.mkdirs();

                JsonElement jsonElement = defaultConfig.toJson();
                String systemFile = SkyAesthetics.GSON.toJson(jsonElement);

                var systemWrite = Files.newBufferedWriter(configPath);
                systemWrite.write(systemFile);
                systemWrite.close();

                return defaultConfig;

            } catch (Exception e1) {
                e1.printStackTrace();
            }

        }

        return defaultConfig;
    }

}
