package fr.tathan.sky_aesthetics.client.skies;

import fr.tathan.sky_aesthetics.client.skies.record.SkyProperties;
import fr.tathan.sky_aesthetics.client.skies.renderer.SkyRenderer;
import fr.tathan.sky_aesthetics.client.skies.utils.SkyHelper;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class PlanetSky extends DimensionSpecialEffects {
    private final SkyRenderer renderer;
    private final SkyProperties properties;

    public PlanetSky(SkyProperties properties) {
        super(properties.cloudSettings().cloudHeight(), true, SkyType.valueOf(properties.skyType()), false, false);
        this.properties = properties;
        this.renderer = new SkyRenderer(properties, this);
    }



    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {

        return fogColor.multiply(brightness * 0.94F + 0.06F, brightness * 0.94F + 0.06F, brightness * 0.91F + 0.09F);
    }

    //Like the overworld
    @Override
    public boolean isSunriseOrSunset(float f) {
        float g = Mth.cos(f * ((float)Math.PI * 2F));
        return g >= -0.4F && g <= 0.4F;
    }

    public int getDefaultSunriseOrSunsetColor(float f) {
        float g = Mth.cos(f * ((float)Math.PI * 2F));
        float h = g / 0.4F * 0.5F + 0.5F;
        float i = Mth.square(1.0F - (1.0F - Mth.sin(h * (float)Math.PI)) * 0.99F);
        return ARGB.colorFromFloat(i, h * 0.3F + 0.7F, h * h * 0.7F + 0.2F, 0.2F);
    }

    @Override
    public int getSunriseOrSunsetColor(float timeOfDay) {

        AtomicInteger sunriseCol = new AtomicInteger(this.getDefaultSunriseOrSunsetColor(timeOfDay));

        this.properties.sunriseColor().ifPresent(sunriseColor -> {
            float g = Mth.cos(timeOfDay * (float) (Math.PI * 2));

            if (g >= -0.4f && g <= 0.4f) {
                float i = g / 0.4f * 0.5f + 0.5f;
                float alpha = 1 - (1 - Mth.sin(i * (float) Math.PI)) * 0.99F;
                alpha *= alpha;

                if (this.properties.sunriseModifier().isPresent()) alpha *= this.properties.sunriseModifier().get();
                sunriseCol.set(ARGB.colorFromFloat(sunriseColor.x / 255f, sunriseColor.y / 255f, sunriseColor.z / 255f, alpha));

            }
        });

        return sunriseCol.get();
    }

    @Override
    public boolean hasGround() {
        return SkyHelper.skyTypeToHasGround(properties.skyType());
    }

    @Override
    public SkyType skyType() {
        return switch (properties.skyType()) {
            case "END" -> SkyType.END;
            case "NONE" -> SkyType.NONE;
            case null, default -> SkyType.OVERWORLD;
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
