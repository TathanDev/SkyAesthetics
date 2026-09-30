package fr.tathan.sky_aesthetics.client.screens.editor;

import fr.tathan.sky_aesthetics.client.screens.editor.widgets.ColorSwatchWidget;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * A simple RGB color picker: one slider per channel plus a live swatch. Components are edited in
 * the 0-1 range; every change is reported to {@code onChange} so callers (and the live preview) can
 * react immediately. Closing returns to the parent screen.
 */
public class ColorPickerScreen extends Screen {

    private static final String[] CHANNELS = {"R", "G", "B"};

    private final Screen parent;
    private final float[] working;
    private final Consumer<float[]> onChange;

    public ColorPickerScreen(Screen parent, float[] initial, Consumer<float[]> onChange) {
        super(Component.translatable("sky_aesthetics.editor.color.title"));
        this.parent = parent;
        this.onChange = onChange;
        this.working = new float[CHANNELS.length];
        for (int i = 0; i < CHANNELS.length && i < initial.length; i++) {
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

        this.addRenderableWidget(new ColorSwatchWidget(x, y, width, 22, () -> argb(working)));
        y += 30;

        for (int i = 0; i < CHANNELS.length; i++) {
            this.addRenderableWidget(new ChannelSlider(x, y, width, i));
            y += 22;
        }

        y += 6;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> this.onClose())
                .bounds(x, y, width, 20).build());
    }

    public static int argb(float[] rgb) {
        return 0xFF000000 | (to255(rgb[0]) << 16) | (to255(rgb[1]) << 8) | to255(rgb[2]);
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
