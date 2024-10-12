package fr.tathan.sky_aesthetics.mixin.client;

import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mrbysco.lunar.client.MoonHandler;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MoonHandler.class)
public interface LunarMoonInfosAccessor {

    @Accessor("moonColor")
    static float[] sky_aesthetic$moonColor() {
        throw new AssertionError();
    }

    @Accessor("moonID")
    static String sky_aesthetic$eventId() {
        throw new AssertionError();
    }
}