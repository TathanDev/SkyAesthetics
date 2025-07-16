package fr.tathan.sky_aesthetics.client.skies;

import fr.tathan.sky_aesthetics.client.DimensionRenderer;
import fr.tathan.sky_aesthetics.client.skies.settings.SkyProperties;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

public class DimensionSky extends DimensionSpecialEffects {
    private final DimensionRenderer renderer;

    private final ResourceKey<Level> dimension;
    private final ResourceLocation skyId;
    private final SkyProperties skyProperties;

    public DimensionSky(SkyProperties skyProperties) {
        this(skyProperties.world(), skyProperties.id(), skyProperties.toDimensionRenderer(), skyProperties);
    }

    public DimensionSky(ResourceKey<Level> dimension, ResourceLocation skyId, DimensionRenderer renderer, SkyProperties skyProperties) {
        super(192, true, SkyType.NORMAL, false, false);
        this.renderer = renderer;
        this.dimension = dimension;
        this.skyId = skyId;
        this.skyProperties = skyProperties;
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return fogColor.multiply(brightness * 0.94F + 0.06F, brightness * 0.94F + 0.06F, brightness * 0.91F + 0.09F);
    }

    @Override
    public float @NotNull[] getSunriseColor(float timeOfDay, float partialTicks) {

        if(this.renderer.skyColor.sunsetColor().isPresent()) {
            Vector3i sunriseColor = this.renderer.skyColor.sunsetColor().get();
            float dayTime = Mth.cos(timeOfDay * (float) (Math.PI * 2));

            if (dayTime >= -0.4f && dayTime <= 0.4f) {
                float i = dayTime / 0.4f * 0.5f + 0.5f;
                float alpha = 1 - (1 - Mth.sin(i * (float) Math.PI)) * 0.99F;
                alpha *= alpha;

                if (this.renderer.skyColor.sunriseAlphaModifier().isPresent()) alpha *= this.renderer.skyColor.sunriseAlphaModifier().get();
                if(this.sunriseCol == null) this.sunriseCol = new float[4];

                this.sunriseCol[0] = sunriseColor.x / 255f ;
                this.sunriseCol[1] = sunriseColor.y / 255f ;
                this.sunriseCol[2] = sunriseColor.z / 255f;
                this.sunriseCol[3] = alpha * 1.5f;
                return this.sunriseCol;
            }
        }
        if (this.sunriseCol == null) {
            this.sunriseCol = new float[4];
        }
        this.sunriseCol = super.getSunriseColor(timeOfDay, partialTicks);
        return this.sunriseCol;
    }

    @Override
    public float getCloudHeight() {
        return this.getRenderer().cloudSettings.cloudHeight();
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public ResourceLocation getSkyId() {
        return skyId;
    }

    public SkyProperties getSkyProperties() {
        return skyProperties;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }


    public DimensionRenderer getRenderer() {
        return renderer;
    }

}
