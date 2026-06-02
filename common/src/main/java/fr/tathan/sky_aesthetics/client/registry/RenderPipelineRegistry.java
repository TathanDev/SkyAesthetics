package fr.tathan.sky_aesthetics.client.registry;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;

public class RenderPipelineRegistry {

    public static final RenderPipeline CELESTIAL_NO_BLEND;
    public static final RenderPipeline COLORED_STARS;

    public static void init() {
    }

    static {
        CELESTIAL_NO_BLEND = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation("pipeline/celestial").withVertexShader("core/position_tex").withFragmentShader("core/position_tex").withSampler("Sampler0").withDepthWrite(false).withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS).build());
        COLORED_STARS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation("pipeline/stars").withVertexShader("core/position_color").withFragmentShader("core/position_color").withBlend(BlendFunction.OVERLAY).withDepthWrite(false).withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS).build());

    }
}
