package fr.tathan.sky_aesthetics.client.screens.editor;

import fr.tathan.sky_aesthetics.client.screens.editor.model.EditablePack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ExportScreen extends Screen {

    private final Screen parent;
    private final EditablePack pack;
    private final Runnable onConfirm;

    public ExportScreen(Screen parent, EditablePack pack, Runnable onConfirm) {
        super(Component.translatable("sky_aesthetics.editor.export.title"));
        this.parent = parent;
        this.pack = pack;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        int w = 240;
        int x = (this.width - w) / 2;
        int y = this.height / 2 - 56;

        this.addRenderableWidget(new StringWidget(x, y, w, 12, this.title, this.font));
        y += 20;

        this.addRenderableWidget(new StringWidget(x, y, w, 10,
                Component.translatable("sky_aesthetics.editor.export.name"), this.font));
        y += 12;
        EditBox nameBox = new EditBox(this.font, x, y, w, 18, Component.empty());
        nameBox.setMaxLength(64);
        nameBox.setValue(pack.name == null ? "" : pack.name);
        nameBox.setResponder(s -> pack.name = s);
        this.addRenderableWidget(nameBox);
        y += 24;

        this.addRenderableWidget(new StringWidget(x, y, w, 10,
                Component.translatable("sky_aesthetics.editor.export.description"), this.font));
        y += 12;
        EditBox descBox = new EditBox(this.font, x, y, w, 18, Component.empty());
        descBox.setMaxLength(128);
        descBox.setValue(pack.description == null ? "" : pack.description);
        descBox.setResponder(s -> pack.description = s);
        this.addRenderableWidget(descBox);
        y += 28;

        int bw = 110;
        int gap = 6;
        int bx = (this.width - bw * 2 - gap) / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose())
                .bounds(bx, y, bw, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("sky_aesthetics.editor.action.export"), b -> {
                    this.minecraft.setScreen(parent);
                    onConfirm.run();
                }).bounds(bx + bw + gap, y, bw, 20).build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
