package fr.tathan.sky_aesthetics.client.skies.settings;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record SkyObject(ResourceLocation texture, boolean blend, float size, Vec3 rotation, Optional<Vector3f> objectRotation, int height, String rotationType) {

    public static Codec<Vector3f> VEC3F = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 3).map((listx) -> new Vector3f(listx.getFirst(), listx.get(1), listx.getLast())), (vector3f) -> List.of(vector3f.x, vector3f.y, vector3f.z));

    public static final Codec<SkyObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("texture").forGetter(SkyObject::texture),
            Codec.BOOL.fieldOf("blend").forGetter(SkyObject::blend),
            Codec.FLOAT.fieldOf("size").forGetter(SkyObject::size),
            Vec3.CODEC.fieldOf("rotation").forGetter(SkyObject::rotation),
            VEC3F.optionalFieldOf("object_rotation").forGetter(SkyObject::objectRotation),
            Codec.INT.fieldOf("height").forGetter(SkyObject::height),
            Codec.STRING.fieldOf("rotation_type").forGetter(SkyObject::rotationType)
    ).apply(instance, SkyObject::new));

    public void setObjectPosition(PoseStack poseStack, float dayAngle) {
        poseStack.mulPose(Axis.YP.rotationDegrees((float) this.rotation().y));
        if(Objects.equals(this.rotationType(), "DAY")) {
            poseStack.mulPose(Axis.XP.rotationDegrees(dayAngle));
        } else if(Objects.equals(this.rotationType(), "NIGHT")) {
            poseStack.mulPose(Axis.XP.rotationDegrees(dayAngle + 180));
        } else {
            poseStack.mulPose(Axis.XP.rotationDegrees((float) this.rotation().x));
        }
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) this.rotation().z));
    }

    public void setObjectRotation(PoseStack poseStack) {
        this.objectRotation.ifPresent((rotation) -> {
            poseStack.translate(0, 100, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees(rotation.x));
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation.y));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation.z));
            poseStack.translate(0, -100, 0);
        });
    }

    public void drawSkyObject(Tesselator tesselator, PoseStack poseStack, float dayAngle) {
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

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.texture());
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex(matrix4f, -this.size() * ratio, this.height() * ratio - 1, -this.size() * ratio).setUv(0f, 0f);
        bufferBuilder.addVertex(matrix4f, this.size() * ratio, this.height() * ratio - 1, -this.size() * ratio).setUv(1f, 0f);
        bufferBuilder.addVertex(matrix4f, this.size() * ratio, this.height() * ratio - 1, this.size() * ratio).setUv(1f, 1f);
        bufferBuilder.addVertex(matrix4f, -this.size() * ratio, this.height() * ratio - 1, this.size() * ratio).setUv(0f, 1f);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        poseStack.popPose();

        if (this.blend()) {
            RenderSystem.disableBlend();
        }
    }

}
