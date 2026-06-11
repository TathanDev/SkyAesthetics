package fr.tathan.sky_aesthetics.client.screens.editor.export;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import com.mojang.serialization.JsonOps;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.screens.editor.model.EditablePack;
import fr.tathan.sky_aesthetics.client.screens.editor.model.EditableSky;
import fr.tathan.sky_aesthetics.client.settings.SkyProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Writes an {@link EditablePack} to a resource pack folder under the client's
 * {@code resourcepacks/} directory. Produces a {@code pack.mcmeta}, an optional {@code pack.png}
 * icon, and one sky JSON per sky at {@code assets/<namespace>/sky_aesthetics/<path>.json}.
 */
public final class SkyPackExporter {

    private SkyPackExporter() {
    }

    /**
     * @param success      whether every file was written
     * @param packPath     the pack folder on disk (may be null if it could not be resolved)
     * @param folderName   the pack folder name (used to enable the pack afterwards)
     * @param writtenFiles files successfully written
     * @param errors       human-readable error messages (empty on success)
     */
    public record Result(boolean success, Path packPath, String folderName, List<Path> writtenFiles, List<String> errors) {
    }

    public static Result export(EditablePack pack) {
        List<String> errors = new ArrayList<>(pack.validate());
        List<Path> written = new ArrayList<>();

        Path packDir = Minecraft.getInstance().getResourcePackDirectory();

        String folderName = pack.folderName();
        Path packPath = packDir.resolve(folderName);

        if (!errors.isEmpty()) {
            return new Result(false, packPath, folderName, written, errors);
        }

        try {
            Files.createDirectories(packPath);

            // Remove any sky JSON files from a previous export
            Path assetsDir = packPath.resolve("assets");
            if (Files.exists(assetsDir)) deleteDirectory(assetsDir);

            // pack.mcmeta
            writePackMeta(packPath.resolve("pack.mcmeta"), pack.description);
            written.add(packPath.resolve("pack.mcmeta"));

            // optional icon
            if (pack.iconPath.isPresent()) {
                try {
                    Path icon = packPath.resolve("pack.png");
                    Files.copy(pack.iconPath.get(), icon, StandardCopyOption.REPLACE_EXISTING);
                    written.add(icon);
                } catch (Exception e) {
                    errors.add("Failed to copy pack icon: " + e.getMessage());
                }
            }

            // skies
            for (EditableSky editable : pack.skies) {
                SkyProperties props = editable.toProperties();
                Identifier id = props.id();
                Path skyFile = packPath
                        .resolve("assets")
                        .resolve(id.getNamespace())
                        .resolve(SkyAesthetics.MODID)
                        .resolve(id.getPath() + ".json");

                JsonElement json = SkyProperties.CODEC
                        .encodeStart(JsonOps.INSTANCE, props)
                        .getOrThrow(msg -> new IllegalStateException("Failed to encode sky '" + id + "': " + msg));

                writeJson(skyFile, json);
                written.add(skyFile);
            }
        } catch (Exception e) {
            SkyAesthetics.LOG.error("Failed to export sky pack '{}'", folderName, e);
            errors.add(e.getMessage() == null ? e.toString() : e.getMessage());
            deleteDirectory(packPath);
            return new Result(false, packPath, folderName, written, errors);
        }

        return new Result(errors.isEmpty(), packPath, folderName, written, errors);
    }

    private static void writePackMeta(Path path, String description) throws Exception {
        PackFormat fmt = PackFormatHelper.currentResourcePackFormat();
        JsonArray fmtArray = new JsonArray();
        fmtArray.add(fmt.major());
        fmtArray.add(fmt.minor());

        JsonObject pack = new JsonObject();
        pack.add("min_format", fmtArray);
        pack.add("max_format", fmtArray);
        pack.addProperty("description", description == null ? "" : description);

        JsonObject root = new JsonObject();
        root.add("pack", pack);
        writeJson(path, root);
    }

    private static void writeJson(Path path, JsonElement element) throws Exception {
        Files.createDirectories(path.getParent());
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            SkyAesthetics.GSON.toJson(element, writer);
        }
    }

    private static void deleteDirectory(Path dir) {
        if (!Files.exists(dir)) return;
        try (var stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.delete(p); } catch (IOException ignored) {}
            });
        } catch (IOException e) {
            SkyAesthetics.LOG.warn("Failed to clean up partial pack at '{}'", dir, e);
        }
    }
}
