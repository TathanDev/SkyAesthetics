package fr.tathan.sky_aesthetics.client.screens.editor.export;

import fr.tathan.SkyAesthetics;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;

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
            cached = PackFormat.of(SharedConstants.RESOURCE_PACK_FORMAT_MAJOR, SharedConstants.RESOURCE_PACK_FORMAT_MINOR);
        }
        return cached;
    }
}
