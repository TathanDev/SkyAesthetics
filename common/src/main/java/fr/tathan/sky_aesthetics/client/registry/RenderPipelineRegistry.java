package fr.tathan.sky_aesthetics.client.registry;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.*;
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
                .withBindGroupLayout(BindGroupLayouts.GLOBALS)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withLocation("pipeline/skyaesthetics_celestial")
                .withVertexShader("core/position_tex")
                .withFragmentShader("core/position_tex")
                .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                .withColorTargetState(ColorTargetState.DEFAULT)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
                .build());

        CELESTIAL_BLEND = RenderPipelines.register(RenderPipeline.builder()
                .withBindGroupLayout(BindGroupLayouts.GLOBALS)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withLocation("pipeline/skyaesthetics_celestial_blend")
                .withVertexShader("core/position_tex")
                .withFragmentShader("core/position_tex")
                .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
                .build());

        COLORED_STARS = RenderPipelines.register(RenderPipeline.builder()
                .withBindGroupLayout(BindGroupLayouts.GLOBALS)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withLocation("pipeline/skyaesthetics_colored_stars")
                .withVertexShader("core/position_color")
                .withFragmentShader("core/position_color")
                .withColorTargetState(new ColorTargetState(BlendFunction.OVERLAY))
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                .build());
    }
}
