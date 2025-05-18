package fr.tathan.sky_aesthetics.client.skies.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import fr.tathan.sky_aesthetics.client.skies.record.SkyObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import org.joml.Matrix4f;

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

        RenderSystem.enableCull();


        final float tileHeight = 32f;
        final float imageWidth = 128f;
        final float imageHeight = 32f;

        final float vTileSize = tileHeight / imageWidth;
        final float uTileSize = tileHeight / imageHeight;


        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

        float size = object.size() * ratio;
        float height = object.height() * ratio; // Use adjusted height

        RenderSystem.setShaderTexture(0, object.texture());


        bufferBuilder.addVertex(matrix4f, -size, height - 1 + size, -size).setUv(vTileSize * 2f, 0f).setColor(1.0f, 1.0f, 1.0f, 1.0f).setNormal(0f, 0f, -1f);
        bufferBuilder.addVertex(matrix4f, size, height - 1 + size, -size).setUv(vTileSize * 3f, 0f).setColor(1.0f, 1.0f, 1.0f, 1.0f).setNormal(0f, 0f, -1f);
        bufferBuilder.addVertex(matrix4f, size, height - 1 - size, -size).setUv(vTileSize * 3f, uTileSize).setColor(1.0f, 1.0f, 1.0f, 1.0f).setNormal(0f, 0f, -1f);
        bufferBuilder.addVertex(matrix4f, -size, height - 1 - size, -size).setUv(vTileSize * 2f, uTileSize).setColor(1.0f, 1.0f, 1.0f, 1.0f).setNormal(0f, 0f, -1f);


        // Bottom face (Clockwise, Normal: 0, -1, 0) - Using Tile 3
        bufferBuilder.addVertex(matrix4f, -size, height - 1 - size, -size).setUv(0f, uTileSize).setColor(1.0f, 1.0f, 1.0f, 1.0f).setNormal(0f, -1f, 0f);
        bufferBuilder.addVertex(matrix4f, size, height - 1 - size, -size).setUv(vTileSize, uTileSize).setColor(1.0f, 1.0f, 1.0f, 1.0f).setNormal(0f, -1f, 0f);
        bufferBuilder.addVertex(matrix4f, size, height - 1 - size, size).setUv(vTileSize, 0f).setColor(1.0f, 1.0f, 1.0f, 1.0f).setNormal(0f, -1f, 0f);
        bufferBuilder.addVertex(matrix4f, -size, height - 1 - size, size).setUv(0f, 0f).setColor(1.0f, 1.0f, 1.0f, 1.0f).setNormal(0f, -1f, 0f);


        // Left face (Clockwise, Normal: -1, 0, 0) - Using Tile 4 (First tile in the second row)
        bufferBuilder.addVertex(matrix4f, -size, height - 1 + size, -size).setUv(0f, uTileSize * 2f).setColor(1.0f, 1.0f, 1.0f, 1.0f).setNormal(-1f, 0f, 0f);
        bufferBuilder.addVertex(matrix4f, -size, height - 1 - size, -size).setUv(0f, uTileSize).setColor(1.0f, 1.0f, 1.0f, 1.0f).setNormal(-1f, 0f, 0f);
        bufferBuilder.addVertex(matrix4f, -size, height - 1 - size, size).setUv(vTileSize, uTileSize).setColor(1.0f, 1.0f, 1.0f, 1.0f).setNormal(-1f, 0f, 0f);
        bufferBuilder.addVertex(matrix4f, -size, height - 1 + size, size).setUv(vTileSize, uTileSize * 2f).setColor(1.0f, 1.0f, 1.0f, 1.0f).setNormal(-1f, 0f, 0f);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        // Disable backface culling
        RenderSystem.disableCull();
        poseStack.popPose();

        if (object.blend()) {
            RenderSystem.disableBlend();
        }
        RenderSystem.enableCull();

    }

}