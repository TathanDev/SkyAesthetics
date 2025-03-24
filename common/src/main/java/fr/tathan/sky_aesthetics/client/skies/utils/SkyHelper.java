package fr.tathan.sky_aesthetics.client.skies.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import fr.tathan.sky_aesthetics.client.data.SkyPropertiesData;
import fr.tathan.sky_aesthetics.client.skies.PlanetSky;
import fr.tathan.sky_aesthetics.client.skies.record.CustomVanillaObject;
import fr.tathan.sky_aesthetics.client.skies.record.SkyObject;
import fr.tathan.sky_aesthetics.client.skies.renderer.SkyRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Objects;
import java.util.function.Consumer;

public class SkyHelper {
    private static final ResourceLocation END_SKY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/environment/end_sky.png");

    public static void drawMoonWithPhase(Tesselator tesselator, PoseStack poseStack, float y, CustomVanillaObject moon, float dayAngle) {
        if (moon.moonTexture().isEmpty()) return;
        int moonPhase = 3; // TODO: Get moon phase
        int xCoord = moonPhase % 4;
        int yCoord = moonPhase / 4 % 2;
        float startX = xCoord / 4.0F;
        float startY = yCoord / 2.0F;
        float endX = (xCoord + 1) / 4.0F;
        float endY = (yCoord + 1) / 2.0F;
        drawCelestialBody(moon.moonTexture().get(), tesselator, poseStack, y, 20f, dayAngle, startX, endX, startY, endY, true);
    }

    public static void drawCelestialBody(SkyObject skyObject, Tesselator tesselator, PoseStack poseStack, float y, float dayAngle, boolean blend) {
        drawCelestialBody(skyObject.texture(), tesselator, poseStack, y, skyObject.size(), dayAngle, blend);
    }

    public static void drawCelestialBody(ResourceLocation texture, Tesselator tesselator, PoseStack poseStack, float y, float size, float dayAngle, boolean blend) {
        drawCelestialBody(texture, tesselator, poseStack, y, size, dayAngle, 0f, 1f, 1f, 0f, blend);
    }

    public static void drawCelestialBody(ResourceLocation texture, Tesselator tesselator, PoseStack poseStack, float y, float size, float dayAngle, float startX, float endX, float startY, float endY, boolean blend) {
        drawCelestialBody(texture, tesselator, poseStack, y, size, dayAngle, startX, endX, startY, endY, blend, new float[]{1f, 1f, 1f, 1f});

    }

    public static void drawCelestialBody(ResourceLocation texture, Tesselator tesselator, PoseStack poseStack, float y, float size, float dayAngle, float startX, float endX, float startY, float endY, boolean blend, float @Nullable [] color) {

        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(CoreShaders.POSITION_TEX);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, dayAngle);
        RenderSystem.setShaderTexture(0, texture);

        if(blend) RenderSystem.enableBlend();

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
        RenderSystem.setShader(CoreShaders.POSITION_TEX);
        RenderSystem.setShaderTexture(0, texture);
        bufferBuilder.addVertex(matrix4f, -size * ratio, y * ratio - 1, -size * ratio).setUv(startX, endY);
        bufferBuilder.addVertex(matrix4f, size * ratio, y * ratio - 1, -size * ratio).setUv(endX, endY);
        bufferBuilder.addVertex(matrix4f, size * ratio, y * ratio - 1, size * ratio).setUv(endX, startY);
        bufferBuilder.addVertex(matrix4f, -size * ratio, y * ratio - 1, size * ratio).setUv(startX, startY);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if(blend) {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }

        RenderSystem.depthMask(true);
    }

    public static void renderCelestialBody(SkyObject object, Tesselator tesselator, PoseStack poseStack, float dayAngle) {

        float ratio = 1;
        if (object.height() > Minecraft.getInstance().gameRenderer.getRenderDistance()) {
            ratio = Minecraft.getInstance().gameRenderer.getRenderDistance() / object.height();
        }

        object.setObjectPosition(poseStack, dayAngle);
        object.setObjectRotation(poseStack);

        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f matrix4f = poseStack.last().pose();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(CoreShaders.POSITION_TEX);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, dayAngle);
        RenderSystem.setShaderTexture(0, object.texture());


        bufferBuilder.addVertex(matrix4f, -object.size() * ratio, object.height() * ratio - 1, -object.size() * ratio).setUv(0f, 0f);
        bufferBuilder.addVertex(matrix4f, object.size() * ratio, object.height() * ratio - 1, -object.size() * ratio).setUv(1f, 0f);
        bufferBuilder.addVertex(matrix4f, object.size() * ratio, object.height() * ratio - 1, object.size() * ratio).setUv(1f, 1f);
        bufferBuilder.addVertex(matrix4f, -object.size() * ratio, object.height() * ratio - 1, object.size() * ratio).setUv(0f, 1f);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if(object.blend()) {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }

        RenderSystem.depthMask(true);

    }



    public static boolean skyTypeToHasGround(String skyType) {
        return switch (skyType) {
            case "END" -> false ;
            case null, default -> true;
        };
    }

    public static void canRenderSky(ClientLevel level, Consumer<PlanetSky> action) {
        for (PlanetSky sky : SkyPropertiesData.SKY_PROPERTIES.values()) {
            if (sky.getProperties().world().equals(level.dimension())) {
                SkyRenderer renderer = sky.getRenderer();
                if (renderer.isSkyRendered()) {
                    action.accept(sky);
                }
            }
        }
    }

    /** Yea I just copied minecraft code. But what ?? I literally modding Minecraft !*/


    public static void renderSunMoonAndStars(CustomVanillaObject object, PoseStack poseStack,  float f, int moonPhase,MultiBufferSource.BufferSource bufferSource, float rainLevel) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(f * 360.0F));
        int alpha = ARGB.white(rainLevel);

        if(object.sun()) renderSun(object, bufferSource, poseStack, alpha);
        if(object.moon()) renderMoon(object, moonPhase, bufferSource, poseStack, alpha);
        bufferSource.endBatch();

        poseStack.popPose();
    }


    private static void renderSun(CustomVanillaObject object, MultiBufferSource bufferSource, PoseStack poseStack, int rainLevel) {
        if (!object.sun()) return;
        float g = object.sunSize().get();
        float h = object.sunHeight().get();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.celestial(object.sunTexture().get()));
        Matrix4f matrix4f = poseStack.last().pose();
        vertexConsumer.addVertex(matrix4f, -g, h, -g).setUv(0.0F, 0.0F).setColor(rainLevel);
        vertexConsumer.addVertex(matrix4f, g, h, -g).setUv(1.0F, 0.0F).setColor(rainLevel);
        vertexConsumer.addVertex(matrix4f, g, h, g).setUv(1.0F, 1.0F).setColor(rainLevel);
        vertexConsumer.addVertex(matrix4f, -g, h, g).setUv(0.0F, 1.0F).setColor(rainLevel);
    }

    private static void renderMoon(CustomVanillaObject object, int phase, MultiBufferSource bufferSource, PoseStack poseStack,int rainLevel) {
        if (!object.moon()) return;

        int j = phase % 4;
        int k = phase / 4 % 2;
        float h = (float)(j) / 4.0F;
        float l = (float)(k) / 2.0F;
        float m = (float)(j + 1) / 4.0F;
        float n = (float)(k + 1) / 2.0F;

        if(!object.moonPhase()) {
            h = 1.0F;
            l = 1.0F;
            m = 0.0F;
            n = 0.0F;
        }        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.celestial(object.moonTexture().get()));
        Matrix4f matrix4f = poseStack.last().pose();

        vertexConsumer.addVertex(matrix4f, -object.moonSize().get(), -object.moonHeight().get(), object.moonSize().get()).setUv(m, n).setColor(rainLevel);
        vertexConsumer.addVertex(matrix4f, object.moonSize().get(), -object.moonHeight().get(), object.moonSize().get()).setUv(h, n).setColor(rainLevel);
        vertexConsumer.addVertex(matrix4f, object.moonSize().get(), -object.moonHeight().get(), -object.moonSize().get()).setUv(h, l).setColor(rainLevel);
        vertexConsumer.addVertex(matrix4f, -object.moonSize().get(), -object.moonHeight().get(), -object.moonSize().get()).setUv(m, l).setColor(rainLevel);
    }


}