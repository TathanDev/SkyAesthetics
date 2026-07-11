package fr.tathan.sky_aesthetics.client.screens.editor;

import com.mojang.blaze3d.platform.InputConstants;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.data.SkiesRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/**
 * Loader-agnostic entry points for the sky editor. The {@link #OPEN_EDITOR} keybinding is
 * registered by each loader, and both the keybinding and the {@code /skyaesthetics editor} command
 * funnel through {@link #openEditor()}.
 */
public final class SkyEditorEntry {

    public static final String KEY_OPEN = "key." + SkyAesthetics.MODID + ".open_editor";

    /** Custom controls category; its label key is {@code key.category.sky_aesthetics.editor}. */
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(SkyAesthetics.MODID, "editor"));

    public static final KeyMapping OPEN_EDITOR = new KeyMapping(
            KEY_OPEN, InputConstants.KEY_O, CATEGORY);

    /** The editor instance currently in "peek" mode (screen closed so the player can move), if any. */
    private static SkyEditorScreen peekingScreen;

    private SkyEditorEntry() {
    }

    /**
     * Opens the editor on the client main thread. If a peek session is active (screen was closed so
     * the player could move) the existing editor is reopened instead of spawning a fresh one, so edits
     * are never silently discarded. Safe to call from key handlers and commands.
     */
    public static void openEditor() {
        Minecraft mc = Minecraft.getInstance();
        if (peekingScreen != null) {
            SkyEditorScreen screen = peekingScreen;
            mc.execute(screen::resumePeek);
        } else {
            mc.execute(() -> mc.setScreen(new SkyEditorScreen()));
        }
    }

    /** Records the editor that just entered peek mode so the next key press can reopen it. */
    public static void beginPeek(SkyEditorScreen screen) {
        peekingScreen = screen;
    }

    /** Clears the peek session (called once the editor has been reopened). */
    public static void endPeek() {
        peekingScreen = null;
    }

    /**
     * Consumes any queued presses of the editor keybinding. Call once per client tick. While peeking
     * the key reopens the existing editor (preserving edits) instead of spawning a fresh one.
     */
    public static void handleKeyInput() {
        if (Minecraft.getInstance().level == null) {
            peekingScreen = null;
            SkiesRegistry.clearPreviewSky();
        }
        while (OPEN_EDITOR.consumeClick()) {
            openEditor();
        }
    }
}
