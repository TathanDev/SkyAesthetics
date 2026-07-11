package fr.tathan.sky_aesthetics.client.screens.editor.widgets;

import fr.tathan.sky_aesthetics.client.screens.editor.ColorPickerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A button that displays the current color as a swatch and opens the {@link ColorPickerScreen} when
 * clicked. The picker reads/writes colors as float components in the range 0-1; the static factories
 * adapt the various model vector types ({@link Vector3i} 0-255, {@link Vector3f}/{@link Vector4f} 0-1).
 */
public class ColorButton extends AbstractButton {

    private final Screen parent;
    private final Supplier<float[]> getter;
    private final Consumer<float[]> setter;
    private final boolean hasAlpha;

    public ColorButton(int width, Screen parent, Supplier<float[]> getter, Consumer<float[]> setter, boolean hasAlpha) {
        super(0, 0, width, EditorWidgets.CONTROL_HEIGHT, Component.empty());
        this.parent = parent;
        this.getter = getter;
        this.setter = setter;
        this.hasAlpha = hasAlpha;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int inset = 3;
        int x = getX() + inset;
        int y = getY() + inset;
        int right = getX() + getWidth() - inset;
        int bottom = getY() + getHeight() - inset;
        graphics.fill(x, y, right, bottom, 0xFF000000);
        graphics.fill(x + 1, y + 1, right - 1, bottom - 1, argb());
    }

    @Override
    public void onPress(InputWithModifiers input) {
        Minecraft.getInstance().gui.setScreen(new ColorPickerScreen(parent, getter.get(), hasAlpha, setter));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    private int argb() {
        float[] c = getter.get();
        int a = (hasAlpha && c.length > 3) ? clamp(c[3]) : 255;
        int r = clamp(c[0]);
        int g = clamp(c[1]);
        int b = clamp(c[2]);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int clamp(float normalised) {
        return Math.clamp(Math.round(normalised * 255f), 0, 255);
    }

    public static ColorButton forVec3i(Screen parent, Vector3i v) {
        return new ColorButton(EditorWidgets.CONTROL_WIDTH, parent,
                () -> new float[]{v.x / 255f, v.y / 255f, v.z / 255f},
                c -> v.set(Math.round(c[0] * 255f), Math.round(c[1] * 255f), Math.round(c[2] * 255f)),
                false);
    }

    public static ColorButton forVec3f(Screen parent, Vector3f v) {
        return new ColorButton(EditorWidgets.CONTROL_WIDTH, parent,
                () -> new float[]{v.x, v.y, v.z},
                c -> v.set(c[0], c[1], c[2]),
                false);
    }

    public static ColorButton forVec4f(Screen parent, Vector4f v) {
        return new ColorButton(EditorWidgets.CONTROL_WIDTH, parent,
                () -> new float[]{v.x, v.y, v.z, v.w},
                c -> v.set(c[0], c[1], c[2], c.length > 3 ? c[3] : v.w),
                true);
    }
}
