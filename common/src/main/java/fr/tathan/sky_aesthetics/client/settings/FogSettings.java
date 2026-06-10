package fr.tathan.sky_aesthetics.client.settings;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.util.Util;
import org.joml.Vector2f;
import org.joml.Vector3i;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Optional;

/**
 * The class containing information about fog settings
 * @param fog Whether fog is enabled
 * @param customFogColor The custom RGB color of the fog (0-255 per channel)
 * @param fogDensity The density settings of the fog (near distance, far distance)
 */
public record FogSettings(Boolean fog, Optional<Vector3i> customFogColor, Optional<Vector2f> fogDensity) {

    public static Codec<Vector2f> VEC2F = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 2).map((listx) -> new Vector2f(listx.getFirst(), listx.getLast())), (vector2f) -> List.of(vector2f.x, vector2f.y));

    public static final Codec<FogSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("fog").forGetter(FogSettings::fog),
            SkyColorSettings.VEC3I.optionalFieldOf("fog_color").forGetter(FogSettings::customFogColor),
            VEC2F.optionalFieldOf("fog_density").forGetter(FogSettings::fogDensity)
    ).apply(instance, FogSettings::new));


    public static FogSettings createDefaultSettings() {
        return new FogSettings(true, Optional.empty(), Optional.empty());
    }

    /**
     * Returns true when a static GPU fog buffer should be pre-built at load time.
     * This covers: fog disabled, or a fully static custom color.
     */
    public boolean needsCustomBuffer() {
        return !this.fog || this.customFogColor.isPresent();
    }

    /**
     * Returns true when the fog buffer must be rebuilt each frame using the vanilla
     * fog color (density-only override — no custom color supplied).
     */
    public boolean needsDynamicBuffer() {
        return this.fog && this.customFogColor.isEmpty() && this.fogDensity.isPresent();
    }

    /**
     * Builds a static GPU fog UBO. Used when fog is disabled or a custom color is
     * explicitly provided. Must be called on the render thread.
     *
     * Layout (std140): vec4 FogColor, float[6] distances
     * (environmentalStart, environmentalEnd, renderDistanceStart,
     *  renderDistanceEnd, FogSkyEnd, FogCloudsEnd).
     */
    public GpuBuffer buildFogBuffer() {
        float r, g, b, a;
        if (this.fog && this.customFogColor.isPresent()) {
            Vector3i color = this.customFogColor.get();
            r = color.x / 255f;
            g = color.y / 255f;
            b = color.z / 255f;
            a = 1.0f;
        } else {
            r = g = b = a = 0f;
        }

        float start, end;
        if (this.fog && this.fogDensity.isPresent()) {
            start = this.fogDensity.get().x;
            end   = this.fogDensity.get().y;
        } else {
            start = Float.MAX_VALUE;
            end   = Float.MAX_VALUE;
        }

        return writeFogBuffer(r, g, b, a, start, end, () -> "Custom sky fog");
    }

    /**
     * Builds a per-frame GPU fog UBO by combining the vanilla fog color with the
     * custom density values. Must be called on the render thread.
     * Only valid when {@link #needsDynamicBuffer()} is true.
     */
    public GpuBuffer buildDynamicFogBuffer(FogData vanillaData) {
        float r = vanillaData.color.x;
        float g = vanillaData.color.y;
        float b = vanillaData.color.z;
        float a = vanillaData.color.w;
        float start = this.fogDensity.get().x;
        float end   = this.fogDensity.get().y;
        return writeFogBuffer(r, g, b, a, start, end, () -> "Dynamic sky fog");
    }

    private static GpuBuffer writeFogBuffer(float r, float g, float b, float a,
                                             float start, float end,
                                             java.util.function.Supplier<String> name) {
        ByteBuffer buf = ByteBuffer.allocateDirect(FogRenderer.FOG_UBO_SIZE).order(ByteOrder.nativeOrder());
        Std140Builder.intoBuffer(buf)
                .putVec4(r, g, b, a)
                .putFloat(start)
                .putFloat(end)
                .putFloat(start)
                .putFloat(end)
                .putFloat(end)
                .putFloat(end);
        buf.flip();
        return RenderSystem.getDevice().createBuffer(name, GpuBuffer.USAGE_UNIFORM, buf);
    }
}
