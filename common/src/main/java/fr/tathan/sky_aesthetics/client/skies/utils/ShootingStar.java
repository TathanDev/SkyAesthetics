package fr.tathan.sky_aesthetics.client.skies.utils;

import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import fr.tathan.sky_aesthetics.client.skies.record.Star;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.world.phys.Vec3;

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
            if (starConfig.rotation().get() == 0) {
                this.rotation = new Random().nextInt(360);
            } else {
                this.rotation = starConfig.rotation().get();
            }
        } else {
            this.rotation = 0;
        }
    }

    private VertexBuffer createStar(Vec3 color) {
        Tesselator tesselator = Tesselator.getInstance();
        RenderSystem.setShader(CoreShaders.POSITION_COLOR);

        VertexBuffer vertexBuffer = new VertexBuffer(BufferUsage.STATIC_WRITE);
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Random random = new Random();


        Vec3 randomPos = new Vec3(random.nextFloat() * 2.0F - 1.0F, random.nextFloat() * 2.0F - 1.0F, random.nextFloat() * 2.0F - 1.0F);
        StarHelper.createStar(randomPos, color, starConfig.scale(), random, bufferBuilder);
        vertexBuffer.bind();
        vertexBuffer.upload(bufferBuilder.buildOrThrow());
        VertexBuffer.unbind();

        return vertexBuffer;
    }

    public boolean render(PoseStack poseStack) {
        life += this.starConfig.speed();
        if (life >= lifeTime) {
            return true;
        }
        RenderSystem.setShader(CoreShaders.POSITION_COLOR);

        RenderSystem.setShaderColor(5f, 4f, 5f, 5f);

        poseStack.pushPose();

        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(life + 180));

        this.starBuffer.bind();
        this.starBuffer.drawWithShader(poseStack.last().pose(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
        VertexBuffer.unbind();
        poseStack.popPose();
        return false;

    }
}
