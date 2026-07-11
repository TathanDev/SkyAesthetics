package fr.tathan.sky_aesthetics.client.screens.editor.model;

import fr.tathan.sky_aesthetics.client.settings.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A mutable, editor-friendly mirror of {@link SkyProperties}.
 *
 * <p>{@link SkyProperties} (and all of its sub-settings) are immutable records, which makes them
 * awkward to bind to mutable UI widgets. This class holds plain mutable fields, with a per-section
 * {@code enabled} flag standing in for each {@code Optional<…>} sub-setting. Use
 * {@link #fromProperties(SkyProperties)} to load an existing sky and {@link #toProperties()} to
 * rebuild the immutable record (for serialization or live preview).</p>
 *
 * <p>Sections that the editor does not yet expose ({@code renderCondition},
 * {@code environmentAttributes}) are preserved verbatim so importing then exporting a sky is
 * loss-less.</p>
 */
public class EditableSky {
    public String skyId = "sky_aesthetics:custom";
    public String dimension = "minecraft:overworld";
    public boolean weather = true;

    public final Cloud cloud = new Cloud();
    public final Sun sun = new Sun();
    public final Moon moon = new Moon();
    public final Stars stars = new Stars();
    public final SkyColor skyColor = new SkyColor();
    public final SkyBox skyBox = new SkyBox();
    public final Light light = new Light();
    public final Fog fog = new Fog();
    public final List<Obj> skyObjects = new ArrayList<>();

    public Optional<SkyProperties.RenderCondition> renderCondition = Optional.empty();
    public Optional<EnvironmentAttributeMap> environmentAttributes = Optional.empty();


    public static class Cloud {
        public boolean enabled = false;
        public boolean showCloud = true;
        public int cloudHeight = 192;
        public boolean colorEnabled = false;
        public Vector3i color = new Vector3i(255, 255, 255);
    }

    public static class Sun {
        public boolean enabled = false;
        public boolean show = true;
        public boolean textureEnabled = false;
        public String texture = "";
        public float size = 30.0f;
        public float intensity = 1.0f;
    }

    public static class Moon {
        public boolean enabled = false;
        public boolean show = true;
        public boolean textureEnabled = false;
        public String texture = "";
        public float size = 20.0f;
        public boolean showPhases = true;
        public float intensity = 1.0f;
    }

    public static class Stars {
        public boolean enabled = false;
        public boolean vanilla = true;
        public boolean movingStars = false;
        public int count = 15000;
        public boolean allDaysVisible = false;
        public float scale = 0.15f;
        public Vector3i color = new Vector3i(255, 255, 255);

        public boolean shootingEnabled = false;
        public int shootingPercentage = 2;
        public Vector2f shootingLifetime = new Vector2f(60, 120);
        public float shootingScale = 1.2f;
        public float shootingSpeed = 1.5f;
        public Vector3f shootingColor = new Vector3f(1.0f, 1.0f, 0.78f); // normalised 0-1
        public boolean shootingRotationEnabled = false;
        public int shootingRotation = 0;
    }

    public static class SkyColor {
        public boolean enabled = false;
        public boolean skyColorEnabled = false;
        public Vector4f skyColor = new Vector4f(0.5f, 0.7f, 1.0f, 1.0f); // normalised 0-1
        public boolean sunsetColorEnabled = false;
        public Vector3i sunsetColor = new Vector3i(255, 100, 0);
        public boolean alphaModifierEnabled = false;
        public int alphaModifier = 1;
    }

    public static class SkyBox {
        public boolean enabled = false;
        public int gradation = 8;
        public String texture = "";
        public Vector3f rotation = new Vector3f();
        public boolean dynamicEnabled = false;
        public Rotation.Axis dynamicAxis = Rotation.Axis.XP;
        public String dynamicRotationType = "NIGHT"; // DAY / NIGHT / STATIC
    }

    public static class Light {
        public boolean enabled = false;
        public boolean forceBrightLightmap = false;
        public boolean constantAmbientLight = false;
    }

    public static class Fog {
        public boolean enabled = false;
        public boolean fog = true;
        public boolean colorEnabled = false;
        public Vector3i color = new Vector3i(180, 200, 220);
        public boolean densityEnabled = false;
        public Vector2f density = new Vector2f(0, 192);
    }

    /** Mutable mirror of {@link SkyObject}. */
    public static class Obj {
        public String texture = "";
        public boolean blend = false;
        public float size = 2.0f;
        public Vector3f rotation = new Vector3f();
        public Vector3f objectRotation = new Vector3f();
        public int height = 0;
        public String rotationType = "STATIC"; // DAY / NIGHT / STATIC
    }


    public static EditableSky fromProperties(SkyProperties props) {
        EditableSky e = new EditableSky();
        e.skyId = props.id().toString();
        e.dimension = props.world().identifier().toString();
        e.weather = props.weather();

        props.cloudSettings().ifPresent(cs -> {
            e.cloud.enabled = true;
            e.cloud.showCloud = cs.showCloud();
            e.cloud.cloudHeight = cs.cloudHeight();
            cs.cloudColor().ifPresent(c -> {
                e.cloud.colorEnabled = true;
                e.cloud.color = new Vector3i(c);
            });
        });

        props.sun().ifPresent(s -> {
            e.sun.enabled = true;
            e.sun.show = s.show();
            s.sunTexture().ifPresent(t -> {
                e.sun.textureEnabled = true;
                e.sun.texture = t.toString();
            });
            e.sun.size = s.size();
            e.sun.intensity = s.intensity();
        });

        props.moon().ifPresent(m -> {
            e.moon.enabled = true;
            e.moon.show = m.show();
            m.moonTexture().ifPresent(t -> {
                e.moon.textureEnabled = true;
                e.moon.texture = t.toString();
            });
            e.moon.size = m.size();
            e.moon.showPhases = m.showPhases();
            e.moon.intensity = m.intensity();
        });

        props.stars().ifPresent(s -> {
            e.stars.enabled = true;
            e.stars.vanilla = s.vanilla();
            e.stars.movingStars = s.movingStars();
            e.stars.count = s.count();
            e.stars.allDaysVisible = s.allDaysVisible();
            e.stars.scale = s.scale();
            e.stars.color = new Vector3i(s.color());
            s.shootingStars().ifPresent(ss -> {
                e.stars.shootingEnabled = true;
                e.stars.shootingPercentage = ss.percentage();
                e.stars.shootingLifetime = new Vector2f(ss.randomLifetime().x, ss.randomLifetime().y);
                e.stars.shootingScale = ss.scale();
                e.stars.shootingSpeed = ss.speed();
                e.stars.shootingColor = new Vector3f((float) ss.color().x, (float) ss.color().y, (float) ss.color().z);
                ss.rotation().ifPresent(r -> {
                    e.stars.shootingRotationEnabled = true;
                    e.stars.shootingRotation = r;
                });
            });
        });

        props.skyColorSettings().ifPresent(sc -> {
            e.skyColor.enabled = true;
            sc.color().ifPresent(c -> {
                e.skyColor.skyColorEnabled = true;
                e.skyColor.skyColor = new Vector4f(c);
            });
            sc.sunsetColor().ifPresent(c -> {
                e.skyColor.sunsetColorEnabled = true;
                e.skyColor.sunsetColor = new Vector3i(c);
            });
            sc.sunriseAlphaModifier().ifPresent(a -> {
                e.skyColor.alphaModifierEnabled = true;
                e.skyColor.alphaModifier = a;
            });
        });

        for (SkyObject o : props.skyObjects()) {
            Obj obj = new Obj();
            obj.texture = o.texture().toString();
            obj.blend = o.blend();
            obj.size = o.size();
            obj.rotation = new Vector3f(o.rotation());
            obj.objectRotation = new Vector3f(o.objectRotation());
            obj.height = o.height();
            obj.rotationType = o.rotationType();
            e.skyObjects.add(obj);
        }

        props.skyBoxSetting().ifPresent(sb -> {
            e.skyBox.enabled = true;
            e.skyBox.gradation = sb.gradation();
            e.skyBox.texture = sb.texture().toString();
            e.skyBox.rotation = new Vector3f(sb.rotation());
            sb.dynamicRotation().ifPresent(r -> {
                e.skyBox.dynamicEnabled = true;
                e.skyBox.dynamicAxis = r.axis();
                e.skyBox.dynamicRotationType = r.rotationType();
            });
        });

        props.lightSettings().ifPresent(l -> {
            e.light.enabled = true;
            e.light.forceBrightLightmap = l.forceBrightLightmap();
            e.light.constantAmbientLight = l.constantAmbientLight();
        });

        props.fogSettings().ifPresent(f -> {
            e.fog.enabled = true;
            e.fog.fog = f.fog();
            f.customFogColor().ifPresent(c -> {
                e.fog.colorEnabled = true;
                e.fog.color = new Vector3i(c);
            });
            f.fogDensity().ifPresent(d -> {
                e.fog.densityEnabled = true;
                e.fog.density = new Vector2f(d);
            });
        });

        e.renderCondition = props.renderCondition();
        e.environmentAttributes = props.environmentAttributes();
        return e;
    }


    public EditableSky copy() {
        EditableSky e = new EditableSky();
        e.skyId = this.skyId;
        e.dimension = this.dimension;
        e.weather = this.weather;

        e.cloud.enabled = cloud.enabled;
        e.cloud.showCloud = cloud.showCloud;
        e.cloud.cloudHeight = cloud.cloudHeight;
        e.cloud.colorEnabled = cloud.colorEnabled;
        e.cloud.color = new Vector3i(cloud.color);

        e.sun.enabled = sun.enabled;
        e.sun.show = sun.show;
        e.sun.textureEnabled = sun.textureEnabled;
        e.sun.texture = sun.texture;
        e.sun.size = sun.size;
        e.sun.intensity = sun.intensity;

        e.moon.enabled = moon.enabled;
        e.moon.show = moon.show;
        e.moon.textureEnabled = moon.textureEnabled;
        e.moon.texture = moon.texture;
        e.moon.size = moon.size;
        e.moon.showPhases = moon.showPhases;
        e.moon.intensity = moon.intensity;

        e.stars.enabled = stars.enabled;
        e.stars.vanilla = stars.vanilla;
        e.stars.movingStars = stars.movingStars;
        e.stars.count = stars.count;
        e.stars.allDaysVisible = stars.allDaysVisible;
        e.stars.scale = stars.scale;
        e.stars.color = new Vector3i(stars.color);
        e.stars.shootingEnabled = stars.shootingEnabled;
        e.stars.shootingPercentage = stars.shootingPercentage;
        e.stars.shootingLifetime = new Vector2f(stars.shootingLifetime);
        e.stars.shootingScale = stars.shootingScale;
        e.stars.shootingSpeed = stars.shootingSpeed;
        e.stars.shootingColor = new Vector3f(stars.shootingColor);
        e.stars.shootingRotationEnabled = stars.shootingRotationEnabled;
        e.stars.shootingRotation = stars.shootingRotation;

        e.skyColor.enabled = skyColor.enabled;
        e.skyColor.skyColorEnabled = skyColor.skyColorEnabled;
        e.skyColor.skyColor = new Vector4f(skyColor.skyColor);
        e.skyColor.sunsetColorEnabled = skyColor.sunsetColorEnabled;
        e.skyColor.sunsetColor = new Vector3i(skyColor.sunsetColor);
        e.skyColor.alphaModifierEnabled = skyColor.alphaModifierEnabled;
        e.skyColor.alphaModifier = skyColor.alphaModifier;

        e.skyBox.enabled = skyBox.enabled;
        e.skyBox.gradation = skyBox.gradation;
        e.skyBox.texture = skyBox.texture;
        e.skyBox.rotation = new Vector3f(skyBox.rotation);
        e.skyBox.dynamicEnabled = skyBox.dynamicEnabled;
        e.skyBox.dynamicAxis = skyBox.dynamicAxis;
        e.skyBox.dynamicRotationType = skyBox.dynamicRotationType;

        e.light.enabled = light.enabled;
        e.light.forceBrightLightmap = light.forceBrightLightmap;
        e.light.constantAmbientLight = light.constantAmbientLight;

        e.fog.enabled = fog.enabled;
        e.fog.fog = fog.fog;
        e.fog.colorEnabled = fog.colorEnabled;
        e.fog.color = new Vector3i(fog.color);
        e.fog.densityEnabled = fog.densityEnabled;
        e.fog.density = new Vector2f(fog.density);

        for (Obj o : skyObjects) {
            Obj c = new Obj();
            c.texture = o.texture;
            c.blend = o.blend;
            c.size = o.size;
            c.rotation = new Vector3f(o.rotation);
            c.objectRotation = new Vector3f(o.objectRotation);
            c.height = o.height;
            c.rotationType = o.rotationType;
            e.skyObjects.add(c);
        }

        e.renderCondition = this.renderCondition;
        e.environmentAttributes = this.environmentAttributes;
        return e;
    }

    public SkyProperties toProperties() {
        Identifier worldId = Identifier.tryParse(dimension == null ? "" : dimension);
        if (worldId == null) throw new IllegalStateException("Invalid dimension: '" + dimension + "'");
        ResourceKey<Level> world = ResourceKey.create(Registries.DIMENSION, worldId);

        Identifier id = Identifier.tryParse(skyId == null ? "" : skyId);
        if (id == null) throw new IllegalStateException("Invalid sky id: '" + skyId + "'");

        Optional<CloudSettings> cloudOpt = cloud.enabled
                ? Optional.of(new CloudSettings(cloud.showCloud, cloud.cloudHeight,
                        cloud.colorEnabled ? Optional.of(new Vector3i(cloud.color)) : Optional.empty()))
                : Optional.empty();

        Optional<CustomVanillaObject.Sun> sunOpt = sun.enabled
                ? Optional.of(new CustomVanillaObject.Sun(sun.show, optTexture(sun.textureEnabled, sun.texture), sun.size, sun.intensity))
                : Optional.empty();

        Optional<CustomVanillaObject.Moon> moonOpt = moon.enabled
                ? Optional.of(new CustomVanillaObject.Moon(moon.show, optTexture(moon.textureEnabled, moon.texture), moon.size, moon.showPhases, moon.intensity))
                : Optional.empty();

        Optional<StarSettings> starsOpt = stars.enabled
                ? Optional.of(new StarSettings(stars.vanilla, stars.movingStars, stars.count, stars.allDaysVisible,
                        stars.scale, new Vector3i(stars.color), buildShootingStars()))
                : Optional.empty();

        Optional<SkyColorSettings> colorOpt = skyColor.enabled
                ? Optional.of(new SkyColorSettings(
                        skyColor.skyColorEnabled ? Optional.of(new Vector4f(skyColor.skyColor)) : Optional.empty(),
                        skyColor.sunsetColorEnabled ? Optional.of(new Vector3i(skyColor.sunsetColor)) : Optional.empty(),
                        skyColor.alphaModifierEnabled ? Optional.of(skyColor.alphaModifier) : Optional.empty()))
                : Optional.empty();

        List<SkyObject> objects = new ArrayList<>();
        for (Obj o : skyObjects) {
            if (o.texture == null || o.texture.isBlank()) continue;
            Identifier texId = Identifier.tryParse(o.texture);
            if (texId == null) continue;
            objects.add(new SkyObject(texId, o.blend, o.size,
                    new Vector3f(o.rotation), new Vector3f(o.objectRotation), o.height, o.rotationType));
        }

        Identifier skyBoxTexId = (skyBox.enabled && skyBox.texture != null) ? Identifier.tryParse(skyBox.texture) : null;
        Optional<SkyBoxSetting> skyBoxOpt = skyBoxTexId != null
                ? Optional.of(new SkyBoxSetting(skyBox.gradation, skyBoxTexId, new Vector3f(skyBox.rotation),
                        skyBox.dynamicEnabled ? Optional.of(new Rotation(skyBox.dynamicAxis, skyBox.dynamicRotationType)) : Optional.empty()))
                : Optional.empty();

        Optional<LightSettings> lightOpt = light.enabled
                ? Optional.of(new LightSettings(light.forceBrightLightmap, light.constantAmbientLight))
                : Optional.empty();

        Optional<FogSettings> fogOpt = fog.enabled
                ? Optional.of(new FogSettings(fog.fog,
                        fog.colorEnabled ? Optional.of(new Vector3i(fog.color)) : Optional.empty(),
                        fog.densityEnabled ? Optional.of(new Vector2f(fog.density)) : Optional.empty()))
                : Optional.empty();

        return new SkyProperties(world, id, cloudOpt, weather, sunOpt, moonOpt, starsOpt, colorOpt,
                objects, renderCondition, environmentAttributes, skyBoxOpt, lightOpt, fogOpt);
    }

    private Optional<StarSettings.ShootingStars> buildShootingStars() {
        if (!stars.shootingEnabled) return Optional.empty();
        return Optional.of(new StarSettings.ShootingStars(
                stars.shootingPercentage,
                new Vec2(stars.shootingLifetime.x, stars.shootingLifetime.y),
                stars.shootingScale,
                stars.shootingSpeed,
                new Vec3(stars.shootingColor.x, stars.shootingColor.y, stars.shootingColor.z),
                stars.shootingRotationEnabled ? Optional.of(stars.shootingRotation) : Optional.empty()));
    }

    private static Optional<Identifier> optTexture(boolean enabled, String value) {
        if (!enabled || value == null || value.isBlank()) return Optional.empty();
        Identifier id = Identifier.tryParse(value);
        return id != null ? Optional.of(id) : Optional.empty();
    }

    public String displayName() {
        if (dimension != null && !dimension.isBlank()) return dimension;
        if (skyId != null && !skyId.isBlank()) return skyId;
        return "New Sky";
    }
}
