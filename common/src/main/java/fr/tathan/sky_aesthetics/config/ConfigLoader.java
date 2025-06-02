package fr.tathan.sky_aesthetics.config;

import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.helper.PlatformHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class ConfigLoader {

    public static SkyConfig loadOrGenerateDefaults() {
        Path systemsFile =  PlatformHelper.getConfigPath().resolve("sky-aesthetics.json");

        try {
            BufferedReader reader = Files.newBufferedReader(systemsFile);
            SkyConfig config = SkyAesthetics.GSON.fromJson(reader, SkyConfig.class);

            Writer writer = new FileWriter(systemsFile.toFile());
            SkyAesthetics.GSON.toJson(config, writer);
            writer.close();

            return config;

        } catch (Exception e) {

            if (!(e instanceof NoSuchFileException))
                e.printStackTrace();

            try {
                File folder = systemsFile.toFile().getParentFile();
                if (!folder.exists())
                    folder.mkdirs();

                Writer writer = new FileWriter(systemsFile.toFile());
                SkyAesthetics.GSON.toJson(new SkyConfig(), writer);
                writer.close();

            } catch (Exception e1) {
                e1.printStackTrace();
            }

        }

        return new SkyConfig();
    }

}
