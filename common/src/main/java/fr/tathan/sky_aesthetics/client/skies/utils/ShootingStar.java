package fr.tathan.sky_aesthetics.client.skies.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import fr.tathan.sky_aesthetics.client.skies.record.Star;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ShootingStar {

    private final float lifeTime;
    private final Star.ShootingStars starConfig;
    private final VertexBuffer starBuffer;
    public final UUID starId;
    private float life;
    private final int rotation;

    private final float randomSpeedModifier;

    public ShootingStar(float lifeTime, Star.ShootingStars starConfig, UUID starId){

        this.lifeTime = lifeTime;
        this.starConfig = starConfig;
        this.starBuffer = createStar(starConfig.color());
        this.starId = starId;
        this.life = 0;
        this.randomSpeedModifier = new Random().nextInt(-20, 10);

        if(starConfig.rotation().isPresent()) {
            switch (starConfig.rotation().get()) {
                case 0 -> this.rotation = new Random().nextInt(360);
                default -> this.rotation = starConfig.rotation().get();
            }
        } else {
            this.rotation = 0;
        }
    }

    private VertexBuffer createStar(Star.Color color) {
        Tesselator tesselator = Tesselator.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);


        BufferBuilder bufferBuilder;

        if (this.starConfig.location().isPresent()) {
            bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        } else {
            bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        }

        Random random = new Random();


        Vec3 randomPos = new Vec3(random.nextFloat() * 2.0F - 1.0F, random.nextFloat() * 2.0F - 1.0F, random.nextFloat() * 2.0F - 1.0F);
        StarHelper.createStar(randomPos, color, starConfig.scale(), random, bufferBuilder, this.starConfig.location().isPresent());
        vertexBuffer.bind();
        vertexBuffer.upload(bufferBuilder.buildOrThrow());
        VertexBuffer.unbind();

        return vertexBuffer;
    }

    public boolean render(PoseStack poseStack, Matrix4f projectionMatrix) {
        life += this.starConfig.speed();
        if (life >= lifeTime) {
            return true;
        }

        if (!this.starConfig.location().isPresent()) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.setShaderColor(5f, 4f, 5f, 5f);
        }

        poseStack.pushPose();

        if(this.starConfig.location().isPresent()) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, this.starConfig.location().get());
        }

        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(life + 180));

        FogRenderer.setupNoFog();
        this.starBuffer.bind();
        this.starBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionColorShader());
        VertexBuffer.unbind();
        poseStack.popPose();
        return false;

    }
}
