package fr.tathan.sky_aesthetics.client.screens.editor.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.List;
import java.util.function.Consumer;

/**
 * Factory helpers for the vanilla widgets used to build the sky editor forms. Each control binds
 * directly to a field of the {@link fr.tathan.sky_aesthetics.client.screens.editor.model.EditableSky}
 * model via a setter callback. Vectors are entered as comma-separated values to keep one widget per
 * field (no nested layouts to register).
 */
public final class EditorWidgets {

    public static final int CONTROL_WIDTH = 170;
    public static final int CONTROL_HEIGHT = 18;

    private EditorWidgets() {
    }

    public static EditBox text(Font font, String initial, Consumer<String> onChange) {
        EditBox box = new EditBox(font, CONTROL_WIDTH, CONTROL_HEIGHT, Component.empty());
        box.setMaxLength(256);
        box.setValue(initial == null ? "" : initial);
        box.setResponder(onChange);
        return box;
    }

    public static EditBox number(Font font, String initial, Consumer<String> onChange) {
        EditBox box = new EditBox(font, CONTROL_WIDTH, CONTROL_HEIGHT, Component.empty());
        box.setValue(initial);
        box.setResponder(onChange);
        return box;
    }

    public static Checkbox check(Font font, Component label, boolean initial, Consumer<Boolean> onChange) {
        return Checkbox.builder(label, font)
                .selected(initial)
                .onValueChange((cb, value) -> onChange.accept(value))
                .build();
    }

    public static Button cycle(int width, List<String> options, String initial, Consumer<String> onChange) {
        final int[] index = {Math.max(0, options.indexOf(initial))};
        return Button.builder(Component.literal(options.get(index[0])), b -> {
            index[0] = (index[0] + 1) % options.size();
            String value = options.get(index[0]);
            b.setMessage(Component.literal(value));
            onChange.accept(value);
        }).width(width).build();
    }

    public static EditBox vec3f(Font font, Vector3f v) {
        EditBox box = new EditBox(font, CONTROL_WIDTH, CONTROL_HEIGHT, Component.empty());
        box.setValue(fmt(v.x) + ", " + fmt(v.y) + ", " + fmt(v.z));
        box.setResponder(s -> {
            float[] p = parseFloats(s, 3);
            if (p != null) v.set(p[0], p[1], p[2]);
        });
        return box;
    }

    public static EditBox vec3i(Font font, Vector3i v) {
        EditBox box = new EditBox(font, CONTROL_WIDTH, CONTROL_HEIGHT, Component.empty());
        box.setValue(v.x + ", " + v.y + ", " + v.z);
        box.setResponder(s -> {
            int[] p = parseInts(s, 3);
            if (p != null) v.set(p[0], p[1], p[2]);
        });
        return box;
    }

    public static EditBox vec4f(Font font, Vector4f v) {
        EditBox box = new EditBox(font, CONTROL_WIDTH, CONTROL_HEIGHT, Component.empty());
        box.setValue(fmt(v.x) + ", " + fmt(v.y) + ", " + fmt(v.z) + ", " + fmt(v.w));
        box.setResponder(s -> {
            float[] p = parseFloats(s, 4);
            if (p != null) v.set(p[0], p[1], p[2], p[3]);
        });
        return box;
    }

    public static EditBox vec2f(Font font, Vector2f v) {
        EditBox box = new EditBox(font, CONTROL_WIDTH, CONTROL_HEIGHT, Component.empty());
        box.setValue(fmt(v.x) + ", " + fmt(v.y));
        box.setResponder(s -> {
            float[] p = parseFloats(s, 2);
            if (p != null) v.set(p[0], p[1]);
        });
        return box;
    }

    // ── Parsing helpers ────────────────────────────────────────────────────────────────

    public static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    public static float parseFloat(String s, float fallback) {
        try {
            return Float.parseFloat(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static float[] parseFloats(String s, int n) {
        String[] parts = s.split(",");
        if (parts.length != n) return null;
        float[] out = new float[n];
        for (int i = 0; i < n; i++) {
            try {
                out[i] = Float.parseFloat(parts[i].trim());
            } catch (Exception e) {
                return null;
            }
        }
        return out;
    }

    private static int[] parseInts(String s, int n) {
        String[] parts = s.split(",");
        if (parts.length != n) return null;
        int[] out = new int[n];
        for (int i = 0; i < n; i++) {
            try {
                out[i] = Integer.parseInt(parts[i].trim());
            } catch (Exception e) {
                return null;
            }
        }
        return out;
    }

    private static String fmt(float f) {
        if (f == Math.rint(f)) return String.valueOf((long) f);
        return String.valueOf(f);
    }
}
