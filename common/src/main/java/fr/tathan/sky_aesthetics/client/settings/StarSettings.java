package fr.tathan.sky_aesthetics.client.settings;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.tathan.sky_aesthetics.client.registry.RenderPipelineRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.util.*;

/**
 * The class containing information about stars
 * @param vanilla If the stars should be like vanilla one's
 * @param movingStars if the stars should move
 * @param count The count of stars
 * @param allDaysVisible If stars should be visible all day
 * @param scale The size of a star
 * @param color The color of the stars
 * @param shootingStars Shooting Star Settings
 */
public record StarSettings(
        boolean vanilla,
        boolean movingStars,
        int count,
        boolean allDaysVisible,
        float scale,
        Vector3i color,
        Optional<ShootingStars> shootingStars
) {

    public static final Codec<StarSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("vanilla").forGetter(StarSettings::vanilla),
            Codec.BOOL.fieldOf("moving_stars").forGetter(StarSettings::movingStars),
            Codec.INT.fieldOf("count").forGetter(StarSettings::count),
            Codec.BOOL.fieldOf("all_days_visible").forGetter(StarSettings::allDaysVisible),
            Codec.FLOAT.fieldOf("scale").forGetter(StarSettings::scale),
            SkyColorSettings.VEC3I.fieldOf("color").forGetter(StarSettings::color),
            ShootingStars.CODEC.optionalFieldOf("shooting_stars").forGetter(StarSettings::shootingStars)
    ).apply(instance, StarSettings::new));



    public static StarSettings createDefaultStars() {
        return new StarSettings(true, false, 15000, false, 0.15f, new Vector3i(255, 255, 255), Optional.empty());
    }


    public BufferHolder buildCustomStars() {

        if(this.vanilla || this.count() <= 0) {
            return null;
        }

        RandomSource randomSource = RandomSource.create(10842L);
        int indexCount = 0;

        GpuBuffer var19;
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION_COLOR.getVertexSize() * this.count() * 4)) {
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);

            for(int i = 0; i < this.count(); ++i) {
                float g = randomSource.nextFloat() * 2.0F - 1.0F;
                float h = randomSource.nextFloat() * 2.0F - 1.0F;
                float j = randomSource.nextFloat() * 2.0F - 1.0F;
                float k = this.scale() + randomSource.nextFloat() * 0.1F;
                float l = Mth.lengthSquared(g, h, j);
                if (!(l <= 0.010000001F) && !(l >= 1.0F)) {

                    int color1 = this.color().x == -1 ? (i & 0xFF) : this.color().x;
                    int color2 = this.color().y == -1 ? (i & 0xFF) : this.color().y;
                    int color3 = this.color().z == -1 ? (i & 0xFF) : this.color().z;

                    Vector3f vector3f = (new Vector3f(g, h, j)).normalize(100.0F);
                    float m = (float)(randomSource.nextDouble() * (double)(float)Math.PI * (double)2.0F);
                    Matrix3f matrix3f = (new Matrix3f()).rotateTowards((new Vector3f(vector3f)).negate(), new Vector3f(0.0F, 1.0F, 0.0F)).rotateZ(-m);
                    bufferBuilder.addVertex((new Vector3f(k, -k, 0.0F)).mul(matrix3f).add(vector3f)).setColor(color1, color2, color3, 255);
                    bufferBuilder.addVertex((new Vector3f(k, k, 0.0F)).mul(matrix3f).add(vector3f)).setColor(color1, color2, color3, 255);
                    bufferBuilder.addVertex((new Vector3f(-k, k, 0.0F)).mul(matrix3f).add(vector3f)).setColor(color1, color2, color3, 255);
                    bufferBuilder.addVertex((new Vector3f(-k, -k, 0.0F)).mul(matrix3f).add(vector3f)).setColor(color1, color2, color3, 255);
                }
            }

            try (MeshData meshData = bufferBuilder.buildOrThrow()) {
                indexCount = meshData.drawState().indexCount();
                var19 = RenderSystem.getDevice().createBuffer(() -> "Stars vertex buffer", 40, meshData.vertexBuffer());
            }
        }

        return new BufferHolder(var19, indexCount);
    }



    public void renderStars(PoseStack poseStack, SkyRenderState skyRenderState, SkyRenderer skyRenderer, @Nullable BufferHolder customStarBuffer) {

        boolean isNightTime = skyRenderState.starBrightness > 0.0F;

        if (this.vanilla && isNightTime) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotation(skyRenderState.starAngle));
            skyRenderer.renderStars(skyRenderState.starBrightness, poseStack);
            poseStack.popPose();
            return;
        }

        float starsAngle = this.movingStars() ? skyRenderState.starAngle : 0.0f;

        if(customStarBuffer != null) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotation(starsAngle));

            if(this.allDaysVisible()) {
                customStarBuffer.render(poseStack, RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS), 1f);
            } else if (isNightTime) {
                customStarBuffer.render(poseStack, RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS), skyRenderState.starBrightness);
            }
            poseStack.popPose();

        }

    }

    public record BufferHolder(GpuBuffer gpuBuffer, int indexCount) {
        public void render(PoseStack poseStack, RenderSystem.AutoStorageIndexBuffer quadIndices, float starBrightness) {
            Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
            matrix4fStack.pushMatrix();
            matrix4fStack.mul(poseStack.last().pose());
            RenderTarget mainRenderTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
            GpuTextureView gpuTextureView = mainRenderTarget.getColorTextureView();
            GpuTextureView gpuTextureView2 = mainRenderTarget.getDepthTextureView();
            GpuBuffer gpuBuffer = quadIndices.getBuffer(this.indexCount);

            // Color is baked into vertex attributes; DynamicTransforms alpha controls brightness
            Vector4f veColor = new Vector4f(1f, 1f, 1f, starBrightness);

            GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(matrix4fStack, veColor, new Vector3f(), new Matrix4f());

            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Stars", gpuTextureView, Optional.empty(), gpuTextureView2, OptionalDouble.empty())) {
                renderPass.setPipeline(RenderPipelineRegistry.COLORED_STARS);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
                renderPass.setVertexBuffer(0, this.gpuBuffer.slice());
                renderPass.setIndexBuffer(gpuBuffer, quadIndices.type());
                renderPass.drawIndexed(indexCount, 1, 0, 0, 0);
            }

            matrix4fStack.popMatrix();
        }
    }

    /**
     *
     * @param percentage The chance for a shooting star to happen
     * @param randomLifetime The random lifetime of star
     * @param scale The scale of the star
     * @param speed The speed of the star
     * @param color The color of the star
     * @param rotation The rotation of the star
     */
    public record ShootingStars(int percentage, Vec2 randomLifetime, float scale, float speed, Vec3 color, Optional<Integer> rotation) {

        public static Codec<Vec2> VEC2 = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 2).map((listx) -> new Vec2(listx.getFirst(), listx.get(1))), (vec2) -> List.of(vec2.x, vec2.y));

        public static final Codec<ShootingStars> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("percentage").forGetter(ShootingStars::percentage),
                VEC2.fieldOf("random_lifetime").forGetter(ShootingStars::randomLifetime),
                Codec.FLOAT.fieldOf("scale").forGetter(ShootingStars::scale),
                Codec.FLOAT.fieldOf("speed").forGetter(ShootingStars::speed),
                Vec3.CODEC.fieldOf("color").forGetter(ShootingStars::color),
                Codec.INT.optionalFieldOf("rotation").forGetter(ShootingStars::rotation)
        ).apply(instance, ShootingStars::new));
    }

}
