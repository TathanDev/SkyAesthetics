package fr.tathan.sky_aesthetics.client.screens;

import fr.tathan.sky_aesthetics.client.skies.settings.*;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.List;
import java.util.Optional;

public class SkiesComponents {

    public static FlowLayout createDefaultComponent(SkyProperties properties) {
        return (FlowLayout) Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(createBasicSettings(properties).id("basic_settings"))
                .child(createCloudSettings(properties.cloudSettings()).id("cloud_settings"))
                .child(createFogSettings(properties.fogSettings()).id("fog_settings"))
                .child(createColorSettings(properties.skyColor()).id("color_settings"))
                .child(createSkyObjects(properties.skyObjects()).id("sky_objects"))
                .child(createStarSettings(properties.stars()).id("star_settings")).id("sky_components");
    }

    public static CollapsibleContainer createBasicSettings(SkyProperties properties) {
        CollapsibleContainer container = Containers.collapsible(Sizing.content(), Sizing.content(0), Component.literal("Basic Settings"), true);

        container.child(Components.textBox(Sizing.fill(75), properties.id().toString()).id("id")).tooltip(Component.literal("The ID of the sky"));
        container.child(Components.textBox(Sizing.fill(75), properties.world().location().toString()).id("dimension").tooltip(Component.literal("The dimension of the sky")));
        container.child(Components.checkbox(Component.literal("weather")).checked(properties.weather()).id("weather")).tooltip(Component.literal("Should the weather be rendered?"));
        return container;
    }

    public static CollapsibleContainer createCloudSettings(Optional<CloudSettings> settings) {
        CollapsibleContainer container = Containers.collapsible(Sizing.content(), Sizing.content(0), Component.literal("Cloud Settings (Optionnal)"), settings.isPresent());

        CloudSettings setting = settings.orElseGet(CloudSettings::createDefaultSettings);

        container.child(Components.checkbox(Component.literal("cloud")).checked(setting.showCloud()).id("cloud").tooltip(Component.literal("Should clouds be rendered")));
        container.child(Components.discreteSlider(Sizing.fill(50), 0, 300).value(setting.cloudHeight()).id("cloud_height").tooltip(Component.literal("Cloud Height in blocks")));
        return container;
    }

    public static CollapsibleContainer createFogSettings(Optional<FogSettings> settings) {
        CollapsibleContainer container = Containers.collapsible(Sizing.content(), Sizing.content(), Component.literal("Fog Settings (Optionnal)"), settings.isPresent());

        FogSettings setting = settings.orElseGet(FogSettings::createDefaultSettings);

        Vector3i fogColor = setting.customFogColor().orElse(new Vector3i(0, 0, 0));
        container.child(Containers.horizontalFlow(Sizing.content(), Sizing.content())
                        .child(Components.textBox(Sizing.fill(25)).text(String.valueOf(fogColor.x)).id("x"))
                        .child(Components.textBox(Sizing.fill(25)).text(String.valueOf(fogColor.y)).id("y"))
                        .child(Components.textBox(Sizing.fill(25)).text(String.valueOf(fogColor.z)).id("z"))
                        .tooltip(Component.literal("The RGB values for the fog color"))
                        .id("fog_color"));

        Vector2f fogDensity = setting.fogDensity().orElse(new Vector2f(0, 0));
        container.child(Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(Components.textBox(Sizing.fill(25)).text(String.valueOf(fogDensity.x)).id("x"))
                .child(Components.textBox(Sizing.fill(25)).text(String.valueOf(fogDensity.y)).id("y"))
                .tooltip(Component.literal("The min and max density of the fog"))
                .id("fog_density"));

        container.child(Components.checkbox(Component.literal("fog")).checked(setting.fog()).id("fog"));

        return container;
    }

    public static CollapsibleContainer createColorSettings(Optional<SkyColorSettings> settings) {
        CollapsibleContainer container = Containers.collapsible(Sizing.content(), Sizing.content(), Component.literal("Sky Color Settings (Optionnal)"), settings.isPresent());

        SkyColorSettings setting = settings.orElseGet(SkyColorSettings::createDefaultSettings);

        Vector3i fogColor = setting.sunsetColor().orElse(new Vector3i(0, 0, 0));
        container.child(Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(Components.textBox(Sizing.fill(20)).text(String.valueOf(fogColor.x)).id("x"))
                .child(Components.textBox(Sizing.fill(20)).text(String.valueOf(fogColor.y)).id("y"))
                .child(Components.textBox(Sizing.fill(20)).text(String.valueOf(fogColor.z)).id("z"))
                .tooltip(Component.literal("RGB values for sunset color"))
                .id("sunset_color"));

        Vector4f fogDensity = setting.color().orElse(new Vector4f(0, 0,0, 0));
        container.child(Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(Components.textBox(Sizing.fill(20)).text(String.valueOf(fogDensity.x)).id("x"))
                .child(Components.textBox(Sizing.fill(20)).text(String.valueOf(fogDensity.y)).id("y"))
                .child(Components.textBox(Sizing.fill(20)).text(String.valueOf(fogDensity.z)).id("z"))
                .child(Components.textBox(Sizing.fill(20)).text(String.valueOf(fogDensity.w)).id("w"))
                .tooltip(Component.literal("RGBA values for sky color"))
                .id("sky_color"));

        int alphaModifier = setting.sunriseAlphaModifier().orElse(1);
        container.child(Components.textBox(Sizing.fill(25)).text(String.valueOf(alphaModifier))
                .tooltip(Component.literal("Sky color alpha modifier for sunrise"))
                .id("alpha_modifier"));
        return container;
    }

    public static CollapsibleContainer createSkyObjects(List<SkyObject> skyObjects) {
        CollapsibleContainer container = (CollapsibleContainer) Containers.collapsible(Sizing.content(), Sizing.content(), Component.literal("Sky Objects"), true).id("sky_objects");

        FlowLayout objectsLayout = (FlowLayout) Containers.verticalFlow(Sizing.content(), Sizing.content()).id("objects");

        for (SkyObject skyObject : skyObjects) {
            objectsLayout.child(createSkyObject(skyObject, container).id("object"));
        }

        container.child(Containers.horizontalFlow(Sizing.content(), Sizing.content())
                        .child(Components.button(Component.literal("Add Sky Object"), (buttonComponent -> {
                            SkyObject newObject = new SkyObject(
                                    ResourceLocation.parse("default_texture"),
                                    false,
                                    1.0f,
                                    new Vector3f(0, 0, 0),
                                    new Vector3f(0, 0, 0),
                                    100,
                                    "STATIC"
                            );
                            objectsLayout.child(createSkyObject(newObject, container).id("object"));
                    })
                        )).id("add_button"));

        container.child(objectsLayout);

        return container;
    }

    public static CollapsibleContainer createSkyObject(SkyObject skyObject, BaseParentComponent parent) {
        CollapsibleContainer container = Containers.collapsible(Sizing.content(), Sizing.content(), Component.literal("Object : " + parent.children().size()), false);

        container
                .child(Components.checkbox(Component.literal("Blend")).checked(skyObject.blend()).id("blend").tooltip(Component.literal("Should the object be blended?")))
                .child(Components.textBox(Sizing.fill(75)).text(skyObject.texture().toString()).id("texture").tooltip(Component.literal("The texture of the sky object")))
                .child(Components.textBox(Sizing.fill(25)).text(String.valueOf(skyObject.size())).id("size").tooltip(Component.literal("The size of the sky object")))
                .child(Components.textBox(Sizing.fill(25)).text(String.valueOf(skyObject.height())).id("height").tooltip(Component.literal("The height of the sky object in blocks")))
                .child(Components.textBox(Sizing.fill(50)).text(skyObject.rotationType()).id("rotation_type"))
                .child(Containers.horizontalFlow(Sizing.content(), Sizing.content())
                        .child(Components.textBox(Sizing.fill(25)).text(String.valueOf(skyObject.rotation().x)).id("x"))
                        .child(Components.textBox(Sizing.fill(25)).text(String.valueOf(skyObject.rotation().y)).id("y"))
                        .child(Components.textBox(Sizing.fill(25)).text(String.valueOf(skyObject.rotation().z)).id("z"))
                        .tooltip(Component.literal("X, Y, Z position of the sky object"))
                        .id("rotation"))
                .child(Containers.horizontalFlow(Sizing.content(), Sizing.content())
                        .child(Components.textBox(Sizing.fill(25)).text(String.valueOf(skyObject.objectRotation().x)).id("x"))
                        .child(Components.textBox(Sizing.fill(25)).text(String.valueOf(skyObject.objectRotation().y)).id("y"))
                        .child(Components.textBox(Sizing.fill(25)).text(String.valueOf(skyObject.objectRotation().z)).id("z"))
                        .tooltip(Component.literal("X, Y, Z rotation of the sky object"))
                        .id("object_rotation"))
                .child(Components.button(Component.literal("Remove"), button -> {
                    parent.removeChild(container);
                }));

        return container;
    }


    public static CollapsibleContainer createStarSettings(StarSettings setting) {
        CollapsibleContainer container = Containers.collapsible(Sizing.content(), Sizing.content(), Component.literal("Star Settings"), true);


        container.child(Components.checkbox(Component.literal("Vanilla")).checked(setting.vanilla()).id("vanilla").tooltip(Component.literal("Should vanilla stars be rendered?")))
                .child(Components.checkbox(Component.literal("Moving Stars")).checked(setting.movingStars()).id("moving_stars").tooltip(Component.literal("Should the stars move?")))
                .child(Components.discreteSlider(Sizing.fill(50), 0, 100000).value(setting.count()).id("count").tooltip(Component.literal("Star Count")))
                .child(Components.checkbox(Component.literal("All Day Visible")).checked(setting.movingStars()).id("all_days_visible").tooltip(Component.literal("Should stars be visible all day?")))
                .child(Components.textBox(Sizing.fill(25), String.valueOf(setting.scale())).id("scale").tooltip(Component.literal("Star Scale")))
                .child(Containers.horizontalFlow(Sizing.content(), Sizing.content())
                        .child(Components.textBox(Sizing.fill(25)).text(String.valueOf(setting.color().x)).id("x"))
                        .child(Components.textBox(Sizing.fill(25)).text(String.valueOf(setting.color().y)).id("y"))
                        .child(Components.textBox(Sizing.fill(25)).text(String.valueOf(setting.color().z)).id("z"))
                        .tooltip(Component.literal("RGB values for star color"))
                        .id("star_color"));

        return container;
    }

}
