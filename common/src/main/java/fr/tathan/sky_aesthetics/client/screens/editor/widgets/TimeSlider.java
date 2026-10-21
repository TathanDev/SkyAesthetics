package fr.tathan.sky_aesthetics.client.screens.editor.widgets;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

public class TimeSlider extends AbstractSliderButton {

    private static final int DAY = 24000;
    private final IntConsumer onChange;

    public TimeSlider(int width, int height, int initialTicks, IntConsumer onChange) {
        super(0, 0, width, height, Component.empty(), initialTicks / (double) DAY);
        this.onChange = onChange;
        updateMessage();
    }

    private int ticks() {
        return (int) Math.round(value * DAY) % DAY;
    }

    @Override
    protected void updateMessage() {
        int ticks = ticks();
        setMessage(Component.literal("Time: " + ticks + " (" + label(ticks) + ")"));
    }

    @Override
    protected void applyValue() {
        onChange.accept(ticks());
    }

    private static String label(int ticks) {
        if (ticks < 1000) return "sunrise";
        if (ticks < 11000) return ticks >= 5000 && ticks <= 7000 ? "noon" : "day";
        if (ticks < 13500) return "sunset";
        if (ticks < 22500) return ticks >= 17000 && ticks <= 19000 ? "midnight" : "night";
        return "sunrise";
    }
}
