package fr.tathan.sky_aesthetics.client.registry;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;

public class RenderPipelineRegistry {

    public static final RenderPipeline CELESTIAL_NO_BLEND;

    public static void init() {
    }

    static {
        CELESTIAL_NO_BLEND = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation("pipeline/celestial").withVertexShader("core/position_tex").withFragmentShader("core/position_tex").withSampler("Sampler0").withDepthStencilState(DepthStencilState.DEFAULT).withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS).build());
    }
}
