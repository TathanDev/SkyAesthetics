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
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Objects;
import java.util.function.Consumer;

public class SkyHelper {
    private static final ResourceLocation END_SKY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/environment/end_sky.png");

    public static void drawMoonWithPhase(Tesselator tesselator, PoseStack poseStack, float y, CustomVanillaObject moon, float dayAngle) {
        int moonPhase = 3;
        int xCoord = moonPhase % 4;
        int yCoord = moonPhase / 4 % 2;
        float startX = xCoord / 4.0F;
        float startY = yCoord / 2.0F;
        float endX = (xCoord + 1) / 4.0F;
        float endY = (yCoord + 1) / 2.0F;
        drawCelestialBody(moon.moonTexture(), tesselator, poseStack, y, 20f, dayAngle, startX, endX, startY, endY, true);
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
        RenderSystem.overlayBlendFunc();
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

//        poseStack.pushPose();
//
//        poseStack.mulPose(Axis.YP.rotationDegrees((float) object.rotation().y));
//        if(Objects.equals(object.rotationType(), "DAY")) {
//            poseStack.mulPose(Axis.XP.rotationDegrees(dayAngle));
//        } else if(Objects.equals(object.rotationType(), "NIGHT")) {
//            poseStack.mulPose(Axis.XP.rotationDegrees(dayAngle + 180));
//        } else {
//            poseStack.mulPose(Axis.XP.rotationDegrees((float) object.rotation().x));
//        }
//        poseStack.mulPose(Axis.ZP.rotationDegrees((float) object.rotation().z));


        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f matrix4f = poseStack.last().pose();
        RenderSystem.depthMask(false);
        RenderSystem.overlayBlendFunc();
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


    public static void renderEndSky(PoseStack poseStack) {
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(CoreShaders.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, END_SKY_TEXTURE);
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


    public static void renderSunMoonAndStars(CustomVanillaObject object, PoseStack poseStack, Tesselator tesselator, float f, int i, float g, float h, FogParameters fogParameters) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(f * 360.0F));

        if(object.sun()) renderSun(object, g, tesselator, poseStack);
        if(object.moon()) renderMoon(object, i, g, tesselator, poseStack);

        poseStack.popPose();
    }


    public static void renderSun(CustomVanillaObject object, float f, Tesselator tesselator, PoseStack poseStack) {
        float g = object.sunSize();
        float h = object.sunHeight();

        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f matrix4f = poseStack.last().pose();
        RenderSystem.depthMask(false);
        RenderSystem.overlayBlendFunc();
        RenderSystem.setShader(CoreShaders.POSITION_TEX);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f);
        RenderSystem.setShaderTexture(0, object.sunTexture());
        RenderSystem.enableBlend();
        bufferBuilder.addVertex(matrix4f, -g, h, -g).setUv(0.0F, 0.0F);
        bufferBuilder.addVertex(matrix4f, g, h, -g).setUv(1.0F, 0.0F);
        bufferBuilder.addVertex(matrix4f, g, h, g).setUv(1.0F, 1.0F);
        bufferBuilder.addVertex(matrix4f, -g, h, g).setUv(0.0F, 1.0F);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
    }

    public static void renderMoon(CustomVanillaObject object, int i, float f, Tesselator tesselator, PoseStack poseStack) {

        int j = i % 4;
        int k = i / 4 % 2;
        float h = (float)(j) / 4.0F;
        float l = (float)(k) / 2.0F;
        float m = (float)(j + 1) / 4.0F;
        float n = (float)(k + 1) / 2.0F;

        if(!object.moonPhase()) {
            h = 1.0F;
            l = 1.0F;
            m = 0.0F;
            n = 0.0F;
        }


        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        RenderSystem.depthMask(false);
        RenderSystem.overlayBlendFunc();
        RenderSystem.setShader(CoreShaders.POSITION_TEX);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f);
        RenderSystem.setShaderTexture(0, object.moonTexture());
        RenderSystem.enableBlend();
        Matrix4f matrix4f = poseStack.last().pose();
        bufferBuilder.addVertex(matrix4f, -object.moonSize(), -object.moonHeight(), object.moonSize()).setUv(m, n);
        bufferBuilder.addVertex(matrix4f, object.moonSize(), -object.moonHeight(), object.moonSize()).setUv(h, n);
        bufferBuilder.addVertex(matrix4f, object.moonSize(), -object.moonHeight(), -object.moonSize()).setUv(h, l);
        bufferBuilder.addVertex(matrix4f, -object.moonSize(), -object.moonHeight(), -object.moonSize()).setUv(m, l);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
    }

}