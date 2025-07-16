package fr.tathan.sky_aesthetics.client.screens;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.data.SkiesRegistry;
import fr.tathan.sky_aesthetics.client.skies.DimensionSky;
import fr.tathan.sky_aesthetics.client.skies.settings.*;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SkyModificationTest extends BaseOwoScreen<FlowLayout> {
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        FlowLayout skyComponents = SkiesComponents.createDefaultComponent(
                SkiesRegistry.SKY_DEV == null ? SkyProperties.createDefault() : SkiesRegistry.SKY_DEV.getSkyProperties()
        );

        rootComponent.child(
                Containers.horizontalFlow(Sizing.fill(), Sizing.content())
                        .child(
                                Containers.collapsible(Sizing.fill(75), Sizing.content(), net.minecraft.network.chat.Component.literal("Sky Settings"), true)
                                        .child(Containers.verticalScroll(Sizing.fill(50), Sizing.fill(90),
                                                skyComponents.id("sky_components")
                                                ).surface(Surface.VANILLA_TRANSLUCENT).id("vertical_scroll")
                                        ).id("collapsible_container")
                                )
                        .child(
                                Containers.verticalFlow(Sizing.fill(), Sizing.content())
                                        .child(Components.button(net.minecraft.network.chat.Component.literal("Save Sky Properties"), button -> {
                                            skyToText(skyComponents);
                                        }).id("save_button"))
                                        .child(createSkiesImportDropdown(rootComponent, skyComponents).id("import_dropdown"))
                                        .child(createToggleDevSkyButton(skyComponents).horizontalSizing(Sizing.content()).id("toggle_button").margins(Insets.top(10)))
                                        .child(createReloadButton(skyComponents).horizontalSizing(Sizing.content()).id("reload_button").margins(Insets.top(10)))

                                        .margins(Insets.of(10, 0, -20, 0))
                        ).id("horizontal_flow")
        );

    }

    public ButtonComponent createToggleDevSkyButton(FlowLayout skyComponents) {
        net.minecraft.network.chat.Component loadButtonText = SkiesRegistry.SKY_DEV == null ?
                net.minecraft.network.chat.Component.literal("Load Dev Sky") :
                net.minecraft.network.chat.Component.literal("Disable Test Sky");

        return Components.button(loadButtonText, button -> {

            if(SkiesRegistry.SKY_DEV == null) {
                setDevSkyComponent(skyComponents);
            } else {
                SkiesRegistry.setSkyDev(null);
            }
            button.setMessage(SkiesRegistry.SKY_DEV == null ?
                    net.minecraft.network.chat.Component.literal("Load Dev Sky") :
                    net.minecraft.network.chat.Component.literal("Disable Test Sky"));

        });
    }

    public ButtonComponent createReloadButton(FlowLayout skyComponents) {

        return Components.button(net.minecraft.network.chat.Component.literal("Reload Sky"), button -> setDevSkyComponent(skyComponents));
    }

    @Nullable
    public SkyProperties setDevSkyComponent(@Nullable FlowLayout skyComponents) {

        if(skyComponents != null) {
            SkyProperties properties = fromComponent(skyComponents);
            SkiesRegistry.setSkyDev(properties.toDimensionSky());
            SkyAesthetics.LOG.error("Sky Dev set to: {}", properties.id());
            return properties;
        } else {
            SkiesRegistry.setSkyDev(null);
        }
        return null;
    }

    public FlowLayout getSkyComponentsFromPath(FlowLayout rootComponent) {
        return rootComponent
                .childById(FlowLayout.class, "horizontal_flow")
                .childById(CollapsibleContainer.class, "collapsible_container")
                .childById(ScrollContainer.class, "vertical_scroll")
                .childById(FlowLayout.class, "sky_components");
    }

    public void skyToText(FlowLayout component) {
        SkyProperties properties = fromComponent(component);
        saveSkyProperties(properties);
    }

    @SuppressWarnings("unchecked")
    public DropdownComponent createSkiesImportDropdown(FlowLayout rootComponent, FlowLayout skyComponents) {
        DropdownComponent dropdown = Components.dropdown(Sizing.content());
        dropdown.margins(Insets.top(10));
        dropdown.text(net.minecraft.network.chat.Component.literal("Import Skies"));

        for (DimensionSky sky : SkiesRegistry.SKY_PROPERTIES.values()) {

            dropdown.button(net.minecraft.network.chat.Component.literal(sky.getSkyId().toString()), (component) -> {
                ScrollContainer<FlowLayout> scrollContainer = rootComponent
                        .childById(FlowLayout.class, "horizontal_flow")
                        .childById(CollapsibleContainer.class, "collapsible_container")
                        .childById(ScrollContainer.class, "vertical_scroll");

                scrollContainer.child(SkiesComponents.createDefaultComponent(sky.getSkyProperties()));

            });
        }



        return dropdown;

    }


    public static SkyProperties fromComponent(FlowLayout component) {

        CollapsibleContainer basicSettings = component.childById(CollapsibleContainer.class, "basic_settings");
        String id = basicSettings.childById(TextBoxComponent.class, "id").getValue();
        String world = basicSettings.childById(TextBoxComponent.class, "dimension").getValue();
        boolean weather = basicSettings.childById(CheckboxComponent.class, "weather").selected();


        CollapsibleContainer cloudSettings = component.childById(CollapsibleContainer.class, "cloud_settings");

        Optional<CloudSettings> cloud = !cloudSettings.expanded() ? Optional.empty() :
                Optional.of(new CloudSettings(
                        cloudSettings.childById(CheckboxComponent.class, "cloud").selected(),
                        (int) cloudSettings.childById(DiscreteSliderComponent.class, "cloud_height").discreteValue()
                ));

        cloud.ifPresent(settings -> SkyAesthetics.LOG.info("Sky Settings:\n {}", settings));

        CollapsibleContainer fogSettings = component.childById(CollapsibleContainer.class, "fog_settings");

        Optional<FogSettings> fog = !fogSettings.expanded() ? Optional.empty() :
                Optional.of(new FogSettings(
                        fogSettings.childById(CheckboxComponent.class, "fog").selected(),
                        getVec3iFromComponent(fogSettings.childById(FlowLayout.class, "fog_color")),
                        getVec2fFromComponent(fogSettings.childById(FlowLayout.class, "fog_density"))
                ));

        CollapsibleContainer skyColorSettings = component.childById(CollapsibleContainer.class, "color_settings");

        Optional<SkyColorSettings> skyColor = !skyColorSettings.expanded() ? Optional.empty() :
                Optional.of(new SkyColorSettings(
                        getVec4fFromComponent(skyColorSettings.childById(FlowLayout.class, "sky_color")),
                        getVec3iFromComponent(skyColorSettings.childById(FlowLayout.class, "sunset_color")),
                        skyColorSettings.childById(TextBoxComponent.class, "alpha_modifier").getValue().equals("-1") ? Optional.empty() : Optional.of((int) convertValue(skyColorSettings.childById(TextBoxComponent.class, "alpha_modifier").getValue(), Integer.class))
                ));

        CollapsibleContainer skyObjectsSettings = component.childById(CollapsibleContainer.class, "sky_objects");

        FlowLayout skyObjects = skyObjectsSettings.childById(FlowLayout.class, "objects");


        List<SkyObject> objects = new ArrayList<>(List.of());

        for (Component child : skyObjects.children()) {

            if (child instanceof CollapsibleContainer container && (container.id() != null && container.id().equals("object"))) {

                if(!container.expanded()) container.toggleExpansion();

                FlowLayout layout = (FlowLayout) container.children().getLast();


                ResourceLocation location = ResourceLocation.parse(layout.childById(TextBoxComponent.class, "texture").getValue());
                boolean blend = layout.childById(CheckboxComponent.class, "blend").selected();
                int size = (int) convertValue(layout.childById(TextBoxComponent.class, "size").getValue(), Integer.class);
                int height = (int) convertValue(layout.childById(TextBoxComponent.class, "height").getValue(), Integer.class);

                String rotationType = getRotationType(layout.childById(TextBoxComponent.class, "rotation_type").getValue());

                FlowLayout rotationLayout = layout.childById(FlowLayout.class, "rotation");
                Vector3f rotation = new Vector3f(
                        (float) convertValue(rotationLayout.childById(TextBoxComponent.class, "x").getValue(), Float.class),
                        (float) convertValue(rotationLayout.childById(TextBoxComponent.class, "y").getValue(), Float.class),
                        (float) convertValue(rotationLayout.childById(TextBoxComponent.class, "z").getValue(), Float.class)
                );

                FlowLayout objectRotationLayout = layout.childById(FlowLayout.class, "object_rotation");
                Vector3f objectRotation = new Vector3f(
                        (float) convertValue(objectRotationLayout.childById(TextBoxComponent.class, "x").getValue(), Float.class),
                        (float) convertValue(objectRotationLayout.childById(TextBoxComponent.class, "y").getValue(), Float.class),
                        (float) convertValue(objectRotationLayout.childById(TextBoxComponent.class, "z").getValue(), Float.class)
                );

                objects.add(new SkyObject(
                        location,
                        blend,
                        size,
                        rotation,
                        objectRotation,
                        height,
                        rotationType
                ));

            }

        }

        CollapsibleContainer starSettings = component.childById(CollapsibleContainer.class, "star_settings");

        StarSettings stars = new StarSettings(
                        starSettings.childById(CheckboxComponent.class, "vanilla").selected(),
                        starSettings.childById(CheckboxComponent.class, "moving_stars").selected(),
                        (int) starSettings.childById(DiscreteSliderComponent.class, "count").discreteValue(),
                        starSettings.childById(CheckboxComponent.class, "all_days_visible").selected(),
                        (float) convertValue(starSettings.childById(TextBoxComponent.class, "scale").getValue(), Float.class),
                        getVec3iFromComponent(starSettings.childById(FlowLayout.class, "star_color")).get(),
                        Optional.empty(),
                        Optional.empty()
                );

        return new SkyProperties(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(world)),
                ResourceLocation.parse(id),
                cloud,
                fog,
                weather,
                Optional.empty(),
                Optional.empty(),
                stars,
                skyColor,
                objects,
                Optional.empty()
        );

    }

    public static String getRotationType(String str) {
        return switch (str.toLowerCase()) {
            case "static" -> "STATIC";
            case "day" -> "DAY";
            default -> "STATIC";
        };
    }

    public static Optional<Vector3i> getVec3iFromComponent(FlowLayout component) {
        Vector3i color = new Vector3i(
                (int) convertValue(component.childById(TextBoxComponent.class, "x").getValue(), Integer.class),
                (int) convertValue(component.childById(TextBoxComponent.class, "y").getValue(), Integer.class),
                (int) convertValue(component.childById(TextBoxComponent.class, "z").getValue(), Integer.class)
        );

        return (color.x() == 0 && color.y() == 0 && color.z() == 0) ? Optional.empty() : Optional.of(color);
    }

    public static Optional<Vector2f> getVec2fFromComponent(FlowLayout component) {
        Vector2f color = new Vector2f(
                (float) convertValue(component.childById(TextBoxComponent.class, "x").getValue(), Float.class),
                (float) convertValue(component.childById(TextBoxComponent.class, "y").getValue(), Float.class)
        );

        return (color.x() == 0 && color.y() == 0 ) ? Optional.empty() : Optional.of(color);
    }

    public static Optional<Vector4f> getVec4fFromComponent(FlowLayout component) {
        Vector4f color = new Vector4f(
                (float) convertValue(component.childById(TextBoxComponent.class, "x").getValue(), Float.class),
                (float) convertValue(component.childById(TextBoxComponent.class, "y").getValue(), Float.class),
                (float) convertValue(component.childById(TextBoxComponent.class, "z").getValue(), Float.class),
                (float) convertValue(component.childById(TextBoxComponent.class, "w").getValue(), Float.class)

        );

        return (color.x() == 0 && color.y() == 0 ) ? Optional.empty() : Optional.of(color);
    }

    public static Object convertValue(String str, Class<?> type) {
        try {
            return switch (type.getSimpleName()) {
                case "int", "Integer" -> Integer.parseInt(str);
                case "long", "Long" -> Long.parseLong(str);
                case "double", "Double" -> Double.parseDouble(str);
                case "float", "Float" -> Float.parseFloat(str);
                default -> str;
            };
        } catch (Exception e) {
            return 0;
        }
    }

    public void saveSkyProperties(SkyProperties properties) {

        try {
            JsonElement element = SkyProperties.CODEC
                    .encodeStart(JsonOps.INSTANCE, properties)
                    .result()
                    .orElseThrow(() -> new IllegalStateException("Failed to encode to JSON"));


            Path resourcePackPath = this.minecraft.getResourcePackDirectory().resolve(properties.id().getPath());
            Path directoryPath = resourcePackPath.resolve("assets").resolve(properties.id().getNamespace()).resolve(SkyAesthetics.MODID);

            Path metadataPath = resourcePackPath.resolve("pack.mcmeta");
            Path filePath = directoryPath.resolve(properties.id().getPath() + ".json");

            if (!filePath.getParent().toFile().exists()) {
                filePath.getParent().toFile().mkdirs();
                metadataPath.getParent().toFile().mkdirs();
            }

            writeFile(filePath, element);

            PackMetadataSection section = new PackMetadataSection(net.minecraft.network.chat.Component.literal(properties.id().getPath() + " Generated Sky"), SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES), Optional.empty());

            JsonElement sectionElement = PackMetadataSection.CODEC
                    .encodeStart(JsonOps.INSTANCE, section)
                    .result()
                    .orElseThrow(() -> new IllegalStateException("Failed to encode to JSON"));

            JsonObject packMetadata = new JsonObject();
            packMetadata.add("pack", sectionElement);

            writeFile(metadataPath, packMetadata);
            Util.getPlatform().openUri(resourcePackPath.toUri());

        } catch (Exception e) {
            SkyAesthetics.LOG.error("Failed to save sky properties to file", e);
        }
    }

    public static void writeFile(Path path, JsonElement element) {
        try {
            File folder = path.toFile().getParentFile();
            if (!folder.exists())
                folder.mkdirs();

            Writer writer = new FileWriter(path.toFile());
            SkyAesthetics.GSON.toJson(element, writer);
            writer.close();
        } catch (Exception e) {
            SkyAesthetics.LOG.error("Failed to write file: {}", path, e);
        }
    }

}
