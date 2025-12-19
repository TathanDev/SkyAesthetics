package fr.tathan.sky_aesthetics.client.skies.settings;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.tathan.sky_aesthetics.client.skies.utils.ShootingStar;
import fr.tathan.sky_aesthetics.client.skies.utils.StarHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3i;

import java.util.*;

/**
 * The class containing information about stars
 * @param vanilla If the stars should be like vanilla one's
 * @param movingStars if the stars should move
 * @param count The count of stars
 * @param allDaysVisible If stars should be visible all day
 * @param scale The size of a star
 * @param color The color of the stars
 * @param shootingStars Shooting Star Settings
 * @param starsTexture The texture of the star
 */
public record StarSettings(boolean vanilla, boolean movingStars, int count, boolean allDaysVisible, float scale, Vector3i color, Optional<ShootingStars> shootingStars, Optional<Identifier> starsTexture) {

    public static final Codec<StarSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("vanilla").forGetter(StarSettings::vanilla),
            Codec.BOOL.fieldOf("moving_stars").forGetter(StarSettings::movingStars),
            Codec.INT.fieldOf("count").forGetter(StarSettings::count),
            Codec.BOOL.fieldOf("all_days_visible").forGetter(StarSettings::allDaysVisible),
            Codec.FLOAT.fieldOf("scale").forGetter(StarSettings::scale),
            SkyColorSettings.VEC3I.fieldOf("color").forGetter(StarSettings::color),
            ShootingStars.CODEC.optionalFieldOf("shooting_stars").forGetter(StarSettings::shootingStars),
            Identifier.CODEC.optionalFieldOf("star_texture").forGetter(StarSettings::starsTexture)
    ).apply(instance, StarSettings::new));



    public static StarSettings createDefaultStars() {
        return new StarSettings(true, false, 30000, false, 0.05f, new Vector3i(255, 255, 255), Optional.empty(), Optional.empty());
    }

    public GpuBuffer getStarsBuffer() {
        if (this.vanilla() ){
            return StarHelper.createVanillaStars();
        }
        if(this.count() > 100) {
            return StarHelper.createStars(this.scale(), this.count(), this.color().x(), this.color().y(), this.color().z(), Optional.empty(), Optional.empty());
        }
        return null;
    }

    public void renderStars(ClientLevel level, float partialTick, PoseStack poseStack, Matrix4f projectionMatrix, FogParameters fog, float nightAngle, VertexBuffer starBuffer, SkyRenderer skyRenderer) {
        float starLight = level.getStarBrightness(partialTick) * (1.0f - level.getRainLevel(partialTick));


        if(starBuffer == null) return;

        if (this.vanilla()) {
            if (starLight > 0.0f) {

                skyRenderer.renderStars(fog, starLight, poseStack);
            }
            return;
        }

        float starsAngle = !this.movingStars() ? -90f : nightAngle;

        if(this.starsTexture().isPresent()) {
            RenderSystem.setShader(CoreShaders.POSITION_TEX_COLOR);
        } else {
            RenderSystem.setShader(CoreShaders.POSITION_COLOR);
        }

        if (this.allDaysVisible()) {
            RenderSystem.setShaderColor(starLight + 1f, starLight + 1f, starLight + 1f, starLight + 1f);
        } else if (starLight > 0.0f) {
            RenderSystem.setShaderColor(starLight, starLight, starLight, starLight);
        }
        StarHelper.drawStars(starBuffer, poseStack, starsAngle, this.starsTexture(), fog);

    }

    public void handleShootingStars(ClientLevel level, PoseStack poseStack, Matrix4f projectionMatrix, StarSettings star, float partialTick, HashMap<UUID, ShootingStar> shootingStars) {
        float starLight = level.getStarBrightness(partialTick) * (1.0f - level.getRainLevel(partialTick));

        if(!star.allDaysVisible() && !(starLight > 0.2F)) {
            if(!shootingStars.isEmpty()) shootingStars.clear();
            return;
        }

        StarSettings.ShootingStars shootingStarConfig = star.shootingStars().get();
        Random random = new Random();
        if (random.nextInt(1001) >= shootingStarConfig.percentage()) {
            UUID starId = UUID.randomUUID();
            var shootingStar = new ShootingStar(random.nextFloat(shootingStarConfig.randomLifetime().x, shootingStarConfig.randomLifetime().y), shootingStarConfig,  starId);
            shootingStars.putIfAbsent(starId, shootingStar);
        }

        if(this.shootingStars.isEmpty()) return;
        ArrayList<UUID> starsToRemove = new ArrayList<>();
        for (ShootingStar shootingStar : shootingStars.values()) {
            if (shootingStar.render(poseStack, projectionMatrix)) {
                starsToRemove.add(shootingStar.starId);
            }
        }
        starsToRemove.forEach(shootingStars::remove);
    }

    /**
     *
     * @param percentage The chance for a shooting star to happen
     * @param randomLifetime The random lifetime of star
     * @param scale The scale of the star
     * @param speed The speed of the star
     * @param color The color of the star
     * @param rotation The rotation of the star
     */
    public record ShootingStars(int percentage, Vec2 randomLifetime, float scale, float speed, Vec3 color, Optional<Integer> rotation) {

        public static Codec<Vec2> VEC2 = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 2).map((listx) -> new Vec2(listx.getFirst(), listx.get(1))), (vec2) -> List.of(vec2.x, vec2.y));

        public static final Codec<ShootingStars> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("percentage").forGetter(ShootingStars::percentage),
                VEC2.fieldOf("random_lifetime").forGetter(ShootingStars::randomLifetime),
                Codec.FLOAT.fieldOf("scale").forGetter(ShootingStars::scale),
                Codec.FLOAT.fieldOf("speed").forGetter(ShootingStars::speed),
                Vec3.CODEC.fieldOf("color").forGetter(ShootingStars::color),
                Codec.INT.optionalFieldOf("rotation").forGetter(ShootingStars::rotation)
        ).apply(instance, ShootingStars::new));
    }

}
