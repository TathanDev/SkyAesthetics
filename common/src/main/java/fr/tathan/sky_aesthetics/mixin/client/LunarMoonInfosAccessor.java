package fr.tathan.sky_aesthetics.mixin.client;

import com.mrbysco.lunar.client.MoonHandler;
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