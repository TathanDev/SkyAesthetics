package fr.tathan.sky_aesthetics.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import fr.tathan.sky_aesthetics.client.data.ConstellationsData;
import fr.tathan.sky_aesthetics.client.settings.Constellation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.util.RandomSource;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class ConstellationRenderer {

    private record BufferEntry(GpuBuffer gpuBuffer, int indexCount) {}

    private static final Map<String, BufferEntry> BUFFERS = new HashMap<>();
    private static boolean dirty = true;

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

        RandomSource rng = RandomSource.create(c.id().hashCode());

        GpuBuffer gpuBuffer;
        int indexCount;

        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(
                DefaultVertexFormat.POSITION.getVertexSize() * totalPoints * 4)) {
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

            for (net.minecraft.world.phys.Vec3 point : allPoints) {
                Vector3f pos = new Vector3f((float) point.x, (float) point.y, (float) point.z).normalize(100.0F);
                float k = quadHalf;
                float angle = (float) (rng.nextDouble() * Math.PI * 2.0);
                Matrix3f orient = new Matrix3f()
                        .rotateTowards(new Vector3f(pos).negate(), new Vector3f(0.0F, 1.0F, 0.0F))
                        .rotateZ(-angle);

                bufferBuilder.addVertex(new Vector3f(k, -k, 0.0F).mul(orient).add(pos));
                bufferBuilder.addVertex(new Vector3f(k,  k, 0.0F).mul(orient).add(pos));
                bufferBuilder.addVertex(new Vector3f(-k, k, 0.0F).mul(orient).add(pos));
                bufferBuilder.addVertex(new Vector3f(-k, -k, 0.0F).mul(orient).add(pos));
            }

            try (MeshData meshData = bufferBuilder.buildOrThrow()) {
                indexCount = meshData.drawState().indexCount();
                final String id = c.id();
                gpuBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Constellation " + id + " stars", 40, meshData.vertexBuffer());
            }
        }

        return new BufferEntry(gpuBuffer, indexCount);
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

        RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = quadIndices.getBuffer(entry.indexCount());

        GpuTextureView colorView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView depthView = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();

        GpuBufferSlice dynamicSlice = RenderSystem.getDynamicUniforms()
                .writeTransform(matrix4fStack, colorUniform, new Vector3f(), new Matrix4f());

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(() -> "Constellation " + c.id(), colorView, OptionalInt.empty(), depthView, OptionalDouble.empty())) {
            renderPass.setPipeline(RenderPipelines.STARS);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicSlice);
            renderPass.setVertexBuffer(0, entry.gpuBuffer());
            renderPass.setIndexBuffer(indexBuffer, quadIndices.type());
            renderPass.drawIndexed(0, 0, entry.indexCount(), 1);
        }

        matrix4fStack.popMatrix();
    }
}
