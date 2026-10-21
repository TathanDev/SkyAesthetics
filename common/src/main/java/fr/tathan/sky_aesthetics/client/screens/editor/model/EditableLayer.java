package fr.tathan.sky_aesthetics.client.screens.editor.model;

import com.mojang.datafixers.util.Either;
import fr.tathan.sky_aesthetics.client.settings.SkyBoxLayer;
import fr.tathan.sky_aesthetics.client.settings.SkyRotation;
import fr.tathan.sky_aesthetics.client.settings.SkyTexture;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class EditableLayer {

    public static final String CUSTOM_CURVE = "custom";
    public static final List<String> FACE_NAMES = List.of("Up", "Down", "North", "South", "East", "West");

    public boolean cubemap = false;
    public String texture = "";
    /** In {@link SkyTexture.Face} order: up, down, north, south, east, west. */
    public final String[] faces = {"", "", "", "", "", ""};

    public int gradation = 32;
    public boolean colorEnabled = false;
    public Vector3i color = new Vector3i(255, 255, 255);
    public float brightness = 1.0f;
    public boolean tintWithSky = false;
    public SkyBoxLayer.Blend blend = SkyBoxLayer.Blend.ALPHA;
    public Vector2f scroll = new Vector2f();
    public boolean animationEnabled = false;
    public int frames = 2;
    public int frametime = 2;
    public SkyBoxLayer.Hemisphere hemisphere = SkyBoxLayer.Hemisphere.FULL;
    public float horizonFade = 0.0f;
    public boolean fogFade = false;

    /** A {@link SkyBoxLayer.Preset} name, or {@link #CUSTOM_CURVE} for keyframes. */
    public String visible = "always";
    public int fade = 1500;
    /** {@code time:value} pairs, e.g. {@code 13000:0, 15000:0.8}. */
    public String keyframes = "0:1";
    public boolean rainEnabled = false;
    public float rainOpacity = 0.0f;
    public boolean thunderEnabled = false;
    public float thunderOpacity = 0.0f;
    /** Comma-separated phases 0-7; empty means every phase. */
    public String moonPhases = "";
    public boolean followStarBrightness = false;
    public boolean heightFadeEnabled = false;
    public Vector2f heightFade = new Vector2f(64, 256);

    public boolean conditionEnabled = false;
    public String conditionBiome = "";
    public String conditionBiomeTag = "";
    public boolean conditionHeightEnabled = false;
    public Vector2f conditionHeight = new Vector2f(0, 320);
    /** {@code any}, or a {@link SkyBoxLayer.Weather} name. */
    public String conditionWeather = "any";
    /** Comma-separated effect ids; the layer shows while the player has any of them. */
    public String conditionEffects = "";
    public int conditionFade = 20;

    public boolean rotationEnabled = false;
    public SkyRotation.Source source = SkyRotation.Source.SUN;
    public float speed = 1.0f;
    public float offset = 0.0f;
    /** One of {@link SkyRotation#AXIS_NAMES}, or {@code custom} for {@link #customAxis}. */
    public String axis = "sun";
    public Vector3f customAxis = new Vector3f(0, 0, 1);
    public Vector3f tilt = new Vector3f();
    public Vector3f orientation = new Vector3f();

    public SkyBoxLayer.Slot slot = SkyBoxLayer.Slot.SKY;
    public final EnumSet<SkyBoxLayer.HidePart> hide = EnumSet.noneOf(SkyBoxLayer.HidePart.class);

    public static EditableLayer from(SkyBoxLayer layer) {
        EditableLayer e = new EditableLayer();
        layer.texture().cubemap().ifPresentOrElse(cube -> {
            e.cubemap = true;
            List<Identifier> faces = cube.faces();
            for (int i = 0; i < faces.size(); i++) e.faces[i] = faces.get(i).toString();
        }, () -> e.texture = layer.texture().panorama().map(Identifier::toString).orElse(""));

        SkyBoxLayer.Look look = layer.look();
        e.gradation = look.gradation();
        look.color().ifPresent(c -> {
            e.colorEnabled = true;
            e.color = new Vector3i(c);
        });
        e.brightness = look.brightness();
        e.tintWithSky = look.tintWithSky();
        e.blend = look.blend();
        e.scroll = new Vector2f(look.scroll());
        look.animation().ifPresent(a -> {
            e.animationEnabled = true;
            e.frames = a.frames();
            e.frametime = a.frametime();
        });
        e.hemisphere = look.hemisphere();
        e.horizonFade = look.horizonFade();
        e.fogFade = look.fogFade();

        SkyBoxLayer.Visibility vis = layer.visibility();
        if (vis.opacity().isPresent()) {
            e.visible = CUSTOM_CURVE;
            e.keyframes = vis.opacity().get().stream()
                    .map(k -> k.time() + ":" + fmt(k.value()))
                    .collect(Collectors.joining(", "));
        } else {
            e.visible = vis.visible().orElse(SkyBoxLayer.Preset.ALWAYS).getSerializedName();
        }
        e.fade = vis.fade();
        vis.rainOpacity().ifPresent(v -> {
            e.rainEnabled = true;
            e.rainOpacity = v;
        });
        vis.thunderOpacity().ifPresent(v -> {
            e.thunderEnabled = true;
            e.thunderOpacity = v;
        });
        e.moonPhases = vis.moonPhases().map(l -> l.stream().map(String::valueOf).collect(Collectors.joining(", "))).orElse("");
        e.followStarBrightness = vis.followStarBrightness();
        vis.heightFade().ifPresent(h -> {
            e.heightFadeEnabled = true;
            e.heightFade = new Vector2f(h);
        });
        e.conditionFade = vis.conditionFade();
        vis.condition().ifPresent(c -> {
            e.conditionEnabled = true;
            e.conditionBiome = c.biome().map(k -> k.identifier().toString()).orElse("");
            e.conditionBiomeTag = c.biomes().map(t -> "#" + t.location()).orElse("");
            c.heightRange().ifPresent(h -> {
                e.conditionHeightEnabled = true;
                e.conditionHeight = new Vector2f(h.x, h.y);
            });
            e.conditionWeather = c.weather().map(SkyBoxLayer.Weather::getSerializedName).orElse("any");
            e.conditionEffects = c.effects().map(list -> list.stream()
                    .map(h -> h.unwrapKey().map(k -> k.identifier().toString()).orElse(""))
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(", "))).orElse("");
        });

        layer.rotation().ifPresent(r -> {
            e.rotationEnabled = true;
            e.source = r.source();
            e.speed = r.speed();
            e.offset = r.offset();
            r.axis().ifLeft(name -> e.axis = name).ifRight(vec -> {
                e.axis = "custom";
                e.customAxis = new Vector3f(vec);
            });
            e.tilt = new Vector3f(r.tilt());
            e.orientation = new Vector3f(r.orientation());
        });

        e.slot = layer.slot();
        e.hide.addAll(layer.hide());
        return e;
    }

    public EditableLayer copy() {
        // Round-tripping through the immutable record would drop half-typed (invalid) text, so copy field by field.
        EditableLayer c = new EditableLayer();
        c.cubemap = cubemap;
        c.texture = texture;
        System.arraycopy(faces, 0, c.faces, 0, faces.length);
        c.gradation = gradation;
        c.colorEnabled = colorEnabled;
        c.color = new Vector3i(color);
        c.brightness = brightness;
        c.tintWithSky = tintWithSky;
        c.blend = blend;
        c.scroll = new Vector2f(scroll);
        c.animationEnabled = animationEnabled;
        c.frames = frames;
        c.frametime = frametime;
        c.hemisphere = hemisphere;
        c.horizonFade = horizonFade;
        c.fogFade = fogFade;
        c.visible = visible;
        c.fade = fade;
        c.keyframes = keyframes;
        c.rainEnabled = rainEnabled;
        c.rainOpacity = rainOpacity;
        c.thunderEnabled = thunderEnabled;
        c.thunderOpacity = thunderOpacity;
        c.moonPhases = moonPhases;
        c.followStarBrightness = followStarBrightness;
        c.heightFadeEnabled = heightFadeEnabled;
        c.heightFade = new Vector2f(heightFade);
        c.conditionEnabled = conditionEnabled;
        c.conditionBiome = conditionBiome;
        c.conditionBiomeTag = conditionBiomeTag;
        c.conditionHeightEnabled = conditionHeightEnabled;
        c.conditionHeight = new Vector2f(conditionHeight);
        c.conditionWeather = conditionWeather;
        c.conditionEffects = conditionEffects;
        c.conditionFade = conditionFade;
        c.rotationEnabled = rotationEnabled;
        c.source = source;
        c.speed = speed;
        c.offset = offset;
        c.axis = axis;
        c.customAxis = new Vector3f(customAxis);
        c.tilt = new Vector3f(tilt);
        c.orientation = new Vector3f(orientation);
        c.slot = slot;
        c.hide.addAll(hide);
        return c;
    }

    /** Builds the immutable layer; throws {@link IllegalStateException} with a readable message on bad input. */
    public SkyBoxLayer toLayer() {
        SkyTexture tex;
        if (cubemap) {
            Identifier[] ids = new Identifier[6];
            for (int i = 0; i < 6; i++) ids[i] = parseId(faces[i], FACE_NAMES.get(i) + " face texture");
            tex = SkyTexture.cubemap(new SkyTexture.Cubemap(ids[0], ids[1], ids[2], ids[3], ids[4], ids[5]));
        } else {
            tex = SkyTexture.panorama(parseId(texture, "texture"));
        }

        SkyBoxLayer.Look look = new SkyBoxLayer.Look(
                Math.clamp(gradation, 4, 256),
                colorEnabled ? Optional.of(new Vector3i(color)) : Optional.empty(),
                brightness,
                tintWithSky,
                blend,
                new Vector2f(scroll),
                animationEnabled ? Optional.of(new SkyBoxLayer.Animation(Math.max(1, frames), Math.max(1, frametime))) : Optional.empty(),
                hemisphere,
                Math.clamp(horizonFade, 0.0f, 90.0f),
                fogFade);

        boolean custom = CUSTOM_CURVE.equals(visible);
        SkyBoxLayer.Visibility vis = new SkyBoxLayer.Visibility(
                custom ? Optional.empty() : Optional.of(parsePreset(visible)),
                Math.clamp(fade, 1, 12000),
                custom ? Optional.of(parseKeyframes(keyframes)) : Optional.empty(),
                rainEnabled ? Optional.of(Math.clamp(rainOpacity, 0.0f, 1.0f)) : Optional.empty(),
                thunderEnabled ? Optional.of(Math.clamp(thunderOpacity, 0.0f, 1.0f)) : Optional.empty(),
                parsePhases(moonPhases),
                followStarBrightness,
                heightFadeEnabled ? Optional.of(new Vector2f(heightFade)) : Optional.empty(),
                conditionEnabled ? Optional.of(buildCondition()) : Optional.empty(),
                Math.clamp(conditionFade, 0, 24000));

        Optional<SkyRotation> rotation = rotationEnabled
                ? Optional.of(new SkyRotation(source, speed, offset,
                        "custom".equals(axis) ? Either.right(new Vector3f(customAxis)) : Either.left(axis),
                        new Vector3f(tilt), new Vector3f(orientation)))
                : Optional.empty();

        // Keep the always-visible preset implicit so a plain layer exports without a "visible" field.
        if (!custom && "always".equals(visible)) {
            vis = new SkyBoxLayer.Visibility(Optional.empty(), vis.fade(), Optional.empty(), vis.rainOpacity(),
                    vis.thunderOpacity(), vis.moonPhases(), vis.followStarBrightness(), vis.heightFade(),
                    vis.condition(), vis.conditionFade());
        }
        return new SkyBoxLayer(tex, look, vis, rotation, slot, List.copyOf(hide), false);
    }

    private SkyBoxLayer.LayerCondition buildCondition() {
        Optional<ResourceKey<net.minecraft.world.level.biome.Biome>> biome = conditionBiome.isBlank()
                ? Optional.empty()
                : Optional.of(ResourceKey.create(Registries.BIOME, parseId(conditionBiome, "condition biome")));
        String tag = conditionBiomeTag.startsWith("#") ? conditionBiomeTag.substring(1) : conditionBiomeTag;
        Optional<TagKey<net.minecraft.world.level.biome.Biome>> biomes = tag.isBlank()
                ? Optional.empty()
                : Optional.of(TagKey.create(Registries.BIOME, parseId(tag, "condition biome tag")));
        Optional<Vec2> height = conditionHeightEnabled
                ? Optional.of(new Vec2(conditionHeight.x, conditionHeight.y))
                : Optional.empty();
        Optional<SkyBoxLayer.Weather> weather = "any".equals(conditionWeather)
                ? Optional.empty()
                : Optional.of(SkyBoxLayer.Weather.valueOf(conditionWeather.toUpperCase(Locale.ROOT)));

        Optional<List<Holder<MobEffect>>> effects = Optional.empty();
        if (!conditionEffects.isBlank()) {
            List<Holder<MobEffect>> list = new ArrayList<>();
            for (String part : conditionEffects.split(",")) {
                if (part.isBlank()) continue;
                Identifier id = parseId(part.trim(), "effect");
                Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.get(id)
                        .orElseThrow(() -> new IllegalStateException("Unknown effect '" + id + "'"));
                list.add(effect);
            }
            effects = Optional.of(List.copyOf(list));
        }
        return new SkyBoxLayer.LayerCondition(biomes, biome, height, weather, effects);
    }

    private static SkyBoxLayer.Preset parsePreset(String name) {
        for (SkyBoxLayer.Preset preset : SkyBoxLayer.Preset.values()) {
            if (preset.getSerializedName().equals(name)) return preset;
        }
        throw new IllegalStateException("Unknown visibility preset '" + name + "'");
    }

    static List<SkyBoxLayer.Keyframe> parseKeyframes(String text) {
        List<SkyBoxLayer.Keyframe> keys = new ArrayList<>();
        for (String part : text.split(",")) {
            if (part.isBlank()) continue;
            String[] tv = part.split(":");
            if (tv.length != 2) throw new IllegalStateException("Keyframe '" + part.trim() + "' should be time:value");
            try {
                int time = Integer.parseInt(tv[0].trim());
                float value = Float.parseFloat(tv[1].trim());
                if (time < 0 || time > 24000 || value < 0 || value > 1) {
                    throw new IllegalStateException("Keyframe '" + part.trim() + "' is out of range (time 0-24000, value 0-1)");
                }
                keys.add(new SkyBoxLayer.Keyframe(time, value));
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Keyframe '" + part.trim() + "' is not a number pair");
            }
        }
        if (keys.isEmpty()) throw new IllegalStateException("Custom opacity needs at least one time:value keyframe");
        return List.copyOf(keys);
    }

    private static Optional<List<Integer>> parsePhases(String text) {
        if (text.isBlank()) return Optional.empty();
        List<Integer> phases = new ArrayList<>();
        for (String part : text.split(",")) {
            if (part.isBlank()) continue;
            try {
                int phase = Integer.parseInt(part.trim());
                if (phase < 0 || phase > 7) throw new IllegalStateException("Moon phase " + phase + " is not between 0 and 7");
                phases.add(phase);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Moon phase '" + part.trim() + "' is not a number");
            }
        }
        return phases.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(phases));
    }

    private static Identifier parseId(String text, String what) {
        Identifier id = text == null || text.isBlank() ? null : Identifier.tryParse(text.trim());
        if (id == null) throw new IllegalStateException("Invalid " + what + " '" + text + "'");
        return id;
    }

    private static String fmt(float f) {
        if (f == Math.rint(f)) return String.valueOf((long) f);
        return String.valueOf(f);
    }
}
