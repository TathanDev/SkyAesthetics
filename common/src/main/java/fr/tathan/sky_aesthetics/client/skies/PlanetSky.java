package fr.tathan.sky_aesthetics.client.skies;

import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.record.SkyProperties;
import fr.tathan.sky_aesthetics.client.skies.renderer.SkyRenderer;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class PlanetSky extends DimensionSpecialEffects {
    private final SkyRenderer renderer;
    private final SkyProperties properties;

    public PlanetSky(SkyProperties properties) {
        super(properties.cloudHeight(), true, SkyType.valueOf(properties.skyType()), false, false);
        this.properties = properties;
        this.renderer = new SkyRenderer(properties);
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return getProperties().fog() ? fogColor.multiply(brightness * 0.94F + 0.06F, brightness * 0.94F + 0.06F, brightness * 0.91F + 0.09F) : fogColor;
    }

    @Override
    public @Nullable float[]  getSunriseColor(float timeOfDay, float partialTicks) {
        this.properties.sunriseColor().ifPresent(sunriseColor -> {
            float g = Mth.cos(timeOfDay * (float) (Math.PI * 2));

            if (g >= -0.4f && g <= 0.4f) {
                float i = g / 0.4f * 0.5f + 0.5f;
                float alpha = 1 - (1 - Mth.sin(i * (float) Math.PI)) * 0.99F;
                alpha *= alpha;

                if (this.properties.sunriseModifier().isPresent()) alpha *= this.properties.sunriseModifier().get();
                if(this.sunriseCol == null) this.sunriseCol = new float[4];

                this.sunriseCol[0] = i * 0.3f + (int) sunriseColor.x / 255f * 0.7f;
                this.sunriseCol[1] = i * i * 0.7f + (int) sunriseColor.y / 255f * 0.5f;
                this.sunriseCol[2] = (int) sunriseColor.z / 255f * 0.6f;
                this.sunriseCol[3] = alpha;
            }
        });

        return this.sunriseCol;
    }


    @Override
    public SkyType skyType() {
        return switch (properties.skyType()) {
            case "END" -> SkyType.END;
            case "NONE" -> SkyType.NONE;
            case null, default -> SkyType.NORMAL;
        };
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }


    public SkyRenderer getRenderer() {
        return renderer;
    }

    public SkyProperties getProperties() {
        return properties;
    }
}
