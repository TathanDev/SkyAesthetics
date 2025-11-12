package fr.tathan.sky_aesthetics.client.skies;

import fr.tathan.sky_aesthetics.client.DimensionRenderer;
import fr.tathan.sky_aesthetics.client.skies.settings.SkyProperties;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class DimensionSky extends DimensionSpecialEffects {
    private final DimensionRenderer renderer;

    private final ResourceKey<Level> dimension;
    private final ResourceLocation skyId;
    private final SkyProperties skyProperties;

    public DimensionSky(SkyProperties skyProperties) {
        this(skyProperties.world(), skyProperties.id(), skyProperties.toDimensionRenderer(), skyProperties);
    }

    public DimensionSky(ResourceKey<Level> dimension, ResourceLocation skyId, DimensionRenderer renderer, SkyProperties skyProperties) {
        super(192, true, SkyType.OVERWORLD, false, false);
        this.renderer = renderer;
        this.dimension = dimension;
        this.skyId = skyId;
        this.skyProperties = skyProperties;
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return fogColor.multiply(brightness * 0.94F + 0.06F, brightness * 0.94F + 0.06F, brightness * 0.91F + 0.09F);
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

        this.renderer.skyColor.sunsetColor().ifPresent(sunriseColor -> {
            float g = Mth.cos(timeOfDay * (float) (Math.PI * 2));

            if (g >= -0.4f && g <= 0.4f) {
                float i = g / 0.4f * 0.5f + 0.5f;
                float alpha = 1 - (1 - Mth.sin(i * (float) Math.PI)) * 0.99F;
                alpha *= alpha;

                if (this.renderer.skyColor.sunriseAlphaModifier().isPresent()) alpha *= this.renderer.skyColor.sunriseAlphaModifier().get();
                sunriseCol.set(ARGB.colorFromFloat(sunriseColor.x / 255f, sunriseColor.y / 255f, sunriseColor.z / 255f, alpha));

            }
        });

        return sunriseCol.get();
    }

    @Override
    public float getCloudHeight() {
        return this.getRenderer().cloudSettings.cloudHeight();
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Override
    public boolean forceBrightLightmap() {
        if(this.getSkyProperties().lightSettings().isPresent()) {
            return this.getSkyProperties().lightSettings().get().forceBrightLightmap();
        }
        return super.forceBrightLightmap();
    }

    @Override
    public boolean constantAmbientLight() {
        if(this.getSkyProperties().lightSettings().isPresent()) {
            return this.getSkyProperties().lightSettings().get().constantAmbientLight();
        }
        return super.constantAmbientLight();
    }

    /** Getter **/
    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public ResourceLocation getSkyId() {
        return skyId;
    }

    public SkyProperties getSkyProperties() {
        return skyProperties;
    }

    public DimensionRenderer getRenderer() {
        return renderer;
    }

}
