package fr.tathan.sky_aesthetics.client.screens;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import fr.tathan.SkyAesthetics;
import fr.tathan.sky_aesthetics.client.skies.settings.*;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.DiscreteSliderComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.List;
import java.util.Optional;

public class SkyModificationTest extends BaseOwoScreen<FlowLayout> {
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.child(Containers.verticalScroll(Sizing.fill(50), Sizing.fill(100),

                SkiesComponents.createDefaultComponent(
                        (FlowLayout) Containers.verticalFlow(Sizing.content(), Sizing.content()).id("sky_components"), SkyProperties.createDefault()
                ).child(Components.button(net.minecraft.network.chat.Component.literal("Save Sky Properties"), button -> {
                    FlowLayout component = rootComponent.childById(FlowLayout.class, "sky_components");
                    SkyProperties properties = fromComponent(component);

                    JsonElement element = SkyProperties.CODEC
                            .encodeStart(JsonOps.INSTANCE, properties)
                            .result()
                            .orElseThrow(() -> new IllegalStateException("Failed to encode to JSON"));

                    SkyAesthetics.LOG.info("Saving sky properties:\n {}", element);

                }).id("save_button")))
        );

    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

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
                        (int) cloudSettings.childById(DiscreteSliderComponent.class, "cloud_height").value()
                ));


        CollapsibleContainer fogSettings = component.childById(CollapsibleContainer.class, "fog_settings");

        Optional<FogSettings> fog = !fogSettings.expanded() ? Optional.empty() :
                Optional.of(new FogSettings(
                        fogSettings.childById(CheckboxComponent.class, "fog").selected(),
                        getVec3iFromComponent(fogSettings.childById(FlowLayout.class, "fog_color")),
                        getVec2fFromComponent(fogSettings.childById(FlowLayout.class, "fog_density"))
                ));

        CollapsibleContainer skyColorSettings = component.childById(CollapsibleContainer.class, "color_settings");
        String alphaModifier = skyColorSettings.childById(TextBoxComponent.class, "alpha_modifier").getValue();
        Optional<SkyColorSettings> skyColor = !skyColorSettings.expanded() ? Optional.empty() :
                Optional.of(new SkyColorSettings(
                        getVec4fFromComponent(skyColorSettings.childById(FlowLayout.class, "sky_color")),
                        getVec3iFromComponent(skyColorSettings.childById(FlowLayout.class, "sunset_color")),
                        alphaModifier.equals("-1") ? Optional.empty() : Optional.of((int) convertValue(alphaModifier, Integer.class))
                ));

        CollapsibleContainer skyObjects = component.childById(CollapsibleContainer.class, "sky_objects");
        List<SkyObject> objects = new java.util.ArrayList<>(List.of());

        for (Component child : skyObjects.children()) {
            if (child instanceof FlowLayout layout) {
                for(Component childs : layout.children()) {

                    if(childs instanceof CollapsibleContainer collapsibleContainer && collapsibleContainer.id() != null && collapsibleContainer.id().equals("object")) {


                        collapsibleContainer.children().forEach((c) -> SkyAesthetics.LOG.info("Child: {} {}", c.getClass().getSimpleName(), c.id() == null ? "null" : c.id()));
                        continue;
                        /**
                        ResourceLocation location = ResourceLocation.parse(collapsibleContainer.childById(TextBoxComponent.class, "texture").getValue());
                        boolean blend = collapsibleContainer.childById(CheckboxComponent.class, "blend").selected();
                        int size = (int) convertValue(collapsibleContainer.childById(TextBoxComponent.class, "size").getValue(), Integer.class);
                        int height = (int) convertValue(collapsibleContainer.childById(TextBoxComponent.class, "height").getValue(), Integer.class);

                        String rotationType = getRotationType(collapsibleContainer.childById(TextBoxComponent.class, "rotation_type").getValue());

                        FlowLayout rotationLayout = collapsibleContainer.childById(FlowLayout.class, "rotation");
                        Vector3f rotation = new Vector3f(
                                (float) convertValue(rotationLayout.childById(TextBoxComponent.class, "x").getValue(), Float.class),
                                (float) convertValue(rotationLayout.childById(TextBoxComponent.class, "y").getValue(), Float.class),
                                (float) convertValue(rotationLayout.childById(TextBoxComponent.class, "z").getValue(), Float.class)
                        );

                        FlowLayout objectRotationLayout = collapsibleContainer.childById(FlowLayout.class, "object_rotation");
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
                         */
                    }
                }
            }
        }

        CollapsibleContainer starSettings = component.childById(CollapsibleContainer.class, "star_settings");

        StarSettings stars = new StarSettings(
                        starSettings.childById(CheckboxComponent.class, "vanilla").selected(),
                        starSettings.childById(CheckboxComponent.class, "moving_stars").selected(),
                        (int) starSettings.childById(DiscreteSliderComponent.class, "count").value(),
                        starSettings.childById(CheckboxComponent.class, "all_days_visible").selected(),
                        (int) convertValue(starSettings.childById(TextBoxComponent.class, "scale").getValue(), Integer.class),
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

}
