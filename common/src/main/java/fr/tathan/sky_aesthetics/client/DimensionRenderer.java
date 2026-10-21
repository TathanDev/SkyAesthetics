package fr.tathan.sky_aesthetics.client;


import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import fr.tathan.sky_aesthetics.client.compat.IrisCompat;
import fr.tathan.sky_aesthetics.client.registry.RenderPipelineRegistry;
import fr.tathan.sky_aesthetics.client.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.MoonPhase;
import java.lang.Math;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import org.jetbrains.annotations.Nullable;

/**
 * The class handling the rendering of a custom sky
 */
public class DimensionRenderer {

    private static final float VANILLA_SUN_SIZE = 30.0f;
    private static final float VANILLA_MOON_SIZE = 20.0f;
    // Same as SkyRenderer.SKY_VERTICES
    private static final int SKY_DISC_VERTICES = 10;

    public final List<SkyObject> skyObjects;
    public final TextureAtlas celestialsAtlas;
    public final StarSettings starSettings;
    public final boolean weather;
    public final CustomVanillaObject.Sun customSun;
    public final CustomVanillaObject.Moon customMoon;
    public final SkyColorSettings skyColorSettings;
    public final LightSettings lightSettings;
    public final SkyProperties.RenderCondition renderCondition;
    public final StarSettings.BufferHolder gpuBuffer;
    public final FogSettings fogSettings;
    public final SkyboxRenderer skybox;

    // Cached custom sun/moon quads (static geometry) so we don't re-upload a GpuBuffer every frame.
    // The moon quad is rebuilt only when the lunar phase changes (its UVs are phase-dependent).
    private GpuBuffer customSunBuffer;
    private GpuBuffer customMoonBuffer;
    private int customMoonBufferPhase = -1;

    private GpuBuffer[] skyObjectBuffers;

    private final List<ActiveShootingStar> activeShootingStars = new ArrayList<>();
    private final RandomSource shootingStarRng = RandomSource.create();
    private long lastGameTime = -1;

    private GpuBuffer shootingStarsBuffer;
    private int shootingStarsIndexCount;
    private boolean shootingStarsDirty;

    private DimensionRenderer(List<SkyObject> skyObjects,
                              boolean weather,
                              CustomVanillaObject.Sun customSun,
                              CustomVanillaObject.Moon customMoon,
                              SkyColorSettings skyColorSettings,
                              LightSettings lightSettings,
                              StarSettings starSettings,
                              SkyProperties.RenderCondition renderCondition,
                              List<SkyBoxLayer> skyBoxLayers,
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
        this.fogSettings = fogSettings;
        this.skybox = new SkyboxRenderer(skyBoxLayers, this.celestialsAtlas);
    }

    public boolean canRenderSky() {
        if(this.renderCondition == null) {
            return true;
        }
        return this.renderCondition.isSkyRendered();
    }

    public void render(SkyRenderState skyRenderState, SkyRenderer skyRenderer) {
        render(skyRenderState, skyRenderer, null, 0.0f);
    }

    /**
     * @param outgoing The previous sky when the sky changes with transition_ticks, or null
     * @param outgoingAlpha The opacity of the previous sky skybox layers (1 to 0)
     */
    public void render(SkyRenderState skyRenderState, SkyRenderer skyRenderer, @Nullable DimensionRenderer outgoing, float outgoingAlpha) {
       PoseStack poseStack = new PoseStack();

       tickShootingStars(skyRenderState.starBrightness);

       SkyClock clock = SkyClock.capture(skyRenderState);
       skybox.prepare(clock);
       if (outgoing != null) outgoing.skybox.prepare(clock);

       drawSkyboxLayers(SkyBoxLayer.Slot.BACKGROUND, outgoing, outgoingAlpha);

       // Sky disc — use custom color if configured
       int skyDiscColor = skyRenderState.skyColor;
       if (skyColorSettings != null && skyColorSettings.color().isPresent()) {
           Vector4f c = skyColorSettings.color().get();
           skyDiscColor = packArgb((int)(c.w * 255), (int)(c.x * 255), (int)(c.y * 255), (int)(c.z * 255));
       }
       renderSkyDisc(skyRenderer, skyDiscColor, 1.0f - skybox.hideFactor(SkyBoxLayer.HidePart.SKY_DISC));

       drawSkyboxLayers(SkyBoxLayer.Slot.SKY, outgoing, outgoingAlpha);

       // Sunrise/sunset — use custom sunset color if configured
       float sunriseKeep = 1.0f - skybox.hideFactor(SkyBoxLayer.HidePart.SUNRISE);
       if (sunriseKeep > 0.0f) {
           int sunriseColor = skyRenderState.sunriseAndSunsetColor;
           if (skyColorSettings != null && skyColorSettings.sunsetColor().isPresent()) {
               Vector3i s = skyColorSettings.sunsetColor().get();
               int alpha = skyColorSettings.sunriseAlphaModifier().orElse(255);
               sunriseColor = packArgb(alpha, s.x, s.y, s.z);
           }
           if (sunriseKeep < 1.0f) {
               int alpha = (int) (((sunriseColor >>> 24) & 0xFF) * sunriseKeep);
               sunriseColor = (sunriseColor & 0x00FFFFFF) | (alpha << 24);
           }
           skyRenderer.renderSunriseAndSunset(poseStack, skyRenderState.sunAngle, sunriseColor);
       }

       float starsKeep = 1.0f - skybox.hideFactor(SkyBoxLayer.HidePart.STARS);
       if (starsKeep > 0.0f) {
           this.starSettings.renderStars(poseStack, skyRenderState, skyRenderer, this.gpuBuffer, starsKeep);
           ConstellationRenderer.renderAll(poseStack, skyRenderState, skyRenderState.starBrightness * starsKeep);
           renderShootingStars(poseStack, skyRenderState.starBrightness * starsKeep);
       }

       drawSkyboxLayers(SkyBoxLayer.Slot.OVER_STARS, outgoing, outgoingAlpha);

       this.renderSkyObjects(poseStack, skyRenderState.sunAngle, skyRenderState.moonAngle, skyRenderState.moonPhase, skyRenderState.rainBrightness, skyRenderer,
               1.0f - skybox.hideFactor(SkyBoxLayer.HidePart.SUN),
               1.0f - skybox.hideFactor(SkyBoxLayer.HidePart.MOON),
               1.0f - skybox.hideFactor(SkyBoxLayer.HidePart.SKY_OBJECTS));

       drawSkyboxLayers(SkyBoxLayer.Slot.FOREGROUND, outgoing, outgoingAlpha);

       if (skyRenderState.shouldRenderDarkDisc) {
           skyRenderer.renderDarkDisc();
       }
   }

    private void drawSkyboxLayers(SkyBoxLayer.Slot slot, @Nullable DimensionRenderer outgoing, float outgoingAlpha) {
        skybox.draw(slot, 1.0f);
        if (outgoing != null) outgoing.skybox.draw(slot, outgoingAlpha);
    }

    private static void renderSkyDisc(SkyRenderer skyRenderer, int color, float keep) {
        if (keep >= 0.999f) {
            skyRenderer.renderSkyDisc(color);
            return;
        }
        if (keep <= 0.004f) return;

        Vector4f modulator = ARGB.vector4fFromARGB32(color);
        modulator.w = keep;
        GpuBufferSlice transform = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), modulator, new Vector3f(), new Matrix4f());
        GpuTextureView colorView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView depthView = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(() -> "Faded sky disc", colorView, OptionalInt.empty(), depthView, OptionalDouble.empty())) {
            renderPass.setPipeline(RenderPipelineRegistry.SKY_DISC_BLEND);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", transform);
            renderPass.setVertexBuffer(0, skyRenderer.topSkyBuffer);
            renderPass.draw(0, SKY_DISC_VERTICES);
        }
    }

    public void renderSkyObjects(PoseStack poseStack, float sunAngle, float moonAngle, MoonPhase moonPhase, float rainBrightness, SkyRenderer skyRenderer) {
        renderSkyObjects(poseStack, sunAngle, moonAngle, moonPhase, rainBrightness, skyRenderer, 1.0f, 1.0f, 1.0f);
    }

    public void renderSkyObjects(PoseStack poseStack, float sunAngle, float moonAngle, MoonPhase moonPhase, float rainBrightness, SkyRenderer skyRenderer,
                                 float sunKeep, float moonKeep, float objectsKeep) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));

        if ((this.customSun == null || this.customSun.show()) && sunKeep > 0.0f) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotation(sunAngle));
            float sunBrightness = rainBrightness * sunKeep * (this.customSun != null ? this.customSun.intensity() : 1.0f);
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

        if ((this.customMoon == null || this.customMoon.show()) && moonKeep > 0.0f) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotation(moonAngle));
            float moonBrightness = rainBrightness * moonKeep * (this.customMoon != null ? this.customMoon.intensity() : 1.0f);
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

        if (objectsKeep > 0.0f) {
            Object irisPhase = IrisCompat.clearPhase();
            GpuBuffer[] objectBuffers = getSkyObjectBuffers();
            for (int i = 0; i < skyObjects.size(); i++) {
                skyObjects.get(i).renderObject(objectBuffers[i], objectsKeep, poseStack, this.celestialsAtlas, sunAngle, moonAngle);
            }
            IrisCompat.restorePhase(irisPhase);
        }

        poseStack.popPose();
    }

    private GpuBuffer[] getSkyObjectBuffers() {
        if (skyObjectBuffers == null || skyObjectBuffers.length != skyObjects.size()) {
            closeSkyObjectBuffers();
            skyObjectBuffers = new GpuBuffer[skyObjects.size()];
            for (int i = 0; i < skyObjectBuffers.length; i++) {
                skyObjectBuffers[i] = skyObjects.get(i).buildSkyObject(this.celestialsAtlas);
            }
        }
        return skyObjectBuffers;
    }

    private void closeSkyObjectBuffers() {
        if (skyObjectBuffers == null) return;
        for (GpuBuffer buffer : skyObjectBuffers) {
            buffer.close();
        }
        skyObjectBuffers = null;
    }

    // -------------------------------------------------------------------------
    // Custom sun
    // -------------------------------------------------------------------------

    private void renderCustomSun(float rainBrightness, PoseStack poseStack) {
        if (customSunBuffer == null) {
            Identifier texture = this.customSun.sunTexture().get();
            customSunBuffer = SkyRenderer.buildCelestialQuad(texture.getPath(), this.celestialsAtlas.getSprite(texture));
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
            renderPass.setPipeline(RenderPipelines.CELESTIAL);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicSlice);
            renderPass.bindTexture("Sampler0", this.celestialsAtlas.getTextureView(), this.celestialsAtlas.getSampler());
            renderPass.setVertexBuffer(0, customSunBuffer);
            renderPass.setIndexBuffer(indexBuffer, quadIndices.type());
            renderPass.drawIndexed(0, 0, 6, 1);
        }

        matrix4fStack.popMatrix();
    }

    // -------------------------------------------------------------------------
    // Custom moon
    // -------------------------------------------------------------------------

    private void renderCustomMoon(MoonPhase moonPhase, float rainBrightness, PoseStack poseStack) {
        GpuBuffer moonBuffer = getCustomMoonBuffer(moonPhase);

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
            renderPass.setPipeline(RenderPipelines.CELESTIAL);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicSlice);
            renderPass.bindTexture("Sampler0", this.celestialsAtlas.getTextureView(), this.celestialsAtlas.getSampler());
            renderPass.setVertexBuffer(0, moonBuffer);
            renderPass.setIndexBuffer(indexBuffer, quadIndices.type());
            renderPass.drawIndexed(0, 0, 6, 1);
        }

        matrix4fStack.popMatrix();
    }

    /**
     * Returns the cached custom-moon quad, (re)building it only when needed. With phases off the full
     * texture is used and the quad is built once; with phases on, the quad carries the current phase's
     * atlas cell UVs and is rebuilt only when the phase changes.
     */
    private GpuBuffer getCustomMoonBuffer(MoonPhase moonPhase) {
        if (!this.customMoon.showPhases()) {
            if (customMoonBuffer == null) {
                Identifier texture = this.customMoon.moonTexture().get();
                customMoonBuffer = SkyRenderer.buildCelestialQuad(texture.getPath(), this.celestialsAtlas.getSprite(texture));
                customMoonBufferPhase = -2;
            }
            return customMoonBuffer;
        }
        int ordinal = moonPhase.ordinal();
        if (customMoonBuffer == null || customMoonBufferPhase != ordinal) {
            if (customMoonBuffer != null) customMoonBuffer.close();
            customMoonBuffer = buildPhasedMoonQuad(moonPhase);
            customMoonBufferPhase = ordinal;
        }
        return customMoonBuffer;
    }

    private GpuBuffer buildPhasedMoonQuad(MoonPhase moonPhase) {
        TextureAtlasSprite sprite = this.celestialsAtlas.getSprite(this.customMoon.moonTexture().get());
        int col = moonPhase.ordinal() % 4;
        int row = moonPhase.ordinal() / 4;
        float u0 = sprite.getU(col / 4.0f);
        float u1 = sprite.getU((col + 1) / 4.0f);
        float v0 = sprite.getV(row / 2.0f);
        float v1 = sprite.getV((row + 1) / 2.0f);
        try (ByteBufferBuilder bbb = ByteBufferBuilder.exactlySized(
                DefaultVertexFormat.POSITION_TEX.getVertexSize() * 4)) {
            BufferBuilder bb = new BufferBuilder(bbb, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bb.addVertex(-1.0F, 0.0F, -1.0F).setUv(u0, v0);
            bb.addVertex( 1.0F, 0.0F, -1.0F).setUv(u1, v0);
            bb.addVertex( 1.0F, 0.0F,  1.0F).setUv(u1, v1);
            bb.addVertex(-1.0F, 0.0F,  1.0F).setUv(u0, v1);
            try (MeshData mesh = bb.buildOrThrow()) {
                return RenderSystem.getDevice().createBuffer(() -> "Custom moon", 40, mesh.vertexBuffer());
            }
        }
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

        boolean hadStars = !activeShootingStars.isEmpty();
        activeShootingStars.forEach(star -> {
            star.remainingTicks--;
            star.position.add(new Vector3f(star.direction).mul(config.speed() * 0.5f)).normalize(100.0f);
        });
        activeShootingStars.removeIf(star -> star.remainingTicks <= 0);

        if (starBrightness > 0 && shootingStarRng.nextInt(100) < config.percentage()) {
            spawnShootingStar(config);
        }

        if (hadStars || !activeShootingStars.isEmpty()) {
            shootingStarsDirty = true;
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

    private void renderShootingStars(PoseStack poseStack, float starBrightness) {
        if (shootingStarsDirty) {
            rebuildShootingStarsBuffer();
            shootingStarsDirty = false;
        }
        if (shootingStarsBuffer == null || starBrightness <= 0) return;

        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul(poseStack.last().pose());

        RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = quadIndices.getBuffer(shootingStarsIndexCount);

        GpuTextureView colorView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView depthView = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();

        GpuBufferSlice dynamicSlice = RenderSystem.getDynamicUniforms().writeTransform(matrix4fStack, new Vector4f(1f, 1f, 1f, starBrightness), new Vector3f(), new Matrix4f());

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(() -> "Shooting stars", colorView, OptionalInt.empty(), depthView, OptionalDouble.empty())) {
            renderPass.setPipeline(RenderPipelineRegistry.COLORED_STARS);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicSlice);
            renderPass.setVertexBuffer(0, shootingStarsBuffer);
            renderPass.setIndexBuffer(indexBuffer, quadIndices.type());
            renderPass.drawIndexed(0, 0, shootingStarsIndexCount, 1);
        }

        matrix4fStack.popMatrix();
    }

    /** Rebuilds the shared quad buffer from the current shooting stars (null when there are none). */
    private void rebuildShootingStarsBuffer() {
        if (shootingStarsBuffer != null) {
            shootingStarsBuffer.close();
            shootingStarsBuffer = null;
        }
        shootingStarsIndexCount = 0;
        if (activeShootingStars.isEmpty()) return;

        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(
                DefaultVertexFormat.POSITION_COLOR.getVertexSize() * 4 * activeShootingStars.size())) {
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            for (ActiveShootingStar star : activeShootingStars) {
                Vec3 c = star.config.color();
                int cr = (int)(c.x * 255);
                int cg = (int)(c.y * 255);
                int cb = (int)(c.z * 255);
                int ca = (int)(255 * star.remainingTicks / (float) star.maxTicks);

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

                bufferBuilder.addVertex(v1.x, v1.y, v1.z).setColor(cr, cg, cb, ca);
                bufferBuilder.addVertex(v0.x, v0.y, v0.z).setColor(cr, cg, cb, ca);
                bufferBuilder.addVertex(v3.x, v3.y, v3.z).setColor(cr, cg, cb, ca);
                bufferBuilder.addVertex(v2.x, v2.y, v2.z).setColor(cr, cg, cb, ca);
            }

            try (MeshData meshData = bufferBuilder.buildOrThrow()) {
                shootingStarsIndexCount = meshData.drawState().indexCount();
                shootingStarsBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Shooting stars", 40, meshData.vertexBuffer());
            }
        }
    }

    /** Close GPU resources held by this renderer (called when resource packs reload). */
    public void close() {
        if (this.gpuBuffer != null) {
            this.gpuBuffer.gpuBuffer().close();
        }
        this.skybox.close();
        if (this.customSunBuffer != null) {
            this.customSunBuffer.close();
        }
        if (this.customMoonBuffer != null) {
            this.customMoonBuffer.close();
        }
        closeSkyObjectBuffers();
        if (this.shootingStarsBuffer != null) {
            this.shootingStarsBuffer.close();
            this.shootingStarsBuffer = null;
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
        public List<SkyBoxLayer> skyBoxLayers = new ArrayList<>();
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
            this.skyBoxLayers.add(SkyBoxLayer.fromLegacy(skyBoxSetting));
            return this;
        }

        public Builder addSkyBoxLayer(SkyBoxLayer layer) {
            this.skyBoxLayers.add(layer);
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
                    List.copyOf(skyBoxLayers),
                    fogSettings
            );
        }
    }

}
