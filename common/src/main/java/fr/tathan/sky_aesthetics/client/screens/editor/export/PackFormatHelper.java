package fr.tathan.sky_aesthetics.client.screens.editor.export;

import fr.tathan.SkyAesthetics;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;

/**
 * Resolves the current resource-pack {@link PackFormat} for {@code pack.mcmeta}.
 *
 * <p>In 26.1.2 the format is written as {@code min_format}/{@code max_format} arrays
 * {@code [major, minor]} — the old scalar {@code pack_format} key is no longer accepted
 * for formats newer than 64.</p>
 */
public final class PackFormatHelper {

    private static PackFormat cached = null;

    private PackFormatHelper() {
    }

    public static PackFormat currentResourcePackFormat() {
        if (cached != null) return cached;
        try {
            cached = SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES);
        } catch (Throwable t) {
            SkyAesthetics.LOG.warn("Could not detect resource pack format; using fallback", t);
            cached = PackFormat.of(46, 0);
        }
        return cached;
    }
}
