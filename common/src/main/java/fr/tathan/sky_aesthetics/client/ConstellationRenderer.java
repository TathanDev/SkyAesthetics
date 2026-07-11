package fr.tathan.sky_aesthetics.client;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import fr.tathan.sky_aesthetics.client.data.ConstellationsData;
import fr.tathan.sky_aesthetics.client.registry.RenderPipelineRegistry;
import fr.tathan.sky_aesthetics.client.settings.Constellation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;
import org.joml.*;

import java.lang.Math;
import java.util.*;

public class ConstellationRenderer {

    private record BufferEntry(GpuBuffer gpuBuffer, int indexCount, boolean textured) {}

    private static final Map<String, BufferEntry> BUFFERS = new HashMap<>();
    private static boolean dirty = true;
    private static TextureAtlas celestialAtlas;

    /** Called from ConstellationsData.apply() — just marks buffers stale, no GPU work. */
    public static void invalidate() {
        dirty = true;
    }

    /** Renders all loaded constellations. Must be called on the render thread. */
    public static void renderAll(PoseStack poseStack, SkyRenderState state) {
        if (dirty) {
            rebuildBuffers();
            dirty = false;
        }
        if (BUFFERS.isEmpty() || state.starBrightness <= 0.0F) return;

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotation(state.starAngle));

        for (Constellation c : ConstellationsData.CONSTELLATIONS.values()) {
            renderOne(c, poseStack, state.starBrightness);
        }

        poseStack.popPose();
    }

    private static void rebuildBuffers() {
        BUFFERS.values().forEach(e -> e.gpuBuffer().close());
        BUFFERS.clear();
        celestialAtlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.CELESTIALS);

        for (Constellation c : ConstellationsData.CONSTELLATIONS.values()) {
            BufferEntry entry = buildBuffer(c);
            if (entry != null) {
                BUFFERS.put(c.id(), entry);
            }
        }
    }

    private static BufferEntry buildBuffer(Constellation c) {
        List<net.minecraft.world.phys.Vec3> allPoints = new ArrayList<>();
        allPoints.add(c.firstPoint());
        allPoints.addAll(c.points());

        if (allPoints.isEmpty()) return null;

        int totalPoints = allPoints.size();
        float quadHalf = Math.max(c.scale() * 0.3f, 0.05f);
        boolean textured = c.starTexture().isPresent();
        VertexFormat format = textured ? DefaultVertexFormat.POSITION_TEX : DefaultVertexFormat.POSITION;

        RandomSource rng = RandomSource.create(c.id().hashCode());

        TextureAtlasSprite sprite = textured ? celestialAtlas.getSprite(c.starTexture().get()) : null;

        GpuBuffer gpuBuffer;
        int indexCount;

        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(
                format.getVertexSize() * totalPoints * 4)) {
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, format);

            for (net.minecraft.world.phys.Vec3 point : allPoints) {
                Vector3f pos = new Vector3f((float) point.x, (float) point.y, (float) point.z).normalize(100.0F);
                float k = quadHalf;
                float angle = (float) (rng.nextDouble() * Math.PI * 2.0);
                Matrix3f orient = new Matrix3f()
                        .rotateTowards(new Vector3f(pos).negate(), new Vector3f(0.0F, 1.0F, 0.0F))
                        .rotateZ(-angle);

                Vector3f br = new Vector3f(k,  -k, 0.0F).mul(orient).add(pos);
                Vector3f tr = new Vector3f(k,   k, 0.0F).mul(orient).add(pos);
                Vector3f tl = new Vector3f(-k,  k, 0.0F).mul(orient).add(pos);
                Vector3f bl = new Vector3f(-k, -k, 0.0F).mul(orient).add(pos);

                if (textured) {
                    bufferBuilder.addVertex(br).setUv(sprite.getU1(), sprite.getV1());
                    bufferBuilder.addVertex(tr).setUv(sprite.getU1(), sprite.getV0());
                    bufferBuilder.addVertex(tl).setUv(sprite.getU0(), sprite.getV0());
                    bufferBuilder.addVertex(bl).setUv(sprite.getU0(), sprite.getV1());
                } else {
                    bufferBuilder.addVertex(br);
                    bufferBuilder.addVertex(tr);
                    bufferBuilder.addVertex(tl);
                    bufferBuilder.addVertex(bl);
                }
            }

            try (MeshData meshData = bufferBuilder.buildOrThrow()) {
                indexCount = meshData.drawState().indexCount();
                final String id = c.id();
                gpuBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Constellation " + id + " stars", 40, meshData.vertexBuffer());
            }
        }

        return new BufferEntry(gpuBuffer, indexCount, textured);
    }

    private static void renderOne(Constellation c, PoseStack poseStack, float brightness) {
        BufferEntry entry = BUFFERS.get(c.id());
        if (entry == null) return;

        net.minecraft.world.phys.Vec3 color = c.color();
        Vector4f colorUniform = new Vector4f(
                (float) color.x / 255f,
                (float) color.y / 255f,
                (float) color.z / 255f,
                brightness);

        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul(poseStack.last().pose());

        RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
        GpuBuffer indexBuffer = quadIndices.getBuffer(entry.indexCount());

        RenderTarget mainRenderTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        GpuTextureView colorView = mainRenderTarget.getColorTextureView();
        GpuTextureView depthView = mainRenderTarget.getDepthTextureView();

        GpuBufferSlice dynamicSlice = RenderSystem.getDynamicUniforms()
                .writeTransform(matrix4fStack, colorUniform, new Vector3f(), new Matrix4f());

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(() -> "Constellation " + c.id(), colorView, Optional.empty(), depthView, OptionalDouble.empty())) {
            if (entry.textured()) {
                renderPass.setPipeline(RenderPipelineRegistry.CELESTIAL_BLEND);
                renderPass.bindTexture("Sampler0", celestialAtlas.getTextureView(), celestialAtlas.getSampler());
            } else {
                renderPass.setPipeline(RenderPipelines.STARS);
            }
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicSlice);
            renderPass.setVertexBuffer(0, entry.gpuBuffer().slice());
            renderPass.setIndexBuffer(indexBuffer, quadIndices.type());
            renderPass.drawIndexed(entry.indexCount(), 1, 0, 0, 0);
        }

        matrix4fStack.popMatrix();
    }
}
