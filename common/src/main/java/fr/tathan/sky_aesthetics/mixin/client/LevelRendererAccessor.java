package fr.tathan.sky_aesthetics.mixin.client;

import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
    @Accessor("skyBuffer")
    VertexBuffer stellaris$getSkyBuffer();

    @Accessor("END_SKY_LOCATION")
    ResourceLocation stellaris$getEndSkyLocation();
}