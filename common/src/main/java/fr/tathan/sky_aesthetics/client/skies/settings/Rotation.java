package fr.tathan.sky_aesthetics.client.skies.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.Objects;

public record Rotation(Axis axis, String rotationType) {

    public static final Codec<Rotation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Axis.CODEC.fieldOf("axis").forGetter(Rotation::axis),
            Codec.STRING.fieldOf("rotation").forGetter(Rotation::rotationType)
    ).apply(instance, Rotation::new));

    public static Rotation createDefaultSettings() {
        return new Rotation(Axis.XP, "NIGHT");
    }

    public void rotatePoseStack(Matrix4f modelMatrix, float dayAngle){
        if(Objects.equals(rotationType, "NIGHT")){
            dayAngle = -dayAngle;
        }
        if(!Objects.equals(rotationType, "STATIC")) {
            modelMatrix.rotate(axis.toQuaternion(dayAngle));
        }
    }

    public enum Axis implements StringRepresentable {
        XP("XP"),
        YP("YP"),
        ZP("ZP");


        public final String name;

        Axis(String name) {
            this.name = name;
        }

        public static final Codec<Axis> CODEC = StringRepresentable.fromEnum(Axis::values);

        public Quaternionf toQuaternion(float rotation) {
            return switch (this) {
                case XP -> com.mojang.math.Axis.XN.rotationDegrees(rotation);
                case YP -> com.mojang.math.Axis.YN.rotationDegrees(rotation);
                case ZP -> com.mojang.math.Axis.ZN.rotationDegrees(rotation);
            };
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
