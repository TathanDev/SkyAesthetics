package fr.tathan.sky_aesthetics.client.skies.settings;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

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

    public void drawSkyObject(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, float dayAngle) {
        if (this.blend()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }

        poseStack.pushPose();


        //Object Position
        this.setObjectPosition(poseStack, dayAngle);

        //Local Rotation
        this.setObjectRotation(poseStack);

        Matrix4f matrix4f = poseStack.last().pose();

        float ratio = 1;
        if (this.height() > Minecraft.getInstance().gameRenderer.getRenderDistance()) {
            ratio = Minecraft.getInstance().gameRenderer.getRenderDistance() / this.height();
        }

        int i = ARGB.white(1f);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.celestial(texture));
        consumer.addVertex(matrix4f, -this.size() * ratio, this.height() * ratio - 1, -this.size() * ratio).setUv(0f, 0f).setColor(i);
        consumer.addVertex(matrix4f, this.size() * ratio, this.height() * ratio - 1, -this.size() * ratio).setUv(1f, 0f).setColor(i);
        consumer.addVertex(matrix4f, this.size() * ratio, this.height() * ratio - 1, this.size() * ratio).setUv(1f, 1f).setColor(i);
        consumer.addVertex(matrix4f, -this.size() * ratio, this.height() * ratio - 1, this.size() * ratio).setUv(0f, 1f).setColor(i);
        poseStack.popPose();
        bufferSource.endBatch();

        if (blend) {
            RenderSystem.disableBlend();
        }
        if (this.blend()) {
            RenderSystem.disableBlend();
        }
    }

}
