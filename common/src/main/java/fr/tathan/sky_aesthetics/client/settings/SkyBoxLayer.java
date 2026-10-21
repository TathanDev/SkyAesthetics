package fr.tathan.sky_aesthetics.client.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.tathan.sky_aesthetics.client.SkyClock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector2f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The class containing information about a skybox layer
 * @param texture The panorama or cubemap of the layer
 * @param look How the layer is drawn
 * @param visibility How visible the layer is over the day
 * @param rotation How the layer turns over time
 * @param slot Where the layer is drawn in the sky
 * @param hide The vanilla parts hidden while the layer is visible
 * @param legacy If the layer comes from the old sky_box field, it keeps the old sphere so old packs look the same
 */
public record SkyBoxLayer(SkyTexture texture, Look look, Visibility visibility, Optional<SkyRotation> rotation, Slot slot, List<HidePart> hide, boolean legacy) {

    public static final Codec<SkyBoxLayer> CODEC = RecordCodecBuilder.<SkyBoxLayer>create(instance -> instance.group(
            SkyTexture.CODEC.fieldOf("texture").forGetter(SkyBoxLayer::texture),
            Look.MAP_CODEC.forGetter(SkyBoxLayer::look),
            Visibility.MAP_CODEC.forGetter(SkyBoxLayer::visibility),
            SkyRotation.CODEC.optionalFieldOf("rotation").forGetter(SkyBoxLayer::rotation),
            Slot.CODEC.optionalFieldOf("slot", Slot.SKY).forGetter(SkyBoxLayer::slot),
            HidePart.CODEC.listOf().optionalFieldOf("hide", List.of()).forGetter(SkyBoxLayer::hide)
    ).apply(instance, (texture, look, visibility, rotation, slot, hide) -> new SkyBoxLayer(texture, look, visibility, rotation, slot, hide, false))).validate(SkyBoxLayer::validate);

    private static DataResult<SkyBoxLayer> validate(SkyBoxLayer layer) {
        if (layer.visibility().visible().isPresent() && layer.visibility().opacity().isPresent()) {
            return DataResult.error(() -> "A skybox layer can use 'visible' or 'opacity', not both");
        }
        if (layer.visibility().opacity().map(List::isEmpty).orElse(false)) {
            return DataResult.error(() -> "'opacity' needs at least one keyframe");
        }
        if (layer.look().animation().map(animation -> animation.frames() < 1 || animation.frametime() < 1).orElse(false)) {
            return DataResult.error(() -> "'animation' needs frames >= 1 and frametime >= 1");
        }
        return DataResult.success(layer);
    }

    /** Hides the vanilla sky disc, which is drawn after the layer and would cover the upper half of the sky */
    public static SkyBoxLayer fromLegacy(SkyBoxSetting setting) {
        Look look = new Look(setting.gradation(), Optional.empty(), 1.0f, false, Blend.OPAQUE, new Vector2f(), Optional.empty(), Hemisphere.FULL, 0.0f, false);
        Optional<SkyRotation> rotation = Optional.of(SkyRotation.fromLegacy(setting.rotation(), setting.dynamicRotation()));

        return new SkyBoxLayer(SkyTexture.panorama(setting.texture()), look, Visibility.createDefaultSettings(), rotation, Slot.BACKGROUND, List.of(HidePart.SKY_DISC), true);
    }

    public boolean hides(HidePart part) {
        return this.hide.contains(part);
    }

    private static float smoothstep(float x) {
        x = Mth.clamp(x, 0.0f, 1.0f);
        return x * x * (3.0f - 2.0f * x);
    }

    /**
     * How the layer is drawn
     * @param gradation The quality of the sphere for panoramas
     * @param color The RGB color of the layer (0-255 per channel)
     * @param brightness Multiplier of the color
     * @param tintWithSky If the layer is multiplied by the vanilla sky color
     * @param blend How the layer is mixed with the sky behind it
     * @param scroll UV units per tick, only for standalone panoramas
     * @param animation A vertical strip of frames, only for standalone textures
     * @param hemisphere UPPER hides everything below the horizon
     * @param horizonFade With UPPER, the height in degrees where the layer fades out near the horizon
     * @param fogFade If the layer fades into the sky fog like the vanilla sky
     */
    public record Look(int gradation, Optional<Vector3i> color, float brightness, boolean tintWithSky, Blend blend, Vector2f scroll, Optional<Animation> animation, Hemisphere hemisphere, float horizonFade, boolean fogFade) {

        public static final MapCodec<Look> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.intRange(4, 256).optionalFieldOf("gradation", 32).forGetter(Look::gradation),
                SkyColorSettings.VEC3I.optionalFieldOf("color").forGetter(Look::color),
                Codec.FLOAT.optionalFieldOf("brightness", 1.0f).forGetter(Look::brightness),
                Codec.BOOL.optionalFieldOf("tint_with_sky", false).forGetter(Look::tintWithSky),
                Blend.CODEC.optionalFieldOf("blend", Blend.ALPHA).forGetter(Look::blend),
                FogSettings.VEC2F.optionalFieldOf("scroll", new Vector2f()).forGetter(Look::scroll),
                Animation.CODEC.optionalFieldOf("animation").forGetter(Look::animation),
                Hemisphere.CODEC.optionalFieldOf("hemisphere", Hemisphere.FULL).forGetter(Look::hemisphere),
                Codec.floatRange(0.0f, 90.0f).optionalFieldOf("horizon_fade", 0.0f).forGetter(Look::horizonFade),
                Codec.BOOL.optionalFieldOf("fog_fade", false).forGetter(Look::fogFade)
        ).apply(instance, Look::new));
    }

    /**
     * @param frames The number of frames, from top to bottom
     * @param frametime The number of ticks per frame
     */
    public record Animation(int frames, int frametime) {

        public static final Codec<Animation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("frames").forGetter(Animation::frames),
                Codec.INT.optionalFieldOf("frametime", 1).forGetter(Animation::frametime)
        ).apply(instance, Animation::new));
    }

    /**
     * How visible the layer is, every setting is multiplied together
     * @param visible A preset of the day cycle
     * @param fade The length in ticks of the preset transitions
     * @param opacity Custom keyframes over the day, used instead of visible
     * @param rainOpacity The opacity when it fully rains
     * @param thunderOpacity The opacity during a full thunderstorm
     * @param moonPhases The moon phases where the layer is visible (0 is the full moon)
     * @param followStarBrightness If the layer fades like the vanilla stars
     * @param heightFade The player heights where the layer fades in (reversed to fade out)
     * @param condition The layer is only visible when the condition is true
     * @param conditionFade The ticks to fade in or out when the condition changes
     */
    public record Visibility(Optional<Preset> visible, int fade, Optional<List<Keyframe>> opacity, Optional<Float> rainOpacity, Optional<Float> thunderOpacity, Optional<List<Integer>> moonPhases, boolean followStarBrightness, Optional<Vector2f> heightFade, Optional<LayerCondition> condition, int conditionFade) {

        public static final MapCodec<Visibility> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Preset.CODEC.optionalFieldOf("visible").forGetter(Visibility::visible),
                Codec.intRange(1, 12000).optionalFieldOf("fade", 1500).forGetter(Visibility::fade),
                Keyframe.CODEC.listOf().optionalFieldOf("opacity").forGetter(Visibility::opacity),
                Codec.floatRange(0.0f, 1.0f).optionalFieldOf("rain_opacity").forGetter(Visibility::rainOpacity),
                Codec.floatRange(0.0f, 1.0f).optionalFieldOf("thunder_opacity").forGetter(Visibility::thunderOpacity),
                Codec.intRange(0, 7).listOf().optionalFieldOf("moon_phases").forGetter(Visibility::moonPhases),
                Codec.BOOL.optionalFieldOf("follow_star_brightness", false).forGetter(Visibility::followStarBrightness),
                FogSettings.VEC2F.optionalFieldOf("height_fade").forGetter(Visibility::heightFade),
                LayerCondition.CODEC.optionalFieldOf("condition").forGetter(Visibility::condition),
                Codec.intRange(0, 24000).optionalFieldOf("condition_fade", 20).forGetter(Visibility::conditionFade)
        ).apply(instance, Visibility::new));

        public static Visibility createDefaultSettings() {
            return new Visibility(Optional.empty(), 1500, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), false, Optional.empty(), Optional.empty(), 20);
        }

        /** The opacity from every setting except the condition, which is faded over time by the renderer */
        public float getOpacity(SkyClock clock) {
            float value = this.opacity.map(keyframes -> Keyframe.evaluate(keyframes, clock.dayTime()))
                    .orElseGet(() -> this.visible.orElse(Preset.ALWAYS).evaluate(clock.dayTime(), this.fade));

            if (this.rainOpacity.isPresent()) {
                value *= Mth.lerp(clock.rainLevel(), 1.0f, this.rainOpacity.get());
            }
            if (this.thunderOpacity.isPresent()) {
                value *= Mth.lerp(clock.thunderLevel(), 1.0f, this.thunderOpacity.get());
            }
            if (this.followStarBrightness) {
                value *= Mth.clamp(clock.starBrightness() * 2.0f, 0.0f, 1.0f);
            }
            if (this.heightFade.isPresent()) {
                Vector2f heightFade = this.heightFade.get();
                float low = Math.min(heightFade.x, heightFade.y);
                float high = Math.max(heightFade.x, heightFade.y);
                float fade = high > low ? smoothstep((float) ((clock.playerY() - low) / (high - low))) : (clock.playerY() >= low ? 1 : 0);
                value *= heightFade.x <= heightFade.y ? fade : 1.0f - fade;
            }
            if (this.moonPhases.isPresent() && !this.moonPhases.get().contains(clock.moonPhase())) {
                value = 0.0f;
            }
            return Mth.clamp(value, 0.0f, 1.0f);
        }
    }

    /**
     * A point of a custom opacity curve
     * @param time The time in ticks (0-24000)
     * @param value The opacity at this time
     */
    public record Keyframe(int time, float value) {

        public static final Codec<Keyframe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 24000).fieldOf("time").forGetter(Keyframe::time),
                Codec.floatRange(0.0f, 1.0f).fieldOf("value").forGetter(Keyframe::value)
        ).apply(instance, Keyframe::new));

        /** Linear interpolation between the keyframes, the last one goes back to the first one after midnight */
        public static float evaluate(List<Keyframe> keyframes, double time) {
            if (keyframes.size() == 1) return keyframes.getFirst().value();

            List<Keyframe> sorted = new ArrayList<>(keyframes);
            sorted.sort(Comparator.comparingInt(Keyframe::time));

            Keyframe before = sorted.getLast();
            double beforeTime = before.time() - SkyClock.DAY_LENGTH;
            for (Keyframe keyframe : sorted) {
                if (keyframe.time() >= time) {
                    return interpolate(before, beforeTime, keyframe, keyframe.time(), time);
                }
                before = keyframe;
                beforeTime = keyframe.time();
            }

            Keyframe first = sorted.getFirst();
            return interpolate(before, beforeTime, first, first.time() + SkyClock.DAY_LENGTH, time);
        }

        private static float interpolate(Keyframe from, double fromTime, Keyframe to, double toTime, double time) {
            double span = toTime - fromTime;
            float delta = span <= 0 ? 1.0f : (float) ((time - fromTime) / span);
            return Mth.lerp(delta, from.value(), to.value());
        }
    }

    /**
     * The layer is visible only when every setting is true
     * @param biomes A biome tag
     * @param biome A biome
     * @param heightRange The min and max height of the player
     * @param weather The weather
     * @param effects The layer is visible when the player has one of these effects
     */
    public record LayerCondition(Optional<TagKey<Biome>> biomes, Optional<ResourceKey<Biome>> biome, Optional<Vec2> heightRange, Optional<Weather> weather, Optional<List<Holder<MobEffect>>> effects) {

        public static final Codec<LayerCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TagKey.codec(Registries.BIOME).optionalFieldOf("biomes").forGetter(LayerCondition::biomes),
                ResourceKey.codec(Registries.BIOME).optionalFieldOf("biome").forGetter(LayerCondition::biome),
                SkyProperties.RenderCondition.VEC2.optionalFieldOf("height_range").forGetter(LayerCondition::heightRange),
                Weather.CODEC.optionalFieldOf("weather").forGetter(LayerCondition::weather),
                MobEffect.CODEC.listOf().optionalFieldOf("effects").forGetter(LayerCondition::effects)
        ).apply(instance, LayerCondition::new));

        public boolean isVisible(SkyClock clock) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            Level level = minecraft.level;

            if (player == null || level == null) return false;

            if (this.biomes.isPresent() && !level.getBiome(player.getOnPos()).is(this.biomes.get())) {
                return false;
            }
            if (this.biome.isPresent() && !level.getBiome(player.getOnPos()).is(this.biome.get())) {
                return false;
            }
            if (this.heightRange.isPresent()) {
                double playerHeight = player.position().y;
                if (playerHeight < this.heightRange.get().x || playerHeight > this.heightRange.get().y) {
                    return false;
                }
            }
            if (this.weather.isPresent() && !this.weather.get().isActive(clock)) {
                return false;
            }
            if (this.effects.isPresent()) {
                return this.effects.get().stream().anyMatch(player::hasEffect);
            }

            return true;
        }
    }

    /** Presets of the day cycle, the day is from 0 to 12000 ticks */
    public enum Preset implements StringRepresentable {
        ALWAYS("always"),
        DAY("day"),
        NIGHT("night"),
        SUNRISE("sunrise"),
        SUNSET("sunset");


        public final String name;

        Preset(String name) {
            this.name = name;
        }

        public static final Codec<Preset> CODEC = StringRepresentable.fromEnum(Preset::values);

        public float evaluate(double time, int fade) {
            double length = Math.max(1, fade);
            return switch (this) {
                case ALWAYS -> 1.0f;
                case DAY -> day(time, length);
                case NIGHT -> 1.0f - day(time, length);
                case SUNSET -> peak(time, 12000.0 + length / 2.0, length / 2.0);
                case SUNRISE -> peak(time, SkyClock.DAY_LENGTH - length / 2.0, length / 2.0);
            };
        }

        // Visible until 12000, fades out after it and fades back in before 24000
        private static float day(double time, double fade) {
            if (time <= 12000.0) return 1.0f;
            if (time <= 12000.0 + fade) return 1.0f - smoothstep((float) ((time - 12000.0) / fade));
            if (time < SkyClock.DAY_LENGTH - fade) return 0.0f;
            return smoothstep((float) ((time - (SkyClock.DAY_LENGTH - fade)) / fade));
        }

        private static float peak(double time, double center, double halfWidth) {
            double distance = Math.abs(time - center);
            distance = Math.min(distance, SkyClock.DAY_LENGTH - distance);
            return 1.0f - smoothstep((float) (distance / halfWidth));
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public enum Weather implements StringRepresentable {
        CLEAR("clear"),
        RAIN("rain"),
        THUNDER("thunder");


        public final String name;

        Weather(String name) {
            this.name = name;
        }

        public static final Codec<Weather> CODEC = StringRepresentable.fromEnum(Weather::values);

        public boolean isActive(SkyClock clock) {
            boolean rain = clock.rainLevel() > 0.5f;
            boolean thunder = clock.thunderLevel() > 0.5f;
            return switch (this) {
                case CLEAR -> !rain;
                case RAIN -> rain && !thunder;
                case THUNDER -> thunder;
            };
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public enum Blend implements StringRepresentable {
        OPAQUE("opaque"),
        ALPHA("alpha"),
        ADD("add"),
        MULTIPLY("multiply"),
        SCREEN("screen");


        public final String name;

        Blend(String name) {
            this.name = name;
        }

        public static final Codec<Blend> CODEC = StringRepresentable.fromEnum(Blend::values);

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public enum Hemisphere implements StringRepresentable {
        FULL("full"),
        UPPER("upper");


        public final String name;

        Hemisphere(String name) {
            this.name = name;
        }

        public static final Codec<Hemisphere> CODEC = StringRepresentable.fromEnum(Hemisphere::values);

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    /**
     * Where the layer is drawn in the sky
     * BACKGROUND: before the vanilla sky color
     * SKY: after the sky color, before the sunrise
     * OVER_STARS: after the stars, before the sun, moon and sky objects
     * FOREGROUND: after everything
     */
    public enum Slot implements StringRepresentable {
        BACKGROUND("background"),
        SKY("sky"),
        OVER_STARS("over_stars"),
        FOREGROUND("foreground");


        public final String name;

        Slot(String name) {
            this.name = name;
        }

        public static final Codec<Slot> CODEC = StringRepresentable.fromEnum(Slot::values);

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public enum HidePart implements StringRepresentable {
        SKY_DISC("sky_disc"),
        SUNRISE("sunrise"),
        STARS("stars"),
        SUN("sun"),
        MOON("moon"),
        SKY_OBJECTS("sky_objects");


        public final String name;

        HidePart(String name) {
            this.name = name;
        }

        public static final Codec<HidePart> CODEC = StringRepresentable.fromEnum(HidePart::values);

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
