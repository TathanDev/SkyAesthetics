package fr.tathan.sky_aesthetics.client.skies.settings;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.tathan.SkyAesthetics;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;


public class SkyBoxSetting {

    public static Codec<SkyBoxSetting> CODEC;

    public VertexBuffer sphere;

    public final int gradation;
    public final ResourceLocation texture;
    public final Vector3f rotation;
    public final Optional<Rotation> dynamicRotation;

    public SkyBoxSetting(int gradation, ResourceLocation texture, Vector3f rotation, Optional<Rotation> dynamicRotation) {
        this.gradation = gradation;
        this.texture = texture;
        this.rotation = rotation;
        this.sphere = this.createSphere();
        this.dynamicRotation = dynamicRotation;
    }

    public VertexBuffer createSphere() {
        final float PI = Mth.PI;
        int gradation = this.gradation;

        Tesselator tesselator = Tesselator.getInstance();
        VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);

        for (int i = 0; i < gradation; i++) {
            float alpha1 = i * PI / gradation;
            float alpha2 = (i + 1) * PI / gradation;

            for (int j = 0; j < gradation * 2; j++) {
                float beta1 = j * 2 * PI / (gradation * 2);
                float beta2 = (j + 1) * 2 * PI / (gradation * 2);

                float x1 = (float) (Math.sin(alpha1) * Math.cos(beta1));
                float y1 = (float) (Math.sin(alpha1) * Math.sin(beta1));
                float z1 = (float) Math.cos(alpha1);

                float x2 = (float) (Math.sin(alpha1) * Math.cos(beta2));
                float y2 = (float) (Math.sin(alpha1) * Math.sin(beta2));
                float z2 = (float) Math.cos(alpha1);

                float x3 = (float) (Math.sin(alpha2) * Math.cos(beta1));
                float y3 = (float) (Math.sin(alpha2) * Math.sin(beta1));
                float z3 = (float) Math.cos(alpha2);

                float x4 = (float) (Math.sin(alpha2) * Math.cos(beta2));
                float y4 = (float) (Math.sin(alpha2) * Math.sin(beta2));
                float z4 = (float) Math.cos(alpha2);

                float u1 = beta1 / (2 * PI);
                float v1 = alpha1 / PI;

                float u2 = beta2 / (2 * PI);
                float v2 = alpha1 / PI;

                float u3 = beta1 / (2 * PI);
                float v3 = alpha2 / PI;

                float u4 = beta2 / (2 * PI);
                float v4 = alpha2 / PI;

                bufferBuilder.addVertex(x1, y1, z1).setUv(u1, v1);
                bufferBuilder.addVertex(x2, y2, z2).setUv(u2, v2);
                bufferBuilder.addVertex(x3, y3, z3).setUv(u3, v3);

                bufferBuilder.addVertex(x3, y3, z3).setUv(u3, v3);
                bufferBuilder.addVertex(x2, y2, z2).setUv(u2, v2);
                bufferBuilder.addVertex(x4, y4, z4).setUv(u4, v4);
            }
        }

        vertexBuffer.bind();
        vertexBuffer.upload(bufferBuilder.buildOrThrow());
        VertexBuffer.unbind();
        return vertexBuffer;
    }


    public void renderSkyBox(PoseStack poseStack, Matrix4f projectionMatrix, Camera camera) {
        poseStack.pushPose();

        FogRenderer.setupNoFog();

        RenderSystem.disableDepthTest();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.texture);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1);

        Matrix4f viewMatrix = new Matrix4f();
        camera.rotation().get(viewMatrix);
        viewMatrix.invert(); // Invert to get proper view transformation

        Matrix4f modelMatrix = new Matrix4f().identity().scale(Minecraft.getInstance().gameRenderer.getRenderDistance());
        modelMatrix
                .rotate(Axis.XP.rotationDegrees(this.rotation.x))
                .rotate(Axis.YP.rotationDegrees(this.rotation.y))
                .rotate(Axis.ZP.rotationDegrees(this.rotation.z));

        // Combine view and model matrices
        Matrix4f mvMatrix = new Matrix4f(viewMatrix).mul(modelMatrix);

        this.sphere.bind();
        this.sphere.drawWithShader(mvMatrix, projectionMatrix, GameRenderer.getPositionTexShader());
        VertexBuffer.unbind();

        RenderSystem.enableDepthTest();
        poseStack.popPose();
    }

    public static SkyBoxSetting createDefaultSettings() {
        return new SkyBoxSetting(100, ResourceLocation.parse("sky_aesthetics:textures/skyboxes/venus.png"), new Vector3f(0, 0, 0), Optional.empty());
    }

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("gradation").forGetter((b) -> b.gradation),
                ResourceLocation.CODEC.fieldOf("texture").forGetter((b) -> b.texture),
                SkyObject.VEC3F.fieldOf("rotation").forGetter((b) -> b.rotation),
                Rotation.CODEC.optionalFieldOf("dynamicRotation").forGetter((b) -> b.dynamicRotation)
        ).apply(instance, SkyBoxSetting::new));

    }


}
