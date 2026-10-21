package fr.tathan.sky_aesthetics.client.registry;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.tathan.SkyAesthetics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.List;

public class RenderPipelineRegistry {

    public static final RenderPipeline CELESTIAL_NO_BLEND;
    public static final RenderPipeline CELESTIAL_BLEND;
    public static final RenderPipeline COLORED_STARS;

    public static final RenderPipeline SKYBOX_OPAQUE;
    public static final RenderPipeline SKYBOX_ALPHA;
    public static final RenderPipeline SKYBOX_ADD;
    public static final RenderPipeline SKYBOX_MULTIPLY;
    public static final RenderPipeline SKYBOX_SCREEN;
    public static final RenderPipeline SKY_DISC_BLEND;

    public static void init() {
    }

    public static List<RenderPipeline> skyboxPipelines() {
        return List.of(SKYBOX_OPAQUE, SKYBOX_ALPHA, SKYBOX_ADD, SKYBOX_MULTIPLY, SKYBOX_SCREEN);
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

        // Multiply and screen are mixed with the alpha in the shader
        SKYBOX_OPAQUE = skybox("opaque", null);
        SKYBOX_ALPHA = skybox("alpha", BlendFunction.TRANSLUCENT);
        SKYBOX_ADD = skybox("add", new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE, SourceFactor.ZERO, DestFactor.ONE));
        SKYBOX_MULTIPLY = skybox("multiply", new BlendFunction(SourceFactor.DST_COLOR, DestFactor.ZERO, SourceFactor.ZERO, DestFactor.ONE));
        SKYBOX_SCREEN = skybox("screen", new BlendFunction(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_COLOR, SourceFactor.ZERO, DestFactor.ONE));

        SKY_DISC_BLEND = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withUniform("Fog", UniformType.UNIFORM_BUFFER)
                .withLocation("pipeline/skyaesthetics_sky_disc_blend")
                .withVertexShader("core/sky")
                .withFragmentShader("core/sky")
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.TRIANGLE_FAN)
                .build());
    }

    private static RenderPipeline skybox(String name, BlendFunction blend) {
        Identifier shader = Identifier.fromNamespaceAndPath(SkyAesthetics.MODID, "core/skybox");
        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withUniform("Fog", UniformType.UNIFORM_BUFFER)
                .withLocation(Identifier.fromNamespaceAndPath(SkyAesthetics.MODID, "pipeline/skybox_" + name))
                .withVertexShader(shader)
                .withFragmentShader(shader)
                .withSampler("Sampler0")
                .withCull(false)
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS);
        if (blend != null) {
            builder.withColorTargetState(new ColorTargetState(blend));
        }
        return RenderPipelines.register(builder.build());
    }
}
