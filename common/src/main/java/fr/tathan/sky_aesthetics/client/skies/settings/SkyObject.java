package fr.tathan.sky_aesthetics.client.skies.settings;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;

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

        poseStack.mulPose(Axis.YP.rotationDegrees(this.rotation().y));
        if(Objects.equals(this.rotationType(), "DAY")) {
            poseStack.mulPose(Axis.XP.rotationDegrees(dayAngle));
        } else if(Objects.equals(this.rotationType(), "NIGHT")) {
            poseStack.mulPose(Axis.XP.rotationDegrees(dayAngle + 180));
        } else {
            poseStack.mulPose(Axis.XP.rotationDegrees(this.rotation().x));
        }
        poseStack.mulPose(Axis.ZP.rotationDegrees(this.rotation().z));
    }

    /**
     * Set the rotation of the object around its own center
     * Rotate the object but don't change its position
     * @param poseStack
     */
    public void setObjectRotation(PoseStack poseStack) {
        poseStack.translate(0, 100, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(objectRotation.x));
        poseStack.mulPose(Axis.YP.rotationDegrees(objectRotation.y));
        poseStack.mulPose(Axis.ZP.rotationDegrees(objectRotation.z));
        poseStack.translate(0, -100, 0);
    }




    public GpuBuffer buildSkyObject(TextureAtlas textureAtlas) {
        return SkyRenderer.buildCelestialQuad(this.texture.getPath(), textureAtlas.getSprite(this.texture));
    }



    public void renderObject(float alpha, PoseStack poseStack, TextureAtlas celestial, float sunAngle, float moonAngle) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotation(sunAngle));

        this.setObjectRotation(poseStack);
        this.setObjectPosition(poseStack, sunAngle);


        RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);

        GpuBuffer objectBuffer = this.buildSkyObject(celestial);

        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();

        matrix4fStack.mul(poseStack.last().pose());
        matrix4fStack.translate(0.0F, 100.0F, 0.0F);
        matrix4fStack.scale(this.size, 1.0F, 30.0F);

        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(matrix4fStack, new Vector4f(1.0F, 1.0F, 1.0F, alpha), new Vector3f(), new Matrix4f());
        GpuTextureView gpuTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView gpuTextureView2 = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
        GpuBuffer gpuBuffer = quadIndices.getBuffer(6);

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sky sun", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty())) {
            renderPass.setPipeline(RenderPipelines.CELESTIAL);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.bindTexture("Sampler0", celestial.getTextureView(), celestial.getSampler());
            renderPass.setVertexBuffer(0, objectBuffer);
            renderPass.setIndexBuffer(gpuBuffer, quadIndices.type());
            renderPass.drawIndexed(0, 0, 6, 1);
        }

        matrix4fStack.popMatrix();

        poseStack.popPose();

    }
}

