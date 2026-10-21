package fr.tathan.sky_aesthetics.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.registry.RenderPipelineRegistry;
import fr.tathan.sky_aesthetics.client.settings.SkyBoxLayer;
import fr.tathan.sky_aesthetics.client.settings.SkyTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import java.lang.Math;
import org.joml.*;

import java.util.*;

/**
 * The class handling the rendering of the skybox layers of a sky
 * Call {@link #prepare(SkyClock)} every frame before drawing the slots
 */
public class SkyboxRenderer {

    private static final float RADIUS = 100.0f;
    private static final float PI = (float) Math.PI;

    private final TextureAtlas celestialsAtlas;
    private final List<LayerState> layers = new ArrayList<>();
    private final Map<MeshKey, Mesh> meshes = new HashMap<>();
    private final Map<SkyBoxLayer.HidePart, Float> hideFactors = new EnumMap<>(SkyBoxLayer.HidePart.class);
    private SkyClock clock;

    public SkyboxRenderer(List<SkyBoxLayer> layers, TextureAtlas celestialsAtlas) {
        this.celestialsAtlas = celestialsAtlas;
        for (SkyBoxLayer layer : layers) {
            this.layers.add(new LayerState(layer));
        }
    }

    public boolean isEmpty() {
        return this.layers.isEmpty();
    }

    public void prepare(SkyClock clock) {
        this.clock = clock;
        this.hideFactors.clear();

        for (LayerState state : this.layers) {
            SkyBoxLayer.Visibility visibility = state.layer.visibility();
            float conditionWeight = 1.0f;
            if (visibility.condition().isPresent()) {
                float target = visibility.condition().get().isVisible(clock) ? 1.0f : 0.0f;
                conditionWeight = state.fadeCondition(target, clock.gameTime(), visibility.conditionFade());
            }

            state.alpha = visibility.getOpacity(clock) * conditionWeight;
            for (SkyBoxLayer.HidePart part : state.layer.hide()) {
                this.hideFactors.merge(part, state.alpha, Math::max);
            }
        }
    }

    /** 0 to keep the vanilla part, 1 to hide it */
    public float hideFactor(SkyBoxLayer.HidePart part) {
        return this.hideFactors.getOrDefault(part, 0.0f);
    }

    /** @param alphaMultiplier Used to fade out the previous sky when the sky changes */
    public void draw(SkyBoxLayer.Slot slot, float alphaMultiplier) {
        if (this.clock == null || alphaMultiplier <= 0.0f) return;

        for (LayerState state : this.layers) {
            if (state.layer.slot() != slot) continue;

            float alpha = state.alpha * alphaMultiplier;
            if (alpha >= 0.004f) {
                drawLayer(state, alpha);
            }
        }
    }

    private void drawLayer(LayerState state, float alpha) {
        SkyBoxLayer.Look look = state.layer.look();

        Matrix4f layerMatrix = state.layer.rotation().map(rotation -> rotation.matrix(this.clock)).orElseGet(Matrix4f::new);
        // The world up seen from the layer, so the shader can find the horizon when the layer turns
        Vector3f localUp = new Matrix3f(layerMatrix).transpose().transform(new Vector3f(0.0f, 1.0f, 0.0f));

        Vector4f color = getLayerColor(look, alpha);
        RenderPipeline pipeline = getPipeline(look.blend(), alpha);

        // The shader settings are sent in the last column of the texture matrix, which the UVs don't use
        float blendMode = switch (look.blend()) {
            case MULTIPLY -> 1.0f;
            case SCREEN -> 2.0f;
            default -> 0.0f;
        };
        float upperHemisphere = look.hemisphere() == SkyBoxLayer.Hemisphere.UPPER ? 1.0f : 0.0f;
        float horizonFade = (float) Math.sin(Math.toRadians(look.horizonFade()));
        float fogFade = look.fogFade() ? 1.0f : 0.0f;

        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul(layerMatrix);

        for (Draw draw : state.getDraws(this)) {
            Matrix4f textureMatrix = draw.standalone() ? getUvTransform(look, draw.panorama()) : new Matrix4f();
            textureMatrix.m20(blendMode).m21(upperHemisphere).m22(horizonFade).m23(fogFade);

            GpuBufferSlice dynamicSlice = RenderSystem.getDynamicUniforms().writeTransform(matrix4fStack, color, localUp, textureMatrix);
            renderDraw(pipeline, dynamicSlice, draw);
        }

        matrix4fStack.popMatrix();
    }

    private static void renderDraw(RenderPipeline pipeline, GpuBufferSlice dynamicSlice, Draw draw) {
        RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = quadIndices.getBuffer(draw.mesh().indexCount());

        GpuTextureView colorView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView depthView = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(() -> "Skybox layer", colorView, OptionalInt.empty(), depthView, OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicSlice);
            renderPass.bindTexture("Sampler0", draw.texture().getTextureView(), draw.texture().sampler());
            renderPass.setVertexBuffer(0, draw.mesh().buffer());
            renderPass.setIndexBuffer(indexBuffer, quadIndices.type());
            renderPass.drawIndexed(0, 0, draw.mesh().indexCount(), 1);
        }
    }

    private static RenderPipeline getPipeline(SkyBoxLayer.Blend blend, float alpha) {
        return switch (blend) {
            // An opaque layer that fades needs to blend
            case OPAQUE -> alpha >= 0.999f ? RenderPipelineRegistry.SKYBOX_OPAQUE : RenderPipelineRegistry.SKYBOX_ALPHA;
            case ALPHA -> RenderPipelineRegistry.SKYBOX_ALPHA;
            case ADD -> RenderPipelineRegistry.SKYBOX_ADD;
            case MULTIPLY -> RenderPipelineRegistry.SKYBOX_MULTIPLY;
            case SCREEN -> RenderPipelineRegistry.SKYBOX_SCREEN;
        };
    }

    private Vector4f getLayerColor(SkyBoxLayer.Look look, float alpha) {
        float r = 1.0f;
        float g = 1.0f;
        float b = 1.0f;
        if (look.color().isPresent()) {
            Vector3i color = look.color().get();
            r = color.x / 255.0f;
            g = color.y / 255.0f;
            b = color.z / 255.0f;
        }
        if (look.tintWithSky()) {
            int skyColor = this.clock.skyColor();
            r *= ((skyColor >> 16) & 0xFF) / 255.0f;
            g *= ((skyColor >> 8) & 0xFF) / 255.0f;
            b *= (skyColor & 0xFF) / 255.0f;
        }
        return new Vector4f(r * look.brightness(), g * look.brightness(), b * look.brightness(), alpha);
    }

    private Matrix4f getUvTransform(SkyBoxLayer.Look look, boolean panorama) {
        Matrix4f matrix = new Matrix4f();
        double time = this.clock.gameTime();

        float u = 0.0f;
        float v = 0.0f;
        if (panorama) {
            u = (float) frac(look.scroll().x * time);
            v = (float) frac(look.scroll().y * time);
        }

        if (look.animation().isPresent()) {
            SkyBoxLayer.Animation animation = look.animation().get();
            int frame = (int) (Math.floor(time / animation.frametime()) % animation.frames());
            float frameHeight = 1.0f / animation.frames();
            matrix.translate(u, (frame + v) * frameHeight, 0.0f).scale(1.0f, frameHeight, 1.0f);
        } else if (u != 0.0f || v != 0.0f) {
            matrix.translate(u, v, 0.0f);
        }
        return matrix;
    }

    private static double frac(double value) {
        return value - Math.floor(value);
    }

    // -------------------------------------------------------------------------
    // Meshes and textures
    // -------------------------------------------------------------------------

    private List<Draw> buildDraws(SkyBoxLayer layer) {
        SkyTexture texture = layer.texture();
        SkyBoxLayer.Look look = layer.look();
        List<Draw> draws = new ArrayList<>();

        if (texture.cubemap().isPresent()) {
            List<Identifier> faces = texture.cubemap().get().faces();
            SkyTexture.Face[] order = SkyTexture.Face.values();
            for (int i = 0; i < order.length; i++) {
                Identifier id = faces.get(i);
                warnIfAtlasCannotAnimate(id, look);

                Mesh mesh = getMesh(new MeshKey(0, false, order[i], getUvRect(id)));
                LayerTexture faceTexture = getTexture(id, AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE);
                draws.add(new Draw(mesh, faceTexture, SkyTexture.isStandalone(id), false));
            }
            return draws;
        }

        Identifier id = texture.panorama().orElseThrow();
        warnIfAtlasCannotAnimate(id, look);

        Mesh mesh = getMesh(new MeshKey(Math.max(4, look.gradation()), layer.legacy(), null, getUvRect(id)));
        AddressMode addressModeV = look.scroll().y != 0.0f ? AddressMode.REPEAT : AddressMode.CLAMP_TO_EDGE;
        LayerTexture panoramaTexture = getTexture(id, AddressMode.REPEAT, addressModeV);
        draws.add(new Draw(mesh, panoramaTexture, SkyTexture.isStandalone(id), true));
        return draws;
    }

    private static void warnIfAtlasCannotAnimate(Identifier id, SkyBoxLayer.Look look) {
        boolean movesUv = look.animation().isPresent() || look.scroll().x != 0.0f || look.scroll().y != 0.0f;
        if (movesUv && !SkyTexture.isStandalone(id)) {
            SkyAesthetics.LOG.warn("Skybox texture {} is a celestial atlas sprite, 'scroll' and 'animation' only work with standalone textures (an id ending with .png)", id);
        }
    }

    private Mesh getMesh(MeshKey key) {
        return this.meshes.computeIfAbsent(key, SkyboxRenderer::buildMesh);
    }

    private LayerTexture getTexture(Identifier id, AddressMode addressModeU, AddressMode addressModeV) {
        if (SkyTexture.isStandalone(id)) {
            AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(id);
            GpuSampler sampler = RenderSystem.getSamplerCache().getSampler(addressModeU, addressModeV, FilterMode.LINEAR, FilterMode.LINEAR, false);
            return new LayerTexture(texture, null, sampler);
        }
        return new LayerTexture(null, this.celestialsAtlas, this.celestialsAtlas.getSampler());
    }

    private UvRect getUvRect(Identifier id) {
        if (SkyTexture.isStandalone(id)) return UvRect.FULL;

        TextureAtlasSprite sprite = this.celestialsAtlas.getSprite(id);
        return new UvRect(sprite.getU(0.0f), sprite.getU(1.0f), sprite.getV(0.0f), sprite.getV(1.0f));
    }

    private static Mesh buildMesh(MeshKey key) {
        int quads = key.face() == null ? key.gradation() * key.gradation() * 2 : 1;

        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION_TEX.getVertexSize() * quads * 4)) {
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            if (key.face() != null) {
                buildFace(bufferBuilder, key.face(), key.uv());
            } else if (key.legacy()) {
                buildLegacySphere(bufferBuilder, key.gradation(), key.uv());
            } else {
                buildSphere(bufferBuilder, key.gradation(), key.uv());
            }

            try (MeshData meshData = bufferBuilder.buildOrThrow()) {
                int indexCount = meshData.drawState().indexCount();
                GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Skybox layer", 40, meshData.vertexBuffer());
                return new Mesh(gpuBuffer, indexCount);
            }
        }
    }

    /**
     * A panorama sphere with the top of the image at the zenith
     * The center of the image faces north and the image goes to the east from left to right
     */
    private static void buildSphere(BufferBuilder bufferBuilder, int gradation, UvRect uv) {
        int columns = gradation * 2;
        for (int i = 0; i < gradation; i++) {
            float v1 = (float) i / gradation;
            float v2 = (float) (i + 1) / gradation;

            for (int j = 0; j < columns; j++) {
                float u1 = (float) j / columns;
                float u2 = (float) (j + 1) / columns;

                addSphereVertex(bufferBuilder, u1, v1, uv);
                addSphereVertex(bufferBuilder, u2, v1, uv);
                addSphereVertex(bufferBuilder, u2, v2, uv);
                addSphereVertex(bufferBuilder, u1, v2, uv);
            }
        }
    }

    private static void addSphereVertex(BufferBuilder bufferBuilder, float u, float v, UvRect uv) {
        float elevation = (0.5f - v) * PI;
        float azimuth = (u - 0.5f) * 2.0f * PI;
        float horizontal = (float) Math.cos(elevation);

        float x = RADIUS * horizontal * (float) Math.sin(azimuth);
        float y = RADIUS * (float) Math.sin(elevation);
        float z = -RADIUS * horizontal * (float) Math.cos(azimuth);
        bufferBuilder.addVertex(x, y, z).setUv(uv.getU(u), uv.getV(v));
    }

    /** The sphere of the old sky_box (poles on the Z axis), so old skies look the same */
    private static void buildLegacySphere(BufferBuilder bufferBuilder, int gradation, UvRect uv) {
        for (int i = 0; i < gradation; i++) {
            float alpha1 = i * PI / gradation;
            float alpha2 = (i + 1) * PI / gradation;

            for (int j = 0; j < gradation * 2; j++) {
                float beta1 = j * 2 * PI / (gradation * 2);
                float beta2 = (j + 1) * 2 * PI / (gradation * 2);

                float x1 = RADIUS * (float)(Math.sin(alpha1) * Math.cos(beta1));
                float y1 = RADIUS * (float)(Math.sin(alpha1) * Math.sin(beta1));
                float z1 = RADIUS * (float) Math.cos(alpha1);

                float x2 = RADIUS * (float)(Math.sin(alpha1) * Math.cos(beta2));
                float y2 = RADIUS * (float)(Math.sin(alpha1) * Math.sin(beta2));

                float x3 = RADIUS * (float)(Math.sin(alpha2) * Math.cos(beta1));
                float y3 = RADIUS * (float)(Math.sin(alpha2) * Math.sin(beta1));
                float z3 = RADIUS * (float) Math.cos(alpha2);

                float x4 = RADIUS * (float)(Math.sin(alpha2) * Math.cos(beta2));
                float y4 = RADIUS * (float)(Math.sin(alpha2) * Math.sin(beta2));

                float u1 = uv.getU(beta1 / (2 * PI));
                float u2 = uv.getU(beta2 / (2 * PI));
                float v1 = uv.getV(alpha1 / PI);
                float v2 = uv.getV(alpha2 / PI);

                bufferBuilder.addVertex(x1, y1, z1).setUv(u1, v1);
                bufferBuilder.addVertex(x2, y2, z1).setUv(u2, v1);
                bufferBuilder.addVertex(x4, y4, z3).setUv(u2, v2);
                bufferBuilder.addVertex(x3, y3, z3).setUv(u1, v2);
            }
        }
    }

    /**
     * A face of the cubemap, as seen from inside the sky
     * North is -Z and east is +X
     */
    private static void buildFace(BufferBuilder bufferBuilder, SkyTexture.Face face, UvRect uv) {
        float[][] corners = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
        for (float[] corner : corners) {
            float u = corner[0];
            float v = corner[1];
            float a = RADIUS * (2 * u - 1);
            float b = RADIUS * (1 - 2 * v);

            Vector3f position = switch (face) {
                case NORTH -> new Vector3f(a, b, -RADIUS);
                case SOUTH -> new Vector3f(-a, b, RADIUS);
                case EAST -> new Vector3f(RADIUS, b, a);
                case WEST -> new Vector3f(-RADIUS, b, -a);
                case UP -> new Vector3f(-a, RADIUS, -b);
                case DOWN -> new Vector3f(a, -RADIUS, -b);
            };
            bufferBuilder.addVertex(position.x, position.y, position.z).setUv(uv.getU(u), uv.getV(v));
        }
    }

    public void close() {
        this.meshes.values().forEach(mesh -> mesh.buffer().close());
        this.meshes.clear();
        this.layers.forEach(state -> state.draws = null);
    }

    // -------------------------------------------------------------------------
    // Inner types
    // -------------------------------------------------------------------------

    private static class LayerState {
        final SkyBoxLayer layer;
        float alpha;
        List<Draw> draws;
        float conditionWeight = -1.0f;
        double lastConditionTime;

        LayerState(SkyBoxLayer layer) {
            this.layer = layer;
        }

        float fadeCondition(float target, double gameTime, int fadeTicks) {
            if (this.conditionWeight < 0.0f || fadeTicks <= 0) {
                this.conditionWeight = target;
            } else {
                double elapsed = Math.clamp(gameTime - this.lastConditionTime, 0.0, 20.0);
                float step = (float) (elapsed / fadeTicks);
                if (target > this.conditionWeight) {
                    this.conditionWeight = Math.min(target, this.conditionWeight + step);
                } else {
                    this.conditionWeight = Math.max(target, this.conditionWeight - step);
                }
            }
            this.lastConditionTime = gameTime;
            return this.conditionWeight;
        }

        List<Draw> getDraws(SkyboxRenderer renderer) {
            if (this.draws == null) {
                this.draws = renderer.buildDraws(this.layer);
            }
            return this.draws;
        }
    }

    private record Draw(Mesh mesh, LayerTexture texture, boolean standalone, boolean panorama) {}

    private record Mesh(GpuBuffer buffer, int indexCount) {}

    private record MeshKey(int gradation, boolean legacy, SkyTexture.Face face, UvRect uv) {}

    private record UvRect(float u0, float u1, float v0, float v1) {
        static final UvRect FULL = new UvRect(0.0f, 1.0f, 0.0f, 1.0f);

        float getU(float delta) {
            return this.u0 + (this.u1 - this.u0) * delta;
        }

        float getV(float delta) {
            return this.v0 + (this.v1 - this.v0) * delta;
        }
    }

    /** A texture of the texture manager (read every frame to follow reloads) or the celestial atlas */
    private record LayerTexture(AbstractTexture texture, TextureAtlas atlas, GpuSampler sampler) {
        GpuTextureView getTextureView() {
            return this.texture != null ? this.texture.getTextureView() : this.atlas.getTextureView();
        }
    }
}
