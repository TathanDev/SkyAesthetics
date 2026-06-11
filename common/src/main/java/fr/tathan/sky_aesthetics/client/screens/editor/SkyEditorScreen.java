package fr.tathan.sky_aesthetics.client.screens.editor;

import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.data.SkiesRegistry;
import fr.tathan.sky_aesthetics.client.screens.editor.model.EditablePack;
import fr.tathan.sky_aesthetics.client.screens.editor.model.EditableSky;
import fr.tathan.sky_aesthetics.client.screens.editor.tabs.EditorTab;
import fr.tathan.sky_aesthetics.client.screens.editor.export.SkyPackExporter;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.input.KeyEvent;
import fr.tathan.sky_aesthetics.client.screens.editor.widgets.ColorButton;
import fr.tathan.sky_aesthetics.client.screens.editor.widgets.EditorWidgets;
import fr.tathan.sky_aesthetics.client.screens.editor.widgets.FormBuilder;
import fr.tathan.sky_aesthetics.client.screens.editor.widgets.FormList;
import fr.tathan.sky_aesthetics.client.settings.Rotation;
import fr.tathan.sky_aesthetics.client.settings.SkyProperties;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * The in-game sky pack editor: a tabbed screen (one tab per setting category) with a left sidebar
 * listing every sky in the pack (one per dimension) and a footer action bar. Editing state lives in
 * the {@link EditablePack}/{@link EditableSky} model; the screen rebuilds its widgets from the model
 * whenever the selected sky changes.
 */
public class SkyEditorScreen extends Screen {

    private static final String DOCS_URL = "https://tathandev.github.io/SkyAesthetics/sky/";
    private static final int SIDEBAR_WIDTH = 124;
    private static final int FOOTER_HEIGHT = 32;

    /** The category tabs, in display order. Keys map to {@code sky_aesthetics.editor.tab.<key>}. */
    private static final String[] CATEGORIES = {
            "basic", "clouds", "fog", "sun", "moon", "stars", "objects", "skybox", "colors", "light"
    };

    private final EditablePack pack;
    private EditableSky active;
    private int activeTabIndex = 0;
    private boolean previewing = false;
    private Button previewButton;
    /** Serialized pack state at the last saved point (open or export); used to detect unsaved edits. */
    private String savedSnapshot;

    /** When true the whole editor UI is hidden so the live sky is visible in the world (toggle: F1). */
    private boolean peeking = false;
    /** True when peeking turned the preview on by itself, so we clear it again when peeking ends. */
    private boolean peekPushedPreview = false;
    /** Serialization of the active sky last pushed to preview; avoids rebuilding the renderer every tick. */
    private String lastPreviewSnapshot = "";

    private TabManager tabManager;
    private TabNavigationBar tabNavigationBar;
    private List<Tab> tabs = new ArrayList<>();
    /** Sidebar sky buttons in the same order as {@code pack.skies}, kept for live label updates. */
    private List<Button> sidebarButtons = new ArrayList<>();

    public SkyEditorScreen() {
        this(new EditablePack());
    }

    public SkyEditorScreen(EditablePack pack) {
        super(Component.translatable("sky_aesthetics.editor.title"));
        this.pack = pack;
    }

    @Override
    protected void init() {
        if (pack.skies.isEmpty()) {
            pack.addSky();
        }
        if (active == null || !pack.skies.contains(active)) {
            active = pack.skies.getFirst();
        }
        if (savedSnapshot == null) {
            savedSnapshot = snapshot();
        }

        tabManager = new TabManager(this::addRenderableWidget, this::removeWidget,
                tab -> activeTabIndex = Math.max(0, tabs.indexOf(tab)), tab -> {
        });

        tabs = buildTabs();
        tabNavigationBar = TabNavigationBar.builder(tabManager, this.width)
                .addTabs(tabs.toArray(new Tab[0]))
                .build();
        this.addRenderableWidget(tabNavigationBar);
        tabNavigationBar.arrangeElements();

        int contentTop = tabNavigationBar.getRectangle().bottom();
        int footerTop = this.height - FOOTER_HEIGHT;

        buildSidebar(contentTop, footerTop);
        buildFooter(footerTop);

        ScreenRectangle tabArea = new ScreenRectangle(
                SIDEBAR_WIDTH, contentTop, this.width - SIDEBAR_WIDTH, Math.max(0, footerTop - contentTop));
        tabManager.setTabArea(tabArea);

        tabNavigationBar.selectTab(Mth.clamp(activeTabIndex, 0, tabs.size() - 1), false);

        if (previewing) pushPreview();
    }

    private List<Tab> buildTabs() {
        List<Tab> result = new ArrayList<>();

        for (String key : CATEGORIES) {
            FormList list = new FormList(this.minecraft, this.font, 200, 100, 0);
            populateTab(key, list, active);
            result.add(new EditorTab(Component.translatable("sky_aesthetics.editor.tab." + key), list));
        }
        return result;
    }

    private static final List<String> ROTATION_TYPES = List.of("DAY", "NIGHT", "STATIC");
    private static final List<String> AXES = List.of("XP", "YP", "ZP");

    /** Fills a category tab's scrollable list with setting rows bound to the sky's model fields. */
    private void populateTab(String key, FormList list, EditableSky sky) {
        FormBuilder f = new FormBuilder(list);
        switch (key) {
            case "basic" -> populateBasic(f, sky);
            case "clouds" -> populateClouds(f, sky);
            case "fog" -> populateFog(f, sky);
            case "sun" -> populateSun(f, sky);
            case "moon" -> populateMoon(f, sky);
            case "stars" -> populateStars(f, sky);
            case "objects" -> populateObjects(f, sky);
            case "skybox" -> populateSkybox(f, sky);
            case "colors" -> populateColors(f, sky);
            case "light" -> populateLight(f, sky);
        }
    }

    private void populateBasic(FormBuilder f, EditableSky sky) {
        f.labelled(lit("Sky ID"), EditorWidgets.text(font, sky.skyId, s -> sky.skyId = s));
        f.labelled(lit("Dimension"), EditorWidgets.text(font, sky.dimension, s -> sky.dimension = s));
        f.wide(EditorWidgets.check(font, lit("Weather"), sky.weather, b -> sky.weather = b));
    }

    private void populateClouds(FormBuilder f, EditableSky sky) {
        f.wide(section("Enable clouds", sky.cloud.enabled, b -> sky.cloud.enabled = b));
        if (!sky.cloud.enabled) return;
        f.wide(EditorWidgets.check(font, lit("Show clouds"), sky.cloud.showCloud, b -> sky.cloud.showCloud = b));
        f.labelled(lit("Cloud height"), EditorWidgets.number(font, String.valueOf(sky.cloud.cloudHeight),
                s -> sky.cloud.cloudHeight = EditorWidgets.parseInt(s, sky.cloud.cloudHeight)));
        f.wide(section("Custom cloud color", sky.cloud.colorEnabled, b -> sky.cloud.colorEnabled = b));
        if (sky.cloud.colorEnabled) {
            f.labelled(lit("Color"), ColorButton.forVec3i(this, sky.cloud.color));
        }
    }

    private void populateFog(FormBuilder f, EditableSky sky) {
        f.wide(section("Enable fog settings", sky.fog.enabled, b -> sky.fog.enabled = b));
        if (!sky.fog.enabled) return;
        f.wide(EditorWidgets.check(font, lit("Fog"), sky.fog.fog, b -> sky.fog.fog = b));
        f.wide(section("Custom fog color", sky.fog.colorEnabled, b -> sky.fog.colorEnabled = b));
        if (sky.fog.colorEnabled) {
            f.labelled(lit("Color"), ColorButton.forVec3i(this, sky.fog.color));
        }
        f.wide(section("Custom density", sky.fog.densityEnabled, b -> sky.fog.densityEnabled = b));
        if (sky.fog.densityEnabled) {
            f.labelled(lit("Density (near, far)"), EditorWidgets.vec2f(font, sky.fog.density));
        }
    }

    private void populateSun(FormBuilder f, EditableSky sky) {
        f.wide(section("Enable custom sun", sky.sun.enabled, b -> sky.sun.enabled = b));
        if (!sky.sun.enabled) return;
        f.wide(EditorWidgets.check(font, lit("Show"), sky.sun.show, b -> sky.sun.show = b));
        f.wide(section("Custom texture", sky.sun.textureEnabled, b -> sky.sun.textureEnabled = b));
        if (sky.sun.textureEnabled) {
            f.labelled(lit("Texture"), EditorWidgets.text(font, sky.sun.texture, s -> sky.sun.texture = s));
        }
        f.labelled(lit("Size"), EditorWidgets.number(font, String.valueOf(sky.sun.size),
                s -> sky.sun.size = EditorWidgets.parseFloat(s, sky.sun.size)));
        f.labelled(lit("Intensity"), EditorWidgets.number(font, String.valueOf(sky.sun.intensity),
                s -> sky.sun.intensity = EditorWidgets.parseFloat(s, sky.sun.intensity)));
    }

    private void populateMoon(FormBuilder f, EditableSky sky) {
        f.wide(section("Enable custom moon", sky.moon.enabled, b -> sky.moon.enabled = b));
        if (!sky.moon.enabled) return;
        f.wide(EditorWidgets.check(font, lit("Show"), sky.moon.show, b -> sky.moon.show = b));
        f.wide(section("Custom texture", sky.moon.textureEnabled, b -> sky.moon.textureEnabled = b));
        if (sky.moon.textureEnabled) {
            f.labelled(lit("Texture"), EditorWidgets.text(font, sky.moon.texture, s -> sky.moon.texture = s));
        }
        f.labelled(lit("Size"), EditorWidgets.number(font, String.valueOf(sky.moon.size),
                s -> sky.moon.size = EditorWidgets.parseFloat(s, sky.moon.size)));
        f.wide(EditorWidgets.check(font, lit("Show phases"), sky.moon.showPhases, b -> sky.moon.showPhases = b));
        f.labelled(lit("Intensity"), EditorWidgets.number(font, String.valueOf(sky.moon.intensity),
                s -> sky.moon.intensity = EditorWidgets.parseFloat(s, sky.moon.intensity)));
    }

    private void populateStars(FormBuilder f, EditableSky sky) {
        f.wide(section("Enable stars", sky.stars.enabled, b -> sky.stars.enabled = b));
        if (!sky.stars.enabled) return;
        f.wide(section("Vanilla stars", sky.stars.vanilla, b -> sky.stars.vanilla = b));
        if (!sky.stars.vanilla) {
            f.wide(EditorWidgets.check(font, lit("Moving stars"), sky.stars.movingStars, b -> sky.stars.movingStars = b));
            f.labelled(lit("Count"), EditorWidgets.number(font, String.valueOf(sky.stars.count),
                    s -> sky.stars.count = EditorWidgets.parseInt(s, sky.stars.count)));
            f.wide(EditorWidgets.check(font, lit("Visible all day"), sky.stars.allDaysVisible, b -> sky.stars.allDaysVisible = b));
            f.labelled(lit("Scale"), EditorWidgets.number(font, String.valueOf(sky.stars.scale),
                    s -> sky.stars.scale = EditorWidgets.parseFloat(s, sky.stars.scale)));
            f.labelled(lit("Color"), ColorButton.forVec3i(this, sky.stars.color));
        }

        f.wide(section("Shooting stars", sky.stars.shootingEnabled, b -> sky.stars.shootingEnabled = b));
        if (sky.stars.shootingEnabled) {
            f.labelled(lit("Chance %"), EditorWidgets.number(font, String.valueOf(sky.stars.shootingPercentage),
                    s -> sky.stars.shootingPercentage = EditorWidgets.parseInt(s, sky.stars.shootingPercentage)));
            f.labelled(lit("Lifetime (min, max)"), EditorWidgets.vec2f(font, sky.stars.shootingLifetime));
            f.labelled(lit("Scale"), EditorWidgets.number(font, String.valueOf(sky.stars.shootingScale),
                    s -> sky.stars.shootingScale = EditorWidgets.parseFloat(s, sky.stars.shootingScale)));
            f.labelled(lit("Speed"), EditorWidgets.number(font, String.valueOf(sky.stars.shootingSpeed),
                    s -> sky.stars.shootingSpeed = EditorWidgets.parseFloat(s, sky.stars.shootingSpeed)));
            f.labelled(lit("Color"), ColorButton.forVec3f(this, sky.stars.shootingColor));
            f.wide(section("Fixed rotation", sky.stars.shootingRotationEnabled, b -> sky.stars.shootingRotationEnabled = b));
            if (sky.stars.shootingRotationEnabled) {
                f.labelled(lit("Rotation (deg)"), EditorWidgets.number(font, String.valueOf(sky.stars.shootingRotation),
                        s -> sky.stars.shootingRotation = EditorWidgets.parseInt(s, sky.stars.shootingRotation)));
            }
        }
    }

    private void populateObjects(FormBuilder f, EditableSky sky) {
        for (int i = 0; i < sky.skyObjects.size(); i++) {
            EditableSky.Obj o = sky.skyObjects.get(i);
            final int index = i;
            f.wide(new StringWidget(lit("Object " + (i + 1)), this.font));
            f.labelled(lit("Texture"), EditorWidgets.text(font, o.texture, s -> o.texture = s));
            f.wide(EditorWidgets.check(font, lit("Blend"), o.blend, b -> o.blend = b));
            f.labelled(lit("Size"), EditorWidgets.number(font, String.valueOf(o.size),
                    s -> o.size = EditorWidgets.parseFloat(s, o.size)));
            f.labelled(lit("Position (x,y,z)"), EditorWidgets.vec3f(font, o.rotation));
            f.labelled(lit("Self-rotation (x,y,z)"), EditorWidgets.vec3f(font, o.objectRotation));
            f.labelled(lit("Height"), EditorWidgets.number(font, String.valueOf(o.height),
                    s -> o.height = EditorWidgets.parseInt(s, o.height)));
            f.labelled(lit("Rotation type"), EditorWidgets.cycle(EditorWidgets.CONTROL_WIDTH, ROTATION_TYPES,
                    o.rotationType, v -> o.rotationType = v));
            f.wide(Button.builder(lit("Remove object " + (i + 1)), b -> {
                sky.skyObjects.remove(index);
                this.rebuildWidgets();
            }).width(EditorWidgets.CONTROL_WIDTH).build());
        }
        f.wide(Button.builder(lit("+ Add object"), b -> {
            sky.skyObjects.add(new EditableSky.Obj());
            this.rebuildWidgets();
        }).width(EditorWidgets.CONTROL_WIDTH).build());
    }

    private void populateSkybox(FormBuilder f, EditableSky sky) {
        f.wide(section("Enable skybox", sky.skyBox.enabled, b -> sky.skyBox.enabled = b));
        if (!sky.skyBox.enabled) return;
        f.labelled(lit("Gradation"), EditorWidgets.number(font, String.valueOf(sky.skyBox.gradation),
                s -> sky.skyBox.gradation = EditorWidgets.parseInt(s, sky.skyBox.gradation)));
        f.labelled(lit("Texture"), EditorWidgets.text(font, sky.skyBox.texture, s -> sky.skyBox.texture = s));
        f.labelled(lit("Rotation (x,y,z)"), EditorWidgets.vec3f(font, sky.skyBox.rotation));
        f.wide(section("Dynamic rotation", sky.skyBox.dynamicEnabled, b -> sky.skyBox.dynamicEnabled = b));
        if (sky.skyBox.dynamicEnabled) {
            f.labelled(lit("Axis"), EditorWidgets.cycle(EditorWidgets.CONTROL_WIDTH, AXES,
                    sky.skyBox.dynamicAxis.name(), v -> sky.skyBox.dynamicAxis = Rotation.Axis.valueOf(v)));
            f.labelled(lit("Rotation type"), EditorWidgets.cycle(EditorWidgets.CONTROL_WIDTH, ROTATION_TYPES,
                    sky.skyBox.dynamicRotationType, v -> sky.skyBox.dynamicRotationType = v));
        }
    }

    private void populateColors(FormBuilder f, EditableSky sky) {
        f.wide(section("Enable sky colors", sky.skyColor.enabled, b -> sky.skyColor.enabled = b));
        if (!sky.skyColor.enabled) return;
        f.wide(section("Sky disc color", sky.skyColor.skyColorEnabled, b -> sky.skyColor.skyColorEnabled = b));
        if (sky.skyColor.skyColorEnabled) {
            f.labelled(lit("Sky color"), ColorButton.forVec4f(this, sky.skyColor.skyColor));
        }
        f.wide(section("Sunset color", sky.skyColor.sunsetColorEnabled, b -> sky.skyColor.sunsetColorEnabled = b));
        if (sky.skyColor.sunsetColorEnabled) {
            f.labelled(lit("Sunset color"), ColorButton.forVec3i(this, sky.skyColor.sunsetColor));
        }
        f.wide(section("Sunrise alpha modifier", sky.skyColor.alphaModifierEnabled, b -> sky.skyColor.alphaModifierEnabled = b));
        if (sky.skyColor.alphaModifierEnabled) {
            f.labelled(lit("Alpha modifier"), EditorWidgets.number(font, String.valueOf(sky.skyColor.alphaModifier),
                    s -> sky.skyColor.alphaModifier = EditorWidgets.parseInt(s, sky.skyColor.alphaModifier)));
        }
    }

    private void populateLight(FormBuilder f, EditableSky sky) {
        f.wide(section("Enable light settings", sky.light.enabled, b -> sky.light.enabled = b));
        if (!sky.light.enabled) return;
        f.wide(EditorWidgets.check(font, lit("Force bright lightmap"), sky.light.forceBrightLightmap,
                b -> sky.light.forceBrightLightmap = b));
        f.wide(EditorWidgets.check(font, lit("Constant ambient light"), sky.light.constantAmbientLight,
                b -> sky.light.constantAmbientLight = b));
    }

    /** A section-enable checkbox that rebuilds the screen so dependent rows show/hide. */
    private net.minecraft.client.gui.components.Checkbox section(String label, boolean initial, java.util.function.Consumer<Boolean> setter) {
        return EditorWidgets.check(font, lit(label), initial, b -> {
            setter.accept(b);
            this.rebuildWidgets();
        });
    }

    private static Component lit(String s) {
        return Component.literal(s);
    }

    private void buildSidebar(int top, int footerTop) {
        int x = 6;
        int w = SIDEBAR_WIDTH - 12;
        int h = 20;
        int gap = 4;
        int y = top + 6;

        sidebarButtons = new ArrayList<>();
        for (EditableSky sky : pack.skies) {
            Button button = Button.builder(Component.literal(sky.displayName()), b -> selectSky(sky))
                    .bounds(x, y, w, h).build();
            if (sky == active) {
                button.active = false; // mark the current selection
            }
            this.addRenderableWidget(button);
            sidebarButtons.add(button);
            y += h + gap;
        }

        y += gap;
        this.addRenderableWidget(Button.builder(
                Component.translatable("sky_aesthetics.editor.sidebar.add"), b -> {
                    active = pack.addSky();
                    this.rebuildWidgets();
                }).bounds(x, y, w, h).build());
        y += h + gap;

        this.addRenderableWidget(Button.builder(
                Component.translatable("sky_aesthetics.editor.sidebar.duplicate"), b -> {
                    active = pack.duplicate(active);
                    this.rebuildWidgets();
                }).bounds(x, y, w, h).build());
        y += h + gap;

        Button remove = Button.builder(
                Component.translatable("sky_aesthetics.editor.sidebar.remove"), b -> {
                    pack.remove(active);
                    active = null; // re-resolved in init()
                    this.rebuildWidgets();
                }).bounds(x, y, w, h).build();
        remove.active = pack.skies.size() > 1;
        this.addRenderableWidget(remove);
    }

    private void buildFooter(int footerTop) {
        int h = 20;
        int y = footerTop + (FOOTER_HEIGHT - h) / 2;

        previewButton = Button.builder(previewLabel(), b -> onPreview()).width(90).build();
        Button preview = previewButton;
        Button peek = Button.builder(Component.translatable("sky_aesthetics.editor.action.peek"),
                b -> togglePeek()).width(80).build();
        Button export = Button.builder(Component.translatable("sky_aesthetics.editor.action.export"),
                b -> this.minecraft.setScreen(new ExportScreen(this, pack, this::onExport))).width(110).build();
        Button docs = Button.builder(Component.translatable("sky_aesthetics.editor.action.docs"),
                b -> Util.getPlatform().openUri(DOCS_URL)).width(110).build();

        int gap = 6;
        List<Button> buttons = List.of(preview, peek, export, docs);
        int total = -gap;
        for (Button button : buttons) total += button.getWidth() + gap;
        int x = (this.width - total) / 2;
        for (Button button : buttons) {
            button.setX(x);
            button.setY(y);
            this.addRenderableWidget(button);
            x += button.getWidth() + gap;
        }
    }

    private void selectSky(EditableSky sky) {
        this.active = sky;
        if (previewing) pushPreview();
        this.rebuildWidgets();
    }

    private Component previewLabel() {
        return Component.translatable(previewing
                ? "sky_aesthetics.editor.action.preview_stop"
                : "sky_aesthetics.editor.action.preview");
    }

    /** Toggles live preview of the active sky in the world behind the editor. */
    private void onPreview() {
        previewing = !previewing;
        if (previewing) {
            lastPreviewSnapshot = "";
            pushPreview();
        } else if (!peeking) {
            SkiesRegistry.clearPreviewSky();
        }
        if (previewButton != null) previewButton.setMessage(previewLabel());
    }

    /** Footer button / F1 handler. The editor screen is open when this runs, so it only enters peek. */
    private void togglePeek() {
        if (!peeking) enterPeek();
    }

    /**
     * Enters "peek" mode: closes the editor screen so the player can walk and look around the world
     * with the edited sky shown live. There is no reload and no lost edits — the same screen instance
     * is reopened (preserving all state) when the editor key is pressed again. While peeking the
     * preview is forced on even if the persistent Preview toggle is off.
     */
    private void enterPeek() {
        peeking = true;
        if (!previewing) {
            peekPushedPreview = true;
            lastPreviewSnapshot = "";
            pushPreview();
        }
        SkyEditorEntry.beginPeek(this);
        if (this.minecraft.gui != null) {
            this.minecraft.gui.setOverlayMessage(Component.translatable(
                    "sky_aesthetics.editor.peek.hint", SkyEditorEntry.OPEN_EDITOR.getTranslatedKeyMessage()), false);
        }
        this.minecraft.setScreen(null);
    }

    /** Reopens the editor from peek mode; called by {@link SkyEditorEntry} when the editor key fires. */
    public void resumePeek() {
        peeking = false;
        SkyEditorEntry.endPeek();
        if (peekPushedPreview && !previewing) {
            peekPushedPreview = false;
            SkiesRegistry.clearPreviewSky();
            lastPreviewSnapshot = "";
        }
        this.minecraft.setScreen(this);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** True whenever a live preview should be tracking the active sky (footer toggle or peek). */
    private boolean isPreviewActive() {
        return previewing || peeking;
    }

    @Override
    public void tick() {
        super.tick();
        // Keep sidebar labels in sync with live dimension/id edits.
        List<EditableSky> skies = pack.skies;
        for (int i = 0; i < Math.min(skies.size(), sidebarButtons.size()); i++) {
            sidebarButtons.get(i).setMessage(Component.literal(skies.get(i).displayName()));
        }
        if (isPreviewActive()) {
            String snap = activeSkySnapshot();
            if (!snap.equals(lastPreviewSnapshot)) {
                pushPreview();
                lastPreviewSnapshot = snap;
            }
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_F1) {
            togglePeek();
            return true;
        }
        return super.keyPressed(event);
    }

    /** Serializes the active sky for change detection; mirrors the per-sky part of {@link #snapshot()}. */
    private String activeSkySnapshot() {
        try {
            return SkyProperties.CODEC.encodeStart(JsonOps.INSTANCE, active.toProperties())
                    .result().map(Object::toString).orElse("invalid");
        } catch (Exception e) {
            return "invalid:" + e.getMessage();
        }
    }

    /** Pushes the active sky's current state to the live-preview slot, ignoring invalid input. */
    private void pushPreview() {
        try {
            SkyProperties props = active.toProperties();
            // Remap to the player's current dimension so the preview is always visible
            // regardless of which dimension the sky is authored for.
            if (this.minecraft.level != null) {
                var dim = this.minecraft.level.dimension();
                if (!props.world().equals(dim)) {
                    props = new SkyProperties(dim, props.id(), props.cloudSettings(),
                            props.weather(), props.sun(), props.moon(), props.stars(),
                            props.skyColorSettings(), props.skyObjects(), props.renderCondition(),
                            props.environmentAttributes(), props.skyBoxSetting(),
                            props.lightSettings(), props.fogSettings());
                }
            }
            SkiesRegistry.setPreviewSky(props);
        } catch (Exception e) {
            SkyAesthetics.LOG.warn("Could not preview sky (invalid field?): {}", e.getMessage());
        }
    }

    /** Validates and exports the whole pack, then auto-enables it and reloads resources. */
    private void onExport() {
        List<String> errors = pack.validate();
        if (!errors.isEmpty()) {
            toast(SystemToast.SystemToastId.PACK_LOAD_FAILURE,
                    Component.translatable("sky_aesthetics.toast.error.title"), lit(errors.getFirst()));
            return;
        }

        SkyPackExporter.Result result = SkyPackExporter.export(pack);
        if (!result.success()) {
            String detail = result.errors().isEmpty() ? "" : result.errors().getFirst();
            toast(SystemToast.SystemToastId.PACK_LOAD_FAILURE,
                    Component.translatable("sky_aesthetics.toast.error.title"), lit(detail));
            return;
        }

        enablePack(result.folderName());
        savedSnapshot = snapshot();
        toast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                Component.translatable("sky_aesthetics.toast.success.title"),
                Component.translatable("sky_aesthetics.toast.success.description", pack.skies.size()));
        if (result.packPath() != null) Util.getPlatform().openPath(result.packPath());
    }

    /** Adds the exported pack to the repository, enables it, and triggers a resource reload. */
    private void enablePack(String folderName) {
        try {
            PackRepository repo = this.minecraft.getResourcePackRepository();
            repo.reload();
            String id = "file/" + folderName;
            if (!repo.getAvailableIds().contains(id)) {
                SkyAesthetics.LOG.warn("Exported pack '{}' not found in repository after reload", id);
                return;
            }
            List<String> selected = new ArrayList<>(repo.getSelectedIds());
            if (!selected.contains(id)) selected.add(id);
            repo.setSelected(selected);
            this.minecraft.options.updateResourcePacks(repo);
            this.minecraft.reloadResourcePacks();
        } catch (Exception e) {
            SkyAesthetics.LOG.error("Failed to auto-enable exported pack '{}'", folderName, e);
        }
    }

    private void toast(SystemToast.SystemToastId id, Component title, Component description) {
        SystemToast.add(this.minecraft.getToastManager(), id, title, description);
    }

    @Override
    public void onClose() {
        if (!hasUnsavedChanges()) {
            SkiesRegistry.clearPreviewSky();
            this.minecraft.setScreen(null);
            return;
        }
        if (previewing) pushPreview();
        this.minecraft.setScreen(new ConfirmScreen(
                confirmed -> {
                    this.minecraft.setScreen(confirmed ? null : this);
                    if (confirmed) SkiesRegistry.clearPreviewSky();
                },
                Component.translatable("sky_aesthetics.editor.confirm.title"),
                Component.translatable("sky_aesthetics.editor.confirm.message")));
    }

    private boolean hasUnsavedChanges() {
        return !snapshot().equals(savedSnapshot);
    }

    /** A stable serialization of the whole pack, used only to detect unsaved edits. */
    private String snapshot() {
        StringBuilder sb = new StringBuilder();
        sb.append(pack.name).append(' ').append(pack.description);
        for (EditableSky sky : pack.skies) {
            sb.append(' ');
            try {
                sb.append(SkyProperties.CODEC.encodeStart(JsonOps.INSTANCE, sky.toProperties())
                        .result().map(Object::toString).orElse("invalid"));
            } catch (Exception e) {
                sb.append("invalid:").append(e.getMessage());
            }
        }
        return sb.toString();
    }
}
