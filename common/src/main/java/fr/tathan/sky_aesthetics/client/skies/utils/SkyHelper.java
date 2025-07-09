package fr.tathan.sky_aesthetics.client.skies.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.DimensionRenderer;
import fr.tathan.sky_aesthetics.client.data.SkyPropertiesData;
import fr.tathan.sky_aesthetics.client.skies.DimensionSky;
import fr.tathan.sky_aesthetics.client.skies.settings.SkyObject;
import fr.tathan.sky_aesthetics.helper.PlatformHelper;
import fr.tathan.sky_aesthetics.mixin.client.LevelRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Arrays;
import java.util.function.Consumer;

public class SkyHelper {
    public static void drawSky(Matrix4f matrix4f, Matrix4f projectionMatrix) {
        ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).stellaris$getSkyBuffer().bind();
        ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).stellaris$getSkyBuffer().drawWithShader(matrix4f, projectionMatrix, RenderSystem.getShader());

        VertexBuffer.unbind();
    }

    public static void drawMoonWithPhase(Tesselator tesselator, PoseStack poseStack, float y, ResourceLocation texture, float dayAngle) {
        int moonPhase = 3; // TODO: Get moon phase
        int xCoord = moonPhase % 4;
        int yCoord = moonPhase / 4 % 2;
        float startX = xCoord / 4.0F;
        float startY = yCoord / 2.0F;
        float endX = (xCoord + 1) / 4.0F;
        float endY = (yCoord + 1) / 2.0F;
        drawCelestialBody(texture, tesselator, poseStack, y, 20f, dayAngle, startX, endX, startY, endY, true);
    }

    public static void drawCelestialBody(ResourceLocation texture, Tesselator tesselator, PoseStack poseStack, float y, float size, float dayAngle, boolean blend) {
        drawCelestialBody(texture, tesselator, poseStack, y, size, dayAngle, 0f, 1f, 1f, 0f, blend);
    }

    public static void drawCelestialBody(ResourceLocation texture, Tesselator tesselator, PoseStack poseStack, float y, float size, float dayAngle, float startX, float endX, float startY, float endY, boolean blend) {
        drawCelestialBody(texture, tesselator, poseStack, y, size, dayAngle, startX, endX, startY, endY, blend, new float[]{1f, 1f, 1f, 1f});

    }

    public static void drawCelestialBody(ResourceLocation texture, Tesselator tesselator, PoseStack poseStack, float y, float size, float dayAngle, float startX, float endX, float startY, float endY, boolean blend, float @Nullable [] color) {
        if (blend) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }

        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(-90f));
        poseStack.mulPose(Axis.XP.rotationDegrees(dayAngle));

        Matrix4f matrix4f = poseStack.last().pose();

        if(color == null) {
            color = new float[]{1f, 1f, 1f, 1f};
        }

        float ratio = 1;
        if (y > Minecraft.getInstance().gameRenderer.getRenderDistance()) {
            ratio = Minecraft.getInstance().gameRenderer.getRenderDistance() / y;
        }

        RenderSystem.setShaderColor(color[0] , color[1], color[2], 4.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex(matrix4f, -size * ratio, y * ratio - 1, -size * ratio).setUv(startX, endY);
        bufferBuilder.addVertex(matrix4f, size * ratio, y * ratio - 1, -size * ratio).setUv(endX, endY);
        bufferBuilder.addVertex(matrix4f, size * ratio, y * ratio - 1, size * ratio).setUv(endX, startY);
        bufferBuilder.addVertex(matrix4f, -size * ratio, y * ratio - 1, size * ratio).setUv(startX, startY);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        poseStack.popPose();

        if (blend) {
            RenderSystem.disableBlend();
        }
    }


    public static void renderEndSky(PoseStack poseStack) {
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).stellaris$getEndSkyLocation());
        Tesselator tesselator = Tesselator.getInstance();

        for(int i = 0; i < 6; ++i) {
            poseStack.pushPose();

            switch (i) {
                case 1, 4 -> poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0F));
                case 2, 5 -> poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90.0F));
                case 3 -> poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(270.0F));
            }

            Matrix4f matrix4f = poseStack.last().pose();
            BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.addVertex(matrix4f, -100.0F, -100.0F, -100.0F).setUv(0.0F, 0.0F).setColor(-14145496);
            bufferBuilder.addVertex(matrix4f, -100.0F, -100.0F, 100.0F).setUv(0.0F, 16.0F).setColor(-14145496);
            bufferBuilder.addVertex(matrix4f, 100.0F, -100.0F, 100.0F).setUv(16.0F, 16.0F).setColor(-14145496);
            bufferBuilder.addVertex(matrix4f, 100.0F, -100.0F, -100.0F).setUv(16.0F, 0.0F).setColor(-14145496);
            BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
            poseStack.popPose();
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }


    public static boolean canRenderSky(ClientLevel level, Consumer<DimensionSky> action) {
        for (DimensionSky sky : SkyPropertiesData.SKY_PROPERTIES.values()) {
            if (sky.getDimension().equals(level.dimension())) {

                // Check if the sky is disabled in the config
                if(Arrays.stream(SkyAesthetics.CONFIG.disabledSkies).anyMatch((s)-> s.equals(sky.getSkyId().toString()))) return false;

                // Check if the sky's dimension is disabled in the properties
                if(Arrays.stream(SkyAesthetics.CONFIG.disabledDimensions).anyMatch((s)-> s.equals(sky.getDimension().location().toString()))) return false;

                DimensionRenderer renderer = sky.getRenderer();
                if (renderer.renderCondition != null && renderer.renderCondition.isSkyRendered(DimensionRenderer.getServerLevel())) {
                    return false;
                }
                action.accept(sky);
                return true;
            }
        }
        return false;
    }

    public static boolean isAModCancelRendering(String[] modIds) {
        for(String modId : modIds) {
            if (PlatformHelper.isModLoaded(modId)) {
                return true;
            }
        }
        return false;
    }
}