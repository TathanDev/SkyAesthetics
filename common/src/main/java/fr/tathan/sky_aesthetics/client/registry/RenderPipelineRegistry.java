package fr.tathan.sky_aesthetics.client.registry;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;

public class RenderPipelineRegistry {

    public static final RenderPipeline CELESTIAL_NO_BLEND;
    public static final RenderPipeline CELESTIAL_BLEND;
    public static final RenderPipeline COLORED_STARS;

    public static void init() {
    }

    static {
        CELESTIAL_NO_BLEND = RenderPipelines.register(RenderPipeline.builder()
                .withLocation("pipeline/skyaesthetics_celestial")
                .withVertexShader("core/position_tex")
                .withFragmentShader("core/position_tex")
                .withBindGroupLayout(BindGroupLayout.builder().withSampler("Sampler0").build())
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
                .build());

        CELESTIAL_BLEND = RenderPipelines.register(RenderPipeline.builder()
                .withLocation("pipeline/skyaesthetics_celestial_blend")
                .withVertexShader("core/position_tex")
                .withFragmentShader("core/position_tex")
                .withBindGroupLayout(BindGroupLayout.builder().withSampler("Sampler0").build())
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
                .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                .build());

        COLORED_STARS = RenderPipelines.register(RenderPipeline.builder()
                .withLocation("pipeline/skyaesthetics_colored_stars")
                .withVertexShader("core/position_color")
                .withFragmentShader("core/position_color")
                .withColorTargetState(new ColorTargetState(BlendFunction.OVERLAY))
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                .build());
    }
}
