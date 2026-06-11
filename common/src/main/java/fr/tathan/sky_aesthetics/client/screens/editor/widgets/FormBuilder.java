package fr.tathan.sky_aesthetics.client.screens.editor.widgets;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

/**
 * Adds editor form rows to a scrollable {@link FormList}: a labelled control (label + control) or a
 * whole-width control.
 */
public class FormBuilder {

    private final FormList list;

    public FormBuilder(FormList list) {
        this.list = list;
    }

    /** A labelled control: label on the left, control on the right. */
    public FormBuilder labelled(Component label, AbstractWidget control) {
        list.addLabelled(label, control);
        return this;
    }

    /** A control spanning the full row width (e.g. a checkbox that carries its own label). */
    public FormBuilder wide(AbstractWidget control) {
        list.addWide(control);
        return this;
    }
}
