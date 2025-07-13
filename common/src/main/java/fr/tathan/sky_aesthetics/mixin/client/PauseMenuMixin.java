package fr.tathan.sky_aesthetics.mixin.client;

import fr.tathan.sky_aesthetics.client.screens.SkyModificationTest;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseMenuMixin extends Screen  {
    protected PauseMenuMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addButton(CallbackInfo ci) {
        this.addRenderableWidget(Button.builder(Component.literal("Sky Modification Test"), button -> {
            this.minecraft.setScreen(new SkyModificationTest());
        }).bounds(10, 10, 200, 20).build());
    }
}
