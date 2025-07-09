package fr.tathan.sky_aesthetics.client.skies.settings;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.tathan.sky_aesthetics.client.skies.utils.ShootingStar;
import fr.tathan.sky_aesthetics.client.skies.utils.StarHelper;
import net.minecraft.Util;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3i;

import java.util.*;

public record StarSettings(boolean vanilla, boolean movingStars, int count, boolean allDaysVisible, float scale, Vector3i color, Optional<ShootingStars> shootingStars, Optional<ResourceLocation> starsTexture) {

    public static final Codec<StarSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("vanilla").forGetter(StarSettings::vanilla),
            Codec.BOOL.fieldOf("moving_stars").forGetter(StarSettings::movingStars),
            Codec.INT.fieldOf("count").forGetter(StarSettings::count),
            Codec.BOOL.fieldOf("all_days_visible").forGetter(StarSettings::allDaysVisible),
            Codec.FLOAT.fieldOf("scale").forGetter(StarSettings::scale),
            SkyColorSettings.VEC3I.fieldOf("color").forGetter(StarSettings::color),
            ShootingStars.CODEC.optionalFieldOf("shooting_stars").forGetter(StarSettings::shootingStars),
            ResourceLocation.CODEC.optionalFieldOf("star_texture").forGetter(StarSettings::starsTexture)
    ).apply(instance, StarSettings::new));

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

    public static StarSettings createDefaultStars() {
        return new StarSettings(true, false, 30000, false, 0.05f, new Vector3i(255, 255, 255), Optional.empty(), Optional.empty());
    }

    public VertexBuffer getStarsBuffer() {
        if(this.count() > 100) {
            return StarHelper.createStars(this.scale(), this.count(), this.color().x(), this.color().y(), this.color().z(), Optional.empty(), Optional.empty());
        } else if (this.vanilla() ){
            return StarHelper.createVanillaStars();
        }
        return null;
    }

    public void renderStars(ClientLevel level, float partialTick, PoseStack poseStack, Matrix4f projectionMatrix, float nightAngle, VertexBuffer starBuffer) {
        float starLight = level.getStarBrightness(partialTick) * (1.0f - level.getRainLevel(partialTick));

        if(starBuffer == null) return;

        if (this.vanilla()) {
            if (starLight > 0.0f) {
                RenderSystem.setShaderColor(starLight, starLight, starLight, starLight);
                FogRenderer.setupNoFog();
                starBuffer.bind();
                starBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionShader());
                VertexBuffer.unbind();
            }
            return;
        }

        float starsAngle = !this.movingStars() ? -90f : nightAngle;

        if (this.allDaysVisible()) {
            if(this.starsTexture().isPresent()) {
                RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            } else {
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
            }

            RenderSystem.setShaderColor(starLight + 1f, starLight + 1f, starLight + 1f, starLight + 1f);
            StarHelper.drawStars(starBuffer, poseStack, projectionMatrix, starsAngle, this.starsTexture());
        } else if (starLight > 0.2F) {
            if(this.starsTexture().isPresent()) {
                RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            } else {
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
            }
            RenderSystem.setShaderColor(starLight + 0.5f, starLight + 0.5f, starLight + 0.5f, starLight + 0.5f);
            StarHelper.drawStars(starBuffer, poseStack, projectionMatrix, starsAngle, this.starsTexture());
        }
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



}
