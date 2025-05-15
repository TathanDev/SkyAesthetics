package fr.tathan.sky_aesthetics.client.skies.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.data.SkyPropertiesData;
import fr.tathan.sky_aesthetics.client.skies.PlanetSky;
import fr.tathan.sky_aesthetics.client.skies.record.CustomVanillaObject;
import fr.tathan.sky_aesthetics.client.skies.record.SkyObject;
import fr.tathan.sky_aesthetics.client.skies.renderer.SkyRenderer;
import fr.tathan.sky_aesthetics.mixin.client.LevelRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ModelUtils {


    public static void drawCelestialCube(SkyObject object, PoseStack poseStack, float dayAngle) {

        if (object.blend()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }

        poseStack.pushPose();

        //Object Position
        object.setObjectPosition(poseStack, dayAngle);

        //Local Rotation
        object.setObjectRotation(poseStack);

        Matrix4f matrix4f = poseStack.last().pose();

        float ratio = 1;
        if (object.height() > Minecraft.getInstance().gameRenderer.getRenderDistance()) {
            ratio = Minecraft.getInstance().gameRenderer.getRenderDistance() / object.height();
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, object.texture());

// Enable backface culling
        RenderSystem.enableCull();
        // Define which face to cull (GL_BACK is the default, but explicit is good)

        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float size = object.size() * ratio;
        float height = object.height() * ratio; // Use adjusted height

        // Define the vertices of the cube
        // Front face (Vertices in counter-clockwise order when viewed from the front)
        bufferBuilder.addVertex(matrix4f, -size, height - 1 + size, size).setUv(0f, 0f);
        bufferBuilder.addVertex(matrix4f, size, height - 1 + size, size).setUv(1f, 0f);
        bufferBuilder.addVertex(matrix4f, size, height - 1 - size, size).setUv(1f, 1f);
        bufferBuilder.addVertex(matrix4f, -size, height - 1 - size, size).setUv(0f, 1f);

        // Back face (Vertices in clockwise order when viewed from the front of THIS face)
        // We define them in an order that makes them "back facing" relative to the viewer when
        // the cube is oriented normally, so culling removes them.
        bufferBuilder.addVertex(matrix4f, -size, height - 1 + size, -size).setUv(0f, 0f);
        bufferBuilder.addVertex(matrix4f, size, height - 1 + size, -size).setUv(1f, 0f);
        bufferBuilder.addVertex(matrix4f, size, height - 1 - size, -size).setUv(1f, 1f);
        bufferBuilder.addVertex(matrix4f, -size, height - 1 - size, -size).setUv(0f, 1f);

        // Top face (Vertices in counter-clockwise order when viewed from the top)
        bufferBuilder.addVertex(matrix4f, -size, height - 1 + size, -size).setUv(0f, 0f);
        bufferBuilder.addVertex(matrix4f, -size, height - 1 + size, size).setUv(0f, 1f);
        bufferBuilder.addVertex(matrix4f, size, height - 1 + size, size).setUv(1f, 1f);
        bufferBuilder.addVertex(matrix4f, size, height - 1 + size, -size).setUv(1f, 0f);


        // Bottom face (Vertices in clockwise order when viewed from the bottom)
        bufferBuilder.addVertex(matrix4f, -size, height - 1 - size, -size).setUv(0f, 0f);
        bufferBuilder.addVertex(matrix4f, size, height - 1 - size, -size).setUv(1f, 0f);
        bufferBuilder.addVertex(matrix4f, size, height - 1 - size, size).setUv(1f, 1f);
        bufferBuilder.addVertex(matrix4f, -size, height - 1 - size, size).setUv(0f, 1f);


        // Left face (Vertices in clockwise order when viewed from the left)
        bufferBuilder.addVertex(matrix4f, -size, height - 1 + size, -size).setUv(0f, 0f);
        bufferBuilder.addVertex(matrix4f, -size, height - 1 - size, -size).setUv(0f, 1f);
        bufferBuilder.addVertex(matrix4f, -size, height - 1 - size, size).setUv(1f, 1f);
        bufferBuilder.addVertex(matrix4f, -size, height - 1 + size, size).setUv(1f, 0f);


        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        // Disable backface culling
        RenderSystem.disableCull();
        poseStack.popPose();

        if (object.blend()) {
            RenderSystem.disableBlend();
        }
    }

    private static void vertex(
            VertexConsumer vertexBuilder,
            Matrix4f matrix4f,
            float x,
            float y,
            float z,
            float u,
            float v) {
        vertexBuilder.addVertex(matrix4f, x, y, z).setUv(u, v);
    }
}