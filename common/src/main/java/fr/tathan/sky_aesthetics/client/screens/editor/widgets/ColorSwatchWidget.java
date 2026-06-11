package fr.tathan.sky_aesthetics.client.screens.editor.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.IntSupplier;

/**
 * A non-interactive widget that fills its bounds with a color (read live from a supplier) and
 * draws a dark border. Used as the preview swatch inside the color picker.
 */
public class ColorSwatchWidget extends AbstractWidget {

    private final IntSupplier argb;

    public ColorSwatchWidget(int x, int y, int width, int height, IntSupplier argb) {
        super(x, y, width, height, Component.empty());
        this.argb = argb;
        this.active = false;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int right = x + getWidth();
        int bottom = y + getHeight();
        graphics.fill(x, y, right, bottom, 0xFF000000);                 // border
        graphics.fill(x + 1, y + 1, right - 1, bottom - 1, 0xFF000000 | (argb.getAsInt() & 0xFFFFFF));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {

    }
}
