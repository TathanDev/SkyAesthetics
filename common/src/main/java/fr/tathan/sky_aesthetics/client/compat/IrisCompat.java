package fr.tathan.sky_aesthetics.client.compat;

import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.registry.RenderPipelineRegistry;
import fr.tathan.sky_aesthetics.helper.PlatformHelper;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;

public class IrisCompat {

    private static final boolean IRIS_LOADED = PlatformHelper.isModLoaded("iris");
    private static boolean irisUsable = true;

    public static void init() {
        if (!IRIS_LOADED) return;
        try {
            Impl.assignPipelines();
        } catch (Exception | LinkageError e) {
            disable(e);
        }
    }

    public static Object clearPhase() {
        if (!IRIS_LOADED || !irisUsable) return null;
        try {
            return Impl.clearPhase();
        } catch (Exception | LinkageError e) {
            disable(e);
            return null;
        }
    }

    public static void restorePhase(Object previous) {
        if (!IRIS_LOADED || !irisUsable || previous == null) return;
        try {
            Impl.restorePhase(previous);
        } catch (Exception | LinkageError e) {
            disable(e);
        }
    }

    private static void disable(Throwable e) {
        irisUsable = false;
        SkyAesthetics.LOG.error("Failed to apply Iris compatibility, disabling it", e);
    }

    // Kept in a nested class so the Iris classes are only loaded when Iris is present.
    private static class Impl {
        // To make the custom pipelines work with shaders we need to register them under something iris understands, it also does this for all vanilla pipelines btw (yikes)
        static void assignPipelines() {
            IrisApi api = IrisApi.getInstance();
            api.assignPipeline(RenderPipelineRegistry.CELESTIAL_BLEND, IrisProgram.SKY_TEXTURED);
            api.assignPipeline(RenderPipelineRegistry.CELESTIAL_NO_BLEND, IrisProgram.SKY_TEXTURED);
            api.assignPipeline(RenderPipelineRegistry.COLORED_STARS, IrisProgram.SKY_BASIC);
        }

        // These 2 blocks are to make the custom rendered sky stuff not be assigned to the renderStage MC_RENDER_STAGE_SUN or MOON.
        // Quite a few shaders use those stages to do custom effects to the sun/moon, and it would break the visuals a lot. Assign to NONE instead.
        static Object clearPhase() {
            WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
            if (pipeline == null) return null;
            WorldRenderingPhase previous = pipeline.getPhase();
            pipeline.setPhase(WorldRenderingPhase.NONE);
            return previous;
        }

        static void restorePhase(Object previous) {
            WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
            if (pipeline != null) pipeline.setPhase((WorldRenderingPhase) previous);
        }
    }
}
