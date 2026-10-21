package fr.tathan.sky_aesthetics.client.screens.editor.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * A scrollable, single-column form of setting rows. Being a {@link ContainerObjectSelectionList} it
 * provides a scrollbar, scissor clipping and input routing for free, so dense category tabs no longer
 * overflow the screen. Each {@link Row} holds an optional label plus one or more control widgets.
 */
public class FormList extends ContainerObjectSelectionList<FormList.Row> {

    private static final int ROW_HEIGHT = 24;
    private static final int LABEL_WIDTH = 120;

    private final Font font;

    public FormList(Minecraft minecraft, Font font, int width, int height, int y0) {
        super(minecraft, width, height, y0, ROW_HEIGHT);
        this.font = font;
    }

    public void addLabelled(Component label, AbstractWidget control) {
        addEntry(new Row(font, label, List.of(control)));
    }

    public void addWide(AbstractWidget control) {
        addEntry(new Row(font, null, List.of(control)));
    }

    public void addRow(List<AbstractWidget> controls) {
        addEntry(new Row(font, null, controls));
    }

    @Override
    public int getRowWidth() {
        return this.width - 12;
    }

    public static class Row extends ContainerObjectSelectionList.Entry<Row> {

        private final StringWidget label; // nullable
        private final List<AbstractWidget> controls;
        private final List<AbstractWidget> widgets = new ArrayList<>();

        Row(Font font, Component label, List<AbstractWidget> controls) {
            this.controls = controls;
            if (label != null) {
                this.label = new StringWidget(label, font);
                this.widgets.add(this.label);
            } else {
                this.label = null;
            }
            this.widgets.addAll(controls);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return widgets;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return widgets;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int x = getX();
            int y = getY();
            int height = getHeight();

            int controlX = x;
            if (label != null) {
                label.setX(x);
                label.setY(y + (height - label.getHeight()) / 2);
                controlX = x + LABEL_WIDTH;
            }
            for (AbstractWidget control : controls) {
                control.setX(controlX);
                control.setY(y + (height - control.getHeight()) / 2);
                controlX += control.getWidth() + 4;
            }

            for (AbstractWidget widget : widgets) {
                widget.extractRenderState(graphics, mouseX, mouseY, partialTick);
            }
        }
    }
}
