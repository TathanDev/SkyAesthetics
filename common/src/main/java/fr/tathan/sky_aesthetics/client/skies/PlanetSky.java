package fr.tathan.sky_aesthetics.client.skies;

import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.record.SkyProperties;
import fr.tathan.sky_aesthetics.client.skies.renderer.SkyRenderer;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public float @NotNull [] getSunriseColor(float f, float g) {
        float[] color = super.getSunriseColor(f, g);

        this.properties.sunriseColor().ifPresent(sunriseColor -> {
            this.sunriseCol[0] = sunriseColor.x();
            this.sunriseCol[1] = sunriseColor.y();
            this.sunriseCol[2] = sunriseColor.z();
            this.sunriseCol[3] = sunriseColor.w();

        });
        return color;
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
