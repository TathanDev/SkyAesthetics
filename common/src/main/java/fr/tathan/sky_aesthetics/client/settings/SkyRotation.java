package fr.tathan.sky_aesthetics.client.settings;

import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.tathan.sky_aesthetics.client.SkyClock;
import net.minecraft.util.StringRepresentable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

/**
 * The class containing information about the rotation of a skybox layer
 * The final rotation is tilt * spin(axis, angle) * orientation
 * @param source What drives the angle
 * @param speed Multiplier on the source, in degrees per tick with GAME_TIME
 * @param offset Degrees added to the angle
 * @param axis The axis name (sun, XP, YP, ZP) or a vector
 * @param tilt Rotation applied around the spin, to tip the spin axis
 * @param orientation Rotation of the texture itself, inside the spin
 */
public record SkyRotation(Source source, float speed, float offset, Either<String, Vector3f> axis, Vector3f tilt, Vector3f orientation) {

    public static final List<String> AXIS_NAMES = List.of("sun", "XP", "YP", "ZP");

    public static Codec<String> AXIS_NAME = Codec.STRING.comapFlatMap((name) -> {
        for (String known : AXIS_NAMES) {
            if (known.equalsIgnoreCase(name)) return DataResult.success(known);
        }
        return DataResult.error(() -> "Unknown axis '" + name + "', expected one of " + AXIS_NAMES + " or [x, y, z]");
    }, (name) -> name);

    public static Codec<Vector3f> TILT = Codec.either(Codec.FLOAT, SkyObject.VEC3F).xmap(
            (either) -> either.map((degrees) -> new Vector3f(degrees, 0, 0), (vector3f) -> vector3f),
            (vector3f) -> vector3f.y == 0 && vector3f.z == 0 ? Either.left(vector3f.x) : Either.right(vector3f));

    public static final Codec<SkyRotation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Source.CODEC.optionalFieldOf("source", Source.SUN).forGetter(SkyRotation::source),
            Codec.FLOAT.optionalFieldOf("speed", 1.0f).forGetter(SkyRotation::speed),
            Codec.FLOAT.optionalFieldOf("offset", 0.0f).forGetter(SkyRotation::offset),
            Codec.either(AXIS_NAME, SkyObject.VEC3F).optionalFieldOf("axis", Either.left("sun")).forGetter(SkyRotation::axis),
            TILT.optionalFieldOf("tilt", new Vector3f()).forGetter(SkyRotation::tilt),
            SkyObject.VEC3F.optionalFieldOf("orientation", new Vector3f()).forGetter(SkyRotation::orientation)
    ).apply(instance, SkyRotation::new));

    /**
     * Convert the old sky_box rotation and dynamic_rotation so it renders exactly like before
     * The old static rotation was applied around the spin (like tilt) and the old axes turned the negative way
     * @param staticRotation The old sky_box rotation
     * @param dynamicRotation The old dynamic_rotation
     */
    public static SkyRotation fromLegacy(Vector3f staticRotation, Optional<Rotation> dynamicRotation) {
        if (dynamicRotation.isEmpty() || dynamicRotation.get().rotationType().equals("STATIC")) {
            return new SkyRotation(Source.STATIC, 1.0f, 0.0f, Either.left("sun"), new Vector3f(staticRotation), new Vector3f());
        }

        Rotation rotation = dynamicRotation.get();
        float speed = rotation.axis() == Rotation.Axis.SUN ? 1.0f : -1.0f;
        if (rotation.rotationType().equals("NIGHT")) {
            speed = -speed;
        }

        String axisName = rotation.axis() == Rotation.Axis.SUN ? "sun" : rotation.axis().name;
        return new SkyRotation(Source.SUN, speed, 0.0f, Either.left(axisName), new Vector3f(staticRotation), new Vector3f());
    }

    /** The sun axis is world Z, because the sun is drawn with YP(-90) then XP(angle) */
    public Vector3f axisVector() {
        return this.axis.map(name -> switch (name) {
            case "XP" -> new Vector3f(1, 0, 0);
            case "YP" -> new Vector3f(0, 1, 0);
            default -> new Vector3f(0, 0, 1);
        }, vector3f -> vector3f.lengthSquared() > 1.0e-6f ? new Vector3f(vector3f).normalize() : new Vector3f(0, 0, 1));
    }

    /** The spin angle in degrees */
    public float angleDegrees(SkyClock clock) {
        double angle = switch (this.source) {
            case SUN -> Math.toDegrees(clock.sunAngle()) * this.speed;
            case MOON -> Math.toDegrees(clock.moonAngle()) * this.speed;
            case STARS -> Math.toDegrees(clock.starAngle()) * this.speed;
            case DAY_TIME -> clock.dayTime() / SkyClock.DAY_LENGTH * 360.0 * this.speed;
            // The game time never stops growing, so we keep it small to avoid stuttering
            case GAME_TIME -> (clock.gameTime() * this.speed) % 360.0;
            case STATIC -> 0.0;
        };
        return (float) (angle + this.offset);
    }

    public Matrix4f matrix(SkyClock clock) {
        Matrix4f matrix = new Matrix4f();
        rotate(matrix, this.tilt);

        float angle = angleDegrees(clock);
        if (angle != 0.0f) {
            matrix.rotate(new Quaternionf().fromAxisAngleRad(axisVector(), (float) Math.toRadians(angle)));
        }

        rotate(matrix, this.orientation);
        return matrix;
    }

    private static void rotate(Matrix4f matrix, Vector3f rotation) {
        if (rotation.x != 0) matrix.rotate(Axis.XP.rotationDegrees(rotation.x));
        if (rotation.y != 0) matrix.rotate(Axis.YP.rotationDegrees(rotation.y));
        if (rotation.z != 0) matrix.rotate(Axis.ZP.rotationDegrees(rotation.z));
    }

    public enum Source implements StringRepresentable {
        SUN("sun"),
        MOON("moon"),
        STARS("stars"),
        DAY_TIME("day_time"),
        GAME_TIME("game_time"),
        STATIC("static");


        public final String name;

        Source(String name) {
            this.name = name;
        }

        public static final Codec<Source> CODEC = StringRepresentable.fromEnum(Source::values);

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
