package fr.tathan.sky_aesthetics.client.skies.utils;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.data.ConstellationsData;
import fr.tathan.sky_aesthetics.client.skies.record.Constellation;
import fr.tathan.sky_aesthetics.client.skies.record.Star;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.BooleanUtils;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class StarHelper {
    public static int starIndexCount;

    public static GpuBuffer createStars(Star star, Optional<List<String>> constellations) {
        RandomSource randomSource = RandomSource.create();

        GpuBuffer buffer;

        GraphicsStatus graphicsMode = Minecraft.getInstance().options.graphicsMode().get();
        int starCount = star.count() / (BooleanUtils.toInteger(graphicsMode == GraphicsStatus.FANCY || graphicsMode == GraphicsStatus.FABULOUS) + 1);


        try (ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(DefaultVertexFormat.POSITION_COLOR.getVertexSize() * starCount * 4)) {
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);


            /* Stars */
            for (int i = 0; i < starCount; i++) {
                float d0 = randomSource.nextFloat() * 2.0F - 1.0F;
                float d1 = randomSource.nextFloat() * 2.0F - 1.0F;
                float d2 = randomSource.nextFloat() * 2.0F - 1.0F;

                float d3 = star.scale();

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
                    float d14 = (float) (randomSource.nextDouble() * Math.TAU);
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

                        int color1, color2, color3;
                        if (star.color().x() == -1) {
                            color1 = 255;
                            color2 = 255;
                            color3 = 255;
                        } else {
                            color1 = (int) star.color().x();
                            color2 = (int) star.color().y();
                            color3 = (int) star.color().z();
                        }

                        bufferBuilder.addVertex(d5 + d25, d6 + d23, d7 + d26)
                                .setColor(color1, color2, color3, 0xAA);
                    }
                }
            }

            /* Constellation */
            /*if (constellations.isPresent()) {
                for (String constellationId : constellations.get()) {

                    Constellation constellation = ConstellationsData.CONSTELLATIONS.get(constellationId);
                    if(constellation != null) {
                        Vec3 color = constellation.color();

                        float x = (float)( constellation.firstPoint().x );
                        float y = (float)( constellation.firstPoint().y);
                        float z = (float)( constellation.firstPoint().z);

                        // First Point
                        createStar(constellation.firstPoint(), color, constellation.scale(), randomSource, bufferBuilder);

                        for (Vec3 point : constellation.points()) {

                            Vec3 pointPos = new Vec3(x + point.x, y + point.y, z + point.z);

                            createStar(pointPos, color, constellation.scale(), randomSource, bufferBuilder);

                        }
                    } else {
                        SkyAesthetics.LOG.error("{} is null", constellationId);
                    }
                }
            }*/


            try (MeshData meshData = bufferBuilder.buildOrThrow()) {
                starIndexCount = meshData.drawState().indexCount();
                buffer = RenderSystem.getDevice().createBuffer(() -> "Stars vertex buffer", BufferType.VERTICES, BufferUsage.STATIC_WRITE, meshData.vertexBuffer());
            }
        }

        return buffer;
    }


    public static void createStar(Vec3 pos, Vec3 color, float scale, RandomSource random, BufferBuilder bufferBuilder) {
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
            float d25 = d24 * d9
                    - d22 * d10;
            float d26 = d22 * d9 + d24 * d10;

            bufferBuilder.addVertex(d5 + d25, d6 + d23, d7 + d26).setColor((int) color.x(), (int) color.y(), (int) color.z (), 0xAA);
        }

    }

    public static GpuBuffer createVanillaStars() {
        RandomSource randomSource = RandomSource.create(10842L);

        GpuBuffer buffer;
        try (ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(DefaultVertexFormat.POSITION.getVertexSize() * 1500 * 4)) {
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

            for(int i = 0; i < 1500; ++i) {
                float g = randomSource.nextFloat() * 2.0F - 1.0F;
                float h = randomSource.nextFloat() * 2.0F - 1.0F;
                float j = randomSource.nextFloat() * 2.0F - 1.0F;
                float k = 0.15F + randomSource.nextFloat() * 0.1F;
                float l = Mth.lengthSquared(g, h, j);
                if (!(l <= 0.010000001F) && !(l >= 1.0F)) {
                    Vector3f vector3f = (new Vector3f(g, h, j)).normalize(100.0F);
                    float m = (float)(randomSource.nextDouble() * (double)(float)Math.PI * (double)2.0F);
                    Matrix3f matrix3f = (new Matrix3f()).rotateTowards((new Vector3f(vector3f)).negate(), new Vector3f(0.0F, 1.0F, 0.0F)).rotateZ(-m);
                    bufferBuilder.addVertex((new Vector3f(k, -k, 0.0F)).mul(matrix3f).add(vector3f));
                    bufferBuilder.addVertex((new Vector3f(k, k, 0.0F)).mul(matrix3f).add(vector3f));
                    bufferBuilder.addVertex((new Vector3f(-k, k, 0.0F)).mul(matrix3f).add(vector3f));
                    bufferBuilder.addVertex((new Vector3f(-k, -k, 0.0F)).mul(matrix3f).add(vector3f));
                }
            }

            try (MeshData meshData = bufferBuilder.buildOrThrow()) {
                starIndexCount = meshData.drawState().indexCount();
                buffer = RenderSystem.getDevice().createBuffer(() -> "Stars vertex buffer", BufferType.VERTICES, BufferUsage.STATIC_WRITE, meshData.vertexBuffer());
            }
        }

        return buffer;
    }
}