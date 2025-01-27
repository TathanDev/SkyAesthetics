package fr.tathan.sky_aesthetics.client.skies.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.data.ConstellationsData;
import fr.tathan.sky_aesthetics.client.skies.record.Constellation;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.BooleanUtils;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class StarHelper {

    public static VertexBuffer createStars(float scale, int amountFancy, int r, int g, int b, Optional<List<String>> constellations, Optional<ResourceLocation> star_texture) {
        Tesselator tesselator = Tesselator.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

        Random random = new Random();
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        GraphicsStatus graphicsMode = Minecraft.getInstance().options.graphicsMode().get();
        int stars = amountFancy / (BooleanUtils.toInteger(graphicsMode == GraphicsStatus.FANCY || graphicsMode == GraphicsStatus.FABULOUS) + 1);

        star_texture.ifPresent(resourceLocation -> RenderSystem.setShaderTexture(0, resourceLocation));

        /** Stars **/
        for (int i = 0; i < stars; i++) {
            float d0 = random.nextFloat() * 2.0F - 1.0F;
            float d1 = random.nextFloat() * 2.0F - 1.0F;
            float d2 = random.nextFloat() * 2.0F - 1.0F;
            float d3 = scale + random.nextFloat() * 0.1F;
            float d4 = d0 * d0 + d1 * d1 + d2 * d2;
            if (d4 < 1.0f && d4 > 0.01f) {
                d4 = (float) (1.0f / Math.sqrt(d4));

                // Position of star
                d0 *= d4;
                d1 *= d4;
                d2 *= d4;

                float d5 = d0 * 100.0f;
                float d6 = d1 * 100.0f;
                float d7 = d2 * 100.0f;
                float d8 = (float) Math.atan2(d0, d2);
                float d9 = (float) Math.sin(d8);
                float d10 = (float) Math.cos(d8);
                float d11 = (float) Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                float d12 = (float) Math.sin(d11);
                    float d13 = (float) Math.cos(d11);
                    float d14 = (float) (random.nextDouble() * Math.PI * 2.0D);
                    float d15 = (float) Math.sin(d14);
                    float d16 = (float) Math.cos(d14);

                    for (int j = 0; j < 4; ++j) {
                        float d18 = ((j & 2) - 1) * d3;
                        float d19 = ((j + 1 & 2) - 1) * d3;
                        float d21 = d18 * d16 - d19 * d15;
                        float d22 = d19 * d16 + d18 * d15;
                        float d23 = d21 * d12 + 0f * d13;
                        float d24 = 0f * d12 - d21 * d13;
                        float d25 = d24 * d9 - d22 * d10;
                    float d26 = d22 * d9 + d24 * d10;

                    int color1 = r == -1 ? i : r;
                    int color2 = g == -1 ? i : g;
                    int color3 = b == -1 ? i : b;

                    if (star_texture.isPresent()) {
                        float u = (j % 2) * 1.0f;
                        float v = (j / 2) * 1.0f;
                        bufferBuilder.addVertex(d5 + d25, d6 + d23, d7 + d26)
                                .setUv(u, v)
                                .setColor(color1, color2, color3, 0xAA);
                    } else {
                        bufferBuilder.addVertex(d5 + d25, d6 + d23, d7 + d26)
                                .setColor(color1, color2, color3, 0xAA);
                    }
                }
            }
        }

        if (constellations.isPresent()) {
            SkyAesthetics.LOG.error("Is present");

            for (String constellationId : constellations.get()) {

                Constellation constellation = ConstellationsData.CONSTELLATIONS.get(constellationId);
                if(constellation != null) {
                    Vec3 color = constellation.color();

                    float x = (float)( constellation.firstPoint().x );
                    float y = (float)( constellation.firstPoint().y);
                    float z = (float)( constellation.firstPoint().z);

                    // First Point
                    createStar(constellation.firstPoint(), color, (int) constellation.scale(), random, bufferBuilder);

                    for (Vec3 point : constellation.points()) {

                        Vec3 pointPos = new Vec3(x + point.x, y + point.y, z + point.z);

                        createStar(pointPos, color, constellation.scale(), random, bufferBuilder);

                    }
                } else {
                    SkyAesthetics.LOG.error("{} is null", constellationId);
                }

            }
        }



        vertexBuffer.bind();
        vertexBuffer.upload(bufferBuilder.buildOrThrow());
        VertexBuffer.unbind();
        return vertexBuffer;
    }

    public static void createStar(Vec3 pos, Vec3 color, float scale, Random random, BufferBuilder bufferBuilder) {
        float d0 = (float) pos.x;
        float d1 = (float) pos.y;
        float d2 = (float) pos.z;
        float d3 = Mth.clamp(scale + random.nextFloat(), scale, scale + 0.2f);
        float d4 = d0 * d0 + d1 * d1 + d2 * d2;

        d4 = (float) (1.0f / Math.sqrt(d4));

        //Position of star
        d0 *= d4;
        d1 *= d4;
        d2 *= d4;

        float d5 = d0 * 100.0f;
        float d6 = d1 * 100.0f;
        float d7 = d2 * 100.0f;
        float d8 = (float) Math.atan2(d0, d2);
        float d9 = (float) Math.sin(d8);
        float d10 = (float) Math.cos(d8);
        float d11 = (float) Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
        float d12 = (float) Math.sin(d11);
        float d13 = (float) Math.cos(d11);
        float d14 = (float) (random.nextDouble() * Math.PI);
        float d15 = (float) Math.sin(d14);
        float d16 = (float) Math.cos(d14);

        for (int j = 0; j < 4; ++j) {
            float d18 = ((j & 2) - 1) * d3;
            float d19 = ((j + 1 & 2) - 1) * d3;
            float d21 = d18 * d16 - d19 * d15;
            float d22 = d19 * d16 + d18 * d15;
            float d23 = d21 * d12 + 0f * d13;
            float d24 = 0f * d12 - d21 * d13;
            float d25 = d24 * d9
                    - d22 * d10;
            float d26 = d22 * d9 + d24 * d10;

            bufferBuilder.addVertex(d5 + d25, d6 + d23, d7 + d26).setColor((int) color.x(), (int) color.y(), (int) color.z (), 0xAA);
        }

    }

    public static void drawStars(VertexBuffer vertexBuffer, PoseStack poseStack, Matrix4f projectionMatrix, float nightTime, Optional<ResourceLocation> stars_texture) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(nightTime));
        FogRenderer.setupNoFog();

        stars_texture.ifPresent(resourceLocation -> RenderSystem.setShaderTexture(0, resourceLocation));

        vertexBuffer.bind();
        vertexBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionTexColorShader());
        VertexBuffer.unbind();
        poseStack.popPose();
    }


    public static VertexBuffer createVanillaStars() {
        VertexBuffer starBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

        starBuffer.bind();
        starBuffer.upload(createVanillaStars(Tesselator.getInstance()));
        VertexBuffer.unbind();

        return starBuffer;
    }


    public static MeshData createVanillaStars(Tesselator tesselator) {
        RandomSource randomSource = RandomSource.create(10842L);
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        for(int j = 0; j < 1500; ++j) {
            float g = randomSource.nextFloat() * 2.0F - 1.0F;
            float h = randomSource.nextFloat() * 2.0F - 1.0F;
            float k = randomSource.nextFloat() * 2.0F - 1.0F;
            float l = 0.15F + randomSource.nextFloat() * 0.1F;
            float m = Mth.lengthSquared(g, h, k);
            if (!(m <= 0.010000001F) && !(m >= 1.0F)) {
                Vector3f vector3f = (new Vector3f(g, h, k)).normalize(100.0F);
                float n = (float)(randomSource.nextDouble() * 3.1415927410125732 * 2.0);
                Quaternionf quaternionf = (new Quaternionf()).rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), vector3f).rotateZ(n);
                bufferBuilder.addVertex(vector3f.add((new Vector3f(l, -l, 0.0F)).rotate(quaternionf))).setColor(255);
                bufferBuilder.addVertex(vector3f.add((new Vector3f(l, l, 0.0F)).rotate(quaternionf))).setColor(255);
                bufferBuilder.addVertex(vector3f.add((new Vector3f(-l, l, 0.0F)).rotate(quaternionf))).setColor(255);
                bufferBuilder.addVertex(vector3f.add((new Vector3f(-l, -l, 0.0F)).rotate(quaternionf))).setColor(255);
            }
        }

        return bufferBuilder.buildOrThrow();
    }

}