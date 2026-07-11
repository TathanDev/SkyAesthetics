package fr.tathan.sky_aesthetics.client.screens.editor;

import fr.tathan.sky_aesthetics.client.screens.editor.widgets.ColorSwatchWidget;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * A simple RGB(A) color picker: one slider per channel plus a live swatch. Components are edited in
 * the 0-1 range; every change is reported to {@code onChange} so callers (and the live preview) can
 * react immediately. Closing returns to the parent screen.
 */
public class ColorPickerScreen extends Screen {

    private static final String[] CHANNELS = {"R", "G", "B", "A"};

    private final Screen parent;
    private final boolean hasAlpha;
    private final float[] working;
    private final Consumer<float[]> onChange;

    public ColorPickerScreen(Screen parent, float[] initial, boolean hasAlpha, Consumer<float[]> onChange) {
        super(Component.translatable("sky_aesthetics.editor.color.title"));
        this.parent = parent;
        this.hasAlpha = hasAlpha;
        this.onChange = onChange;
        int channels = hasAlpha ? 4 : 3;
        this.working = new float[channels];
        for (int i = 0; i < channels && i < initial.length; i++) {
            this.working[i] = initial[i];
        }
    }

    @Override
    protected void init() {
        int width = 200;
        int x = (this.width - width) / 2;
        int y = this.height / 2 - 70;

        this.addRenderableWidget(new StringWidget(x, y, width, 12, this.title, this.font));
        y += 18;

        this.addRenderableWidget(new ColorSwatchWidget(x, y, width, 22, this::argb));
        y += 30;

        int channels = hasAlpha ? 4 : 3;
        for (int i = 0; i < channels; i++) {
            this.addRenderableWidget(new ChannelSlider(x, y, width, i));
            y += 22;
        }

        y += 6;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> this.onClose())
                .bounds(x, y, width, 20).build());
    }

    private int argb() {
        int a = hasAlpha ? to255(working[3]) : 255;
        int r = to255(working[0]);
        int g = to255(working[1]);
        int b = to255(working[2]);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int to255(float v) {
        return Math.clamp(Math.round(v * 255f), 0, 255);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }

    private class ChannelSlider extends AbstractSliderButton {
        private final int index;

        ChannelSlider(int x, int y, int width, int index) {
            super(x, y, width, 20, Component.empty(), working[index]);
            this.index = index;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(CHANNELS[index] + ": " + to255(working[index])));
        }

        @Override
        protected void applyValue() {
            working[index] = (float) this.value;
            onChange.accept(working.clone());
        }
    }
}
