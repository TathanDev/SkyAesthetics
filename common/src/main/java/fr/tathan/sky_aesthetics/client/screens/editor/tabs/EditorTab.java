package fr.tathan.sky_aesthetics.client.screens.editor.tabs;

import fr.tathan.sky_aesthetics.client.screens.editor.widgets.FormList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * A single category tab in the sky editor. Its content is a scrollable {@link FormList} that fills
 * the tab area, so tabs with many rows scroll instead of overflowing.
 */
public class EditorTab implements Tab {

    private final Component title;
    private final FormList list;

    public EditorTab(Component title, FormList list) {
        this.title = title;
        this.list = list;
    }

    public FormList list() {
        return list;
    }

    @Override
    public Component getTabTitle() {
        return title;
    }

    @Override
    public Component getTabExtraNarration() {
        return title;
    }

    @Override
    public void visitChildren(Consumer<AbstractWidget> consumer) {
        consumer.accept(list);
    }

    @Override
    public void doLayout(ScreenRectangle rectangle) {
        list.updateSizeAndPosition(rectangle.width(), rectangle.height(), rectangle.left(), rectangle.top());
    }

    @Override
    public Layout getLayout() {
        return LinearLayout.horizontal();
    }
}
