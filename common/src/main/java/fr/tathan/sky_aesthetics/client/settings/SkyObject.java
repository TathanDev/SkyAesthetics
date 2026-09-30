package fr.tathan.sky_aesthetics.client.settings;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.tathan.sky_aesthetics.client.registry.RenderPipelineRegistry;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;

/**
 *
 * @param texture The Object texture
 * @param blend Should the object blend
 * @param size The size of the object
 * @param rotation The position of the object in the sky
 * @param objectRotation The rotation of the object
 * @param height The Object's height
 * @param rotationType The type of rotation DAY, NIGHT or STATIC
 */
public record SkyObject(Identifier texture, boolean blend, float size, Vector3f rotation, Vector3f objectRotation, int height, String rotationType) {

    public static Codec<Vector3f> VEC3F = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 3).map((listx) -> new Vector3f(listx.getFirst(), listx.get(1), listx.getLast())), (vector3f) -> List.of(vector3f.x, vector3f.y, vector3f.z));

    public static final Codec<SkyObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("texture").forGetter(SkyObject::texture),
            Codec.BOOL.fieldOf("blend").forGetter(SkyObject::blend),
            Codec.FLOAT.fieldOf("size").forGetter(SkyObject::size),
            VEC3F.fieldOf("rotation").forGetter(SkyObject::rotation),
            VEC3F.fieldOf("object_rotation").forGetter(SkyObject::objectRotation),
            Codec.INT.fieldOf("height").forGetter(SkyObject::height),
            Codec.STRING.fieldOf("rotation_type").forGetter(SkyObject::rotationType)
    ).apply(instance, SkyObject::new));

    /**
     * Set the position of the object in the sky
     * @param poseStack
     * @param dayAngle
     */
    public void setObjectPosition(PoseStack poseStack, float dayAngle) {

        poseStack.rotate(Axis.YP.rotationDegrees(this.rotation().y));
        if(Objects.equals(this.rotationType(), "DAY")) {
            poseStack.rotate(Axis.XP.rotation(dayAngle));
        } else if(Objects.equals(this.rotationType(), "NIGHT")) {
            poseStack.rotate(Axis.XP.rotation(dayAngle + (float) Math.PI));
        } else {
            poseStack.rotate(Axis.XP.rotationDegrees(this.rotation().x));
        }
        poseStack.rotate(Axis.ZP.rotationDegrees(this.rotation().z));
    }

    /**
     * Set the rotation of the object around its own center
     * Rotate the object but don't change its position
     * @param poseStack
     */
    public void setObjectRotation(PoseStack poseStack) {
        poseStack.translate(0, this.height, 0);
        poseStack.rotate(Axis.XP.rotationDegrees(objectRotation.x));
        poseStack.rotate(Axis.YP.rotationDegrees(objectRotation.y));
        poseStack.rotate(Axis.ZP.rotationDegrees(objectRotation.z));
        poseStack.translate(0, -this.height, 0);
    }

    public GpuBuffer buildSkyObject(TextureAtlas textureAtlas) {
        return SkyRenderer.buildCelestialQuad(this.texture.getPath(), textureAtlas.getSprite(this.texture));
    }

    public void renderObject(RenderPass renderPass, List<GpuBuffer> scratchBuffers, float alpha, PoseStack poseStack, TextureAtlas celestial, float sunAngle) {
        poseStack.pushPose();

        this.setObjectRotation(poseStack);
        this.setObjectPosition(poseStack, sunAngle);

        RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);

        GpuBuffer objectBuffer = this.buildSkyObject(celestial);

        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();

        matrix4fStack.mul(poseStack.last().pose());
        matrix4fStack.translate(0.0F, (float) this.height, 0.0F);
        matrix4fStack.scale(this.size, 1.0F, this.size);

        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(matrix4fStack, new Vector4f(1.0F, 1.0F, 1.0F, alpha), new Vector3f(), new Matrix4f());
        GpuBuffer gpuBuffer = quadIndices.getBuffer(6);

        renderPass.pushDebugGroup(() -> "Sky sun");
        renderPass.setPipeline(RenderSystem.getCompiledPipeline(this.blend ? RenderPipelineRegistry.CELESTIAL_BLEND : RenderPipelineRegistry.CELESTIAL_NO_BLEND));
        RenderSystem.bindDefaultUniforms(renderPass);
        renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
        renderPass.setUniform("Sampler0", celestial.getTextureView(), celestial.getSampler());
        renderPass.setVertexBuffer(0, objectBuffer.slice());
        renderPass.setIndexBuffer(gpuBuffer, quadIndices.type());
        renderPass.drawIndexed(6, 1,0, 0, 0);
        renderPass.popDebugGroup();

        matrix4fStack.popMatrix();

        scratchBuffers.add(objectBuffer);

        poseStack.popPose();

    }
}

