package fr.tathan.sky_aesthetics.client;


import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import fr.tathan.sky_aesthetics.client.registry.RenderPipelineRegistry;
import fr.tathan.sky_aesthetics.client.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.data.AtlasIds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.MoonPhase;
import java.lang.Math;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * The class handling the rendering of a custom sky
 */
public class DimensionRenderer {

    private static final float VANILLA_SUN_SIZE = 30.0f;
    private static final float VANILLA_MOON_SIZE = 20.0f;

    public final List<SkyObject> skyObjects;
    public final TextureAtlas celestialsAtlas;
    public final StarSettings starSettings;
    public final boolean weather;
    public final CustomVanillaObject.Sun customSun;
    public final CustomVanillaObject.Moon customMoon;
    public final SkyColorSettings skyColorSettings;
    public final LightSettings lightSettings;
    public final SkyBoxSetting skyBoxSetting;
    public final SkyProperties.RenderCondition renderCondition;
    public final StarSettings.BufferHolder gpuBuffer;
    public final FogSettings fogSettings;

    private final GpuBuffer skyboxBuffer;
    private final int skyboxIndexCount;
    private final GpuBuffer customFogBuffer;

    private final List<ActiveShootingStar> activeShootingStars = new ArrayList<>();
    private final RandomSource shootingStarRng = RandomSource.create();
    private long lastGameTime = -1;

    private DimensionRenderer(List<SkyObject> skyObjects,
                              boolean weather,
                              CustomVanillaObject.Sun customSun,
                              CustomVanillaObject.Moon customMoon,
                              SkyColorSettings skyColorSettings,
                              LightSettings lightSettings,
                              StarSettings starSettings,
                              SkyProperties.RenderCondition renderCondition,
                              SkyBoxSetting skyBoxSetting,
                              FogSettings fogSettings) {
        this.skyObjects = skyObjects;
        this.weather = weather;
        this.celestialsAtlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.CELESTIALS);
        this.customSun = customSun;
        this.customMoon = customMoon;
        this.skyColorSettings = skyColorSettings;
        this.lightSettings = lightSettings;
        this.starSettings = starSettings;
        this.gpuBuffer = starSettings.buildCustomStars();
        this.renderCondition = renderCondition;
        this.skyBoxSetting = skyBoxSetting;
        this.fogSettings = fogSettings;
        if (skyBoxSetting != null) {
            SkyboxMesh mesh = buildSkyboxMesh(skyBoxSetting, this.celestialsAtlas);
            this.skyboxBuffer = mesh.buffer();
            this.skyboxIndexCount = mesh.indexCount();
        } else {
            this.skyboxBuffer = null;
            this.skyboxIndexCount = 0;
        }
        this.customFogBuffer = (fogSettings != null && fogSettings.needsCustomBuffer())
                ? fogSettings.buildFogBuffer() : null;
    }

    /**
     * Returns the fog GpuBufferSlice to use for this sky's render pass.
     * When fog settings are present and override vanilla, returns a slice of the
     * pre-built custom fog buffer; otherwise returns the vanilla skyFog slice.
     */
    public GpuBufferSlice getCustomFogSlice(GpuBufferSlice vanillaFog) {
        return customFogBuffer != null ? customFogBuffer.slice() : vanillaFog;
    }

    public boolean canRenderSky() {
        if(this.renderCondition == null) {
            return true;
        }
        return this.renderCondition.isSkyRendered(getServerLevel());
    }

    public void render(SkyRenderState skyRenderState, SkyRenderer skyRenderer) {
       PoseStack poseStack = new PoseStack();

       tickShootingStars(skyRenderState.starBrightness);

       renderSkybox(poseStack, skyRenderState.sunAngle);

       // Sky disc — use custom color if configured
       if (skyColorSettings != null && skyColorSettings.color().isPresent()) {
           Vector4f c = skyColorSettings.color().get();
           int packed = packArgb((int)(c.w * 255), (int)(c.x * 255), (int)(c.y * 255), (int)(c.z * 255));
           skyRenderer.renderSkyDisc(packed);
       } else {
           skyRenderer.renderSkyDisc(skyRenderState.skyColor);
       }

       // Sunrise/sunset — use custom sunset color if configured
       if (skyColorSettings != null && skyColorSettings.sunsetColor().isPresent()) {
           Vector3i s = skyColorSettings.sunsetColor().get();
           int alpha = skyColorSettings.sunriseAlphaModifier().orElse(255);
           int packed = packArgb(alpha, s.x, s.y, s.z);
           skyRenderer.renderSunriseAndSunset(poseStack, skyRenderState.sunAngle, packed);
       } else {
           skyRenderer.renderSunriseAndSunset(poseStack, skyRenderState.sunAngle, skyRenderState.sunriseAndSunsetColor);
       }

       this.starSettings.renderStars(poseStack, skyRenderState, skyRenderer, skyRenderState.starAngle, this.gpuBuffer);

       ConstellationRenderer.renderAll(poseStack, skyRenderState);

       renderShootingStars(poseStack, skyRenderState);

       this.renderSkyObjects(poseStack, skyRenderState.sunAngle, skyRenderState.moonAngle, skyRenderState.moonPhase, skyRenderState.rainBrightness, skyRenderer);

       if (skyRenderState.shouldRenderDarkDisc) {
           skyRenderer.renderDarkDisc();
       }
   }

    public void renderSkyObjects(PoseStack poseStack, float sunAngle, float moonAngle, MoonPhase moonPhase, float rainBrightness, SkyRenderer skyRenderer) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));

        if (this.customSun == null || this.customSun.show()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotation(sunAngle));
            float sunBrightness = rainBrightness * (this.customSun != null ? this.customSun.intensity() : 1.0f);
            if (this.customSun != null && this.customSun.sunTexture().isPresent()) {
                renderCustomSun(sunBrightness, poseStack);
            } else {
                if (this.customSun != null && this.customSun.size() != VANILLA_SUN_SIZE) {
                    float factor = this.customSun.size() / VANILLA_SUN_SIZE;
                    poseStack.scale(factor, 1.0f, factor);
                }
                skyRenderer.renderSun(sunBrightness, poseStack);
            }
            poseStack.popPose();
        }

        if (this.customMoon == null || this.customMoon.show()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotation(moonAngle));
            float moonBrightness = rainBrightness * (this.customMoon != null ? this.customMoon.intensity() : 1.0f);
            if (this.customMoon != null && this.customMoon.moonTexture().isPresent()) {
                renderCustomMoon(moonPhase, moonBrightness, poseStack);
            } else {
                if (this.customMoon != null && this.customMoon.size() != VANILLA_MOON_SIZE) {
                    float factor = this.customMoon.size() / VANILLA_MOON_SIZE;
                    poseStack.scale(factor, 1.0f, factor);
                }
                skyRenderer.renderMoon(moonPhase, moonBrightness, poseStack);
            }
            poseStack.popPose();
        }

        for(SkyObject skyObject : skyObjects) {
            skyObject.renderObject(1, poseStack, this.celestialsAtlas, sunAngle);
        }

        poseStack.popPose();
    }

    // -------------------------------------------------------------------------
    // Skybox
    // -------------------------------------------------------------------------

    private void renderSkybox(PoseStack poseStack, float sunAngle) {
        if (skyBoxSetting == null || skyboxBuffer == null || skyboxIndexCount == 0) return;

        poseStack.pushPose();

        poseStack.mulPose(Axis.XP.rotationDegrees(skyBoxSetting.rotation().x));
        poseStack.mulPose(Axis.YP.rotationDegrees(skyBoxSetting.rotation().y));
        poseStack.mulPose(Axis.ZP.rotationDegrees(skyBoxSetting.rotation().z));

        skyBoxSetting.dynamicRotation().ifPresent(r -> r.rotatePoseStack(poseStack.last().pose(), sunAngle));

        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul(poseStack.last().pose());

        RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = quadIndices.getBuffer(skyboxIndexCount);

        GpuBufferSlice dynamicSlice = RenderSystem.getDynamicUniforms()
                .writeTransform(matrix4fStack, new Vector4f(1f, 1f, 1f, 1f), new Vector3f(), new Matrix4f());

        GpuTextureView colorView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView depthView = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(() -> "Skybox", colorView, OptionalInt.empty(), depthView, OptionalDouble.empty())) {
            renderPass.setPipeline(RenderPipelineRegistry.CELESTIAL_NO_BLEND);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicSlice);
            renderPass.bindTexture("Sampler0", this.celestialsAtlas.getTextureView(), this.celestialsAtlas.getSampler());
            renderPass.setVertexBuffer(0, skyboxBuffer);
            renderPass.setIndexBuffer(indexBuffer, quadIndices.type());
            renderPass.drawIndexed(0, 0, skyboxIndexCount, 1);
        }

        matrix4fStack.popMatrix();
        poseStack.popPose();
    }

    private record SkyboxMesh(GpuBuffer buffer, int indexCount) {}

    private static SkyboxMesh buildSkyboxMesh(SkyBoxSetting setting, TextureAtlas atlas) {
        int g = Math.max(4, setting.gradation());
        int totalCells = g * (g * 2);
        int totalVertices = totalCells * 4;
        float PI = (float) Math.PI;

        TextureAtlasSprite sprite = atlas.getSprite(setting.texture());

        try (ByteBufferBuilder bbb = ByteBufferBuilder.exactlySized(
                DefaultVertexFormat.POSITION_TEX.getVertexSize() * totalVertices)) {
            BufferBuilder bb = new BufferBuilder(bbb, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            for (int i = 0; i < g; i++) {
                float alpha1 = i * PI / g;
                float alpha2 = (i + 1) * PI / g;

                for (int j = 0; j < g * 2; j++) {
                    float beta1 = j * 2 * PI / (g * 2);
                    float beta2 = (j + 1) * 2 * PI / (g * 2);

                    float x1 = 100f * (float)(Math.sin(alpha1) * Math.cos(beta1));
                    float y1 = 100f * (float)(Math.sin(alpha1) * Math.sin(beta1));
                    float z1 = 100f * (float) Math.cos(alpha1);

                    float x2 = 100f * (float)(Math.sin(alpha1) * Math.cos(beta2));
                    float y2 = 100f * (float)(Math.sin(alpha1) * Math.sin(beta2));

                    float x3 = 100f * (float)(Math.sin(alpha2) * Math.cos(beta1));
                    float y3 = 100f * (float)(Math.sin(alpha2) * Math.sin(beta1));
                    float z3 = 100f * (float) Math.cos(alpha2);

                    float x4 = 100f * (float)(Math.sin(alpha2) * Math.cos(beta2));
                    float y4 = 100f * (float)(Math.sin(alpha2) * Math.sin(beta2));

                    float u1 = sprite.getU(beta1 / (2 * PI));
                    float u2 = sprite.getU(beta2 / (2 * PI));
                    float v1 = sprite.getV(alpha1 / PI);
                    float v2 = sprite.getV(alpha2 / PI);

                    bb.addVertex(x1, y1, z1).setUv(u1, v1);
                    bb.addVertex(x2, y2, z1).setUv(u2, v1);
                    bb.addVertex(x4, y4, z3).setUv(u2, v2);
                    bb.addVertex(x3, y3, z3).setUv(u1, v2);
                }
            }

            try (MeshData mesh = bb.buildOrThrow()) {
                int indexCount = mesh.drawState().indexCount();
                GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Skybox sphere", 40, mesh.vertexBuffer());
                return new SkyboxMesh(gpuBuffer, indexCount);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Custom sun
    // -------------------------------------------------------------------------

    private void renderCustomSun(float rainBrightness, PoseStack poseStack) {
        TextureAtlasSprite sprite = this.celestialsAtlas.getSprite(this.customSun.sunTexture().get());

        GpuBuffer sunBuffer;
        try (ByteBufferBuilder bbb = ByteBufferBuilder.exactlySized(
                DefaultVertexFormat.POSITION_TEX.getVertexSize() * 4)) {
            BufferBuilder bb = new BufferBuilder(bbb, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bb.addVertex(-0.5F, -0.5F, 0.0F).setUv(sprite.getU0(), sprite.getV1());
            bb.addVertex(-0.5F,  0.5F, 0.0F).setUv(sprite.getU0(), sprite.getV0());
            bb.addVertex( 0.5F,  0.5F, 0.0F).setUv(sprite.getU1(), sprite.getV0());
            bb.addVertex( 0.5F, -0.5F, 0.0F).setUv(sprite.getU1(), sprite.getV1());
            try (MeshData mesh = bb.buildOrThrow()) {
                sunBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Custom sun", 40, mesh.vertexBuffer());
            }
        }

        RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = quadIndices.getBuffer(6);

        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul(poseStack.last().pose());
        matrix4fStack.translate(0.0F, 100.0F, 0.0F);
        matrix4fStack.scale(this.customSun.size(), 1.0F, this.customSun.size());

        GpuBufferSlice dynamicSlice = RenderSystem.getDynamicUniforms()
                .writeTransform(matrix4fStack, new Vector4f(1f, 1f, 1f, rainBrightness), new Vector3f(), new Matrix4f());

        GpuTextureView colorView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView depthView = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(() -> "Custom sun", colorView, OptionalInt.empty(), depthView, OptionalDouble.empty())) {
            renderPass.setPipeline(RenderPipelineRegistry.CELESTIAL_NO_BLEND);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicSlice);
            renderPass.bindTexture("Sampler0", this.celestialsAtlas.getTextureView(), this.celestialsAtlas.getSampler());
            renderPass.setVertexBuffer(0, sunBuffer);
            renderPass.setIndexBuffer(indexBuffer, quadIndices.type());
            renderPass.drawIndexed(0, 0, 6, 1);
        }

        matrix4fStack.popMatrix();
        sunBuffer.close();
    }

    // -------------------------------------------------------------------------
    // Custom moon
    // -------------------------------------------------------------------------

    private void renderCustomMoon(MoonPhase moonPhase, float rainBrightness, PoseStack poseStack) {
        TextureAtlasSprite sprite = this.celestialsAtlas.getSprite(this.customMoon.moonTexture().get());

        float u0, u1, v0, v1;
        if (this.customMoon.showPhases()) {
            int col = moonPhase.ordinal() % 4;
            int row = moonPhase.ordinal() / 4;
            u0 = sprite.getU(col / 4.0f);
            u1 = sprite.getU((col + 1) / 4.0f);
            v0 = sprite.getV(row / 2.0f);
            v1 = sprite.getV((row + 1) / 2.0f);
        } else {
            u0 = sprite.getU0();
            u1 = sprite.getU1();
            v0 = sprite.getV0();
            v1 = sprite.getV1();
        }

        GpuBuffer moonBuffer;
        try (ByteBufferBuilder bbb = ByteBufferBuilder.exactlySized(
                DefaultVertexFormat.POSITION_TEX.getVertexSize() * 4)) {
            BufferBuilder bb = new BufferBuilder(bbb, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bb.addVertex(-0.5F, -0.5F, 0.0F).setUv(u0, v1);
            bb.addVertex(-0.5F,  0.5F, 0.0F).setUv(u0, v0);
            bb.addVertex( 0.5F,  0.5F, 0.0F).setUv(u1, v0);
            bb.addVertex( 0.5F, -0.5F, 0.0F).setUv(u1, v1);
            try (MeshData mesh = bb.buildOrThrow()) {
                moonBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Custom moon", 40, mesh.vertexBuffer());
            }
        }

        RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = quadIndices.getBuffer(6);

        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul(poseStack.last().pose());
        matrix4fStack.translate(0.0F, 100.0F, 0.0F);
        matrix4fStack.scale(this.customMoon.size(), 1.0F, this.customMoon.size());

        GpuBufferSlice dynamicSlice = RenderSystem.getDynamicUniforms()
                .writeTransform(matrix4fStack, new Vector4f(1f, 1f, 1f, rainBrightness), new Vector3f(), new Matrix4f());

        GpuTextureView colorView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView depthView = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(() -> "Custom moon", colorView, OptionalInt.empty(), depthView, OptionalDouble.empty())) {
            renderPass.setPipeline(RenderPipelineRegistry.CELESTIAL_NO_BLEND);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicSlice);
            renderPass.bindTexture("Sampler0", this.celestialsAtlas.getTextureView(), this.celestialsAtlas.getSampler());
            renderPass.setVertexBuffer(0, moonBuffer);
            renderPass.setIndexBuffer(indexBuffer, quadIndices.type());
            renderPass.drawIndexed(0, 0, 6, 1);
        }

        matrix4fStack.popMatrix();
        moonBuffer.close();
    }

    // -------------------------------------------------------------------------
    // Shooting stars
    // -------------------------------------------------------------------------

    private void tickShootingStars(float starBrightness) {
        if (starSettings.shootingStars().isEmpty()) return;
        if (Minecraft.getInstance().level == null) return;

        long currentTime = Minecraft.getInstance().level.getGameTime();
        if (currentTime == lastGameTime) return;
        lastGameTime = currentTime;

        StarSettings.ShootingStars config = starSettings.shootingStars().get();

        activeShootingStars.forEach(star -> {
            star.remainingTicks--;
            star.position.add(new Vector3f(star.direction).mul(config.speed() * 0.5f)).normalize(100.0f);
        });
        activeShootingStars.removeIf(star -> star.remainingTicks <= 0);

        if (starBrightness > 0 && shootingStarRng.nextInt(100) < config.percentage()) {
            spawnShootingStar(config);
        }
    }

    private void spawnShootingStar(StarSettings.ShootingStars config) {
        float px = shootingStarRng.nextFloat() * 2 - 1;
        float py = shootingStarRng.nextFloat() * 2 - 1;
        float pz = shootingStarRng.nextFloat() * 2 - 1;
        Vector3f pos = new Vector3f(px, py, pz).normalize(100.0f);

        float dx = shootingStarRng.nextFloat() * 2 - 1;
        float dy = shootingStarRng.nextFloat() * 2 - 1;
        float dz = shootingStarRng.nextFloat() * 2 - 1;
        Vector3f dir = new Vector3f(dx, dy, dz);
        Vector3f radial = new Vector3f(pos).normalize();
        float dot = dir.dot(radial);
        dir.sub(new Vector3f(radial).mul(dot)).normalize();

        if (config.rotation().isPresent() && config.rotation().get() != 0) {
            float rad = (float) Math.toRadians(config.rotation().get());
            float cosA = (float) Math.cos(rad);
            float sinA = (float) Math.sin(rad);
            Vector3f k = new Vector3f(radial);
            Vector3f rotated = new Vector3f(dir).mul(cosA)
                    .add(new Vector3f(k).cross(dir).mul(sinA))
                    .add(new Vector3f(k).mul(k.dot(dir) * (1 - cosA)));
            dir.set(rotated);
        }

        int minLife = (int) config.randomLifetime().x;
        int maxLife = (int) config.randomLifetime().y;
        int lifetime = minLife + (maxLife > minLife ? shootingStarRng.nextInt(maxLife - minLife) : 0);
        if (lifetime <= 0) lifetime = 20;

        activeShootingStars.add(new ActiveShootingStar(pos, dir, lifetime, config));
    }

    private void renderShootingStars(PoseStack poseStack, SkyRenderState state) {
        if (activeShootingStars.isEmpty()) return;
        for (ActiveShootingStar star : activeShootingStars) {
            renderOneShootingStar(star, poseStack, state);
        }
    }

    private void renderOneShootingStar(ActiveShootingStar star, PoseStack poseStack, SkyRenderState state) {
        float alpha = (star.remainingTicks / (float) star.maxTicks) * state.starBrightness;
        if (alpha <= 0) return;

        Vec3 c = star.config.color();
        int cr = (int)(c.x * 255);
        int cg = (int)(c.y * 255);
        int cb = (int)(c.z * 255);

        Vector3f pos = star.position;
        Vector3f dir = star.direction;
        Vector3f norm = new Vector3f(pos).normalize();
        Vector3f right = new Vector3f(dir).cross(norm).normalize();

        float halfLen = star.config.scale() * 2.0f;
        float halfWid = star.config.scale() * 0.25f;

        Vector3f v0 = new Vector3f(pos).add(new Vector3f(dir).mul(halfLen)).add(new Vector3f(right).mul(halfWid));
        Vector3f v1 = new Vector3f(pos).add(new Vector3f(dir).mul(halfLen)).sub(new Vector3f(right).mul(halfWid));
        Vector3f v2 = new Vector3f(pos).sub(new Vector3f(dir).mul(halfLen * 0.5f)).sub(new Vector3f(right).mul(halfWid));
        Vector3f v3 = new Vector3f(pos).sub(new Vector3f(dir).mul(halfLen * 0.5f)).add(new Vector3f(right).mul(halfWid));

        GpuBuffer shootingStarBuffer;
        int indexCount;
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(
                DefaultVertexFormat.POSITION_COLOR.getVertexSize() * 4)) {
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.addVertex(v1.x, v1.y, v1.z).setColor(cr, cg, cb, 255);
            bufferBuilder.addVertex(v0.x, v0.y, v0.z).setColor(cr, cg, cb, 255);
            bufferBuilder.addVertex(v3.x, v3.y, v3.z).setColor(cr, cg, cb, 255);
            bufferBuilder.addVertex(v2.x, v2.y, v2.z).setColor(cr, cg, cb, 255);
            try (MeshData meshData = bufferBuilder.buildOrThrow()) {
                indexCount = meshData.drawState().indexCount();
                shootingStarBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Shooting star", 40, meshData.vertexBuffer());
            }
        }

        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul(poseStack.last().pose());

        RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = quadIndices.getBuffer(indexCount);

        GpuTextureView colorView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView depthView = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();

        // Color is in vertex attributes; DynamicTransforms alpha carries the fade
        GpuBufferSlice dynamicSlice = RenderSystem.getDynamicUniforms()
                .writeTransform(matrix4fStack, new Vector4f(1f, 1f, 1f, alpha), new Vector3f(), new Matrix4f());

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(() -> "Shooting star", colorView, OptionalInt.empty(), depthView, OptionalDouble.empty())) {
            renderPass.setPipeline(RenderPipelineRegistry.COLORED_STARS);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicSlice);
            renderPass.setVertexBuffer(0, shootingStarBuffer);
            renderPass.setIndexBuffer(indexBuffer, quadIndices.type());
            renderPass.drawIndexed(0, 0, indexCount, 1);
        }

        matrix4fStack.popMatrix();
        shootingStarBuffer.close();
    }

    /** Close GPU resources held by this renderer (called when resource packs reload). */
    public void close() {
        if (this.gpuBuffer != null) {
            this.gpuBuffer.gpuBuffer().close();
        }
        if (this.skyboxBuffer != null) {
            this.skyboxBuffer.close();
        }
        if (this.customFogBuffer != null) {
            this.customFogBuffer.close();
        }
        activeShootingStars.clear();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Pack ARGB components (each 0-255) into a single int. */
    private static int packArgb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    // -------------------------------------------------------------------------
    // Inner types
    // -------------------------------------------------------------------------

    private static class ActiveShootingStar {
        final Vector3f position;
        final Vector3f direction;
        int remainingTicks;
        final int maxTicks;
        final StarSettings.ShootingStars config;

        ActiveShootingStar(Vector3f position, Vector3f direction, int lifetime, StarSettings.ShootingStars config) {
            this.position = position;
            this.direction = direction;
            this.remainingTicks = lifetime;
            this.maxTicks = lifetime;
            this.config = config;
        }
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    public static ServerLevel getServerLevel() {
        Minecraft minecraft = Minecraft.getInstance();
        IntegratedServer integratedServer = minecraft.getSingleplayerServer();
        return integratedServer != null ? integratedServer.getLevel(minecraft.level.dimension()) : null;
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    public static class Builder {

        public List<SkyObject> skyObjects = new ArrayList<>();
        public boolean weather = true;
        public CustomVanillaObject.Sun customSun = null;
        public CustomVanillaObject.Moon customMoon = null;
        public SkyColorSettings skyColorSettings = null;
        public LightSettings lightSettings = null;
        public StarSettings star = StarSettings.createDefaultStars();
        public SkyProperties.RenderCondition renderCondition = null;
        public SkyBoxSetting skyBoxSetting = null;
        public FogSettings fogSettings = null;

        public Builder() {}

        public Builder setWeather(boolean weather) {
            this.weather = weather;
            return this;
        }

        public Builder setStar(StarSettings star) {
            this.star = star;
            return this;
        }

        public Builder setCustomSun(CustomVanillaObject.Sun sun) {
            this.customSun = sun;
            return this;
        }

        public Builder setCustomMoon(CustomVanillaObject.Moon moon) {
            this.customMoon = moon;
            return this;
        }

        public Builder setSkyColorSettings(SkyColorSettings skyColorSettings) {
            this.skyColorSettings = skyColorSettings;
            return this;
        }

        public Builder setLightSettings(LightSettings lightSettings) {
            this.lightSettings = lightSettings;
            return this;
        }

        public Builder setRenderCondition(SkyProperties.RenderCondition renderCondition) {
            this.renderCondition = renderCondition;
            return this;
        }

        public Builder addSkyObject(SkyObject skyObject) {
            this.skyObjects.add(skyObject);
            return this;
        }

        public Builder setSkyBoxSetting(SkyBoxSetting skyBoxSetting) {
            this.skyBoxSetting = skyBoxSetting;
            return this;
        }

        public Builder setFogSettings(FogSettings fogSettings) {
            this.fogSettings = fogSettings;
            return this;
        }

        public DimensionRenderer build() {
            return new DimensionRenderer(
                    skyObjects,
                    weather,
                    customSun,
                    customMoon,
                    skyColorSettings,
                    lightSettings,
                    star,
                    renderCondition,
                    skyBoxSetting,
                    fogSettings
            );
        }
    }

}
