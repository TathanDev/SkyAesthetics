package fr.tathan.sky_aesthetics.client.registry;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;

public class RenderPipelineRegistry {

    public static final RenderPipeline CELESTIAL_NO_BLEND;
    public static final RenderPipeline CELESTIAL_BLEND;
    public static final RenderPipeline COLORED_STARS;

    public static void init() {
    }

    static {
        CELESTIAL_NO_BLEND = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation("pipeline/skyaesthetics_celestial")
                .withVertexShader("core/position_tex")
                .withFragmentShader("core/position_tex")
                .withSampler("Sampler0")
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
                .build());

        CELESTIAL_BLEND = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation("pipeline/skyaesthetics_celestial_blend")
                .withVertexShader("core/position_tex")
                .withFragmentShader("core/position_tex")
                .withSampler("Sampler0")
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
                .build());

        COLORED_STARS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation("pipeline/skyaesthetics_colored_stars")
                .withVertexShader("core/position_color")
                .withFragmentShader("core/position_color")
                .withColorTargetState(new ColorTargetState(BlendFunction.OVERLAY))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                .build());
    }
}
