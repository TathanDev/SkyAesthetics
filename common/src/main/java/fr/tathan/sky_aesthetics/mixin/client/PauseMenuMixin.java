package fr.tathan.sky_aesthetics.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import fr.tathan.sky_aesthetics.client.screens.SkyModificationScreen;
import fr.tathan.sky_aesthetics.helper.PlatformHelper;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
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

    @Inject(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout;arrangeElements()V"))
    private void createSkyButton(CallbackInfo ci, @Local GridLayout.RowHelper rowHelper, @Local GridLayout gridLayout) {

        if(PlatformHelper.isModLoaded("owo")) {
            rowHelper.addChild(new SpacerElement(200, 30), 2);

            rowHelper.addChild(Button.builder(Component.translatable("sky_aesthetics.pause_menu.sky_modification_screen"), button -> {
                this.minecraft.setScreen(new SkyModificationScreen());
            }).bounds(0, 30, 200, 20).build(), 2);

            gridLayout.arrangeElements();
        }
    }

}
