package fr.tathan.sky_aesthetics.client;

import net.minecraft.client.renderer.fog.FogData;

/**
 * Holds the most recently computed vanilla FogData so that sky renderers can
 * use the dynamically-computed fog color while still overriding density.
 * Updated by FogRendererMixin each frame before the frame graph executes.
 */
public final class FogDataCapture {
    private static FogData last;

    private FogDataCapture() {}

    public static void capture(FogData data) {
        last = data;
    }

    public static FogData getLast() {
        return last;
    }
}
